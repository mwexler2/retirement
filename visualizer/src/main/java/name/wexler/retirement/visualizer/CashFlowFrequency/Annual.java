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
import java.time.Month;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Created by mwexler on 7/9/16.
 */
public class Annual extends CashFlowFrequency {
    private final MonthDay monthDay;
    private final int firstYear;
    private final int lastYear;

    public Annual(@JacksonInject("context") Context context,
                  @JsonProperty(value = "id", required = true) String id,
                  @JsonProperty("accrueStart") LocalDate accrueStart,
                  @JsonProperty("accrueEnd") LocalDate accrueEnd,
                  @JsonProperty("firstPaymentDate") LocalDate firstPaymentDate,
                  @JsonProperty("apportionmentPeriod") ApportionmentPeriod apportionmentPeriod)
    throws DuplicateEntityException
    {
        super(context, id, accrueStart, accrueEnd, firstPaymentDate, apportionmentPeriod);
        this.monthDay = MonthDay.of(firstPaymentDate.getMonth(), firstPaymentDate.getDayOfMonth());
        this.firstYear = firstPaymentDate.getYear();
        this.lastYear = accrueEnd.getYear();
    }

    @JsonIgnore
    public LocalDate getFirstPeriodStart() {
        return getAccrueStart().withDayOfYear(1);
    }

    @JsonIgnore
    @Override
    public List<CashFlowPeriod> getCashFlowPeriods() {
        ArrayList<CashFlowPeriod> result = new ArrayList<>();

        int accrualStartYear = getAccrueStart().getYear();
        int accrualEndYear = getAccrueEnd().getYear();
        int paymentYearOffset = getFirstPaymentDate().getYear() - accrualStartYear;
        BigDecimal totalDays = BigDecimal.valueOf(getAccrueStart().until(getAccrueEnd(), DAYS));
        for (int thisYear = getAccrueStart().getYear(); thisYear <= getAccrueEnd().getYear(); ++thisYear) {
            LocalDate thisAccrualStart = getAccrueStart();
            if (thisYear > accrualStartYear)
                thisAccrualStart = LocalDate.of(thisYear, Month.JANUARY, 1);

            LocalDate thisAccrualEnd = getAccrueEnd();
            if (thisYear < accrualEndYear)
                thisAccrualEnd = LocalDate.of(thisYear, Month.DECEMBER, 31);

            LocalDate cashFlowDate = LocalDate.of(thisYear + paymentYearOffset, getFirstPaymentDate().getMonth(), getFirstPaymentDate().getDayOfMonth());

            BigDecimal daysInPeriod = BigDecimal.valueOf(thisAccrualEnd.lengthOfYear());
            BigDecimal accrualDays = BigDecimal.valueOf(thisAccrualStart.until(thisAccrualEnd, DAYS));
            BigDecimal portion = accrualDays.divide(daysInPeriod, 2, RoundingMode.HALF_UP);
            result.add(new CashFlowPeriod(thisAccrualStart, thisAccrualEnd, cashFlowDate, portion));
        }

        return result;
    }

    @JsonIgnore
    public ChronoUnit getChronoUnit() {
        return ChronoUnit.YEARS;
    }

    @JsonIgnore
    public BigDecimal getUnitMultiplier() {
        return BigDecimal.valueOf(1);
    }

    @JsonIgnore
    public BigDecimal unitsPerYear() {
        return BigDecimal.valueOf(1);
    }
}
