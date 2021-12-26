package name.wexler.retirement.visualizer.CashFlowFrequency;

import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.CashFlowEstimator.Salary;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Person;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance.NO_ID;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by mwexler on 8/13/16.
 */
public class CashFlowTypeTest {
    private Annual annual;
    private Biweekly biweekly;
    private Monthly monthly;
    private SemiMonthly semiMonthly;
    private Quarterly quarterly;
    private Context context;
    private CashFlowCalendar cashFlowCalendar;


    @Before
    public void setUp() throws Exception {
        context = new Context();
        Assumptions assumptions = new Assumptions();
        context.setAssumptions(assumptions);
        LocalDate annualAccrueStart = LocalDate.of(2014, Month.AUGUST, 17);
        LocalDate annualAccrueEnd = LocalDate.now().plusMonths(13);
        LocalDate annualFirstPayment = LocalDate.of(2015, Month.MARCH, 15);
        annual = new Annual(context, "annual1", annualAccrueStart, annualAccrueEnd, annualFirstPayment,
                CashFlowFrequency.ApportionmentPeriod.ANNUAL);

        LocalDate biweeklyAccrueStart = LocalDate.of(2014, Month.DECEMBER, 23);
        LocalDate biweeklyAccrueEnd = LocalDate.of(2016, Month.JANUARY, 2);
        LocalDate biweeklyFirstPeriodStart = LocalDate.of(2014, Month.DECEMBER, 20);
        LocalDate biweeklyFirstPayment = LocalDate.of(2015, Month.JANUARY, 9);
        biweekly = new Biweekly(context, "biweekly1",
                biweeklyFirstPeriodStart,
                biweeklyAccrueStart,
                biweeklyAccrueEnd,
                biweeklyFirstPayment,
                CashFlowFrequency.ApportionmentPeriod.ANNUAL);

        LocalDate monthlyAccrueStart = LocalDate.of(1999, Month.APRIL, 2);
        LocalDate monthlyAccrueEnd = LocalDate.now().plusMonths(27);
        LocalDate monthlyFirstPayment = LocalDate.of(1999, Month.MAY, 5);
        monthly = new Monthly(context, "monthly1",
                monthlyAccrueStart,
                monthlyAccrueEnd,
                monthlyFirstPayment,
                CashFlowFrequency.ApportionmentPeriod.ANNUAL);

        LocalDate quarterlyAccrueStart = LocalDate.of(1999, Month.APRIL, 2);
        LocalDate quarterlyAccrueEnd = LocalDate.of(2019, Month.APRIL, 3);
        LocalDate quarterlyFirstPayment = LocalDate.of(1999, Month.MAY, 5);
        quarterly = new Quarterly(context, "quarterly1",
                quarterlyAccrueStart,
                quarterlyAccrueEnd,
                quarterlyFirstPayment,
                CashFlowFrequency.ApportionmentPeriod.ANNUAL);

        LocalDate semimonthlyAccrueStart = LocalDate.of(2016, Month.FEBRUARY, 14);
        LocalDate semimonthlyAccrueEnd = LocalDate.now().plusMonths(7);
        LocalDate semimonthlyFirstPayment = LocalDate.of(2016, Month.FEBRUARY, 19);
        semiMonthly = new SemiMonthly(context, "semiMonthly1",
                semimonthlyAccrueStart,
                semimonthlyAccrueEnd,
                semimonthlyFirstPayment,
                19, 7,
                CashFlowFrequency.ApportionmentPeriod.ANNUAL
        );

        String[] cashFlowSources = new String[0];
        String[] assets = new String[0];
        String[] liabilities = new String[0];
        String[] accounts = new String[0];
        Scenario scenario = mock(Scenario.class);
        cashFlowCalendar = new CashFlowCalendar(scenario, assumptions);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void CashFlowType() {
        LocalDate annualAccrueStart = LocalDate.of(2014, Month.AUGUST, 17);
        LocalDate annualAccrueEnd = LocalDate.of(2015, Month.SEPTEMBER, 20);
        LocalDate annualFirstPayment = LocalDate.of(2015, Month.MARCH, 15);
        try {
            CashFlowFrequency duplicate =
                    new Annual(context, "annual1", annualAccrueStart, annualAccrueEnd, annualFirstPayment,
                            CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        } catch (Exception e) {
            is(instanceOf(Exception.class));
        }
    }

    @Test
    public void getAccrueStart() {
        assertEquals(LocalDate.of(2014, Month.DECEMBER, 23), biweekly.getAccrueStart());
        assertEquals(LocalDate.of(1999, Month.APRIL, 2), monthly.getAccrueStart());
        assertEquals(LocalDate.of(2016, Month.FEBRUARY, 14), semiMonthly.getAccrueStart());
        assertEquals(LocalDate.of(2014, Month.AUGUST, 17), annual.getAccrueStart());
        assertEquals(LocalDate.of(1999, Month.APRIL, 2), quarterly.getAccrueStart());
    }


    @Test()
    public void getFlowInstances()  throws Exception {
        BigDecimal annualAmount = BigDecimal.valueOf(1000.00);
        Person employee1 = new Person(context, "employee1", LocalDate.of(2004, Month.MARCH, 31), 65,
                "Employee", "1");
        Company employer1 = new Company(context, "employer1", "Employer 1");
        Company bank = new Company(context, "bank1", "Bank #1");
        CashFlowSink defaultSink = new AssetAccount(context, "checking1", Arrays.asList(employee1.getId()),
                "Checking account 1", bank.getId(), Collections.emptyList(), null, AccountReader.mintTxnSource);
        Job job1 = new Job(context, "job1", employer1.getId(), employee1.getId(), defaultSink.getId());
        job1.setStartDate(LocalDate.of(2010, Month.SEPTEMBER, 17));
        job1.setEndDate(LocalDate.now().plusMonths(27));
        CashFlowFrequency biweekly = new Biweekly(context, "salary1Frequency",
                job1.getStartDate(),
                job1.getStartDate(),
                job1.getEndDate(),
                job1.getStartDate().plusDays(7), CashFlowFrequency.ApportionmentPeriod.EQUAL_MONTHLY);
        CashFlowEstimator salary1 = new Salary(context, "salary1", "job1", "salary1Frequency", BigDecimal.valueOf(42000.42));

        List<CashFlowInstance> biweeklyCashFlows = biweekly.getFutureCashFlowInstances(cashFlowCalendar, salary1,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                BigDecimal amount = annualAmount.divide(BigDecimal.valueOf(26), 2, RoundingMode.HALF_UP);
                BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                return new CashFlowInstance(NO_ID, false, salary1, defaultSink,
                        CashFlowCalendar.ITEM_TYPE.INCOME.toString(), "Parent Category","test",
                        accrualStart, accrualEnd, cashFlowDate, amount, balance, "salary for " + salary1.getName());
                });
        assertEquals(BigDecimal.valueOf(38.46), biweeklyCashFlows.get(0).getAmount());
        assertEquals(BigDecimal.valueOf(38.46), biweeklyCashFlows.get(biweeklyCashFlows.size() - 1).getAmount());
        assertEquals(59, biweeklyCashFlows.size());


        List<CashFlowInstance> monthlyCashFlows = monthly.getFutureCashFlowInstances(cashFlowCalendar, salary1,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal amount = annualAmount.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                    return new CashFlowInstance(NO_ID, true, salary1, defaultSink,
                            CashFlowCalendar.ITEM_TYPE.TRANSFER.toString(), "Parent Category","test",
                            accrualStart, accrualEnd, cashFlowDate, amount, balance,
                            "Estimated salary for " + salary1.getName());
                });
        assertEquals(BigDecimal.valueOf(83.33), monthlyCashFlows.get(0).getAmount());
        assertEquals(BigDecimal.valueOf(83.33), monthlyCashFlows.get(monthlyCashFlows.size() - 1).getAmount());
        assertEquals(28, monthlyCashFlows.size());

        List<CashFlowInstance> semiMonthlyCashFlows = semiMonthly.getFutureCashFlowInstances(cashFlowCalendar, salary1,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal amount = annualAmount.divide(BigDecimal.valueOf(24), 2, RoundingMode.HALF_UP);
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                    return new CashFlowInstance(NO_ID, false, salary1, defaultSink,
                            CashFlowCalendar.ITEM_TYPE.EXPENSE.toString(), "Parent Category", "test",
                            accrualStart, accrualEnd, cashFlowDate, amount, balance,
                            "Salary for " + salary1.getName());
                });
        assertEquals(BigDecimal.valueOf(41.67), semiMonthlyCashFlows.get(0).getAmount());
        assertEquals(BigDecimal.valueOf(41.67), semiMonthlyCashFlows.get(semiMonthlyCashFlows.size() - 1).getAmount());
        assertEquals(15, semiMonthlyCashFlows.size());
        System.out.println("semiMonthly:");
        System.out.println(semiMonthlyCashFlows);
        System.out.println("");

        List<CashFlowInstance> annualCashFlows = annual.getFutureCashFlowInstances(cashFlowCalendar, salary1,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                    return new CashFlowInstance(NO_ID, false, salary1, defaultSink,
                            CashFlowCalendar.ITEM_TYPE.INCOME.toString(), "Parent Category", "test",
                            accrualStart, accrualEnd, cashFlowDate, annualAmount, balance,
                            "Salary for " + salary1.getName());
                });
        assertEquals(3, annualCashFlows.size());
        assertEquals(annualAmount, annualCashFlows.get(0).getAmount());
        assertEquals(annualAmount, annualCashFlows.get(1).getAmount());

        List<CashFlowInstance> quarterlyCashFlows = annual.getFutureCashFlowInstances(cashFlowCalendar, salary1,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) ->
                {
                    BigDecimal amount = annualAmount.divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                    return new CashFlowInstance(NO_ID, false, salary1, defaultSink,
                            CashFlowCalendar.ITEM_TYPE.INCOME.toString(), "Parent Category","test",
                            accrualStart, accrualEnd, cashFlowDate, amount, balance,
                            "Salary for " + salary1.getName());
                });
        assertEquals(3, annualCashFlows.size());
        assertEquals(annualAmount, annualCashFlows.get(0).getAmount());
        assertEquals(annualAmount, annualCashFlows.get(1).getAmount());
    }


    @Test
    public void getAccrueEnd() {
        LocalDate result = biweekly.getAccrueEnd();
        assertEquals(LocalDate.of(2016, Month.JANUARY, 2), result);

        result = quarterly.getAccrueEnd();
        assertEquals("quarterly getAccrueEnd failed", LocalDate.of(2019, Month.APRIL, 3), result);
    }

    @Test
    public void getFirstPaymentDate() {
        LocalDate result = monthly.getFirstPaymentDate();
        assertEquals(LocalDate.of(1999, Month.MAY, 5), result);

         result = quarterly.getFirstPaymentDate();
        assertEquals("quartly first payment date", LocalDate.of(1999, Month.MAY, 5), result);
    }


    @Test
    public void fromJSON() throws Exception {

        String annualStr = "{\"type\":\"annual\",\"id\":\"annual1a\",\"firstPaymentDate\":\"2015-07-04\",\"accrueStart\":\"2014-04-15\",\"accrueEnd\":\"2015-10-31\"}";
        String quarterlyStr = "{\"type\":\"quarterly\",\"id\":\"quarterly\",\"firstPaymentDate\":\"2015-07-04\",\"accrueStart\":\"2014-04-15\",\"accrueEnd\":\"2015-10-31\"}";
        String biweeklyStr = "{\"type\":\"biweekly\",\"id\":\"biweekly1a\",\"firstPeriodStart\":\"2014-12-20\",\"firstPaymentDate\":\"2014-12-26\",\"accrueStart\":\"2014-12-23\",\"accrueEnd\":\"2016-01-02\"}";
        String monthlyStr = "{\"type\":\"monthly\",\"id\":\"monthly1a\",\"firstPaymentDate\":\"1999-04-07\",\"accrueStart\":\"1999-04-02\",\"accrueEnd\":\"2019-04-03\"}";
        String semiMonthlyStr = "{\"type\":\"semimonthly\",\"id\":\"semiMonthly1a\",\"firstPaymentDate\":\"2016-02-19\",\"accrueStart\":\"2016-02-14\",\"accrueEnd\":\"2018-02-14\",\"firstPaymentDayOfMonth\":19,\"secondPaymentDayOfMonth\":7}";

        Annual annual1a = context.fromJSON(Annual.class, annualStr);
        assertEquals(annual1a.getId(), "annual1a");

        Biweekly biweekly1a = context.fromJSON(Biweekly.class, biweeklyStr);
        assertEquals(biweekly1a.getId(), "biweekly1a");

        Monthly monthly1a = context.fromJSON(Monthly.class, monthlyStr);
        assertEquals(monthly1a.getId(), "monthly1a");

        SemiMonthly semiMonthly1a = context.fromJSON(SemiMonthly.class, semiMonthlyStr);
        assertEquals(semiMonthly1a.getId(), "semiMonthly1a");

    }

}