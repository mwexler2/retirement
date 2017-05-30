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
    private Map<String, IncomeSource> _incomeSources;
    private Map<String, ExpenseSource> _expenseSources;
    private List<CashFlowInstance> incomeCashFlowInstances = null;
    private List<CashFlowInstance> expenseCashFlowInstances = null;
    private Map<Integer, Map<String, BigDecimal>> incomeCashFlowYears = null;
    private Map<Integer, Map<String, BigDecimal>> expenseCashFlowYears = null;

    public CashFlowCalendar() {
        _incomeSources = new HashMap<>();
        _expenseSources = new HashMap<>();
    }

    public void addIncomeSources(List<IncomeSource> incomeSources) {
        indexed = false;
        incomeSources.forEach(item->{
            _incomeSources.put(item.getId(), item);
        });
    }

    public void addExpenseSources(List<ExpenseSource> expenseSources) {
        indexed = false;
        expenseSources.forEach(item->{
            _expenseSources.put(item.getId(), item);
        });
    }

    public String getIncomeSourceName(String incomeSourceId) {
        return _incomeSources.get(incomeSourceId).getName();
    }

    public String getExpenseSourceName(String expenseSourceId) {
        return _expenseSources.get(expenseSourceId).getName();
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
        _incomeSources.values().forEach(incomeSource->{
            cashFlowNameAndIds.put(incomeSource.getId(), incomeSource.getName());
        });
        return cashFlowNameAndIds;
    }

    public Map<String, String> getExpenseCashFlowNameAndIds() {
        if (!indexed)
            indexCashFlows();
        Map<String, String> cashFlowNameAndIds = new HashMap<String, String>();
        _expenseSources.values().forEach(expenseSource->{
            cashFlowNameAndIds.put(expenseSource.getId(), expenseSource.getName());
        });
        return cashFlowNameAndIds;
    }

    public BigDecimal getAnnualExpense(String cashFlowId, Integer year) {
        if (!indexed)
            indexCashFlows();
        return getAnnualCashFlow(expenseCashFlowYears, cashFlowId, year);
    }

    public BigDecimal getAnnualIncome(String cashFlowId, Integer year) {
        if (!indexed)
            indexCashFlows();
        return getAnnualCashFlow(incomeCashFlowYears, cashFlowId, year);
    }

    public BigDecimal getAnnualCashFlow(Map<Integer, Map<String, BigDecimal>> cashFlowYears, String cashFlowId, Integer year) {
        Map<String, BigDecimal> yearMap = cashFlowYears.get(year);
        BigDecimal income = BigDecimal.ZERO;
        if (yearMap != null  && yearMap.containsKey(cashFlowId)) {
            income = yearMap.get(cashFlowId);
        }
        return income;
    }

    public BigDecimal getAnnualExpense(Integer year) {
        return getAnnualCashFlow(expenseCashFlowYears, year);
    }

    public BigDecimal getAnnualIncome(Integer year) {
        return getAnnualCashFlow(incomeCashFlowYears, year);
    }

    public BigDecimal getAnnualCashFlow(Map<Integer, Map<String, BigDecimal>> cashFlowYears, Integer year) {
        if (!indexed)
            indexCashFlows();
        Map<String, BigDecimal> yearMap = cashFlowYears.get(year);
        BigDecimal total = BigDecimal.ZERO;
        if (yearMap != null) {
            Iterator<BigDecimal> cashFlows = yearMap.values().iterator();
            while (cashFlows.hasNext()) {
                BigDecimal cashFlow = cashFlows.next();
                total = total.add(cashFlow);
            }
        }
        return total;
    }

    private void indexCashFlows() {
        indexIncomeCashFlows();
        indexExpenseCashFlows();
        indexed = true;
    }

    private void indexIncomeCashFlows() {
        incomeCashFlowInstances = new ArrayList<>();
        incomeCashFlowYears = new HashMap<>();
        _incomeSources.values().forEach(incomeSource -> {
            List<CashFlowInstance> cashFlowInstances = incomeSource.getCashFlowInstances();
            indexCashFlowInstances(cashFlowInstances, incomeSource.getId(), incomeCashFlowInstances, incomeCashFlowYears);
        });
    }

    private void indexExpenseCashFlows() {
        expenseCashFlowInstances = new ArrayList<>();
        expenseCashFlowYears = new HashMap<>();
        _expenseSources.values().forEach(expenseSource -> {
            List<CashFlowInstance> cashFlowInstances = expenseSource.getCashFlowInstances();
            indexCashFlowInstances(cashFlowInstances, expenseSource.getId(), expenseCashFlowInstances, expenseCashFlowYears);
        });
    }

    private void indexCashFlowInstances(List<CashFlowInstance> cashFlowInstances,
                                        String id,
                                        List<CashFlowInstance> masterCashFlowInstances,
                                        Map<Integer, Map<String, BigDecimal>>  cashFlowYears) {
        masterCashFlowInstances.addAll(cashFlowInstances);
        cashFlowInstances.forEach(cashFlowInstance -> {
            int thisYear = cashFlowInstance.getCashFlowDate().getYear();
            Map<String, BigDecimal> cashFlowAmounts = cashFlowYears.get(thisYear);
            if (cashFlowAmounts == null) {
                cashFlowAmounts = new HashMap<String, BigDecimal>();
                cashFlowYears.put(thisYear, cashFlowAmounts);
            }
            BigDecimal total = cashFlowAmounts.get(id);
            if (total == null)
                total = BigDecimal.ZERO;
            total = total.add(cashFlowInstance.getAmount());
            cashFlowAmounts.put(id, total);
        });
    }

}
