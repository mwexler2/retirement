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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.CashFlowInstance.AssetTransaction;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
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
public abstract class Asset extends Entity implements CashFlowSource, CashFlowSink {
    private final List<Balance> _balances;
    private final Context context;
    private final List<Entity> _owners;
    private static final String assetsPath = "assets.json";
    private static final BigDecimal monthsInYear = BigDecimal.valueOf(12);
    private static final LocalDate endOfTime = LocalDate.of(2040, 1, 1);

    static public void readAssets(Context context) throws IOException {
        context.fromJSONFileList(Asset[].class, assetsPath);
    }

    @JsonCreator
    protected Asset(@JacksonInject("context") Context context,
                    @JsonProperty("id") String id,
                    @JsonProperty("owners") List<String> ownerIds, @JsonProperty("initialBalance") CashBalance initialBalance,
                    @JsonProperty("interimBalances") List<CashBalance> interimBalances) throws DuplicateEntityException {
        super(context, id, Asset.class);
        this.context = context;
        this._owners = context.getByIds(Entity.class, ownerIds);
        this._balances = new ArrayList<>();
        if (initialBalance != null)
            this._balances.add(initialBalance);
        interimBalances.sort(Comparator.comparing(Balance::getBalanceDate));
        this._balances.addAll(interimBalances);
        context.put(Asset.class, id, this);
    }

    abstract public String getName();

    @JsonIgnore
    public Context getContext() {
        return context;
    }

    @JsonIgnore
    public List<Balance> getBalances(Scenario scenario) {
        return this._balances;
    }

    @JsonIgnore
    public List<Balance> getBalances(Scenario scenario, int year) {
        List<Balance> balances;

        balances = _balances.stream()
                        .filter(balance -> year == balance.getBalanceDate().getYear())
                        .collect(Collectors.toList());

        return balances;
    }

    public Balance getBalanceAtDate(Scenario scenario, LocalDate valueDate) {
        if (valueDate.isBefore(this.getStartDate())) {
            return new CashBalance(valueDate, BigDecimal.ZERO);
        }
        Balance recentBalance = null;
        List<Balance> balances = getBalances(scenario);
        int i =  Collections.binarySearch(balances, new CashBalance(valueDate, BigDecimal.ZERO),
                Comparator.comparing(Balance::getBalanceDate));
        if (i >= 0) {
            recentBalance = balances.get(i);
        } else if (i < -1) {
            recentBalance = balances.get(-i - 2);
        }
        return recentBalance;
    }

    @JsonIgnore
    private LocalDate getStartDate() {
        if (_balances.size() > 0)
            return _balances.get(0).getBalanceDate();
        return LocalDate.ofEpochDay(0);
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
            BigDecimal multiplier = BigDecimal.ONE.add(rate.multiply(BigDecimal.valueOf(i)).divide(monthsInYear, RoundingMode.HALF_UP));
            BigDecimal calculatedBalanceAmount = base.getValue().multiply(multiplier);
            Balance calculatedBalance = new CashBalance(calculatedBalanceDate, calculatedBalanceAmount);
            balances.add(calculatedBalance);
        }
        return balances;
    }

    public List<CashFlowInstance> getEstimatedAssetValues(Assumptions assumptions) {
        List<CashFlowInstance> cashFlowInstances = new ArrayList<>();

        Balance prevBalance = null;
        List<Balance> intermediateBalances = new ArrayList<>();
        for (Balance balance : _balances) {
            if (prevBalance != null) {
                List<Balance> linearBalances = linearGrowth(
                        prevBalance,
                        prevBalance.getBalanceDate().until(balance.getBalanceDate(), ChronoUnit.MONTHS),
                        assumptions.getLongTermInvestmentReturn());
                intermediateBalances.addAll(linearBalances);
            }
            prevBalance = balance;
        }
        if (prevBalance != null)
            intermediateBalances.addAll(linearGrowth(prevBalance, prevBalance.getBalanceDate().until(endOfTime, ChronoUnit.MONTHS), assumptions.getLongTermInvestmentReturn()));
        _balances.addAll(intermediateBalances);

        _balances.stream().
                forEach(balance -> {
                    AssetTransaction instance = new AssetTransaction(
                            true, this, this, "interimBalance", "balance",
                            balance.getBalanceDate(),
                            balance.getBalanceDate(),
                            balance.getBalanceDate(), BigDecimal.ZERO, BigDecimal.ZERO) {
                    };
                    instance.setAssetBalance(balance.getValue());
                    cashFlowInstances.add(instance);
                });

        return cashFlowInstances;
    }

    @Override
    public String getItemType() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isOwner(Entity entity) {
        return _owners.contains(entity);
    }

    @Override
    public void updateRunningTotal(CashFlowInstance cashFlow, boolean negate) {
        return;
    }

    public void prependBalance(Balance balance) {
        this._balances.add(0, balance);
    }
}
