package name.wexler.retirement.CashFlow;

import name.wexler.retirement.CashFlowSource.CashFlowSource;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LiabilityCashFlowInstance extends CashFlowInstance {
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal impounds;

    public LiabilityCashFlowInstance(CashFlowSource cashFlowSource,
                                     LocalDate accrualStart, LocalDate accrualEnd,
                                     LocalDate cashFlowDate,
                                     BigDecimal principal, BigDecimal interest, BigDecimal impounds,
                                     BigDecimal balance) {
        super(cashFlowSource, accrualStart, accrualEnd, cashFlowDate, principal.add(interest).add(impounds), balance);
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

}
