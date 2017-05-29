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

import com.fasterxml.jackson.annotation.*;
import name.wexler.retirement.CashFlow.CashFlowInstance;
import name.wexler.retirement.CashFlow.CashFlowType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Created by mwexler on 7/9/16.
 */
@JsonPropertyOrder({ "type", "id", "source", "job", "baseAnnualSalary"})
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Salary.class, name = "salary"),
        @JsonSubTypes.Type(value = BonusAnnualPct.class, name = "bonusAnnualPct"),
        @JsonSubTypes.Type(value = BonusPeriodicFixed.class, name = "bonusPeriodicFixed")})
public abstract class IncomeSource {
    private String id;
    private CashFlowType cashFlow;

    public IncomeSource(@JsonProperty(value = "context", required = true) Context context,
                        @JsonProperty("id") String id,
                        @JsonProperty(value = "cashFlow", required = true) String cashFlowId) throws Exception {
        this.id = id;
        if (context.getById(IncomeSource.class, id) != null)
            throw new Exception("Key " + id + " already exists");
        context.put(IncomeSource.class, id, this);
        this.cashFlow = context.<CashFlowType>getById(CashFlowType.class, cashFlowId);
        if (this.cashFlow == null) {
            throw new Exception("income cashflow " + cashFlowId + " not found");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IncomeSource that = (IncomeSource) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return cashFlow != null ? cashFlow.equals(that.cashFlow) : that.cashFlow == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (cashFlow != null ? cashFlow.hashCode() : 0);
        return result;
    }



    public BigDecimal getMonthlyCashFlow(YearMonth yearMonth, BigDecimal annualAmount) {
        return cashFlow.getMonthlyCashFlow(yearMonth, annualAmount);
    }

    public BigDecimal getMonthlyCashFlow(BigDecimal annualAmount) {
        return cashFlow.getMonthlyCashFlow(annualAmount);
    }

    public abstract BigDecimal getAnnualCashFlow(int year);

    public abstract List<CashFlowInstance> getCashFlowInstances();

    public BigDecimal getAnnualCashFlow() {
        return getAnnualCashFlow(LocalDate.now().getYear());
    }

    abstract public String getName();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public CashFlowType getCashFlow() {
        return cashFlow;
    }

    @JsonProperty("cashFlow")
    public String getGetFlowId() {
        return cashFlow.getId();
    }

}
