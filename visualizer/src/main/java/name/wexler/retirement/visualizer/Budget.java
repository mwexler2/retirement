package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.Entity.Category;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class Budget {
    private String parentCategory;
    private Boolean isIncome;
    private Boolean isTransfer;
    private Boolean isExpense;
    private Optional<BigDecimal> amount;
    private BigDecimal budget;
    private BigDecimal rBal;
    private String category;
    private String grouping;
    private Optional<Long> period;
    private Optional<BigDecimal> aamt;
    private Optional<BigDecimal> tbgt;
    private Optional<Boolean> isLast;
    public static final String INCOME_GROUPING = "income";
    public static final String EXPENSE_GROUPING = "expense";

    public Budget(@NotNull Context context,
                  @NotNull String grouping,
                  @NotNull Boolean isIncome,
                  @NotNull Boolean isTransfer,
                  @NotNull Boolean isExpense,
                  @NotNull Optional<BigDecimal> amount,
                  @NotNull BigDecimal budget,
                  @NotNull BigDecimal rBal,
                  @NotNull String parentCategory,
                  @NotNull String category,
                  @NotNull Optional<Long> period,
                  @NotNull Optional<BigDecimal> aamt,
                  @NotNull Optional<BigDecimal> tbgt,
                  @NotNull Optional<Boolean> isLast
    ) {
        this.grouping = grouping;
        this.isIncome = isIncome;
        this.isTransfer = isTransfer;
        this.isExpense = isExpense;
        this.amount = amount;
        this.budget = budget;
        this.rBal = rBal;
        this.parentCategory = parentCategory;
        this.category = category;
        this.period = period;
        this.aamt = aamt;
        this.tbgt = tbgt;
        this.isLast = isLast;
    }

    public String getParentCategory() {
        return parentCategory;
    }

    public String getCategory() {
        return category;
    }

    public Optional<BigDecimal> getAmount() {
        return amount;
    }

    public String getGrouping() {
        return grouping;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public Optional<Long> getPeriod() {
        return period;
    }

    public Optional<BigDecimal> getAamt() {
        return aamt;
    }

    public Optional<BigDecimal> getTbgt() {
        return tbgt;
    }

    public Optional<Boolean> getLast() {
        return isLast;
    }

    public String getItemType() {
        if (grouping.equalsIgnoreCase("income")) {
            return Category.INCOME;
        }
        return Category.EXPENSE;
    }
}
