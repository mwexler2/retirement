package name.wexler.retirement.CashFlow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import name.wexler.retirement.Account;
import name.wexler.retirement.Context;
import name.wexler.retirement.RealProperty;

import java.math.BigDecimal;


public class SecurityTransaction extends AssetTransaction {
    ShareBalance change;

    public SecurityTransaction(
            @JsonProperty(value = "context", required = true) Context context,
            @JsonProperty(value = "account", required = true) Account account,
            @JsonProperty(value = "cashAmount", required=true) BigDecimal amount,
            @JsonProperty(value = "shareChange", required=true) ShareBalance shareChange) {
        super(
                account.getCashFlowSource(),
                shareChange.getBalanceDate(),
                shareChange.getBalanceDate(),
                shareChange.getBalanceDate(), amount, BigDecimal.ZERO);
        change = shareChange;
    }
}
