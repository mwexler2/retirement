package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.CashFlowSource;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.util.List;

public interface Account extends CashFlowSource, CashFlowSink {
    void addCashFlowInstances(List<CashFlowInstance> instances);

    Entity getCompany();

    String getName();
}
