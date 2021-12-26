package name.wexler.retirement.visualizer.Tables;

import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.CashFlowSource;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TableListTest {
    List<ColumnDefinition> columnDefinitions = Collections.emptyList();
    TableList tableList = new TableList(columnDefinitions);

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void getColumnDefinitions() {
        List<ColumnDefinition> colDefs = tableList.getColumnDefinitions();
        assertEquals(colDefs, columnDefinitions);
    }

    @Test
    public void getItemCategory() {
        String itemCategory = tableList.getItemCategory(Collections.emptyMap());
        assertEquals(null, itemCategory);
    }

    @Test
    public void getItemType() {
        String itemType = tableList.getItemType(Collections.emptyMap());
        assertEquals(null, itemType);
    }
}