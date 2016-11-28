/*
*
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

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.Context;
import name.wexler.retirement.JSONMonthDayDeserialize;
import name.wexler.retirement.JSONMonthDaySerialize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;

/**
 * Created by mwexler on 7/9/16.
 */
public class Annual extends CashFlowSource {
    private MonthDay monthDay;
    private int firstYear;
    private int lastYear;

    public Annual(@JacksonInject("context") Context context,
                  @JsonProperty(value = "id", required = true) String id,
                  @JsonProperty("accrueStart") LocalDate accrueStart,
                  @JsonProperty("accrueEnd") LocalDate accrueEnd,
                  @JsonProperty("firstPaymentDate") LocalDate firstPaymentDate)
    throws Exception
    {
        super(context, id, accrueStart, accrueEnd, firstPaymentDate);
        this.monthDay = MonthDay.of(firstPaymentDate.getMonth(), firstPaymentDate.getDayOfMonth());
        this.firstYear = firstPaymentDate.getYear();
        this.lastYear = accrueEnd.getYear();
    }

    public BigDecimal getMonthlyCashFlow(YearMonth yearMonth, BigDecimal annualAmount) {
        if (yearMonth.getYear() >= firstYear &&
                yearMonth.getYear() <= lastYear &&
                monthDay.getMonth() == yearMonth.getMonth()) {
            return annualAmount;
        }
        return  BigDecimal.ZERO;
    }

    public BigDecimal getMonthlyCashFlow(BigDecimal annualAmount) {
        return getMonthlyCashFlow(YearMonth.now(), annualAmount);
    }

    public BigDecimal getAnnualCashFlow(int year, BigDecimal annualAmount) {
        if (year >= firstYear && year <= lastYear) {
            return annualAmount;
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getAnnualCashFlow(BigDecimal annualAmount) {

        return getAnnualCashFlow(LocalDate.now().getYear(), annualAmount);
    }

    @JsonIgnore
    public MonthDay getMonthDay() {
        return monthDay;
    }

    @JsonIgnore
    public int getFirstYear() {
        return firstYear;
    }

    @JsonIgnore
    public int getLastYear() {
        return lastYear;
    }
}
