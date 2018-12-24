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

package name.wexler.retirement.CashFlowSource;

import com.fasterxml.jackson.annotation.*;
import name.wexler.retirement.CashFlow.CashFlowCalendar;
import name.wexler.retirement.CashFlow.CashFlowInstance;
import name.wexler.retirement.Context;
import name.wexler.retirement.Job;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Salary extends CashFlowSource {
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonIgnore
    private Job job;
    private BigDecimal baseAnnualSalary;
    @JsonIgnore
    private ChronoUnit payUnit;
    private BigDecimal payUnitMultiplier;
    private BigDecimal amountPerUnit;

    public Salary(@JacksonInject("context") Context context,
                  @JsonProperty("id") String id,
                  @JsonProperty("job") String jobId,
                  @JsonProperty("cashFlow") String cashFlowId,
                  @JsonProperty("baseAnnualSalary") BigDecimal baseAnnualSalary) throws Exception {
        super(context, id, cashFlowId,
                Arrays.asList(((Job) context.getById(Job.class, jobId)).getEmployee()),
                Arrays.asList(((Job) context.getById(Job.class, jobId)).getEmployer()));
        this.setJobId(context, jobId);
        this.baseAnnualSalary = baseAnnualSalary;
        this.payUnit = this.getCashFlow().getChronoUnit();
        this.payUnitMultiplier = this.getCashFlow().getUnitMultiplier();
        this.amountPerUnit = baseAnnualSalary.divide(getCashFlow().unitsPerYear(), 2, RoundingMode.HALF_UP);
    }

    @JsonIgnore
    public String getName() {
        if (job == null)
            return "Unkonwn job salary";
        return job.getName() + " Salary";
    }

    @JsonProperty(value = "job")
    private void setJobId(@JacksonInject("context") Context context,
                          @JsonProperty(value = "job", required = true) String jobId) {
        this.job = context.getById(Job.class, jobId);
    }

    @JsonProperty(value = "job")
    public String getJobId() {
        return job.getId();
    }

    public Job getJob() {
        return job;
    }


    public void setJob(Job job) {
        this.job = job;
    }

    public BigDecimal getBaseAnnualSalary() {
        return baseAnnualSalary;
    }


    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar cashFlowCalendar) {
        return getCashFlow().getCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal amount = baseAnnualSalary.multiply(percent).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getBalance();
                    return new CashFlowInstance(this, accrualStart, accrualEnd, cashFlowDate, amount, balance);
                }
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Salary salary = (Salary) o;

        if (this.getId() != null ? !this.getId().equals(salary.getId()) : salary.getId() != null) return false;
        if (job != null ? !job.equals(salary.job) : salary.job != null) return false;
        return baseAnnualSalary != null ? baseAnnualSalary.equals(salary.baseAnnualSalary) : salary.baseAnnualSalary == null;

    }

    @Override
    public int hashCode() {
        int result = this.getId() != null ? this.getId().hashCode() : 0;
        result = 31 * result + (job != null ? job.hashCode() : 0);
        result = 31 * result + (baseAnnualSalary != null ? baseAnnualSalary.hashCode() : 0);
        return result;
    }
}
