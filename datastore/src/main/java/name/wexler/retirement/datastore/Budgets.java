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
                + " date int NOT NULL,\n"
                + " grouping TEXT NOT NULL,\n"
                + " st INTEGER,\n"
                + " period INTEGER,\n"
                + " ramt INTEGER,\n"
                + " isIncome BOOLEAN int NOT NULL,\n"
                + " isTransfer BOOLEAN int NOT NULL,\n"
                + " isExpense BOOLEAN int NOT NULL,\n"
                + " amt REAL,\n"
                + " pid INTEGER NOT NULL,\n"
                + " aamt INTEGER,\n"
                + " type INTEGER NOT NULL,\n"
                + " bgt REAL NOT NULL,\n"
                + " tbgt REAL,\n"
                + " rbal REAL NOT NULL,\n"
                + " ex BOOLEAN NOT NULL,\n"
                + " isLast BOOLEAN,\n"
                + " cat TEXT NOT NULL UNIQUE,\n"
                + " catName TEXT NOT NULL,\n"
                + " catTypeFilter TEXT NOT NULL,\n"
                + " parent TEXT NOT NULL\n"
                + ");\n";
        try (Statement stmt = conn.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull public
    void insertRow(@NotNull String grouping,
                   @NotNull Map<String, Object> line) {
        final String none = "none";

        String sql = "INSERT OR REPLACE INTO budgets \n"
                + "(st, ramt, isIncome, isTransfer, isExpense, amt, pid, type, bgt, " +
                "   rbal, ex, cat," +
                "   catName, id, catTypeFilter, parent, grouping," +
                "   period, aamt, tbgt, isLast, date" +
                ") \n" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?," +
                "          ?, ?, ?," +
                "          ?, ?, ?, ?, ?," +
                "          ?, ?, ?, ?, ?);\n";

        try (PreparedStatement pstmt = conn.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, getOptionalLong(line, "st"));
            pstmt.setLong(2, getOptionalLong(line, "ramt"));
            setOptionalBoolean(pstmt, 3, "isIncome", line);
            setOptionalBoolean(pstmt, 4, "isTransfer", line);
            setOptionalBoolean(pstmt, 5, "isIncome", line);

            setOptionalDouble(pstmt, 6, "amt", line);
            pstmt.setLong(7, getOptionalLong(line, "pid"));
            pstmt.setLong(8, getOptionalLong(line, "type"));
            setOptionalDouble(pstmt, 9, "bgt", line);
            setOptionalDouble(pstmt, 10, "rbal", line);
            setOptionalBoolean(pstmt, 11,"ex", line);
            pstmt.setString( 12, (String) line.get("cat"));
            pstmt.setString( 13, (String) line.get("catName"));
            pstmt.setLong( 14, (long) line.get("id"));
            pstmt.setString( 15, (String) line.get("catTypeFilter"));
            pstmt.setString( 16, (String) line.get("parent"));
            pstmt.setString(17, grouping);
            setOptionalLong(pstmt, 18, "period",  line);
            setOptionalDouble(pstmt, 19, "aamt", line);
            setOptionalDouble(pstmt, 20, "tbgt", line);
            setOptionalBoolean(pstmt, 21,"isLast", line);
            setOptionalLong(pstmt, 22,"date", line);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NumberFormatException nfe) {
            throw new RuntimeException(nfe);
        }
    }

    private void setOptionalBoolean(PreparedStatement pstmt,
                                    int index,  // Index of field in insert statement
                                    String name, // name of field in line
                                    Map<String, Object> line) throws SQLException {
        if (line.get(name) instanceof Boolean)
            pstmt.setBoolean(index, (Boolean) line.get(name));
        else
            pstmt.setNull(index, Types.BOOLEAN);
    }

    private void setOptionalLong(PreparedStatement pstmt,
                                    int index,  // Index of field in insert statement
                                    String name, // name of field in line
                                    Map<String, Object> line) throws SQLException {
        if (line.get(name) instanceof Long)
            pstmt.setLong(index, (Long) line.get(name));
        else
            pstmt.setNull(index, Types.INTEGER);
    }

    private void setOptionalDouble(PreparedStatement pstmt,
                                    int index,  // Index of field in insert statement
                                    String name, // name of field in line
                                    Map<String, Object> line) throws SQLException {
        if (line.get(name) instanceof Double)
            pstmt.setDouble(index, (Double) line.get(name));
        else
            pstmt.setNull(index, Types.DOUBLE);
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
                "SELECT date, st, ramt, isIncome, isTransfer, isExpense, " +
                        "amt, pid, type, bgt, rbal, ex,\n" +
                        "cat,catName,id,catTypeFilter," +
                        "CASE WHEN parent=\"Root\" THEN cat ELSE parent END AS parent, grouping,\n" +
                        "period, aamt, tbgt, isLast" +
                        " \n" +
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
