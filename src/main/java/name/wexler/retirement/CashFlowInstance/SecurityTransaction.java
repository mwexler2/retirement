package name.wexler.retirement.CashFlowInstance;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.Asset.Account;
import name.wexler.retirement.Asset.Asset;
import name.wexler.retirement.CashFlowFrequency.ShareBalance;
import name.wexler.retirement.Context;

import java.math.BigDecimal;


public class SecurityTransaction extends AssetTransaction {
    private final ShareBalance change;
    private final Account account;

    public SecurityTransaction(
            Context context,
            Account account,
            BigDecimal amount,
            ShareBalance shareChange) {
        super(
                account.getCashFlowSource(),
                shareChange.getBalanceDate(),
                shareChange.getBalanceDate(),
                shareChange.getBalanceDate(), amount, BigDecimal.ZERO);
        change = shareChange;
        this.account = account;
    }

    public SecurityTransaction(
            @JacksonInject("context")  Context context,
            @JsonProperty(value = "account", required = true) String accountId,
            @JsonProperty(value = "cashAmount", required=true) BigDecimal amount,
            @JsonProperty(value = "shareChange", required=true) ShareBalance shareChange) {
        this(context,
                (Account) context.getById(Asset.class, accountId),
                amount,
                shareChange);
    }

    public ShareBalance getChange() {
        return change;
    }

    public String toString() {
        return this.getCashFlowDate() + ": " + change.getSecurity().getId() + " - " +
                change.getShares() + " * $" + change.getSharePrice();
    }
}
