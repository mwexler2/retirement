package name.wexler.retirement;

import name.wexler.retirement.CashFlow.Annual;
import name.wexler.retirement.CashFlow.Biweekly;
import name.wexler.retirement.CashFlow.CashFlowType;
import name.wexler.retirement.CashFlow.Monthly;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

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

        Company employer = new Company(context, "employer1");
        Person employee = new Person(context, "employee1");
        Job job1 = new Job(context, "job1", employer, employee);
        job1.setStartDate(LocalDate.of(2015, Month.MAY, 1));
        job1.setEndDate(LocalDate.of(2016, Month.DECEMBER, 31));

        LocalDate job1FirstPaycheck = LocalDate.of(2015, Month.MAY, 15);
        Monthly monthly = new Monthly(context, "job1CashFlowSource1", job1.getStartDate(), job1.getEndDate(), job1FirstPaycheck);
        salary = new Salary(context, "salary1", "job1", monthly.getId());
        salary.setBaseAnnualSalary(BigDecimal.valueOf(100000.00));

        LocalDate job1FirstBonus = LocalDate.of(2016, Month.JUNE, 6);
        Annual annual = new Annual(context, "job1BonusSource1",
                job1.getStartDate(), job1.getEndDate(), job1FirstBonus);
        bonusAnnualPct = new BonusAnnualPct(context,  "bonusAnnualPct1", "job1", "salary1", BigDecimal.valueOf(10.0),
                annual.getId());

        LocalDate job1FirstPeriodStart = LocalDate.of(2015, Month.APRIL, 25);
        CashFlowType biweeklySource = new Biweekly(context, "biweekly1", job1FirstPeriodStart, LocalDate.of(2010, Month.MAY, 17),
                LocalDate.of(2017, Month.MARCH, 1), job1FirstPaycheck);
        bonusPeriodicFixed = new BonusPeriodicFixed(context, "bonusPeriodicFixed1", "job1", BigDecimal.valueOf(17000.00),
                biweeklySource.getId());

    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void getId() throws Exception {
        String name1 = salary.getId();
        assertEquals(name1, "salary1");
        String name2 = bonusAnnualPct.getId();
        assertEquals(name2, "bonusAnnualPct1");
        String name3 = bonusPeriodicFixed.getId();
        assertEquals(name3, "bonusPeriodicFixed1");
    }


    @Test
    public void equals() throws Exception {
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

        IncomeSource incomeSource2a = context.fromJSON(Bonus.class, bonus1Str);
        assertEquals("bonus1a", incomeSource2a.getId());

        String bonus2Str = "{\"type\":\"bonusPeriodicFixed\",\"id\":\"bonusPeriodicFixed1a\",\"job\":\"job1\",\"annualAmount\":17000.0,\"cashFlow\":\"biweekly1\"}}";

        IncomeSource fixedBonus = context.fromJSON(Bonus.class, bonus2Str);
        assertEquals("bonusPeriodicFixed1a", fixedBonus.getId());
    }

}