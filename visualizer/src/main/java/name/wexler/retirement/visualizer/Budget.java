package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.Entity.Category;

import java.math.BigDecimal;
import java.util.List;

public class Budget {
    private String parentCategory;
    private Boolean isIncome;
    private Boolean isTransfer;
    private Boolean isExpense;
    private BigDecimal amount;
    private BigDecimal budget;
    private BigDecimal rBal;
    private String category;
    private String grouping;
    public static final String INCOME_GROUPING = "income";

    public Budget(Context context,
                  String grouping,
                  Boolean isIncome,
                  Boolean isTransfer,
                  Boolean isExpense,
                  BigDecimal amount,
                  BigDecimal budget,
                  BigDecimal rBal,
                  String parentCategory,
                  String category) {
        this.grouping = grouping;
        this.isIncome = isIncome;
        this.isTransfer = isTransfer;
        this.isExpense = isExpense;
        this.amount = amount;
        this.budget = budget;
        this.rBal = rBal;
        this.parentCategory = parentCategory;
        this.category = category;
    }

    public String getParentCategory() {
        return parentCategory;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getGrouping() {
        return grouping;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public String getItemType() {
        if (grouping.equalsIgnoreCase("income")) {
            return Category.INCOME;
        }
        return Category.EXPENSE;
    }
}
