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
import name.wexler.retirement.visualizer.Entity.Category;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Expense.Expense;
import name.wexler.retirement.visualizer.Expense.Spending;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

import static name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance.NO_ID;

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


    // Calculate any unpaid base alimony for the rest of the year.
    private List<CashFlowInstance> calculateUnpaidBaseAlimony(CashFlowCalendar cashFlowCalendar) {
        return getCashFlowFrequency().getFutureCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                    CashFlowInstance cashFlowInstance =
                            new CashFlowInstance(NO_ID, true, this, defaultSink,
                                    getItemType(), getParentCategory(), getCategory(),
                                    accrualStart, accrualEnd, cashFlowDate, baseAlimony, balance,
                                    payee.getName());
                    return cashFlowInstance;
                });
    }

    // Calculate Smith/Ostler payments for the rest of the year.
    private List<CashFlowInstance> calculateSmithOstler(
            CashFlowCalendar cashFlowCalendar
    ) {
        // The base income is specified monthly but smith/ostler is calculated quarterly (we should parameterize this)
        BigDecimal quarterlyBaseIncome = baseIncome.multiply(BigDecimal.valueOf(3));

        return smithOstlerCashFlow.getFutureCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal income = calendar.sumMatchingCashFlowForPeriod(accrualStart, accrualEnd,
                            (instance) -> {
                                boolean matches = instance.getItemType().equals("INCOME") &&
                                        instance.getCashFlowSource().isOwner(this.getPayers().get(0));
                                return matches;
                            });
                    BigDecimal smithOstlerIncome = income.compareTo(quarterlyBaseIncome) <= 0
                            ? BigDecimal.ZERO
                            : income.subtract(quarterlyBaseIncome);
                    BigDecimal alimony = smithOstlerIncome.multiply(smithOstlerRate).setScale(2, RoundingMode.HALF_UP);
                    if (alimony.compareTo(BigDecimal.ZERO) < 0) {
                        String description = "Estimated Smith Ostler for " + this.payee.getName();
                        return new CashFlowInstance(NO_ID,
                                true, this, defaultSink,
                                getItemType(), getParentCategory(), getCategory(),
                                accrualStart, accrualEnd, cashFlowDate,
                                alimony, BigDecimal.ZERO,
                                description);
                    } else {
                        return null;
                    }
                });
    }

    // remove everything over the max alimony for the year.
    private List<CashFlowInstance> removeCashFlowsOverMax(
            CashFlowCalendar cashFlowCalendar, List<CashFlowInstance> allAlimonyCashFlows
    ) {
        List<CashFlowInstance> result = new ArrayList<>(allAlimonyCashFlows.size());
        Map<Integer, BigDecimal> ytdAlimonies = new HashMap<>();
        Spending spending = this.getContext().getById(Expense.class, "spending");
        for (CashFlowInstance instance : allAlimonyCashFlows) {
            BigDecimal amount = instance.getAmount();
            int accrualYear = instance.getAccrualEnd().getYear();
            if (!ytdAlimonies.containsKey(accrualYear))
                ytdAlimonies.put(accrualYear, cashFlowCalendar.sumMatchingCashFlowForPeriod(
                        LocalDate.of(accrualYear, Month.JANUARY, 1),
                        LocalDate.of(accrualYear, Month.DECEMBER, 31),
                        (calendarInstance) -> calendarInstance.getCategory().equals(ALIMONY)
                ));
            BigDecimal ytdAlimony = ytdAlimonies.get(accrualYear);
            BigDecimal remainingBalance = this.maxAlimony.subtract(ytdAlimony).min(BigDecimal.ZERO);
            if (amount.compareTo(remainingBalance) < 0) {
                amount = remainingBalance;
                String description = "Estimated " + instance.getDescription();
                CashFlowInstance remainingBalanceInstance = new CashFlowInstance(
                        NO_ID,
                        true, spending, defaultSink,
                        getItemType(), getParentCategory(), getCategory(),
                        instance.getAccrualStart(),
                        instance.getAccrualEnd(),
                        instance.getCashFlowDate(),
                        amount, BigDecimal.ZERO, description);
                result.add(remainingBalanceInstance);
                ytdAlimonies.replace(accrualYear, ytdAlimony);
            } else if (instance.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                result.add(instance);
                ytdAlimony = ytdAlimony.add(amount);
                ytdAlimonies.replace(accrualYear, ytdAlimony);
            }
        }
        return result;
    }

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getEstimatedFutureCashFlows(CashFlowCalendar cashFlowCalendar) {
        List<CashFlowInstance> baseCashFlows = calculateUnpaidBaseAlimony(cashFlowCalendar);
        List<CashFlowInstance> smithOstlerCashFlows = calculateSmithOstler(cashFlowCalendar);

        // Combine the base cashflows and Smith/Ostler into a single list
        List<CashFlowInstance> allAlimonyCashFlows = new ArrayList<>(baseCashFlows.size() + smithOstlerCashFlows.size());
        allAlimonyCashFlows.addAll(baseCashFlows);
        allAlimonyCashFlows.addAll(smithOstlerCashFlows);
        allAlimonyCashFlows.sort((final CashFlowInstance instance1, final CashFlowInstance instance2) ->
            instance1.getCashFlowDate().compareTo(instance2.getAccrualEnd()));

        return removeCashFlowsOverMax(cashFlowCalendar, allAlimonyCashFlows);
    }

    @JsonIgnore
    @Override
    public String getName() {
        String result;
        result = this.getId() + ": " + payor.getName() + "(" + payee.getName() + ")";
        return result;
    }

    @Override
    @NotNull String getParentCategory() {
        return Category.BILLS_AND_UTILITIES;
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

    @JsonProperty(value = "payor")
    public String getPayorId() {
        return payor.getId();
    }

    private void setPayorId(@JacksonInject("context") Context context,
                            @JsonProperty(value = "payor", required = true) String payorId) {
        this.payor = context.getById(Entity.class, payorId);
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

    @Override
    public boolean isOwner(Entity entity) {
        return this.payor.getId().equals(entity.getId());
    }

    @JsonIgnore
    @Override
    public CASH_ESTIMATE_PASS getPass() {
        return CASH_ESTIMATE_PASS.DERIVED_EXPENSES;   // Need to calculate all income before computing alimony, because alimony is computed from rest of income
    }
}
