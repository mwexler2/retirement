package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.CashFlowSource;
import name.wexler.retirement.visualizer.Entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReimbursementInstance extends CashFlowInstance {
    private Entity employer;

    public ReimbursementInstance(long id, CashFlowSource cashFlowSource, CashFlowSink cashFlowSink,
                                 @NotNull String parentCategory,
                                 @NotNull String category,
                                 LocalDate accrualStart, LocalDate accrualEnd,
                                 LocalDate cashFlowDate, BigDecimal amount, BigDecimal balance,
                                 Entity employer, String description) {
        super(id, false, cashFlowSource, cashFlowSink,
                CashFlowCalendar.ITEM_TYPE.INCOME.toString(), parentCategory, category,
                accrualStart, accrualEnd, cashFlowDate, amount, balance, description);
        this.employer = employer;
    }
}
