package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.math.BigDecimal;

public interface CashFlowSink {
    void updateRunningTotal(CashFlowInstance cashFlow);
    void setRunningTotal(BigDecimal runningTotal);
    String getId();
    boolean isOwner(Entity entity);
}
