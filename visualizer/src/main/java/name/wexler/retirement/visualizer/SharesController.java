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

package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowFrequency.ShareBalance;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowInstance.LiabilityCashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowInstance.SecurityTransaction;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.Tables.CashFlowTableList;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class SharesController {
    private static final String VIEW_INDEX = "index";
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(SharesController.class);
    Retirement retirement = Retirement.getInstance();

    private final Comparator<Map<String, Object>> byTickerCompanyAndAccount = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            int result = 0;
            result = ((String) o1.getOrDefault("ticker", "")).
                    compareTo((String) o2.getOrDefault("ticker", ""));
            if (result == 0)
                result = ((String) o1.getOrDefault("accountCompany", "")).
                        compareTo((String) o2.getOrDefault("accountCompany", ""));
            if (result == 0)
                result = ((String) o1.getOrDefault("accountName", "")).compareTo((String) o2.getOrDefault("accountName", ""));
            return result;
        }
    };

    private List<Map<String, Object>> getShareBalances(Collection<Asset> assets, Function<AssetAccount, Collection<ShareBalance>> shareBalanceMethod) {
        List<Map<String, Object>> shareBalances = new ArrayList<>();
        for (Asset asset : assets) {
            if (asset instanceof AssetAccount) {
                AssetAccount account = (AssetAccount) asset;
                for (ShareBalance shareBalance : shareBalanceMethod.apply(account)) {
                    Map<String, Object> shareBalanceMap = new HashMap<>();
                    shareBalanceMap.put("balanceDate", shareBalance.getBalanceDate());
                    shareBalanceMap.put("shares", shareBalance.getShares());
                    shareBalanceMap.put("ticker", shareBalance.getSecurity().getName());
                    shareBalanceMap.put("sharePrice", shareBalance.getSharePrice());
                    shareBalanceMap.put("shareValue", shareBalance.getShareValue());
                    shareBalanceMap.put("accountName", account.getName());
                    shareBalanceMap.put("accountCompany", account.getCompany().getCompanyName());
                    shareBalances.add(shareBalanceMap);
                }
            }
        }
        Collections.sort(shareBalances, byTickerCompanyAndAccount);
        return shareBalances;
    }

    @RequestMapping(value = "/visualizer/scenario/{scenarioId}/shareBalances/now", method = RequestMethod.GET)
    public ModelAndView retirementSecuritiesNow(@PathVariable String scenarioId, ModelMap model) {
        model.put("scenarioId", scenarioId);
        CashFlowCalendar cashFlowCalendar = retirement.getCashFlowCalendar(scenarioId);
        List<Map<String, Object>> shareBalances =
                getShareBalances(cashFlowCalendar.getAssets(), AssetAccount::getCurrentShareBalances);
        model.put("shareBalances", shareBalances);
        return new ModelAndView("shareBalances", "command",  model);
    }

    @RequestMapping(value = "/visualizer/scenario/{scenarioId}/shareBalances/start", method = RequestMethod.GET)
    public ModelAndView retirementSecuritiesStart(@PathVariable String scenarioId, ModelMap model) {
        model.put("scenarioId", scenarioId);
        CashFlowCalendar cashFlowCalendar = retirement.getCashFlowCalendar(scenarioId);
        List<Map<String, Object>> shareBalances =
                getShareBalances(cashFlowCalendar.getAssets(), AssetAccount::getStartShareBalances);
        model.put("shareBalances", shareBalances);
        return new ModelAndView("shareBalances", "command",  model);
    }
}