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
import name.wexler.retirement.CashFlow.CashFlowFrequency;
import name.wexler.retirement.CashFlow.Balance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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
    private BigDecimal monthsPerYear = BigDecimal.valueOf(12);


    @JsonCreator
    public Liability(@JacksonInject("context") Context context,
                @JsonProperty("id") String id,
                @JsonProperty("lender") String lenderId,
                @JsonProperty("borrowers") String[] borrowersIds,
                @JsonProperty("asset") Asset security,
                @JsonProperty("startDate") LocalDate startDate,
                @JsonProperty("endDate") LocalDate endDate,
                @JsonProperty("term") int term,
                @JsonProperty("interestRate") BigDecimal interestRate,
                @JsonProperty("startingBalance") BigDecimal startingBalance,
                @JsonProperty("paymentAmount") BigDecimal paymentAmount,
                @JsonProperty("source") String sourceId) throws Exception {
        super(context, id, sourceId,
                context.getListById(Entity.class, lenderId),
                context.getByIds(Entity.class, Arrays.asList(borrowersIds)));
        this._startingBalance = new Balance(startDate, startingBalance);
        this.security = security;
        this.startDate = startDate;
        this.endDate = endDate;
        this.term = term;
        this.interestRate = interestRate;
        this.paymentAmount = paymentAmount;
        context.put(Liability.class, id, this);
    }


    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar cashFlowCalendar) {
        return getCashFlow().getCashFlowInstances(cashFlowCalendar, (calendar, accrualStart, accrualEnd) -> paymentAmount);
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

    public Balance getStartingBalance() {
        return _startingBalance;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

}
