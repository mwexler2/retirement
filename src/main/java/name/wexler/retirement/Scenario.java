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


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Created by mwexler on 6/28/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Scenario {
    private Assumptions assumptions;
    private IncomeSource[] incomeSources;
    private String name;
    private int[] years;

    public ExpenseSource[] getExpenseSources() {
        return expenseSources;
    }

    public void setExpenseSources(ExpenseSource[] expenseSources) {
        this.expenseSources = expenseSources;
    }

    private ExpenseSource[] expenseSources;

    Scenario() {
    }

    Scenario(String name, IncomeSource[] incomeSources, ExpenseSource[] expenseSources) {
        this.name = name;
        this.incomeSources = incomeSources;
        this.expenseSources = expenseSources;
        int startYear = 2016;
        this.years = new int[51];
        for (int i = 0; i <= 50; ++i) {
            years[i] = startYear + i;
        }
    }

    public BigDecimal getAnnualIncome(int year) {
        BigDecimal income = BigDecimal.ZERO;

        for (IncomeSource is : this.incomeSources) {
            income = income.add(is.getAnnualCashFlow(year));
        }
        return income;
    }

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

    public IncomeSource[] getIncomeSources() {
        return incomeSources;
    }

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
}
