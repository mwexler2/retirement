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

package name.wexler.retirement.visualizer.CashFlowFrequency;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.visualizer.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Created by mwexler on 7/9/16.
 */
public class Monthly extends CashFlowFrequency {
    @JsonIgnore
    private static final int periodsPerYear = 12;

    public Monthly(@JacksonInject("context") Context context,
                   @JsonProperty(value = "id", required = true) String id,
                   @JsonProperty("accrueStart") LocalDate accrueStart,
                   @JsonProperty("accrueEnd") LocalDate accrueEnd,
                   @JsonProperty("firstPaymentDate") LocalDate firstPaymentDate,
                   @JsonProperty("apportionmentPeriod") ApportionmentPeriod apportionmentPeriod)
    throws Exception
    {
        super(context, id, accrueStart, accrueEnd, firstPaymentDate, apportionmentPeriod);

    }

    @JsonIgnore
    public LocalDate getFirstPeriodStart() {
        return getAccrueStart().withDayOfMonth(1);
    }


    @JsonIgnore
    @Override
    public List<CashFlowPeriod> getCashFlowPeriods() {
        ArrayList<CashFlowPeriod> result = new ArrayList<>();

        YearMonth startYearMonth = YearMonth.of(getAccrueStart().getYear(), getAccrueStart().getMonth());
        YearMonth endYearMonth = YearMonth.of(getAccrueEnd().getYear(), getAccrueEnd().getMonth());
        YearMonth firstPaymentYearMonth = YearMonth.of(getFirstPaymentDate().getYear(), getFirstPaymentDate().getMonth());
        BigDecimal totalDays = BigDecimal.valueOf(getAccrueStart().until(getAccrueEnd(), DAYS));
        long paymentMonthOffset = startYearMonth.until(firstPaymentYearMonth, ChronoUnit.MONTHS);
        for (YearMonth thisYearMonth = startYearMonth; !thisYearMonth.isAfter(endYearMonth); thisYearMonth = thisYearMonth.plusMonths(1)) {
            LocalDate thisAccrueStart = LocalDate.of(thisYearMonth.getYear(), thisYearMonth.getMonth(), 1);
            if (thisAccrueStart.isBefore(getAccrueStart()))
                thisAccrueStart = getAccrueStart();
            LocalDate thisAccrueEnd = thisAccrueStart.withDayOfMonth(thisAccrueStart.lengthOfMonth());
            if (thisAccrueEnd.isAfter(getAccrueEnd()))
                thisAccrueEnd = getAccrueEnd();
            LocalDate cashFlowMonth = thisAccrueStart.plusMonths(paymentMonthOffset);
            // Need to make sure that if the day of month of the first payment is greater than the last day of month in the accrual period, we just use the end
            // of the month instead so we don't generate an invalid date like November 31.
            int cashFlowDay = Integer.min(getFirstPaymentDate().getDayOfMonth(), cashFlowMonth.lengthOfMonth());
            LocalDate cashFlowDate = cashFlowMonth.withDayOfMonth(cashFlowDay);
            BigDecimal daysInPeriod = BigDecimal.valueOf(thisYearMonth.lengthOfMonth());
            BigDecimal accrualDays = BigDecimal.valueOf(thisAccrueStart.until(thisAccrueEnd, DAYS)+1);
            BigDecimal portion = accrualDays.divide(daysInPeriod, 4, RoundingMode.HALF_UP);
            portion = portion.divide(BigDecimal.valueOf(periodsPerYear), 10, RoundingMode.HALF_UP);
            result.add(new CashFlowPeriod(thisAccrueStart, thisAccrueEnd, cashFlowDate, portion));
        }

        return result;
    }

    @JsonIgnore
    public ChronoUnit getChronoUnit() {
        return ChronoUnit.MONTHS;
    }

    @JsonIgnore
    public BigDecimal getUnitMultiplier() {
        return BigDecimal.valueOf(1);
    }

    @JsonIgnore
    public BigDecimal unitsPerYear() {
        return BigDecimal.valueOf(12);
    }
}
