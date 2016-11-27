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

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.CashFlow.CashFlowSource;
import name.wexler.retirement.Context;
import name.wexler.retirement.JSONDateDeserialize;
import name.wexler.retirement.JSONDateSerialize;

import java.math.BigDecimal;
import java.time.*;

/**
 * Created by mwexler on 7/9/16.
 */
public class Monthly extends CashFlowSource {
    private int firstDayOfMonth;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate startDate;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate endDate;
    @JsonIgnore
    private BigDecimal monthsPerYear = BigDecimal.valueOf(12);

    public Monthly(@JacksonInject("context") Context context,
                   @JsonProperty(value = "id", required = true) String id,
                   @JsonProperty(value = "firstDayOfMonth", required = true) int firstDayOfMonth,
                   @JsonProperty(value = "startDate", required = true) LocalDate startDate,
                   @JsonProperty(value = "endDate", required = true) LocalDate endDate)
    throws Exception
    {
        super(context, id);
        this.firstDayOfMonth = firstDayOfMonth;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public BigDecimal getMonthlyCashFlow(YearMonth yearMonth, BigDecimal annualAmount) {
        BigDecimal monthlySalary = new BigDecimal(0.00);

        YearMonth startYearMonth = YearMonth.of(startDate.getYear(), startDate.getMonth());
        YearMonth endYearMonth = YearMonth.of(endDate.getYear(), endDate.getMonth());
        if (yearMonth.isBefore(startYearMonth) || yearMonth.isAfter(endYearMonth)) {
            monthlySalary = BigDecimal.ZERO;
        } else if (yearMonth.isAfter(startYearMonth) && yearMonth.isBefore(endYearMonth)) {
            monthlySalary = annualAmount.divide(monthsPerYear);
        } else {
            LocalDate firstDateInMonth = yearMonth.atDay(1);
            LocalDate lastDateInMonth = yearMonth.atEndOfMonth();
            if (yearMonth == startYearMonth) {
                firstDateInMonth = yearMonth.atDay(startDate.getDayOfMonth());
            } else if (yearMonth == endYearMonth) {
                lastDateInMonth = yearMonth.atDay(endDate.getDayOfMonth());
            }
            int days = firstDateInMonth.until(lastDateInMonth).getDays();
            monthlySalary = annualAmount.multiply(BigDecimal.valueOf(days).divide(BigDecimal.valueOf(startDate.lengthOfYear())));
        }
        return monthlySalary;
    }

    public BigDecimal getMonthlyCashFlow(BigDecimal annualAmount) {
        return getMonthlyCashFlow(YearMonth.now(), annualAmount);
    }

    public BigDecimal getAnnualCashFlow(int year, BigDecimal annualAmount) {
        BigDecimal result = BigDecimal.ZERO;

        for (Month m : Month.values())
            result = result.add(getMonthlyCashFlow(YearMonth.of(year, m), annualAmount));
        return result;
    }

    public BigDecimal getAnnualCashFlow(BigDecimal annualAmount) {
        return getAnnualCashFlow(LocalDate.now().getYear(), annualAmount);
    }


    public int getFirstDayOfMonth() {
        return firstDayOfMonth;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
