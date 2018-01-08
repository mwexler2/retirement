package name.wexler.retirement;

import name.wexler.retirement.CashFlow.Balance;
import name.wexler.retirement.CashFlow.CashFlowType;
import name.wexler.retirement.CashFlow.Monthly;
import name.wexler.retirement.CashFlow.Quarterly;
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
public class AlimonyTest {
    private Alimony alimony;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        Person payee = new Person(context, "payee1");
        Person payor = new Person(context, "payor1");
          LocalDate accrueStart = LocalDate.of(2011, Month.MAY, 1);
        LocalDate accrueEnd = LocalDate.of(2031, Month.APRIL, 1);
        LocalDate firstPaymentDate = LocalDate.of(accrueStart.getYear(), accrueStart.getMonth(), 14);
        CashFlowType monthly = new Monthly(context, "monthly-alimony1", accrueStart, accrueEnd, firstPaymentDate);
        firstPaymentDate = LocalDate.of(2011, Month.JULY, 12);
        CashFlowType quarterly = new Quarterly(context, "quarterly-alimony1", accrueStart, accrueEnd, firstPaymentDate);
        alimony = new Alimony(context, "alimony1", payee.getId(), payor.getId(),
                LocalDate.of(2014, Month.OCTOBER, 10),
                LocalDate.of(2030, Month.JUNE, 1),
                BigDecimal.valueOf(50000.00), BigDecimal.valueOf(1200.00), BigDecimal.valueOf(0.33), BigDecimal.valueOf(102500.00), monthly.getId(),
                quarterly.getId());
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void getId() throws Exception {
        String name1 = alimony.getId();
        assertEquals(name1, "alimony1");
    }


    @Test
    public void equals() throws Exception {
        assertEquals(alimony, alimony);
    }

    @Test
    public void toJSON() throws Exception {
        String expenseSource1Str = context.toJSON(alimony);
        assertEquals("{\"type\":\"alimony\",\"id\":\"alimony1\",\"startDate\":\"2014-10-10\",\"endDate\":\"2030-06-01\",\"payee\":\"payee1\",\"payor\":\"payor1\",\"baseCashFlowType\":\"monthly-alimony1\",\"smithOstlerCashFlowType\":\"quarterly-alimony1\"}", expenseSource1Str);
    }


    @Test
    public void deserialize() throws Exception {
        String expenseSource1aStr = "{\"type\":\"liability\",\"id\":\"liability1a\",\"source\":\"monthly-liability1\",\"borrowers\":[\"borrower1\"],\"job\":\"job1\",\"baseAnnualSalary\":100000.0}";
        ExpenseSource expenseSource1a = context.fromJSON(ExpenseSource.class, expenseSource1aStr);
        assertEquals("liability1a", expenseSource1a.getId());
    }

}