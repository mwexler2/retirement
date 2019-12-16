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
import name.wexler.retirement.visualizer.CashFlowInstance.Account;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.CashFlowEstimator.Liability;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by mwexler on 6/28/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "id", "assumptions", "name", "cashFlowSources"})
public class Scenario extends Entity {
    private final String name;
    private Assumptions _assumptions;
    private static final String scenariosPath = "scenarios.json";
    private List<CashFlowEstimator> _cashFlowEstimators;

    @JsonIgnore
    private final CashFlowCalendar calendar;

    static public List<Scenario> readScenarios(Context context) throws IOException {
        Assumptions.readAssumptions(context);
        return context.fromJSONFileList(Scenario[].class, scenariosPath);
    }

    @JsonCreator
    public Scenario(@JacksonInject("context") Context context,
             @JsonProperty("id") String id,
             @JsonProperty("name") String name,
             @JsonProperty("cashFlowSources") String[] cashFlowEstimators,
             @JsonProperty("assets") String[] assets,
             @JsonProperty("liabilities") String[] liabilities,
             @JsonProperty("accounts") String[] accounts,
             @JsonProperty("assumptions") Assumptions assumptions) throws DuplicateEntityException {
        super(context, id, Scenario.class);
        this.name = name;
        this._assumptions = assumptions;
        _cashFlowEstimators = new ArrayList<>();

        setCashFlowEstimators(context, cashFlowEstimators);
        calendar = new CashFlowCalendar(this, assumptions);
        calendar.addCashFlowInstances(getHistoricalCashFlowInstances());
        setCurrentBalances();
        for (int pass = 1; pass <= 3; ++pass) {
            List<CashFlowInstance> cashFlowInstances = getFutureCashFlowInstances(calendar, pass);
            calendar.addCashFlowInstances(cashFlowInstances);
        }
        calendar.computeBalances();
        setAssetIds(context, assets);
        setLiabilityIds(context, liabilities);
        setAccountIds(context, accounts);
        context.put(Scenario.class, id, this);
    }

    private List<CashFlowInstance> getFutureCashFlowInstances(CashFlowCalendar calendar, int pass) {
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

    private List<CashFlowInstance> getHistoricalCashFlowInstances() {
        try {
            AccountReader accountReader = new AccountReader(getContext());
            return accountReader.readCashFlowInstances(getContext());
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
        return null;
    }

    private void setCurrentBalances() {
        try {
            AccountReader accountReader = new AccountReader(getContext());
            Map<String, BigDecimal> currentBalances = accountReader.getAccountBalances(getContext());
            for (Map.Entry<String, BigDecimal> entry: currentBalances.entrySet()) {
                if (entry.getValue() == null) {
                    System.err.println("account: " + entry.getKey() + " has no current balance.");
                    continue;
                }
                CashFlowSink sink = getContext().getById(Account.class, entry.getKey());
                if (sink == null) {
                    System.err.println("account: " + entry.getKey() + " has no corresponding account");
                    continue;
                }
                sink.setRunningTotal(entry.getValue());
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }


    @JsonProperty(value = "cashFlowSources")
    private void setCashFlowEstimators(@JacksonInject("context") Context context,
                                       @JsonProperty(value = "cashFlowSources", required = true) String[] cashFlowSourceIds) {
        for (String cashFlowSourceId : cashFlowSourceIds) {
            _cashFlowEstimators.add(context.getById(CashFlowEstimator.class, cashFlowSourceId));
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
    private void setAssetIds(@JacksonInject("context") Context context,
                             @JsonProperty(value = "assets", required = true) String[] assetIds) {
        List<Asset> assets = new ArrayList<>(assetIds.length);
        for (String assetId : assetIds) {
            assets.add(context.getById(Asset.class, assetId));
        }
        calendar.addAssets(assets);
    }

    @JsonProperty(value = "liabilities")
    private void setLiabilityIds(@JacksonInject("context") Context context,
                             @JsonProperty(value = "liabilities", required = true) String[] liabilityIds) {
        List<Liability> liabilities = new ArrayList<>(liabilityIds.length);
        for (String id : liabilityIds) {
            liabilities.add(context.getById(Liability.class, id));
        }
        calendar.addLiabilities(liabilities);
    }

    @JsonProperty(value = "assumptions")
    public Assumptions getAssumptions() {
        return _assumptions;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public List<Integer> getYears() {
        return calendar.getYears();
    }

    public CashFlowCalendar getCashFlowCalendar() {
        return calendar;
    }
}
