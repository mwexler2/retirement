package name.wexler.retirement.CashFlow;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.*;

/**
 * Created by mwexler on 11/29/16.
 */
public class CashFlowInstanceTest {
    private CashFlowInstance cfi;
    private final BigDecimal thousand = BigDecimal.valueOf(1000.00);

    @Before
    public void setUp() throws Exception {
        BigDecimal thousand = BigDecimal.valueOf(1000.00);
        cfi = new CashFlowInstance(
                LocalDate.of(2014, Month.MAY, 1),
                LocalDate.of(2014, Month.MAY, 15),
                LocalDate.of(2014, Month.MAY, 25),
                thousand
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
    public void isPaidInDateRange() throws Exception {

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
    public void getAmount() throws Exception {
        assertEquals(thousand, cfi.getAmount());
    }

}