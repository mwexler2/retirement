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

    @Before
    public void setUp() throws Exception {
        context = new Context();
        context.setAssumptions(new Assumptions());


        Person.readPeople(context);
        Expense.readExpenses(context);
        Company.readCompanies(context);
        AssetAccount.readAssetAccounts(context);
        Job.readJobs(context);
        CashFlowFrequency.readCashFlowFrequencies(context);
        try {
            DriverManager.registerDriver(new JDBC());
        } catch (SQLException var1) {
            var1.printStackTrace();
        }
        DataStore ds = new DataStore();
        Security.readSecurities(context, ds);
        CashFlowEstimator.readCashFlowSources(context);
        Asset.readAssets(context);

        List<Scenario> scenarios = Scenario.readScenarios(context);
        scenario1 = scenarios.get(0);
    }

    @After
    public void tearDown() {

    }


    @Test
    public void getName() {
        String name1 = scenario1.getName();
        assertEquals("Go to Amazon", name1);
    }


    @Test
    public void equals() {
        assertNotEquals(scenario1, scenario2);
    }


    @Test
    public void deserialize() throws Exception {
        String scenario1aStr = "{\"type\": \"scenario\", \"id\": \"s1a\", \"assumptions\":null,\"cashFlowSources\":[],\"name\":\"scenario1a\",\"expenseSources\":[],\"assets\":[],\"liabilities\":[],\"accounts\":[]}";
        String scenario2aStr = "{\"type\": \"scenario\", \"id\": \"s2a\", \"assumptions\":null,\"cashFlowSources\":[],\"name\":\"scenario2a\",\"expenseSources\":[],\"assets\":[],\"liabilities\":[],\"accounts\":[]}";

        Scenario scenario1a = context.fromJSON(Scenario.class, scenario1aStr);
        assertEquals("scenario1a", scenario1a.getName());

        Scenario sceanrio2a = context.fromJSON(Scenario.class, scenario2aStr);
        assertEquals("scenario2a", sceanrio2a.getName());
    }

}