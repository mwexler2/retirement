package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.math.BigDecimal;

public interface CashFlowSink {
    void updateRunningTotal(CashFlowInstance cashFlow);
    String getId();
    boolean isOwner(Entity entity);
}
