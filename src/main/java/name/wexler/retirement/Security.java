package name.wexler.retirement;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.CashFlow.Balance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by mwexler on 6/4/17.
 */
public class Security extends Asset {

    @JsonCreator
    public Security(@JacksonInject("context") Context context,
                    @JsonProperty("id") String id,
                    @JsonProperty("owner") String ownerId,
                    @JsonProperty("initialAssetValue") Balance initialAssetValue,
                    @JsonProperty("interimBalances") List<Balance> interimBalances) {
        super(context, id, ownerId, initialAssetValue, interimBalances);
    }

    public String getName() {
        return getId();
    }
}
