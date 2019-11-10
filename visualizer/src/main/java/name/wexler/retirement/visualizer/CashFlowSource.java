package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Entity.Entity;

public interface CashFlowSource {
    public void sourceCashFlowInstance(CashFlowInstance cashFlowInstance);

    public String getId();
}
