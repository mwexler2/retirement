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

package name.wexler.retirement.Asset;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.*;
import name.wexler.retirement.CashFlowFrequency.*;
import name.wexler.retirement.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.CashFlowInstance.SecurityTransaction;
import name.wexler.retirement.CashFlowSource.AccountSource;
import name.wexler.retirement.CashFlowSource.CashFlowSource;
import name.wexler.retirement.Entity.Company;
import name.wexler.retirement.Entity.Entity;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mwexler on 7/9/16.
 */
public class Account extends Asset {
    private static final List<Account> accounts = new ArrayList<>();

    private final AccountSource cashFlowSource;
    private final String accountName;
    private final Company company;

    // History of balances for Cash and Securities
    private final Map<LocalDate, Map<String, ShareBalance>> shareBalancesByDateAndSymbol = new HashMap<>();
    private final Map<LocalDate, CashBalance> accountValueByDate = new HashMap<>();

    @JsonIgnore
    private final List<CashFlowInstance> cashFlowInstances = new ArrayList<>();

    static public void readAccounts(Context context) {
        Set<String> companyIds = new HashSet<>();
        for (Account account: accounts) {
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
    public Account(@JacksonInject("context") Context context,
                      @JsonProperty(value = "id", required = true) String id,
                      @JsonProperty(value = "owners", required = true) List<String> ownerIds,
                      @JsonProperty(value = "initialBalance", defaultValue = "0.00") CashBalance initialBalance,
                      @JsonProperty(value = "interimBalances", required = true) List<CashBalance> interimBalances,
                      @JsonProperty(value = "accountName", required = true) String accountName,
                      @JsonProperty(value = "company", required = true) String companyId,
                      @JsonProperty(value = "indicator") String indicator) throws CashFlowSourceNotFoundException {
        super(context, id, ownerIds, initialBalance, interimBalances);
        this.accountName = accountName;
        this.company = context.getById(Entity.class, companyId);
        this.cashFlowSource = context.getById(CashFlowSource.class, id);
        if (cashFlowSource == null) {
            throw new CashFlowSourceNotFoundException(id);
        }
        context.put(Account.class, indicator, this);
        accounts.add(this);
    }

    public void addCashFlowInstances(List<CashFlowInstance> instances) {
        cashFlowInstances.addAll(instances);
        this.cashFlowInstances.sort(Comparator.comparing(CashFlowInstance::getCashFlowDate));
        computeBalances(this.cashFlowInstances);
        cashFlowSource.setCashFlowInstances(this.cashFlowInstances);
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

    public AccountSource getCashFlowSource() {
        return cashFlowSource;
    }

    private Company getCompany() {
        return company;
    }

    @Override @JsonIgnore
    public List<Balance> getBalances(Scenario scenario) {
        List<Balance> balances = new ArrayList<>(this.accountValueByDate.values());
        balances.sort(Comparator.comparing(Balance::getBalanceDate));
        return balances;
    }

    // Return the total value of securities and cash at each point during the year where it cash or share quantity
    // changed.
    @Override
    @JsonIgnore
    public List<Balance> getBalances(Scenario scenario, int year) {
        List<Balance> balances =
                accountValueByDate.keySet()
                        .stream()
                        .filter(date -> year == date.getYear())
                        .sorted()
                        .map(date -> accountValueByDate.get(date))
                        .collect(Collectors.toList());

        return balances;
    }

    private void processSecurityTransaction(SecurityTransaction txn,
                                            Map<String, ShareBalance> shareBalancesBySymbol) {
        ShareBalance change = txn.getChange();
        Security security = change.getSecurity();
        String symbol = security.getId();

        // Update running share balance for this symbol, creating an entry if it doesn't already exist.
        if (!shareBalancesBySymbol.containsKey(symbol)) {
            shareBalancesBySymbol.put(symbol,
                    new ShareBalance(LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, security));
        }


        // Store the share balance by date and symbol
        if (!shareBalancesByDateAndSymbol.containsKey(txn.getCashFlowDate())) {
            shareBalancesByDateAndSymbol.put(txn.getCashFlowDate(),
                    new HashMap<>());
        }
        Map<String, ShareBalance> shareBalancesAtTxnDate = shareBalancesByDateAndSymbol.get(txn.getCashFlowDate());
        ShareBalance oldBalance = shareBalancesAtTxnDate.get(symbol);
        if (oldBalance == null) {
            oldBalance = new ShareBalance(LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, security);
        }
        if (!change.getShares().equals(BigDecimal.ZERO)) {
            ShareBalance oldShareBalance = shareBalancesBySymbol.get(symbol);
            ShareBalance newShareBalance = oldShareBalance.applyChange(change);
            shareBalancesBySymbol.put(symbol, newShareBalance);

            ShareBalance newBalance = oldBalance.applyChange(change);
            shareBalancesAtTxnDate.put(symbol, newShareBalance);
        }
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
        return cashValue.add(shareValue).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private void computeBalances(List<CashFlowInstance> cashFlowInstances) {
        // Running Balances for Cash and Securities
        CashBalance cashBalance = new CashBalance(getInitialBalance().getBalanceDate(), getInitialBalanceAmount());
        Map<String, ShareBalance> shareBalancesBySymbol = new HashMap<>();

        cashFlowInstances.stream().
                forEach(instance -> {
                    cashBalance.applyChange(instance.getCashFlowDate(), instance.getAmount());
                    if (instance instanceof SecurityTransaction) {
                        processSecurityTransaction((SecurityTransaction) instance, shareBalancesBySymbol);

                    }
                    instance.setCashBalance(cashBalance.getValue());
                    BigDecimal totalValue = calculateTotalValue(shareBalancesBySymbol, cashBalance);
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
}
