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

package name.wexler.retirement.visualizer.CashFlowFrequency;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({ "type", "id", "accrueStart", "accrueEnd", "firstPaymentDate", "firstPaymentDayOfMonth", "secondPaymentDayOfMonth" })
public class SemiMonthly extends Monthly {
    private int firstPaymentDayOfMonth;
    private int secondPaymentDayOfMonth;
    private static final int middleOfMonth = 15;
    private static final int periodsPerYear = 24;

    public SemiMonthly(@JacksonInject("context") Context context,
                       @JsonProperty(value = "id", required = true) String id,
                       @JsonProperty("accrueStart") LocalDate accrueStart,
                       @JsonProperty("accrueEnd") LocalDate accrueEnd,
                       @JsonProperty("firstPaymentDate") LocalDate firstPaymentDate,
                       @JsonProperty(value = "firstPaymentDayOfMonth", required = true) int firstDayOfMonth,
                       @JsonProperty(value = "secondPaymentDayOfMonth", required = true) int secondDayOfMonth,
                       @JsonProperty("apportionmentPeriod") ApportionmentPeriod apportionmentPeriod)
    throws Exception
    {
        super(context, id, accrueStart, accrueEnd, firstPaymentDate, apportionmentPeriod);
        this.firstPaymentDayOfMonth = firstDayOfMonth;
        this.secondPaymentDayOfMonth = secondDayOfMonth;
    }

    public int getFirstPaymentDayOfMonth() { return firstPaymentDayOfMonth; }

    public int getSecondPaymentDayOfMonth() {
        return secondPaymentDayOfMonth;
    }

    @JsonIgnore
    @Override
    public List<CashFlowPeriod> getCashFlowPeriods() {
        ArrayList<CashFlowPeriod> result = new ArrayList<>();
        YearMonth startYearMonth = YearMonth.of(getAccrueStart().getYear(), getAccrueStart().getMonth());
        YearMonth endYearMonth = YearMonth.of(getAccrueEnd().getYear(), getAccrueEnd().getMonth());
        YearMonth firstPaymentYearMonth = YearMonth.of(getFirstPaymentDate().getYear(), getFirstPaymentDate().getMonth());
        for (YearMonth thisYearMonth = startYearMonth; !thisYearMonth.isAfter(endYearMonth); thisYearMonth = thisYearMonth.plusMonths(1)) {
            LocalDate thisAccrueStart = LocalDate.of(thisYearMonth.getYear(), thisYearMonth.getMonth(), 1);
            if (thisAccrueStart.isBefore(getAccrueStart()))
                thisAccrueStart = getAccrueStart();
            LocalDate thisAccrueEnd = thisAccrueStart.withDayOfMonth(thisAccrueStart.lengthOfMonth());
            if (thisAccrueEnd.isAfter(getAccrueEnd()))
                thisAccrueEnd = getAccrueEnd();
            LocalDate firstCashFlowDate = thisAccrueStart.withDayOfMonth(firstPaymentDayOfMonth);

            LocalDate firstAccrueStart = thisAccrueStart;
            LocalDate firstAccrueEnd = thisAccrueEnd;
            if (firstAccrueEnd.getDayOfMonth() > this.middleOfMonth)
                firstAccrueEnd = firstAccrueEnd.withDayOfMonth(this.middleOfMonth);
            if (firstCashFlowDate.isBefore(firstAccrueEnd))
                firstCashFlowDate = firstCashFlowDate.plusMonths(1);
            if (!firstAccrueEnd.isBefore(this.getAccrueStart())) {
                BigDecimal daysInPeriod = BigDecimal.valueOf(this.middleOfMonth);
                // Add 1 day to make it inclusive
                BigDecimal accrualDays = BigDecimal.valueOf(firstAccrueStart.until(firstAccrueEnd.plusDays(1), DAYS));
                BigDecimal portion = accrualDays.divide(daysInPeriod, 4, RoundingMode.HALF_UP);
                portion = portion.divide(BigDecimal.valueOf(periodsPerYear), 10, RoundingMode.HALF_UP);
                result.add(new CashFlowPeriod(firstAccrueStart, firstAccrueEnd, firstCashFlowDate, portion));
            }

            LocalDate secondAccrueStart = thisAccrueStart.withDayOfMonth(this.middleOfMonth + 1);
            LocalDate secondAccrueEnd = thisAccrueEnd;
            LocalDate secondCashFlowDate = thisAccrueStart.withDayOfMonth(secondPaymentDayOfMonth);
            if (secondCashFlowDate.isBefore(secondAccrueEnd))
                secondCashFlowDate = secondCashFlowDate.plusMonths(1);
            if (secondAccrueStart.isBefore(this.getAccrueEnd())) {
                BigDecimal daysInPeriod = BigDecimal.valueOf(secondAccrueEnd.lengthOfMonth() - this.middleOfMonth);
                BigDecimal accrualDays = BigDecimal.valueOf(secondAccrueStart.until(secondAccrueEnd.plusDays(1), DAYS));
                BigDecimal portion = accrualDays.divide(daysInPeriod, 4, RoundingMode.HALF_UP);
                portion = portion.divide(BigDecimal.valueOf(periodsPerYear), 10, RoundingMode.HALF_UP);
                result.add(new CashFlowPeriod(secondAccrueStart, secondAccrueEnd, secondCashFlowDate, portion));
            }
        }

        return result;
    }

    @JsonIgnore
    public ChronoUnit getChronoUnit() {
        return ChronoUnit.MONTHS;
    }

    @JsonIgnore
    public BigDecimal getUnitMultiplier() {
        return BigDecimal.valueOf(0.5);
    }

    @JsonIgnore
    public BigDecimal unitsPerYear() {
        return BigDecimal.valueOf(24);
    }
}
