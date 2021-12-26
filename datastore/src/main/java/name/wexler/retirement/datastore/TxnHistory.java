package name.wexler.retirement.datastore;

import com.opencsv.CSVReaderHeaderAware;
import name.wexler.retirement.jdbcDrivers.generic.JDBCDriverConnection;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class TxnHistory {
    JDBCDriverConnection conn = null;
    private static final int initialTxns = 100;
    private static final int initialHistory = 1000;
    public static final String source = "source";
    public static final String txnId = "txnId";

    public TxnHistory(JDBCDriverConnection conn) {
        this.conn = conn;
        boolean exists = conn.tableExists("txnHistory");
        if (!exists) {
            createTable();
        }
    }

    private void createTable() {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE txnHistory (\n"
                + " id integer PRIMARY KEY,\n"
                + " source TEXT,\n"
                + " notes TEXT,\n"
                + " date int NOT NULL,\n"
                + " amount REAL NOT NULL,\n"
                + " account_name TEXT NOT NULL,\n"
                + " description TEXT,\n"
                + " original_description TEXT,\n"
                + " txn_type TEXT NOT NULL,\n"
                + " category TEXT NOT NULL,\n"
                + " labels TEXT,\n"
                + " symbol TEXT,\n"
                + " shares REAL,\n"
                + " fi TEXT,\n" 
                + " isBuy INTEGER,\n"
                + " isCheck INTEGER,\n"
                + " isChild INTEGER,\n"
                + " isDebit INTEGER,\n"
                + " isDuplicate INTEGER,\n"
                + " isEdited INTEGER,\n"
                + " isFirstDate INTEGER,\n"
                + " isLinkedToRule INTEGER,\n"
                + " isMatched INTEGER,\n"
                + " isPending INTEGER,\n"
                + " isPercent INTEGER,\n"
                + " isSell INTEGER,\n"
                + " isSpending INTEGER,\n"
                + " isTransfer INTEGER\n"
                + " txnId TEXT\n"
                + " maturity_date INTEGER\n"
                + ");\n";
        try (Statement stmt = conn.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertRow(Map<String, Object> line) {
        final String none = "none";

        String sql = "INSERT OR REPLACE INTO txnHistory \n"
                + "(date, description, original_description, amount, txn_type, category, account_name, labels, notes, " +
                "   symbol, shares, fi," +
                "   isBuy, isCheck, isChild, isDebit, isDuplicate, isEdited, isFirstDate, isLinkedToRule, isMatched, isPending, isPercent, isSell, isSpending, isTransfer," +
                "   source, txnId, maturity_date) \n"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?," +
                "          ?, ?, ?," +
                "          ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
                "          ?, ?, ?);\n";

        try (PreparedStatement pstmt = conn.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, getOptionalLong(line, "odate"));
            pstmt.setString(2, (String) line.get("merchant"));
            pstmt.setString(3, (String) line.get("omerchant"));
            if (line.get("amount") instanceof Double)
                pstmt.setDouble( 4, (Double) line.get("amount"));
            else
                pstmt.setNull(4, Types.DOUBLE);
            pstmt.setString(5, (String) line.get("txnType"));
            pstmt.setString(6, (String) line.get("category"));
            String accountName = (String) line.getOrDefault("account", none);
            if (accountName == null)
                accountName = none;
            pstmt.setString(7, accountName);
            pstmt.setString(8, line.get("labels").toString());
            pstmt.setString(9, (String) line.get("notes"));
            pstmt.setString(10, (String) line.get("symbol"));
            if (line.get("shares") instanceof Double)
                pstmt.setDouble(11, (Double) line.get("shares"));
            else
                pstmt.setNull(11, Types.DOUBLE);
            pstmt.setString(12, (String) line.get("fi"));
            if (line.get("isBuy") instanceof Boolean)
                pstmt.setBoolean( 13, (Boolean) line.get("isBuy"));
            else
                pstmt.setNull(13, Types.BOOLEAN);
            pstmt.setBoolean( 14, (Boolean) line.get("isCheck"));
            pstmt.setBoolean( 15, (Boolean) line.get("isChild"));
            pstmt.setBoolean( 16, (Boolean) line.get("isDebit"));
            pstmt.setBoolean( 17, (Boolean) line.get("isDuplicate"));
            pstmt.setBoolean( 18, (Boolean) line.get("isEdited"));
            pstmt.setBoolean( 19, (Boolean) line.get("isFirstDate"));
            pstmt.setBoolean( 20, (Boolean) line.get("isLinkedToRule"));
            pstmt.setBoolean( 21, (Boolean) line.get("isMatched"));
            pstmt.setBoolean( 22, (Boolean) line.get("isPending"));
            pstmt.setBoolean( 23, (Boolean) line.get("isPercent"));
            if (line.get("isSell") instanceof Boolean)
                pstmt.setBoolean( 24, (Boolean) line.get("isSell"));
            else
                pstmt.setNull(24, Types.BOOLEAN);
            pstmt.setBoolean( 25, (Boolean) line.get("isSpending"));
            pstmt.setBoolean( 26, (Boolean) line.get("isTransfer"));
            pstmt.setString( 27, (String) line.get(source));
            pstmt.setString(28, (String) line.get(TxnHistory.txnId));
            pstmt.setLong(29, getOptionalLong(line, "maturityDate"));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage() + ": " + line.toString());
        } catch (NumberFormatException nfe) {
            System.out.println(nfe.getMessage() + ": " + line.toString());
        }
    }

    private Long getOptionalLong(Map<String, Object> line, String field) {
        Object o = line.get(field);
        if (o instanceof Long) {
            return (Long) o;
        }
        return 0L;
    }

    public LocalDate getLastDate() {
        LocalDate result = null;

        String sql = "SELECT MAX(date), count(*)\n"
         + "FROM txnHistory\n";
        try (Statement stmt = conn.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();

            long lastDate = rs.getLong(1);
            if (lastDate != 0)
                return Instant.ofEpochMilli(lastDate).atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return LocalDate.ofEpochDay(0);
    }

    public @NotNull ResultSet getTransactions() {
        String sql =
                "SELECT txnHistory.id, date, " +
                        "description, " +
                        "original_description, " +
                        "amount, " +
                        "txn_type, " +
                        "itemType, " +
                        "IFNULL(cooked_category, txnHistory.category) AS category, " +
                        "TRIM(account_name) AS account_name, " +
                        "labels, " +
                        "notes, " +
                        "fi,\n" +
                        "isBuy,isCheck,isChild,isDebit,isDuplicate,isEdited,isFirstDate,isLinkedToRule," +
                        "isMatched,isPending,isPercent,isSell,isSpending," +
                        "txnHistory.isTransfer, \n" +
                        "symbol, shares, source, IFNULL(budgets.parent, categoryMapping.parentCategory) AS parent \n" +
                        "FROM txnHistory \n" +
                        "LEFT JOIN categoryMapping ON LOWER(categoryMapping.raw_category)=LOWER(txnHistory.category)\n" +
                        "LEFT JOIN budgets ON budgets.cat=txnHistory.category\n" +
                        "WHERE isDuplicate=0\n";
        try {
            Statement stmt = conn.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql);
            return rs;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
