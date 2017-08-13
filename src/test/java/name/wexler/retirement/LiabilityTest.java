package name.wexler.retirement;

import name.wexler.retirement.CashFlow.Balance;
import name.wexler.retirement.CashFlow.CashFlowType;
import name.wexler.retirement.CashFlow.Monthly;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
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
        Company lender = new Company(context, "lender1");
        Person borrower = new Person(context, "borrower1");
        String[] streetAddress = {"123 Main Street"};
        List<Balance> interimBalances = Arrays.asList(new Balance(LocalDate.of(2014, Month.JANUARY, 15), BigDecimal.valueOf(42.00)));
        Asset asset = new RealProperty(context, "real-property1", borrower.getId(), BigDecimal.valueOf(100000.00), LocalDate.of(2010, Month.APRIL, 15),
                streetAddress,
                "Anytown", "Count County", "AS", "01234", "US",
                interimBalances);
        String[] borrowers = { borrower.getId() };
        LocalDate accrueStart = LocalDate.of(2011, Month.MAY, 1);
        LocalDate accrueEnd = LocalDate.of(2031, Month.APRIL, 1);
        LocalDate firstPaymentDate = LocalDate.of(accrueStart.getYear(), accrueStart.getMonth(), 14);
        CashFlowType monthly = new Monthly(context, "monthly-liability1", accrueStart, accrueEnd, firstPaymentDate);
        liability = new Liability(context, "liability1", lender.getId(), borrowers, asset,
                LocalDate.of(2014, Month.OCTOBER, 10),
                LocalDate.of(2030, Month.JUNE, 1),
                30 * 12, BigDecimal.valueOf(3.875/12), BigDecimal.valueOf(50000.0),
                BigDecimal.valueOf(500.00), monthly.getId());
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void getId() throws Exception {
        String name1 = liability.getId();
        assertEquals(name1, "liability1");
    }


    @Test
    public void equals() throws Exception {
        assertEquals(liability, liability);
    }

    @Test
    public void toJSON() throws Exception {
        String expenseSource1Str = context.toJSON(liability);
        assertEquals("{\"type\":\"liability\",\"id\":\"liability1\",\"source\":\"monthly-liability1\",\"lender\":\"lender1\",\"borrowers\":[\"borrower1\"],\"security\":\"real-property1\",\"startDate\":\"2014-10-10\",\"endDate\":\"2030-06-01\",\"term\":360,\"interestRate\":0.3229166666666667,\"startingBalance\":50000.0,\"paymentAmount\":500.0,\"cashFlow\":\"monthly-liability1\"}", expenseSource1Str);
    }


    @Test
    public void deserialize() throws Exception {
        String expenseSource1aStr = "{\"type\":\"liability\",\"id\":\"liability1a\",\"source\":\"monthly-liability1\",\"borrowers\":[\"borrower1\"],\"job\":\"job1\",\"baseAnnualSalary\":100000.0}";
        ExpenseSource expenseSource1a = context.fromJSON(ExpenseSource.class, expenseSource1aStr);
        assertEquals("liability1a", expenseSource1a.getId());
    }

}