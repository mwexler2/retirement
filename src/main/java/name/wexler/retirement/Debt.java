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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id", "source", "lender", "borrowers", "security", "startDate", "term", "interestRate", "startingBalance", "paymentAmount" })
public class Debt extends ExpenseSource {
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

    public Debt(@JacksonInject("context") Context context,
                @JsonProperty("id") String id,
                @JsonProperty("lender") String lenderId) throws Exception {
        super(context, id);
        setLenderId(context, lenderId);
    }


    public Debt(Context context, String id, Entity lender, Entity[] borrowers, Asset security,
         LocalDate startDate, int term, BigDecimal interestRate, BigDecimal startingBalance,
         BigDecimal paymentAmount, CashFlowType source) throws Exception {
        super(context, id);
        this.setCashFlow(source);
        this.security = security;
        this.lender = lender;
        this.borrowers = borrowers;
        this.startDate = startDate;
        this.term = term;
        this.interestRate = interestRate;
        this.startingBalance = startingBalance;
        this.paymentAmount = paymentAmount;
    }

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
    public BigDecimal getAnnualCashFlow() {
        return getAnnualCashFlow(LocalDate.now().getYear());
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

    @JsonProperty(value = "source")
    public String getSourceId() {
        return this.getCashFlow().getId();
    }

    @JsonProperty(value = "security")
    public String getSecurityId() {
        return this.security.getId();
    }

    public void setLenderId(@JacksonInject("context") Context context,
                            @JsonProperty(value="lender", required=true) String lenderId) {
        this.lender = context.getById(Entity.class, lenderId);
    }

    @JsonProperty(value = "lender")
    public String getLenderId() {
        return lender.getId();
    }

    public Entity getLender() {
        return lender;
    }

    public void setLender(Entity lender) {
        this.lender = lender;
    }

    public void setBorrowersIds(@JacksonInject("context") Context context,
                                @JsonProperty(value="borrowers", required=true) String[] borrowerIds) {
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
