package name.wexler.retirement.visualizer.CashFlowInstance;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.CashFlowSource;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SecurityTransaction.class, name = "security")
})
public abstract class AssetTransaction extends CashFlowInstance {
    public AssetTransaction(boolean estimated,
                            CashFlowSource cashFlowSource, CashFlowSink cashFlowSink,
                            String itemType, String category,
                            LocalDate accrualStart, LocalDate accrualEnd, LocalDate cashFlowDate,
                            BigDecimal amount,
                            BigDecimal balance) {
        super(estimated, cashFlowSource, cashFlowSink, itemType, category, accrualStart, accrualEnd, cashFlowDate, amount, balance);
    }
}
