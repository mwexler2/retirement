/*
* Retirement calculator - Allows one to run various retirement scenarios and see how much money you have to retire on.
*
* Copyright (C) 2016  Michael C. Wexler

* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*
* I can be reached at mike.wexler@gmail.com.
*
*/

package name.wexler.retirement.visualizer.Asset;

import com.fasterxml.jackson.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Scenario;
import name.wexler.retirement.visualizer.CashFlowFrequency.Balance;
import name.wexler.retirement.visualizer.Entity.Entity;

/**
 * Created by mwexler on 7/9/16.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RealProperty.class, name = "real-property"),
        @JsonSubTypes.Type(value = AssetAccount.class, name = "account")
})
public abstract class Asset extends Entity {
    private final Context context;


    private final List<Entity> _owners;

    private static final String assetsPath = "assets.json";

    static public void readAssets(Context context) throws IOException {
        context.fromJSONFileList(Asset[].class, assetsPath);
    }

    @JsonCreator
    protected Asset(@JacksonInject("context") Context context,
                @JsonProperty("id") String id,
                @JsonProperty("owners") List<String> ownerIds) throws DuplicateEntityException {
        super(context, id, Asset.class);
        this.context = context;
        this._owners = context.getByIds(Entity.class, ownerIds);
        context.put(Asset.class, id, this);
    }

    abstract public String getName();

    @JsonIgnore
    public Context getContext() {
        return context;
    }

    public List<Entity> getOwners() {
        return _owners;
    }

    @JsonProperty(value = "owners")
    public List<String> getOwnerIds() {
        List<String> result = new ArrayList<>(_owners.size());
        for (Entity owner : _owners) {
            String id = owner.getId();
            result.add(id);
        }
        return result;
    }

    public List<Balance> linearGrowth(Balance base, long period, BigDecimal rate) {
        List<Balance> balances = new ArrayList<>();

        for (int i = 1; i <= period; ++i) {
            LocalDate calculatedBalanceDate = base.getBalanceDate().plusMonths(i);
            BigDecimal multiplier = BigDecimal.ONE.add(rate.multiply(BigDecimal.valueOf(i)));
            BigDecimal calculatedBalanceAmount = base.getValue().multiply(multiplier);
            Balance calculatedBalance = new CashBalance(calculatedBalanceDate, calculatedBalanceAmount);
            balances.add(calculatedBalance);
        }
        return balances;
    }
}
