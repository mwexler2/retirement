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
import name.wexler.retirement.visualizer.Asset.Account;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowCalendar;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowFrequency.Balance;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowInstance.LiabilityCashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowSource.CashFlowSource;
import name.wexler.retirement.visualizer.CashFlowSource.Liability;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by mwexler on 6/28/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "id", "assumptions", "name", "cashFlowSources"})
public class Scenario extends Entity {
    private final String name;
    private Assumptions _assumptions;
    private static final String scenariosPath = "scenarios.json";

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
             @JsonProperty("cashFlowSources") String[] cashFlowSources,
             @JsonProperty("assets") String[] assets,
             @JsonProperty("liabilities") String[] liabilities,
             @JsonProperty("accounts") String[] accounts,
             @JsonProperty("assumptions") Assumptions assumptions) throws DuplicateEntityException {
        super(context, id, Scenario.class);
        this.name = name;
        this._assumptions = assumptions;
        calendar = new CashFlowCalendar(this, assumptions);
        setCashFlowSourceIds(context, cashFlowSources);
        setAssetIds(context, assets);
        setLiabilityIds(context, liabilities);
        setAccountIds(context, accounts);
        context.put(Scenario.class, id, this);
    }


    @JsonProperty(value = "cashFlowSources")
    private void setCashFlowSourceIds(@JacksonInject("context") Context context,
                                      @JsonProperty(value = "cashFlowSources", required = true) String[] cashFlowSourceIds) {
        List<CashFlowSource> cashFlowSources = new ArrayList<>(cashFlowSourceIds.length);
        for (String cashFlowSourceId : cashFlowSourceIds) {
            cashFlowSources.add(context.getById(CashFlowSource.class, cashFlowSourceId));
        }
        calendar.addCashFlowSources(cashFlowSources);
    }

    @JsonProperty(value = "cashFlowSources")
    public String[] getCashFlowSourceIds() {
        Map<String, String> nameAndIds = calendar.getCashFlowNameAndIds();
        String[] result = nameAndIds.keySet().toArray(new String[nameAndIds.size()]);
        return result;
    }

    @JsonProperty(value = "accounts")
    private void setAccountIds(@JacksonInject("context") Context context,
                                      @JsonProperty(value = "accounts", required = true) String[] accountIds) {
        List<Account> accounts = new ArrayList<>(accountIds.length);
        for (String accountId : accountIds) {
            accounts.add(context.getById(Asset.class, accountId));
        }
        calendar.addAccounts(accounts);
    }

    @JsonIgnore
    public String getCashFlowSourceName(String cashFlowSourceId) {
        return calendar.getCashFlowSourceName(cashFlowSourceId);
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

    @JsonProperty(value = "assets")
    public String[] getAssetIds() {
        Map<String, String> nameAndIds = calendar.getAssetNameAndIds();
        String[] result = nameAndIds.keySet().toArray(new String[nameAndIds.size()]);
        return result;
    }

    @JsonProperty(value = "liabilities")
    public String[] getLiabilityIds() {
        Map<String, String> nameAndIds = calendar.getLiabilityNameAndIds();
        String[] result = nameAndIds.keySet().toArray(new String[nameAndIds.size()]);
        return result;
    }

    @JsonProperty(value = "assumptions")
    public Assumptions getAssumptions() {
        return _assumptions;
    }

    @JsonIgnore
    public String getLiabilityName(String id) {
        return calendar.getLiabilityName(id);
    }

    @JsonIgnore
    public String getAssetName(String assetId) {
        return calendar.getAssetName(assetId);
    }

    @JsonIgnore
    public BigDecimal getAnnualIncome(String incomeSourceId, int year ) {
        return calendar.getAnnualCashFlow(incomeSourceId, year);
    }

    @JsonIgnore
    public BigDecimal getAssetValue(String assetId, int year ) {
        return calendar.getAssetValue(assetId, year);
    }

    @JsonIgnore
    public BigDecimal getLiabilityAmount(String id, int year ) {
        return calendar.getLiabilityAmount(id, year);
    }


    @JsonIgnore
    public BigDecimal getAnnualIncome(int year) {
        return calendar.getAnnualCashFlow(year);
    }

    @JsonIgnore
    public BigDecimal getLiabilityAmount(int year) {
        return calendar.getLiabilityAmount(year);
    }

    @JsonIgnore
    public BigDecimal getNetWorth(int year) {
        return getAssetValue(year).subtract(getLiabilityAmount(year));
    }

    @JsonIgnore
    public BigDecimal getAssetValue(int year) {
        return calendar.getAssetValue(year);
    }


    @JsonIgnore
    public BigDecimal getNetIncome(int year) {
        BigDecimal netIncome = calendar.getAnnualCashFlow(year);
        return netIncome;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public List<Integer> getYears() {
        return calendar.getYears();
    }

    @JsonIgnore
    public int getNumYears() {
        return calendar.getYears().size();
    }

    @JsonIgnore
    public List<CashFlowInstance> getCashFlows(String cashFlowId) {
        return calendar.getCashFlows(cashFlowId);
    }

    @JsonIgnore
    public Collection<Balance> getAssetValues(String assetId) { return calendar.getAssetValues(assetId); }

    @JsonIgnore
    public Collection<Balance> getAssetValues(String assetId, int year) {
        return calendar.getAssetValues(assetId, year);
    }

    @JsonIgnore
    public Collection<Balance> getLiabilityBalances(String liabilityId) { return calendar.getLiabilityBalances(liabilityId); }

    @JsonIgnore
    public List<LiabilityCashFlowInstance> getLiabilityCashFlowInstances(String liabilityId) {
        return calendar.getLiabilityCashFlowInstances(liabilityId);
    }

    @JsonIgnore
    public List<CashFlowInstance> getCashFlows(String cashFlowId, Integer year) {
        return calendar.getCashFlows(cashFlowId, year);
    }
}
