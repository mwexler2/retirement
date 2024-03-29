package name.wexler.retirement.visualizer.CashFlowFrequency;

import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.CashFlowEstimator.Salary;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Person;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;

import static name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance.NO_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

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
        AccountReader accountReader = mock(AccountReader.class);
        Context context = new Context(accountReader);
        context.setAssumptions(assumptions);
        Person employee1 = new Person(context, "employee1", LocalDate.of(1999, Month.DECEMBER, 31), 62,
                "Employee", "1");
        Company employer1 = new Company(context, "employer1", "Employer #1");
        Company bank = new Company(context, "bank1", "Bank #1");
        CashFlowSink defaultSink = new AssetAccount(context, "checking1", Arrays.asList(employee1.getId()),
                "Checking account 1", bank.getId(), Collections.emptyList(), null, AccountReader.mintTxnSource);
        Job job1 = new Job(context, "job1", employer1.getId(), employee1.getId(), defaultSink.getId());
        CashFlowFrequency salary1Freq = new Monthly(context, "salary1CashFlow",
                job1.getStartDate(),
                job1.getEndDate(),
                LocalDate.of(2010, Month.APRIL, 15),
                CashFlowFrequency.ApportionmentPeriod.EQUAL_MONTHLY);
        CashFlowEstimator salary1 = new Salary(context, "salary1", "job1", "salary1CashFlow", BigDecimal.valueOf(42000.42));
        cfi = new CashFlowInstance(NO_ID, true,
                salary1,
                defaultSink,
                CashFlowCalendar.ITEM_TYPE.TRANSFER.toString(),
                "Parent Category",
                "test",
                LocalDate.of(2014, Month.MAY, 1),
                LocalDate.of(2014, Month.MAY, 15),
                LocalDate.of(2014, Month.MAY, 25),
                thousand,
                BigDecimal.ZERO,
                "Estimated salary for " + salary1.getName());

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