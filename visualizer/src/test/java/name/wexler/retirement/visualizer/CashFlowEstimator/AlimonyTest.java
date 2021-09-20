package name.wexler.retirement.visualizer.CashFlowEstimator;

import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.visualizer.CashFlowFrequency.Monthly;
import name.wexler.retirement.visualizer.CashFlowFrequency.Quarterly;
import name.wexler.retirement.visualizer.CashFlowFrequency.SemiMonthly;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Entity.Category;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Entity.Person;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

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
public class AlimonyTest {
    private Alimony alimony;
    private Context context;
    private Assumptions assumptions;
    @Mock
    private Scenario scenario;
    private CashFlowCalendar cashFlowCalendar;
    Person payee;
    Person payor;
    private final BigDecimal baseIncome = BigDecimal.valueOf(50000.00);
    private final BigDecimal baseAlimony = BigDecimal.valueOf(1200);
    private final BigDecimal smithOstlerRate = BigDecimal.valueOf(-0.33);
    private final BigDecimal maxAlimony = BigDecimal.valueOf(102500.00);

    @Before
    public void setUp() throws Exception {
        context = new Context();
        assumptions = new Assumptions();
        context.setAssumptions(assumptions);

        payee = new Person(context, "payee1", LocalDate.of(1976, Month.JULY, 4), 65,
                "Payee", "1");
        payor = new Person(context, "payor1", LocalDate.of(1945, Month.AUGUST, 14), 65,
                "Payor", "1");
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
        Company bank = new Company(context, "bank1", "Bank #1");
        CashFlowSink defaultSink = new AssetAccount(context, "checking1", Arrays.asList(payor.getId()),
                "Checking account 1", bank.getId(), Collections.emptyList(), null, AccountReader.mintTxnSource);
        alimony = new Alimony(context, "alimony1", payee.getId(), payor.getId(),
                baseIncome, baseAlimony, smithOstlerRate, maxAlimony, monthly.getId(),
                quarterly.getId(), defaultSink.getId());
        cashFlowCalendar = setupCashFlowCalendar(scenario, assumptions, alimony, defaultSink);
    }

    private CashFlowCalendar setupCashFlowCalendar(
            Scenario scenario,
            Assumptions assumptions,
            Alimony alimony,
            CashFlowSink defaultSink) throws
            AssetAccount.NotFoundException, Entity.DuplicateEntityException, Exception {
        cashFlowCalendar = new CashFlowCalendar(scenario, assumptions);
        List<CashFlowInstance> cashFlowInstances = new ArrayList<CashFlowInstance>();

        cashFlowInstances.add(
                new CashFlowInstance(
                        true,
                        alimony,
                        defaultSink,
                        alimony.getItemType(),
                        alimony.getCategory(),
                        LocalDate.of(1999, Month.OCTOBER, 1),
                        LocalDate.of(1999, Month.DECEMBER, 31),
                        LocalDate.of(2000, Month.JANUARY, 15),
                        BigDecimal.ONE,
                        BigDecimal.ZERO, "Estimated Alimony")
        );
        new Company(context,
                "company1",
                "Company #1");
        new Job(context,
                "job1",
                "company1",
                payor.getId(),
                defaultSink.getId());
        new SemiMonthly(context,
                "salary-semimonthly",
                LocalDate.of(1999, Month.JANUARY, 1),
                LocalDate.of(2001, Month.DECEMBER, 31),
                LocalDate.of(1999, Month.JANUARY, 22),
                7,
                22,
                CashFlowFrequency.ApportionmentPeriod.EQUAL_MONTHLY);
        CashFlowSource salary = new Salary(
                context,
                "salary1",
                "job1",
                "salary-semimonthly",
                BigDecimal.valueOf(1000.00));
        cashFlowInstances.add(
                new CashFlowInstance(
                        false,
                        salary,
                        defaultSink,
                        salary.getItemType(),
                        Salary.PAYCHECK,
                        LocalDate.of(1999, Month.OCTOBER, 1),
                        LocalDate.of(1999, Month.OCTOBER, 15),
                        LocalDate.of(1999, Month.OCTOBER, 22),
                        BigDecimal.valueOf(500.00),
                        BigDecimal.ZERO,
                        "Estimated Salary")
        );
        LocalDate now = LocalDate.now();
        Month firstMonthOfQuarter = now.getMonth().firstMonthOfQuarter();
        Month lastMonthOfQuarter = now.getMonth().firstMonthOfQuarter().plus(2);
        cashFlowInstances.add(
                new CashFlowInstance(
                        true,
                        salary,
                        defaultSink,
                        salary.getItemType(),
                        Salary.PAYCHECK,
                        LocalDate.of(now.getYear(), firstMonthOfQuarter, 1),
                        LocalDate.of(now.getYear(), lastMonthOfQuarter,
                                lastMonthOfQuarter.length(false)),
                        now.plusDays(14),
                        baseIncome.multiply(BigDecimal.valueOf(4)),
                        BigDecimal.ZERO,
                        "Estimated paycheck")
        );
        cashFlowCalendar.addCashFlowInstances(cashFlowInstances);
        return cashFlowCalendar;
    }

    @After
    public void tearDown() {
    }


    @Test
    public void getId() {
        String name1 = alimony.getId();
        assertEquals(name1, "alimony1");
    }


    @Test
    public void equals() {
        assertEquals(alimony, alimony);
    }

    @Test
    public void toJSON() throws Exception {
        String expenseSource1Str = context.toJSON(alimony);
        assertEquals("{\"type\":\"alimony\",\"id\":\"alimony1\",\"payee\":\"payee1\",\"payor\":\"payor1\",\"cashFlow\":\"monthly-alimony1\",\"smithOstlerCashFlowType\":\"quarterly-alimony1\",\"category\":\"Alimony\"}", expenseSource1Str);
    }


    @Test
    public void deserialize() throws Exception {
        String expenseSource1aStr = "{\"type\":\"alimony\",\"id\":\"alimony1a\",\"baseCashFlow\":\"monthly-alimony1\",\"baseIncome\":100.00,\"baseAlimony\":10.00,\"smithOstlerRate\":15.00,\"payee\":\"payee1\",\"payor\":\"payor1\",\"cashFlow\":\"monthly-alimony1\",\"smithOstlerCashFlowType\":\"quarterly-alimony1\",\"defaultSink\":\"orSwim\",\"category\":\"Alimony\"}";
        CashFlowEstimator expenseSource1a = context.fromJSON(CashFlowEstimator.class, expenseSource1aStr);
        assertEquals("alimony1a", expenseSource1a.getId());
    }

    @Test
    public void testGetEstimatedFutureCashFlows() {
        List<CashFlowInstance> cashFlowsInstances = alimony.getEstimatedFutureCashFlows(cashFlowCalendar);
        assertEquals(1, cashFlowsInstances.size());
    }

    @Test
    public void testGetName() {
        assertEquals("alimony1: Payor 1(Payee 1)", alimony.getName());
    }

    @Test
    public void testGetItemType() {
        assertEquals(Category.EXPENSE, alimony.getItemType());
    }

    @Test
    public void testIsOwner() {
        assertEquals(true, alimony.isOwner(payor));
        assertEquals(false, alimony.isOwner(payee));
    }

    @Test
    public void testGetPass() {
        assertEquals(CASH_ESTIMATE_PASS.DERIVED_EXPENSES,
                alimony.getPass());
    }
}