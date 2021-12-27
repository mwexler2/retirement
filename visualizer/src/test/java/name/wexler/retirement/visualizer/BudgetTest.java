package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.Entity.Category;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class BudgetTest {
    private name.wexler.retirement.visualizer.Budget budget;
    private name.wexler.retirement.visualizer.Budget budget2;

    @Before
    public void setUp() throws Exception {
        AccountReader accountReader = mock(AccountReader.class);
        Context context1 = new Context(accountReader);   // Each budget needs its own context.
        Optional<LocalDate> date = Optional.ofNullable(null);
        budget = new name.wexler.retirement.visualizer.Budget(context1,
                date,
                1,
                0,
                name.wexler.retirement.visualizer.Budget.INCOME_GROUPING,
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

        Context context2 = new Context(accountReader);
        budget2 = new name.wexler.retirement.visualizer.Budget(context2,
                Optional.ofNullable(null),
                1,
                0,
                name.wexler.retirement.visualizer.Budget.EXPENSE_GROUPING,
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
        Assert.assertEquals(Budget.INCOME_GROUPING, budget.getGrouping());
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
        assertEquals(Category.INCOME_ITEM_TYPE, budget.getItemType());
        assertEquals(Category.EXPENSE_ITEM_TYPE, budget2.getItemType());
    }
}