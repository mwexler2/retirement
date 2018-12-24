package name.wexler.retirement.CashFlow;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.Asset.Account;
import name.wexler.retirement.Context;

import java.math.BigDecimal;


public class SecurityTransaction extends AssetTransaction {
    ShareBalance change;
    Account account;

    public SecurityTransaction(
            @JacksonInject("context")  Context context,
            @JsonProperty(value = "account", required = true) String accountId,
            @JsonProperty(value = "cashAmount", required=true) BigDecimal amount,
            @JsonProperty(value = "shareChange", required=true) ShareBalance shareChange) {
        super(
                ((Account) context.getById(Account.class, accountId)).getCashFlowSource(),
                shareChange.getBalanceDate(),
                shareChange.getBalanceDate(),
                shareChange.getBalanceDate(), amount, BigDecimal.ZERO);
        change = shareChange;
    }
}
