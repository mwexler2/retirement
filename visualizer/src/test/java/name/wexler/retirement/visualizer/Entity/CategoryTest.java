package name.wexler.retirement.visualizer.Entity;

import name.wexler.retirement.visualizer.AccountReader;
import name.wexler.retirement.visualizer.Context;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class CategoryTest {
    Category category1;
    AccountReader accountReader = mock(AccountReader.class);
    Context context = new Context(accountReader);

    @Before
    public void setUp() throws Exception {
        category1 = new Category(context, "Foo Expense", Category.EXPENSE_ITEM_TYPE);
    }

    @Test
    public void getId() {
        String id = category1.getId();
        assertEquals("Foo Expense", id);
    }

    @Test
    public void getContext() {
        Context c = category1.getContext();
        assertEquals(context, c);
    }

    @Test
    public void getItemType() {
        String itemType = category1.getItemType();
        assertEquals("EXPENSE", itemType);
    }

    @Test
    public void getName() {
        String name = category1.getName();
        assertEquals("Foo Expense", name);
    }
}