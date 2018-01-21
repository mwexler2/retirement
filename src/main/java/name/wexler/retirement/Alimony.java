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
import name.wexler.retirement.CashFlow.CashFlowFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id", "startDate", "endDate", "payee", "payor", "cashFlow", "smithOstlerCashFlowType" })
public class Alimony extends CashFlowSource {
    @JsonIgnore
    private Entity payee;
    @JsonIgnore
    private Entity payor;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate startDate;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate endDate;
    private BigDecimal baseIncome;
    private BigDecimal baseAlimony;
    private BigDecimal smithOstlerRate;
    private BigDecimal maxAlimony;
    private CashFlowFrequency smithOstlerCashFlow;
    private final BigDecimal quartersPerYear = BigDecimal.valueOf(4);

    @JsonCreator
    public Alimony(@JacksonInject("context") Context context,
                   @JsonProperty("id") String id,
                   @JsonProperty("payee") String payeeId,
                   @JsonProperty("payor") String payorId,
                   @JsonProperty("startDate") LocalDate startDate,
                   @JsonProperty("endDate") LocalDate endDate,
                   @JsonProperty("baseIncome") BigDecimal baseIncome,
                   @JsonProperty("baseAlimony") BigDecimal baseAlimony,
                   @JsonProperty("smithOstlerRate") BigDecimal smithOstlerRate,
                   @JsonProperty("maxAlimony") BigDecimal maxAlimony,
                   @JsonProperty("baseCashFlow") String baseCashFlowId,
                   @JsonProperty("smithOstlerCashFlow") String smithOstlerCashFlowId
    ) throws Exception {
        super(context, id, baseCashFlowId,
                context.getListById(Entity.class, payeeId),
                context.getListById(Entity.class, payorId));
        this.setPayeeId(context, payeeId);
        this.setPayorId(context, payorId);
        this.startDate = startDate;
        this.endDate = endDate;
        this.baseIncome = baseIncome;
        this.baseAlimony = baseAlimony;
        this.smithOstlerRate = smithOstlerRate;
        this.maxAlimony = maxAlimony;
        setSmithOstlerCashFlowId(context, smithOstlerCashFlowId);
        context.put(Alimony.class, id, this);
    }

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar cashFlowCalendar) {
        List<CashFlowInstance> baseCashFlows = getCashFlow().getCashFlowInstances(cashFlowCalendar,
                (calendar, accrualStart, accrualEnd) -> baseAlimony);
        List<CashFlowInstance> smithOstlerCashFlows = smithOstlerCashFlow.getCashFlowInstances(cashFlowCalendar,
                (calendar, accrualStart, accrualEnd) -> {
                    BigDecimal income = calendar.sumMatchingCashFlowForPeriod(accrualStart, accrualEnd,
                            (source) -> {
                                if (source.isPayee(this.payor)) {
                                    return true;
                                }
                                return false;
                            });
                    BigDecimal alimony = income.subtract(baseIncome).multiply(smithOstlerRate);
                    return alimony;
                });
        List<CashFlowInstance> allAlimonyCashFlows = new ArrayList<>(baseCashFlows.size() + smithOstlerCashFlows.size());
        allAlimonyCashFlows.addAll(baseCashFlows);
        allAlimonyCashFlows.addAll(smithOstlerCashFlows);
        return allAlimonyCashFlows;
    }

    @JsonIgnore
    @Override
    public String getName() {
        String result;
        List<Balance> interimBalances = new ArrayList<Balance>();
        result = payor.getName() + "(" + payee.getName() + ")";
        return result;
    }


    @JsonProperty(value = "smithOstlerCashFlowType")
    public String getSmithOstlerCashFlowTypeId() {
        return smithOstlerCashFlow.getId();
    }

    private void setSmithOstlerCashFlowId(@JacksonInject("context") Context context,
                                     @JsonProperty(value = "smithOstlerCashFlow", required = true) String smithOstlerCashFlowId) {
        smithOstlerCashFlow = context.getById(CashFlowFrequency.class, smithOstlerCashFlowId);
    }


    @JsonProperty(value = "payee")
    public String getPayeeId() {
        return payee.getId();
    }

    private void setPayeeId(@JacksonInject("context") Context context,
                            @JsonProperty(value = "payee", required = true) String payeeId) {
        this.payee = context.getById(Entity.class, payeeId);
    }

    public Entity getPayee() {
        return payee;
    }

    public void setPayee(Entity payee) {
        this.payee = payee;
    }

    @JsonProperty(value = "payor")
    public String getPayorId() {
        return payor.getId();
    }

    private void setPayorId(@JacksonInject("context") Context context,
                            @JsonProperty(value = "payor", required = true) String payorId) {
        this.payor = context.getById(Entity.class, payorId);
    }

    public Entity getPayor() {
        return payor;
    }

    public void setPayor(Entity payee) {
        this.payor = payor;
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() { return endDate; }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
}
