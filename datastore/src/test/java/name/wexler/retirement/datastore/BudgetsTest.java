package name.wexler.retirement.datastore;

import name.wexler.retirement.jdbcDrivers.generic.JDBCDriverConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.sql.*;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class BudgetsTest {
    @Mock private JDBCDriverConnection conn;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    private Budgets budgets;

    @Test
    public void insertRow() {
        budgets.insertRow("income", Map.of(
                "id", (long) 42
        ));
        budgets.insertRow("expenses", Map.of(
                "id", (long) 42,
                "pid", (long) 41,
                "isIncome", true,
                "isExpense", false,
                "isTransfer", false,
                "amt", 10.00,
                "bgt", 20.00,
                "rbal", 30.00,
                "ex", true
        ));
    }

    @Test
    public void getBudgets() {
        Statement statement = Mockito.mock(Statement.class);
        try {
            when(connection.createStatement()).thenReturn(statement);
            ResultSet resultSet = Mockito.mock(ResultSet.class);
            when(statement.executeQuery(anyString())).thenReturn(resultSet);
            ResultSet rs = budgets.getBudgets();
            assertEquals(resultSet, rs);
        } catch (SQLException sqle) {
            assertFalse(sqle.getMessage(), true);
        }
    }

    @Test
    public void Budgets() {
        when(conn.tableExists("budgets")).thenReturn(false);
        Statement statement = Mockito.mock(Statement.class);
        try {
            when(connection.createStatement()).thenReturn(statement);
            budgets = new Budgets(conn);
        } catch (SQLException sqle) {
            assertFalse(sqle.getMessage(), true);
        }
    }

    @Before
    public void setUp() throws Exception {
        conn = Mockito.mock(JDBCDriverConnection.class);
        when(conn.tableExists("budgets")).thenReturn(true);
        connection = Mockito.mock(Connection.class);
        preparedStatement = Mockito.mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(conn.getConnection()).thenReturn(connection);
        budgets = new Budgets(conn);
    }
}