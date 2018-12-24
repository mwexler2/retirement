package name.wexler.retirement.CashFlowSource;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.CashFlow.CashFlowCalendar;
import name.wexler.retirement.CashFlow.CashFlowInstance;
import name.wexler.retirement.Context;
import name.wexler.retirement.Entity.Entity;

import java.util.List;

public class AccountSource extends CashFlowSource {
    @JsonCreator
    public AccountSource(@JacksonInject("context") Context context,
                     @JsonProperty(value = "id",              required = true) String id,
                     @JsonProperty(value = "cashFlowId",      required = true) String cashFlowId,
                     @JsonProperty(value = "payees",          required = true) List<String> payees,
                     @JsonProperty(value = "payors",          required = true) List<String> payors
                     ) throws IllegalArgumentException {
        super(context, id, cashFlowId,
                context.getByIds(Entity.class, payees),
                context.getByIds(Entity.class, payors));
    }

    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar calendar) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}