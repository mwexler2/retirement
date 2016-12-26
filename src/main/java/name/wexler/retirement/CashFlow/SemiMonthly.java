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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import name.wexler.retirement.Context;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwexler on 7/9/16.
 */
@JsonPropertyOrder({ "type", "id", "accrueStart", "accrueEnd", "firstPaymentDate", "firstDayOfMonth", "secondDayOfMonth" })
public class SemiMonthly extends Monthly {
    private int firstDayOfMonth;
    private int secondDayOfMonth;

    public SemiMonthly(@JacksonInject("context") Context context,
                       @JsonProperty(value = "id", required = true) String id,
                       @JsonProperty("accrueStart") LocalDate accrueStart,
                       @JsonProperty("accrueEnd") LocalDate accrueEnd,
                       @JsonProperty("firstPaymentDate") LocalDate firstPaymentDate,
                       @JsonProperty(value = "firstDayOfMonth", required = true) int firstDayOfMonth,
                       @JsonProperty(value = "secondDayOfMonth", required = true) int secondDayOfMonth)
    throws Exception
    {
        super(context, id, accrueStart, accrueEnd, firstPaymentDate);
        this.firstDayOfMonth = firstDayOfMonth;
        this.secondDayOfMonth = secondDayOfMonth;
    }

    public int getFirstDayOfMonth() {
        return firstDayOfMonth;
    }

    public int getSecondDayOfMonth() {
        return secondDayOfMonth;
    }

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(BigDecimal annualAmount) {
        ArrayList<CashFlowInstance> result = new ArrayList<>();

        return result;
    }
}
