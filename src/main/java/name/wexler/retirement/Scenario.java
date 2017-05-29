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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import name.wexler.retirement.CashFlow.CashFlowCalendar;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mwexler on 6/28/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "assumptions", "name", "incomeSources", "expenseSources"})
public class Scenario {
    private Assumptions assumptions;
    private String name;

    @JsonIgnore
    private CashFlowCalendar calendar;

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

    @JsonIgnore
    public CashFlowCalendar getCalendar() {
        return calendar;
    }

    @JsonProperty(value = "incomeSources")
    public void setIncomeSourceIds(@JacksonInject("context") Context context,
                                   @JsonProperty(value="incomeSources", required=true) String[] incomeSourceIds) {
        List<IncomeSource> incomeSources = new ArrayList<>(incomeSourceIds.length);
        for (int i = 0; i < incomeSourceIds.length; ++i) {
            incomeSources.add(context.<IncomeSource>getById(IncomeSource.class, incomeSourceIds[i]));
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
    public void setExpenseSourceIds(@JacksonInject("context") Context context,
                                    @JsonProperty(value="expenseSources", required=true) String[] expenseSourceIds) {
        List<ExpenseSource> expenseSources = new ArrayList<>(expenseSourceIds.length);
        for (int i = 0; i < expenseSourceIds.length; ++i) {
            expenseSources.add(context.<ExpenseSource>getById(ExpenseSource.class, expenseSourceIds[i]));
        }
        calendar.addExpenseSources(expenseSources);
    }

    @JsonProperty(value = "expenseSources")
    public String[] getExpenseSourceIds() {
        Map<String, String> nameAndIds = calendar.getExpenseCashFlowNameAndIds();
        String[] result = nameAndIds.keySet().toArray(new String[nameAndIds.size()]);
        return result;
    }

    /*
    @JsonIgnore
    public List<IncomeSource> getIncomeSources() {
        return calendar.getIncomeSources();
    }

    @JsonIgnore
    public List<ExpenseSource> getExpenseSources() {
        return calendar.getExpenseSources();
    }
    */

    @JsonIgnore
    public BigDecimal getAnnualIncome(int year) {
        return calendar.getAnnualIncome(year);
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
