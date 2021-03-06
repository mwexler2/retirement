package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.CashFlowSource;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentInstance extends CashFlowInstance {
    private Entity payee;
    private String category;

    public PaymentInstance(CashFlowSource cashFlowSource,
                           CashFlowSink cashFlowSink,
                           String category,
                           LocalDate accrualStart, LocalDate accrualEnd,
                           LocalDate cashFlowDate, BigDecimal amount, BigDecimal balance,
                           Entity payee) {
        super(false, cashFlowSource, cashFlowSink,
                CashFlowCalendar.ITEM_TYPE.EXPENSE.toString(), category,
                accrualStart, accrualEnd, cashFlowDate, amount, balance);
        this.payee = payee;
        this.category = category;
    }
}
