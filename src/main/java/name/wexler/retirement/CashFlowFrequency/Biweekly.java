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

package name.wexler.retirement.CashFlowFrequency;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.Context;
import name.wexler.retirement.JSON.JSONDateDeserialize;
import name.wexler.retirement.JSON.JSONDateSerialize;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Created by mwexler on 7/9/16.
 */
public class Biweekly extends CashFlowFrequency {
    private DayOfWeek dayOfWeek;
    private static final int twoWeeks = 2 * 7;
    private static final int weeksInBiweek = 2;
    private static final int periodsPeryear = 26;

    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate firstPeriodStart;


    public Biweekly(@JacksonInject("context") Context context,
                    @JsonProperty(value = "id", required = true) String id,
                    @JsonProperty(value = "firstPeriodStart", required = true) LocalDate firstPeriodStart,
                    @JsonProperty(value = "accrueStart", required = true) LocalDate accrueStart,
                    @JsonProperty(value = "accrueEnd", required = true) LocalDate accrueEnd,
                    @JsonProperty(value = "firstPaymentDate", required = true) LocalDate firstPaymentDate,
                    @JsonProperty("apportionmentPeriod") ApportionmentPeriod apportionmentPeriod)
    throws Exception
    {
        super(context, id, accrueStart, accrueEnd, firstPaymentDate, apportionmentPeriod);
        this.firstPeriodStart = firstPeriodStart;
        this.dayOfWeek = firstPaymentDate.getDayOfWeek();
    }

    public LocalDate getFirstPeriodStart() {
        return this.firstPeriodStart;
    }

    @JsonIgnore
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    @JsonIgnore
    @Override
    public List<CashFlowPeriod> getCashFlowPeriods() {
        ArrayList<CashFlowPeriod> result = new ArrayList<>();

        int paymentOffset = getFirstPeriodStart().until(getFirstPaymentDate()).getDays();
        BigDecimal totalDays = BigDecimal.valueOf(getAccrueStart().until(getAccrueEnd(), ChronoUnit.DAYS));
        for (LocalDate thisPeriodStart = getFirstPeriodStart(); thisPeriodStart.isBefore(getAccrueEnd());
                thisPeriodStart = thisPeriodStart.plusWeeks(weeksInBiweek)) {
            LocalDate thisAccrualStart = thisPeriodStart;
            if (thisAccrualStart.isBefore(getAccrueStart()))
                thisAccrualStart = getAccrueStart();
            // accrual end = 2 weeks after accrual start, but the last day counts as part of the period so we need to subtract 1.
            LocalDate thisAccrualEnd = thisPeriodStart.plusWeeks(2).plusDays(-1);
            if (thisAccrualEnd.isAfter(getAccrueEnd()))
                thisAccrualEnd = getAccrueEnd();
            LocalDate cashFlowDate = thisPeriodStart.plusDays(paymentOffset);
            BigDecimal daysInPeriod = BigDecimal.valueOf(twoWeeks);
            // Add one to the number of days to make it inclusive
            BigDecimal accrualDays = BigDecimal.valueOf(thisAccrualStart.until(thisAccrualEnd, DAYS) + 1);
            BigDecimal portion = accrualDays.divide(daysInPeriod, 10, BigDecimal.ROUND_HALF_UP);
            portion = portion.divide(BigDecimal.valueOf(periodsPeryear), 10, BigDecimal.ROUND_HALF_UP);
            result.add(new CashFlowPeriod(thisAccrualStart, thisAccrualEnd, cashFlowDate, portion));
        }

        return result;
    }

    @JsonIgnore
    public ChronoUnit getChronoUnit() {
        return ChronoUnit.DAYS;
    }

    @JsonIgnore
    public BigDecimal getUnitMultiplier() {
        return BigDecimal.valueOf(14);
    }

    @JsonIgnore
    public BigDecimal unitsPerYear() {
        return BigDecimal.valueOf(26);
    }
}
