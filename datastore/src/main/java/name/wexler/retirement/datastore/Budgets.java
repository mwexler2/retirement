package name.wexler.retirement.datastore;

import name.wexler.retirement.jdbcDrivers.generic.JDBCDriverConnection;

import java.sql.*;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class Budgets {
    JDBCDriverConnection conn = null;
    public static final String source = "source";

    public Budgets(JDBCDriverConnection conn) {
        this.conn = conn;
        boolean exists = conn.tableExists("budgets");
        if (!exists) {
            createTable();
        }
    }

    private void createTable() {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE budgets (\n"
                + " id integer PRIMARY KEY,\n"
                + " grouping TEXT NOT NULL,\n"
                + " st INTEGER,\n"
                + " ramt INTEGER,\n"
                + " isIncome BOOLEAN int NOT NULL,\n"
                + " isTransfer BOOLEAN int NOT NULL,\n"
                + " isExpense BOOLEAN int NOT NULL,\n"
                + " amt REAL NOT NULL,\n"
                + " pid INTEGER NOT NULL,\n"
                + " type INTEGER NOT NULL,\n"
                + " bgt REAL NOT NULL,\n"
                + " rbal REAL NOT NULL,\n"
                + " ex BOOLEAN NOT NULL,\n"
                + " cat TEXT NOT NULL,\n"
                + " catName TEXT NOT NULL,\n"
                + " catTypeFilter TEXT NOT NULL,\n"
                + " parent TEXT NOT NULL\n"
                + ");\n";
        try (Statement stmt = conn.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @NotNull public
    void insertRow(@NotNull String grouping,
                   @NotNull Map<String, Object> line) {
        final String none = "none";

        String sql = "INSERT OR REPLACE INTO budgets \n"
                + "(st, ramt, isIncome, isTransfer, isExpense, amt, pid, type, bgt, " +
                "   rbal, ex, cat," +
                "   catName, id, catTypeFilter, parent, grouping) \n"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?," +
                "          ?, ?, ?," +
                "          ?, ?, ?, ?, ?);\n";

        try (PreparedStatement pstmt = conn.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, getOptionalLong(line, "st"));
            pstmt.setLong(2, getOptionalLong(line, "ramt"));
            if (line.get("isIncome") instanceof Boolean)
                pstmt.setBoolean( 3, (Boolean) line.get("isIncome"));
            else
                pstmt.setNull(3, Types.BOOLEAN);
            if (line.get("isTransfer") instanceof Boolean)
                pstmt.setBoolean( 4, (Boolean) line.get("isTransfer"));
            else
                pstmt.setNull(4, Types.BOOLEAN);
            if (line.get("isExpense") instanceof Boolean)
                pstmt.setBoolean( 5, (Boolean) line.get("isExpense"));
            else
                pstmt.setNull(5, Types.BOOLEAN);
            if (line.get("amt") instanceof Double)
                pstmt.setDouble( 6, (Double) line.get("amt"));
            else
                pstmt.setNull(6, Types.DOUBLE);
            pstmt.setLong(7, getOptionalLong(line, "pid"));
            pstmt.setLong(8, getOptionalLong(line, "type"));
            if (line.get("bgt") instanceof Double)
                pstmt.setDouble(9, (Double) line.get("bgt"));
            else
                pstmt.setNull(9, Types.DOUBLE);
            if (line.get("rbal") instanceof Double)
                pstmt.setDouble(10, (Double) line.get("rbal"));
            else
                pstmt.setNull(10, Types.DOUBLE);
            if (line.get("ex") instanceof Boolean)
                pstmt.setBoolean( 11, (Boolean) line.get("ex"));
            else
                pstmt.setNull(11, Types.BOOLEAN);
            pstmt.setString( 12, (String) line.get("cat"));
            pstmt.setString( 13, (String) line.get("catName"));
            pstmt.setLong( 14, (long) line.get("id"));
            pstmt.setString( 15, (String) line.get("catTypeFilter"));
            pstmt.setString( 16, (String) line.get("parent"));
            pstmt.setString(17, grouping);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage() + ": " + line.toString());
        } catch (NumberFormatException nfe) {
            System.out.println(nfe.getMessage() + ": " + line.toString());
        }
    }

    private @NotNull
    Long getOptionalLong(@NotNull Map<String, Object> line, @NotNull String field) {
        Object o = line.get(field);
        if (o instanceof Long) {
            return (Long) o;
        }
        return 0L;
    }

    public @NotNull ResultSet getBudgets() {
        String sql =
                "SELECT st, ramt, isIncome, isTransfer, isExpense, " +
                        "amt, pid, type, bgt, rbal, ex,\n" +
                        "cat,catName,id,catTypeFilter,parent, grouping \n" +
                        "FROM budgets \n";
        try {
            Statement stmt = conn.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql);
            return rs;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
