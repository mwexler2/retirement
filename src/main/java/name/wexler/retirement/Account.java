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
import name.wexler.retirement.CashFlow.*;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mwexler on 7/9/16.
 */
public class Account extends Asset {

    private AccountSource cashFlowSource;
    private final String accountName;
    private final Company company;

    private final List<ShareBalance> securities;

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
        this.securities = new ArrayList<>();
        company = context.getById(Company.class, companyId);
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

    public List<ShareBalance> getSecurities() {
        return securities;
    }

    private Map<LocalDate, List<Balance>> _getBalanceMap() {
        Map<LocalDate, List<Balance>> balanceMap = new HashMap<>();

        for (ShareBalance shareBalance : getSecurities()) {
            LocalDate balanceDate = shareBalance.getBalanceDate();
            if (!balanceMap.containsKey(balanceDate)) {
                balanceMap.put(balanceDate, new ArrayList<Balance>());
            }
            BigDecimal value = shareBalance.getSharePrice().multiply(shareBalance.getShares());
            balanceMap.get(balanceDate).add(new CashBalance(balanceDate, value));
        }
        return balanceMap;

    }

    public AccountSource getCashFlowSource() {
        return cashFlowSource;
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
}
