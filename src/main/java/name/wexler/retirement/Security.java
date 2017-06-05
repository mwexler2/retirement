package name.wexler.retirement;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by mwexler on 6/4/17.
 */
public class Security extends Asset {

    @JsonCreator
    public Security(@JacksonInject("context") Context context,
                    @JsonProperty("id") String id,
                    @JsonProperty("owner") Entity owner,
                    @JsonProperty("initialAssetValue") BigDecimal initialAssetValue,
                    @JsonProperty("initialAssetValueDate") LocalDate initialAssetValueDate) {
        super(context, id, owner, initialAssetValue, initialAssetValueDate);
    }

    public String getName() {
        return getId();
    }
}
