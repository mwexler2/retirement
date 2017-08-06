package name.wexler.retirement.CashFlow;

import name.wexler.retirement.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
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
    private List<IncomeSource> incomeSources;
    private List<ExpenseSource> expenseSources;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        calendar = new CashFlowCalendar();
        incomeSources = new ArrayList<>();
        expenseSources = new ArrayList<>();

        Company employer = new Company(context, "employer1");
        employer.setCompanyName("Employment Co");
        Person employee = new Person(context, "employee1");
        Job job1 = new Job(context, "job1", employer, employee);
        job1.setStartDate(LocalDate.of(2015, Month.MAY, 1));
        job1.setEndDate(LocalDate.of(2016, Month.DECEMBER, 31));

        LocalDate job1FirstPaycheck = LocalDate.of(2015, Month.MAY, 15);
        Monthly monthly = new Monthly(context, "job1CashFlowSource1", job1.getStartDate(), job1.getEndDate(), job1FirstPaycheck);
        Salary salary = new Salary(context, "salary1", "job1", monthly.getId());
        salary.setBaseAnnualSalary(BigDecimal.valueOf(100000.00));
        incomeSources.add(salary);

        LocalDate job1FirstBonus = LocalDate.of(2016, Month.JUNE, 6);
        Annual annual = new Annual(context, "job1BonusSource1",
                job1.getStartDate(), job1.getEndDate(), job1FirstBonus);
        BonusAnnualPct bonusAnnualPct = new BonusAnnualPct(context,  "bonusAnnualPct1", "job1", "salary1", BigDecimal.valueOf(10.0),
                annual.getId());
        incomeSources.add(bonusAnnualPct);

        LocalDate job1FirstPeriodStart = LocalDate.of(2015, Month.APRIL, 25);
        CashFlowType biweeklySource = new Biweekly(context, "biweekly1", job1FirstPeriodStart, LocalDate.of(2010, Month.MAY, 17),
                LocalDate.of(2017, Month.MARCH, 1), job1FirstPaycheck);
        BonusPeriodicFixed bonusPeriodicFixed = new BonusPeriodicFixed(context, "bonusPeriodicFixed1", "job1", BigDecimal.valueOf(17000.00),
                biweeklySource.getId());
        incomeSources.add(bonusPeriodicFixed);
        calendar.addIncomeSources(incomeSources);

        Company lender = new Company(context, "lender1");
        lender.setCompanyName("Lender's Bank");
        Person borrower = new Person(context, "borrower1");
        String[] streetAddress = {"123 Main Street"};
        Asset asset = new RealProperty(context, "real-property1", borrower.getId(), BigDecimal.valueOf(100000.00), LocalDate.of(2010, Month.APRIL, 15),
                streetAddress,
                "Anytown", "Count County", "AS", "01234", "US");
        String[] borrowers = { borrower.getId() };
        LocalDate accrueStart = LocalDate.of(2011, Month.MAY, 1);
        LocalDate accrueEnd = LocalDate.of(2031, Month.APRIL, 1);
        LocalDate firstPaymentDate = LocalDate.of(accrueStart.getYear(), accrueStart.getMonth(), 14);
        CashFlowType monthlyPayment = new Monthly(context, "monthly-debt1", accrueStart, accrueEnd, firstPaymentDate);
        Liability debt = new Liability(context, "debt1", lender.getId(), borrowers, asset,
                LocalDate.of(2014, Month.OCTOBER, 10),
                LocalDate.of(2020, Month.APRIL, 27),
                30 * 12, BigDecimal.valueOf(3.875/12), BigDecimal.valueOf(50000.0),
                BigDecimal.valueOf(500.00), monthlyPayment.getId());
        expenseSources.add(debt);
        calendar.addExpenseSources(expenseSources);

    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void addIncomeSources() {
        calendar.addIncomeSources(incomeSources);
    }

    @Test
    public void addExpenseSources() {
        calendar.addExpenseSources(expenseSources);
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
        Map<String, String> result = calendar.getIncomeCashFlowNameAndIds();
        System.out.println("cashFlowIds = " + result);
        assertEquals(3, result.size());
        assertThat(result.keySet(), hasItem("bonusPeriodicFixed1"));
        assertThat(result.keySet(), hasItem("salary1"));
        assertThat(result.keySet(), hasItem("bonusAnnualPct1"));
    }

    @Test
    public void getAnnualExpense() {
        assertEquals(0, BigDecimal.valueOf(4000.00).compareTo(calendar.getAnnualExpense("debt1", 2011)));
        assertEquals(0, BigDecimal.valueOf(6000.00).compareTo(calendar.getAnnualExpense("debt1", 2012)));
        assertEquals(0, BigDecimal.valueOf(6000.00).compareTo(calendar.getAnnualExpense("debt1", 2030)));
        assertEquals(0, BigDecimal.valueOf(2000.00).compareTo(calendar.getAnnualExpense("debt1", 2031)));
        assertEquals(0, BigDecimal.ZERO.compareTo(calendar.getAnnualExpense("debt1", 1999)));
        assertEquals(0, BigDecimal.ZERO.compareTo(calendar.getAnnualExpense("bad-debt", 2012)));
        assertEquals(0, BigDecimal.ZERO.compareTo(calendar.getAnnualExpense("bad-debt", 1999)));
    }

    @Test
    public void getAnnualIncome() {
        assertEquals(0, BigDecimal.valueOf(11115.45).  compareTo(calendar.getAnnualIncome("bonusPeriodicFixed1", 2015)));
        assertEquals(0, BigDecimal.valueOf(66666.64).  compareTo(calendar.getAnnualIncome("salary1",             2015)));
        assertEquals(0, BigDecimal.ZERO.               compareTo(calendar.getAnnualIncome("bonusAnnualPct1",     2012)));
        assertEquals(0, BigDecimal.valueOf(17000.10).  compareTo(calendar.getAnnualIncome("bonusPeriodicFixed1", 2016)));
        assertEquals(0, BigDecimal.valueOf(99999.96).  compareTo(calendar.getAnnualIncome("salary1",             2016)));
        assertEquals(0, BigDecimal.valueOf(1000000.00).compareTo(calendar.getAnnualIncome("bonusAnnualPct1",     2016)));
        assertEquals(0, BigDecimal.valueOf(3923.10).   compareTo(calendar.getAnnualIncome("bonusPeriodicFixed1", 2017)));
        assertEquals(0, BigDecimal.ZERO.               compareTo(calendar.getAnnualIncome("salary1",             2017)));
        assertEquals(0, BigDecimal.valueOf(1000000.00).compareTo(calendar.getAnnualIncome("bonusAnnualPct1",     2017)));
        assertEquals(0, BigDecimal.ZERO.               compareTo(calendar.getAnnualIncome("salary1",             1999)));
        assertEquals(0, BigDecimal.ZERO.               compareTo(calendar.getAnnualIncome("bad-salary",          2016)));
        assertEquals(0, BigDecimal.ZERO.               compareTo(calendar.getAnnualIncome("bad-salary",          1999)));
    }

    @Test
    public void getExpenseCashFlowIds() {
        Map<String, String> result = calendar.getExpenseCashFlowNameAndIds();
        System.out.println("cashFlowIds = " + result);
        assertEquals(1, result.size());
        assertThat(result.keySet(), hasItem("debt1"));
    }
}