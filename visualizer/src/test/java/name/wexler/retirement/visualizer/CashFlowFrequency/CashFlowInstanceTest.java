package name.wexler.retirement.visualizer.CashFlowFrequency;

import name.wexler.retirement.visualizer.Assumptions;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowSource.CashFlowSource;
import name.wexler.retirement.visualizer.CashFlowSource.Salary;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Person;
import name.wexler.retirement.visualizer.Job;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;

/**
 * Created by mwexler on 11/29/16.
 */
public class CashFlowInstanceTest {
    private CashFlowInstance cfi;
    private final BigDecimal thousand = BigDecimal.valueOf(1000.00);

    @Before
    public void setUp() throws Exception {
        BigDecimal thousand = BigDecimal.valueOf(1000.00);
        Assumptions assumptions = new Assumptions();
        Context context = new Context();
        context.setAssumptions(assumptions);
        Person employee1 = new Person(context, "employee1", LocalDate.of(1999, Month.DECEMBER, 31), 62);
        Company company1 = new Company(context, "company1");
        String[] incomeSourceIds = new String[0];
        Job job1 = new Job(context, "job1", "employer1", "employee1", incomeSourceIds);
        CashFlowFrequency salary1Freq = new Monthly(context, "salary1CashFlow",
                job1.getStartDate(),
                job1.getEndDate(),
                LocalDate.of(2010, Month.APRIL, 15),
                CashFlowFrequency.ApportionmentPeriod.EQUAL_MONTHLY);
        CashFlowSource salary1 = new Salary(context, "salary1", "job1", "salary1CashFlow", BigDecimal.valueOf(42000.42));
        cfi = new CashFlowInstance(
                salary1,
                LocalDate.of(2014, Month.MAY, 1),
                LocalDate.of(2014, Month.MAY, 15),
                LocalDate.of(2014, Month.MAY, 25),
                thousand,
                BigDecimal.ZERO
        );

    }

    private class RangeResult {
        final LocalDate startDate;
        final LocalDate endDate;
        final boolean inRange;

        RangeResult(LocalDate startDate, LocalDate endDate, boolean inRange) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.inRange = inRange;
        }
    }

    @Test
    public void isPaidInDateRange() {

        RangeResult[] testData = new RangeResult[]{
                new RangeResult(
                        LocalDate.of(2013, Month.JANUARY, 1),
                        LocalDate.of(2013, Month.DECEMBER, 1),
                        false),
                new RangeResult(
                        LocalDate.of(2013, Month.JANUARY, 1),
                        LocalDate.of(2015, Month.DECEMBER, 31),
                        true),
                new RangeResult(
                        LocalDate.of(2014, Month.MAY, 24),
                        LocalDate.of(2014, Month.MAY, 24),
                        false),
                new RangeResult(
                        LocalDate.of(2015, Month.JANUARY, 1),
                        LocalDate.of(2015, Month.DECEMBER, 1),
                        false),
                new RangeResult(
                        LocalDate.of(2014, Month.MAY, 24),
                        LocalDate.of(2014, Month.MAY, 25),
                        true),
                new RangeResult(
                        LocalDate.of(2014, Month.MAY, 25),
                        LocalDate.of(2014, Month.MAY, 26),
                        true),

        };
        for (RangeResult r : testData) {
            assertEquals(r.inRange, cfi.isPaidInDateRange(r.startDate, r.endDate));
        }
    }

    @Test
    public void getAmount() {
        assertEquals(thousand, cfi.getAmount());
    }

}