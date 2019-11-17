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

package name.wexler.retirement.visualizer.Asset;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.CashFlowInstance.Account;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.CashFlowFrequency.Balance;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowInstance.SecurityTransaction;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.CashFlowFrequency.ShareBalance;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mwexler on 7/9/16.
 */
public class AssetAccount extends Asset implements Account {
    private static final List<AssetAccount> accounts = new ArrayList<>();

    private final String accountName;
    private final Company company;

    // History of balances for Cash and Securities
    private final Map<LocalDate, Map<String, ShareBalance>> shareBalancesByDateAndSymbol = new HashMap<>();
    private final Map<LocalDate, CashBalance> accountValueByDate = new HashMap<>();

    private static final String assetAccountsPath = "assetAccounts.json";

    static public void readAssetAccounts(Context context) throws IOException {
        context.fromJSONFileList(Asset[].class, assetAccountsPath);
    }

    @JsonIgnore
    private final List<CashFlowInstance> cashFlowInstances = new ArrayList<>();

    public boolean isOwner(Entity entity) {
        return this.getOwners().contains(entity);
    }

    public class NotFoundException extends Exception {
        public NotFoundException(Class c, String id) {
            super(c.getSimpleName() + ": " + id + " not found");
        }
    }

    @JsonCreator
    public AssetAccount(@JacksonInject("context") Context context,
                        @JsonProperty(value = "id", required = true) String id,
                        @JsonProperty(value = "owners", required = true) List<String> ownerIds,
                        @JsonProperty(value = "initialBalance", defaultValue = "0.00") CashBalance initialBalance,
                        @JsonProperty(value = "interimBalances", required = true) List<CashBalance> interimBalances,
                        @JsonProperty(value = "accountName", required = true) String accountName,
                        @JsonProperty(value = "company", required = true) String companyId,
                        @JsonProperty(value = "indicators", required = true) List<String> indicators)
            throws NotFoundException, DuplicateEntityException {
        super(context, id, ownerIds, initialBalance, interimBalances);
        this.accountName = accountName;
        this.company = context.getById(Entity.class, companyId);
        if (this.company == null) {
            throw new NotFoundException(Company.class, companyId);
        }
        for (String indicator : indicators) {
            context.put(AssetAccount.class, indicator, this);
        }
        accounts.add(this);
    }

    public String getId() {
        return super.getId();
    }

    public String getName() {
        return accountName;
    }

    public Company getCompany() {
        return company;
    }

    // Return the total value of securities and cash at each point during the year where it cash or share quantity
    // changed.
    @Override
    @JsonIgnore
    public List<Balance> getBalances(Scenario scenario, int year) {
        this.cashFlowInstances.sort(Comparator.comparing(CashFlowInstance::getCashFlowDate));
        computeBalances(this.cashFlowInstances);
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
        return cashValue.add(shareValue).setScale(2, RoundingMode.HALF_UP);
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


   public String toString() {
        return this.accountName + " (" + this.company.getCompanyName() + ")";
   }

   @Override public void sourceCashFlowInstance(CashFlowInstance cashFlowInstance) { }

   @Override public void sinkCashFlowInstance(CashFlowInstance cashFlowInstance) { }

   @Override
    public String getItemType() {
        return AssetAccount.class.getSimpleName();
   }
}
