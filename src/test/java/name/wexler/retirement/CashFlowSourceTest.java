package name.wexler.retirement;

import name.wexler.retirement.CashFlow.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by mwexler on 8/13/16.
 */
public class CashFlowSourceTest {
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
        LocalDate biweeklyFirstPayment = LocalDate.of(2015, Month.MARCH, 15);
        biweekly = new Biweekly(context, "biweekly1",
                biweeklyAccrueStart,
                biweeklyAccrueEnd,
                biweeklyFirstPayment);

        LocalDate monthlyAccrueStart = LocalDate.of(1999, Month.APRIL, 2);
        LocalDate monthlyAccrueEnd = LocalDate.of(2019, Month.APRIL, 3);
        LocalDate monthlyFirstPayment = LocalDate.of(2015, Month.JANUARY, 7);
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
    public void getAccrueStart() throws Exception {
        assertEquals(LocalDate.of(2014, Month.DECEMBER, 23), biweekly.getAccrueStart());
        assertEquals(LocalDate.of(1999, Month.APRIL, 2), monthly.getAccrueStart());
        assertEquals(LocalDate.of(2016, Month.FEBRUARY, 14), semiMonthly.getAccrueStart());
        assertEquals(LocalDate.of(2014, Month.AUGUST, 17), annual.getAccrueStart());
    }



    @Test
    public void toJSON() throws Exception {
        String annualStr = context.toJSON(annual);
        assertEquals("{\"type\":\"annual\",\"id\":\"annual1\",\"accrueStart\":\"2014-08-17\",\"accrueEnd\":\"2015-09-20\",\"firstPaymentDate\":\"2015-03-15\"}",
                annualStr);

        String biweeklyStr = context.toJSON(biweekly);
        assertEquals("{\"type\":\"biweekly\",\"id\":\"biweekly1\",\"accrueStart\":\"2014-12-23\",\"accrueEnd\":\"2016-01-02\",\"firstPaymentDate\":\"2015-03-15\"}",
                biweeklyStr);

        String monthlyStr = context.toJSON(monthly);
        assertEquals("{\"type\":\"monthly\",\"id\":\"monthly1\",\"accrueStart\":\"1999-04-02\",\"accrueEnd\":\"2019-04-03\",\"firstPaymentDate\":\"2015-01-07\"}",
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