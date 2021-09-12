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

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Context;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockOption extends EquityCompensation {
    BigDecimal strikePrice;

    public StockOption(@JacksonInject("context") Context context,
                       @JsonProperty(value = "id", required = true) String id,
                       @JsonProperty(value = "job", required = true) String jobId,
                       @JsonProperty(value = "cashFlow", required = true) String cashFlowId,
                       @JsonProperty(value = "security", required = true) String securityId,
                       @JsonProperty(value = "totalShares", required = true) int totalShares,
                       @JsonProperty(value = "strikePrice", required = true) BigDecimal strikePrice) throws DuplicateEntityException {
        super(context, id, jobId, cashFlowId, securityId, totalShares);
        this.strikePrice = strikePrice;
    }


    @JsonIgnore
    public String getName() {
        return getJob().getName() + " Stock Option";
    }

    @JsonIgnore
    @Override
    public List<CashFlowInstance> getEstimatedFutureCashFlows(CashFlowCalendar cashFlowCalendar) {
        return getCashFlowFrequency().getFutureCashFlowInstances(cashFlowCalendar, this,
                (calendar, cashFlowId, accrualStart, accrualEnd, cashFlowDate, percent, prevCashFlowInstance) -> {
            BigDecimal sharePrice = getSecurity().getSharePriceAtDate(accrualEnd, calendar.getAssumptions());
            BigDecimal shares = BigDecimal.valueOf(getTotalShares()).multiply(percent);
            BigDecimal amount = sharePrice.subtract(strikePrice).multiply(shares);
            BigDecimal balance = (prevCashFlowInstance == null) ? BigDecimal.ZERO : prevCashFlowInstance.getCashBalance();
            return new CashFlowInstance(true, this, getJob().getDefaultSink(),
                    this.getItemType(), getCategory(), accrualStart, accrualEnd, cashFlowDate, amount, balance);
        });
    }

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
