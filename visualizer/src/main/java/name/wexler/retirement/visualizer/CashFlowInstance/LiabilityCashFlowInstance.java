package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.CashFlowSource;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LiabilityCashFlowInstance extends CashFlowInstance {
    private final BigDecimal principal;
    private final BigDecimal interest;
    private final BigDecimal impounds;

    public LiabilityCashFlowInstance(boolean estimated, CashFlowSource cashFlowSource, CashFlowSink cashFlowSink,
                                     String category,
                                     LocalDate accrualStart, LocalDate accrualEnd,
                                     LocalDate cashFlowDate,
                                     BigDecimal principal, BigDecimal interest, BigDecimal impounds,
                                     BigDecimal balance) {
        super(false, cashFlowSource, cashFlowSink,
                CashFlowCalendar.ITEM_TYPE.EXPENSE.toString(), category,
                accrualStart, accrualEnd, cashFlowDate,
                principal.add(interest).add(impounds), balance);
        this.principal = principal;
        this.interest = interest;
        this.impounds = impounds;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public BigDecimal getImpounds() {
        return impounds;
    }

    public BigDecimal getBalance() { return getCashBalance(); }
}
