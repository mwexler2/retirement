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

import name.wexler.retirement.visualizer.CashFlowFrequency.Balance;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowInstance.LiabilityCashFlowInstance;
import name.wexler.retirement.datastore.DataStore;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RetirementController {

    private static final String VIEW_INDEX = "index";
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(RetirementController.class);


    @RequestMapping(value = "/visualizer/scenario/{scenarioId}/asset/{assetId}/year/{year}", method = RequestMethod.GET)
    public ModelAndView retirementAsset(@PathVariable String assetId,
                                        @PathVariable String scenarioId,
                                        @PathVariable int year,
                                        ModelMap model) {

        Retirement retirement = new Retirement();
        model.put("assetId", assetId);
        model.put("scenarioId", scenarioId);
        model.put("year", year);
        List<CashFlowInstance> selectedCashFlows =
                retirement.getCashFlowCalendar(scenarioId).getCashFlowInstances().stream().
                        filter(instance -> instance.getCashFlowSinkId().equals(assetId)).
                        filter(instance -> instance.getYear() == year).
                        collect(Collectors.toList());
        model.put("cashFlows", selectedCashFlows);
        return new ModelAndView("asset", "command", model);
    }

    @RequestMapping(value = "/visualizer/scenario/{scenarioId}/asset/{assetId}", method = RequestMethod.GET)
    public ModelAndView retirementAsset(@PathVariable String assetId, @PathVariable String scenarioId, ModelMap model) {
        Retirement retirement = new Retirement();
        model.put("assetId", assetId);
        model.put("scenarioId", scenarioId);
        List<CashFlowInstance> selectedCashFlows =
                retirement.getCashFlowCalendar(scenarioId).getCashFlowInstances().stream().
                        filter(instance -> instance.getCashFlowSinkId().equals(assetId)).
                        collect(Collectors.toList());
        model.put("cashFlows", selectedCashFlows);
        return new ModelAndView("asset", "command", model);
    }

    @RequestMapping(value = "/visualizer/scenario/{scenarioId}/liability/{liabilityId}/year/{year}", method = RequestMethod.GET)
    public ModelAndView retirementLiability(@PathVariable String liabilityId,
                                            @PathVariable String scenarioId,
                                            @PathVariable int year,
                                            ModelMap model) {
        Retirement retirement = new Retirement();
        model.put("cashFlowId", liabilityId);
        model.put("scenarioId", scenarioId);
        model.put("year", year);
        List<LiabilityCashFlowInstance> cashFlowInstances =
                retirement.getCashFlowCalendar(scenarioId).
                        getLiabilityCashFlowInstances(liabilityId, year);
        model.put("cashFlows", cashFlowInstances);
        return new ModelAndView("cashFlows", "command", model);
    }

    @RequestMapping(value = "/visualizer/scenario/{scenarioId}/liability/{liabilityId}", method = RequestMethod.GET)
    public ModelAndView retirementLiability(@PathVariable String liabilityId,
                                            @PathVariable String scenarioId,
                                            ModelMap model) {
        Retirement retirement = new Retirement();
        model.put("cashFlowId", liabilityId);
        model.put("scenarioId", scenarioId);
        List<LiabilityCashFlowInstance> cashFlowInstances = retirement.getCashFlowCalendar(scenarioId).getLiabilityCashFlowInstances(liabilityId);
        model.put("cashFlows", cashFlowInstances);
        return new ModelAndView("cashFlows", "command", model);
    }

    @RequestMapping(value = "/visualizer/scenario/{scenarioId}/{grouping}/{category}/year/{year}", method = RequestMethod.GET)
    public ModelAndView retirementExpensesByCategory(@PathVariable String category,
                                            @PathVariable String scenarioId,
                                            @PathVariable int year,
                                            @PathVariable String grouping,
                                            ModelMap model) {
        Retirement retirement = new Retirement();
        model.put("category", category);
        model.put("scenarioId", scenarioId);
        List<CashFlowInstance> selectedCashFlows =
                retirement.getCashFlowCalendar(scenarioId).getCashFlowInstances().stream().
                        filter(instance -> instance.getCategory().equals(category)).
                        filter(instance -> instance.getCashFlowDate().getYear() == year).
                        sorted().
                        collect(Collectors.toList());
        model.put("cashFlows", selectedCashFlows);
        return new ModelAndView("cashFlows", "command", model);
    }

    @RequestMapping(value = "/visualizer/scenario/{scenarioId}/{grouping}/{category}", method = RequestMethod.GET)
    public ModelAndView retirementExpensesByCategory(@PathVariable String category,
                                                      @PathVariable String scenarioId,
                                                      @PathVariable String grouping,
                                                      ModelMap model) {
        Retirement retirement = new Retirement();
        model.put("category", category);
        model.put("scenarioId", scenarioId);
        List<CashFlowInstance> selectedCashFlows =
                retirement.getCashFlowCalendar(scenarioId).getCashFlowInstances().stream().
                        filter(instance -> instance.getCategory().equals(category)).
                        sorted().
                        collect(Collectors.toList());
        model.put("cashFlows", selectedCashFlows);
        return new ModelAndView("cashFlows", "command", model);
    }


    @RequestMapping(value = "/visualizer", method = RequestMethod.GET)
    public ModelAndView retirement(ModelMap model) {
        return new ModelAndView("retirement", "command", new Retirement());
    }

    @RequestMapping(value = "/visualizer/{name}", method = RequestMethod.GET)
    public String updateRetirement(@PathVariable String name, ModelMap model) {
        logger.debug("[updateRetirement]");
        return VIEW_INDEX;

    }

}