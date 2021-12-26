package name.wexler.retirement.visualizer.Tables;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
