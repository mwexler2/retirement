package name.wexler.retirement;

import name.wexler.retirement.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.CashFlowFrequency.Monthly;
import name.wexler.retirement.CashFlowFrequency.Quarterly;
import name.wexler.retirement.CashFlowSource.Alimony;
import name.wexler.retirement.CashFlowSource.CashFlowSource;
import name.wexler.retirement.Entity.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

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
        context.setAssumptions(new Assumptions());
        Person payee = new Person(context, "payee1", LocalDate.of(1976, 07, 04), 65);
        Person payor = new Person(context, "payor1", LocalDate.of(1945, Month.AUGUST, 14), 65);
          LocalDate accrueStart = LocalDate.of(2011, Month.MAY, 1);
        LocalDate accrueEnd = LocalDate.of(2031, Month.APRIL, 1);
        LocalDate firstPaymentDate = LocalDate.of(accrueStart.getYear(), accrueStart.getMonth(), 14);
        CashFlowFrequency monthly =
                new Monthly(context, "monthly-alimony1", accrueStart, accrueEnd, firstPaymentDate,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        firstPaymentDate = LocalDate.of(2011, Month.JULY, 12);
        CashFlowFrequency quarterly =
                new Quarterly(context, "quarterly-alimony1", accrueStart, accrueEnd, firstPaymentDate,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        alimony = new Alimony(context, "alimony1", payee.getId(), payor.getId(),
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
        assertEquals("{\"type\":\"alimony\",\"id\":\"alimony1\",\"payee\":\"payee1\",\"payor\":\"payor1\",\"cashFlow\":\"monthly-alimony1\",\"smithOstlerCashFlowType\":\"quarterly-alimony1\"}", expenseSource1Str);
    }


    @Test
    public void deserialize() throws Exception {
        String expenseSource1aStr = "{\"type\":\"alimony\",\"id\":\"alimony1a\",\"baseCashFlow\":\"monthly-alimony1\",\"payee\":\"payee1\",\"cashFlow\":\"monthly-alimony1\",\"smithOstlerCashFlowType\":\"quarterly-alimony1\"}";
        CashFlowSource expenseSource1a = context.fromJSON(CashFlowSource.class, expenseSource1aStr);
        assertEquals("alimony1a", expenseSource1a.getId());
    }

}