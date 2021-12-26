package name.wexler.retirement.visualizer.Tables;

import name.wexler.retirement.visualizer.Assumptions;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.CashFlowSource;
import name.wexler.retirement.visualizer.Scenario;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class CashFlowTableListTest {
    private List<ColumnDefinition> columnDefinitions = Collections.emptyList();

    private Scenario scenario = mock(Scenario.class);
    private CashFlowCalendar cashFlowCalendar = new CashFlowCalendar(scenario, new Assumptions());
    private CashFlowTableList tableList = new CashFlowTableList(cashFlowCalendar, columnDefinitions);
    private static LocalDate startDate = LocalDate.EPOCH;
    private static LocalDate endDate = startDate.plusMonths(13);

    @Before
    public void setUp() throws Exception {

    }


    @Test
    public void getAssetsAndLiabilities() {
        TableList tableList = CashFlowTableList.getAssetsAndLiabilities(scenario, cashFlowCalendar, startDate.getYear(), endDate.getYear());
        assertEquals(0, tableList.size());
    }

    @Test
    public void getCashFlowTable() {
        CashFlowCalendar cashFlowCalendar = mock(CashFlowCalendar.class);
        TableList cashFlows = tableList.getCashFlowTable(scenario, cashFlowCalendar, cashFlowCalendar.getCashFlowInstances(), 2000, 2001);
        assertEquals(0, cashFlows.size());
    }

    @Test
    public void getCashFlowTableTwoEntries() {
        CashFlowSource source = mock(CashFlowSource.class);
        CashFlowSink sink = mock(CashFlowSink.class);
        CashFlowInstance cashFlowInstance = new CashFlowInstance(
                1L, false, source, sink,
                "itemType", "parentCategory", "category",
                startDate, endDate, startDate,
                BigDecimal.ONE, BigDecimal.TEN,
                "description");
        cashFlowCalendar.addCashFlowInstances(List.of(cashFlowInstance));
        cashFlowInstance = new CashFlowInstance(
                2L, false, source, sink,
                "itemType", "otherParent", "category",
                LocalDate.EPOCH, LocalDate.EPOCH.plusMonths(13), LocalDate.EPOCH,
                BigDecimal.ONE, BigDecimal.TEN,
                "description");
        cashFlowCalendar.addCashFlowInstances(List.of(cashFlowInstance));
        TableList cashFlows = tableList.getCashFlowTable(scenario, cashFlowCalendar, cashFlowCalendar.getCashFlowInstances(),
                startDate.getYear(), endDate.getYear());
        assertEquals(2, cashFlows.size());
        assertEquals(6, cashFlows.get(0).size());
        assertEquals("itemType", cashFlows.get(0).get("itemType"));
        assertEquals("otherParent", cashFlows.get(0).get("parentCategory"));
        assertEquals("category", cashFlows.get(0).get("itemCategory"));
        assertEquals("itemType", cashFlows.get(1).get("itemType"));
        assertEquals("parentCategory", cashFlows.get(1).get("parentCategory"));
        assertEquals("category", cashFlows.get(1).get("itemCategory"));
    }

    @Test
    public void getCashFlowTableOneEntry() {
        CashFlowSource source = mock(CashFlowSource.class);
        CashFlowSink sink = mock(CashFlowSink.class);
        CashFlowInstance cashFlowInstance = new CashFlowInstance(
                1L, false, source, sink,
                "itemType", "parentCategory", "category",
                startDate, endDate, startDate,
                BigDecimal.ONE, BigDecimal.TEN,
                "description");
        cashFlowCalendar.addCashFlowInstances(List.of(cashFlowInstance));
        TableList cashFlows = tableList.getCashFlowTable(scenario, cashFlowCalendar, cashFlowCalendar.getCashFlowInstances(),
                startDate.getYear(), endDate.getYear());
        assertEquals(1, cashFlows.size());
        assertEquals(6, cashFlows.get(0).size());
        assertEquals("itemType", cashFlows.get(0).get("itemType"));
        assertEquals("parentCategory", cashFlows.get(0).get("parentCategory"));
        assertEquals("category", cashFlows.get(0).get("itemCategory"));
    }

}