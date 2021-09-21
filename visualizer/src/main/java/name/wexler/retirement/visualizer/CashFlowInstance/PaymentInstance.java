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

    public PaymentInstance(long id,
                           CashFlowSource cashFlowSource,
                           CashFlowSink cashFlowSink,
                           String category,
                           LocalDate accrualStart, LocalDate accrualEnd,
                           LocalDate cashFlowDate, BigDecimal amount, BigDecimal balance,
                           Entity payee, String description) {
        super(id,false, cashFlowSource, cashFlowSink,
                CashFlowCalendar.ITEM_TYPE.EXPENSE.toString(), category,
                accrualStart, accrualEnd, cashFlowDate, amount.negate(), balance, description);
        this.payee = payee;
        this.category = category;
    }
}
