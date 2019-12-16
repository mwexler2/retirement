package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowFrequency.*;
import name.wexler.retirement.visualizer.CashFlowEstimator.*;
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

import static java.math.BigDecimal.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by mwexler on 8/13/16.
 */
public class BonusTest {
    private Salary salary;
    private BonusAnnualPct bonusAnnualPct;
    private BonusPeriodicFixed bonusPeriodicFixed;
    private Context context;


    @Before
    public void setUp() throws Exception {
        context = new Context();
        context.setAssumptions(new Assumptions());

        Company employer = new Company(context, "employer1");
        Person employee = new Person(context, "employee1", LocalDate.of(1976, Month.MARCH, 28), 65);
        Company bank = new Company(context, "bank1");
        CashFlowSink defaultSink = new AssetAccount(context, "checking1", Arrays.asList(employee.getId()),
                "Checking account 1", bank.getId(), Collections.emptyList());
        Job job1 = new Job(context, "job1", employer.getId(), employee.getId(), defaultSink.getId());
        job1.setStartDate(LocalDate.of(2015, Month.MAY, 1));
        job1.setEndDate(LocalDate.of(2016, Month.DECEMBER, 31));

        LocalDate job1FirstPaycheck = LocalDate.of(2015, Month.MAY, 15);
        Monthly monthly =
                new Monthly(context, "job1CashFlowSource1", job1.getStartDate(), job1.getEndDate(), job1FirstPaycheck,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        salary = new Salary(context, "salary1", "job1", monthly.getId(),
                valueOf(100000.00));

        LocalDate job1FirstBonus = LocalDate.of(2016, Month.JUNE, 6);
        Annual annual =
                new Annual(context, "job1BonusSource1",
                job1.getStartDate(), job1.getEndDate(), job1FirstBonus,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        bonusAnnualPct = new BonusAnnualPct(context,  "bonusAnnualPct1", "job1", "salary1", valueOf(10.0),
                annual.getId());

        LocalDate job1FirstPeriodStart = LocalDate.of(2015, Month.APRIL, 25);
        CashFlowFrequency biweeklySource =
                new Biweekly(context, "biweekly1", job1FirstPeriodStart, LocalDate.of(2010, Month.MAY, 17),
                LocalDate.of(2017, Month.MARCH, 1), job1FirstPaycheck,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        bonusPeriodicFixed = new BonusPeriodicFixed(context, "bonusPeriodicFixed1", "job1", valueOf(17000.00),
                biweeklySource.getId());

    }

    @After
    public void tearDown() {
    }


    @Test
    public void getId() {
        String name1 = salary.getId();
        assertEquals(name1, "salary1");
        String name2 = bonusAnnualPct.getId();
        assertEquals(name2, "bonusAnnualPct1");
        String name3 = bonusPeriodicFixed.getId();
        assertEquals(name3, "bonusPeriodicFixed1");
    }


    @Test
    public void equals() {
        assertNotEquals(salary, bonusAnnualPct);
    }

    @Test
    public void toJSON() throws Exception {
        String bonusAnnualPctStr = context.toJSON(bonusAnnualPct);
        assertEquals("{\"type\":\"bonusAnnualPct\",\"id\":\"bonusAnnualPct1\",\"job\":\"job1\",\"salary\":\"salary1\",\"bonusPct\":10.0,\"cashFlow\":\"job1BonusSource1\"}",
                bonusAnnualPctStr);
        String bonusPeriodicFixedStr = context.toJSON(bonusPeriodicFixed);
        assertEquals("{\"type\":\"bonusPeriodicFixed\",\"id\":\"bonusPeriodicFixed1\",\"job\":\"job1\",\"annualAmount\":17000.0,\"cashFlow\":\"biweekly1\"}",
                bonusPeriodicFixedStr);
    }


    @Test
    public void fromJSON() throws Exception {
        String bonus1Str = "{\"type\":\"bonusAnnualPct\",\"id\":\"bonus1a\",\"source\":null,\"job\":\"job1\",\"salary\":\"is1\",\"bonusPct\":10.0,\"cashFlow\":\"job1BonusSource1\"}";

        CashFlowSource incomeSource2a = context.fromJSON(Bonus.class, bonus1Str);
        assertEquals("bonus1a", incomeSource2a.getId());

        String bonus2Str = "{\"type\":\"bonusPeriodicFixed\",\"id\":\"bonusPeriodicFixed1a\",\"job\":\"job1\",\"annualAmount\":17000.0,\"cashFlow\":\"biweekly1\"}}";

        CashFlowSource fixedBonus = context.fromJSON(Bonus.class, bonus2Str);
        assertEquals("bonusPeriodicFixed1a", fixedBonus.getId());
    }

}