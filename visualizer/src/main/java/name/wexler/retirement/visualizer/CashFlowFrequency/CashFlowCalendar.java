package name.wexler.retirement.visualizer.CashFlowFrequency;

import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowEstimator.Liability;
import name.wexler.retirement.visualizer.Assumptions;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowInstance.LiabilityCashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Scenario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

/**
 * Created by mwexler on 12/30/16.
 */
public class CashFlowCalendar {
    public interface CashFlowChecker {
        boolean check(CashFlowInstance source);
    }
    private boolean _assetsIndexed = false;
    private boolean _liabilitiesIndexed = false;
    private boolean _cashFlowsIndexed = false;
    private final Map<String, CashFlowEstimator> _cashFlowEstimators;
    private final Map<String, Asset> _assets;
    private final Map<String, Liability> _liabilities;
    private final Map<String, AssetAccount> _accounts;
    private List<CashFlowInstance> cashFlowInstances = null;
    private Map<Integer, Map<String, BigDecimal>> cashFlowYears = null;
    private Map<Integer, Map<String, BigDecimal>> assetValueYears = null;
    private Map<Integer, Map<String, BigDecimal>> liabilityValueYears = null;
    private Map<String, TreeMap<LocalDate, Balance>> balanceAtDate = null;
    private final Assumptions _assumptions;
    private final Scenario _scenario;

    /**
     *
     * @param scenario
     * @param assumptions
     */
    public CashFlowCalendar(Scenario scenario, Assumptions assumptions) {
        _scenario = scenario;
        _assumptions = assumptions;
        _cashFlowEstimators = new HashMap<>();
        _assets = new HashMap<>();
        _liabilities = new HashMap<>();
        _accounts = new HashMap<>();
    }

    public void addCashFlowEstimators(List<CashFlowEstimator> cashFlowEstimators) {
        _cashFlowsIndexed = false;
        cashFlowEstimators.forEach(item->
                _cashFlowEstimators.put(item.getId(), item));
    }

    public void addAssets(List<Asset> assets) {
        _assetsIndexed = false;
        assets.forEach(item-> _assets.put(item.getId(), item));
    }

    public void addLiabilities(List<Liability> liabilities) {
        _liabilitiesIndexed = false;
        liabilities.forEach(item-> _liabilities.put(item.getId(), item));
    }

    public void addAccounts(List<AssetAccount> accounts) {
        accounts.forEach(account->_accounts.put(account.getId(), account));
    }

    public List<Integer> getYears() {
        if (!_cashFlowsIndexed)
            indexCashFlows();
        Set<Integer> yearSet = cashFlowYears.keySet();
        List<Integer> yearList = new ArrayList<>(yearSet);
        yearList.sort(Comparator.naturalOrder());
        return yearList;
    }

    public BigDecimal getAnnualCashFlow(String cashFlowId, Integer year) {
        if (!_cashFlowsIndexed)
            indexCashFlows();
        return getBalance(cashFlowYears, cashFlowId, year);
    }


    public BigDecimal getAssetValue(String assetId, Integer year) {
        if (!_assetsIndexed)
            indexAssets();
        Balance balance = _assets.get(assetId).getBalanceAtDate(_scenario,
                LocalDate.of(year, Month.JANUARY, 1));
        return balance.getValue();
    }

    public BigDecimal getLiabilityAmount(String id, Integer year) {
        if (!_liabilitiesIndexed)
            indexLiabilities();
        return getBalance(liabilityValueYears, id, year).negate();
    }

    private BigDecimal getEntityValue(Entity entity, Integer year) {
        if (entity instanceof Asset) {
            return getAssetValue(entity.getId(), year);
        } else if (entity instanceof Liability) {
            return getLiabilityAmount(entity.getId(), year);
        } else if (entity instanceof CashFlowEstimator) {
            return getAnnualCashFlow(entity.getId(), year);
        } else {
            System.err.println("Can't handle entity type: " + entity.getClass().getSimpleName());
            return BigDecimal.ZERO;
        }
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
            if (checker.check(cashFlowInstance)) {
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
        return getBalance(liabilityValueYears, year).negate();
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
        _cashFlowEstimators.values().forEach(cashFlowSource -> {
            String id = cashFlowSource.getId();
            TreeMap<LocalDate, Balance> balanceAtDateForId = balanceAtDate.getOrDefault(id, new TreeMap<>());
            if (!balanceAtDate.containsKey(id)) {
                balanceAtDate.put(id, balanceAtDateForId);
            }
            List<CashFlowInstance> cashFlowInstances = cashFlowSource.getCashFlowInstances(this);
            for (CashFlowInstance cashFlowInstance : cashFlowInstances) {
                LocalDate cashFlowDate = cashFlowInstance.getCashFlowDate();
                balanceAtDateForId.put(cashFlowInstance.getCashFlowDate(),
                        new CashBalance(cashFlowInstance.getCashFlowDate(), cashFlowInstance.getCashBalance()));
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
                Balance balance = asset.getBalanceAtDate(_scenario, LocalDate.of(year, Month.JANUARY, 1));
                indexBalances(balance, asset.getId(), assetValueYears);
            }
        });
    }

    private void indexLiabilities() {
        liabilityValueYears = new HashMap<>();

        _cashFlowEstimators.values().forEach(cashFlowSource -> {
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

        List<Balance> assetValues = _assets.get(assetId).getBalances(_scenario);
        return assetValues;
    }

    public List<Balance> getAssetValues(String assetId, int year) {
        if (!_assetsIndexed)
            indexAssets();

        List<Balance> assetValues = _assets.get(assetId).getBalances(_scenario, year);
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


    public class TableList extends ArrayList<Map<String, Object>> {
        private List<ColumnDefinition> columnDefinitions;
        TableList(List<ColumnDefinition> columnDefinitions) {
            super();
            this.columnDefinitions = columnDefinitions;
        }

        public List<ColumnDefinition> getColumnDefinitions() {
            return columnDefinitions;
        }

        public String getItemCategory(Map<String, Object> item) { return (String) item.get("itemCategory"); }
        public String getItemType(Map<String, Object> item) { return (String) item.get("itemType"); }
        public String getItemName(Map<String, Object> item) { return (String) item.get("name"); }
    }

    public class AmountAndLink {
        private BigDecimal amount;
        private String link;

        public AmountAndLink(BigDecimal amount, String link) {
            this.amount = amount;
            this.link = link;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public String getLink() {
            return link;
        }
    }

    public Map createRow(Entity entity, String itemType) {
            Map row = new HashMap();
            row.put("id", entity.getId());
            row.put("name", decorateName(itemType, entity.getId(), entity.getName()));
            row.put("itemType", itemType);
            row.put("itemCategory", entity.getCategory());
            for (int year: getYears()) {
                String link = String.join("/",
                        "scenario", this._scenario.getId(),
                        itemType, entity.getId(),
                        "year", Integer.toString(year));
                row.put(Integer.toString(year),
                        new AmountAndLink(getEntityValue(entity, year), link));
            }
            return row;
    }

    public String decorateName(String itemType, String id, String name) {
        return "<a href='scenario/" + this._scenario.getId() + "/" + itemType + "/" + id + "'>" +
                name + "</a>";
    }

    private Comparator<Map<String, Object>> byTypeClassAndName = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            int result = 0;
            result = ((String) o1.getOrDefault("itemType", "")).
                    compareTo((String) o2.getOrDefault("itemType", ""));
            if (result == 0)
                result = ((String) o1.getOrDefault("itemCategory", "")).
                        compareTo((String) o2.getOrDefault("itemCategory", ""));
            if (result == 0)
                result = ((String) o1.getOrDefault("name", "")).compareTo((String) o2.getOrDefault("name", ""));
            return result;
        }
    };

    private TableList getTableList() {
        List<ColumnDefinition> columnDefinitions = new ArrayList<>();
        columnDefinitions.add(ColumnDefinition.Builder.newInstance().
                setName("").
                setProperty("name").
                setTotal(false).
                build());
        for (int year : this.getYears()) {
            columnDefinitions.add(ColumnDefinition.Builder.newInstance().
                    setName(Integer.toString(year)).
                    setParamName("key").
                    setProperty(Integer.toString(year)).
                    setDecorator(MoneyTableColumnDecorator.class.getName()).
                    setTotal(true).
                    setHref("/visualizer/scenario/" + this._scenario.getId()).
                    build());
        }
        return new TableList(columnDefinitions);
    }

    public TableList getAssetsAndLiabilities() {
        TableList tableList = getTableList();


        String itemType = "asset";
        for (Asset asset: this._assets.values()) {
            Map assetRow = createRow(asset, itemType);
            tableList.add(assetRow);
        }

        itemType = "liability";
        for (Liability liability: _liabilities.values()) {
            Map<String, Object> liabilityRow = createRow(liability, itemType);
            tableList.add(liabilityRow);
        }

        Collections.sort(tableList, byTypeClassAndName);
        return tableList;
    }

    public TableList getCashFlows() {
        if (!_cashFlowsIndexed)
            indexCashFlows();

        TableList tableList = getTableList();

        String itemType = "cashflow";
        for (CashFlowEstimator cashFlowEstimator : this._cashFlowEstimators.values()) {
            Map cashFlowSourceRow = createRow(cashFlowEstimator, itemType);
            tableList.add(cashFlowSourceRow);
        }
        Collections.sort(tableList, byTypeClassAndName);
        return tableList;
    }
}
