package name.wexler.retirement.CashFlow;

import name.wexler.retirement.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

/**
 * Created by mwexler on 12/30/16.
 */
public class CashFlowCalendar {
    public interface CashFlowChecker {
        boolean check(CashFlowSource source);
    }
    private boolean _assetsIndexed = false;
    private boolean _liabilitiesIndexed = false;
    private boolean _cashFlowsIndexed = false;
    private final Map<String, CashFlowSource> _cashFlowSources;
    private final Map<String, Asset> _assets;
    private final Map<String, Liability> _liabilities;
    private final Map<String, Account> _accounts;
    private List<CashFlowInstance> cashFlowInstances = null;
    private Map<Integer, Map<String, BigDecimal>> cashFlowYears = null;
    private Map<Integer, Map<String, BigDecimal>> assetValueYears = null;
    private Map<Integer, Map<String, BigDecimal>> liabilityValueYears = null;
    private Map<String, TreeMap<LocalDate, Balance>> balanceAtDate = null;
    private Assumptions _assumptions;

    public CashFlowCalendar(Assumptions assumptions) {
        _assumptions = assumptions;
        _cashFlowSources = new HashMap<>();
        _assets = new HashMap<>();
        _liabilities = new HashMap<>();
        _accounts = new HashMap<>();
    }

    public void addCashFlowSources(List<CashFlowSource> cashFlowSources) {
        _cashFlowsIndexed = false;
        cashFlowSources.forEach(item-> _cashFlowSources.put(item.getId(), item));
    }

    public void addAssets(List<Asset> assets) {
        _assetsIndexed = false;
        assets.forEach(item-> _assets.put(item.getId(), item));
    }

    public void addLiabilities(List<Liability> liabilities) {
        _liabilitiesIndexed = false;
        liabilities.forEach(item-> _liabilities.put(item.getId(), item));
    }

    public void addAccounts(List<Account> accounts) {
        accounts.forEach(account->_accounts.put(account.getId(), account));
    }


    public String getCashFlowSourceName(String cashFlowSourceId) {
        return _cashFlowSources.get(cashFlowSourceId).getName();
    }

    public String getAssetName(String assetId) {
        return _assets.get(assetId).getName();
    }

    public String getLiabilityName(String id) {
        return _liabilities.get(id).getName();
    }

    public List<Integer> getYears() {
        if (!_cashFlowsIndexed)
            indexCashFlows();
        Set<Integer> yearSet = cashFlowYears.keySet();
        List<Integer> yearList = new ArrayList<>(yearSet);
        yearList.sort(Comparator.naturalOrder());
        return yearList;
    }

    public Map<String, String> getCashFlowNameAndIds() {
        if (!_cashFlowsIndexed)
            indexCashFlows();
        Map<String, String> cashFlowNameAndIds = new HashMap<>();
        _cashFlowSources.values().forEach(cashFlowSource-> cashFlowNameAndIds.put(cashFlowSource.getId(), cashFlowSource.getName()));
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

    public BigDecimal getAnnualCashFlow(String cashFlowId, Integer year) {
        if (!_cashFlowsIndexed)
            indexCashFlows();
        return getAnnualCashFlow(cashFlowYears, cashFlowId, year);
    }


    public BigDecimal getAssetValue(String assetId, Integer year) {
        if (!_assetsIndexed)
            indexAssets();
        List<Balance> assetValues = _assets.get(assetId).getBalances();
        Balance balance = _assets.get(assetId).getBalanceAtDate(LocalDate.of(year, Month.JANUARY, 1));
        return balance.getValue();
    }

    public BigDecimal getLiabilityAmount(String id, Integer year) {
        if (!_liabilitiesIndexed)
            indexLiabilities();
        return getBalance(liabilityValueYears, id, year);
    }

    private BigDecimal getAnnualCashFlow(Map<Integer, Map<String, BigDecimal>> cashFlowYears, String cashFlowId, Integer year) {
        Map<String, BigDecimal> yearMap = cashFlowYears.get(year);
        BigDecimal cashFlow = BigDecimal.ZERO;
        if (yearMap != null  && yearMap.containsKey(cashFlowId)) {
            cashFlow = yearMap.get(cashFlowId);
        }
        return cashFlow;
    }

    private BigDecimal getBalance(Map<Integer, Map<String, BigDecimal>> years, String id, Integer year) {
        Map<String, BigDecimal> yearMap = years.get(year);
        BigDecimal balance = BigDecimal.ZERO;
        if (yearMap != null  && yearMap.containsKey(id)) {
            balance = yearMap.get(id);
        }
        return balance;
    }

    public BigDecimal sumMatchingCashFlowForPeriod(LocalDate accrualStart, LocalDate accrualEnd, CashFlowChecker checker) {
        BigDecimal sum = BigDecimal.ZERO;
        for (CashFlowInstance cashFlowInstance : this.cashFlowInstances) {
            cashFlowInstance.getCashFlowSource();
            if (checker.check(cashFlowInstance.getCashFlowSource())) {
                if (cashFlowInstance.isPaidInDateRange(accrualStart, accrualEnd)) {
                    sum = sum.add(cashFlowInstance.getAmount());
                }
            }
        }
        return sum;
    }

    public BigDecimal getAnnualCashFlow(Integer year) {
        return getAnnualCashFlow(cashFlowYears, year);
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

    public Assumptions getAssumptions() {
        return _assumptions;
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
        cashFlowInstances = new ArrayList<>();
        cashFlowYears = new HashMap<>();
        balanceAtDate = new HashMap<>();
        _cashFlowSources.values().forEach(cashFlowSource -> {
            String id = cashFlowSource.getId();
            TreeMap<LocalDate, Balance> balanceAtDateForId = balanceAtDate.getOrDefault(id, new TreeMap<>());
            if (!balanceAtDate.containsKey(id)) {
                balanceAtDate.put(id, balanceAtDateForId);
            }
            List<CashFlowInstance> cashFlowInstances = cashFlowSource.getCashFlowInstances(this);
            for (CashFlowInstance cashFlowInstance : cashFlowInstances) {
                LocalDate cashFlowDate = cashFlowInstance.getCashFlowDate();
                balanceAtDateForId.put(cashFlowInstance.getCashFlowDate(),
                        new CashBalance(cashFlowInstance.getCashFlowDate(), cashFlowInstance.getBalance()));
            }
            indexCashFlowInstances(cashFlowInstances, cashFlowSource.getId(), this.cashFlowInstances, cashFlowYears);
        });
        _cashFlowsIndexed = true;
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


    private void indexAssets() {
        assetValueYears = new HashMap<>();
        _assets.values().forEach(asset -> {
            for (int year : getYears()) {
                Balance balance = asset.getBalanceAtDate(LocalDate.of(year, Month.JANUARY, 1));
                indexBalances(balance, asset.getId(), assetValueYears);
            }
        });
    }

    private void indexLiabilities() {
        liabilityValueYears = new HashMap<>();

        _cashFlowSources.values().forEach(cashFlowSource -> {
            if (cashFlowSource instanceof Liability) {
                Liability liability = (Liability) cashFlowSource;
                Balance prevBalance = liability.getStartingBalance();
                for (int year : getYears()) {
                    LocalDate beginningOfYear = LocalDate.of(year, Month.JANUARY, 1);

                    TreeMap<LocalDate, Balance> balanceAtDateForId = balanceAtDate.get(liability.getId());
                    Balance balance = new CashBalance(beginningOfYear, BigDecimal.ZERO);
                    Map.Entry<LocalDate, Balance>  entry = balanceAtDateForId.lowerEntry(beginningOfYear);
                    if (entry != null)
                        balance = balanceAtDateForId.lowerEntry(beginningOfYear).getValue();
                    indexBalances(balance, liability.getId(), liabilityValueYears);
                    prevBalance = balance;
                }
            }
        });
        _liabilitiesIndexed = true;
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
        balances.put(id, balance.getValue());
    }

    public List<CashFlowInstance> getCashFlows(String cashFlowId) {
        if (!_cashFlowsIndexed)
            indexCashFlows();

        List<CashFlowInstance> cashFlows = new ArrayList<>();

        for (CashFlowInstance cashFlow: this.cashFlowInstances) {
            if (cashFlow.getCashFlowId().equals(cashFlowId)) {
                cashFlows.add(cashFlow);
            }
        }
        return cashFlows;
    }

    public List<CashFlowInstance> getCashFlows(String cashFlowId, Integer year) {
        if (!_cashFlowsIndexed)
            indexCashFlows();

        List<CashFlowInstance> cashFlows = new ArrayList<>();

        for (CashFlowInstance cashFlow: this.cashFlowInstances) {
            if (cashFlow.getCashFlowId().equals(cashFlowId)  && cashFlow.getCashFlowDate().getYear() == year.intValue()) {
                cashFlows.add(cashFlow);
            }
        }
        return cashFlows;
    }

    public List<Balance> getAssetValues(String assetId) {
        if (!_assetsIndexed)
            indexAssets();

        List<Balance> assetValues = _assets.get(assetId).getBalances();
        return assetValues;
    }

    public List<Balance> getAssetValues(String assetId, int year) {
        if (!_assetsIndexed)
            indexAssets();

        List<Balance> assetValues = _assets.get(assetId).getBalances(year);
        return assetValues;
    }

    public List<Balance> getLiabilityBalances(String liabilityId) {
        if (!_cashFlowsIndexed)
            indexCashFlows();
        return new ArrayList<Balance>(balanceAtDate.get(liabilityId).values());
    }

    public List<LiabilityCashFlowInstance> getLiabilityCashFlowInstances(String liabilityId) {
        return (List<LiabilityCashFlowInstance>) (List<?>) getCashFlows(liabilityId);
    }
}
