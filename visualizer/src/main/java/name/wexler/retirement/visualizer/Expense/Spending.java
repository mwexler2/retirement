package name.wexler.retirement.visualizer.Expense;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowFrequency.Balance;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.util.Comparator;
import java.util.List;

public class Spending extends Expense {
    @JsonCreator
    protected Spending(@JacksonInject("context") Context context,
                    @JsonProperty("id") String id) throws Entity.DuplicateEntityException {
        super(context, id);
    }

    public void sourceCashFlowInstance(CashFlowInstance cashFlowInstance) {

    }

    @Override
    public boolean isOwner(Entity entity) {
        return false;
    }
}
