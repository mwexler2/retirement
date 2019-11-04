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

package name.wexler.retirement.visualizer.CashFlowSource;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowFrequency.Balance;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowFrequency.ShareBalance;
import name.wexler.retirement.visualizer.CashFlowInstance.Account;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowInstance.LiabilityCashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowInstance.SecurityTransaction;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mwexler on 7/9/16.
 */
public class CreditCardAccount extends Liability implements Account {
    private static final List<CreditCardAccount> accounts = new ArrayList<>();

    private final String accountName;
    private final Company company;

    // History of balances for Cash and Securities
    private final Map<LocalDate, Map<String, ShareBalance>> shareBalancesByDateAndSymbol = new HashMap<>();
    private final Map<LocalDate, CashBalance> accountValueByDate = new HashMap<>();

    @JsonIgnore
    private final List<CashFlowInstance> cashFlowInstances = new ArrayList<>();

    static public void readAccounts(Context context) {
        Set<String> companyIds = new HashSet<>();
        for (CreditCardAccount account: accounts) {
            companyIds.add(account.getCompany().getId());
        }
        companyIds.add(MintAccountReader.mintPseudoCompany);
        getAccountHistory(context, companyIds);
    }

    public class CashFlowSourceNotFoundException extends Exception {
        public CashFlowSourceNotFoundException(String id) {
            super("CashFlowSource: " + id + " not found");
        }
    }

    @JsonCreator
    public CreditCardAccount(@JacksonInject("context") Context context,
                             @JsonProperty(value = "id", required = true) String id,
                             @JsonProperty("borrowers") List<String> borrowersIds,
                             @JsonProperty(value = "startDate",       required=true) LocalDate startDate,
                             @JsonProperty("endDate") LocalDate endDate,
                             @JsonProperty(value = "interestRate",    required = true) BigDecimal interestRate,
                             @JsonProperty(value = "accountName", required = true) String accountName,
                             @JsonProperty(value = "company", required = true) String companyId,
                             @JsonProperty(value = "indicator") String indicator,
                             @JsonProperty(value = "source",          required = true) String sourceId)
            throws DuplicateEntityException {
        super(context, id, companyId, borrowersIds, startDate, endDate, interestRate, BigDecimal.ZERO, sourceId);
        this.accountName = accountName;
        this.company = context.getById(Entity.class, companyId);
        context.put(CreditCardAccount.class, indicator, this);
        accounts.add(this);
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

    public Company getCompany() {
        return company;
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

   private static void readCashFlowInstances(Context context, String companyId) throws IOException, ClassNotFoundException {
       AccountReader accountReader = AccountReader.factory(companyId);

       accountReader.readCashFlowInstances(context, companyId);
   }

   private static void getAccountHistory(Context context, Collection<String> companyIds) {
       for (String companyId: companyIds) {
           try {
               readCashFlowInstances(context, companyId);
           } catch (ClassNotFoundException cnfe) {
               System.out.println("No reader for " + companyId + " skipping.");
           } catch (IOException ioe) {
               ioe.printStackTrace();
           }
       }
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

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getCashFlowInstances(CashFlowCalendar cashFlowCalendar) {
        return cashFlowInstances;
    }

    public void addCashFlowInstances(List<CashFlowInstance> instances) {
        cashFlowInstances.addAll(instances);
        this.cashFlowInstances.sort(Comparator.comparing(CashFlowInstance::getCashFlowDate));
        computeBalances(this.cashFlowInstances);
    }

    public CashFlowSource getCashFlowSource() {
        return this;
    }
}
