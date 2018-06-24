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

import name.wexler.retirement.CashFlow.CashFlowInstance;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RetirementController {

    private static final String VIEW_INDEX = "index";
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(RetirementController.class);

    @RequestMapping(value = "/retirement/scenario/{scenarioId}/cashflow/{cashFlowId}/year/{year}", method = RequestMethod.GET)
    public ModelAndView retirementCashFlow(@PathVariable String cashFlowId,
                                           @PathVariable String scenarioId,
                                           @PathVariable int year,
                                           ModelMap model) {
        Retirement retirement = new Retirement();
        model.put("cashFlowId", cashFlowId);
        model.put("scenarioId", scenarioId);
        model.put("year", year);
        List<CashFlowInstance> cashFlows = retirement.getCashFlows(scenarioId, cashFlowId, year);
        model.put("cashFlows", cashFlows);
        return new ModelAndView("yearCashFlows", model);
    }

    @RequestMapping(value = "/retirement/scenario/{scenarioId}/cashflow/{cashFlowId}", method = RequestMethod.GET)
    public ModelAndView retirementCashFlow(@PathVariable String cashFlowId, @PathVariable String scenarioId, ModelMap model) {
        Retirement retirement = new Retirement();
        model.put("cashFlowId", cashFlowId);
        model.put("scenarioId", scenarioId);
        List<CashFlowInstance> cashFlows = retirement.getCashFlows(scenarioId, cashFlowId);
        model.put("cashFlows", cashFlows);
        return new ModelAndView("cashFlows", model);
    }

    @RequestMapping(value = "/retirement", method = RequestMethod.GET)
    public ModelAndView retirement(ModelMap model) {
        return new ModelAndView("retirement", "command", new Retirement());
    }

    @RequestMapping(value = "/retirement/{name}", method = RequestMethod.GET)
    public String updateRetirement(@PathVariable String name, ModelMap model) {
        logger.debug("[updateRetirement]");
        return VIEW_INDEX;

    }

}