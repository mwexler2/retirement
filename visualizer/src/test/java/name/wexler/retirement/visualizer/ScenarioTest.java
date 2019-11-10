package name.wexler.retirement.visualizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.Asset.RealProperty;
import name.wexler.retirement.visualizer.CashFlowFrequency.*;
import name.wexler.retirement.visualizer.CashFlowEstimator.*;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Entity.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.math.BigDecimal;
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
        Person mike = new Person(context, "mike", LocalDate.of(1984, Month.APRIL, 1), 45);
        Company yahoo = new Company(context, "yahoo");
        Company bank = new Company(context, "bank1");
        CashFlowSink defaultSink = new AssetAccount(context, "checking1", Arrays.asList(mike.getId()),
                new CashBalance(LocalDate.of(2015, 10, 1), BigDecimal.ZERO), Collections.emptyList(),
                "Checking account 1", bank.getId(), Collections.emptyList());
        Job job1 = new Job(context, "job1", "yahoo", "mike", defaultSink.getId());
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
        List<CashBalance> interimBalances = Collections.emptyList();
        CashBalance initialBalance = new CashBalance(LocalDate.of(2012, Month.JUNE, 10), BigDecimal.valueOf(100000.00));
        List<String> ownerIds = Collections.singletonList(mike.getId());
        RealProperty mainStreet = new RealProperty(context, "main", ownerIds, initialBalance, address,
                "anyTown", "AnyCount", "AS", "00000", "US", interimBalances);
        List<String> borrowers = new ArrayList<>();
        borrowers.add(mike.getId());

        Liability liability1 = new SecuredLoan(context, "liability1", bankOfNowhere.getId(), borrowers, mainStreet,
                LocalDate.of(2012, Month.JUNE, 20),
                LocalDate.of(2012, Month.JUNE, 21), 360, BigDecimal.valueOf(0.375), BigDecimal.valueOf(50000.00) ,
                BigDecimal.valueOf(200.00), BigDecimal.valueOf(42.35), liability1Monthly.getId(), defaultSink.getId(), Arrays.asList(""));

        List<CashBalance> account1Cash = new ArrayList<>();
        List<CashBalance> account2Cash = new ArrayList<>();
        List<ShareBalance> account1Securities = new ArrayList<>();
        List<ShareBalance> account2Securities = new ArrayList<>();
        List<String> account1Owners = Collections.singletonList("mike");
        List<String> account2Owners = Collections.singletonList("mike");

        List<String> payors = new ArrayList<>();
        new Monthly(context, "account1", LocalDate.now(), LocalDate.now(), LocalDate.now(),
                CashFlowFrequency.ApportionmentPeriod.EQUAL_MONTHLY);
        new Monthly(context, "account2", LocalDate.now(), LocalDate.now(), LocalDate.now(),
                CashFlowFrequency.ApportionmentPeriod.EQUAL_MONTHLY);

        AssetAccount account1 = new AssetAccount(context, "account1", account1Owners,
                new CashBalance(LocalDate.of(2015, Month.MARCH, 31), BigDecimal.ZERO),
                account1Cash,
                "My 401(k)","Bank of Nowhere", null);
        AssetAccount account2 = new AssetAccount(context, "account2", account2Owners,
                new CashBalance(LocalDate.of(2014, Month.MARCH, 1), BigDecimal.ZERO),
                account2Cash,"My Checking","Bank of Somewhere", null);
        String[] is = {"salary1", "liability1"};
        String[] assets = {"main"};
        String[] liabilities = {"liability1"};
        String[] accounts = {"account1", "account2"};
        Assumptions assumptions = new Assumptions();
        scenario1 = new Scenario(context, "scenario1", "Scenario 1", is, assets, liabilities, accounts, assumptions);
        scenario2 = new Scenario(context, "scenario2", "Scenario 2", is, assets, liabilities, accounts, assumptions);
    }

    @After
    public void tearDown() {

    }


    @Test
    public void getName() {
        String name1 = scenario1.getName();
        assertEquals(name1, "Scenario 1");
        String name2 = scenario2.getName();
        assertEquals(name2, "Scenario 2");
    }


    @Test
    public void equals() {
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
        String scenario1aStr = "{\"assumptions\":null,\"cashFlowSources\":[],\"name\":\"scenario1a\",\"expenseSources\":[],\"assets\":[],\"liabilities\":[],\"accounts\":[]}";
        String scenario2aStr = "{\"assumptions\":null,\"cashFlowSources\":[],\"name\":\"scenario2a\",\"expenseSources\":[],\"assets\":[],\"liabilities\":[],\"accounts\":[]}";

        Scenario scenario1a = context.fromJSON(Scenario.class, scenario1aStr);
        assertEquals("scenario1a", scenario1a.getName());

        Scenario sceanrio2a = context.fromJSON(Scenario.class, scenario2aStr);
        assertEquals("scenario2a", sceanrio2a.getName());
    }

}