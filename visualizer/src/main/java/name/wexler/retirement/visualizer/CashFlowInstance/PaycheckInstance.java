package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.CashFlowSource;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Job;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaycheckInstance extends CashFlowInstance {
    private Entity employer;

    public PaycheckInstance(CashFlowSource cashFlowSource,
                            Job job,
                            String category,
                            LocalDate accrualStart, LocalDate accrualEnd,
                            LocalDate cashFlowDate, BigDecimal amount, BigDecimal balance) {
        super(false, cashFlowSource, job.getDefaultSink(), category, accrualStart, accrualEnd, cashFlowDate, amount, balance);
        this.employer = job.getEmployer();
    }
}
