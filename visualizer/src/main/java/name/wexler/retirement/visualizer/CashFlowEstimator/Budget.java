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

package name.wexler.retirement.visualizer.CashFlowEstimator;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Category;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.JSON.JSONDateDeserialize;
import name.wexler.retirement.visualizer.JSON.JSONDateSerialize;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance.NO_ID;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id", "source", "security", "startDate", "endDate", "paymentAmount" })
public class Budget extends CashFlowEstimator {
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private final LocalDate endDate;
    private BigDecimal paymentAmount;
    private BigDecimal periodsPerYear = BigDecimal.valueOf(12);
    private CashFlowSink defaultSink;
    Map<String, Boolean> skipGroupings = new HashMap<>();
    Map<String, Boolean> skipParentCategories = new HashMap<>();
    Map<String, Boolean> skipCategories = new HashMap<>();

    @JsonCreator
    public Budget(@JacksonInject("context") Context context,
                  @JsonProperty(value = "id",              required = true) String id,
                  @JsonProperty("endDate") LocalDate endDate,
                  @JsonProperty(value = "paymentAmount",   required = true) BigDecimal paymentAmount,
                  @JsonProperty(value = "source",          required = true) String sourceId,
                  @JsonProperty(value = "defaultSink",   required = true) String defaultSourceId,
                  @JsonProperty(value = "skipGroupings",   required = true) List<String> skipGroupings,
                  @JsonProperty(value = "skipParentCategories",   required = true)List<String> skipParentCategories,
                  @JsonProperty(value = "skipCategories",   required = true)List<String> skipCategories)
    throws DuplicateEntityException {
        super(context, id, sourceId,
                Collections.emptyList(),
                Collections.emptyList());
        this.endDate = endDate;
        this.paymentAmount = paymentAmount;
        this.defaultSink = context.getById(Asset.class, defaultSourceId);
        skipGroupings.forEach(grouping -> this.skipGroupings.put(grouping, true));
        skipParentCategories.forEach(parentCategory -> this.skipParentCategories.put(parentCategory, true));
        skipCategories.forEach(category -> this.skipCategories.put(category, true));
        context.put(Budget.class, id, this);
    }

    private boolean skipBudget(name.wexler.retirement.visualizer.Budget budget) {
        if (skipGroupings.containsKey(budget.getGrouping()))
            return true;
        if (skipParentCategories.containsKey(budget.getParentCategory()))
            return true;
        if (skipCategories.containsKey(budget.getCategory()))
            return true;
        return false;
    }

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getEstimatedFutureCashFlows(CashFlowCalendar cashFlowCalendar) {
        List<CashFlowInstance> allInstances = new ArrayList<>();

        for (name.wexler.retirement.visualizer.Budget budget : cashFlowCalendar.getBudgets()) {
            if (skipBudget(budget))
                continue;
            List<CashFlowInstance> cashFlowInstances =
                    getCashFlowFrequency().getFutureCashFlowInstances(cashFlowCalendar, this,
                            (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                                BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                                String description = "Monthly budget for " + budget.getCategory();
                                BigDecimal amount = budget.getBudget();
                                if (budget.getItemType().equals(Category.EXPENSE))
                                    amount = amount.negate();
                                CashFlowInstance instance = new CashFlowInstance(NO_ID, true, this, defaultSink,
                                        budget.getItemType(), budget.getCategory(),
                                        accrualStart, accrualEnd, cashFlowDate, amount, balance, description);
                                return instance;
                            });
            allInstances.addAll(cashFlowInstances);
        };
        return allInstances;
    }


    @JsonIgnore
    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @JsonProperty(value = "source")
    public String getSourceId() {
        return this.getCashFlowFrequency().getId();
    }


    public LocalDate getEndDate() { return endDate; }

    @JsonIgnore
    @Override
    public String getItemType() {
        return CashFlowCalendar.ITEM_TYPE.INCOME.name();
    }

    @Override
    public boolean isOwner(Entity entity) {
        return this.isPayee(entity);
    }
}
