package name.wexler.retirement.datastore;

import name.wexler.retirement.jdbcDrivers.generic.JDBCDriverConnection;

import java.sql.*;
import java.time.*;
import java.util.Map;

public class AccountTable {
    JDBCDriverConnection conn = null;

    public AccountTable(JDBCDriverConnection conn) {
        this.conn = conn;
        boolean exists = conn.tableExists("accounts");
        if (!exists) {
            createTable();
        }
    }

    private void createTable() {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE accounts (\n"
                + " linkedAccountId TEXT,\n"
                + " addAccountDate INTEGER,\n"
                + " fiLoginDisplayName TEXT,\n"
                + " dueDate DATE,\n"
                + " isTerminal INTEGER,\n"
                + " linkCreationTime DATETIME,\n"
                + " isActive INTEGER,\n"
                + " lastUpdated INTEGER,\n"
                + " rateType INTEGER,\n"
                + " fiName TEXT ,\n"
                + " origAmount REAL,\n"
                + " klass TEXT ,\n"
                + " accountTypeInt INTEGER,\n"
                + " id INTEGER PRIMARY KEY,\n"
                + " fiLoginId INTEGER,\n"
                + " accountType TEXT ,\n"
                + " currentBalance REAL,\n"
                + " fiLoginStatus TEXT ,\n"
                + " origDate DATE ,\n"
                + " linkStatus TEXT,\n"
                + " accountId INTEGER,\n"
                + " yodleeAccountId INTEGER,\n"
                + " name  TEXT,\n"
                + " status INTEGER,\n"
                + " accountName TEXT,\n"
                + " ccAggrStatus INTEGER,\n"
                + " exclusionType  INTEGER,\n"
                + " linkedAccount TEXT,\n"
                + " isHiddenFromPlanningTrends INTEGER,\n"
                + " accountStatus INTEGER,\n"
                + " accountSystemStatus TEXT ,\n"
                + " fiLastUpdated INTEGER,\n"
                + " yodleeAccountNumberLast4 TEXT,\n"
                + " isError INTEGER,\n"
                + " isAccountNotFound INTEGER,\n"
                + " rate REAL,\n"
                + " lastUpdatedInString TEXT,\n"
                + " currency  TEXT,\n"
                + " term INTEGER,\n"
                + " isHostAccount INTEGER,\n"
                + " value REAL,\n"
                + " usageType TEXT,\n"
                + " interestRate REAL,\n"
                + " isAccountClosedByMint INTEGER,\n"
                + " userName TEXT,\n"
                + " yodleeName  TEXT,\n"
                + " closeDate INTEGER,\n"
                + " dueAmt REAL,\n"
                + " amountDue REAL,\n"
                + " isClosed INTEGER,\n"
                + " fiLoginUIStatus TEXT,\n"
                + " addAccountDateInDate DATE,\n"
                + " closeDateInDate DATE,\n"
                + " fiLastUpdatedInDate DATE,\n"
                + " lastUpdatedInDate DATE,\n"
                + " availableMoney REAL,\n"
                + " totalFees REAL,\n"
                + " totalCredit REAL,\n"
                + " nextPaymentAmount REAL,\n"
                + " nextPaymentDate DATE "
                + ");\n";
        try (Statement stmt = conn.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteAllRows() {
        String sql = "DELETE FROM accounts";
        try {
            Statement stmt = conn.getConnection().createStatement();
            stmt.execute(sql);
        } catch (SQLException se) {
            System.err.println(se);
        }
    }

    public void insertRow(Map<String, Object> line) {
        String sql = "INSERT INTO accounts (\n"
                + " addAccountDate, fiLoginDisplayName, dueDate, isTerminal, \n"
                + " isActive, lastUpdated, rateType, fiName, origAmount, klass, accountTypeInt, id, fiLoginId, \n"
                + " accountType, currentBalance, fiLoginStatus, origDate, linkStatus, accountId, \n"
                + " yodleeAccountId, name, status, accountName, ccAggrStatus, exclusionType, \n"
                + " isHiddenFromPlanningTrends, accountStatus, accountSystemStatus, fiLastUpdated, yodleeAccountNumberLast4, isError, isAccountNotFound, rate, \n"
                + " lastUpdatedInString, currency, term, isHostAccount, value, usageType, \n"
                + " interestRate, isAccountClosedByMint, userName, yodleeName, closeDate, dueAmt, amountDue,\n"
                + " isClosed, fiLoginUIStatus, addAccountDateInDate, closeDateInDate, fiLastUpdatedInDate, lastUpdatedInDate, \n"
                + " availableMoney, totalFees, totalCredit, nextPaymentAmount, nextPaymentDate) \n"
                + " VALUES (?, ?, ?, ?, " +
                "           ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                "           ?, ?, ?, ?, ?, " +
                "           ?, ?, ?, ?, ?, ?, ?, " +
                "           ?, ?, ?, ?, ?, ?, ?, ?," +
                "           ?, ?, ?, ?, ?, ?, " +
                "           ?, ?, ?, ?, ?, ?, ?, " +
                "           ?, ?, ?, ?, ?, ?, " +
                "           ?, ?, ?, ?, ?" +
                ");\n";

        try (PreparedStatement pstmt = conn.getConnection().prepareStatement(sql)) {
            pstmt.setLong   ( 1, (Long)    line.get("addAccountDate"));
            pstmt.setString ( 2, (String)  line.get("fiLoginDisplayName"));
            pstmt.setString ( 3, (String)  line.get("dueDate"));
            pstmt.setBoolean( 4, (Boolean) line.get("isTerminal"));
            pstmt.setBoolean( 5, (Boolean) line.get("isActive"));
            pstmt.setLong   ( 6, (Long)    line.get("lastUpdated"));
            if (line.get("rateType") instanceof Long)
                pstmt.setLong   ( 7, (Long)    line.get("rateType"));
            else
                pstmt.setNull(7, Types.BIGINT);
            pstmt.setString ( 8, (String)  line.get("fiName"));
            if (line.get("origAmount") instanceof Double)
                pstmt.setDouble (9, (Double)  line.get("origAmount"));
            else
                pstmt.setNull(9, Types.BIGINT);
            pstmt.setString (10, (String)  line.get("klass"));
            pstmt.setLong   (11, (Long)    line.get("accountTypeInt"));
            pstmt.setLong   (12, (Long)    line.get("id"));
            pstmt.setLong   (13, (Long)   line.get("fiLoginId"));
            pstmt.setString (14, (String)  line.get("accountType"));
            if (line.get("currentBalance") instanceof Double)
                pstmt.setDouble (15, (Double)  line.get("currentBalance"));
            else
                pstmt.setNull(15, Types.BIGINT);
            pstmt.setString (16, (String)  line.get("fiLoginStatus"));
            pstmt.setString (17, (String)  line.get("origDate"));
            pstmt.setString (18, (String)  line.get("linkStatus"));
            pstmt.setLong   (19, (Long)    line.get("accountId"));
            if (line.get("yodleeAccountId") instanceof Long)
                pstmt.setLong   (20, (Long)    line.get("yodleeAccountId"));
            else
                pstmt.setNull(20, Types.BIGINT);
            pstmt.setString (21, (String)  line.get("name"));
            pstmt.setString (22, (String)  line.get("status"));
            pstmt.setString (23, (String)  line.get("accountName"));
            if (line.get("ccAggrStatus") instanceof Long)
                pstmt.setLong   (24, (Long)    line.get("ccAggrStatus"));
            else
                pstmt.setNull(24, Types.BIGINT);
            pstmt.setString (25, (String)  line.get("exclusionType"));
            pstmt.setBoolean(26, (Boolean) line.get("isHiddenFromPlanningTrends"));
            pstmt.setString (27, (String)  line.get("accountStatus"));
            pstmt.setString (28, (String)  line.get("accountSystemStatus"));
            pstmt.setLong   (29, (Long)    line.get("fiLastUpdated"));
            pstmt.setString (30, (String)  line.get("yodleeAccountNumberLast4"));
            pstmt.setBoolean(31, (Boolean) line.get("isError"));
            pstmt.setBoolean(32, (Boolean) line.get("isAccountNotFound"));
            if (line.get("rate") instanceof Double)
                pstmt.setDouble (33, (Double)  line.get("rate"));
            else
                pstmt.setNull(33, Types.REAL);
            pstmt.setString (34, (String)  line.get("lastUpdatedInString"));
            pstmt.setString (35, (String)  line.get("currency"));
            if (line.get("term") instanceof Integer)
                pstmt.setLong    (36, (Long) line.get("term"));
            else
                pstmt.setNull(36, Types.BIGINT);
            pstmt.setBoolean(37, (Boolean) line.get("isHostAccount"));
            if (line.get("value") instanceof Double)
                pstmt.setDouble (38, (Double)  line.get("value"));
            else if (line.get("interestRate") instanceof Long)
                pstmt.setLong (38, (Long)  line.get("value"));
            else
                pstmt.setNull(38, Types.REAL);
            pstmt.setString (39, (String)  line.get("usageType"));
            if (line.get("interestRate") instanceof Double)
                pstmt.setDouble (40, (Double)  line.get("interestRate"));
            else if (line.get("interestRate") instanceof Long)
                pstmt.setLong (40, (Long)  line.get("interestRate"));
            else
                pstmt.setNull(40, Types.REAL);
            pstmt.setBoolean(41, (Boolean) line.get("isAccountClosedByMint"));
            pstmt.setString (42, (String)  line.get("userName"));
            pstmt.setString (43, (String)  line.get("yodleeName"));
            if (line.get("closeDate") instanceof Long)
                pstmt.setLong   (44, (Long)    line.get("closeDate"));
            else
                pstmt.setNull(44, Types.BIGINT);
            if (line.get("dueAmt") instanceof Long)
                pstmt.setLong   (45, (Long)  line.get("dueAmt"));
            else
                pstmt.setNull(45, Types.BIGINT);
            if (line.get("amountDue") instanceof Double)
                pstmt.setDouble (46, (Double)  line.get("amountDue"));
            else
                pstmt.setNull(46, Types.REAL);
            pstmt.setBoolean(47, (Boolean) line.get("isClosed"));
            pstmt.setString (48, (String)  line.get("fiLoginUIStatus"));
            pstmt.setString (49, (String)  line.get("addAccountDateInDate"));
            pstmt.setString (50, (String)  line.get("closeDateInDate"));
            pstmt.setString (51, (String)  line.get("fiLastUpdatedInDate"));
            pstmt.setString (52, (String)  line.get("lastUpdatedInDate"));
            if (line.get("availableMoney") instanceof Double)
                pstmt.setDouble (53, (Double)  line.get("availableMoney"));
            else
                pstmt.setNull(53, Types.REAL);
            if (line.get("totalFees") instanceof Double)
                pstmt.setDouble (54, (Double)  line.get("totalFees"));
            else
                pstmt.setNull(54, Types.REAL);
            if (line.get("totalCredit") instanceof Double)
                pstmt.setDouble (55, (Double)  line.get("totalCredit"));
            else
                pstmt.setNull(55, Types.REAL);
            if (line.get("nextPaymentAmount") instanceof Double)
                pstmt.setDouble (56, (Double)  line.get("nextPaymentAmount"));
            else
                pstmt.setNull(56, Types.REAL);
            if (line.get("nextPaymentDate") instanceof String)
                pstmt.setString (57, (String)  line.get("nextPaymentDate"));
            else
                pstmt.setNull(57, Types.REAL);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException nfe) {
            System.out.println(nfe.getMessage());
        }
    }

    public ResultSet getAccounts() {
        String sql =
                "SELECT name, accountName, userName, yodleeName, value " +
                        "FROM accounts \n";
        try {
            Statement stmt = conn.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql);
            return rs;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}
