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

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.CashFlowFrequency.Balance;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.CashFlowFrequency.ShareBalance;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by mwexler on 7/9/16.
 */
public class CreditCardAccount extends Liability {
    private static final List<CreditCardAccount> accounts = new ArrayList<>();

    private final String accountName;
    private final Company company;
    private BigDecimal runningTotal;

    // History of balances for Cash and Securities
    private final Map<LocalDate, Map<String, ShareBalance>> shareBalancesByDateAndSymbol = new HashMap<>();


    public class CashFlowSourceNotFoundException extends Exception {
        public CashFlowSourceNotFoundException(String id) {
            super("CashFlowEstimator: " + id + " not found");
        }
    }

    @JsonCreator
    public CreditCardAccount(@JacksonInject("context") Context context,
                             @JsonProperty(value = "id", required = true) String id,
                             @JsonProperty("borrowers") List<String> borrowersIds,
                             @JsonProperty(value = "startDate", required = true) LocalDate startDate,
                             @JsonProperty("endDate") LocalDate endDate,
                             @JsonProperty(value = "interestRate", required = true) BigDecimal interestRate,
                             @JsonProperty(value = "accountName", required = true) String accountName,
                             @JsonProperty(value = "company", required = true) String companyId,
                             @JsonProperty(value = "indicator") String indicator,
                             @JsonProperty(value = "source", required = true) String sourceId)
            throws DuplicateEntityException {
        super(context, id, companyId, borrowersIds, startDate, endDate, interestRate, BigDecimal.ZERO, sourceId);
        this.accountName = accountName;
        this.company = context.getById(Entity.class, companyId);
        context.put(CreditCardAccount.class, indicator, this);
        accounts.add(this);
        runningTotal = BigDecimal.ZERO;
    }

    @JsonIgnore
    public void setRunningTotal(BigDecimal bigDecimal) {
        this.runningTotal = runningTotal;
    }

    public String getId() {
        return super.getId();
    }

    public String getName() {
        return accountName;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getInstitutionName() {
        return company.getCompanyName();
    }

    private BigDecimal calculateTotalValue(Map<String, ShareBalance> shareBalancesBySymbol,
                                           CashBalance cashBalance) {
        // Calculate the total account value at the transaction date
        BigDecimal cashValue = cashBalance.getValue();


        BigDecimal shareValue = shareBalancesBySymbol
                .values()
                .stream()
                .map(ShareBalance::getValue)
                .reduce(BigDecimal.ZERO,
                        (a, b) -> a.add(b));
        return cashValue.add(shareValue).setScale(2, RoundingMode.HALF_UP);
    }



    public String toString() {
        return this.accountName + " (" + this.company.getCompanyName() + ")";
    }

    public BigDecimal getPaymentAmount() {
        return BigDecimal.ZERO;
    }

    public Balance computeNewBalance(CashFlowInstance cashFlowInstance, Balance prevBalance) {
        BigDecimal interest = prevBalance.getValue().multiply(getPeriodicInterestRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal principal = getPaymentAmount().subtract(interest);
        return new CashBalance(cashFlowInstance.getCashFlowDate(), prevBalance.getValue().subtract(principal));
    }

    public CashFlowEstimator getCashFlowSource() {
        return this;
    }

    public boolean isOwner(Entity entity) {
        return this.getBorrowerIds().contains(entity);
    }

    @Override
    @JsonIgnore
    public void updateRunningTotal(CashFlowInstance cashFlowInstance, boolean negate) {
        runningTotal = runningTotal.add(cashFlowInstance.getAmount());
        cashFlowInstance.setCashBalance(runningTotal);
    }
}
