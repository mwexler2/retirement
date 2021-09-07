package name.wexler.retirement.visualizer.CashFlowEstimator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.BigDecimalCloseTo.closeTo;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

@RunWith(Parameterized.class)
public class TaxTableTest {
    TaxTable taxTable;
    int year;
    BigDecimal income;
    BigDecimal expectedTax;

    @Before
    public void setUp() throws Exception {
        TaxTable.TaxYearTable.TaxBracket bracket1 = new TaxTable.TaxYearTable.TaxBracket(BigDecimal.ZERO, BigDecimal.valueOf(0.0));
        taxTable = new TaxTable(Map.of(
                Integer.valueOf(2021),
                        new TaxTable.TaxYearTable(Arrays.asList(
                                new TaxTable.TaxYearTable.TaxBracket(BigDecimal.ZERO, BigDecimal.valueOf(0.10)),
                                new TaxTable.TaxYearTable.TaxBracket(BigDecimal.valueOf(19900.00), BigDecimal.valueOf(0.12)),
                                new TaxTable.TaxYearTable.TaxBracket(BigDecimal.valueOf(81050.00), BigDecimal.valueOf(0.22)),
                                new TaxTable.TaxYearTable.TaxBracket(BigDecimal.valueOf(172750.00), BigDecimal.valueOf(0.24)),
                                new TaxTable.TaxYearTable.TaxBracket(BigDecimal.valueOf(329850.00), BigDecimal.valueOf(0.32)),
                                new TaxTable.TaxYearTable.TaxBracket(BigDecimal.valueOf(418850.00), BigDecimal.valueOf(0.35)),
                                new TaxTable.TaxYearTable.TaxBracket(BigDecimal.valueOf(628300.00), BigDecimal.valueOf(0.37))
                        ))

        ));
    }

    public TaxTableTest(int year, BigDecimal income, BigDecimal expectedTax) {
        this.year = year;
        this.income = income;
        this.expectedTax = expectedTax;
    }

    @Test
    public void computeTax() {
        assertThat(taxTable.computeTax(year, income), closeTo(expectedTax, BigDecimal.valueOf(0.001)));
    }

    @Parameterized.Parameters(name = "{index}: Test with year={0}, income={1}, expectedTax: {2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {2021, BigDecimal.ZERO, BigDecimal.ZERO},
                {2021, BigDecimal.ONE, BigDecimal.valueOf(0.10)},
                {2021, BigDecimal.valueOf(19900.00), BigDecimal.valueOf(1990.00)},
                {2021, BigDecimal.valueOf(172751.00), BigDecimal.valueOf(1990.12)},
                {2021, BigDecimal.valueOf(329851.00), BigDecimal.valueOf(1990.12)},
                {2021, BigDecimal.valueOf(418850.00), BigDecimal.valueOf(1990.12)},
                {2021, BigDecimal.valueOf(628301.00), BigDecimal.valueOf(1990.12)}
        });
    }
}