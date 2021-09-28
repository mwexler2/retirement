package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.Entity.Category;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.assertEquals;


public class BudgetTest {
    private Budget budget;
    private Budget budget2;

    @Before
    public void setUp() throws Exception {
        Context context = new Context();
        budget = new Budget(context,
                Budget.INCOME_GROUPING,
                true,
                false,
                true,
                Optional.of(BigDecimal.ONE),
                BigDecimal.TEN,
                BigDecimal.ZERO,
                "parent",
                "category",
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        budget2 = new Budget(context,
                Budget.EXPENSE_GROUPING,
                true,
                false,
                true,
                Optional.empty(),
                BigDecimal.TEN,
                BigDecimal.ZERO,
                "parent",
                "category",
                Optional.of((long) 6),
                Optional.of(BigDecimal.ONE),
                Optional.of(BigDecimal.TEN),
                Optional.of(true)
        );
    }


    @Test
    public void getParentCategory() {
        assertEquals("parent", budget.getParentCategory());
    }

    @Test
    public void getCategory() {
        assertEquals("category", budget.getCategory());
    }

    @Test
    public void getAmount() {
        assertEquals(BigDecimal.ONE, budget.getAmount().get());
    }

    @Test
    public void getGrouping() {
        assertEquals(Budget.INCOME_GROUPING, budget.getGrouping());
    }

    @Test
    public void getBudget() {
        assertEquals(BigDecimal.TEN, budget.getBudget());
    }

    @Test
    public void getPeriod() {
        assertEquals(Optional.empty(), budget.getPeriod());
        assertEquals(Long.valueOf(6), budget2.getPeriod().get());
    }

    @Test
    public void getAamt() {
        assertEquals(Optional.empty(), budget.getAamt());
        assertEquals(Optional.of(BigDecimal.ONE), budget2.getAamt());
    }

    @Test
    public void getTbgt() {
        assertEquals(Optional.empty(), budget.getTbgt());
        assertEquals(Optional.of(BigDecimal.TEN), budget2.getTbgt());
    }

    @Test
    public void getLast() {
        assertEquals(Optional.empty(), budget.getLast());
        assertEquals(Optional.of(true), budget2.getLast());
    }

    @Test
    public void getItemType() {
        assertEquals(Category.INCOME, budget.getItemType());
        assertEquals(Category.EXPENSE, budget2.getItemType());
    }
}