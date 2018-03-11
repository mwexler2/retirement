package name.wexler.retirement;

import name.wexler.retirement.CashFlow.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by mwexler on 8/13/16.
 */
public class RSUTest {
    private Salary salary;
    private RSU rsu1;
    private RSU rsu2;
    private Context context;


    @Before
    public void setUp() throws Exception {
        context = new Context();

        Company employer = new Company(context, "employer1");
        Person employee = new Person(context, "employee1");
        Job job1 = new Job(context, "job1", employer, employee);
        job1.setStartDate(LocalDate.of(2015, Month.MAY, 1));
        job1.setEndDate(LocalDate.of(2016, Month.DECEMBER, 31));

        LocalDate job1FirstPaycheck = LocalDate.of(2015, Month.MAY, 15);
        Monthly monthly =
                new Monthly(context, "job1CashFlowSource1", job1.getStartDate(), job1.getEndDate(), job1FirstPaycheck,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        salary = new Salary(context, "salary1", "job1", monthly.getId());
        salary.setBaseAnnualSalary(BigDecimal.valueOf(100000.00));

        Annual job1AnnualVesting = new Annual(
                context, "job1AnnualVesting",
                LocalDate.of(2014, Month.APRIL, 28),
                LocalDate.of(2018, Month.APRIL, 28),
                LocalDate.of(2015, Month.APRIL, 28),
                CashFlowFrequency.ApportionmentPeriod.WHOLE_TERM);

        LocalDate job1FirstRSU = LocalDate.of(2016, Month.JUNE, 6);
        Security aapl = new Security(context, "AAPL", Arrays.asList("employee1"),
                new Balance(job1FirstRSU, BigDecimal.ZERO),
                new ArrayList<Balance>(0));
        List<Vesting> vestings = Arrays.asList(
                Vesting.of(12, BigDecimal.valueOf(0.05)),
                Vesting.of(24, BigDecimal.valueOf(0.15)),
                Vesting.of(30, BigDecimal.valueOf(0.20)),
                Vesting.of(36, BigDecimal.valueOf(0.20)),
                Vesting.of(42, BigDecimal.valueOf(0.20)),
                Vesting.of(48, BigDecimal.valueOf(0.20))
        );
        VestingSchedule vestingSchedule1 =
                new VestingSchedule(context, "vestingSchedule1",
                        job1FirstRSU,
                        job1FirstRSU.plusMonths(48),
                        job1FirstRSU.plusMonths(12),
                        vestings,
                        CashFlowFrequency.ApportionmentPeriod.WHOLE_TERM);
        rsu1 = new RSU(context,  "rsu1", "job1", "vestingSchedule1", "AAPL");

        LocalDate job1FirstPeriodStart = LocalDate.of(2015, Month.APRIL, 25);
        CashFlowFrequency biweeklySource = new Biweekly(context, "biweekly1",
                job1FirstPeriodStart, LocalDate.of(2010, Month.MAY, 17),
                LocalDate.of(2017, Month.MARCH, 1), job1FirstPaycheck,
                CashFlowFrequency.ApportionmentPeriod.WHOLE_TERM);
        rsu2 = new RSU(context, "rsu2", "job1", "biweekly1", "MGTX");

    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void getId() throws Exception {
        String name1 = salary.getId();
        assertEquals(name1, "salary1");
        String name2 = rsu1.getId();
        assertEquals(name2, "rsu1");
        String name3 = rsu2.getId();
        assertEquals(name3, "rsu2");
    }


    @Test
    public void equals() throws Exception {
        assertNotEquals(salary, rsu2);
    }

    @Test
    public void toJSON() throws Exception {
        String rsuVestingScheduleStr = context.toJSON(rsu1);
        assertEquals("{\"type\":\"RSU\",\"id\":\"rsu1\",\"job\":\"job1\",\"cashFlow\":\"vestingSchedule1\"," +
                        "\"security\":\"AAPL\"}",
                rsuVestingScheduleStr);

    }


    @Test
    public void fromJSON() throws Exception {
        String rsu1Str = "{\"type\":\"RSU\",\"id\":\"rsu1a\",\"job\":\"job1\",\"cashFlow\":\"vestingSchedule1" +
                "\", \"security\":\"AAPL\"}";

        CashFlowSource incomeSource2a = context.fromJSON(RSU.class, rsu1Str);
        assertEquals("rsu1a", incomeSource2a.getId());

        String rsu2Str = "{\"type\":\"RSU\",\"id\":\"rsu2a\",\"job\":\"job1\",\"cashFlow\":\"job1AnnualVesting\",\"security\":\"MGTX\"}}";

        CashFlowSource fixedRSU = context.fromJSON(RSU.class, rsu2Str);
        assertEquals("rsu2a", fixedRSU.getId());
    }

}