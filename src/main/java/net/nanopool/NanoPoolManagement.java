/*
   Copyright 2008-2009 Christian Vest Hansen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.nanopool;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import net.nanopool.cas.ResizableCasArray;

/**
 *
 * @author cvh
 */
public class NanoPoolManagement implements NanoPoolManagementMBean {
    private final NanoPoolDataSource np;

    public NanoPoolManagement(DataSource np) {
        if (np == null) throw new NullPointerException(
                "DataSource parameter must be non-null and of type " +
                "net.nanopool.NanoPoolDataSource.");
        this.np = (NanoPoolDataSource)np;
    }

    public int getCurrentOpenConnectionsCount() {
        try {
            return FsmMixin.countOpenConnections(np.connectors);
        } catch (CasArrayOutdatedException _) {
            return getCurrentOpenConnectionsCount();
        }
    }

    public int getPoolSize() {
        return np.connectors.length();
    }

    public long getConnectionTimeToLive() {
        return np.state.ttl;
    }

    public String getContentionHandlerClassName() {
        try {
            return np.state.contentionHandler.getClass().getName();
        } catch (NullPointerException npe) {
            return "null";
        }
    }

    public String getContentionHandler() {
        return String.valueOf(np.state.contentionHandler);
    }

    public boolean isShutDown() {
        for (Connector cn : np.connectors) {
            if (cn == FsmMixin.shutdownMarker) return true;
        }
        return false;
    }

    public String shutDown() {
        List<SQLException> faults = np.shutdown();
        if (faults.size() > 0) {
            StringWriter sos = new StringWriter();
            PrintWriter pout = new PrintWriter(sos);
            for (SQLException ex : faults) {
                ex.printStackTrace(pout);
            }
            pout.close();
            return sos.toString();
        }
        return "Shutdown completed successfully.";
    }

    public String getSourceConnectionClassName() {
        try {
            return np.source.getClass().getName();
        } catch (NullPointerException npe) {
            return "null";
        }
    }

    public String getSourceConnection() {
        return String.valueOf(np.source);
    }

    public int getConnectionsCreated() {
        int createdCount = 0;
        Connector[] connectors = np.allConnectors;
        for (Connector cn : connectors) {
            if (cn != null) createdCount += cn.getRealConnectionsCreated();
        }
        return createdCount;
    }

    public int getConnectionsLeased() {
        int leasedCount = 0;
        Connector[] connectors = np.allConnectors;
        for (Connector cn : connectors) {
            if (cn != null) leasedCount += cn.getConnectionsLeased();
        }
        return leasedCount;
    }

    public void resetCounters() {
        Connector[] connectors = np.allConnectors;
        for (Connector cn : connectors) {
            if (cn != null) cn.resetCounters();
        }
    }

    public String listConnectionOwningThreadsStackTraces() {
        StringBuilder sb = new StringBuilder();
        Connector[] connectors = np.allConnectors;
        int i = -1;
        for (Connector cn : connectors) {
            i++;
            if (cn == null)
                continue;
            Thread owner = cn.getOwner();
            sb.append('[').append(i).append("] ").append(cn);
            if (owner == null) {
                sb.append(" is not currently owned by anyone.\n");
            } else {
                sb.append(" owned by " + owner.getName() + ":\n");
                StackTraceElement[] trace = owner.getStackTrace();
                if (trace.length == 0)
                    sb.append("    No stack trace available.\n");
                for (StackTraceElement frame : trace) {
                    sb.append("    ").append(frame).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public void dumpConnectionOwningThreadsStackTraces() {
        System.err.print(listConnectionOwningThreadsStackTraces());
    }

    public void resizePool(int newSize) {
        FsmMixin.resizePool(np, newSize);
    }

    public void interruptConnection(int id) {
        Connector cn = np.allConnectors[id];
        Thread owner = cn.getOwner();
        if (owner != null) {
            owner.interrupt();
        }
    }

    public void killConnection(int id) {
        Connector cn = np.allConnectors[id];
        Thread owner = cn.getOwner();
        if (owner != null) {
            owner.stop();
            try {
                cn.invalidate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            try {
                cn.returnToPool();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean isReziable() {
        return np.connectors instanceof ResizableCasArray;
    }
}