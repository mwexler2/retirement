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

package name.wexler.retirement;


import com.fasterxml.jackson.annotation.*;
import name.wexler.retirement.CashFlow.CashFlowCalendar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mwexler on 6/28/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "assumptions", "name", "incomeSources", "expenseSources"})
public class Scenario {
    private Assumptions assumptions;
    private final String name;

    @JsonIgnore
    private final CashFlowCalendar calendar;

    @JsonCreator
    Scenario(@JacksonInject("context") Context context,
             @JsonProperty("name") String name,
             @JsonProperty("incomeSources") String[] incomeSources,
             @JsonProperty("expenseSources") String[] expenseSources) {
        this.name = name;
        calendar = new CashFlowCalendar();
        setIncomeSourceIds(context, incomeSources);
        setExpenseSourceIds(context, expenseSources);
    }


    @JsonProperty(value = "incomeSources")
    private void setIncomeSourceIds(@JacksonInject("context") Context context,
                                    @JsonProperty(value = "incomeSources", required = true) String[] incomeSourceIds) {
        List<IncomeSource> incomeSources = new ArrayList<>(incomeSourceIds.length);
        for (String incomeSourceId : incomeSourceIds) {
            incomeSources.add(context.getById(IncomeSource.class, incomeSourceId));
        }
        calendar.addIncomeSources(incomeSources);
    }

    @JsonProperty(value = "incomeSources")
    public String[] getIncomeSourceIds() {
        Map<String, String> nameAndIds = calendar.getIncomeCashFlowNameAndIds();
        String[] result = nameAndIds.keySet().toArray(new String[nameAndIds.size()]);
        return result;
    }

    @JsonProperty(value = "expenseSources")
    private void setExpenseSourceIds(@JacksonInject("context") Context context,
                                     @JsonProperty(value = "expenseSources", required = true) String[] expenseSourceIds) {
        List<ExpenseSource> expenseSources = new ArrayList<>(expenseSourceIds.length);
        for (String expenseSourceId : expenseSourceIds) {
            expenseSources.add(context.getById(ExpenseSource.class, expenseSourceId));
        }
        calendar.addExpenseSources(expenseSources);
    }

    @JsonProperty(value = "expenseSources")
    public String[] getExpenseSourceIds() {
        Map<String, String> nameAndIds = calendar.getExpenseCashFlowNameAndIds();
        String[] result = nameAndIds.keySet().toArray(new String[nameAndIds.size()]);
        return result;
    }

    @JsonIgnore
    public String getIncomeSourceName(String incomeSourceId) {
        return calendar.getIncomeSourceName(incomeSourceId);
    }

    @JsonIgnore
    public String getExpenseSourceName(String expenseSourceId) {
        return calendar.getExpenseSourceName(expenseSourceId);
    }


    @JsonProperty(value = "assets")
    private void setAssetIds(@JacksonInject("context") Context context,
                             @JsonProperty(value = "assets", required = true) String[] assetIds) {
        List<Asset> assets = new ArrayList<>(assetIds.length);
        for (String assetId : assetIds) {
            assets.add(context.getById(IncomeSource.class, assetId));
        }
        calendar.addAssets(assets);
    }

    @JsonProperty(value = "assets")
    public String[] getAssetIds() {
        Map<String, String> nameAndIds = calendar.getAssetNameAndIds();
        String[] result = nameAndIds.keySet().toArray(new String[nameAndIds.size()]);
        return result;
    }

    @JsonProperty(value = "liabilities")
    public String[] getLiabilityIds() {
        String[] liabilityIds = new String[0];
        return liabilityIds;
    }

    @JsonIgnore
    public String getAssetName(String assetId) {
        return calendar.getAssetName(assetId);
    }

    @JsonIgnore
    public BigDecimal getAnnualIncome(String incomeSourceId, int year ) {
        return calendar.getAnnualIncome(incomeSourceId, year);
    }

    @JsonIgnore
    public BigDecimal getAssetValue(String assetId, int year ) {
        return calendar.getAssetValue(assetId, year);
    }

    @JsonIgnore
    public BigDecimal getAnnualExpense(String expenseSourceId, int year) {
        return calendar.getAnnualExpense(expenseSourceId, year);
    }

    @JsonIgnore
    public BigDecimal getAnnualIncome(int year) {
        return calendar.getAnnualIncome(year);
    }

    @JsonIgnore
    public BigDecimal getLiabilityAmount(int year) {
        return BigDecimal.ZERO;
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
    public BigDecimal getAnnualExpense(int year) {
        return calendar.getAnnualExpense(year);
    }

    @JsonIgnore
    public BigDecimal getNetIncome(int year) {
        BigDecimal grossIncome = calendar.getAnnualIncome(year);
        BigDecimal expenses = calendar.getAnnualExpense(year);
        BigDecimal netIncome = grossIncome.subtract(expenses);
        return netIncome;
    }

    public Assumptions getAssumptions() {
        return assumptions;
    }

    public void setAssumptions(Assumptions assumptions) {
        this.assumptions = assumptions;
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
}
