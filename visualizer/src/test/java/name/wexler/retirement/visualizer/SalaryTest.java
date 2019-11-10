package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.visualizer.CashFlowFrequency.Monthly;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
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

import static org.junit.Assert.assertEquals;

/**
 * Created by mwexler on 8/13/16.
 */
public class SalaryTest {
    private Salary incomeSource1;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        context.setAssumptions(new Assumptions());
        Company employer = new Company(context, "employer1");
        Person employee = new Person(context, "employee1", LocalDate.of(1955, Month.JULY, 4), 77);
        Company bank = new Company(context, "bank1");
        CashFlowSink defaultSink = new AssetAccount(context, "checking1", Arrays.asList(employee.getId()),
                new CashBalance(LocalDate.of(2015, 10, 1), BigDecimal.ZERO), Collections.emptyList(),
                "Checking account 1", bank.getId(), Collections.emptyList());
        Job job1 = new Job(context, "job1", employer.getId(), employee.getId(), defaultSink.getId());
        job1.setStartDate(LocalDate.of(2015, Month.MAY, 1));
        job1.setEndDate(LocalDate.of(2016, Month.DECEMBER, 31));
        LocalDate job1FirstPaycheck = LocalDate.of(2015, Month.MAY, 17);
        Monthly job1CashFlow =
                new Monthly(context, "job1CashFlow1", job1.getStartDate(), job1.getEndDate(), job1FirstPaycheck,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        incomeSource1 = new Salary(context, "salary1", "job1", job1CashFlow.getId(),
                BigDecimal.valueOf(100000.00));
    }

    @After
    public void tearDown() {
    }


    @Test
    public void getId() {
        String name1 = incomeSource1.getId();
        assertEquals(name1, "salary1");
    }


    @Test
    public void toJSON() throws Exception {
        String incomeSource1Str = context.toJSON(incomeSource1);
        assertEquals("{\"type\":\"salary\",\"id\":\"salary1\",\"job\":\"job1\",\"baseAnnualSalary\":100000.0,\"cashFlow\":\"job1CashFlow1\"}", incomeSource1Str);
    }


    @Test
    public void fromJSON() throws Exception {
        String incomeSource1aStr = "{\"type\":\"salary\",\"id\":\"salary1a\",\"cashFlow\":\"job1CashFlow1\",\"job\":\"job1\",\"baseAnnualSalary\":100000.0}";
        CashFlowEstimator incomeSource1a = context.fromJSON(CashFlowEstimator.class, incomeSource1aStr);
        assertEquals("salary1a", incomeSource1a.getId());
    }

}