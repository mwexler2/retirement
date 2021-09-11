package name.wexler.retirement.visualizer.CashFlowEstimator;

import name.wexler.retirement.visualizer.AccountReader;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.Asset.RealProperty;
import name.wexler.retirement.visualizer.Assumptions;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.visualizer.CashFlowFrequency.Monthly;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.CashFlowEstimator.Liability;
import name.wexler.retirement.visualizer.CashFlowEstimator.SecuredLoan;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by mwexler on 8/13/16.
 */
public class LiabilityTest {
    private Liability liability;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        context.setAssumptions(new Assumptions());
        Company lender = new Company(context, "lender1", "Lender 1");
        Person borrower = new Person(context, "borrower1", LocalDate.of(1980, Month.FEBRUARY, 29), 80);
        String[] streetAddress = {"123 Main Street"};
        List<CashBalance> interimBalances = Collections.singletonList(new CashBalance(LocalDate.of(2014, Month.JANUARY, 15), BigDecimal.valueOf(42.00)));
        CashBalance initialBalance = new CashBalance(LocalDate.of(2010, Month.APRIL, 15), BigDecimal.valueOf(100000.00));
        List<String> borrowerIds = Collections.singletonList(borrower.getId());
        Asset asset = new RealProperty(context, "real-property1", borrowerIds, initialBalance,
                streetAddress,
                "Anytown", "Count County", "AS", "01234", "US",
                interimBalances);
        List<String> borrowers = new ArrayList<>();
        borrowers.add(borrower.getId());
        LocalDate accrueStart = LocalDate.of(2011, Month.MAY, 1);
        LocalDate accrueEnd = LocalDate.of(2031, Month.APRIL, 1);
        LocalDate firstPaymentDate = LocalDate.of(accrueStart.getYear(), accrueStart.getMonth(), 14);
        CashFlowFrequency monthly =
                new Monthly(context, "monthly-liability1", accrueStart, accrueEnd, firstPaymentDate,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        Company bank = new Company(context, "bank1", "Bank #1");
        CashFlowSink defaultSink = new AssetAccount(context, "checking1", Arrays.asList(borrower.getId()),
                "Checking account 1", bank.getId(), Collections.emptyList(), null, AccountReader.mintTxnSource);
        liability = new SecuredLoan(context, "liability1", lender.getId(), borrowers, asset,
                LocalDate.of(2014, Month.OCTOBER, 10),
                LocalDate.of(2030, Month.JUNE, 1),
                30 * 12, BigDecimal.valueOf(3.875/12), BigDecimal.valueOf(50000.0),
                BigDecimal.valueOf(500.00), BigDecimal.valueOf(473.33), monthly.getId(), defaultSink.getId(), Arrays.asList("foo"));
    }

    @After
    public void tearDown() {
    }


    @Test
    public void getId() {
        String name1 = liability.getId();
        assertEquals(name1, "liability1");
    }


    @Test
    public void equals() {
        assertEquals(liability, liability);
    }

    @Test
    public void deserialize() throws Exception {
        String expenseSource1aStr = "{\"type\":\"securedLoan\",\"id\":\"liability1a\",\"source\":\"monthly-liability1\",\"defaultSink\":\"sink\",\"borrowers\":[\"borrower1\"],\"job\":\"job1\",\"baseAnnualSalary\":100000.0,\"startDate\":\"2014-01-01\",\"term\":15,\"interestRate\":4.75,\"startingBalance\":42752.53,\"paymentAmount\":432.23,\"impoundAmount\":0.00,\"indicators\":[]}";
        CashFlowEstimator expenseSource1a = context.fromJSON(CashFlowEstimator.class, expenseSource1aStr);
        assertEquals("liability1a", expenseSource1a.getId());
    }

}