package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Job;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaycheckInstance extends CashFlowInstance {
    private Entity employer;

    public PaycheckInstance(
            Job job,
            CashFlowSink sink,
            String category,
            LocalDate accrualStart, LocalDate accrualEnd,
            LocalDate cashFlowDate, BigDecimal amount, BigDecimal balance, String description) {
        super(false, job, sink,
                CashFlowCalendar.ITEM_TYPE.INCOME.toString(), category,
                accrualStart, accrualEnd, cashFlowDate, amount, balance, description);
        this.employer = job.getEmployer();
    }
}
