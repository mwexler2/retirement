package name.wexler.retirement.CashFlowFrequency;

import name.wexler.retirement.*;
import name.wexler.retirement.Asset.Asset;
import name.wexler.retirement.Asset.RealProperty;
import name.wexler.retirement.CashFlowSource.*;
import name.wexler.retirement.Entity.Company;
import name.wexler.retirement.Entity.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;

/**
 * Created by mwexler on 8/13/16.
 */

public class CashFlowCalendarTest {
    private Context context;
    private CashFlowCalendar calendar;
    private List<CashFlowSource> cashFlowSources;

    @Before
    public void setUp() throws Exception {
        Assumptions assumptions = new Assumptions();
        context = new Context();
        context.setAssumptions(assumptions);
        calendar = new CashFlowCalendar(assumptions);
        cashFlowSources = new ArrayList<>();

        Company employer = new Company(context, "employer1");
        employer.setCompanyName("Employment Co");
        Person employee = new Person(context, "employee1", LocalDate.of(1966, Month.APRIL, 1), 62);
        Job job1 = new Job(context, "job1", employer, employee);
        LocalDate job1StartDate = LocalDate.of(2015, Month.MAY, 1);
        job1.setStartDate((job1StartDate));
        LocalDate job1EndDate = LocalDate.of(2015, Month.DECEMBER, 31);
        job1.setEndDate(job1EndDate);

        LocalDate job1FirstPaycheck = LocalDate.of(2015, Month.MAY, 15);
        Monthly monthly = new Monthly(context, "job1CashFlowSource1", job1.getStartDate(), job1.getEndDate(),
                job1FirstPaycheck, CashFlowFrequency.ApportionmentPeriod.EQUAL_MONTHLY);
        Salary salary = new Salary(context, "salary1", "job1", monthly.getId(),
                BigDecimal.valueOf(100000.00));
        cashFlowSources.add(salary);

        LocalDate job1FirstBonus = LocalDate.of(2016, Month.JUNE, 6);
        Annual annual = new Annual(context, "job1BonusSource1",
                job1.getStartDate(), job1.getEndDate(), job1FirstBonus,
                CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        BonusAnnualPct bonusAnnualPct = new BonusAnnualPct(context,  "bonusAnnualPct1", "job1", "salary1", BigDecimal.valueOf(10.0),
                annual.getId());
        cashFlowSources.add(bonusAnnualPct);

        LocalDate job1FirstPeriodStart = LocalDate.of(2015, Month.APRIL, 25);
        CashFlowFrequency biweeklySource =
                new Biweekly(context, "biweekly1", job1FirstPeriodStart, job1StartDate, job1EndDate, job1FirstPaycheck,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        BonusPeriodicFixed bonusPeriodicFixed = new BonusPeriodicFixed(context, "bonusPeriodicFixed1", "job1", BigDecimal.valueOf(17000.00),
                biweeklySource.getId());
        cashFlowSources.add(bonusPeriodicFixed);

        Company lender = new Company(context, "lender1");
        lender.setCompanyName("Lender's Bank");
        Person borrower = new Person(context, "borrower1", LocalDate.of(2000, Month.JANUARY, 1), 70);
        List<String> borrowerIds = Arrays.asList(borrower.getId());
        String[] streetAddress = {"123 Main Street"};
        List<CashBalance> interimBalances = Arrays.asList(new CashBalance(LocalDate.of(2017, 7, 4), BigDecimal.valueOf(42000.42)));
        CashBalance initialBalance = new CashBalance(LocalDate.of(2010, Month.APRIL, 15), BigDecimal.valueOf(100000.0));
        Asset asset = new RealProperty(context, "real-property1", borrowerIds, initialBalance,
                streetAddress,
                "Anytown", "Count County", "AS", "01234", "US", interimBalances);
        String[] borrowers = { borrower.getId() };
        LocalDate accrueStart = LocalDate.of(2011, Month.MAY, 1);
        LocalDate accrueEnd = LocalDate.of(2031, Month.APRIL, 1);
        LocalDate firstPaymentDate = LocalDate.of(accrueStart.getYear(), accrueStart.getMonth(), 14);
        CashFlowFrequency monthlyPayment =
                new Monthly(context, "monthly-debt1", accrueStart, accrueEnd, firstPaymentDate,
                        CashFlowFrequency.ApportionmentPeriod.WHOLE_TERM);
        Liability debt = new Liability(context, "debt1", lender.getId(), borrowers, asset,
                LocalDate.of(2014, Month.OCTOBER, 10),
                LocalDate.of(2020, Month.APRIL, 27),
                30 * 12, BigDecimal.valueOf(3.875/12), BigDecimal.valueOf(50000.0),
                BigDecimal.valueOf(500.00), BigDecimal.ZERO, monthlyPayment.getId());
        cashFlowSources.add(debt);
        calendar.addCashFlowSources(cashFlowSources);

    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void addIncomeSources() {
        calendar.addCashFlowSources(cashFlowSources);
    }


    @Test
    public void getYears() {
        List<Integer> result = calendar.getYears();
        assertEquals(21, result.size());
        assertEquals(2011, result.get(0).intValue());
        assertEquals(2031, result.get(20).intValue());
    }

    @Test
    public void getIncomeCashFlowIds() {
        Map<String, String> result = calendar.getCashFlowNameAndIds();
        System.out.println("cashFlowIds = " + result);
        assertEquals(4, result.size());
        assertThat(result.keySet(), hasItem("bonusPeriodicFixed1"));
        assertThat(result.keySet(), hasItem("salary1"));
        assertThat(result.keySet(), hasItem("bonusAnnualPct1"));
    }

    @Test
    public void getAnnualExpense() {
        assertEquals(BigDecimal.valueOf(4000.00).setScale(2, RoundingMode.HALF_UP), calendar.getAnnualCashFlow("debt1", 2011));
        assertEquals(BigDecimal.valueOf(6000.00).setScale(2, RoundingMode.HALF_UP), calendar.getAnnualCashFlow("debt1", 2012));
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), calendar.getAnnualCashFlow("debt1", 2030));
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), calendar.getAnnualCashFlow("debt1", 2031));
        assertEquals(BigDecimal.ZERO, calendar.getAnnualCashFlow("debt1", 1999));
        assertEquals(BigDecimal.ZERO, calendar.getAnnualCashFlow("bad-debt", 2012));
        assertEquals(BigDecimal.ZERO, calendar.getAnnualCashFlow("bad-debt", 1999));
    }

    @Test
    public void getAnnualIncome() {
        assertEquals(BigDecimal.valueOf(10835.23).setScale(2), calendar.getAnnualCashFlow("bonusPeriodicFixed1", 2015));
        assertEquals(BigDecimal.valueOf(66666.64).setScale(2), calendar.getAnnualCashFlow("salary1",             2015));
        assertEquals(BigDecimal.ZERO, calendar.getAnnualCashFlow("bonusAnnualPct1",     2012));
        assertEquals(BigDecimal.valueOf(607.14).setScale(2),calendar.getAnnualCashFlow("bonusPeriodicFixed1", 2016));
        assertEquals(BigDecimal.ZERO, calendar.getAnnualCashFlow("salary1",             2016));
        assertEquals(BigDecimal.valueOf(1000000.00).setScale(2), calendar.getAnnualCashFlow("bonusAnnualPct1",     2016));
        assertEquals(BigDecimal.ZERO,calendar.getAnnualCashFlow("bonusPeriodicFixed1", 2017));
        assertEquals(BigDecimal.ZERO, calendar.getAnnualCashFlow("salary1",             2017));
        assertEquals(BigDecimal.ZERO, calendar.getAnnualCashFlow("bonusAnnualPct1",     2017));
        assertEquals(BigDecimal.ZERO, calendar.getAnnualCashFlow("salary1",             1999));
        assertEquals(BigDecimal.ZERO, calendar.getAnnualCashFlow("bad-salary",          2016));
        assertEquals(BigDecimal.ZERO, calendar.getAnnualCashFlow("bad-salary",          1999));
    }
}