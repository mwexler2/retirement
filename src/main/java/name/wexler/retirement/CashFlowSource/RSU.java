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

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.CashFlowFrequency.CashFlowCalendar;
import name.wexler.retirement.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.Context;
import name.wexler.retirement.Job;
import name.wexler.retirement.Security;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RSU extends CashFlowSource {
    @JsonIgnore
    private Job job;
    private Security security;
    private int totalShares;

    public RSU(@JacksonInject("context") Context context,
               @JsonProperty(value = "id", required = true) String id,
               @JsonProperty(value = "job", required = true) String jobId,
               @JsonProperty(value = "cashFlow", required = true) String cashFlowId,
               @JsonProperty(value = "security", required = true) String securityId,
               @JsonProperty(value = "totalShares", required = true) int totalShares)
            throws Exception {
        super(context, id, cashFlowId,
                Arrays.asList(((Job) context.getById(Job.class, jobId)).getEmployee()),
                Arrays.asList(((Job) context.getById(Job.class, jobId)).getEmployer()));
        this.setJobId(context, jobId);
        this.setSecurityId(context, securityId);
        this.totalShares = totalShares;
    }


    @JsonIgnore
    public String getName() {
        return job.getName() + " RSU";
    }

    @JsonProperty(value = "job")
    public String getJobId() {
        return job.getId();
    }

    public int getTotalShares() {
        return this.totalShares;
    }

    private void setJobId(@JacksonInject("context") Context context, @JsonProperty(value = "job", required = true) String jobId) {
        this.job = context.getById(Job.class, jobId);
    }

    private void setSecurityId(@JacksonInject("context") Context context, @JsonProperty(value = "security", required = true) String securityId) {
        this.security = context.getById(Security.class, securityId);
    }

    @JsonProperty("security")
    public String getSecurity() {
        return this.security.getName();
    }

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar cashFlowCalendar) {
        return getCashFlow().getCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
            BigDecimal sharePrice = this.security.getSharePriceAtDate(accrualEnd, calendar.getAssumptions());
            BigDecimal shares = BigDecimal.valueOf(totalShares).multiply(percent);
            BigDecimal amount = sharePrice.multiply(shares);
            BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getBalance();
            return new CashFlowInstance(this, accrualStart, accrualEnd, cashFlowDate, amount, balance);
        });
    }

}
