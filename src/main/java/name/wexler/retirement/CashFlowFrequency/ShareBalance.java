package name.wexler.retirement.CashFlowFrequency;

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

    public ShareBalance(LocalDate balanceDate,
                        BigDecimal shares,
                        BigDecimal sharePrice,
                        Security security) {
        this.balanceDate = balanceDate;
        this.shares = shares;
        this.sharePrice = sharePrice;
        this.security = security;
    }

    public ShareBalance(@JacksonInject("context") Context context,
                        @JsonDeserialize(using=JSONDateDeserialize.class) @JsonProperty(value = "date", required = true) LocalDate balanceDate,
                        @JsonProperty(value = "shares", defaultValue = "0.00") BigDecimal shares,
                        @JsonProperty(value = "sharePrice", required = true) BigDecimal sharePrice,
                        @JsonProperty(value = "security", required = true) String securityId) {
        this(balanceDate, shares, sharePrice, context.getById(Security.class, securityId));
    }

    public BigDecimal getValue() {
        return shares.multiply(sharePrice).setScale(2, BigDecimal.ROUND_HALF_UP);
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

    public Security getSecurity() { return security; }

    public ShareBalance applyChange(ShareBalance change) {
        BigDecimal shares = this.shares.add(change.shares).setScale(2, BigDecimal.ROUND_HALF_UP);
        return new ShareBalance(this.getBalanceDate(), shares, change.sharePrice, change.getSecurity());
    }

    @Override
    public String toString() {
        String result = balanceDate.toString() + ": " + getValue();
        return result;
    }
}
