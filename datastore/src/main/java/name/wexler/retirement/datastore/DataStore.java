package name.wexler.retirement.datastore;

import name.wexler.retirement.jdbcDrivers.generic.JDBCDriverConnection;

public class DataStore {
    JDBCDriverConnection conn = null;
    TickerHistory tickerHistory;

    public DataStore() {
        conn = JDBCDriverConnection.driverFactory("sqlite", "retirement");
        tickerHistory = new TickerHistory(conn);
    }

    public TickerHistory getTickerHistory() {
        return tickerHistory;
    }
}
