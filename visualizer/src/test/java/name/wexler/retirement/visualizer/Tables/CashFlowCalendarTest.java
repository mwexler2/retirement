package name.wexler.retirement.visualizer.Tables;

import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowInstance.LiabilityCashFlowInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CashFlowCalendarTest {
    private CashFlowCalendar cashFlowCalendar;

    @Before
    public void setUp() throws Exception {
        Scenario scenario = mock(Scenario.class);
        Context context = new Context();
        when(scenario.getContext()).thenReturn(context);
        Assumptions assumptions = mock(Assumptions.class);
        cashFlowCalendar = new CashFlowCalendar(scenario, assumptions);
    }

    @Test
    public void getBudgets() {
        List<Budget> setBudgets = Collections.emptyList();
        cashFlowCalendar.addBudgets(setBudgets);
        List<Budget> budgets = cashFlowCalendar.getBudgets();
        assertEquals(setBudgets, budgets);
    }

    @Test
    public void computeBalances() {
        cashFlowCalendar.computeBalances();
    }

    @Test
    public void addAssets() {
        cashFlowCalendar.addAssets(Collections.emptyList());
    }

    @Test
    public void addLiabilities() {
        cashFlowCalendar.addLiabilities(Collections.emptyList());
    }

    @Test
    public void getAssetValue() {
        BigDecimal assetValue = cashFlowCalendar.getAssetValue("asset1", 2020);
        assert(BigDecimal.ZERO.compareTo(assetValue) == 0);
    }

    @Test
    public void getLiabilityAmount() {
        BigDecimal liabilityAmount = cashFlowCalendar.getLiabilityAmount("liability1", 2020);
        assert(BigDecimal.ZERO.compareTo(liabilityAmount) == 0);
    }

    @Test
    public void sumMatchingCashFlowForPeriod() {
        BigDecimal sum = cashFlowCalendar.sumMatchingCashFlowForPeriod(
                LocalDate.EPOCH,
                LocalDate.MAX,
                instance -> (instance.isEstimate()));
        assert(BigDecimal.ZERO.compareTo(sum) == 0);
    }

    @Test
    public void getAssumptions() {
        Assumptions assumptions = cashFlowCalendar.getAssumptions();
        assertEquals(0, assumptions.getYearsInShortTerm());
    }

    @Test
    public void getCashFlowsBySink() {
        List<CashFlowInstance> cashFlows = cashFlowCalendar.getCashFlowsBySink("cashFlow1");
        assertEquals(0, cashFlows.size());
    }

    @Test
    public void testGetCashFlowsBySink() {
        List<CashFlowInstance> cashFlows = cashFlowCalendar.getCashFlowsBySink("cashFlow1", 2020);
        assertEquals(0, cashFlows.size());
    }

    @Test
    public void getLiabilityCashFlowInstances() {
        List<LiabilityCashFlowInstance> cashFlows = cashFlowCalendar.getLiabilityCashFlowInstances("liability1");
        assertEquals(0, cashFlows.size());
    }

    @Test
    public void testGetLiabilityCashFlowInstances() {
        List<LiabilityCashFlowInstance> cashFlows = cashFlowCalendar.getLiabilityCashFlowInstances("liability1", 2020);
        assertEquals(0, cashFlows.size());
    }

    @Test
    public void getCashFlowInstances() {
        List<CashFlowInstance> cashFlows = cashFlowCalendar.getCashFlowInstances();
        assertEquals(0, cashFlows.size());
    }
}