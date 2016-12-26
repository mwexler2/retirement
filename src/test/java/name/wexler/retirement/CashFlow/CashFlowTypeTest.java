package name.wexler.retirement.CashFlow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import name.wexler.retirement.CashFlow.*;
import name.wexler.retirement.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.*;

/**
 * Created by mwexler on 8/13/16.
 */
public class CashFlowTypeTest {
    Annual annual;
    Biweekly biweekly;
    Monthly monthly;
    SemiMonthly semiMonthly;
    Context context;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        LocalDate annualAccrueStart = LocalDate.of(2014, Month.AUGUST, 17);
        LocalDate annualAccrueEnd = LocalDate.of(2015, Month.SEPTEMBER, 20);
        LocalDate annualFirstPayment = LocalDate.of(2015, Month.MARCH, 15);
        annual = new Annual(context, "annual1", annualAccrueStart, annualAccrueEnd, annualFirstPayment);

        LocalDate biweeklyAccrueStart = LocalDate.of(2014, Month.DECEMBER, 23);
        LocalDate biweeklyAccrueEnd = LocalDate.of(2016, Month.JANUARY, 2);
        LocalDate biweeklyFirstPayment = LocalDate.of(2015, Month.JANUARY, 1);
        biweekly = new Biweekly(context, "biweekly1",
                biweeklyAccrueStart,
                biweeklyAccrueEnd,
                biweeklyFirstPayment);

        LocalDate monthlyAccrueStart = LocalDate.of(1999, Month.APRIL, 2);
        LocalDate monthlyAccrueEnd = LocalDate.of(2019, Month.APRIL, 3);
        LocalDate monthlyFirstPayment = LocalDate.of(1999, Month.MAY, 5);
        monthly = new Monthly(context, "monthly1",
                monthlyAccrueStart,
                monthlyAccrueEnd,
                monthlyFirstPayment);

        LocalDate semimonthlyAccrueStart = LocalDate.of(2016, Month.FEBRUARY, 14);
        LocalDate semimonthlyAccrueEnd = LocalDate.of(2018, Month.FEBRUARY, 14);
        LocalDate semimonthlyFirstPayment = LocalDate.of(2016, Month.FEBRUARY, 19);
        semiMonthly = new SemiMonthly(context, "semiMonthly1",
                semimonthlyAccrueStart,
                semimonthlyAccrueEnd,
                semimonthlyFirstPayment,
                7, 19
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
            CashFlowType duplicate = new Annual(context, "annual1", annualAccrueStart, annualAccrueEnd, annualFirstPayment);
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
    }

    @Test()
    public void getMonthlyCashFlow()  {
        BigDecimal result = biweekly.getMonthlyCashFlow(YearMonth.of(2015, Month.JANUARY), BigDecimal.valueOf(50000.00));
        assertEquals(BigDecimal.valueOf(1923.08), result);

        result = annual.getMonthlyCashFlow(YearMonth.of(2015, Month.JANUARY), BigDecimal.valueOf(50000.00));
        assertEquals(BigDecimal.ZERO, result);

        result = semiMonthly.getMonthlyCashFlow(YearMonth.of(2015, Month.JANUARY), BigDecimal.valueOf(50000.00));
        assertEquals(BigDecimal.ZERO, result);

        result = monthly.getMonthlyCashFlow(YearMonth.of(2015, Month.JUNE), BigDecimal.valueOf(3523.00));
        assertEquals(BigDecimal.valueOf(293.58), result);
    }

    @Test()
    public void getFlowInstances()  {
        BigDecimal annualAmount = BigDecimal.valueOf(1000.00);

        List<CashFlowInstance> biweeklyCashFlows = biweekly.getCashFlowInstances(annualAmount);
//        assertEquals(LocalDate.of(1999, Month.MAY, 5), biweeklyCashFlows.get(0).getCashFlowDate());
//        assertEquals(LocalDate.of(2019, Month.MAY, 5), biweeklyCashFlows.get(biweeklyCashFlows.size() - 1).getCashFlowDate());
 //       assertEquals(BigDecimal.valueOf(38.46), biweeklyCashFlows.get(0).getAmount());
//        assertEquals(BigDecimal.valueOf(38.46), biweeklyCashFlows.get(biweeklyCashFlows.size() - 1).getAmount());
//        assertEquals(241, biweeklyCashFlows.size());
        System.out.println("biweekly:");
        System.out.println(biweeklyCashFlows);
        System.out.println("");

        List<CashFlowInstance> monthlyCashFlows = monthly.getCashFlowInstances(annualAmount);
        assertEquals(LocalDate.of(1999, Month.MAY, 5), monthlyCashFlows.get(0).getCashFlowDate());
        assertEquals(LocalDate.of(2019, Month.MAY, 5), monthlyCashFlows.get(monthlyCashFlows.size() - 1).getCashFlowDate());
        assertEquals(BigDecimal.valueOf(83.33), monthlyCashFlows.get(0).getAmount());
        assertEquals(BigDecimal.valueOf(83.33), monthlyCashFlows.get(monthlyCashFlows.size() - 1).getAmount());
        assertEquals(241, monthlyCashFlows.size());

        List<CashFlowInstance> semiMonthlyCashFlows = semiMonthly.getCashFlowInstances(annualAmount);
        System.out.println("semiMonthly:");
        System.out.println(semiMonthlyCashFlows);
        System.out.println("");

        List<CashFlowInstance> annualCashFlows = annual.getCashFlowInstances(annualAmount);
        assertEquals(2, annualCashFlows.size());
        assertEquals(annualAmount, annualCashFlows.get(0).getAmount());
        assertEquals(LocalDate.of(2015, Month.MARCH, 15), annualCashFlows.get(0).getCashFlowDate());
        assertEquals(annualAmount, annualCashFlows.get(1).getAmount());
        assertEquals(LocalDate.of(2016, Month.MARCH, 15), annualCashFlows.get(1).getCashFlowDate());
    }

    @Test
    public void getAnnualCashFlow() {
        BigDecimal result = annual.getAnnualCashFlow(2015, BigDecimal.valueOf(34334.22));
        assertEquals(BigDecimal.valueOf(34334.22), result);

        result = semiMonthly.getAnnualCashFlow(BigDecimal.valueOf(17000.00));
        assertTrue("result of annual cashflow doesn't match", BigDecimal.valueOf(15526.70).compareTo(result) == 0);
    }

    @Test
    public void getAccrueEnd() {
        LocalDate result = biweekly.getAccrueEnd();
        assertEquals(LocalDate.of(2016, Month.JANUARY, 2), result);
    }

    @Test
    public void getFirstPaymentDate() {
        LocalDate result = monthly.getFirstPaymentDate();
        assertEquals(LocalDate.of(1999, Month.MAY, 5), result);
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
        assertEquals(DayOfWeek.THURSDAY, result);
    }

    @Test
    public void toJSON() throws Exception {
        String annualStr = context.toJSON(annual);
        assertEquals("{\"type\":\"annual\",\"id\":\"annual1\",\"accrueStart\":\"2014-08-17\",\"accrueEnd\":\"2015-09-20\",\"firstPaymentDate\":\"2015-03-15\"}",
                annualStr);

        String biweeklyStr = context.toJSON(biweekly);
        assertEquals("{\"type\":\"biweekly\",\"id\":\"biweekly1\",\"accrueStart\":\"2014-12-23\",\"accrueEnd\":\"2016-01-02\",\"firstPaymentDate\":\"2015-01-01\"}",
                biweeklyStr);

        String monthlyStr = context.toJSON(monthly);
        assertEquals("{\"type\":\"monthly\",\"id\":\"monthly1\",\"accrueStart\":\"1999-04-02\",\"accrueEnd\":\"2019-04-03\",\"firstPaymentDate\":\"1999-05-05\"}",
                monthlyStr);

        String semiMonthlyStr = context.toJSON(semiMonthly);
        assertEquals("{\"type\":\"semimonthly\",\"id\":\"semiMonthly1\",\"accrueStart\":\"2016-02-14\",\"accrueEnd\":\"2018-02-14\",\"firstPaymentDate\":\"2016-02-19\",\"firstDayOfMonth\":7,\"secondDayOfMonth\":19}",
                semiMonthlyStr);
    }


    @Test
    public void fromJSON() throws Exception {

        String annualStr = "{\"type\":\"annual\",\"id\":\"annual1a\",\"firstPaymentDate\":\"2015-07-04\",\"accrueStart\":\"2014-04-15\",\"accrueEnd\":\"2015-10-31\"}";
        String biweeklyStr = "{\"type\":\"biweekly\",\"id\":\"biweekly1a\",\"firstPaymentDate\":\"2014-12-26\",\"accrueStart\":\"2014-12-23\",\"accrueEnd\":\"2016-01-02\"}";
        String monthlyStr = "{\"type\":\"monthly\",\"id\":\"monthly1a\",\"firstPaymentDate\":\"1999-04-07\",\"accrueStart\":\"1999-04-02\",\"accrueEnd\":\"2019-04-03\"}";
        String semiMonthlyStr = "{\"type\":\"semimonthly\",\"id\":\"semiMonthly1a\",\"firstPaymentDate\":\"2016-02-19\",\"accrueStart\":\"2016-02-14\",\"accrueEnd\":\"2018-02-14\",\"firstDayOfMonth\":7,\"secondDayOfMonth\":19}";

        Annual annual1a = context.<Annual>fromJSON(Annual.class, annualStr);
        assertEquals(annual1a.getId(), "annual1a");

        Biweekly biweekly1a = context.<Biweekly>fromJSON(Biweekly.class, biweeklyStr);
        assertEquals(biweekly1a.getId(), "biweekly1a");

        Monthly monthly1a = context.<Monthly>fromJSON(Monthly.class, monthlyStr);
        assertEquals(monthly1a.getId(), "monthly1a");

        SemiMonthly semiMonthly1a = context.<SemiMonthly>fromJSON(SemiMonthly.class, semiMonthlyStr);
        assertEquals(semiMonthly1a.getId(), "semiMonthly1a");

    }

}