package name.wexler.retirement.CashFlow;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.*;
import name.wexler.retirement.JSON.JSONDateDeserialize;
import name.wexler.retirement.JSON.JSONDateSerialize;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by mwexler on 11/29/16.
 */
public class ShareBalance implements Balance {
    @JsonDeserialize(using= JSONDateDeserialize.class)
    @JsonSerialize(using= JSONDateSerialize.class)
    private final LocalDate balanceDate;
    private final BigDecimal shares;
    private final BigDecimal sharePrice;
    private final Security security;

    public ShareBalance(@JacksonInject("context") Context context,
                        @JsonDeserialize(using=JSONDateDeserialize.class) @JsonProperty(value = "date", required = true) LocalDate balanceDate,
                        @JsonProperty(value = "shares", defaultValue = "0.00") BigDecimal shares,
                        @JsonProperty(value = "sharePrice", required = true) BigDecimal sharePrice,
                        @JsonProperty(value = "security", required = true) String securityId) {
        this.balanceDate = balanceDate;
        this.shares = shares;
        this.sharePrice = sharePrice;
        this.security = context.getById(Security.class, securityId);
    }

    public BigDecimal getValue() {
        return shares.multiply(sharePrice);
    }

    public BigDecimal getSharePrice() {
        return sharePrice;
    }

    public BigDecimal getShares() { return shares; }

    public String getId() {
        return security.getId();
    }

    public LocalDate getBalanceDate() {
        return balanceDate;
    }


    @Override
    public String toString() {
        String result = balanceDate.toString() + ": " + getValue();
        return result;
    }
}
