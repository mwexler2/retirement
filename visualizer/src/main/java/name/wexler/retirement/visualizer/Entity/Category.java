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
    public static final String EXPENSE = "EXPENSE";
    public static final String INCOME = "INCOME";
    public static final String TRANSFER = "TRANSFER";
    public static final String BILLS_AND_UTILITIES = "Bills & Utilities";
    public static final String HOME = "Home";
    public static final String MORTGAGE = "Mortgage";
    public static final String INVESTMENT = "Invesmtent";
    public static final String TRANSFERS = "Transfers";
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
