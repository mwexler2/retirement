package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Entity.Entity;

public interface CashFlowSink {
    void sinkCashFlowInstance(CashFlowInstance cashFlowInstance);

    String getId();

    boolean isOwner(Entity entity);
}
