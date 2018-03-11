package name.wexler.retirement.CashFlow;

import name.wexler.retirement.Assumptions;
import name.wexler.retirement.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.*;

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

    @Before
    public void setUp() throws Exception {
        context = new Context();
        LocalDate annualAccrueStart = LocalDate.of(2014, Month.AUGUST, 17);
        LocalDate annualAccrueEnd = LocalDate.of(2015, Month.SEPTEMBER, 20);
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
        LocalDate monthlyAccrueEnd = LocalDate.of(2019, Month.APRIL, 3);
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
        LocalDate semimonthlyAccrueEnd = LocalDate.of(2018, Month.FEBRUARY, 14);
        LocalDate semimonthlyFirstPayment = LocalDate.of(2016, Month.FEBRUARY, 19);
        semiMonthly = new SemiMonthly(context, "semiMonthly1",
                semimonthlyAccrueStart,
                semimonthlyAccrueEnd,
                semimonthlyFirstPayment,
                7, 19,
                CashFlowFrequency.ApportionmentPeriod.ANNUAL
        );
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void CashFlowType() throws Exception {
        LocalDate annualAccrueStart = LocalDate.of(2014, Month.AUGUST, 17);
        LocalDate annualAccrueEnd = LocalDate.of(2015, Month.SEPTEMBER, 20);
        LocalDate annualFirstPayment = LocalDate.of(2015, Month.MARCH, 15);
        try {
            CashFlowFrequency duplicate =
                    new Annual(context, "annual1", annualAccrueStart, annualAccrueEnd, annualFirstPayment,
                            CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        } catch (Exception e) {
            assertThat(e, isA(Exception.class));
        }
    }

    @Test
    public void getAccrueStart() throws Exception {
        assertEquals(LocalDate.of(2014, Month.DECEMBER, 23), biweekly.getAccrueStart());
        assertEquals(LocalDate.of(1999, Month.APRIL, 2), monthly.getAccrueStart());
        assertEquals(LocalDate.of(2016, Month.FEBRUARY, 14), semiMonthly.getAccrueStart());
        assertEquals(LocalDate.of(2014, Month.AUGUST, 17), annual.getAccrueStart());
        assertEquals(LocalDate.of(1999, Month.APRIL, 2), quarterly.getAccrueStart());
    }


    @Test()
    public void getFlowInstances()  {
        Assumptions assumptions = new Assumptions();
        CashFlowCalendar cashFlowCalendar = new CashFlowCalendar(assumptions);
        BigDecimal annualAmount = BigDecimal.valueOf(1000.00);

        List<CashFlowInstance> biweeklyCashFlows = biweekly.getCashFlowInstances(cashFlowCalendar,
                (calendar, accrualStart, accrualEnd, percent) ->
                annualAmount.divide(BigDecimal.valueOf(26), 2, BigDecimal.ROUND_HALF_UP));
        assertEquals(LocalDate.of(2015, Month.JANUARY, 9), biweeklyCashFlows.get(0).getCashFlowDate());
        assertEquals(LocalDate.of(2016, Month.JANUARY, 8), biweeklyCashFlows.get(biweeklyCashFlows.size() - 1).getCashFlowDate());
        assertEquals(BigDecimal.valueOf(38.46), biweeklyCashFlows.get(0).getAmount());
        assertEquals(BigDecimal.valueOf(38.46), biweeklyCashFlows.get(biweeklyCashFlows.size() - 1).getAmount());
        assertEquals(27, biweeklyCashFlows.size());


        List<CashFlowInstance> monthlyCashFlows = monthly.getCashFlowInstances(cashFlowCalendar,
                (calendar, accrualStart, accrualEnd, percent) ->
                annualAmount.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP));
        assertEquals(LocalDate.of(1999, Month.MAY, 5), monthlyCashFlows.get(0).getCashFlowDate());
        assertEquals(LocalDate.of(2019, Month.MAY, 5), monthlyCashFlows.get(monthlyCashFlows.size() - 1).getCashFlowDate());
        assertEquals(BigDecimal.valueOf(83.33), monthlyCashFlows.get(0).getAmount());
        assertEquals(BigDecimal.valueOf(83.33), monthlyCashFlows.get(monthlyCashFlows.size() - 1).getAmount());
        assertEquals(241, monthlyCashFlows.size());

        List<CashFlowInstance> semiMonthlyCashFlows = semiMonthly.getCashFlowInstances(cashFlowCalendar,
                (calendar, accrualStart, accrualEnd, percent) -> annualAmount.divide(BigDecimal.valueOf(24), 2, BigDecimal.ROUND_HALF_UP));
        assertEquals(LocalDate.of(2016, Month.FEBRUARY, 19), semiMonthlyCashFlows.get(0).getCashFlowDate());
        assertEquals(LocalDate.of(2016, Month.MARCH, 7), semiMonthlyCashFlows.get(1).getCashFlowDate());
        assertEquals(LocalDate.of(2018, Month.FEBRUARY, 19), semiMonthlyCashFlows.get(semiMonthlyCashFlows.size() - 1).getCashFlowDate());
        assertEquals(BigDecimal.valueOf(41.67), semiMonthlyCashFlows.get(0).getAmount());
        assertEquals(BigDecimal.valueOf(41.67), semiMonthlyCashFlows.get(semiMonthlyCashFlows.size() - 1).getAmount());
        assertEquals(49, semiMonthlyCashFlows.size());
        System.out.println("semiMonthly:");
        System.out.println(semiMonthlyCashFlows);
        System.out.println("");

        List<CashFlowInstance> annualCashFlows = annual.getCashFlowInstances(cashFlowCalendar,
                (calendar, accrualStart, accrualEnd, percent) -> annualAmount);
        assertEquals(2, annualCashFlows.size());
        assertEquals(annualAmount, annualCashFlows.get(0).getAmount());
        assertEquals(LocalDate.of(2015, Month.MARCH, 15), annualCashFlows.get(0).getCashFlowDate());
        assertEquals(annualAmount, annualCashFlows.get(1).getAmount());
        assertEquals(LocalDate.of(2016, Month.MARCH, 15), annualCashFlows.get(1).getCashFlowDate());

        List<CashFlowInstance> quarterlyCashFlows = annual.getCashFlowInstances(cashFlowCalendar,
                (calendar, accrualStart, accrualEnd, percent) ->
                annualAmount.divide(BigDecimal.valueOf(4), 2, BigDecimal.ROUND_HALF_UP));
        assertEquals(2, annualCashFlows.size());
        assertEquals(annualAmount, annualCashFlows.get(0).getAmount());
        assertEquals(LocalDate.of(2015, Month.MARCH, 15), annualCashFlows.get(0).getCashFlowDate());
        assertEquals(annualAmount, annualCashFlows.get(1).getAmount());
        assertEquals(LocalDate.of(2016, Month.MARCH, 15), annualCashFlows.get(1).getCashFlowDate());
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
    public void getMonthDay() {
        MonthDay result = annual.getMonthDay();
        MonthDay expectedResult = MonthDay.of(Month.MARCH, 15);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getFirstYear() {
        int result = annual.getFirstYear();
        assertEquals(2015, result);
    }

    @Test
    public void getLastYear() {

        int result = annual.getLastYear();
        assertEquals(2015, result);
    }

    @Test
    public void getDayOfWeek() {
        DayOfWeek result = biweekly.getDayOfWeek();
        assertEquals(DayOfWeek.FRIDAY, result);
    }

    @Test
    public void toJSON() throws Exception {
        String annualStr = context.toJSON(annual);
        assertEquals("{\"type\":\"annual\",\"id\":\"annual1\",\"accrueStart\":\"2014-08-17\",\"accrueEnd\":\"2015-09-20\",\"firstPaymentDate\":\"2015-03-15\",\"apportionmentPeriod\":\"ANNUAL\"}",
                annualStr);

        String biweeklyStr = context.toJSON(biweekly);
        assertEquals("{\"type\":\"biweekly\",\"id\":\"biweekly1\",\"firstPeriodStart\":\"2014-12-20\",\"accrueStart\":\"2014-12-23\",\"accrueEnd\":\"2016-01-02\",\"firstPaymentDate\":\"2015-01-09\",\"apportionmentPeriod\":\"ANNUAL\"}",
                biweeklyStr);

        String monthlyStr = context.toJSON(monthly);
        assertEquals("{\"type\":\"monthly\",\"id\":\"monthly1\",\"accrueStart\":\"1999-04-02\",\"accrueEnd\":\"2019-04-03\",\"firstPaymentDate\":\"1999-05-05\",\"apportionmentPeriod\":\"ANNUAL\"}",
                monthlyStr);

        String semiMonthlyStr = context.toJSON(semiMonthly);
        assertEquals("{\"type\":\"semimonthly\",\"id\":\"semiMonthly1\",\"accrueStart\":\"2016-02-14\",\"accrueEnd\":\"2018-02-14\",\"firstPaymentDate\":\"2016-02-19\",\"firstDayOfMonth\":7,\"secondDayOfMonth\":19,\"apportionmentPeriod\":\"ANNUAL\"}",
                semiMonthlyStr);

        String quarterlyStr = context.toJSON(quarterly);
        assertEquals("{\"type\":\"quarterly\",\"id\":\"quarterly1\",\"accrueStart\":\"1999-04-02\",\"accrueEnd\":\"2019-04-03\",\"firstPaymentDate\":\"1999-05-05\",\"apportionmentPeriod\":\"ANNUAL\"}",
                quarterlyStr);
    }


    @Test
    public void fromJSON() throws Exception {

        String annualStr = "{\"type\":\"annual\",\"id\":\"annual1a\",\"firstPaymentDate\":\"2015-07-04\",\"accrueStart\":\"2014-04-15\",\"accrueEnd\":\"2015-10-31\"}";
        String quarterlyStr = "{\"type\":\"quarterly\",\"id\":\"quarterly\",\"firstPaymentDate\":\"2015-07-04\",\"accrueStart\":\"2014-04-15\",\"accrueEnd\":\"2015-10-31\"}";
        String biweeklyStr = "{\"type\":\"biweekly\",\"id\":\"biweekly1a\",\"firstPeriodStart\":\"2014-12-20\",\"firstPaymentDate\":\"2014-12-26\",\"accrueStart\":\"2014-12-23\",\"accrueEnd\":\"2016-01-02\"}";
        String monthlyStr = "{\"type\":\"monthly\",\"id\":\"monthly1a\",\"firstPaymentDate\":\"1999-04-07\",\"accrueStart\":\"1999-04-02\",\"accrueEnd\":\"2019-04-03\"}";
        String semiMonthlyStr = "{\"type\":\"semimonthly\",\"id\":\"semiMonthly1a\",\"firstPaymentDate\":\"2016-02-19\",\"accrueStart\":\"2016-02-14\",\"accrueEnd\":\"2018-02-14\",\"firstDayOfMonth\":7,\"secondDayOfMonth\":19}";

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