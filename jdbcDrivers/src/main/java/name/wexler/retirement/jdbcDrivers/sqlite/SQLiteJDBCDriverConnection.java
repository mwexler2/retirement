package name.wexler.retirement.jdbcDrivers.sqlite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import name.wexler.retirement.jdbcDrivers.generic.JDBCDriverConnection;
import org.sqlite.JDBC;

/**
 *
 * @author Mike Wexler
 */
public class SQLiteJDBCDriverConnection extends JDBCDriverConnection {
    public SQLiteJDBCDriverConnection(String database) {
        String url = _getDatabaseURL(database);

        try {
            Connection conn = DriverManager.getConnection(url);
            this.setConnection(conn);
            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }



    @Override public boolean tableExists(String tableName) {
        try {
            DatabaseMetaData md = getConnection().getMetaData();
            ResultSet rs = md.getTables(null, null, tableName, null);
            rs.next();
            return rs.getRow() > 0;
        } catch(SQLException se){
            System.out.println(se.getMessage());
        }
        return false;
    }


    private String _getDatabaseURL(String database) {
        String userHome = System.getProperty("user.home");
        String resourceDir = userHome + "/.retirement/history";
        String url = "jdbc:sqlite:" + resourceDir + "/"  + database + ".db";

        return url;
    }
}