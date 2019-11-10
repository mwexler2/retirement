package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowFrequency.*;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.CashFlowEstimator.RSU;
import name.wexler.retirement.visualizer.CashFlowEstimator.Salary;
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
        context.setAssumptions(new Assumptions());

        Company employer = new Company(context, "employer1");
        Person employee = new Person(context, "employee1", LocalDate.of(1776, Month.JULY, 4), 200);
        Company bank = new Company(context, "bank1");
        CashFlowSink defaultSink = new AssetAccount(context, "checking1", Arrays.asList(employee.getId()),
                new CashBalance(LocalDate.of(2015, 10, 1), BigDecimal.ZERO), Collections.emptyList(),
                "Checking account 1", bank.getId(), Collections.emptyList());
        Job job1 = new Job(context, "job1", employer.getId(), employee.getId(), defaultSink.getId());
        job1.setStartDate(LocalDate.of(2015, Month.MAY, 1));
        job1.setEndDate(LocalDate.of(2016, Month.DECEMBER, 31));

        LocalDate job1FirstPaycheck = LocalDate.of(2015, Month.MAY, 15);
        Monthly monthly =
                new Monthly(context, "job1CashFlowSource1", job1.getStartDate(), job1.getEndDate(), job1FirstPaycheck,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        salary = new Salary(context, "salary1", "job1", monthly.getId(),
                BigDecimal.valueOf(100000.00));

        Annual job1AnnualVesting = new Annual(
                context, "job1AnnualVesting",
                LocalDate.of(2014, Month.APRIL, 28),
                LocalDate.of(2018, Month.APRIL, 28),
                LocalDate.of(2015, Month.APRIL, 28),
                CashFlowFrequency.ApportionmentPeriod.WHOLE_TERM);

        LocalDate job1FirstRSU = LocalDate.of(2016, Month.JUNE, 6);
        Security aapl = new Security(context, "AAPL");
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
                        vestings);
        rsu1 = new RSU(context,  "rsu1", "job1", "vestingSchedule1", "AAPL", 1000);

        LocalDate job1FirstPeriodStart = LocalDate.of(2015, Month.APRIL, 25);
        CashFlowFrequency biweeklySource = new Biweekly(context, "biweekly1",
                job1FirstPeriodStart, LocalDate.of(2010, Month.MAY, 17),
                LocalDate.of(2017, Month.MARCH, 1), job1FirstPaycheck,
                CashFlowFrequency.ApportionmentPeriod.WHOLE_TERM);
        rsu2 = new RSU(context, "rsu2", "job1", "biweekly1", "MGTX", 1500);

    }

    @After
    public void tearDown() {
    }


    @Test
    public void getId() {
        String name1 = salary.getId();
        assertEquals(name1, "salary1");
        String name2 = rsu1.getId();
        assertEquals(name2, "rsu1");
        String name3 = rsu2.getId();
        assertEquals(name3, "rsu2");
    }


    @Test
    public void equals() {
        assertNotEquals(salary, rsu2);
    }

    @Test
    public void toJSON() throws Exception {
        String rsuVestingScheduleStr = context.toJSON(rsu1);
        assertEquals("{\"type\":\"RSU\",\"id\":\"rsu1\",\"job\":\"job1\",\"cashFlow\":\"vestingSchedule1\"," +
                        "\"security\":\"AAPL\",\"totalShares\":1000}",
                rsuVestingScheduleStr);

    }


    @Test
    public void fromJSON() throws Exception {
        String rsu1Str = "{\"type\":\"RSU\",\"id\":\"rsu1a\",\"job\":\"job1\",\"cashFlow\":\"vestingSchedule1" +
                "\", \"security\":\"AAPL\",\"totalShares\":1500}";

        CashFlowEstimator incomeSource2a = context.fromJSON(RSU.class, rsu1Str);
        assertEquals("rsu1a", incomeSource2a.getId());

        String rsu2Str = "{\"type\":\"RSU\",\"id\":\"rsu2a\",\"job\":\"job1\",\"cashFlow\":\"job1AnnualVesting\",\"security\":\"MGTX\",\"totalShares\":700}}";

        CashFlowEstimator fixedRSU = context.fromJSON(RSU.class, rsu2Str);
        assertEquals("rsu2a", fixedRSU.getId());
    }

}