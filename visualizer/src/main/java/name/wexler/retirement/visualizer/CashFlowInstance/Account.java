package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.CashFlowSource.CashFlowSource;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.util.List;

public interface Account {
    public void addCashFlowInstances(List<CashFlowInstance> instances);

    public Entity getCompany();

    public CashFlowSource getCashFlowSource();
}
