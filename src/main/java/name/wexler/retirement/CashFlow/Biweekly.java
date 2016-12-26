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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwexler on 7/9/16.
 */
public class Biweekly extends CashFlowType {
    private DayOfWeek dayOfWeek;
    private static final int twoWeeks = 2 * 7;

    public Biweekly(@JacksonInject("context") Context context,
                    @JsonProperty(value = "id", required = true) String id,
                    @JsonProperty("accrueStart") LocalDate accrueStart,
                    @JsonProperty("accrueEnd") LocalDate accrueEnd,
                    @JsonProperty("firstPaymentDate") LocalDate firstPaymentDate)
    throws Exception
    {
        super(context, id, accrueStart, accrueEnd, firstPaymentDate);
        this.dayOfWeek = firstPaymentDate.getDayOfWeek();
    }

    public BigDecimal getMonthlyCashFlow(YearMonth yearMonth, BigDecimal annualAmount) {
        BigDecimal monthlyAmount = BigDecimal.ZERO;
        LocalDate monthStart = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1);
        LocalDate monthEnd = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), yearMonth.lengthOfMonth());
        long daysSinceFirstPayment = monthStart.toEpochDay() - getFirstPaymentDate().toEpochDay();
        if (daysSinceFirstPayment >= 0) {
            long biweeksSinceLastPayment = daysSinceFirstPayment/twoWeeks;
            LocalDate recentPaymentDate = getFirstPaymentDate().plusDays(biweeksSinceLastPayment * 14);
            while (!recentPaymentDate.isAfter(monthEnd)) {
                if (!recentPaymentDate.isBefore(monthStart)) {
                    monthlyAmount = annualAmount.divide(BigDecimal.valueOf(26.0), 2, BigDecimal.ROUND_HALF_UP);
                }
                recentPaymentDate = recentPaymentDate.plusDays(twoWeeks);
            }
        }
        return monthlyAmount;
    }

    @JsonIgnore
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(BigDecimal annualAmount) {
        ArrayList<CashFlowInstance> result = new ArrayList<>();

        return result;
    }
}
