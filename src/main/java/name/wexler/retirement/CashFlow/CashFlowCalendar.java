package name.wexler.retirement.CashFlow;

import name.wexler.retirement.Asset;
import name.wexler.retirement.ExpenseSource;
import name.wexler.retirement.IncomeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

/**
 * Created by mwexler on 12/30/16.
 */
public class CashFlowCalendar {
    private boolean _assetsIndexed = false;
    private boolean _cashFlowsIndexed = false;
    private final Map<String, IncomeSource> _incomeSources;
    private final Map<String, ExpenseSource> _expenseSources;
    private final Map<String, Asset> _assets;
    private List<CashFlowInstance> incomeCashFlowInstances = null;
    private List<CashFlowInstance> expenseCashFlowInstances = null;
    private Map<Integer, Map<String, BigDecimal>> incomeCashFlowYears = null;
    private Map<Integer, Map<String, BigDecimal>> expenseCashFlowYears = null;
    private Map<Integer, Map<String, BigDecimal>> assetValueYears = null;

    public CashFlowCalendar() {
        _incomeSources = new HashMap<>();
        _expenseSources = new HashMap<>();
        _assets = new HashMap<>();
    }

    public void addIncomeSources(List<IncomeSource> incomeSources) {
        _cashFlowsIndexed = false;
        incomeSources.forEach(item-> _incomeSources.put(item.getId(), item));
    }

    public void addAssets(List<Asset> assets) {
        _assetsIndexed = false;
        assets.forEach(item-> _assets.put(item.getId(), item));
    }

    public void addExpenseSources(List<ExpenseSource> expenseSources) {
        _cashFlowsIndexed = false;
        expenseSources.forEach(item-> _expenseSources.put(item.getId(), item));
    }

    public String getIncomeSourceName(String incomeSourceId) {
        return _incomeSources.get(incomeSourceId).getName();
    }

    public String getAssetName(String assetId) {
        return _assets.get(assetId).getName();
    }

    public String getExpenseSourceName(String expenseSourceId) {
        return _expenseSources.get(expenseSourceId).getName();
    }

    public List<Integer> getYears() {
        if (!_cashFlowsIndexed)
            indexCashFlows();
        Set<Integer> incomeYearSet = incomeCashFlowYears.keySet();
        Set<Integer> expenseYearSet = expenseCashFlowYears.keySet();
        Set<Integer> yearSet = new HashSet<>();
        yearSet.addAll(incomeYearSet);
        yearSet.addAll(expenseYearSet);
        List<Integer> yearList = new ArrayList<>(yearSet);
        yearList.sort(Comparator.naturalOrder());
        return yearList;
    }

    public Map<String, String> getIncomeCashFlowNameAndIds() {
        if (!_cashFlowsIndexed)
            indexCashFlows();
        Map<String, String> cashFlowNameAndIds = new HashMap<>();
        _incomeSources.values().forEach(incomeSource-> cashFlowNameAndIds.put(incomeSource.getId(), incomeSource.getName()));
        return cashFlowNameAndIds;
    }

    public Map<String, String> getAssetNameAndIds() {
        if (!_cashFlowsIndexed)
            indexAssets();
        Map<String, String> assetNameAndIds = new HashMap<>();
        _assets.values().forEach(asset-> assetNameAndIds.put(asset.getId(), asset.getName()));
        return assetNameAndIds;
    }

    public Map<String, String> getExpenseCashFlowNameAndIds() {
        if (!_cashFlowsIndexed)
            indexCashFlows();
        Map<String, String> cashFlowNameAndIds = new HashMap<>();
        _expenseSources.values().forEach(expenseSource-> cashFlowNameAndIds.put(expenseSource.getId(), expenseSource.getName()));
        return cashFlowNameAndIds;
    }

    public BigDecimal getAnnualExpense(String cashFlowId, Integer year) {
        if (!_cashFlowsIndexed)
            indexCashFlows();
        return getAnnualCashFlow(expenseCashFlowYears, cashFlowId, year);
    }

    public BigDecimal getAnnualIncome(String cashFlowId, Integer year) {
        if (!_cashFlowsIndexed)
            indexCashFlows();
        return getAnnualCashFlow(incomeCashFlowYears, cashFlowId, year);
    }

    public BigDecimal getAssetValue(String assetId, Integer year) {
        if (!_assetsIndexed)
            indexAssets();
        return getAssetValue(assetValueYears, assetId, year);
    }

    private BigDecimal getAnnualCashFlow(Map<Integer, Map<String, BigDecimal>> cashFlowYears, String cashFlowId, Integer year) {
        Map<String, BigDecimal> yearMap = cashFlowYears.get(year);
        BigDecimal income = BigDecimal.ZERO;
        if (yearMap != null  && yearMap.containsKey(cashFlowId)) {
            income = yearMap.get(cashFlowId);
        }
        return income;
    }

    private BigDecimal getAssetValue(Map<Integer, Map<String, BigDecimal>> assetYears, String assetId, Integer year) {
        Map<String, BigDecimal> yearMap = assetYears.get(year);
        BigDecimal assetValue = BigDecimal.ZERO;
        if (yearMap != null  && yearMap.containsKey(assetId)) {
            assetValue = yearMap.get(assetId);
        }
        return assetValue;
    }

    public BigDecimal getAnnualExpense(Integer year) {
        return getAnnualCashFlow(expenseCashFlowYears, year);
    }

    public BigDecimal getAnnualIncome(Integer year) {
        return getAnnualCashFlow(incomeCashFlowYears, year);
    }

    public BigDecimal getAssetValue(Integer year) {
        return getAssetValue(assetValueYears, year);
    }

    private BigDecimal getAnnualCashFlow(Map<Integer, Map<String, BigDecimal>> cashFlowYears, Integer year) {
        if (!_cashFlowsIndexed)
            indexCashFlows();
        Map<String, BigDecimal> yearMap = cashFlowYears.get(year);
        BigDecimal total = BigDecimal.ZERO;
        if (yearMap != null) {
            for (BigDecimal cashFlow : yearMap.values()) {
                total = total.add(cashFlow);
            }
        }
        return total;
    }

    private BigDecimal getAssetValue(Map<Integer, Map<String, BigDecimal>> assetYears, Integer year) {
        if (!_assetsIndexed)
            indexAssets();
        Map<String, BigDecimal> yearMap = assetValueYears.get(year);
        BigDecimal total = BigDecimal.ZERO;
        if (yearMap != null) {
            for (BigDecimal assetValue : yearMap.values()) {
                total = total.add(assetValue);
            }
        }
        return total;
    }

    private void indexCashFlows() {
        indexIncomeCashFlows();
        indexExpenseCashFlows();
        _cashFlowsIndexed = true;
    }

    private void indexIncomeCashFlows() {
        incomeCashFlowInstances = new ArrayList<>();
        incomeCashFlowYears = new HashMap<>();
        _incomeSources.values().forEach(incomeSource -> {
            List<CashFlowInstance> cashFlowInstances = incomeSource.getCashFlowInstances();
            indexCashFlowInstances(cashFlowInstances, incomeSource.getId(), incomeCashFlowInstances, incomeCashFlowYears);
        });
    }

    private void indexAssets() {
        assetValueYears = new HashMap<>();
        for (int year : getYears()) {
            _assets.values().forEach(asset -> {
                AssetValue assetValue = asset.getAssetValue(LocalDate.of(year, Month.JANUARY, 1));
                indexValues(assetValue, asset.getId(), assetValueYears);
            });
        }
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
                cashFlowAmounts = new HashMap<>();
                cashFlowYears.put(thisYear, cashFlowAmounts);
            }
            BigDecimal total = cashFlowAmounts.get(id);
            if (total == null)
                total = BigDecimal.ZERO;
            total = total.add(cashFlowInstance.getAmount());
            cashFlowAmounts.put(id, total);
        });
    }

    private void indexValues(AssetValue assetValue,
                                        String id,
                                        Map<Integer, Map<String, BigDecimal>> assetYears) {
        int thisYear = assetValue.getAssetValueDate().getYear();
        Map<String, BigDecimal> assetValues = assetYears.get(thisYear);
        if (assetValues == null) {
            assetValues = new HashMap<>();
            assetYears.put(thisYear, assetValues);
        }
        BigDecimal total = assetValues.get(id);
        if (total == null)
            total = BigDecimal.ZERO;
        total = total.add(assetValue.getValue());
        assetValues.put(id, total);
        _assetsIndexed = true;
    }
}
