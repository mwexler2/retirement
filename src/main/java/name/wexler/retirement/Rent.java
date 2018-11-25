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
import name.wexler.retirement.CashFlow.Balance;
import name.wexler.retirement.CashFlow.CashFlowCalendar;
import name.wexler.retirement.CashFlow.CashFlowInstance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id", "source", "lessors", "lessees", "security", "startDate", "endDate", "paymentAmount" })
public class Rent extends CashFlowSource {
    private Asset security;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate startDate;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate endDate;
    private BigDecimal paymentAmount;
    private BigDecimal periodsPerYear = BigDecimal.valueOf(12);


    @JsonCreator
    public Rent(@JacksonInject("context") Context context,
                @JsonProperty(value = "id",              required = true) String id,
                @JsonProperty("lessee") List<String> lesseeIds,
                @JsonProperty("lessor") List<String> lessorIds,
                @JsonProperty("asset") Asset security,
                @JsonProperty(value = "startDate",       required=true) LocalDate startDate,
                @JsonProperty("endDate") LocalDate endDate,
                @JsonProperty(value = "paymentAmount",   required = true) BigDecimal paymentAmount,
                @JsonProperty(value = "source",          required = true) String sourceId) throws Exception {
        super(context, id, sourceId,
                context.getByIds(Entity.class, lesseeIds),
                context.getByIds(Entity.class, lessorIds));
        this.security = security;
        this.startDate = startDate;
        this.endDate = endDate;
        this.paymentAmount = paymentAmount;
        context.put(Rent.class, id, this);
    }


    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar cashFlowCalendar) {
        return getCashFlow().getCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getBalance();
                    return new CashFlowInstance(this, accrualStart, accrualEnd, cashFlowDate, paymentAmount, balance);
                });
    }

    @JsonIgnore
    @Override
    public String getName() {
        String result;
        List<Balance> interimBalances = new ArrayList<Balance>();

        if (security != null) {
            result = security.getName() + "(" + getlessor().getName() + ")";
        } else {
            result = getlessor().getName();
        }
        return result;
    }

    @JsonProperty(value = "source")
    public String getSourceId() {
        return this.getCashFlow().getId();
    }


    @JsonProperty(value = "security")
    public String getSecurityId() {
        return this.security.getId();
    }


    @JsonProperty(value = "lessor")
    public String getlessorId() {
        return getlessor().getId();
    }


    public Entity getlessor() {
        return getPayees().get(0);
    }


    @JsonProperty(value = "lessees")
    public List<String> getlesseeIds() {

        List<Entity> lessees = getlessees();
        List<String> result = new ArrayList<>(lessees.size());
        for (int i = 0; i < lessees.size(); ++i)
            result.add(lessees.get(i).getId());
        return result;
    }

    public List<Entity> getlessees() {
        return getPayers();
    }

    public LocalDate getEndDate() { return endDate; }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }
}
