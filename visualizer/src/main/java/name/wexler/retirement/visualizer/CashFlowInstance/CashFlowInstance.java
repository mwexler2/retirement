package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.CashFlowSource.CashFlowSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Created by mwexler on 11/29/16.
 */
public class CashFlowInstance {
    private final CashFlowSource cashFlowSource;
    private final LocalDate accrualStart;
    private final LocalDate accrualEnd;
    private final LocalDate cashFlowDate;
    private final BigDecimal amount;
    private String action;
    private BigDecimal cashBalance;
    private BigDecimal assetBalance;
    private String description = "";
    private String notes = "";
    private List<String> labels = Collections.emptyList();
    private String category = "";

    public CashFlowInstance(CashFlowSource cashFlowSource, LocalDate accrualStart, LocalDate accrualEnd, LocalDate cashFlowDate, BigDecimal amount,
                            BigDecimal balance) {
        this.accrualStart = accrualStart;
        this.accrualEnd = accrualEnd;
        this.cashFlowDate = cashFlowDate;
        this.amount = amount;
        this.cashBalance = balance;
        this.assetBalance = BigDecimal.ZERO;
        this.cashFlowSource = cashFlowSource;
    }

    public String getAction() { return action; }

    public void setAction(String action) { this.action = action; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes; }

    public List<String> getLabels() { return labels; }

    public void setLabels(List<String> labels) { this.labels = labels; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getCashFlowId() {
        return cashFlowSource.getId();
    }

    public CashFlowSource getCashFlowSource() {
        return cashFlowSource;
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


    @Override
    public String toString() {
        String result = cashFlowDate.toString() + ": " + getAmount() + " => " + cashBalance;
        return result;
    }
}