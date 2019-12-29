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
import name.wexler.retirement.datastore.PositionHistory;
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
    private BigDecimal cashBalance = BigDecimal.ZERO;

    // History of balances for Cash and Securities
    private final Map<LocalDate, Map<String, ShareBalance>> shareBalancesByDateAndSymbol = new HashMap<>();
    private final Map<String, ShareBalance> shareBalancesBySymbol = new HashMap<>();
    private final Map<LocalDate, CashBalance> accountValueByDate = new HashMap<>();
    private String accountId = null;
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
                        @JsonProperty(value = "accountName", required = true) String accountName,
                        @JsonProperty(value = "company", required = true) String companyId,
                        @JsonProperty(value = "indicators", required = true) List<String> indicators,
                        @JsonProperty(value = "accountId") String accountId)
            throws NotFoundException, DuplicateEntityException {
        super(context, id, ownerIds);
        this.accountName = accountName;
        this.company = context.getById(Entity.class, companyId);
        if (this.company == null) {
            throw new NotFoundException(Company.class, companyId);
        }
        for (String indicator : indicators) {
            context.put(AssetAccount.class, indicator, this);
        }
        this.accountId = accountId;
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

    public String getAccountId() { return accountId; }

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

    private BigDecimal calculateAssetValue() {
        BigDecimal assetValue = shareBalancesBySymbol
                .values()
                .stream()
                .map(ShareBalance::getValue)
                .reduce(BigDecimal.ZERO,
                        (a, b) -> a.add(b));
        return assetValue;
    }

   public String toString() {
        return this.accountName + " (" + this.company.getCompanyName() + ")";
   }

   @Override public void sourceCashFlowInstance(CashFlowInstance cashFlowInstance) { }


   @Override
    public String getItemType() {
        return AssetAccount.class.getSimpleName();
   }

    private void setPositions(Map<String, PositionHistory.Position> positions) {
        for (Map.Entry<String, PositionHistory.Position> positionEntry : positions.entrySet()) {
            // Update running share balance for this symbol, creating an entry if it doesn't already exist.

            String symbol = positionEntry.getKey();
            ShareBalance shareBalance = new ShareBalance(getContext(), positionEntry.getValue());
            if (!shareBalancesBySymbol.containsKey(symbol))
                shareBalancesBySymbol.put(symbol, shareBalance);

            // Store the share balance by date and symbol
            if (!shareBalancesByDateAndSymbol.containsKey(shareBalance.getBalanceDate())) {
                shareBalancesByDateAndSymbol.put(shareBalance.getBalanceDate(),
                        new HashMap<>());
            }
            shareBalancesByDateAndSymbol.get(shareBalance.getBalanceDate()).put(symbol, shareBalance);
        }
    }

    public void setRunningTotal(BigDecimal runningTotal, Map<String, PositionHistory.Position> positions) {
        this.setPositions(positions);
        BigDecimal shareValue = shareBalancesBySymbol
                .values()
                .stream()
                .map(ShareBalance::getValue)
                .reduce(BigDecimal.ZERO,
                        (a, b) -> a.add(b));
        this.cashBalance = runningTotal.subtract(shareValue);
    }

    @Override
    @JsonIgnore
    public void updateRunningTotal(CashFlowInstance cashFlowInstance) {
        cashFlowInstance.setCashBalance(cashBalance);
        cashFlowInstance.setAssetBalance(calculateAssetValue());
        cashBalance = cashBalance.add(cashFlowInstance.getAmount());
        if (cashFlowInstance instanceof SecurityTransaction) {
            SecurityTransaction securityTransaction = (SecurityTransaction) cashFlowInstance;
            ShareBalance shareBalanceChange = securityTransaction.getChange();
            String symbol = shareBalanceChange.getSecurity().getName();
            ShareBalance currentBalance = shareBalancesBySymbol.getOrDefault(symbol, new ShareBalance(
                    cashFlowInstance.getCashFlowDate(), BigDecimal.ZERO, BigDecimal.ZERO, shareBalanceChange.getSecurity()));
            ShareBalance newBalance = currentBalance.applyChange(shareBalanceChange);
            shareBalancesBySymbol.put(symbol, newBalance);
        }
    }

    public Collection<ShareBalance> getCurrentShareBalances() {
        return shareBalancesBySymbol.values();
    }
}
