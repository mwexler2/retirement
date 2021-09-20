package name.wexler.retirement.visualizer;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class BudgetTest {
    private Budget budget;

    @Before
    public void setUp() throws Exception {
        Context context = new Context();
        budget = new Budget(context,
                Budget.INCOME_GROUPING,
                true,
                false,
                true,
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                "parent",
                "category");
    }

    @Test
    public void empty() {

    }
}