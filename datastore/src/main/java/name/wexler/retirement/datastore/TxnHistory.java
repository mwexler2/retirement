package name.wexler.retirement.datastore;

import com.opencsv.CSVReaderHeaderAware;
import name.wexler.retirement.jdbcDrivers.generic.JDBCDriverConnection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class TxnHistory {
    JDBCDriverConnection conn = null;
    private static final int initialTxns = 100;
    private static final int initialHistory = 1000;

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
                + " notes TEXT,\n"
                + " date DATE NOT NULL,\n"
                + " amount REAL NOT NULL,\n"
                + " account_name TEXT NOT NULL,\n"
                + " description TEXT,\n"
                + " original_description TEXT,\n"
                + " txn_type TEXT NOT NULL,\n"
                + " category TEXT NOT NULL,\n"
                + " labels TEXT\n"
                + ");\n";
        try (Statement stmt = conn.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertRow(Map<String, Object> line) {
        String sql = "INSERT INTO txnHistory \n"
                + "(date, description, original_description, amount, txn_type, category, account_name, labels, notes) \n"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);\n";

        try (PreparedStatement pstmt = conn.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, (String) line.get("date"));
            pstmt.setString(2, (String) line.get("description"));
            pstmt.setString(3, (String) line.get("original_description"));
            pstmt.setDouble(4, (Double) line.get("amount"));
            pstmt.setString(5, (String) line.get("txn_type"));
            pstmt.setString(6, (String) line.get("category"));
            pstmt.setString(7, (String) line.get("account_name"));
            pstmt.setString(8, (String) line.get("labels"));
            pstmt.setString(9, (String) line.get("notes"));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException nfe) {
            System.out.println(nfe.getMessage());
        }
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
}
