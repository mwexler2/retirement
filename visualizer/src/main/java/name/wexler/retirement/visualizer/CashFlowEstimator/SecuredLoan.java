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
import name.wexler.retirement.visualizer.CashFlowFrequency.Balance;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowInstance.LiabilityCashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id", "source", "lender", "borrowers", "security", "startDate", "endDate", "term", "interestRate", "startingBalance", "paymentAmount" })
public class SecuredLoan extends Liability {
    private final Asset security;
    private final int term;
    private BigDecimal paymentAmount;
    private final BigDecimal impoundAmount;
    private CashFlowSink defaultSink;
    private BigDecimal runningTotal;

    @JsonCreator
    public SecuredLoan(@JacksonInject("context") Context context,
                       @JsonProperty(value = "id",              required = true) String id,
                       @JsonProperty("lender") String lenderId,
                       @JsonProperty("borrowers") List<String> borrowersIds,
                       @JsonProperty("asset") Asset security,
                       @JsonProperty(value = "startDate",       required=true) LocalDate startDate,
                       @JsonProperty("endDate") LocalDate endDate,
                       @JsonProperty(value = "term",            required=true) int term,
                       @JsonProperty(value = "interestRate",    required = true) BigDecimal interestRate,
                       @JsonProperty(value = "startingBalance", required = true) BigDecimal startingBalance,
                       @JsonProperty(value = "paymentAmount",   required = true) BigDecimal paymentAmount,
                       @JsonProperty(value = "impoundAmount",   required = true) BigDecimal impoundAmount,
                       @JsonProperty(value = "source",          required = true) String sourceId,
                       @JsonProperty(value = "defaultSink",   required = true) String defaultSinkId,
                       @JsonProperty(value = "indicators",      required = true) List<String> indicators)
    throws DuplicateEntityException {
        super(context, id, lenderId, borrowersIds, startDate, endDate, interestRate,
                startingBalance, sourceId);
        this.security = security;
        this.term = term;
        BigDecimal periodsPerYear = BigDecimal.valueOf(12);
        this.paymentAmount = paymentAmount;
        this.impoundAmount = impoundAmount;
        this.defaultSink = context.getById(Asset.class, defaultSinkId);
        for (String indicator : indicators) {
            context.put(SecuredLoan.class, indicator, this);
        }
    }

    @JsonIgnore
    public void setRunningTotal(BigDecimal bigDecimal) {
        this.runningTotal = runningTotal;
    }

    @JsonIgnore
    public boolean isOwner(Entity entity) {
        return this.getBorrowerIds().contains(entity.getId());
    }

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getEstimatedFutureCashFlows(CashFlowCalendar cashFlowCalendar) {

        return getCashFlowFrequency().getFutureCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) ->
        {
            BigDecimal balance = getStartingBalance().getValue();
            if (prevCashFlowInstance != null)
                 balance = prevCashFlowInstance.getCashBalance();
            BigDecimal interest = balance.multiply(getPeriodicInterestRate()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principal = paymentAmount.subtract(impoundAmount).subtract(interest).setScale(2, RoundingMode.HALF_UP);
            if (principal.compareTo(balance) >= 1)
                principal = balance;
            balance = balance.subtract(principal);
            return new LiabilityCashFlowInstance(true,this, defaultSink,
                    getCategory(), accrualStart, accrualEnd, cashFlowDate,
                    principal, interest, impoundAmount, balance);
        });
    }

    @JsonIgnore
    @Override
    public String getName() {
        String result;

        if (security != null) {
            result = security.getName() + "(" + getLender().getName() + ")";
        } else {
            result = getLender().getName();
        }
        return result;
    }

    @JsonProperty(value = "source")
    public String getSourceId() {
        return this.getCashFlowFrequency().getId();
    }


    @JsonProperty(value = "security")
    public String getSecurityId() {
        return this.security.getId();
    }

    public int getTerm() {
        return term;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Balance computeNewBalance(CashFlowInstance cashFlowInstance, Balance prevBalance) {
        BigDecimal interest = prevBalance.getValue().multiply(getPeriodicInterestRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal principal = paymentAmount.subtract(interest).subtract(impoundAmount);
        return new CashBalance(cashFlowInstance.getCashFlowDate(), prevBalance.getValue().subtract(principal));
    }

    @JsonIgnore
    @Override
    public String getItemType() {
        return CashFlowCalendar.ITEM_TYPE.EXPENSE.name();
    }
}
