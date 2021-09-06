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
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Expense.Expense;
import name.wexler.retirement.visualizer.Expense.Spending;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id", "startDate", "endDate", "payee", "payor", "cashFlow" })
public class IncomeTax extends CashFlowEstimator {
    @JsonIgnore
    private Entity payee;
    @JsonIgnore
    private Entity payor;

    private CashFlowSink defaultSink;
    public static String INCOME_TAX = "Income Tax";
    private static final BigDecimal INCOME_TAX_RATE = BigDecimal.valueOf(0.10);

    @JsonCreator
    public IncomeTax(@JacksonInject("context") Context context,
                     @JsonProperty(value = "id", required = true) String id,
                     @JsonProperty(value = "payee", required = true) String payeeId,
                     @JsonProperty(value = "payor", required = true) String payorId,
                     @JsonProperty(value = "cashFlow", required = true) String cashFlowId,
                     @JsonProperty(value = "defaultSink", required = true) String defaultSinkId
    ) throws DuplicateEntityException {
        super(context, id, cashFlowId,
                context.getListById(Entity.class, payeeId),
                context.getListById(Entity.class, payorId));
        this.setPayeeId(context, payeeId);
        this.setPayorId(context, payorId);
        this.defaultSink = context.getById(Asset.class, defaultSinkId);
        context.put(IncomeTax.class, id, this);
    }


    @JsonIgnore
    @Override
    public List<CashFlowInstance> getEstimatedFutureCashFlows(CashFlowCalendar cashFlowCalendar) {
        List<CashFlowInstance> incomeTaxCashFlows = getCashFlowFrequency().getFutureCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                    BigDecimal income = calendar.sumMatchingCashFlowForPeriod(accrualStart, accrualEnd,
                            (instance) -> {
                                return instance.getCashFlowSink().isOwner(this.payor);
                            });
                    BigDecimal ytdIncomeTax = calendar.sumMatchingCashFlowForPeriod(
                            LocalDate.of(accrualStart.getYear(), Month.JANUARY, 1),
                            LocalDate.of(accrualEnd.getYear(), Month.DECEMBER, 31),
                            (instance) -> {
                                boolean match = instance.getCategory().equals(INCOME_TAX);
                                return match;
                            });
                    BigDecimal incomeTax = income.multiply(INCOME_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
                    CashFlowInstance cashFlowInstance =
                            new CashFlowInstance(true,this, defaultSink,
                            getItemType(), getCategory(),
                            accrualStart, accrualEnd, cashFlowDate, incomeTax, balance);
                    cashFlowInstance.setDescription("Smith Ostler for " + this.payee.getName());
                    return cashFlowInstance;
                });
        return incomeTaxCashFlows;
    }

    @JsonIgnore
    @Override
    public String getName() {
        String result;
        result = payor.getName() + "(" + payee.getName() + ")";
        return result;
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

    @Override
    public String getCategory() {
        return INCOME_TAX;
    }

    @JsonIgnore
    @Override
    public int getPass() {
        return 3;   // Need to calculate all other income before computing alimony, because alimony is computed from rest of income
    }
}
