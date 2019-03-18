package name.wexler.retirement.jdbcDrivers.generic;

import name.wexler.retirement.jdbcDrivers.sqlite.SQLiteJDBCDriverConnection;

import java.sql.Connection;
import java.util.List;

public abstract class JDBCDriverConnection {
    Connection conn;

    protected JDBCDriverConnection() {

    }

    public static JDBCDriverConnection driverFactory(String type, String database) {
        if (type.equals("sqlite")) {
            return new SQLiteJDBCDriverConnection(database);
        }
        throw new NoClassDefFoundError("Can't find driver for " + type);
    }

    public Connection getConnection() {
        return conn;
    }

    protected void  setConnection(Connection conn) {
        this.conn = conn;
    }

    public abstract boolean tableExists(String tableName);
}
