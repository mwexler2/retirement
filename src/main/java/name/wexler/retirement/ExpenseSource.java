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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import name.wexler.retirement.CashFlow.CashFlowSource;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Created by mwexler on 7/9/16.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Debt.class, name = "debt")
         })
public abstract class ExpenseSource {
    private CashFlowSource source;

    public ExpenseSource() {

    }

    public ExpenseSource(CashFlowSource source) {
        this.source = source;
    }

    abstract public String getName();

    public  BigDecimal getMonthlyCashFlow(YearMonth yearMonth, BigDecimal annualAmount) {
        return source.getMonthlyCashFlow(yearMonth, annualAmount);
    }

    public BigDecimal getMonthlyCashFlow(BigDecimal annualAmount) {

        return source.getMonthlyCashFlow(annualAmount);
    }

    public BigDecimal getAnnualCashFlow(int year, BigDecimal annualAmount) {
        return source.getAnnualCashFlow(year, annualAmount);
    }

    public abstract BigDecimal getAnnualCashFlow(int year);

    public BigDecimal getAnnualCashFlow(BigDecimal annualAmount) {
        return source.getAnnualCashFlow(annualAmount);
    }

}
