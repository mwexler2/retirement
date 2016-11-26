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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by mwexler on 6/28/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "assumptions", "name", "incomeSources", "expenseSources", "numYears" })
public class Scenario {
    private Assumptions assumptions;
    private String name;

    @JsonIgnore
    private IncomeSource[] incomeSources;

    @JsonIgnore
    private ExpenseSource[] expenseSources;

    @JsonIgnore
    private int[] years;


    @JsonCreator
    Scenario(@JacksonInject("context") Context context,
             @JsonProperty("name") String name,
             @JsonProperty("incomeSources") String[] incomeSources,
             @JsonProperty("expenseSources") String[] expenseSources) {
        this.name = name;
        this.setIncomeSourceIds(context, incomeSources);
        this.setExpenseSourceIds(context, expenseSources);
        int startYear = 2016;
        this.years = new int[51];
        for (int i = 0; i <= 50; ++i) {
            years[i] = startYear + i;
        }
    }

    @JsonIgnore
    public ExpenseSource[] getExpenseSources() {
        return expenseSources;
    }

    @JsonIgnore
    public void setExpenseSources(ExpenseSource[] expenseSources) {
        this.expenseSources = expenseSources;
    }

    @JsonProperty(value = "incomeSources")
    public void setIncomeSourceIds(@JacksonInject("context") Context context,
                                @JsonProperty(value="incomeSources", required=true) String[] incomeSourceIds) {
        this.incomeSources = new IncomeSource[incomeSourceIds.length];
        for (int i = 0; i < incomeSourceIds.length; ++i) {
            incomeSources[i] = context.<IncomeSource>getById(IncomeSource.class, incomeSourceIds[i]);
        }
    }

    @JsonProperty(value = "incomeSources")
    public String[] getIncomeSourceIds() {
        String[] result = new String[incomeSources.length];
        for (int i = 0; i < incomeSources.length; ++i)
            result[i] = incomeSources[i].getId();
        return result;
    }

    @JsonProperty(value = "expenseSources")
    public void setExpenseSourceIds(@JacksonInject("context") Context context,
                                   @JsonProperty(value="expenseSources", required=true) String[] expenseSourceIds) {
        this.expenseSources = new ExpenseSource[expenseSourceIds.length];
        for (int i = 0; i < expenseSourceIds.length; ++i) {
            expenseSources[i] = context.<ExpenseSource>getById(ExpenseSource.class, expenseSourceIds[i]);
        }
    }

    @JsonProperty(value = "expenseSources")
    public String[] getExpenseSourceIds() {
        String[] result = new String[expenseSources.length];
        for (int i = 0; i < expenseSources.length; ++i)
            result[i] = expenseSources[i].getId();
        return result;
    }

    @JsonIgnore
    public BigDecimal getAnnualIncome(int year) {
        BigDecimal income = BigDecimal.ZERO;

        for (IncomeSource is : this.incomeSources) {
            income = income.add(is.getAnnualCashFlow(year));
        }
        return income;
    }

    @JsonIgnore
    public BigDecimal getAnnualExpense(int year) {
        BigDecimal expense = BigDecimal.ZERO;

        for (ExpenseSource es : this.expenseSources) {
            expense = expense.add(es.getAnnualCashFlow(year));
        }
        return expense;
    }

    public Assumptions getAssumptions() {
        return assumptions;
    }

    public void setAssumptions(Assumptions assumptions) {
        this.assumptions = assumptions;
    }

    @JsonIgnore
    public IncomeSource[] getIncomeSources() {
        return incomeSources;
    }

    @JsonIgnore
    public void setIncomeSources(IncomeSource[] incomeSources) {
        this.incomeSources = incomeSources;
    }

    public String getName() {
        return name;
    }

    public int[] getYears() {
        return this.years;
    }

    public int getNumYears() {
        return this.years.length;
    }

    public String toJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        ObjectWriter writer = mapper.writer();
        String result = writer.writeValueAsString(this);
        return result;
    }

    static public Scenario fromJSON(Context context,
                                  String json) throws Exception {
        ObjectMapper mapper = context.getObjectMapper();
        ObjectWriter writer = mapper.writer();
        Scenario result = (Scenario) mapper.readValue(json, Scenario.class);
        return result;
    }

    static public Scenario[] fromJSONFile(Context context, String filePath) throws IOException {
        File entityFile = new File(filePath);
        ObjectMapper incomeSourceMapper = context.getObjectMapper();
        Scenario[] result = incomeSourceMapper.readValue(entityFile, Scenario[].class);
        return result;
    }
}
