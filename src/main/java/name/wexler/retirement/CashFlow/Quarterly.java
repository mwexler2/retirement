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
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwexler on 7/9/16.
 */
public class Quarterly extends CashFlowType {

    @JsonIgnore
    final
    BigDecimal quartersPerYear = BigDecimal.valueOf(4);

    public Quarterly(@JacksonInject("context") Context context,
                     @JsonProperty(value = "id", required = true) String id,
                     @JsonProperty("accrueStart") LocalDate accrueStart,
                     @JsonProperty("accrueEnd") LocalDate accrueEnd,
                     @JsonProperty("firstPaymentDate") LocalDate firstPaymentDate)
    throws Exception
    {
        super(context, id, accrueStart, accrueEnd, firstPaymentDate);

    }

    @JsonIgnore
    public LocalDate getFirstPeriodStart() {
        return getAccrueStart().withDayOfMonth(1);
    }

    public BigDecimal getMonthlyCashFlow(YearMonth yearMonth, BigDecimal annualAmount) {
        int firstDayOfQuarter = getFirstPaymentDate().getDayOfMonth();

        BigDecimal quarterlyCashFlow = new BigDecimal(0.00);

        YearMonth startYearMonth = YearMonth.of(getAccrueStart().getYear(), getAccrueStart().getMonth());
        YearMonth endYearMonth = YearMonth.of(getAccrueEnd().getYear(), getAccrueEnd().getMonth());
        if (yearMonth.isBefore(startYearMonth) || yearMonth.isAfter(endYearMonth)) {
            quarterlyCashFlow = BigDecimal.ZERO;
        } else if (yearMonth.isAfter(startYearMonth) && yearMonth.isBefore(endYearMonth)) {
            quarterlyCashFlow = annualAmount.divide(quartersPerYear, 2, BigDecimal.ROUND_HALF_UP);
        } else {
            LocalDate firstDateInMonth = yearMonth.atDay(1);
            LocalDate lastDateInMonth = yearMonth.atEndOfMonth();
            if (yearMonth == startYearMonth) {
                firstDateInMonth = yearMonth.atDay(getAccrueStart().getDayOfMonth());
            } else if (yearMonth == endYearMonth) {
                lastDateInMonth = yearMonth.atDay(getAccrueEnd().getDayOfMonth());
            }
            int days = firstDateInMonth.until(lastDateInMonth).getDays();
            quarterlyCashFlow = annualAmount.multiply(BigDecimal.valueOf(days).divide(BigDecimal.valueOf(getAccrueStart().lengthOfYear()), 2, BigDecimal.ROUND_HALF_UP));
        }
        return quarterlyCashFlow;
    }

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(BigDecimal annualAmount) {
        ArrayList<CashFlowInstance> result = new ArrayList<>();

        YearMonth startYearMonth = YearMonth.of(getAccrueStart().getYear(), getAccrueStart().getMonth());
        YearMonth endYearMonth = YearMonth.of(getAccrueEnd().getYear(), getAccrueEnd().getMonth());
        YearMonth firstPaymentYearMonth = YearMonth.of(getFirstPaymentDate().getYear(), getFirstPaymentDate().getMonth());
        BigDecimal monthlyAmount = annualAmount.divide(quartersPerYear, 2, BigDecimal.ROUND_HALF_UP);
        long paymentMonthOffset = startYearMonth.until(firstPaymentYearMonth, ChronoUnit.MONTHS);
        for (YearMonth thisYearMonth = startYearMonth; !thisYearMonth.isAfter(endYearMonth); thisYearMonth = thisYearMonth.plusMonths(1)) {
            LocalDate thisAccrueStart = LocalDate.of(thisYearMonth.getYear(), thisYearMonth.getMonth(), 1);
            if (thisAccrueStart.isBefore(getAccrueStart()))
                thisAccrueStart = getAccrueStart();
            LocalDate thisAccrueEnd = thisAccrueStart.withDayOfMonth(thisAccrueStart.lengthOfMonth());
            if (thisAccrueEnd.isAfter(getAccrueEnd()))
                thisAccrueEnd = getAccrueEnd();
            LocalDate cashFlowDate = thisAccrueStart.plusMonths(paymentMonthOffset).withDayOfMonth(getFirstPaymentDate().getDayOfMonth());
            result.add(new CashFlowInstance(thisAccrueStart, thisAccrueEnd, cashFlowDate, monthlyAmount));
        }

        return result;
    }
}
