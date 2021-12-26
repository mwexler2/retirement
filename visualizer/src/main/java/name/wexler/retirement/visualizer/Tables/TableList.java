package name.wexler.retirement.visualizer.Tables;

import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.CashFlowEstimator.Liability;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class TableList extends ArrayList<Map<String, Object>> {
    private final List<ColumnDefinition> columnDefinitions;

    TableList(List<ColumnDefinition> columnDefinitions) {
        super();
        this.columnDefinitions = columnDefinitions;
    }

    public List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public String getItemCategory(@NotNull Map<String, Object> item) {
        return (String) item.get("itemCategory");
    }

    public String getItemType(@NotNull Map<String, Object> item) {
        return (String) item.get("itemType");
    }

}
