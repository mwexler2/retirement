package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.CashFlowSource.CashFlowSource;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.util.List;

public interface Account {
    void addCashFlowInstances(List<CashFlowInstance> instances);

    Entity getCompany();

    CashFlowSource getCashFlowSource();
}
