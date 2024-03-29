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
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.visualizer.CashFlowSource;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowFrequency.Balance;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
        @JsonSubTypes.Type(value = BonusPeriodicFixed.class, name = "bonusPeriodicFixed"),
        @JsonSubTypes.Type(value = Rent.class, name="rent"),
        @JsonSubTypes.Type(value = SecuredLoan.class, name = "securedLoan"),
        @JsonSubTypes.Type(value = CreditCardAccount.class, name = "creditCard"),
        @JsonSubTypes.Type(value = Alimony.class, name = "alimony"),
        @JsonSubTypes.Type(value = IncomeTax.class, name = "incomeTax"),
        @JsonSubTypes.Type(value = RSU.class, name="RSU"),
        @JsonSubTypes.Type(value = StockOption.class, name="stockOption"),
        @JsonSubTypes.Type(value = Budget.class, name="budgets")
})
public abstract class CashFlowEstimator extends Entity implements CashFlowSource {
    private static final List<CashFlowEstimator> cashFlowEstimators = new ArrayList<>();
    private final List<Entity> payers;
    private final List<Entity> payees;
    private final CashFlowFrequency cashFlow;
    private static final String cashFlowSourcesPath = "cashFlowSources.json";

    static public void readCashFlowSources(Context context) throws IOException {
        context.fromJSONFileList(CashFlowEstimator[].class, cashFlowSourcesPath);
    }

    public CashFlowEstimator(@JsonProperty(value = "context", required = true) Context context,
                             @JsonProperty("id") String id,
                             @JsonProperty(value = "cashFlow", required = true) String cashFlowId,
                             List<Entity> payees, List<Entity> payers)
            throws NoSuchElementException, IllegalArgumentException, DuplicateEntityException {
        super(context, id, CashFlowEstimator.class);
        this.payees = payees;
        this.payers = payers;
        this.cashFlow = context.getById(CashFlowFrequency.class, cashFlowId);
        if (this.cashFlow == null) {
            throw new NoSuchElementException("CashFlowFrequency " + cashFlowId + " not found");
        }
        cashFlowEstimators.add(this);
    }

    protected List<CashFlowEstimator> getCashFlowEstimators() {
        return cashFlowEstimators;
    }

    public String getCategory() {
        return getClass().getSimpleName();
    }

    @JsonIgnore
    private LocalDate getStartDate() {
        return cashFlow.getAccrueStart();
    }

    @JsonIgnore
    public LocalDate getEndDate() {
        return cashFlow.getAccrueEnd();
    }

    public boolean isPayer(Entity payer) {
        return payers.contains(payer);
    }

    public boolean isPayee(Entity payee) {
        return payees.contains(payee);
    }

    @JsonIgnore
    public List<Entity> getPayers() {
        return payers;
    }

    @JsonIgnore
    public List<Entity> getPayees() {
        return payees;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CashFlowEstimator that = (CashFlowEstimator) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        return cashFlow != null ? cashFlow.equals(that.cashFlow) : that.cashFlow == null;

    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (cashFlow != null ? cashFlow.hashCode() : 0);
        return result;
    }

    public abstract List<CashFlowInstance> getEstimatedFutureCashFlows(CashFlowCalendar calendar);

    abstract public String getName();

    @JsonIgnore
    public CashFlowFrequency getCashFlowFrequency() {
        return cashFlow;
    }

    @JsonProperty("cashFlow")
    public String getGetFlowId() {
        return cashFlow.getId();
    }


    public Balance computeNewBalance(CashFlowInstance cashFlowInstance, Balance prevBalance) {
        Balance newBalance = new CashBalance(cashFlowInstance.getCashFlowDate(), prevBalance.getValue().add(cashFlowInstance.getAmount()));
        return newBalance;
    }

    @JsonIgnore
    public Balance getStartingBalance() {

        return new CashBalance(this.getStartDate(), BigDecimal.ZERO);
    }

    @JsonIgnore
    public void sourceCashFlowInstance(CashFlowInstance cashFlowInstance) {

    }

    @JsonIgnore
    @NotNull
    abstract String getParentCategory();

    @JsonIgnore
    @Override
    public String getItemType() {
        return CashFlowEstimator.class.getSimpleName();
    }

    @JsonIgnore
    public CASH_ESTIMATE_PASS getPass() {
        return CASH_ESTIMATE_PASS.BASE_CASH_FLOWS;   // By default estimators run the first pass
    }
}
