package name.wexler.retirement.datastore;

import com.opencsv.CSVReaderHeaderAware;
import name.wexler.retirement.jdbcDrivers.generic.JDBCDriverConnection;
import org.springframework.cglib.core.Local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TickerHistory {
    JDBCDriverConnection conn = null;
    private static final int initialTickers = 100;
    private static final int initialHistory = 1000;

    public TickerHistory(JDBCDriverConnection conn) {
        this.conn = conn;
        boolean exists = conn.tableExists("tickerHistory");
        if (!exists) {
            createTable();
            populateTable();
        }
    }

    private void createTable() {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE tickerHistory (\n"
                + " id integer PRIMARY KEY,\n"
                + " name text NOT NULL,\n"
                + " date DATE,\n"
                + " open REAL\n"
                + " close REAL,\n"
                + " low REAL,\n"
                + " high REAL,\n"
                + " adjClose REAL,\n"
                + " volume integer\n"
                + ");\n";
        try (Statement stmt = conn.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void populateTable() {
        File dir = new File(_getTickerDirectory());
        File[] children = dir.listFiles();
        for (File child : children) {
            if (child.getName().matches("[A-Z]+.csv")) {
                populateTableFromFile(child.getAbsoluteFile());
            }
        }
    }

    private void populateTableFromFile(File csvFile) {
        String ticker = csvFile.toString().replace(".csv", "");
        ticker = ticker.substring(ticker.lastIndexOf('/') + 1);

        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(csvFile))) {
            Map<String, String> line;
            while ((line = reader.readMap()) != null) {
                if (!line.containsKey("Date"))
                    continue;
                if (line.get("Open") == null || line.get("Open").equals("null")) {
                    continue;
                }
                insertRow(ticker, line);
            }
        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public void insertRow(String ticker, Map<String, String> line) {
        String sql = "INSERT INTO tickerHistory \n"
                + "(name, date, open, close, low, high, adjClose, volume) \n"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?);\n";

        try (PreparedStatement pstmt = conn.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, ticker);
            pstmt.setString(2, line.get("Date"));
            pstmt.setDouble(3, Double.parseDouble(line.get("Open")));
            pstmt.setDouble(4, Double.parseDouble(line.get("Close")));
            pstmt.setDouble(5, Double.parseDouble(line.get("Low")));
            pstmt.setDouble(6, Double.parseDouble(line.get("High")));
            pstmt.setDouble(7, Double.parseDouble(line.get("Adj Close")));
            pstmt.setDouble(8, Integer.parseInt(line.get("Volume")));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException nfe) {
            System.out.println(nfe.getMessage());
        }
    }

    public Map<String, LocalDate> getTickers() {
        Map<String, LocalDate> result = new HashMap<>(initialTickers);

        String sql = "SELECT name, MAX(date), count(*)\n"
         + "FROM tickerHistory\n"
         + " GROUP BY name;\n";
        try (Statement stmt = conn.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String ticker = rs.getString(1);
                String lastDateStr = rs.getString(2);
                LocalDate lastDate = LocalDate.parse(lastDateStr);
                result.put(ticker, lastDate);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public Map<LocalDate, BigDecimal> getHistoricalPrices(String ticker) {
        Map<LocalDate, BigDecimal> historicalPrices = new HashMap<>(initialHistory);

        String sql = "SELECT date, close\n"
                + "FROM tickerHistory\n"
                + "WHERE name=?\n"
                + "AND   date>'2008-01-01';\n";
        try (PreparedStatement pstmt = conn.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, ticker);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dateStr = rs.getString(1);
                    LocalDate date = LocalDate.parse(dateStr);
                    BigDecimal close = rs.getBigDecimal(2);
                    historicalPrices.put(date, close);
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return historicalPrices;
    }

    private String _getTickerDirectory() {
        String userHome = System.getProperty("user.home");
        String resourceDir = userHome + "/.retirement/history";

        return resourceDir;
    }
}
