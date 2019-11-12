package name.wexler.retirement.visualizer.Expense;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowSource;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.io.IOException;

@JsonSubTypes({
        @JsonSubTypes.Type(value = Spending.class, name = "spending")})
abstract public class Expense extends Entity implements CashFlowSource {
    private Context context;
    private static final String expensesPath = "expenses.json";

    @JsonCreator
    protected Expense(@JacksonInject("context") Context context,
                    @JsonProperty("id") String id) throws DuplicateEntityException {
        super(context, id, Expense.class);
        this.context = context;
    }

    static public void readExpenses(Context context) throws IOException {
        context.fromJSONFileList(Expense[].class, expensesPath);
    }

    public String getName() {
        return getId();
    }

    public String getItemType() {
        return CashFlowCalendar.ITEM_TYPE.EXPENSE.name();
    }
}
