package name.wexler.retirement.visualizer.CashFlowSource;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowCalendar;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;

import java.util.List;
import java.util.stream.Collectors;

public class AccountSource extends CashFlowSource {
    private List<CashFlowInstance> cashFlowInstances = null;
    private String accountName = null;

    @JsonCreator
    public AccountSource(@JacksonInject("context") Context context,
                        @JsonProperty(value = "id",              required = true) String id,
                        @JsonProperty(value = "cashFlow",        required = true) String cashFlowId,
                        @JsonProperty(value = "payees",          required = true) List<String> payees,
                        @JsonProperty(value = "payors",          required = true) List<String> payors,
                         @JsonProperty(value = "accountName",    required = true) String accountName
                     ) throws IllegalArgumentException, DuplicateEntityException {
        super(context, id, cashFlowId,
                context.getByIds(Entity.class, payees),
                context.getByIds(Entity.class, payors));
        this.accountName = accountName;
    }

    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar calendar) {
        return cashFlowInstances;
    }

    public void setCashFlowInstances(List<CashFlowInstance> cashFlowInstances) {
        this.cashFlowInstances = cashFlowInstances;
    }

    @Override
    public String getName() {
        StringBuilder result = new StringBuilder();

        result.append(String.join(", ",
                this.getPayers().stream().map((payer) ->
                        payer.getId())
                        .collect(Collectors.toList())));
        result.append(" ");
        result.append(this.accountName);
        result.append(" (");
        result.append(String.join(", ",
                this.getPayees().stream().map((payee) ->
                        payee.getId())
                        .collect(Collectors.toList())));
        result.append(")");
        return result.toString();
    }
}