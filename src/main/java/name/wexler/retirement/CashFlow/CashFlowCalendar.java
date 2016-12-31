package name.wexler.retirement.CashFlow;

import name.wexler.retirement.ExpenseSource;
import name.wexler.retirement.IncomeSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwexler on 12/30/16.
 */
public class CashFlowCalendar {
    private boolean indexed = false;
    private List<IncomeSource> incomeSources;
    private List<ExpenseSource> expenseSources;
    private List<CashFlowInstance> incomeeCashFlowInstances = null;
    private List<CashFlowInstance> expenseCashFlowInstances = null;

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

    public int[] getYears() {
        if (!indexed)
            indexCashFlows();
        int[] years = new int[1];
        return years;
    }

    public List<String> getCashFlowIds() {
        if (!indexed)
            indexCashFlows();
        List<String> cashFlowIds = new ArrayList<>();
        return cashFlowIds;
    }

    public String getCashFlowName(String id) {
        String result = "";

        return result;
    }

    private void indexCashFlows() {
        incomeeCashFlowInstances = new ArrayList<>();
        expenseCashFlowInstances = new ArrayList<>();
        indexed = true;
    }
}
