package name.wexler.retirement.CashFlow;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by mwexler on 11/29/16.
 */
public class CashFlowInstance {
    private final String cashFlowId;
    private final LocalDate accrualStart;
    private final LocalDate accrualEnd;
    private final LocalDate cashFlowDate;
    private final BigDecimal amount;

    public CashFlowInstance(String cashFlowId, LocalDate accrualStart, LocalDate accrualEnd, LocalDate cashFlowDate, BigDecimal amount) {
        this.cashFlowId = cashFlowId;
        this.accrualStart = accrualStart;
        this.accrualEnd = accrualEnd;
        this.cashFlowDate = cashFlowDate;
        this.amount = amount;
    }

    public String getCashFlowId() {
        return cashFlowId;
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

    public LocalDate getCashFlowDate() {
        return cashFlowDate;
    }


    @Override
    public String toString() {
        String result = cashFlowDate.toString() + ": " + getAmount();
        return result;
    }
}
