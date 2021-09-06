package name.wexler.retirement.visualizer.CashFlowEstimator;

import name.wexler.retirement.visualizer.AccountReader;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.Assumptions;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.CashFlowEstimator.IncomeTax;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.visualizer.CashFlowFrequency.Quarterly;
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
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by mwexler on 8/13/16.
 */
public class IncomeTaxTest {
    private IncomeTax incomeTax;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        context.setAssumptions(new Assumptions());
        Person payee = new Person(context, "payee1", LocalDate.of(1976, Month.JULY, 4), 65);
        Person payor = new Person(context, "payor1", LocalDate.of(1945, Month.AUGUST, 14), 65);
          LocalDate accrueStart = LocalDate.of(2011, Month.MAY, 1);
        LocalDate accrueEnd = LocalDate.of(2031, Month.APRIL, 1);
        LocalDate firstPaymentDate = LocalDate.of(accrueStart.getYear(), accrueStart.getMonth(), 14);
        firstPaymentDate = LocalDate.of(2011, Month.JULY, 12);
        CashFlowFrequency quarterly =
                new Quarterly(context, "quarterly-incomeTax1", accrueStart, accrueEnd, firstPaymentDate,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        Company bank = new Company(context, "bank1");
        CashFlowSink defaultSink = new AssetAccount(context, "checking1", Arrays.asList(payor.getId()),
                "Checking account 1", bank.getId(), Collections.emptyList(), null, AccountReader.mintTxnSource);
        incomeTax = new IncomeTax(context, "incomeTax1", payee.getId(), payor.getId(), quarterly.getId(), defaultSink.getId());
    }

    @After
    public void tearDown() {
    }


    @Test
    public void getId() {
        String name1 = incomeTax.getId();
        assertEquals(name1, "incomeTax1");
    }


    @Test
    public void equals() {
        assertEquals(incomeTax, incomeTax);
    }

    @Test
    public void toJSON() throws Exception {
        String expenseSource1Str = context.toJSON(incomeTax);
        assertEquals("{\"type\":\"incomeTax\",\"id\":\"incomeTax1\",\"payee\":\"payee1\",\"payor\":\"payor1\",\"cashFlow\":\"quarterly-incomeTax1\",\"category\":\"" + incomeTax.INCOME_TAX + "\"}", expenseSource1Str);
    }


    @Test
    public void deserialize() throws Exception {
        String expenseSource1aStr = "{\"type\":\"incomeTax\",\"id\":\"incomeTax1a\",\"payee\":\"payee1\",\"payor\":\"payor1\",\"cashFlow\":\"quarterly-incomeTax1\",\"defaultSink\":\"orSwim\",\"category\":\"" + incomeTax.INCOME_TAX + "\"}";
        CashFlowEstimator expenseSource1a = context.fromJSON(CashFlowEstimator.class, expenseSource1aStr);
        assertEquals("incomeTax1a", expenseSource1a.getId());
    }

}