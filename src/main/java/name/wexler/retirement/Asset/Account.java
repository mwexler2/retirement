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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mwexler on 7/9/16.
 */
public class Account extends Asset {
    private static List<Account> accounts = new ArrayList<>();
    private static final String accountsPath = "accounts.json";

    private final AccountSource cashFlowSource;
    private final String accountName;
    private final Company company;
    private final String indicator;

    // History of balances for Cash and Securities
    private Map<LocalDate, CashBalance> cashBalanceAtDate = new HashMap<>();
    private Map<LocalDate, Map<String, ShareBalance>> shareBalancesByDateAndSymbol = new HashMap<>();
    private Map<LocalDate, CashBalance> accountValueByDate = new HashMap<>();

    @JsonIgnore
    private List<CashFlowInstance> cashFlowInstances = new ArrayList<>();
    static public void readAccounts(Context context) throws IOException {
        Set<Company> companies = new HashSet<>();
        for (Account account: accounts) {
            companies.add(account.getCompany());
        }
        getAccountHistory(context, companies);
        return;

    }

    @JsonCreator
    public Account(@JacksonInject("context") Context context,
                      @JsonProperty(value = "id", required = true) String id,
                      @JsonProperty(value = "owners", required = true) List<String> ownerIds,
                      @JsonProperty(value = "initialBalance", defaultValue = "0.00") CashBalance initialBalance,
                      @JsonProperty(value = "interimBalances", required = true) List<CashBalance> interimBalances,
                      @JsonProperty(value = "accountName", required = true) String accountName,
                      @JsonProperty(value = "company", required = true) String companyId,
                      @JsonProperty(value = "indicator") String indicator) {
        super(context, id, ownerIds, initialBalance, interimBalances);
        this.accountName = accountName;
        this.company = context.getById(Entity.class, companyId);
        this.cashFlowSource = context.getById(CashFlowSource.class, id);
        this.indicator = indicator;
        context.put(Account.class, indicator, this);
        accounts.add(this);
    }

    public void addCashFlowInstances(List<CashFlowInstance> instances) {
        cashFlowInstances.addAll(instances);
        this.cashFlowInstances.sort(Comparator.comparing(CashFlowInstance::getCashFlowDate));
        computeBalances(this.cashFlowInstances);
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

    public Company getCompany() {
        return company;
    }

    @Override @JsonIgnore
    public List<Balance> getBalances() {
        List<Balance> balances = new ArrayList<Balance>(this.accountValueByDate.values());
        balances.sort(Comparator.comparing(Balance::getBalanceDate));
        return balances;
    }

    // Return the total value of securities and cash at each point during the year where it cash or share quantity
    // changed.
    @Override
    @JsonIgnore
    public List<Balance> getBalances(int year) {
        List<Balance> balances =
                accountValueByDate.values()
                        .stream()
                        .filter(balance -> year == balance.getBalanceDate().getYear())
                        .sorted(Comparator.comparing(CashBalance::getBalanceDate))
                        .collect(Collectors.toList());

        return balances;
    }

    private void processSecurityTransaction(SecurityTransaction txn,
                                            CashBalance cashBalance,
                                            Map<String, ShareBalance> shareBalancesBySymbol) {
        ShareBalance change = txn.getChange();
        Security security = change.getSecurity();
        String symbol = security.getId();

        System.out.println("    change = " + change);
        // Update running share balance for this symbol, creating an entry if it doesn't already exist.
        if (!shareBalancesBySymbol.containsKey(symbol)) {
            shareBalancesBySymbol.put(symbol,
                    new ShareBalance(LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, security));
        }
        ShareBalance oldShareBalance = shareBalancesBySymbol.get(symbol);
        ShareBalance newShareBalance = oldShareBalance.applyChange(change);
        System.out.println("    oldShareBalance = " + oldShareBalance + ", newShareBalance = " + newShareBalance);
        shareBalancesBySymbol.put(symbol, newShareBalance);

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
        ShareBalance newBalance = oldBalance.applyChange(change);
        shareBalancesAtTxnDate.put(symbol, newShareBalance);
        System.out.println("    oldBalance = " + oldBalance + ", newBalance = " + newBalance);

        // Calculate the total account value at the transaction date
        BigDecimal cashValue = cashBalance.getValue();
        BigDecimal shareValue = shareBalancesBySymbol
                .values()
                .stream()
                .map(ShareBalance::getValue)
                .reduce(BigDecimal.ZERO,
                        (a, b) -> a.add(b));
        BigDecimal totalValue = cashValue.add(shareValue).setScale(2, BigDecimal.ROUND_HALF_UP);
        System.out.println("     accountValue: " + totalValue + " = " + cashValue + " + " + shareValue);
        accountValueByDate.put(txn.getCashFlowDate(),
                new CashBalance(txn.getCashFlowDate(), totalValue));
    }

    private void computeBalances(List<CashFlowInstance> cashFlowInstances) {
        // Running Balances for Cash and Securities
        CashBalance cashBalance = new CashBalance(LocalDate.now(), BigDecimal.ZERO);
        Map<String, ShareBalance> shareBalancesBySymbol = new HashMap<>();

        System.out.println(this);
        cashFlowInstances.stream().
                forEach(instance -> {
                    // Update the running cash balance
                    System.out.println("  " + instance);
                    cashBalance.applyChange(instance.getCashFlowDate(), instance.getAmount());

                    System.out.println("  cashBalance = " + cashBalance);
                    // Put the new balance in the current transaction
                    instance.setBalance(cashBalance.getValue());

                    // Store the history cash balance by date
                    cashBalanceAtDate.put(instance.getCashFlowDate(), cashBalance);

                    if (instance instanceof SecurityTransaction) {
                        processSecurityTransaction((SecurityTransaction) instance, cashBalance, shareBalancesBySymbol);
                    }
                });
    }

   private static void readCashFlowInstances(Context context, Company company) throws IOException, ClassNotFoundException {
       AccountReader accountReader = AccountReader.factory(company);

       accountReader.readCashFlowInstances(context, company);
   }

   public static void getAccountHistory(Context context, Collection<Company> companies) {
       for (Company company: companies) {
           try {
               readCashFlowInstances(context, company);
           } catch (ClassNotFoundException cnfe) {
               System.out.println("No reader for " + company.getId() + " skipping.");
           } catch (IOException ioe) {
               ioe.printStackTrace();
           }
       }
   }

   public String toString() {
        return this.accountName + " (" + this.company.getCompanyName() + ")";
   }
}
