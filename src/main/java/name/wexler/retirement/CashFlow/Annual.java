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
import name.wexler.retirement.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwexler on 7/9/16.
 */
public class Annual extends CashFlowType {
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

    private final BigDecimal periodsPerYear = BigDecimal.ONE;
    @JsonIgnore
    @Override
    public BigDecimal getPeriodsPerYear() { return periodsPerYear; }

    @JsonIgnore
    public LocalDate getFirstPeriodStart() {
        return getAccrueStart().withDayOfYear(1);
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

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar calendar, SingleCashFlowGenerator generator) {
        ArrayList<CashFlowInstance> result = new ArrayList<>();

        int accrualStartYear = getAccrueStart().getYear();
        int accrualEndYear = getAccrueEnd().getYear();
        int paymentYearOffset = getFirstPaymentDate().getYear() - accrualStartYear;
        for (int thisYear = getAccrueStart().getYear(); thisYear <= getAccrueEnd().getYear(); ++thisYear) {
            LocalDate thisAccrualStart = getAccrueStart();
            if (thisYear > accrualStartYear)
                thisAccrualStart = LocalDate.of(thisYear, Month.JANUARY, 1);

            LocalDate thisAccrualEnd = getAccrueEnd();
            if (thisYear < accrualEndYear)
                thisAccrualEnd = LocalDate.of(thisYear, Month.DECEMBER, 31);

            LocalDate cashFlowDate = LocalDate.of(thisYear + paymentYearOffset, getFirstPaymentDate().getMonth(), getFirstPaymentDate().getDayOfMonth());
            BigDecimal singleFlowAmount = generator.getSingleCashFlowAmount(calendar, thisAccrualStart, thisAccrualEnd);
            result.add(new CashFlowInstance(thisAccrualStart, thisAccrualEnd, cashFlowDate, singleFlowAmount));
        }

        return result;
    }
}
