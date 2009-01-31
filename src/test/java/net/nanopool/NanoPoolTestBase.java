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

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;
import org.junit.Before;

/**
 * Abstract base class for NanoPoolDataSource test cases.
 * @author cvh
 */
public abstract class NanoPoolTestBase {
    NanoPoolDataSource npds;

    @Before
    public void setUp() throws SQLException {
        ConnectionPoolDataSource source = buildCpds();
        Configuration config = buildConfig();
        npds = buildNpds(source, config);
    }

    protected ConnectionPoolDataSource buildCpds() throws SQLException {
        MysqlConnectionPoolDataSource source =
            new MysqlConnectionPoolDataSource();
        source.setServerName("localhost");
        source.setPort(3306);
        source.setDatabaseName("test");
        source.setUser("root");
        source.setPassword("");
        
        // timeouts:
        source.setLoginTimeout(5 /*seconds*/);
        source.setConnectTimeout(5000 /*milliseconds*/);
        source.setSocketTimeout(5000 /*milliseconds*/);

        return source;
    }

    protected Configuration buildConfig() {
        return new Configuration().setPoolSize(10).setTimeToLive(300000);
    }

    protected NanoPoolDataSource buildNpds(ConnectionPoolDataSource source,
            Configuration config) {
        return new NanoPoolDataSource(source, config);
    }
}