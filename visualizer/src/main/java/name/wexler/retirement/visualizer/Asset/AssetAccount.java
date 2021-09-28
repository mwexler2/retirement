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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mwexler on 7/9/16.
 */
public class AssetAccount extends Asset implements Account {
    private static final List<AssetAccount> accounts = new ArrayList<>();

    private final String accountName;
    private final Company company;
    private final String txnSource;
    private Balance cashBalance = new CashBalance(LocalDate.ofEpochDay(0), BigDecimal.ZERO);

    // History of balances for Cash and Securities
    private final Map<LocalDate, Map<String, ShareBalance>> shareBalancesByDateAndSymbol = new HashMap<>();
    private final Map<String, ShareBalance> startingShareBalancesBySymbol = new HashMap<>();
    private final Map<String, ShareBalance> currentShareBalancesBySymbol = new HashMap<>();
    private final Map<String, ShareBalance> runningShareBalancesBySymbol = new HashMap<>();

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
                        @JsonProperty(value = "accountId") String accountId,
                        @JsonProperty(value = "txnSource", defaultValue = AccountReader.mintTxnSource) String txnSource)
            throws NotFoundException, DuplicateEntityException {
        super(context, id, ownerIds, null, Collections.EMPTY_LIST);
        this.accountName = accountName;
        this.company = context.getById(Entity.class, companyId);
        this.txnSource = txnSource;
        if (this.company == null) {
            throw new NotFoundException(Company.class, companyId);
        }
        for (String indicator : indicators) {
            context.put(AssetAccount.class, indicator, this);
        }
        context.put(AssetAccount.class, accountId, this);
        this.accountId = accountId;
        accounts.add(this);
    }

    @Override
    public String getTxnSource() {
        return txnSource;
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

    private BigDecimal calculateAssetValue(Map<String, ShareBalance> shareBalancesBySymbol) {
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



   @Override
    public String getItemType() {
        return AssetAccount.class.getSimpleName();
   }

    private void setPositions(Map<String, PositionHistory.Position> positions) {
        for (Map.Entry<String, PositionHistory.Position> positionEntry : positions.entrySet()) {
            // Update running share balance for this symbol, creating an entry if it doesn't already exist.

            String symbol = positionEntry.getKey();
            ShareBalance shareBalance = new ShareBalance(getContext(), positionEntry.getValue());
            if (!currentShareBalancesBySymbol.containsKey(symbol))
                currentShareBalancesBySymbol.put(symbol, shareBalance);

            // Store the share balance by date and symbol
            if (!shareBalancesByDateAndSymbol.containsKey(shareBalance.getBalanceDate())) {
                shareBalancesByDateAndSymbol.put(shareBalance.getBalanceDate(),
                        new HashMap<>());
            }
            shareBalancesByDateAndSymbol.get(shareBalance.getBalanceDate()).put(symbol, shareBalance);
        }
        runningShareBalancesBySymbol.putAll(currentShareBalancesBySymbol);
    }

    public void setStartingBalance() {
        startingShareBalancesBySymbol.putAll(currentShareBalancesBySymbol);
        this.prependBalance(this.cashBalance);
    }

    public void setRunningTotal(LocalDate balanceDate, BigDecimal runningTotal, Map<String, PositionHistory.Position> positions) {
        this.setPositions(positions);
        BigDecimal shareValue = currentShareBalancesBySymbol
                .values()
                .stream()
                .map(ShareBalance::getValue)
                .reduce(BigDecimal.ZERO,
                        (a, b) -> a.add(b));
        this.cashBalance = new CashBalance(balanceDate, runningTotal.subtract(shareValue));
    }

    @Override
    @JsonIgnore
    public void updateRunningTotal(CashFlowInstance cashFlowInstance, boolean negate) {
        cashFlowInstance.setCashBalance(cashBalance.getValue());
        cashFlowInstance.setAssetBalance(calculateAssetValue(runningShareBalancesBySymbol));
        BigDecimal cashFlowAmount = cashFlowInstance.getAmount();
        if (negate)
            cashFlowAmount = cashFlowAmount.negate();
        cashBalance = new CashBalance(cashFlowInstance.getCashFlowDate(), cashBalance.getValue().add(cashFlowAmount));
        if (cashFlowInstance instanceof SecurityTransaction) {
            SecurityTransaction securityTransaction = (SecurityTransaction) cashFlowInstance;
            ShareBalance shareBalanceChange = securityTransaction.getChange();
            String symbol = shareBalanceChange.getSecurity().getName();
            ShareBalance currentBalance = runningShareBalancesBySymbol.getOrDefault(symbol, new ShareBalance(
                    cashFlowInstance.getCashFlowDate(), BigDecimal.ZERO, BigDecimal.ZERO, shareBalanceChange.getSecurity()));
            ShareBalance newBalance = currentBalance.applyChange(shareBalanceChange, negate);
            runningShareBalancesBySymbol.put(symbol, newBalance);
        }
    }

    public Collection<ShareBalance> getCurrentShareBalances() {
        return currentShareBalancesBySymbol.values();
    }

    public Collection<ShareBalance> getStartShareBalances() {
        return startingShareBalancesBySymbol.values();
    }

    @Override
    public CashFlowInstance processSymbol(Context context, String symbol, String description, String category, String itemType,
                                          BigDecimal shares, LocalDate txnDate, BigDecimal txnAmount) {
        CashFlowInstance instance = null;
        AssetAccount assetAccount = (AssetAccount) this;
        BigDecimal sharePrice = null;
        Security security;
        if (symbol == null || symbol.equals("")) {
            Pattern p1 = Pattern.compile("(\\d+(\\.\\d+)?) of ([^ ]+) @ \\$(\\d+\\.\\d+).*", Pattern.CASE_INSENSITIVE);
            Pattern p2 = Pattern.compile("YOU BOUGHT ([^ ]+).*");
            Matcher m = p1.matcher(description.replace(",", ""));
            if (m.matches()) {
                shares = BigDecimal.valueOf(Double.parseDouble(m.group(1)));
                if (category.equals("Buy"))
                    shares = shares.negate();
                symbol = m.group(3);
                sharePrice = BigDecimal.valueOf(Double.parseDouble(m.group(4)));
            } else {
                m = p2.matcher(description);
                if (m.matches())
                    symbol = m.group(1);
            }
        }
        if (symbol == null || symbol.equals("")) {
            System.err.println("No security specified in " + description);
            return null;
        } else {
            security = context.getById(Security.class, symbol);
            try {
                if (security == null)
                    security = new Security(context, symbol);
            } catch (Entity.DuplicateEntityException dee) {
                throw(new RuntimeException(dee));
            }
            if (sharePrice == null) {
                if (shares == null || shares.compareTo(BigDecimal.ZERO) == 0) {
                    shares = BigDecimal.ZERO;
                    sharePrice = BigDecimal.ZERO;
                } else {
                    sharePrice = txnAmount.divide(shares, 2, RoundingMode.HALF_UP).abs();
                }
            }
            ShareBalance shareChange = new ShareBalance(txnDate, shares, sharePrice, security);
            instance = new SecurityTransaction(context, assetAccount, itemType, category, txnAmount, shareChange,
                    description);
        }
        return instance;
    }

    @Override
    public void setRunningTotal(LocalDate balanceDate, BigDecimal value) {
        this.cashBalance = new CashBalance(balanceDate, value);
    }
}
