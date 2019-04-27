package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.CashFlowSource.CashFlowSource;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentInstance extends CashFlowInstance {
    private Entity payee;
    private String category;

    public PaymentInstance(CashFlowSource cashFlowSource,
                           LocalDate accrualStart, LocalDate accrualEnd,
                           LocalDate cashFlowDate, BigDecimal amount, BigDecimal balance,
                           Entity payee, String category) {
        super(cashFlowSource, accrualStart, accrualEnd, cashFlowDate, amount, balance);
        this.payee = payee;
        this.category = category;
    }
}
