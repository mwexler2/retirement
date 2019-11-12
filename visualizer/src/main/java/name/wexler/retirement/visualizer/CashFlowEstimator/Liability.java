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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowInstance.Account;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.JSON.JSONDateDeserialize;
import name.wexler.retirement.visualizer.JSON.JSONDateSerialize;
import name.wexler.retirement.visualizer.CashFlowFrequency.Balance;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id", "source", "lender", "borrowers", "startDate", "endDate", "interestRate", "startingBalance" })
abstract public class Liability extends CashFlowEstimator implements Account {
    @JsonDeserialize(using= JSONDateDeserialize.class)
    @JsonSerialize(using= JSONDateSerialize.class)
    private LocalDate startDate;
    @JsonIgnore
    private final List<CashFlowInstance> cashFlowInstances = new ArrayList<>();
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private final LocalDate endDate;
    private final BigDecimal interestRate;
    private final BigDecimal periodicInterestRate;
    private final Balance _startingBalance;
    private final Map<LocalDate, CashBalance> accountValueByDate = new HashMap<>();

    @JsonCreator
    public Liability(@JacksonInject("context") Context context,
                @JsonProperty(value = "id",              required = true) String id,
                @JsonProperty("lender") String lenderId,
                @JsonProperty("borrowers") List<String> borrowersIds,
                @JsonProperty(value = "startDate",       required=true) LocalDate startDate,
                @JsonProperty("endDate") LocalDate endDate,
                @JsonProperty(value = "interestRate",    required = true) BigDecimal interestRate,
                @JsonProperty(value = "startingBalance", required = true) BigDecimal startingBalance,
                @JsonProperty(value = "source",          required = true) String sourceId)
    throws DuplicateEntityException {
        super(context, id, sourceId,
                context.getListById(Entity.class, lenderId),
                context.getByIds(Entity.class, borrowersIds));
        this._startingBalance = new CashBalance(startDate, startingBalance);
        this.startDate = startDate;
        this.endDate = endDate;
        this.interestRate = interestRate;
        BigDecimal periodsPerYear = BigDecimal.valueOf(12);
        this.periodicInterestRate = interestRate.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)
                .divide(periodsPerYear, RoundingMode.HALF_UP)
                .setScale(10, RoundingMode.HALF_UP);
        context.put(Liability.class, id, this);
    }


    @JsonIgnore
    @Override
    public String getName() {
        String result;

        result = getLender().getName();
        return result;
    }

    @JsonIgnore
    public Company getCompany() {
        return (Company) getLender();
    }

    @JsonProperty(value = "source")
    public String getSourceId() {
        return this.getCashFlow().getId();
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
        for (Entity borrower : borrowers) result.add(borrower.getId());
        return result;
    }

    private List<Entity> getBorrowers() {
        return getPayers();
    }

    public LocalDate getEndDate() { return endDate; }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public BigDecimal getPeriodicInterestRate() { return periodicInterestRate; }

    @Override public Balance getStartingBalance() {
        return _startingBalance;
    }

    abstract public BigDecimal getPaymentAmount();

    abstract public Balance computeNewBalance(CashFlowInstance cashFlowInstance, Balance prevBalance);

    private void computeBalances(List<CashFlowInstance> cashFlowInstances) {
        // Running Balances for Cash and Securities
        CashBalance cashBalance = new CashBalance(getStartingBalance().getBalanceDate(), getStartingBalance().getValue());

        cashFlowInstances.stream().
                forEach(instance -> {
                    cashBalance.applyChange(instance.getCashFlowDate(), instance.getAmount());
                    instance.setCashBalance(cashBalance.getValue());
                    BigDecimal totalValue = cashBalance.getValue();
                    instance.setAssetBalance(totalValue);
                    accountValueByDate.put(instance.getCashFlowDate(),
                            new CashBalance(instance.getCashFlowDate(), totalValue));
                });
    }

    public void addCashFlowInstances(List<CashFlowInstance> instances) {
        cashFlowInstances.addAll(instances);
        this.cashFlowInstances.sort(Comparator.comparing(CashFlowInstance::getCashFlowDate));
        computeBalances(this.cashFlowInstances);
    }

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar cashFlowCalendar) {
        return cashFlowInstances;
    }

    @JsonIgnore
    public CashFlowEstimator getCashFlowSource() {
        return this;
    }

    @JsonIgnore
    public void sinkCashFlowInstance(CashFlowInstance cashFlowInstance) {

    }

    @JsonIgnore
    @Override
    public String getItemType() {
        return CashFlowCalendar.ITEM_TYPE.EXPENSE.name();
    }
}
