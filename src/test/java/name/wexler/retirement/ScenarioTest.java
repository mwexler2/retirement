package name.wexler.retirement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import name.wexler.retirement.CashFlow.Balance;
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
        Person mike = new Person(context, "mike");
        Company yahoo = new Company(context, "yahoo");
        Job job1 = new Job(context, "job1", "yahoo", "mike");
        job1.setStartDate(LocalDate.of(2015, Month.APRIL, 1));
        job1.setEndDate(LocalDate.of(2015, Month.DECEMBER, 15));
        LocalDate job1FirstPeriodStart = LocalDate.of(2015, Month.APRIL, 1);
        LocalDate job1FirstPaycheck = LocalDate.of(2015, Month.APRIL, 2);
        Biweekly biweekly = new Biweekly(context, "job1CashFlowSource1", job1FirstPeriodStart, job1.getStartDate(), job1.getEndDate(), job1FirstPaycheck);
        Salary salary1 = new Salary(context, "salary1", "job1", biweekly.getId());
        salary1.setBaseAnnualSalary(BigDecimal.valueOf(27000.00));
        Company bankOfNowhere = new Company(context, "bon1");
        Monthly liability1Monthly = new Monthly(context, "liability1CashFlowSource1",
                LocalDate.of(2015, Month.FEBRUARY, 1),
                LocalDate.of(2015, Month.FEBRUARY, 14),
                LocalDate.of(2045, Month.MARCH, 13));
        String[] address = {"123 Mainstreet"};

        Entity[] mainBorrowers = {mike};
        List<Balance> interimBalances = Arrays.asList();
        RealProperty mainStreet = new RealProperty(context, "main", mike.getId(), BigDecimal.valueOf(100000.00), LocalDate.of(2012, Month.JUNE, 10), address,
                "anyTown", "AnyCount", "AS", "00000", "US", interimBalances);
        String[] borrowers = {mike.getId()};

        Liability liability1 = new Liability(context, "liability1", bankOfNowhere.getId(), borrowers, mainStreet,
                LocalDate.of(2012, Month.JUNE, 20),
                LocalDate.of(2012, Month.JUNE, 21), 360, BigDecimal.valueOf(0.375), BigDecimal.valueOf(50000.00) ,
                BigDecimal.valueOf(200.00), liability1Monthly.getId());
        liability1.setPaymentAmount(BigDecimal.valueOf(273.99));

        liability1.setCashFlow(liability1Monthly);
        String[] is = {"salary1"};
        String[] es = {"liability1"};
        String[] assets = {"main"};
        String[] liabilities = {"liability1"};
        Assumptions assumptions = new Assumptions();
        scenario1 = new Scenario(context, "scenario1", is, es, assets, liabilities, assumptions);
        scenario2 = new Scenario(context, "scenario2", is, es, assets, liabilities, assumptions);
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void getName() throws Exception {
        String name1 = scenario1.getName();
        assertEquals(name1, "scenario1");
        String name2 = scenario2.getName();
        assertEquals(name2, "scenario2");
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
        assertEquals("{\"assumptions\":{\"longTermInvestmentReturn\":0.07,\"shortTermInvestmentReturn\":0.03,\"inflation\":0.04,\"yearsInShortTerm\":10},\"name\":\"scenario1\",\"incomeSources\":[\"salary1\"],\"expenseSources\":[\"liability1\"],\"assets\":[\"main\"],\"liabilities\":[\"liability1\"]}", scenario1Str);

        String scenario2Str = context.toJSON(scenario2);
        assertEquals("{\"assumptions\":{\"longTermInvestmentReturn\":0.07,\"shortTermInvestmentReturn\":0.03,\"inflation\":0.04,\"yearsInShortTerm\":10},\"name\":\"scenario2\",\"incomeSources\":[\"salary1\"],\"expenseSources\":[\"liability1\"],\"assets\":[\"main\"],\"liabilities\":[\"liability1\"]}", scenario2Str);
    }


    @Test
    public void deserialize() throws Exception {
        String scenario1aStr = "{\"assumptions\":null,\"incomeSources\":[],\"name\":\"scenario1a\",\"expenseSources\":[],\"assets\":[],\"liabilities\":[]}";
        String scenario2aStr = "{\"assumptions\":null,\"incomeSources\":[],\"name\":\"scenario2a\",\"expenseSources\":[],\"assets\":[],\"liabilities\":[]}";

        Scenario scenario1a = context.fromJSON(Scenario.class, scenario1aStr);
        assertEquals("scenario1a", scenario1a.getName());

        Scenario sceanrio2a = context.fromJSON(Scenario.class, scenario2aStr);
        assertEquals("scenario2a", sceanrio2a.getName());
    }

}