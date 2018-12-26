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
import java.time.Month;
import java.util.*;

/**
 * Created by mwexler on 7/9/16.
 */
public class Account extends Asset {

    private AccountSource cashFlowSource;
    private final String accountName;
    private final Company company;
    private static final String accountsPath = "accounts.json";
    @JsonIgnore
    private final Map<String, List<ShareBalance>> securities;
    private List<CashFlowInstance> cashFlowInstances;

    static public void readAccounts(Context context) throws IOException {
        List<Account> accounts = context.fromJSONFileList(Account[].class, accountsPath);
        // String securityTxnsPath = "/securityTxn.json";
        // this.securityTxns = context.fromJSONFileList(SecurityTransaction[].class, securityTxnsPath);
        getAccountHistory(accounts);
        return;

    }

    @JsonCreator
    public Account(@JacksonInject("context") Context context,
                      @JsonProperty(value = "id", required = true) String id,
                      @JsonProperty(value = "owners", required = true) List<String> ownerIds,
                      @JsonProperty(value = "initialBalance", defaultValue = "0.00") CashBalance initialBalance,
                      @JsonProperty(value = "interimBalances", required = true) List<CashBalance> interimBalances,
                      @JsonProperty(value = "accountName", required = true) String accountName,
                      @JsonProperty(value = "company", required = true) String companyId) {
        super(context, id, ownerIds, initialBalance, interimBalances);
        context.put(Account.class, id, this);
        this.accountName = accountName;
        this.securities = new HashMap<>();
        company = context.getById(Entity.class, companyId);
        List<Entity> owners = context.getByIds(Entity.class, ownerIds);
        cashFlowSource = context.getById(CashFlowSource.class, id);
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

    @JsonIgnore
    public Map<String, List<ShareBalance>> getSecurities() {
        return securities;
    }

    private Map<LocalDate, List<Balance>> _getBalanceMap() {
        Map<LocalDate, List<Balance>> balanceMap = new HashMap<>();

        for (List<ShareBalance> securityBalanceList: getSecurities().values()) {
            for (ShareBalance shareBalance : securityBalanceList) {
                LocalDate balanceDate = shareBalance.getBalanceDate();
                if (!balanceMap.containsKey(balanceDate)) {
                    balanceMap.put(balanceDate, new ArrayList<Balance>());
                }
                BigDecimal value = shareBalance.getSharePrice().multiply(shareBalance.getShares());
                balanceMap.get(balanceDate).add(new CashBalance(balanceDate, value));
            }
        }
        return balanceMap;

    }

    public AccountSource getCashFlowSource() {
        return cashFlowSource;
    }

    public Company getCompany() {
        return company;
    }

    @Override @JsonIgnore
    public List<Balance> getBalances() {
        List<Balance> balances = new ArrayList<>();
        Map<LocalDate, List<Balance>> balanceMap = _getBalanceMap();

        for (LocalDate date : balanceMap.keySet()) {
            List<Balance> dateBalances = balanceMap.get(date);
            BigDecimal total = BigDecimal.ZERO;
            for (Balance balance : dateBalances) {
                total = total.add(balance.getValue());
            }
            balances.add(new CashBalance(date, total));
        }
        return balances;
    }

    @Override
    @JsonIgnore
    public List<Balance> getBalances(int year) {
        List<Balance> balances = new ArrayList<>();
        Map<LocalDate, List<Balance>> balanceMap = _getBalanceMap();

        LocalDate lastDateInYear = balanceMap.keySet()
                .stream()
                .filter(balance -> year == balance.getYear())
                .max(LocalDate::compareTo)
                .orElse(LocalDate.of(year, Month.JANUARY, 1));

        List<Balance> dateBalances = balanceMap.get(lastDateInYear);

        return dateBalances;
    }

    /* public BigDecimal getAccountValue(LocalDate date, Assumptions assumptions) {
        BigDecimal result = this.getBalanceAtDate(date).getValue();

        Map<String, ShareBalance> shareBalances = new HashMap<>();
        for (ShareBalance security : getSecurities()) {
            shareBalances.put(security.getId(), security);
        }
        for (ShareBalance security : shareBalances.values()) {
            result = result.add(security.getValue());
        }
        return result;
    } */

   private void readCashFlowInstances() throws ClassNotFoundException {
       AccountReader accountReader = AccountReader.factory(this);

       cashFlowInstances = accountReader.readCashFlowInstances(this);

       for (CashFlowInstance instance: cashFlowInstances) {
           if (instance instanceof SecurityTransaction) {
               SecurityTransaction txn = (SecurityTransaction) instance;
               String id = txn.getChange().getSecurity().getId();
               ShareBalance lastBalance;
               List<ShareBalance> shareBalanceList;
               if (!securities.containsKey(id)) {
                   shareBalanceList = new ArrayList<>();
                   securities.put(id, shareBalanceList);
                   lastBalance = txn.getChange();
               } else {
                   shareBalanceList = securities.get(id);
                   ShareBalance prevBalance = shareBalanceList.get(shareBalanceList.size() - 1);
                   BigDecimal totalShares = prevBalance.getShares().add(txn.getChange().getShares());
                   lastBalance = new ShareBalance(
                           this.getContext(), txn.getAccrualStart(), totalShares, txn.getChange().getSharePrice(),
                           txn.getChange().getSecurity().getId());
               }
               shareBalanceList.add(lastBalance);
           }
       }
   }

   public static void getAccountHistory(List<Account> accounts) {
       for (Account account: accounts) {
           try {
               account.readCashFlowInstances();
           } catch (ClassNotFoundException cnfe) {
               System.out.println("No reader for " + account.getCompany().getId() + " skipping.");
           }
       }
   }
}
