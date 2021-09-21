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
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance.NO_ID;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id", "startDate", "endDate", "payee", "payor", "cashFlow" })
public class IncomeTax extends CashFlowEstimator {


    TaxTable taxTable;

    private CashFlowSink defaultSink;
    public static String INCOME_TAX = "Income Tax";
    private static final BigDecimal INCOME_TAX_RATE = BigDecimal.valueOf(0.10);

    @JsonCreator
    public IncomeTax(@JacksonInject("context") Context context,
                     @JsonProperty(value = "id", required = true) String id,
                     @JsonProperty(value = "payee", required = true) String payeeId,
                     @JsonProperty(value = "payors", required = true) List<String> payorIds,
                     @JsonProperty(value = "cashFlow", required = true) String cashFlowId,
                     @JsonProperty(value = "defaultSink", required = true) String defaultSinkId,
                     @JsonProperty(value = "taxTable", required = true) TaxTable taxTable
    ) throws DuplicateEntityException {
        super(context, id, cashFlowId,
                context.getListById(Entity.class, payeeId),
                context.getByIds(Entity.class, payorIds));
        this.defaultSink = context.getById(Asset.class, defaultSinkId);
        this.taxTable = taxTable;
        context.put(IncomeTax.class, id, this);
    }


    @JsonIgnore
    @Override
    public List<CashFlowInstance> getEstimatedFutureCashFlows(CashFlowCalendar cashFlowCalendar) {
        List<CashFlowInstance> incomeTaxCashFlows = getCashFlowFrequency().getFutureCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
                    BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
                    BigDecimal income = calendar.sumMatchingCashFlowForPeriod(accrualStart, accrualEnd,
                            (instance) -> true);
                    try {
                        BigDecimal incomeTax = taxTable.computeTax(accrualEnd.getYear(), income).negate();
                        String description = "Estimated " + this.getName();
                        return new CashFlowInstance(NO_ID, true, this, defaultSink,
                                    getItemType(), getCategory(),
                                    accrualStart, accrualEnd, cashFlowDate, incomeTax, balance, description);
                    } catch (TaxTable.TaxYearNotFoundException tynfe) {
                        return null;
                    }
                });
        return incomeTaxCashFlows;
    }

    @JsonIgnore
    @Override
    public String getName() {
        return this.getId() + " for " + getPayers().stream().map((payer) -> payer.getName()).collect(Collectors.joining(",")) + "/" +
                getPayees().stream().map((payee) -> payee.getName()).collect(Collectors.joining(","));
    }

    @JsonIgnore
    @Override
    public String getItemType() {
        return CashFlowCalendar.ITEM_TYPE.EXPENSE.name();
    }

    @Override
    public boolean isOwner(Entity entity) {
        return this.isPayer(entity);
    }

    @Override
    public String getCategory() {
        return INCOME_TAX;
    }

    @JsonIgnore
    @Override
    public CASH_ESTIMATE_PASS getPass() {
        return CASH_ESTIMATE_PASS.TAXES;   // Need to calculate all income and expenses before computing taxes
    }
}
