package name.wexler.retirement;

import name.wexler.retirement.CashFlow.CashFlowType;
import name.wexler.retirement.CashFlow.Monthly;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by mwexler on 8/13/16.
 */
public class DebtTest {
    Debt debt;
    Context context;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        Company lender = new Company(context, "lender1");
        Person borrower = new Person(context, "borrower1");
        String[] streetAddress = {"123 Main Street"};
        Asset asset = new RealProperty("real-property1", borrower, BigDecimal.valueOf(100000.00), LocalDate.of(2010, Month.APRIL, 15),
                streetAddress,
                "Anytown", "Count County", "AS", "01234", "US");
        Person[] borrowers = { borrower };
        LocalDate accrueStart = LocalDate.of(2011, Month.MAY, 1);
        LocalDate accrueEnd = LocalDate.of(2031, Month.APRIL, 1);
        LocalDate firstPaymentDate = LocalDate.of(accrueStart.getYear(), accrueStart.getMonth(), 14);
        CashFlowType monthly = new Monthly(context, "monthly-debt1", accrueStart, accrueEnd, firstPaymentDate);
        debt = new Debt(context, "debt1", lender, borrowers, asset,
                LocalDate.of(2014, Month.OCTOBER, 10), 30 * 12, BigDecimal.valueOf(3.875/12), BigDecimal.valueOf(50000.0),
                BigDecimal.valueOf(500.00), monthly);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void getId() throws Exception {
        String name1 = debt.getId();
        assertEquals(name1, "debt1");
    }


    @Test
    public void equals() throws Exception {
        assertEquals(debt, debt);
    }

    @Test
    public void toJSON() throws Exception {
        String expenseSource1Str = context.toJSON(debt);
        assertEquals("{\"type\":\"debt\",\"id\":\"debt1\",\"source\":\"monthly-debt1\",\"lender\":\"lender1\",\"borrowers\":[\"borrower1\"],\"security\":\"real-property1\",\"startDate\":\"2014-10-10\",\"term\":360,\"interestRate\":0.3229166666666667,\"startingBalance\":50000.0,\"paymentAmount\":500.0}", expenseSource1Str);
    }


    @Test
    public void deserialize() throws Exception {
        String expenseSource1aStr = "{\"type\":\"debt\",\"id\":\"debt1a\",\"source\":null,\"job\":\"job1\",\"baseAnnualSalary\":100000.0}";
        ExpenseSource expenseSource1a = context.fromJSON(ExpenseSource.class, expenseSource1aStr);
        assertEquals("debt1a", expenseSource1a.getId());
    }

}