package name.wexler.retirement.CashFlow;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by mwexler on 11/29/16.
 */
public class CashFlowInstance {
    private LocalDate accrualStart;
    private LocalDate accrualEnd;
    private LocalDate cashFlowDate;
    private BigDecimal amount;

    public CashFlowInstance(LocalDate accrualStart, LocalDate accrualEnd, LocalDate cashFlowDate, BigDecimal amount) {
        this.accrualStart = accrualStart;
        this.accrualEnd = accrualEnd;
        this.cashFlowDate = cashFlowDate;
        this.amount = amount;
    }

    public boolean isPaidInDateRange(LocalDate startDate, LocalDate endDate) {
        if (!cashFlowDate.isAfter(endDate) && !cashFlowDate.isBefore((startDate))) {
            return true;
        }
        return false;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getCashFlowDate() {
        return cashFlowDate;
    }


    @Override
    public String toString() {
        String result = cashFlowDate.toString() + ": " + getAmount();
        return result;
    }
}
