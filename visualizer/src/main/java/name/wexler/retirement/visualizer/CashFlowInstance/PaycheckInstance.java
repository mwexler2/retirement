package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.CashFlowSource.CashFlowSource;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaycheckInstance extends CashFlowInstance {
    private Entity employer;

    public PaycheckInstance(CashFlowSource cashFlowSource,
                            LocalDate accrualStart, LocalDate accrualEnd,
                            LocalDate cashFlowDate, BigDecimal amount, BigDecimal balance,
                            Entity employer) {
        super(cashFlowSource, accrualStart, accrualEnd, cashFlowDate, amount, balance);
        this.employer = employer;
    }
}
