package name.wexler.retirement.CashFlow;

import name.wexler.retirement.Asset;
import name.wexler.retirement.Liability;
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
    private boolean _liabilitiesIndexed = false;
    private boolean _cashFlowsIndexed = false;
    private final Map<String, IncomeSource> _incomeSources;
    private final Map<String, ExpenseSource> _expenseSources;
    private final Map<String, Asset> _assets;
    private final Map<String, Liability> _liabilities;
    private List<CashFlowInstance> incomeCashFlowInstances = null;
    private List<CashFlowInstance> expenseCashFlowInstances = null;
    private Map<Integer, Map<String, BigDecimal>> incomeCashFlowYears = null;
    private Map<Integer, Map<String, BigDecimal>> expenseCashFlowYears = null;
    private Map<Integer, Map<String, BigDecimal>> assetValueYears = null;
    private Map<Integer, Map<String, BigDecimal>> liabilityValueYears = null;

    public CashFlowCalendar() {
        _incomeSources = new HashMap<>();
        _expenseSources = new HashMap<>();
        _assets = new HashMap<>();
        _liabilities = new HashMap<>();
    }

    public void addIncomeSources(List<IncomeSource> incomeSources) {
        _cashFlowsIndexed = false;
        incomeSources.forEach(item-> _incomeSources.put(item.getId(), item));
    }

    public void addAssets(List<Asset> assets) {
        _assetsIndexed = false;
        assets.forEach(item-> _assets.put(item.getId(), item));
    }

    public void addLiabilities(List<Liability> liabilities) {
        _liabilitiesIndexed = false;
        liabilities.forEach(item-> _liabilities.put(item.getId(), item));
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

    public String getLiabilityName(String id) {
        return _liabilities.get(id).getName();
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
        if (!_assetsIndexed)
            indexAssets();
        Map<String, String> assetNameAndIds = new HashMap<>();
        _assets.values().forEach(asset-> assetNameAndIds.put(asset.getId(), asset.getName()));
        return assetNameAndIds;
    }

    public Map<String, String> getLiabilityNameAndIds() {
        if (!_liabilitiesIndexed)
            indexLiabilities();
        Map<String, String> liabilityNameAndIds = new HashMap<>();
        _liabilities.values().forEach(liability-> liabilityNameAndIds.put(liability.getId(), liability.getName()));
        return liabilityNameAndIds;
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

    public BigDecimal getAssetValue(String id, Integer year) {
        if (!_assetsIndexed)
            indexAssets();
        return getBalance(assetValueYears, id, year);
    }

    public BigDecimal getLiabilityAmount(String id, Integer year) {
        if (!_liabilitiesIndexed)
            indexLiabilities();
        return getBalance(liabilityValueYears, id, year);
    }

    private BigDecimal getAnnualCashFlow(Map<Integer, Map<String, BigDecimal>> cashFlowYears, String cashFlowId, Integer year) {
        Map<String, BigDecimal> yearMap = cashFlowYears.get(year);
        BigDecimal income = BigDecimal.ZERO;
        if (yearMap != null  && yearMap.containsKey(cashFlowId)) {
            income = yearMap.get(cashFlowId);
        }
        return income;
    }

    private BigDecimal getBalance(Map<Integer, Map<String, BigDecimal>> years, String id, Integer year) {
        Map<String, BigDecimal> yearMap = years.get(year);
        BigDecimal balance = BigDecimal.ZERO;
        if (yearMap != null  && yearMap.containsKey(id)) {
            balance = yearMap.get(id);
        }
        return balance;
    }

    public BigDecimal getAnnualExpense(Integer year) {
        return getAnnualCashFlow(expenseCashFlowYears, year);
    }

    public BigDecimal getAnnualIncome(Integer year) {
        return getAnnualCashFlow(incomeCashFlowYears, year);
    }

    public BigDecimal getAssetValue(Integer year) {
        if (!_assetsIndexed)
            indexAssets();
        return getBalance(assetValueYears, year);
    }

    public BigDecimal getLiabilityAmount(Integer year) {
        if (!_liabilitiesIndexed)
            indexLiabilities();
        return getBalance(liabilityValueYears, year);
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

    private BigDecimal getBalance(Map<Integer, Map<String, BigDecimal>> years, Integer year) {
        Map<String, BigDecimal> yearMap = years.get(year);
        BigDecimal total = BigDecimal.ZERO;
        if (yearMap != null) {
            for (BigDecimal balance : yearMap.values()) {
                total = total.add(balance);
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
                Balance balance = asset.getBalanceAtDate(LocalDate.of(year, Month.JANUARY, 1));
                indexBalances(balance, asset.getId(), assetValueYears);
            });
        }
    }

    private void indexLiabilities() {
        liabilityValueYears = new HashMap<>();
        for (int year : getYears()) {
            _expenseSources.values().forEach(expenseSource -> {
                if (expenseSource instanceof Liability) {
                    Liability liability = (Liability) expenseSource;
                    Balance balance = liability.getBalance(LocalDate.of(year, Month.JANUARY, 1));
                    indexBalances(balance, liability.getId(), liabilityValueYears);
                }
            });
        }
        _liabilitiesIndexed = true;
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


    private void indexBalances(Balance balance,
                             String id,
                             Map<Integer, Map<String, BigDecimal>> years) {
        int thisYear = balance.getBalanceDate().getYear();
        Map<String, BigDecimal> balances = years.get(thisYear);
        if (balances == null) {
            balances = new HashMap<>();
            years.put(thisYear, balances);
        }
        BigDecimal total = balances.get(id);
        if (total == null)
            total = BigDecimal.ZERO;
        total = total.add(balance.getValue());
        balances.put(id, total);
    }
}
