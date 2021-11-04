package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Job;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaycheckInstance extends CashFlowInstance {
    private Entity employer;

    public PaycheckInstance(
            long id,
            Job job,
            CashFlowSink sink,
            @NotNull String parentCategory,
            @NotNull String category,
            LocalDate accrualStart, LocalDate accrualEnd,
            LocalDate cashFlowDate, BigDecimal amount, BigDecimal balance, String description) {
        super(id, false, job, sink,
                CashFlowCalendar.ITEM_TYPE.INCOME.toString(), parentCategory, category,
                accrualStart, accrualEnd, cashFlowDate, amount, balance, description);
        this.employer = job.getEmployer();
    }
}
