package name.wexler.retirement.CashFlow;

import name.wexler.retirement.Context;
import name.wexler.retirement.ExpenseSource;
import name.wexler.retirement.IncomeSource;

import java.lang.reflect.Array;
import java.math.BigDecimal;
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
    private Map<Integer, Map<String, BigDecimal>> incomeCashFlowYears = null;
    private Map<Integer, Map<String, BigDecimal>> expenseCashFlowYears = null;

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

    public List<Integer> getYears() {
        if (!indexed)
            indexCashFlows();
        Set<Integer> incomeYearSet = incomeCashFlowYears.keySet();
        Set<Integer> expenseYearSet = expenseCashFlowYears.keySet();
        Set<Integer> yearSet = new HashSet<Integer>();
        yearSet.addAll(incomeYearSet);
        yearSet.addAll(expenseYearSet);
        List<Integer> yearList = new ArrayList<>(yearSet);
        yearList.sort(new Comparator<Integer>(){
            public int compare(Integer i1, Integer i2){
                return i1.compareTo(i2);
            }
        });
        return yearList;
    }

    public Map<String, String> getIncomeCashFlowNameAndIds() {
        if (!indexed)
            indexCashFlows();
        Map<String, String> cashFlowNameAndIds = new HashMap<String, String>();
        incomeSources.forEach(incomeSource->{
            cashFlowNameAndIds.put(incomeSource.getId(), incomeSource.getName());
        });
        return cashFlowNameAndIds;
    }

    public Map<String, String> getExpenseCashFlowNameAndIds() {
        if (!indexed)
            indexCashFlows();
        Map<String, String> cashFlowNameAndIds = new HashMap<String, String>();
        expenseSources.forEach(expenseSource->{
            cashFlowNameAndIds.put(expenseSource.getId(), expenseSource.getName());
        });
        return cashFlowNameAndIds;
    }

    public BigDecimal getAnnualExpense(String cashFlowId, Integer year) {
        Map<String, BigDecimal> yearMap = expenseCashFlowYears.get(year);
        BigDecimal expense = BigDecimal.ZERO;
        if (yearMap != null) {
            expense = yearMap.get(cashFlowId);
        }
        return expense;
    }

    public BigDecimal getAnnualIncome(String cashFlowId, Integer year) {
        Map<String, BigDecimal> yearMap = incomeCashFlowYears.get(year);
        BigDecimal income = BigDecimal.ZERO;
        if (yearMap != null) {
            income = yearMap.get(cashFlowId);
        }
        return income;
    }

    private void indexCashFlows() {

        expenseCashFlowInstances = new ArrayList<>();
        expenseCashFlowYears = new HashMap<>();
        indexIncomeCashFlows();
        indexExpenseCashFlows();
        indexed = true;
    }

    private void indexIncomeCashFlows() {
        incomeCashFlowInstances = new ArrayList<>();
        incomeCashFlowYears = new HashMap<>();
        incomeSources.forEach(incomeSource -> {
            List<CashFlowInstance> cashFlowInstances = incomeSource.getCashFlowInstances();
            incomeCashFlowInstances.addAll(cashFlowInstances);
            cashFlowInstances.forEach(incomeCashFlowInstance -> {
                int thisYear = incomeCashFlowInstance.getCashFlowDate().getYear();
                Map<String, BigDecimal> cashFlowAmounts = incomeCashFlowYears.get(thisYear);
                if (cashFlowAmounts == null) {
                    cashFlowAmounts = new HashMap<String, BigDecimal>();
                    incomeCashFlowYears.put(incomeCashFlowInstance.getCashFlowDate().getYear(), cashFlowAmounts);
                }
                BigDecimal total = BigDecimal.ZERO;
                Iterator<CashFlowInstance> cashFlowIterator = cashFlowInstances.iterator();
                while (cashFlowIterator.hasNext()) {
                    CashFlowInstance cashFlowInstance = cashFlowIterator.next();
                    total = total.add(cashFlowInstance.getAmount());
                }
                cashFlowAmounts.put(incomeSource.getId(), total);
            });
        });
    }

    private void indexExpenseCashFlows() {
        expenseCashFlowInstances = new ArrayList<>();
        expenseCashFlowYears = new HashMap<>();
        expenseSources.forEach(expenseSource -> {
            List<CashFlowInstance> cashFlowInstances = expenseSource.getCashFlowInstances();
            expenseCashFlowInstances.addAll(cashFlowInstances);
            cashFlowInstances.forEach(expenseCashFlowInstance -> {
                int thisYear = expenseCashFlowInstance.getCashFlowDate().getYear();
                Map<String, BigDecimal> cashFlowAmounts = expenseCashFlowYears.get(thisYear);
                if (cashFlowAmounts == null) {
                    cashFlowAmounts = new HashMap<String, BigDecimal>();
                    expenseCashFlowYears.put(thisYear, cashFlowAmounts);
                }
                BigDecimal total = cashFlowAmounts.get(expenseSource.getId());
                if (total == null)
                    total = BigDecimal.ZERO;
                total = total.add(expenseCashFlowInstance.getAmount());
                cashFlowAmounts.put(expenseSource.getId(), total);
            });
        });
    }

}
