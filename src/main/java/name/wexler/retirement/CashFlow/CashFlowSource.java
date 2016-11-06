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

package name.wexler.retirement.CashFlow;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import name.wexler.retirement.Debt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

/**
 * Created by mwexler on 7/9/16.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Annual.class, name = "annual"),
        @JsonSubTypes.Type(value = Biweekly.class, name = "biweekly"),
        @JsonSubTypes.Type(value = Monthly.class, name = "monthly"),
        @JsonSubTypes.Type(value = SemiMonthly.class, name = "semimonthly")
})
public abstract class CashFlowSource {
    private String id;

    public CashFlowSource(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public abstract BigDecimal getMonthlyCashFlow(YearMonth yearMonth, BigDecimal annualAmount);

    public BigDecimal getMonthlyCashFlow(BigDecimal annualAmount) {
        return getMonthlyCashFlow(YearMonth.now(), annualAmount);
    }

    public BigDecimal getAnnualCashFlow(int year, BigDecimal annualAmount) {
        BigDecimal result = new BigDecimal(0.00);

        for (Month m : Month.values())
            result = result.add(getMonthlyCashFlow(YearMonth.of(year, m), annualAmount));
        return result;
    }

    public BigDecimal getAnnualCashFlow(BigDecimal annualAmount) {
        return getAnnualCashFlow(LocalDate.now().getYear(), annualAmount);
    }
}
