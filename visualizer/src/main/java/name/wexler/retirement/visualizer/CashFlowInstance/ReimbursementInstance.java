package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.CashFlowSource;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReimbursementInstance extends CashFlowInstance {
    private Entity employer;

    public ReimbursementInstance(CashFlowSource cashFlowSource, CashFlowSink cashFlowSink,
                                 String category,
                                 LocalDate accrualStart, LocalDate accrualEnd,
                                 LocalDate cashFlowDate, BigDecimal amount, BigDecimal balance,
                                 Entity employer) {
        super(false, cashFlowSource, cashFlowSink,
                CashFlowCalendar.ITEM_TYPE.INCOME.toString(), category,
                accrualStart, accrualEnd, cashFlowDate, amount, balance);
        this.employer = employer;
    }
}
