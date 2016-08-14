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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.CashFlow.CashFlowSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Debt extends ExpenseSource {
    private Entity lender;
    private Entity[] borrowers;
    private Asset security;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate startDate;
    private int term;
    private BigDecimal interestRate;
    private BigDecimal startingBalance;
    private BigDecimal paymentAmount;
    private BigDecimal monthsPerYear = BigDecimal.valueOf(12);


    public Debt() {

    }

    public Debt(Entity lender, Entity[] borrowers, Asset security,
         LocalDate startDate, int term, BigDecimal interestRate, BigDecimal startingBalance,
         BigDecimal paymentAmount, CashFlowSource source) {
        super(source);
        this.security = security;
        this.lender = lender;
        this.borrowers = borrowers;
        this.startDate = startDate;
        this.term = term;
        this.interestRate = interestRate;
        this.startingBalance = startingBalance;
        this.paymentAmount = paymentAmount;
    }

    public BigDecimal getMonthlyCashFlow(YearMonth yearMonth) {
        return paymentAmount.negate();
    }

    public BigDecimal getAnnualCashFlow(int year) {
        return paymentAmount.negate().multiply(monthsPerYear);
    }

    public BigDecimal getAnnualCashFlow() {
        return getAnnualCashFlow(LocalDate.now().getYear());
    }


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

    public Entity getLender() {
        return lender;
    }

    public void setLender(Entity lender) {
        this.lender = lender;
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

    public BigDecimal getMonthsPerYear() {
        return monthsPerYear;
    }

    public void setMonthsPerYear(BigDecimal monthsPerYear) {
        this.monthsPerYear = monthsPerYear;
    }
}
