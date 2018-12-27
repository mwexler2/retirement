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

package name.wexler.retirement.Asset;

import com.fasterxml.jackson.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import name.wexler.retirement.CashFlowFrequency.Balance;
import name.wexler.retirement.CashFlowFrequency.CashBalance;
import name.wexler.retirement.Context;
import name.wexler.retirement.Entity.Entity;

/**
 * Created by mwexler on 7/9/16.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RealProperty.class, name = "real-property"),
        @JsonSubTypes.Type(value = Account.class, name = "account")
})
public abstract class Asset {
    private final CashBalance _initialBalance;
    private final List<CashBalance> _interimBalances;
    private final Context context;


    private List<Entity> _owners;

    public String getId() {
        return _id;
    }

    private final String _id;
    private static final String assetsPath = "assets.json";

    static public List<Asset> readAssets(Context context) throws IOException {
        return context.fromJSONFileList(Asset[].class, assetsPath);
    }

    @JsonCreator
    protected Asset(@JacksonInject("context") Context context,
                @JsonProperty("id") String id,
                @JsonProperty("owners") List<String> ownerIds,
                    @JsonProperty("initialBalance") CashBalance initialBalance,
                    @JsonProperty("interimBalances") List<CashBalance> interimBalances) {
        this.context = context;
        this._id = id;
        this._owners = context.getByIds(Entity.class, ownerIds);
        this._initialBalance = initialBalance;
        interimBalances.sort(Comparator.comparing(Balance::getBalanceDate));
        _interimBalances = interimBalances;
        context.put(Asset.class, id, this);
    }

    abstract public String getName();

    @JsonIgnore
    public Context getContext() {
        return context;
    }

    @JsonIgnore
    public List<Balance> getBalances() {
        List<Balance> balances = new ArrayList<>();

        balances.add(getInitialBalance());
        balances.addAll(_interimBalances);

        return balances;
    }

    @JsonIgnore
    public List<Balance> getBalances(int year) {
        List<Balance> balances = new ArrayList<>();

        balances.add(getInitialBalance());
        balances.addAll(
                _interimBalances.stream()
                        .filter(balance -> year == balance.getBalanceDate().getYear())
                        .collect(Collectors.toList()));

        return balances;
    }

    public Balance getBalanceAtDate(LocalDate valueDate) {
        if (valueDate.isBefore(this.getStartDate())) {
            return new CashBalance(valueDate, BigDecimal.ZERO);
        }
        Balance recentBalance = _initialBalance;
        List<Balance> balances = getBalances();
        int i =  Collections.binarySearch(balances, new CashBalance(valueDate, BigDecimal.ZERO),
                Comparator.comparing(Balance::getBalanceDate));
        if (i >= 0) {
            recentBalance = balances.get(i);
        } else if (i < -1) {
            recentBalance = balances.get(-i - 2);
        }
        return recentBalance;
    }


    public Balance getInitialBalance() {
        return _initialBalance;
    }

    @JsonIgnore
    public BigDecimal getInitialBalanceAmount() {
        return _initialBalance.getValue();
    }

    @JsonIgnore
    public LocalDate getStartDate() {
        return _initialBalance.getBalanceDate();
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
