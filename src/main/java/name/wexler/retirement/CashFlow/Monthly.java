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
import name.wexler.retirement.Context;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwexler on 7/9/16.
 */
public class Monthly extends CashFlowFrequency {

    @JsonIgnore
    final
    BigDecimal monthsPerYear = BigDecimal.valueOf(12);

    public Monthly(@JacksonInject("context") Context context,
                   @JsonProperty(value = "id", required = true) String id,
                   @JsonProperty("accrueStart") LocalDate accrueStart,
                   @JsonProperty("accrueEnd") LocalDate accrueEnd,
                   @JsonProperty("firstPaymentDate") LocalDate firstPaymentDate)
    throws Exception
    {
        super(context, id, accrueStart, accrueEnd, firstPaymentDate);

    }

    private final BigDecimal periodsPerYear = BigDecimal.valueOf(12);
    @JsonIgnore
    @Override
    public BigDecimal getPeriodsPerYear() { return periodsPerYear; }

    @JsonIgnore
    public LocalDate getFirstPeriodStart() {
        return getAccrueStart().withDayOfMonth(1);
    }


    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar calendar, SingleCashFlowGenerator generator) {
        ArrayList<CashFlowInstance> result = new ArrayList<>();

        YearMonth startYearMonth = YearMonth.of(getAccrueStart().getYear(), getAccrueStart().getMonth());
        YearMonth endYearMonth = YearMonth.of(getAccrueEnd().getYear(), getAccrueEnd().getMonth());
        YearMonth firstPaymentYearMonth = YearMonth.of(getFirstPaymentDate().getYear(), getFirstPaymentDate().getMonth());
        long paymentMonthOffset = startYearMonth.until(firstPaymentYearMonth, ChronoUnit.MONTHS);
        for (YearMonth thisYearMonth = startYearMonth; !thisYearMonth.isAfter(endYearMonth); thisYearMonth = thisYearMonth.plusMonths(1)) {
            LocalDate thisAccrueStart = LocalDate.of(thisYearMonth.getYear(), thisYearMonth.getMonth(), 1);
            if (thisAccrueStart.isBefore(getAccrueStart()))
                thisAccrueStart = getAccrueStart();
            LocalDate thisAccrueEnd = thisAccrueStart.withDayOfMonth(thisAccrueStart.lengthOfMonth());
            if (thisAccrueEnd.isAfter(getAccrueEnd()))
                thisAccrueEnd = getAccrueEnd();
            LocalDate cashFlowDate = thisAccrueStart.plusMonths(paymentMonthOffset).withDayOfMonth(getFirstPaymentDate().getDayOfMonth());
            BigDecimal singleFlowAmount = generator.getSingleCashFlowAmount(calendar, thisAccrueStart, thisAccrueEnd);
            result.add(new CashFlowInstance(thisAccrueStart, thisAccrueEnd, cashFlowDate, singleFlowAmount));
        }

        return result;
    }
}
