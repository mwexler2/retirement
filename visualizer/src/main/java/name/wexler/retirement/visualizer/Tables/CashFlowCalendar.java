package name.wexler.retirement.visualizer.Tables;

import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowEstimator.Liability;
import name.wexler.retirement.visualizer.Assumptions;
import name.wexler.retirement.visualizer.CashFlowFrequency.Balance;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.CashFlowFrequency.ShareBalance;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowInstance.LiabilityCashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Scenario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mwexler on 12/30/16.
 */
public class CashFlowCalendar {
    public static enum ITEM_TYPE {INCOME, EXPENSE, TRANSFER};

    public interface CashFlowChecker {
        boolean check(CashFlowInstance source);
    }
    private final Map<String, Asset> _assets;
    private final Map<String, Liability> _liabilities;
    private List<CashFlowInstance> cashFlowInstances = new ArrayList<>();
    private Map<String, BigDecimal> currentBalances = new HashMap<>();
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
        _assets = new HashMap<>();
        _liabilities = new HashMap<>();
    }

    public void addCashFlowInstances(List<CashFlowInstance> cashFlowInstances) {
        this.cashFlowInstances.addAll(cashFlowInstances);
    }

    public void computeBalances() {
        cashFlowInstances.sort(Comparator.comparing(CashFlowInstance::getCashFlowDate));

        ListIterator<CashFlowInstance> listIterator = cashFlowInstances.listIterator(cashFlowInstances.size());

        while (listIterator.hasPrevious()) {
            CashFlowInstance instance = listIterator.previous();
            if (instance.isEstimate())
                continue;   // We are counting back from actual balance, skip estimates
            instance.getCashFlowSink().updateRunningTotal(instance);
        }
    }

    public void addAssets(List<Asset> assets) {
        assets.forEach(item-> _assets.put(item.getId(), item));
    }

    public void addLiabilities(List<Liability> liabilities) {
        liabilities.forEach(item-> _liabilities.put(item.getId(), item));
    }

    public List<Integer> getYears() {
        return cashFlowInstances.stream().
                map(CashFlowInstance::getYear).
                distinct().
                sorted().
                collect(Collectors.toList());
    }

    public BigDecimal getAnnualCashFlow(String cashFlowId, Integer year) {
        return cashFlowInstances.stream().
                filter(instance->instance.getYear() == year).
                filter(instance->instance.getCashFlowId() == cashFlowId).
                map(instance->instance.getAmount()).
                collect(Collectors.reducing(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal getAssetValue(String assetId, Integer year) {
        Asset asset = _scenario.getContext().getById(Asset.class, assetId);
        Optional<CashFlowInstance> finalInstanceForYear =
                cashFlowInstances.stream().
                        filter(instance -> instance.getCashFlowSink() == asset).
                        filter(instance -> instance.getCashFlowDate().isBefore(LocalDate.of(year + 1, Month.JANUARY, 1))).
                        reduce((first, second) -> second);
        BigDecimal finalBalance = BigDecimal.ZERO;
        if (finalInstanceForYear.isPresent())
            finalBalance = finalInstanceForYear.get().getCashBalance();
        return finalBalance;
    }

    public BigDecimal getLiabilityAmount(String id, Integer year) {
        return cashFlowInstances.stream().
                        filter(instance->instance.getCashFlowSource() instanceof Liability).
                        filter(instance->instance.getCashFlowSourceId() == id).
                        filter(instance->instance.getYear() == year).
                        sorted((a,b) -> a.getCashFlowDate().compareTo(b.getCashFlowDate())).
                        collect(Collectors.mapping(instance -> instance.getAmount(),
                                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)));
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


    public Assumptions getAssumptions() {
        return _assumptions;
    }

    public List<CashFlowInstance> getCashFlowsBySink(String cashFlowId) {
        List<CashFlowInstance> cashFlows =
                cashFlowInstances.stream().
                        filter(instance -> instance.getCashFlowSinkId().equals(cashFlowId)).
                        collect(Collectors.toList());
        return cashFlows;
    }

    public List<CashFlowInstance> getCashFlowsBySink(String cashFlowId, Integer year) {
        List<CashFlowInstance> cashFlows =
                cashFlowInstances.stream().
                        filter(instance -> instance.getCashFlowSinkId().equals(cashFlowId)).
                        filter(instance -> instance.getYear() == year).
                        collect(Collectors.toList());
        return cashFlows;
    }

    public List<LiabilityCashFlowInstance> getLiabilityCashFlowInstances(String liabilityId) {
        return (List<LiabilityCashFlowInstance>) (List<?>) getCashFlowsBySink(liabilityId);
    }

    public List<LiabilityCashFlowInstance> getLiabilityCashFlowInstances(String liabilityId, int year) {
        return (List<LiabilityCashFlowInstance>) (List<?>) getCashFlowsBySink(liabilityId, year);
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

    public TableList createTableListFromNestedHash(Map<String, Map<String, Map<Integer, BigDecimal>>> nestedHash) {
        TableList tableList = getTableList();

        for (Map.Entry<String, Map<String, Map<Integer, BigDecimal>>> outerEntry: nestedHash.entrySet()) {
            String itemType = outerEntry.getKey();
            for (Map.Entry<String, Map<Integer, BigDecimal>> innerEntry: outerEntry.getValue().entrySet()) {
                Map<String, Object> row = new HashMap<>();
                String itemCategory = innerEntry.getKey();
                row.put("itemCategory", itemCategory);
                row.put("itemType", itemType);
                row.put("name", decorateName(itemType, innerEntry.getKey(), innerEntry.getKey()));
                for (int year : getYears()) {
                    String link = String.join("/",
                            "scenario", this._scenario.getId(),
                            itemType, itemCategory,
                            "year", Integer.toString(year));
                    row.put(Integer.toString(year),
                            new AmountAndLink(innerEntry.getValue().getOrDefault(year, BigDecimal.ZERO), link));
                }
                tableList.add(row);
            }
        }
        Collections.sort(tableList, byTypeClassAndName);
        return tableList;
    }

    public TableList getCashFlows() {
        Map<String, Map<String, Map<Integer, BigDecimal>>> categoryMap =
                cashFlowInstances.stream().
                        collect(Collectors.groupingBy(CashFlowInstance::getItemType,
                                Collectors.groupingBy(CashFlowInstance::getCategory,
                                Collectors.groupingBy(CashFlowInstance::getYear,
                                Collectors.mapping(instance -> instance.getAmount(),
                                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))));
        return createTableListFromNestedHash(categoryMap);
    }

    public List<CashFlowInstance> getCashFlowInstances() {
        return cashFlowInstances;
    }

    private Comparator<Map<String, Object>> byTickerCompanyAndAccount = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            int result = 0;
            result = ((String) o1.getOrDefault("ticker", "")).
                    compareTo((String) o2.getOrDefault("ticker", ""));
            if (result == 0)
                result = ((String) o1.getOrDefault("accountCompany", "")).
                        compareTo((String) o2.getOrDefault("accountCompany", ""));
            if (result == 0)
                result = ((String) o1.getOrDefault("accountName", "")).compareTo((String) o2.getOrDefault("accountName", ""));
            return result;
        }
    };

    public List<Map<String, Object>> getCurrentShareBalances() {
        List<Map<String, Object>> shareBalances = new ArrayList<>();
        for (Asset asset : _assets.values()) {
            if (asset instanceof AssetAccount) {
                AssetAccount account = (AssetAccount) asset;
                for (ShareBalance shareBalance : account.getCurrentShareBalances()) {
                    Map<String, Object> shareBalanceMap = new HashMap<>();
                    shareBalanceMap.put("balanceDate", shareBalance.getBalanceDate());
                    shareBalanceMap.put("shares", shareBalance.getShares());
                    shareBalanceMap.put("ticker", shareBalance.getSecurity().getName());
                    shareBalanceMap.put("sharePrice", shareBalance.getSharePrice());
                    shareBalanceMap.put("shareValue", shareBalance.getShareValue());
                    shareBalanceMap.put("accountName", account.getName());
                    shareBalanceMap.put("accountCompany", account.getCompany().getCompanyName());
                    shareBalances.add(shareBalanceMap);
                }
            }
        }
        Collections.sort(shareBalances, byTickerCompanyAndAccount);
        return shareBalances;
    }
}
