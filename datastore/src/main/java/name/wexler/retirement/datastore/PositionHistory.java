package name.wexler.retirement.datastore;

import name.wexler.retirement.jdbcDrivers.generic.JDBCDriverConnection;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PositionHistory {
    public class Position {
        public String getName() {
            return name;
        }

        public Date getDate() {
            return date;
        }

        public BigDecimal getUnits() {
            return units;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal getMktValue() {
            return mktValue;
        }

        private final String name;
        private final Date date;
        private final BigDecimal units;
        private final BigDecimal unitPrice;
        private final BigDecimal mktValue;

        public Position(String name, Date date, BigDecimal units, BigDecimal unitPrice, BigDecimal mktValue) {
            this.name = name;
            this.date = date;
            this.units = units;
            this.unitPrice = unitPrice;
            this.mktValue = mktValue;
        }
    }
    
    JDBCDriverConnection conn = null;

    public PositionHistory(JDBCDriverConnection conn) {
        this.conn = conn;
        boolean exists = conn.tableExists("positionHistory");
        if (!exists) {
            createTable();
        }
    }

    private void createTable() {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE positionHistory (\n"
                + " id integer PRIMARY KEY,\n"
                + " name text NOT NULL,\n"
                + " account_id NOT NULL,"
                + " date DATE,\n"
                + " units REAL, \n"
                + " pos_type text not null,\n"
                + " unit_price REAL,\n"
                + " mkt_value REAL\n"
                + ");\n";
        try (Statement stmt = conn.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteAllRows() {
        String sql = "DELETE FROM positionHistory";
        try {
            Statement stmt = conn.getConnection().createStatement();
            stmt.execute(sql);
        } catch (SQLException se) {
            System.err.println(se);
        }
    }

    public void insertRow(Date date, String ticker, String accountId, BigDecimal units, String posType, BigDecimal unitPrice, BigDecimal mktValue) {
        String sql = "INSERT INTO positionHistory \n"
                + "(account_id, name, date, units, pos_type, unit_price, mkt_value) \n"
                + "VALUES (?, ?, ?, ?, ?, ?, ?);\n";

        try (PreparedStatement pstmt = conn.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            pstmt.setString(2, ticker);
            pstmt.setString(3, date.toString());
            pstmt.setBigDecimal(4, units);
            pstmt.setString(5, posType);
            pstmt.setBigDecimal(6, unitPrice);
            pstmt.setBigDecimal(7, mktValue);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException nfe) {
            System.out.println(nfe.getMessage());
        }
    }

    public Map<String, Position> getAccountPositions(String accountId) {
        Map<String, Position> positions = new HashMap<>();

        String sql = "SELECT name, date, units, pos_type, unit_price, mkt_value\n"
                + "FROM positionHistory\n"
                + "WHERE account_id=?";
        try (PreparedStatement pstmt = conn.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    Position position = new Position(
                            name,
                            rs.getDate("date"),
                            rs.getBigDecimal("units"),
                            rs.getBigDecimal("unit_price"),
                            rs.getBigDecimal("mkt_value"));
                    positions.put(name, position);
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return positions;
    }
}
