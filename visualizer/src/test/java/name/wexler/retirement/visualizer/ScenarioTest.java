package name.wexler.retirement.visualizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import name.wexler.retirement.datastore.DataStore;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.Asset.RealProperty;
import name.wexler.retirement.visualizer.CashFlowFrequency.*;
import name.wexler.retirement.visualizer.CashFlowEstimator.*;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Entity.Person;
import name.wexler.retirement.visualizer.Expense.Expense;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sqlite.JDBC;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by mwexler on 8/13/16.
 */
public class ScenarioTest {
    private Scenario scenario1;
    private Scenario scenario2;
    private Context context;
    AccountReader accountReader = mock(AccountReader.class);

    @Before
    public void setUp() throws Exception {
        context = new Context(accountReader);
        context.setAssumptions(new Assumptions());

        scenario1 = new Scenario(context, accountReader, "scenario1", "Scenario 1",
                new String[] {}, new String[] {}, new String[] {}, new String[] {},
                context.getAssumptions());
        scenario2 = new Scenario(context, accountReader, "scenario2", "Scenario 2",
                new String[] {}, new String[] {}, new String[] {}, new String[] {},
                context.getAssumptions());
    }

    @After
    public void tearDown() {

    }

    @Test
    public void readScenarios() throws IOException {
        List<Scenario> scenarios = Scenario.readScenarios(context);
        assertEquals(1, scenarios.size());
    }

    @Test
    public void readAssets() throws IOException {
        List<Scenario> scenarios = Scenario.readScenarios(context);
        assertEquals(1, scenarios.size());
    }

    @Test
    public void getName() {
        String name1 = scenario1.getName();
        assertEquals("Scenario 1", name1);
    }


    @Test
    public void equals() {
        assertNotEquals(scenario1, scenario2);
    }


    @Test
    public void deserialize() throws Exception {
        String scenario1aStr = "{\"type\": \"scenario\", \"id\": \"s1a\", \"assumptions\":null,\"cashFlowSources\":[],\"name\":\"scenario1a\",\"expenseSources\":[],\"assets\":[],\"liabilities\":[],\"accounts\":[]}";
        String scenario2aStr = "{\"type\": \"scenario\", \"id\": \"s2a\", \"assumptions\":null,\"cashFlowSources\":[],\"name\":\"scenario2a\",\"expenseSources\":[],\"assets\":[],\"liabilities\":[],\"accounts\":[]}";

        // Note, each Scenario requires a different context because things like budgets might have same ids but different values.
        Context context1 = new Context(accountReader);
        Scenario scenario1a = context1.fromJSON(Scenario.class, scenario1aStr);
        assertEquals("scenario1a", scenario1a.getName());

        Context context2 = new Context(accountReader);
        Scenario sceanrio2a = context2.fromJSON(Scenario.class, scenario2aStr);
        assertEquals("scenario2a", sceanrio2a.getName());
    }

}