package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.CashFlowSource;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.util.List;

public interface Account extends CashFlowSource, CashFlowSink {
    Entity getCompany();
    String getTxnSource();

    String getName();
}
