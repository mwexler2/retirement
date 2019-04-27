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

package name.wexler.retirement.visualizer.CashFlowSource;

import com.fasterxml.jackson.annotation.*;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowCalendar;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id", "source", "job", "salary", "bonusPct", "bonusDay" })
public class BonusAnnualPct extends Bonus {
    @JsonIdentityReference(alwaysAsId = true)
    private Salary salary;
    private final BigDecimal bonusPct;

    public BonusAnnualPct(@JacksonInject("context") Context context,
                          @JsonProperty(value = "id", required = true) String id,
                          @JsonProperty(value = "job", required = true) String jobId,
                          @JsonProperty(value = "salary", required = true) String salaryId,
                          @JsonProperty(value = "bonusPct", required = true) BigDecimal bonusPCT,
                          @JsonProperty(value = "cashFlow", required = true) String cashFlowId)
    throws DuplicateEntityException {
        super(context, id, jobId, cashFlowId);
        this.setSalaryId(context, salaryId);
        this.bonusPct = bonusPCT;
    }

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar cashFlowCalendar) {
        BigDecimal annualAmount = salary.getBaseAnnualSalary().multiply(bonusPct);
        return getCashFlow().getCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
            return new CashFlowInstance(this, accrualStart, accrualEnd, cashFlowDate, annualAmount, balance);
                });
    }

    @JsonProperty(value = "salary")
    public String getSalaryId() {
        return salary.getId();
    }

    private void setSalaryId(@JacksonInject("context") Context context,
                             @JsonProperty(value = "salary", required = true) String salaryId) {
        this.salary = context.getById(CashFlowSource.class, salaryId);
    }

    public BigDecimal getBonusPct() {
        return bonusPct;
    }
}
