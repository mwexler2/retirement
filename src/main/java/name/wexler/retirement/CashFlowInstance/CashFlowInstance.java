package name.wexler.retirement.CashFlowInstance;

import com.sun.xml.internal.xsom.impl.scd.Iterators;
import name.wexler.retirement.CashFlowSource.CashFlowSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
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
    private BigDecimal balance;
    private String descripotion = "";
    private String notes = "";
    private List<String> labels = Arrays.asList();
    private String category = "";

    public CashFlowInstance(CashFlowSource cashFlowSource, LocalDate accrualStart, LocalDate accrualEnd, LocalDate cashFlowDate, BigDecimal amount,
                            BigDecimal balance) {
        this.accrualStart = accrualStart;
        this.accrualEnd = accrualEnd;
        this.cashFlowDate = cashFlowDate;
        this.amount = amount;
        this.balance = balance;
        this.cashFlowSource = cashFlowSource;
    }

    public String getAction() { return action; }

    public void setAction(String action) { this.action = action; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes; }

    public List<String> getLabes() { return labels; }

    public void setLabels(List<String> labels) { this.labels = labels; }

    public String getDescripotion() { return descripotion; }

    public void setDescripotion(String descripotion) { this.descripotion = descripotion; }

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

    public BigDecimal getBalance() { return balance; }

    public void setBalance(BigDecimal newBalance) {
        this.balance = newBalance;
    }

    public LocalDate getCashFlowDate() {
        return cashFlowDate;
    }


    @Override
    public String toString() {
        String result = cashFlowDate.toString() + ": " + getAmount() + " => " + balance;
        return result;
    }
}
