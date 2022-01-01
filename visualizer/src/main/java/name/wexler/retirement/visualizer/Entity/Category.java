package name.wexler.retirement.visualizer.Entity;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import name.wexler.retirement.visualizer.Context;

import java.io.IOException;
import java.util.List;

@JsonPropertyOrder({ "type", "name", "itemType"})
public class Category extends Entity {
    private final String name;
    private final String itemType;
    private static final String categoryPath = "categories.json";
    public static final String EXPENSE_ITEM_TYPE = "EXPENSE";
    public static final String INCOME_ITEM_TYPE = "INCOME";
    public static final String BILLS_AND_UTILITIES_CATEGORY = "Bills & Utilities";
    public static final String HOME_CATEGORY = "Home";
    public static final String MORTGAGE_CATEGORY = "Mortgage";
    public static final String INVESTMENT = "Invesmtents";
    public static final String TRANSFER_ITEM_TYPE = "TRANSFER";
    public static final String UNKNOWN = "Unknown";

    public static void readCategories(Context context) throws IOException {
        context.fromJSONFileList(Category[].class, categoryPath);
    }

    @JsonCreator
    public Category(@JacksonInject("context") Context context,
                    @JsonProperty("name") String id,
                    @JsonProperty("itemType") String itemType) throws DuplicateEntityException {
        super(context, id, Category.class);
        this.name = id;
        this.itemType = itemType;
    }

    public String getItemType() {
        return itemType;
    }

    @Override
    public String getName() {
        return name;
    }
}
