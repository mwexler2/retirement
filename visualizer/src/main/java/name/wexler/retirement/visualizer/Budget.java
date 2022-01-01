package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.CashFlowFrequency.Annual;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.visualizer.CashFlowFrequency.Monthly;
import name.wexler.retirement.visualizer.CashFlowFrequency.SemiAnnual;
import name.wexler.retirement.visualizer.Entity.Category;
import name.wexler.retirement.visualizer.Entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class Budget {
    public static final String BUDGET_CONTEXT_ID_PREFIX = "Budget-";
    private final String parentCategory;
    private final int type;
    private final static int EVERY_MONTH = 0;
    private final static int EVERY_FEW_MONTHS = 1;
    private final static int ONCE = 2;
    private final Boolean isIncome;
    private final Boolean isTransfer;
    private final Boolean isExpense;
    private final Optional<BigDecimal> amount;
    private final BigDecimal budget;
    private final BigDecimal rBal;
    private final String category;
    private final String grouping;
    private final Optional<Long> period;
    private final Optional<BigDecimal> aamt;
    private final Optional<BigDecimal> tbgt;
    private final Optional<Boolean> isLast;
    private final Optional<LocalDate> date;
    private CashFlowFrequency cashFlowFrequency;
    public static final String INCOME_GROUPING = "income";
    public static final String EXPENSE_GROUPING = "expense";

    public Budget(@NotNull Context context,
                  @NotNull Optional<LocalDate> date,
                  @NotNull int st,
                  @NotNull int type,
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
        this.type = type;
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
        this.date = date;
        try {
            if (type == EVERY_MONTH) {
                LocalDate startOfNextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1);
                LocalDate endOfNextMonth = startOfNextMonth.withDayOfMonth(startOfNextMonth.lengthOfMonth());
                LocalDate paymentDate = startOfNextMonth.withDayOfMonth(15);
                this.cashFlowFrequency = new Monthly(context, "budget-" + category, startOfNextMonth, endOfNextMonth, paymentDate,
                        CashFlowFrequency.ApportionmentPeriod.EQUAL_MONTHLY);
            } else if (type == EVERY_FEW_MONTHS) {
                if (period.get() == 12) {
                    LocalDate startOfNextYear = date.get().withDayOfYear(1);
                    LocalDate endOfNextYear = date.get().withDayOfYear(startOfNextYear.lengthOfYear());
                    this.cashFlowFrequency = new Annual(context, "budget-" + category,
                            startOfNextYear, endOfNextYear, date.get(),
                            CashFlowFrequency.ApportionmentPeriod.ANNUAL);
                } else if (period.get() == 6) {
                    LocalDate startOfHalf = date.get().withDayOfMonth(1);
                    LocalDate endOfHalf = date.get().withMonth(6).withDayOfMonth(30);
                    LocalDate endOfYear = date.get().withMonth(12).withDayOfMonth(31);
                    this.cashFlowFrequency = new SemiAnnual(context, "budget-" + category,
                            startOfHalf, endOfYear, endOfHalf, date.get(),
                            CashFlowFrequency.ApportionmentPeriod.ANNUAL);
                }
            } else if (type == ONCE) { // Once
                this.cashFlowFrequency = new Monthly(context, "budget-" + category,
                        date.get(), date.get(), date.get(),
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
            }
        } catch (Entity.DuplicateEntityException dee) {
            throw new RuntimeException(dee);
        }

        if (cashFlowFrequency == null)
            throw new RuntimeException("No cash flow Frequency for " + category + ", type = " + type);
    }

    public CashFlowFrequency getCashFlowFrequency() {
        return cashFlowFrequency;
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
        if (type == ONCE || (type == EVERY_FEW_MONTHS && period.equals(12)))
            return tbgt.get();
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

    public Optional<LocalDate> getDate() {
        return date;
    }

    public String getItemType() {
        if (grouping.equalsIgnoreCase("income")) {
            return Category.INCOME_ITEM_TYPE;
        }
        return Category.EXPENSE_ITEM_TYPE;
    }
}
