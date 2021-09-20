package name.wexler.retirement.datastore;

import name.wexler.retirement.jdbcDrivers.generic.JDBCDriverConnection;

public class DataStore {
    JDBCDriverConnection conn = null;
    TickerHistory tickerHistory;
    TxnHistory txnHistory;
    Budgets budgets;
    AccountTable accountTable;
    PositionHistory positionHistory;

    public DataStore() {
        conn = JDBCDriverConnection.driverFactory("sqlite", "retirement");
        tickerHistory = new TickerHistory(conn);
        txnHistory = new TxnHistory(conn);
        budgets = new Budgets(conn);
        accountTable = new AccountTable(conn);
        positionHistory = new PositionHistory(conn);
    }

    public TickerHistory getTickerHistory() {
        return tickerHistory;
    }

    public TxnHistory getTxnHistory() { return txnHistory;}

    public Budgets getBudgets() { return budgets;}

    public AccountTable getAccountTable() { return accountTable; }

    public PositionHistory getPositionHistory() { return positionHistory; }
}
