package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.CashFlowSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by mwexler on 11/29/16.
 */

public class CashFlowInstance implements Comparable<CashFlowInstance> {
    private final CashFlowSource cashFlowSource;
    private final LocalDate accrualStart;
    private final LocalDate accrualEnd;
    private final LocalDate cashFlowDate;
    private final BigDecimal amount;
    private BigDecimal cashBalance;
    private BigDecimal assetBalance;
    private String description = "";
    private String notes = "";
    private List<String> labels = Collections.emptyList();
    public String itemType = "";
    private String category = "";
    private CashFlowSink cashFlowSink;
    private boolean estimated;
    private long id;
    public static long NO_ID = 2102102102;

    public CashFlowInstance(long id, boolean estimated,
                            CashFlowSource cashFlowSource, CashFlowSink cashFlowSink,
                            String itemType, String category,
                            LocalDate accrualStart, LocalDate accrualEnd, LocalDate cashFlowDate,
                            BigDecimal amount, BigDecimal balance, String description) {
        this.id = id;
        this.estimated = estimated;
        this.accrualStart = accrualStart;
        this.accrualEnd = accrualEnd;
        this.cashFlowDate = cashFlowDate;
        this.amount = amount;
        this.cashBalance = balance;
        this.assetBalance = BigDecimal.ZERO;
        this.cashFlowSource = cashFlowSource;
        this.cashFlowSink = cashFlowSink;
        this.itemType = itemType;
        this.category = category;
        this.description = description;
    }

    public int compareTo(CashFlowInstance that) {
        int result = 0;
        result = this.cashFlowDate.compareTo(that.getCashFlowDate());
        if (result != 0)
            return result;
        result = this.accrualStart.compareTo(that.accrualStart);
        if (result != 0)
            return result;
        result = this.category.compareTo(that.category);
        if (result != 0)
            return result;
        result = this.description.compareTo(that.description);
        if (result != 0)
            return result;
        return result;
    }

    public long getId() {
        return this.id;
    }
    public boolean isEstimate() {
        return estimated;
    }

    public String getCategory() {
        if (category == null)
            return "unknown";
        return category;
    }

    public String getItemType() {
        if (itemType == null)
            return "unknown";
        return itemType;
    }

    public void setCategory(String category) { this.category = category; }

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes; }

    public List<String> getLabels() { return labels; }

    public void setLabels(List<String> labels) { this.labels = labels; }

    public String getDescription() { return description; }

    public String getCashFlowId() {
        return cashFlowSource.getId();
    }

    public CashFlowSource getCashFlowSource() {
        return cashFlowSource;
    }

    public CashFlowSink getCashFlowSink() {
        return cashFlowSink;
    }

    public String getCashFlowSinkId() {
        return cashFlowSink.getId();
    }

    public LocalDate getAccrualStart() {
        return accrualStart;
    }

    public LocalDate getAccrualEnd() {
        return accrualEnd;
    }

    public boolean isPaidInDateRange(LocalDate startDate, LocalDate endDate) {
        return !cashFlowDate.isAfter(endDate) && !cashFlowDate.isBefore((startDate));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getCashBalance() { return cashBalance; }

    public void setAssetBalance(BigDecimal newBalance) {
        this.assetBalance = newBalance;
    }

    public BigDecimal getAssetBalance() { return assetBalance; }

    public void setCashBalance(BigDecimal newBalance) {
        this.cashBalance = newBalance;
    }

    public LocalDate getCashFlowDate() {
        return cashFlowDate;
    }

    public int getYear() {
        return cashFlowDate.getYear();
    }

    public String getCashFlowSourceId() {
        return getCashFlowSource().getId();
    }

    @Override
    public String toString() {
        String result = cashFlowDate.toString() + ": " + getAmount() + " => " + cashBalance;
        return result;
    }
}
