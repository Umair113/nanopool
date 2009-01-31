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
package net.nanopool.hooks;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.ConnectionPoolDataSource;

/**
 *
 * @author cvh
 */
public class CountingHook implements Hook {
    private final AtomicLong counter = new AtomicLong();

    public void run(EventType type, ConnectionPoolDataSource source, Connection con, SQLException sqle) {
        counter.incrementAndGet();
    }

    public long getCount() {
        return counter.get();
    }

    public void resetCount() {
        counter.set(0);
    }
}