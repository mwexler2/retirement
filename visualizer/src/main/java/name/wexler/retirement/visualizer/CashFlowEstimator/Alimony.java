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

package name.wexler.retirement.visualizer.CashFlowEstimator;

import com.fasterxml.jackson.annotation.*;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Expense.Expense;
import name.wexler.retirement.visualizer.Expense.Spending;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id", "startDate", "endDate", "payee", "payor", "cashFlow", "smithOstlerCashFlowType" })
public class Alimony extends CashFlowEstimator {
    @JsonIgnore
    private Entity payee;
    @JsonIgnore
    private Entity payor;
    private final BigDecimal baseIncome;
    private final BigDecimal baseAlimony;
    private final BigDecimal smithOstlerRate;
    private final BigDecimal maxAlimony;
    private CashFlowFrequency smithOstlerCashFlow;
    private CashFlowSink defaultSink;
    private static String ALIMONY = "Alimony";

    @JsonCreator
    public Alimony(@JacksonInject("context") Context context,
                   @JsonProperty(value = "id", required = true) String id,
                   @JsonProperty(value = "payee", required = true) String payeeId,
                   @JsonProperty(value = "payor", required = true) String payorId,
                   @JsonProperty(value = "baseIncome", required = true) BigDecimal baseIncome,
                   @JsonProperty(value = "baseAlimony", required = true) BigDecimal baseAlimony,
                   @JsonProperty(value = "smithOstlerRate", required = true) BigDecimal smithOstlerRate,
                   @JsonProperty("maxAlimony") BigDecimal maxAlimony,
                   @JsonProperty("baseCashFlow") String baseCashFlowId,
                   @JsonProperty("smithOstlerCashFlow") String smithOstlerCashFlowId,
                   @JsonProperty(value = "defaultSink", required = true) String defaultSinkId
    ) throws DuplicateEntityException {
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
        this.defaultSink = context.getById(Asset.class, defaultSinkId);
        context.put(Alimony.class, id, this);
    }


    @JsonIgnore
    @Override
    public List<CashFlowInstance> getEstimatedFutureCashFlows(CashFlowCalendar cashFlowCalendar) {
        List<CashFlowInstance> baseCashFlows = getCashFlowFrequency().getFutureCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                    CashFlowInstance cashFlowInstance =
                            new CashFlowInstance(true, this, defaultSink,
                            getItemType(), getCategory(),
                            accrualStart, accrualEnd, cashFlowDate, baseAlimony, balance);
                    cashFlowInstance.setDescription(payee.getName());
                    return cashFlowInstance;
                });
        List<CashFlowInstance> smithOstlerCashFlows = smithOstlerCashFlow.getFutureCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                    BigDecimal income = calendar.sumMatchingCashFlowForPeriod(accrualStart, accrualEnd,
                            (instance) -> {
                                return instance.getCashFlowSink().isOwner(this.payor);
                            });
                    BigDecimal ytdAlimony = calendar.sumMatchingCashFlowForPeriod(
                            LocalDate.of(accrualStart.getYear(), Month.JANUARY, 1),
                            LocalDate.of(accrualEnd.getYear(), Month.DECEMBER, 31),
                            (instance) -> {
                                boolean match = instance.getCategory().equals(ALIMONY);
                                if (instance.getAmount().compareTo(BigDecimal.valueOf(100000)) > 0 ||
                                        instance.getAmount().compareTo(BigDecimal.valueOf(-100000)) < 0) {
                                    System.out.println("big item, amount = " + instance.getAmount() + ", category = " + instance.getCategory());
                                }
                                if (match) System.out.println("category = " + instance.getCategory() + ", match = " + match);
                                return match;
                            });
                    BigDecimal alimony = income.compareTo(baseIncome) <= 0 ? BigDecimal.ZERO : income.subtract(baseIncome).multiply(smithOstlerRate).setScale(2, RoundingMode.HALF_UP);
                    if (accrualStart.getYear() == 2020) {
                        System.out.println("income = " + income + ", ytdAlimony = " + ytdAlimony + ", alimony = " + alimony);
                    }
                    alimony = alimony.max(this.maxAlimony.subtract(ytdAlimony));
                    CashFlowInstance cashFlowInstance =
                            new CashFlowInstance(true,this, defaultSink,
                            getItemType(), getCategory(),
                            accrualStart, accrualEnd, cashFlowDate, alimony, balance);
                    cashFlowInstance.setDescription("Smith Ostler for " + this.payee.getName());
                    return cashFlowInstance;
                });
        List<CashFlowInstance> allAlimonyCashFlows = new ArrayList<>(baseCashFlows.size() + smithOstlerCashFlows.size());
        allAlimonyCashFlows.addAll(baseCashFlows);
        allAlimonyCashFlows.addAll(smithOstlerCashFlows);
        allAlimonyCashFlows.sort((final CashFlowInstance instance1, final CashFlowInstance instance2) ->
            instance1.getCashFlowDate().compareTo(instance2.getAccrualEnd()));
        List<CashFlowInstance> result = new ArrayList<>(allAlimonyCashFlows.size());
        Map<Integer, BigDecimal> remainingBalance = new HashMap<>();
        CashFlowInstance prevCashFlowInstance = null;
        Spending spending = this.getContext().getById(Expense.class, "spending");
        for (CashFlowInstance instance : allAlimonyCashFlows) {
            Integer year = instance.getAccrualEnd().getYear();
            if (remainingBalance.get(year) == null) {
                remainingBalance.put(year, maxAlimony);
            }
            BigDecimal amount = instance.getAmount();
            if (amount.compareTo(remainingBalance.get(year)) < 0) {
                amount = remainingBalance.get(year);
                BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                instance = new CashFlowInstance(
                        true, spending, defaultSink,
                        getItemType(), getCategory(),
                        instance.getAccrualStart(),
                        instance.getAccrualEnd(),
                        instance.getCashFlowDate(),
                        amount, balance);
                instance.setDescription("Remaining balance for " + this.payee.getName());
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


    private void setPayeeId(@JacksonInject("context") Context context,
                            @JsonProperty(value = "payee", required = true) String payeeId) {
        this.payee = context.getById(Entity.class, payeeId);
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

    public void setPayor(Entity payor) {
        this.payor = payor;
    }

    @JsonProperty(value = "payee")
    public String getPayeeId() {
        return payee.getId();
    }


    @JsonIgnore
    @Override
    public String getItemType() {
        return CashFlowCalendar.ITEM_TYPE.EXPENSE.name();
    }

    @JsonIgnore
    @Override
    public int getPass() {
        return 3;   // Need to calculate all other income before computing alimony, because alimony is computed from rest of income
    }
}
