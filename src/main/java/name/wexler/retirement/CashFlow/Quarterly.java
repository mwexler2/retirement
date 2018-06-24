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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Created by mwexler on 7/9/16.
 */
public class Quarterly extends CashFlowFrequency {
    private static final int periodsPerYear = 4;

    public Quarterly(@JacksonInject("context") Context context,
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
        for (YearMonth thisYearMonth = startYearMonth; !thisYearMonth.isAfter(endYearMonth); thisYearMonth = thisYearMonth.plusMonths(3)) {
            LocalDate thisAccrueStart = LocalDate.of(thisYearMonth.getYear(), thisYearMonth.getMonth(), 1);
            if (thisAccrueStart.isBefore(getAccrueStart()))
                thisAccrueStart = getAccrueStart();
            YearMonth thisAccrueEndMonth = thisYearMonth.plusMonths(2);
            LocalDate thisAccrueEnd = LocalDate.of(thisAccrueEndMonth.getYear(), thisAccrueEndMonth.getMonth(), thisAccrueEndMonth.lengthOfMonth());
            if (thisAccrueEnd.isAfter(getAccrueEnd()))
                thisAccrueEnd = getAccrueEnd();
            LocalDate cashFlowDate = thisAccrueStart.plusMonths(paymentMonthOffset).withDayOfMonth(getFirstPaymentDate().getDayOfMonth());
            LocalDate quarterStartDate = LocalDate.of(thisYearMonth.getYear(), thisYearMonth.getMonth(), 1);
            LocalDate quarterEndDate = LocalDate.of(thisAccrueEndMonth.getYear(), thisAccrueEndMonth.getMonth(), thisAccrueEndMonth.lengthOfMonth());
            BigDecimal daysInPeriod = BigDecimal.valueOf(quarterStartDate.until(quarterEndDate, DAYS));
            BigDecimal accrualDays = BigDecimal.valueOf(thisAccrueStart.until(thisAccrueEnd, DAYS));
            BigDecimal portion = accrualDays.divide(daysInPeriod, 2, BigDecimal.ROUND_HALF_UP);
            portion = portion.divide(BigDecimal.valueOf(periodsPerYear), 10, BigDecimal.ROUND_HALF_UP);
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
        return BigDecimal.valueOf(3);
    }

    @JsonIgnore
    public BigDecimal unitsPerYear() {
        return BigDecimal.valueOf(4);
    }
}
