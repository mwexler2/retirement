package name.wexler.retirement.datastore;

import name.wexler.retirement.jdbcDrivers.generic.JDBCDriverConnection;

public class DataStore {
    JDBCDriverConnection conn = null;
    TickerHistory tickerHistory;
    TxnHistory txnHistory;
    AccountTable accountTable;
    PositionHistory positionHistory;

    public DataStore() {
        conn = JDBCDriverConnection.driverFactory("sqlite", "retirement");
        tickerHistory = new TickerHistory(conn);
        txnHistory = new TxnHistory(conn);
        accountTable = new AccountTable(conn);
        positionHistory = new PositionHistory(conn);
    }

    public TickerHistory getTickerHistory() {
        return tickerHistory;
    }

    public TxnHistory getTxnHistory() { return txnHistory;}

    public AccountTable getAccountTable() { return accountTable; }

    public PositionHistory getPositionHistory() { return positionHistory; }
}
