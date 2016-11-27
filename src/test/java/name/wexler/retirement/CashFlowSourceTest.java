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
        annual = new Annual(context, "annual1", MonthDay.of(Month.JULY, 4), 2014, 2015);
        biweekly = new Biweekly(context, "biweekly1", DayOfWeek.THURSDAY,
                LocalDate.of(2014, Month.DECEMBER, 23),
                LocalDate.of(2016, Month.JANUARY, 2));
        monthly = new Monthly(context, "monthly1", 7,
                LocalDate.of(1999, Month.APRIL, 2),
                LocalDate.of(2019, Month.APRIL, 3));
        semiMonthly = new SemiMonthly(context, "semiMonthly1",
                7, 19,
                LocalDate.of(2016, Month.FEBRUARY, 14),
                LocalDate.of(2018, Month.FEBRUARY, 14)
        );
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void getStartDate() throws Exception {
        assertEquals(LocalDate.of(2014, Month.DECEMBER, 23), biweekly.getStartDate());
        assertEquals(LocalDate.of(1999, Month.APRIL, 2), monthly.getStartDate());
        assertEquals(LocalDate.of(2016, Month.FEBRUARY, 14), semiMonthly.getStartDate());
    }



    @Test
    public void toJSON() throws Exception {
        String annualStr = context.toJSON(annual);
        assertEquals("{\"type\":\"annual\",\"id\":\"annual1\",\"monthDay\":\"--07-04\",\"firstYear\":2014,\"lastYear\":2015}",
                annualStr);

        String biweeklyStr = context.toJSON(biweekly);
        assertEquals("{\"type\":\"biweekly\",\"id\":\"biweekly1\",\"dayOfWeek\":\"THURSDAY\",\"startDate\":\"2014-12-23\",\"endDate\":\"2016-01-02\"}",
                biweeklyStr);

        String monthlyStr = context.toJSON(monthly);
        assertEquals("{\"type\":\"monthly\",\"id\":\"monthly1\",\"firstDayOfMonth\":7,\"startDate\":\"1999-04-02\",\"endDate\":\"2019-04-03\"}",
                monthlyStr);

        String semiMonthlyStr = context.toJSON(semiMonthly);
        assertEquals("{\"type\":\"semimonthly\",\"id\":\"semiMonthly1\",\"startDate\":\"2016-02-14\",\"endDate\":\"2018-02-14\",\"firstDayOfMonth\":7,\"secondDayOfMonth\":19}",
                semiMonthlyStr);
    }


    @Test
    public void fromJSON() throws Exception {

        String annualStr = "{\"type\":\"annual\",\"id\":\"annual1a\",\"monthDay\":\"--07-04\",\"firstYear\":2014,\"lastYear\":2015}";
        String biweeklyStr = "{\"type\":\"biweekly\",\"id\":\"biweekly1a\",\"dayOfWeek\":\"THURSDAY\",\"startDate\":\"2014-12-23\",\"endDate\":\"2016-01-02\"}";
        String monthlyStr = "{\"type\":\"monthly\",\"id\":\"monthly1a\",\"firstDayOfMonth\":7,\"startDate\":\"1999-04-02\",\"endDate\":\"2019-04-03\"}";
        String semiMonthlyStr = "{\"type\":\"semimonthly\",\"id\":\"semiMonthly1a\",\"startDate\":\"2016-02-14\",\"endDate\":\"2018-02-14\",\"firstDayOfMonth\":7,\"secondDayOfMonth\":19}";

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