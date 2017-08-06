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
import name.wexler.retirement.CashFlow.CashFlowInstance;
import name.wexler.retirement.CashFlow.CashFlowType;
import name.wexler.retirement.CashFlow.Balance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id", "source", "lender", "borrowers", "security", "startDate", "term", "interestRate", "startingBalance", "paymentAmount" })
public class Liability extends ExpenseSource {
    @JsonIgnore
    private Entity lender;
    @JsonIgnore
    private Entity[] borrowers;
    @JsonIgnore
    private Asset security;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate startDate;
    private int term;
    private BigDecimal interestRate;
    private BigDecimal startingBalance;
    private BigDecimal paymentAmount;
    private BigDecimal monthsPerYear = BigDecimal.valueOf(12);


    @JsonCreator
    public Liability(@JacksonInject("context") Context context,
                @JsonProperty("id") String id,
                @JsonProperty("lender") String lenderId,
                @JsonProperty("borrowers") String[] borrowersIds,
                @JsonProperty("asset") Asset security,
                @JsonProperty("startDate") LocalDate startDate,
                @JsonProperty("term") int term,
                @JsonProperty("interestRate") BigDecimal interestRate,
                @JsonProperty("startingBalance") BigDecimal startingBalance,
                @JsonProperty("paymentAmount") BigDecimal paymentAmount,
                @JsonProperty("source") String sourceId) throws Exception {
        super(context, id);
        this.security = security;
        this.setLenderId(context, lenderId);
        this.setBorrowersIds(context, borrowersIds);
        this.setSourceId(context, sourceId);
        this.startDate = startDate;
        this.term = term;
        this.interestRate = interestRate;
        this.startingBalance = startingBalance;
        this.paymentAmount = paymentAmount;
        context.put(Liability.class, id, this);
    }

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances() {
        return getCashFlow().getCashFlowInstances(paymentAmount.multiply(BigDecimal.valueOf(12)));
    }

    public BigDecimal getMonthlyCashFlow(YearMonth yearMonth) {
        return paymentAmount.negate();
    }

    public BigDecimal getAnnualCashFlow(int year) {
        return paymentAmount.negate().multiply(monthsPerYear);
    }


    @JsonIgnore
    @Override
    public String getName() {
        String result;

        if (security != null) {
            result = security.getName() + "(" + lender.getName() + ")";
        } else {
            result = lender.getName();
        }
        return result;
    }

    public Balance getBalance(LocalDate valueDate) {
        BigDecimal principalPayments = BigDecimal.ZERO;
        BigDecimal value = startingBalance.add(principalPayments);
        return new Balance(valueDate, value);
    }

    @JsonProperty(value = "source")
    public String getSourceId() {
        return this.getCashFlow().getId();
    }

    private void setSourceId(@JacksonInject("context") Context context,
                             @JsonProperty(value = "source", required = true) String sourceId) {
        this.setCashFlow(context.getById(CashFlowType.class, sourceId));
    }

    @JsonProperty(value = "security")
    public String getSecurityId() {
        return this.security.getId();
    }


    @JsonProperty(value = "lender")
    public String getLenderId() {
        return lender.getId();
    }

    private void setLenderId(@JacksonInject("context") Context context,
                             @JsonProperty(value = "lender", required = true) String lenderId) {
        this.lender = context.getById(Entity.class, lenderId);
    }

    public Entity getLender() {
        return lender;
    }

    public void setLender(Entity lender) {
        this.lender = lender;
    }

    private void setBorrowersIds(@JacksonInject("context") Context context,
                                 @JsonProperty(value = "borrowers", required = true) String[] borrowerIds) {
        this.borrowers = new Entity[borrowerIds.length];
        for (int i = 0; i < borrowerIds.length; ++i) {
            borrowers[i] = context.getById(Entity.class, borrowerIds[i]);
        }
    }

    @JsonProperty(value = "borrowers")
    public String[] getBorrowerIds() {

        String[] result = new String[borrowers.length];
        for (int i = 0; i < borrowers.length; ++i)
            result[i] = borrowers[i].getId();
        return result;
    }

    public Entity[] getBorrowers() {
        return borrowers;
    }

    public void setBorrowers(Entity[] borrowers) {
        this.borrowers = borrowers;
    }

    public Asset getSecurity() {
        return security;
    }

    public void setSecurity(Asset security) {
        this.security = security;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getStartingBalance() {
        return startingBalance;
    }

    public void setStartingBalance(BigDecimal startingBalance) {
        this.startingBalance = startingBalance;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    @JsonIgnore
    public BigDecimal getMonthsPerYear() {
        return monthsPerYear;
    }

    public void setMonthsPerYear(BigDecimal monthsPerYear) {
        this.monthsPerYear = monthsPerYear;
    }
}
