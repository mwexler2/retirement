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

package name.wexler.retirement;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.CashFlow.CashFlowSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.YearMonth;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id",  "job", "annualAmount", "cashFlow" })
public class BonusPeriodicFixed extends Bonus {
    @JsonIdentityReference(alwaysAsId = true)
    private BigDecimal annualAmount;

    public BonusPeriodicFixed(@JacksonInject("context") Context context,
                              @JsonProperty(value = "id", required = true) String id,
                              @JsonProperty(value = "job", required = true) String jobId,
                              @JsonProperty(value = "annualAmount", required = true) BigDecimal annualAmount,
                              @JsonProperty(value = "cashFlow", required = true) String cashFlowId)
            throws Exception {
        super(context, id, jobId, cashFlowId);
        this.annualAmount = annualAmount;
    }

    public BigDecimal getAnnualCashFlow(int year) {
        BigDecimal annualBonusAmount = BigDecimal.ZERO;
        for (Month month : Month.values()) {
            YearMonth yearMonth =  YearMonth.of(year, month);
            annualBonusAmount = annualBonusAmount.add(this.getMonthlyCashFlow(yearMonth, this.annualAmount));
        }
        return annualBonusAmount;
    }

    @Override
    public BigDecimal getMonthlyCashFlow(YearMonth yearMonth, BigDecimal annualAmount) {
        BigDecimal bonusAmount = this.getCashFlow().getMonthlyCashFlow(yearMonth, annualAmount);
        return bonusAmount;
    }

    public BigDecimal getAnnualAmount() {
        return annualAmount;
    }

    public void setAnnualAmount(BigDecimal bonusPct) {
        this.annualAmount = annualAmount;
    }

    @JsonProperty(value = "cashFlow")
    public String getCashFlowId() {
        return this.getCashFlow().getId();
    }
}
