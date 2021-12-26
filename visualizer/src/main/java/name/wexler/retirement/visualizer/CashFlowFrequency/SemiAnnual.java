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
import name.wexler.retirement.visualizer.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Created by mwexler on 7/9/16.
 */
public class SemiAnnual extends CashFlowFrequency {
    private final MonthDay monthDay;
    private final int firstYear;
    private final int lastYear;
    private static final int periodsPerYear = 2;
    private final LocalDate firstAccrualEnd;

    public SemiAnnual(@JacksonInject("context") Context context,
                      @JsonProperty(value = "id", required = true) String id,
                      @JsonProperty("accrueStart") LocalDate accrueStart,
                      @JsonProperty("accrueEnd") LocalDate accrueEnd,
                      @JsonProperty("firstAccrualEnd") LocalDate firstAccrualEnd,
                      @JsonProperty("firstPaymentDate") LocalDate firstPaymentDate,
                      @JsonProperty("apportionmentPeriod") ApportionmentPeriod apportionmentPeriod)
    throws Exception
    {
        super(context, id, accrueStart, accrueEnd, firstPaymentDate, apportionmentPeriod);
        this.firstAccrualEnd = firstAccrualEnd;
        this.monthDay = MonthDay.of(firstPaymentDate.getMonth(), firstPaymentDate.getDayOfMonth());
        this.firstYear = firstPaymentDate.getYear();
        this.lastYear = accrueEnd.getYear();
    }

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
    public List<CashFlowPeriod> getCashFlowPeriods() {
        ArrayList<CashFlowPeriod> result = new ArrayList<>();

        int accrualStartYear = getAccrueStart().getYear();
        int accrualEndYear = getAccrueEnd().getYear();
        int paymentYearOffset = getFirstPaymentDate().getYear() - accrualStartYear;

        LocalDate thisAccrualStart = getAccrueStart();
        LocalDate thisAccrualEnd = this.firstAccrualEnd;
        LocalDate cashFlowDate = this.getFirstPaymentDate();
        while (thisAccrualEnd.isBefore(getAccrueEnd())) {
            YearMonth thisStartYearMonth = YearMonth.of(thisAccrualStart.getYear(), thisAccrualStart.getMonth());
            YearMonth thisEndYearMonth = YearMonth.of(thisAccrualEnd.getYear(), thisAccrualEnd.getMonth());
            BigDecimal daysInPeriod = BigDecimal.valueOf(thisStartYearMonth.until(thisEndYearMonth, DAYS));
            BigDecimal accrualDays = BigDecimal.valueOf(thisAccrualStart.until(thisAccrualEnd, DAYS));
            BigDecimal portion = accrualDays.divide(daysInPeriod, 4, RoundingMode.HALF_UP);
            portion = portion.divide(BigDecimal.valueOf(periodsPerYear), 10, RoundingMode.HALF_UP);
            result.add(new CashFlowPeriod(thisAccrualStart, thisAccrualEnd, cashFlowDate, portion));
            thisAccrualStart = thisAccrualEnd.plusDays(1);
            thisAccrualEnd = thisAccrualStart.plusMonths(6);
            cashFlowDate = cashFlowDate.plusMonths(6);
        }

        return result;
    }

    public ChronoUnit getChronoUnit() {
        return ChronoUnit.MONTHS;
    }

    public BigDecimal getUnitMultiplier() {
        return BigDecimal.valueOf(6);
    }

    public BigDecimal unitsPerYear() {
        return BigDecimal.valueOf(2);
    }
}
