package name.wexler.retirement.CashFlow;

import name.wexler.retirement.Context;
import name.wexler.retirement.ExpenseSource;
import name.wexler.retirement.IncomeSource;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by mwexler on 12/30/16.
 */
public class CashFlowCalendar {
    private boolean indexed = false;
    private List<IncomeSource> incomeSources;
    private List<ExpenseSource> expenseSources;
    private List<CashFlowInstance> incomeCashFlowInstances = null;
    private List<CashFlowInstance> expenseCashFlowInstances = null;
    private Map<Integer, Integer> cashFlowYears = null;

    public CashFlowCalendar() {
        incomeSources = new ArrayList<>();
        expenseSources = new ArrayList<>();
    }

    public void addIncomeSources(List<IncomeSource> incomeSources) {
        indexed = false;
        this.incomeSources.addAll(incomeSources);
    }

    public void addExpenseSources(List<ExpenseSource> expenseSources) {
        indexed = false;
        this.expenseSources.addAll(expenseSources);
    }

    public Integer[] getYears() {
        if (!indexed)
            indexCashFlows();
        Set<Integer> yearSet = cashFlowYears.keySet();
        Integer[] years = yearSet.toArray(new Integer[0]);
        Arrays.sort(years);
        return years;
    }

    public Map<String, String> getIncomeCashFlowNameAndIds() {
        if (!indexed)
            indexCashFlows();
        Map<String, String> cashFlowNameAndIds = new HashMap<String, String>();
        incomeSources.forEach(incomeSource->{
            cashFlowNameAndIds.put(incomeSource.getCashFlow().getId(), incomeSource.getName());
        });
        return cashFlowNameAndIds;
    }

    public Map<String, String> getExpenseCashFlowNameAndIds() {
        if (!indexed)
            indexCashFlows();
        Map<String, String> cashFlowNameAndIds = new HashMap<String, String>();
        expenseSources.forEach(expenseSource->{
            cashFlowNameAndIds.put(expenseSource.getCashFlow().getId(), expenseSource.getName());
        });
        return cashFlowNameAndIds;
    }


    private void indexCashFlows() {
        incomeCashFlowInstances = new ArrayList<>();
        expenseCashFlowInstances = new ArrayList<>();
        cashFlowYears = new HashMap<>();
        incomeSources.forEach(incomeSource->{
            incomeCashFlowInstances.addAll(incomeSource.getCashFlowInstances());
        });
        expenseSources.forEach(expenseSource->{
            expenseCashFlowInstances.addAll(expenseSource.getCashFlowInstances());
        });
        incomeCashFlowInstances.forEach(incomeCashFlowInstance->{
            cashFlowYears.put(incomeCashFlowInstance.getCashFlowDate().getYear(), 1);
        });
        expenseCashFlowInstances.forEach(expenseCashFlowInstance->{
            cashFlowYears.put(expenseCashFlowInstance.getCashFlowDate().getYear(), 1);
        });
        indexed = true;
    }
}
