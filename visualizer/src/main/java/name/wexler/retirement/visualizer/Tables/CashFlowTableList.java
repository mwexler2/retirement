package name.wexler.retirement.visualizer.Tables;

import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.CashFlowEstimator.Liability;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Scenario;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class CashFlowTableList extends TableList {
    private static final String VISUALIZER_PATH_ELEM = "visualizer";
    private static final String SCENARIO_PATH_ELEM = "scenario";
    private static final String GROUPING_PATH_ELEM = "grouping";
    public static final String FIXED_COLUMN = "fixed-column";

    private final List<ColumnDefinition> columnDefinitions;
    private final CashFlowCalendar cashFlowCalendar;

    CashFlowTableList(CashFlowCalendar cashFlowCalendar, List<ColumnDefinition> columnDefinitions) {
        super(columnDefinitions);
        this.columnDefinitions = columnDefinitions;
        this.cashFlowCalendar = cashFlowCalendar;
    }

    @NotNull
    @org.jetbrains.annotations.Contract(" -> new")
    private static CashFlowTableList getTableList(Scenario scenario, CashFlowCalendar cashFlowCalendar, int startYear, int endYear) {
        List<ColumnDefinition> columnDefinitions = new ArrayList<>();
        columnDefinitions.add(ColumnDefinition.Builder.newInstance().
                setName("Category").
                setProperty("name").
                setClassName(FIXED_COLUMN).
                setHeaderClassName(FIXED_COLUMN).
                setTotal(false).
                build());
        for (int year = startYear; year <= endYear; ++year) {
            columnDefinitions.add(ColumnDefinition.Builder.newInstance().
                    setName(Integer.toString(year)).
                    setParamName("key").
                    setClassName("money").
                    setProperty(Integer.toString(year)).
                    setDecorator(MoneyTableColumnDecorator.class.getName()).
                    setTotal(true).
                    setHref(String.join("/",
                                    VISUALIZER_PATH_ELEM,
                                    SCENARIO_PATH_ELEM, scenario.getId())).
                    build());
        }
        return new CashFlowTableList(cashFlowCalendar, columnDefinitions);
    }

    private BigDecimal getAnnualCashFlow(String cashFlowId, Integer year) {
        return cashFlowCalendar.getCashFlowInstances().stream().
                filter(instance->instance.getYear() == year).
                filter(instance->instance.getCashFlowId() == cashFlowId).
                map(instance->instance.getAmount()).
                collect(Collectors.reducing(BigDecimal.ZERO, BigDecimal::add));
    }


    private BigDecimal getEntityValue(Entity entity, Integer year) {
        if (entity instanceof Asset) {
            return cashFlowCalendar.getAssetValue(entity.getId(), year);
        } else if (entity instanceof Liability) {
            return cashFlowCalendar.getLiabilityAmount(entity.getId(), year);
        } else if (entity instanceof CashFlowEstimator) {
            return getAnnualCashFlow(entity.getId(), year);
        } else {
            System.err.println("Can't handle entity type: " + entity.getClass().getSimpleName());
            return BigDecimal.ZERO;
        }
    }
    private Map<String, Object> createRow(String scenarioId,
                          Entity entity,
                          final @NotNull String parentCategory,
                          final @NotNull String itemType,
                          int startYear,
                          int endYear) {
        Map<String, Object> row = new HashMap();
        row.put("id", entity.getId());
        row.put("name", decorateName(scenarioId, itemType, entity.getId(), entity.getName()));
        row.put("itemType", itemType);
        row.put("parentCategory", parentCategory);
        row.put("itemCategory", entity.getCategory());
        for (int year = startYear; year <= endYear; ++year) {
            String link = "/" +
                    String.join("/",
                    VISUALIZER_PATH_ELEM,
                    "scenario", scenarioId,
                    itemType, entity.getId(),
                    "year", Integer.toString(year));
            row.put(Integer.toString(year),
                    new AmountAndLink(getEntityValue(entity, year), link));
        }
        return row;
    }

    private static String decorateName(String scenarioId, String itemType, String id, String name) {
        return "<a href='" +
            String.join("/",
                    VISUALIZER_PATH_ELEM,
                    SCENARIO_PATH_ELEM, scenarioId,
                    GROUPING_PATH_ELEM, itemType,
                    id) + "'>" +
                name + "</a>";
    }

    private static final Comparator<Map<String, Object>> byTypeParentAndCategory = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            int result = 0;
            result = ((String) o1.getOrDefault("itemType", "")).
                    compareTo((String) o2.getOrDefault("itemType", ""));
            if (result == 0)
                result = ((String) o1.getOrDefault("parentCategory", "")).
                        compareTo((String) o2.getOrDefault("parentCategory", ""));
            if (result == 0)
                result = ((String) o1.getOrDefault("itemCategory", "")).
                        compareTo((String) o2.getOrDefault("itemCategory", ""));
            return result;
        }
    };

    static public CashFlowTableList getAssetsAndLiabilities(Scenario scenario,
                                                            CashFlowCalendar cashFlowCalendar,
                                                            int startYear, int endYear) {
        CashFlowTableList tableList = getTableList(scenario, cashFlowCalendar, startYear, endYear);

        String itemType = "asset";
        String parentCategory = "parent";
        for (Asset asset: cashFlowCalendar.getAssets()) {
            Map assetRow = tableList.createRow(scenario.getId(), asset, parentCategory, itemType, startYear, endYear);
            tableList.add(assetRow);
        }

        itemType = "liability";
        for (Liability liability: cashFlowCalendar.getLiabilities().values()) {
            Map<String, Object> liabilityRow =
                    tableList.createRow(scenario.getId(), liability, parentCategory, itemType, startYear, endYear);
            tableList.add(liabilityRow);
        }

        Collections.sort(tableList, byTypeParentAndCategory);
        return tableList;
    }

    private static CashFlowTableList createTableListFromNestedHash(@NotNull Map<String, Map<String, Map<String, Map<Integer, BigDecimal>>>> nestedHash,
                                                            Scenario scenario,
                                                            CashFlowCalendar cashFlowCalendar,
                                                            int startYear,
                                                            int endYear) {
        CashFlowTableList tableList = getTableList(scenario, cashFlowCalendar, startYear, endYear);

        for (Map.Entry<String, Map<String, Map<String, Map<Integer, BigDecimal>>>> outerEntry: nestedHash.entrySet()) {
            String itemType = outerEntry.getKey();
            for (Map.Entry<String, Map<String, Map<Integer, BigDecimal>>> middleEntry: outerEntry.getValue().entrySet()) {
                String parentCategory = middleEntry.getKey();
                for (Map.Entry<String, Map<Integer, BigDecimal>> innerEntry: middleEntry.getValue().entrySet()) {
                    Map<String, Object> row = new HashMap<>();
                    String itemCategory = innerEntry.getKey();
                    row.put("itemCategory", itemCategory);
                    row.put("parentCategory", parentCategory);
                    row.put("itemType", itemType);
                    row.put("name", decorateName(scenario.getId(), itemType, innerEntry.getKey(), innerEntry.getKey()));
                    for (int year = startYear; year <= endYear; ++year) {
                        String link = "/" +
                                String.join("/",
                                VISUALIZER_PATH_ELEM,
                                SCENARIO_PATH_ELEM,
                                scenario.getId(),
                                GROUPING_PATH_ELEM,
                                itemType, itemCategory,
                                "year", Integer.toString(year));
                        row.put(Integer.toString(year),
                                new AmountAndLink(innerEntry.getValue().getOrDefault(year, BigDecimal.ZERO), link));
                    }
                    tableList.add(row);
                }
            }
        }
        Collections.sort(tableList, byTypeParentAndCategory);
        return tableList;
    }

    public static CashFlowTableList getCashFlowTable(Scenario scenario,
                                                     CashFlowCalendar cashFlowCalendar,
                                                     List<CashFlowInstance> instances,
                                                     int startYear, int endYear) {
        Map<String, Map<String, Map<String, Map<Integer, BigDecimal>>>> categoryMap =
                instances.stream().
                        collect(Collectors.groupingBy(CashFlowInstance::getItemType,
                                Collectors.groupingBy(CashFlowInstance::getParentCategory,
                                        Collectors.groupingBy(CashFlowInstance::getCategory,
                                                Collectors.groupingBy(CashFlowInstance::getYear,
                                                        Collectors.mapping(instance -> instance.getAmount(),
                                                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)))))));
        return createTableListFromNestedHash(categoryMap, scenario, cashFlowCalendar,startYear, endYear);
    }
}
