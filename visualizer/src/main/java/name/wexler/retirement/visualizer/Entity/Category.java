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
    public static final String INVESTMENT = "Investments";
    public static final String INTEREST_INCOME = "Interest Income";
    public static final String TRANSFER_ITEM_TYPE = "TRANSFER";
    public static final String TRANSFER_CATEGORY = "Transfer";
    public static final String JOURNAL_CATEGORY = "Journal";
    public static final String OVERDRAFT_CATEGORY = "Overdraft";
    public static final String CHECK_CATEGORY = "Check";
    public static final String SALARY_CATEGORY = "Salary";
    public static final String CONTRIBUTION_CATEGORY = "Contribution";
    public static final String REINVEST_SHARES_CATEGORY = "Reinvest Shares";
    public static final String REINVEST_DIVIDENDS_CATEGORY = "Dividend Reinvestment";
    public static final String UNCATEGORIZED_CATEGORY = "Uncategorized";
    public static final String PAYROLL_CONTRIBUTION_CATEGORY = "Payroll Contribution";
    public static final String RETIREMENT_CATEGORY = "Retirement";
    public static final String FUNDS_RECEIVED_CATEGORY = "Funds Received";
    public static final String BANK_FEE_CATEGORY = "Bank fee";
    public static final String FEES_AND_CHARGES_CATEGORY = "Fees & Charges";
    public static final String PRINCIPAL_CATEGORY = "Principal";
    public static final String DIVIDENDS_AND_CAPITAL_GAINS_CATEGORY = "Dividends & Capital Gains";

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
