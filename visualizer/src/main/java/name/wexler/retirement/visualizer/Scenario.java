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

package name.wexler.retirement.visualizer;


import com.fasterxml.jackson.annotation.*;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowEstimator.CASH_ESTIMATE_PASS;
import name.wexler.retirement.visualizer.Entity.Category;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.CashFlowEstimator.Liability;
import name.wexler.retirement.visualizer.Entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

/**
 * Created by mwexler on 6/28/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "id", "assumptions", "name", "cashFlowSources"})
public class Scenario extends Entity {
    private final String name;
    private final Assumptions _assumptions;
    private static final String scenariosPath = "scenarios.json";
    private AccountReader accountReader;
    private final List<CashFlowEstimator> _cashFlowEstimators;

    @JsonIgnore
    private final CashFlowCalendar calendar;

    static public @NotNull
    List<Scenario> readScenarios(@NotNull Context context) throws IOException {
        Assumptions.readAssumptions(context);
        Category.readCategories(context);
        return context.fromJSONFileList(Scenario[].class, scenariosPath);
    }

    @JsonCreator
    public Scenario(
            @JacksonInject("context") Context context,
             @JacksonInject("accountReader") AccountReader accountReader,
             @JsonProperty(value = "id", required = true) String id,
             @JsonProperty(value = "name", required = true) String name,
             @JsonProperty(value = "cashFlowSources", required = true) String[] cashFlowEstimators,
             @JsonProperty(value = "assets", required = true) String[] assets,
             @JsonProperty(value = "liabilities", required = true) String[] liabilities,
             @JsonProperty(value = "accounts", required = true) String[] accounts,
             @JsonProperty(value = "assumptions", required = true) Assumptions assumptions) throws DuplicateEntityException {
        super(context, id, Scenario.class);
        this.name = name;
        this.accountReader = accountReader;
        this._assumptions = assumptions;

        _cashFlowEstimators = new ArrayList<>();
        setCashFlowEstimators(context, cashFlowEstimators);
        calendar = new CashFlowCalendar(this, assumptions);
        List<Asset> assetList = setAssetIds(context, assets);
        calendar.addCashFlowInstances(getHistoricalCashFlowInstances());
        calendar.addCashFlowInstances(getEstimatedAssetValues(assetList));
        calendar.addBudgets(getBudgets());
        setCurrentBalances();
        Iterator<CASH_ESTIMATE_PASS> passIterator =
                CASH_ESTIMATE_PASS.BASE_CASH_FLOWS.iterator();
        while (passIterator.hasNext()) {
            CASH_ESTIMATE_PASS pass = passIterator.next();
            List<CashFlowInstance> cashFlowInstances = getFutureCashFlowInstances(calendar, pass);
            calendar.addCashFlowInstances(cashFlowInstances);
        }
        calendar.computeBalances();
        setLiabilityIds(context, liabilities);
        setAccountIds(context, accounts);
        context.put(Scenario.class, id, this);
    }

    private @NotNull
    List<CashFlowInstance> getEstimatedAssetValues(@NotNull List<Asset> assets) {
        final List<CashFlowInstance> cashFlowInstances = new ArrayList<>();
        assets.
                stream().
                forEach(asset -> {
                    List<CashFlowInstance> estimatorInstances = asset.getEstimatedAssetValues(getAssumptions());
                    cashFlowInstances.addAll(estimatorInstances);
                });
        return cashFlowInstances;
    }

    private @NotNull
    List<CashFlowInstance> getFutureCashFlowInstances
            (
                    @NotNull CashFlowCalendar calendar,
                    @NotNull CASH_ESTIMATE_PASS pass
            ) {
        final List<CashFlowInstance> cashFlowInstances = new ArrayList<>();
        _cashFlowEstimators.
                stream().
                filter(estimator -> estimator.getPass() == pass).
                forEach(cashFlowSource -> {
                    String id = cashFlowSource.getId();
                    List<CashFlowInstance> estimatorInstances = cashFlowSource.getEstimatedFutureCashFlows(calendar);
                    cashFlowInstances.addAll(estimatorInstances);
                });
        return cashFlowInstances;
    }

    private @NotNull
    List<CashFlowInstance> getHistoricalCashFlowInstances() {
        try {
            return accountReader.readCashFlowInstances(getContext());
        } catch (IOException ioe) {
            throw new RuntimeException("Can't getHistoricalCashFlowInstances", ioe);
        }
    }

    private @NotNull
    List<Budget> getBudgets() {
        try {
            return accountReader.readBudgets(getContext());
        } catch (IOException ioe) {
            throw new RuntimeException("Can't getBudgets", ioe);
        }
    }

    private @NotNull
    void setCurrentBalances() {
        try {
            accountReader.getAccountBalances(getContext());
        } catch (IOException ioe) {
            throw new RuntimeException("Can't setCurrentBalances", ioe);
        }
    }


    @JsonProperty(value = "cashFlowSources")
    private void setCashFlowEstimators(@JacksonInject("context") Context context,
                                       @JsonProperty(value = "cashFlowSources", required = true) String[] cashFlowSourceIds) {
        for (String cashFlowSourceId : cashFlowSourceIds) {
            CashFlowEstimator cashFlowEstimator = context.getById(CashFlowEstimator.class, cashFlowSourceId);
            if (cashFlowEstimator != null)
                _cashFlowEstimators.add(cashFlowEstimator);
            else
                System.err.println("Can't find CashFlowEstimator: " + cashFlowSourceId);
        }
    }

    @JsonProperty(value = "accounts")
    private void setAccountIds(@JacksonInject("context") Context context,
                                      @JsonProperty(value = "accounts", required = true) String[] accountIds) {
        List<AssetAccount> accounts = new ArrayList<>(accountIds.length);
        for (String accountId : accountIds) {
            accounts.add(context.getById(Asset.class, accountId));
        }
    }

    @JsonProperty(value = "assets")
    private List<Asset> setAssetIds(@JacksonInject("context") Context context,
                             @JsonProperty(value = "assets", required = true) String[] assetIds) {
        List<Asset> assets = new ArrayList<>(assetIds.length);
        for (String assetId : assetIds) {
            Asset asset = context.getById(Asset.class, assetId);
            if (asset != null)
                assets.add(asset);
            else
                System.err.println("Can't find asset: " + assetId);
        }
        calendar.addAssets(assets);
        return assets;
    }

    @JsonProperty(value = "liabilities")
    private void setLiabilityIds(@JacksonInject("context") Context context,
                             @JsonProperty(value = "liabilities", required = true) String[] liabilityIds) {
        List<Liability> liabilities = new ArrayList<>(liabilityIds.length);
        for (String id : liabilityIds) {
            Liability liability = context.getById(Liability.class, id);
            if (liability != null)
                liabilities.add(liability);
            else
                System.out.println("Liability not found: " + id);
        }
        calendar.addLiabilities(liabilities);
    }

    @JsonProperty(value = "assumptions")
    public @NotNull
    Assumptions getAssumptions() {
        return _assumptions;
    }

    public @NotNull
    String getName() {
        return name;
    }

    @JsonIgnore @NotNull
    public CashFlowCalendar getCashFlowCalendar() {
        return calendar;
    }
}
