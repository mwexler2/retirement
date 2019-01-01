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
import name.wexler.retirement.CashFlowFrequency.Balance;
import name.wexler.retirement.CashFlowFrequency.CashFlowCalendar;
import name.wexler.retirement.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.Context;
import name.wexler.retirement.Entity.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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
        List<CashFlowInstance> baseCashFlows = getCashFlow().getCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                    return new CashFlowInstance(this, accrualStart, accrualEnd, cashFlowDate, baseAlimony, balance);
                });
        List<CashFlowInstance> smithOstlerCashFlows = smithOstlerCashFlow.getCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                    BigDecimal income = calendar.sumMatchingCashFlowForPeriod(accrualStart, accrualEnd,
                            (source) -> {
                                if (source.isPayee(this.payor)) {
                                    return true;
                                }
                                return false;
                            });
                    BigDecimal alimony = income.subtract(baseIncome).multiply(smithOstlerRate).setScale(2, RoundingMode.HALF_UP);
                    return new CashFlowInstance(this, accrualStart, accrualEnd, cashFlowDate, alimony, balance);
                });
        List<CashFlowInstance> allAlimonyCashFlows = new ArrayList<>(baseCashFlows.size() + smithOstlerCashFlows.size());
        allAlimonyCashFlows.addAll(baseCashFlows);
        allAlimonyCashFlows.addAll(smithOstlerCashFlows);
        allAlimonyCashFlows.sort((final CashFlowInstance instance1, final CashFlowInstance instance2) ->
            instance1.getCashFlowDate().compareTo(instance2.getAccrualEnd()));
        List<CashFlowInstance> result = new ArrayList<>(allAlimonyCashFlows.size());
        Map<Integer, BigDecimal> remainingBalance = new HashMap<>();
        CashFlowInstance prevCashFlowInstance = null;
        for (CashFlowInstance instance : allAlimonyCashFlows) {
            Integer year = instance.getAccrualEnd().getYear();
            if (remainingBalance.get(year) == null) {
                remainingBalance.put(year, maxAlimony);
            }
            BigDecimal amount = instance.getAmount();
            if (amount.compareTo(remainingBalance.get(year)) < 0) {
                amount = remainingBalance.get(year);
                BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                instance = new CashFlowInstance(this,
                        instance.getAccrualStart(),
                        instance.getAccrualEnd(),
                        instance.getCashFlowDate(),
                        amount, balance);
            }
            result.add(instance);
            remainingBalance.put(year, remainingBalance.get(year).subtract(amount));
            prevCashFlowInstance = instance;
        }
        return result;
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
}
