package name.wexler.retirement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import name.wexler.retirement.CashFlow.Balance;
import name.wexler.retirement.CashFlow.CashFlowFrequency;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import name.wexler.retirement.CashFlow.Biweekly;
import name.wexler.retirement.CashFlow.Monthly;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
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
        Person mike = new Person(context, "mike", LocalDate.of(1984, Month.APRIL, 1), 45);
        Company yahoo = new Company(context, "yahoo");
        Job job1 = new Job(context, "job1", "yahoo", "mike");
        job1.setStartDate(LocalDate.of(2015, Month.APRIL, 1));
        job1.setEndDate(LocalDate.of(2015, Month.DECEMBER, 15));
        LocalDate job1FirstPeriodStart = LocalDate.of(2015, Month.APRIL, 1);
        LocalDate job1FirstPaycheck = LocalDate.of(2015, Month.APRIL, 2);
        Biweekly biweekly =
                new Biweekly(context, "job1CashFlowSource1", job1FirstPeriodStart, job1.getStartDate(), job1.getEndDate(), job1FirstPaycheck,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        Salary salary1 = new Salary(context, "salary1", "job1", biweekly.getId(),
                BigDecimal.valueOf(27000.00));

        Company bankOfNowhere = new Company(context, "bon1");
        Monthly liability1Monthly =
                new Monthly(context, "liability1CashFlowSource1",
                LocalDate.of(2015, Month.FEBRUARY, 1),
                LocalDate.of(2015, Month.FEBRUARY, 14),
                LocalDate.of(2045, Month.MARCH, 13),
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        String[] address = {"123 Mainstreet"};

        Entity[] mainBorrowers = {mike};
        List<Balance> interimBalances = Arrays.asList();
        Balance initialBalance = new Balance(LocalDate.of(2012, Month.JUNE, 10), BigDecimal.valueOf(100000.00));
        List<String> ownerIds = Arrays.asList(mike.getId());
        RealProperty mainStreet = new RealProperty(context, "main", ownerIds, initialBalance, address,
                "anyTown", "AnyCount", "AS", "00000", "US", interimBalances);
        String[] borrowers = {mike.getId()};

        Liability liability1 = new Liability(context, "liability1", bankOfNowhere.getId(), borrowers, mainStreet,
                LocalDate.of(2012, Month.JUNE, 20),
                LocalDate.of(2012, Month.JUNE, 21), 360, BigDecimal.valueOf(0.375), BigDecimal.valueOf(50000.00) ,
                BigDecimal.valueOf(200.00), BigDecimal.valueOf(42.35), liability1Monthly.getId());

        String[] is = {"salary1", "liability1"};
        String[] assets = {"main"};
        String[] liabilities = {"liability1"};
        Assumptions assumptions = new Assumptions();
        scenario1 = new Scenario(context, "scenario1", "Scenario 1", is, assets, liabilities, assumptions);
        scenario2 = new Scenario(context, "scenario2", "Scenario 2", is, assets, liabilities, assumptions);
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void getName() throws Exception {
        String name1 = scenario1.getName();
        assertEquals(name1, "Scenario 1");
        String name2 = scenario2.getName();
        assertEquals(name2, "Scenario 2");
    }


    @Test
    public void equals() throws Exception {
        assertNotEquals(scenario1, scenario2);
    }

    @Test
    public void serialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        ObjectWriter writer = mapper.writer();
        String scenario1Str = context.toJSON(scenario1);
        assertEquals("{\"id\":\"scenario1\",\"assumptions\":{\"longTermInvestmentReturn\":0.07,\"shortTermInvestmentReturn\":0.03,\"inflation\":0.04,\"yearsInShortTerm\":10},\"name\":\"Scenario 1\",\"cashFlowSources\":[\"salary1\",\"liability1\"],\"assets\":[\"main\"],\"liabilities\":[\"liability1\"]}", scenario1Str);

        String scenario2Str = context.toJSON(scenario2);
        assertEquals("{\"id\":\"scenario2\",\"assumptions\":{\"longTermInvestmentReturn\":0.07,\"shortTermInvestmentReturn\":0.03,\"inflation\":0.04,\"yearsInShortTerm\":10},\"name\":\"Scenario 2\",\"cashFlowSources\":[\"salary1\",\"liability1\"],\"assets\":[\"main\"],\"liabilities\":[\"liability1\"]}", scenario2Str);
    }


    @Test
    public void deserialize() throws Exception {
        String scenario1aStr = "{\"assumptions\":null,\"cashFlowSources\":[],\"name\":\"scenario1a\",\"expenseSources\":[],\"assets\":[],\"liabilities\":[]}";
        String scenario2aStr = "{\"assumptions\":null,\"cashFlowSources\":[],\"name\":\"scenario2a\",\"expenseSources\":[],\"assets\":[],\"liabilities\":[]}";

        Scenario scenario1a = context.fromJSON(Scenario.class, scenario1aStr);
        assertEquals("scenario1a", scenario1a.getName());

        Scenario sceanrio2a = context.fromJSON(Scenario.class, scenario2aStr);
        assertEquals("scenario2a", sceanrio2a.getName());
    }

}