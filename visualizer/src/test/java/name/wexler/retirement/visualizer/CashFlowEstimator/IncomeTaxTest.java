package name.wexler.retirement.visualizer.CashFlowEstimator;

import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.CashFlowEstimator.IncomeTax;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.visualizer.CashFlowFrequency.Quarterly;
import name.wexler.retirement.visualizer.CashFlowInstance.Account;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Entity.Person;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.ListUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by mwexler on 8/13/16.
 */
public class IncomeTaxTest {
    private IncomeTax federalIncomeTax;
    private IncomeTax stateIncomeTax;
    private IncomeTax medicareTax;
    private IncomeTax socialSecurityTax;
    Assumptions assumptions;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        assumptions = new Assumptions();
        context.setAssumptions(assumptions);
        Person payor = new Person(
                context, "payor1",
                LocalDate.of(1976, Month.JULY, 4),
                65, "Payor", "1");
        Company irsPayee = new Company(context, "irs", "Internal Revenue Service");
        Company ftbPayee = new Company(context, "ftb", "Franchise Tax Board");
        LocalDate now = LocalDate.now();
        int nextYear = now.getYear() + 1;
        LocalDate accrueStart = LocalDate.of(nextYear, Month.JANUARY, 1);
        LocalDate accrueEnd = LocalDate.of(nextYear, Month.DECEMBER, 31);
        LocalDate firstPaymentDate = LocalDate.of(nextYear, Month.APRIL, 15);
        CashFlowFrequency quarterly =
                new Quarterly(context, "quarterly-incomeTax1", accrueStart, accrueEnd, firstPaymentDate,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        Company bank = new Company(context, "bank1", "Bank #1");
        CashFlowSink defaultSink = new AssetAccount(context, "checking1", Arrays.asList(payor.getId()),
                "Checking account 1", bank.getId(), Collections.emptyList(), null, AccountReader.mintTxnSource);
        TaxTable taxTable = mock(TaxTable.class);
        federalIncomeTax = new IncomeTax(
                context,
                "federalIncomeTax",
                irsPayee.getId(),
                Arrays.asList(payor.getId()),
                quarterly.getId(),
                defaultSink.getId(), taxTable);
        stateIncomeTax = new IncomeTax(context, "stateIncomeTax",
                ftbPayee.getId(),
                Arrays.asList(payor.getId()),
                quarterly.getId(),
                defaultSink.getId(),
                taxTable);
        medicareTax = new IncomeTax(context, "medicareTax",
                irsPayee.getId(),
                Arrays.asList(payor.getId()),
                quarterly.getId(),
                defaultSink.getId(),
                taxTable);
        socialSecurityTax = new IncomeTax(context, "socialSecurityTax", irsPayee.getId(),
                Arrays.asList(payor.getId()), quarterly.getId(), defaultSink.getId(),
                taxTable);
    }

    @After
    public void tearDown() {
    }


    @Test
    public void getId() {
        String name1 = federalIncomeTax.getId();
        assertEquals("federalIncomeTax", name1);
    }


    @Test
    public void equals() {
        assertEquals(federalIncomeTax, federalIncomeTax);
    }

    @Test
    public void equalsDifferent() {
        assertNotEquals(federalIncomeTax, stateIncomeTax);
    }

    @Test
    public void toJSON() throws Exception {
        String expenseSource1Str = context.toJSON(federalIncomeTax);
        assertEquals("{\"type\":\"incomeTax\",\"id\":\"federalIncomeTax\",\"cashFlow\":\"quarterly-incomeTax1\",\"category\":\"" + federalIncomeTax.INCOME_TAX + "\"}", expenseSource1Str);
    }


    @Test
    public void deserialize() throws Exception {
        String expenseSource1aStr = "{\"type\":\"incomeTax\",\"id\":\"incomeTax1a\",\"payors\":[\"payor1\"],\"payee\":\"payee1\",\"cashFlow\":\"quarterly-incomeTax1\",\"defaultSink\":\"orSwim\",\"category\":\"" +
                federalIncomeTax.INCOME_TAX + "\"" +
                ",\"taxTable\": {\"taxRateMap\": {}}" +"}";
        CashFlowEstimator expenseSource1a = context.fromJSON(CashFlowEstimator.class, expenseSource1aStr);
        assertEquals("incomeTax1a", expenseSource1a.getId());
    }

    @Test
    public void getEstimatedFutureCashFlows() throws Entity.DuplicateEntityException {
        String[] cashFlowEstimators = new String[] {
                federalIncomeTax.getId(),
                stateIncomeTax.getId(),
                medicareTax.getId(),
                socialSecurityTax.getId()
        };
        String[] assets = new String[] {};
        String[] liabilities = new String[] {};
        String[] accounts = new String[] {};
        Assumptions assumptions = new Assumptions();
        Scenario scenario = mock(Scenario.class);
        CashFlowCalendar cashFlowCalendar = new CashFlowCalendar(scenario, assumptions);
        List<CashFlowInstance> federalIncomeTaxCashFlows = federalIncomeTax.getEstimatedFutureCashFlows(cashFlowCalendar);
        assertEquals(4, federalIncomeTaxCashFlows.size());
        List<CashFlowInstance> stateIncomeTaxCashFlows = stateIncomeTax.getEstimatedFutureCashFlows(cashFlowCalendar);
        assertEquals(4, stateIncomeTaxCashFlows.size());
        List<CashFlowInstance> medicareTaxCashFlows = medicareTax.getEstimatedFutureCashFlows(cashFlowCalendar);
        assertEquals(4, medicareTaxCashFlows.size());
        List<CashFlowInstance> socialSecurityTaxCashFlows = socialSecurityTax.getEstimatedFutureCashFlows(cashFlowCalendar);
        assertEquals(4, socialSecurityTaxCashFlows.size());
    }


    @Test
    public void getName() {
        assertEquals("federalIncomeTax for Payor 1/Internal Revenue Service",
                federalIncomeTax.getName());
    }

    @Test
    public void getPass() {
        assertEquals(3, federalIncomeTax.getPass());
    }
}