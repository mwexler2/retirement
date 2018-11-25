package name.wexler.retirement.CashFlow;

import name.wexler.retirement.CashFlowSource;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by mwexler on 11/29/16.
 */
public class CashFlowInstance {
    private final CashFlowSource cashFlowSource;
    private final LocalDate accrualStart;
    private final LocalDate accrualEnd;
    private final LocalDate cashFlowDate;
    private final BigDecimal amount;
    private final BigDecimal balance;

    public CashFlowInstance(CashFlowSource cashFlowSource, LocalDate accrualStart, LocalDate accrualEnd, LocalDate cashFlowDate, BigDecimal amount,
                            BigDecimal balance) {
        this.accrualStart = accrualStart;
        this.accrualEnd = accrualEnd;
        this.cashFlowDate = cashFlowDate;
        this.amount = amount;
        this.balance = balance;
        this.cashFlowSource = cashFlowSource;
    }

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

    public LocalDate getCashFlowDate() {
        return cashFlowDate;
    }


    @Override
    public String toString() {
        String result = cashFlowDate.toString() + ": " + getAmount();
        return result;
    }
}
