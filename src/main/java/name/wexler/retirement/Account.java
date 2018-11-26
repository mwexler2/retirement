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

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.CashFlow.Balance;
import name.wexler.retirement.CashFlow.CashBalance;
import name.wexler.retirement.CashFlow.ShareBalance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by mwexler on 7/9/16.
 */
public class Account extends Asset {
    private final String id;
    private final String accountName;
    private final String institutionName;

    private final List<ShareBalance> securities;

    @JsonCreator
    public Account(@JacksonInject("context") Context context,
                      @JsonProperty(value = "id", required = true) String id,
                      @JsonProperty(value = "owners", required = true) List<String> ownerIds,
                      @JsonProperty(value = "initialBalance", defaultValue = "0.00") CashBalance initialBalance,
                      @JsonProperty(value = "interimBalances", required = true) List<CashBalance> interimBalances,
                      @JsonProperty(value = "accountName", required = true) String accountName,
                      @JsonProperty(value = "institutionName", required = true) String institutionName,
                      @JsonProperty(value = "securities", required = true) List<ShareBalance> securities) {
        super(context, id, ownerIds, initialBalance, interimBalances);
        this.id = id;
        context.put(Account.class, id, this);
        this.accountName = accountName;
        this.institutionName = institutionName;
        this.securities = securities;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return accountName;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public List<ShareBalance> getSecurities() {
        return securities;
    }

    @Override @JsonIgnore
    public List<Balance> getBalances() {
        List<Balance> balances = new ArrayList<>();
        Map<LocalDate, List<Balance>> balanceMap = new HashMap<>();

        for (ShareBalance shareBalance : getSecurities()) {
            LocalDate balanceDate = shareBalance.getBalanceDate();
            if (!balanceMap.containsKey(balanceDate)) {
                balanceMap.put(balanceDate, new ArrayList<Balance>());
            }
            BigDecimal value = shareBalance.getSharePrice().multiply(shareBalance.getShares());
            balanceMap.get(balanceDate).add(new CashBalance(balanceDate, value));
        }

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
}
