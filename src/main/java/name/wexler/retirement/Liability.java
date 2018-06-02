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
import name.wexler.retirement.CashFlow.CashFlowCalendar;
import name.wexler.retirement.CashFlow.CashFlowInstance;
import name.wexler.retirement.CashFlow.Balance;

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
@JsonPropertyOrder({ "type", "id", "source", "lender", "borrowers", "security", "startDate", "endDate", "term", "interestRate", "startingBalance", "paymentAmount" })
public class Liability extends CashFlowSource {
    private Asset security;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate startDate;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate endDate;
    private int term;
    private BigDecimal interestRate;
    private Balance _startingBalance;
    private BigDecimal paymentAmount;
    private BigDecimal impoundAmount;
    private BigDecimal periodsPerYear = BigDecimal.valueOf(12);


    @JsonCreator
    public Liability(@JacksonInject("context") Context context,
                @JsonProperty(value = "id",              required = true) String id,
                @JsonProperty("lender") String lenderId,
                @JsonProperty("borrowers") String[] borrowersIds,
                @JsonProperty("asset") Asset security,
                @JsonProperty(value = "startDate",       required=true) LocalDate startDate,
                @JsonProperty("endDate") LocalDate endDate,
                @JsonProperty(value = "term",            required=true) int term,
                @JsonProperty(value = "interestRate",    required = true) BigDecimal interestRate,
                @JsonProperty(value = "startingBalance", required = true) BigDecimal startingBalance,
                @JsonProperty(value = "paymentAmount",   required = true) BigDecimal paymentAmount,
                @JsonProperty(value = "impoundAmount",   required = true) BigDecimal impoundAmount,
                @JsonProperty(value = "source",          required = true) String sourceId) throws Exception {
        super(context, id, sourceId,
                context.getListById(Entity.class, lenderId),
                context.getByIds(Entity.class, Arrays.asList(borrowersIds)));
        this._startingBalance = new Balance(startDate, startingBalance);
        this.security = security;
        this.startDate = startDate;
        this.endDate = endDate;
        this.term = term;
        this.interestRate = interestRate.divide(BigDecimal.valueOf(100)).divide(periodsPerYear, RoundingMode.HALF_UP).setScale(10, RoundingMode.HALF_UP);
        this.paymentAmount = paymentAmount;
        this.impoundAmount = impoundAmount;
        context.put(Liability.class, id, this);
    }


    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar cashFlowCalendar) {
        return getCashFlow().getCashFlowInstances(cashFlowCalendar, this, (calendar, cashFlowId, accrualStart, accrualEnd, percent) -> paymentAmount);
    }

    @JsonIgnore
    @Override
    public String getName() {
        String result;
        List<Balance> interimBalances = new ArrayList<Balance>();

        if (security != null) {
            result = security.getName() + "(" + getLender().getName() + ")";
        } else {
            result = getLender().getName();
        }
        return result;
    }

    public Balance getBalanceAtDate(Balance prevBalance, LocalDate valueDate) {
        BigDecimal balance = BigDecimal.ZERO;
        if (!valueDate.isBefore(startDate) && !valueDate.isAfter(endDate)) {
            BigDecimal principalPayments = BigDecimal.ZERO;
            balance = _startingBalance.getValue().add(principalPayments);
        }
        return new Balance(valueDate, balance);
    }

    @JsonProperty(value = "source")
    public String getSourceId() {
        return this.getCashFlow().getId();
    }


    @JsonProperty(value = "security")
    public String getSecurityId() {
        return this.security.getId();
    }


    @JsonProperty(value = "lender")
    public String getLenderId() {
        return getLender().getId();
    }


    public Entity getLender() {
        return getPayees().get(0);
    }


    @JsonProperty(value = "borrowers")
    public List<String> getBorrowerIds() {

        List<Entity> borrowers = getBorrowers();
        List<String> result = new ArrayList<>(borrowers.size());
        for (int i = 0; i < borrowers.size(); ++i)
            result.add(borrowers.get(i).getId());
        return result;
    }

    public List<Entity> getBorrowers() {
        return getPayers();
    }

    public LocalDate getEndDate() { return endDate; }


    public int getTerm() {
        return term;
    }


    public BigDecimal getInterestRate() {
        return interestRate;
    }

    @Override public Balance getStartingBalance() {
        return _startingBalance;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Balance computeNewBalance(CashFlowInstance cashFlowInstance, Balance prevBalance) {
        BigDecimal interest = prevBalance.getValue().multiply(interestRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal principal = paymentAmount.subtract(interest).subtract(impoundAmount);
        return new Balance(cashFlowInstance.getCashFlowDate(), prevBalance.getValue().subtract(principal));
    }
}
