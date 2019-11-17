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

package name.wexler.retirement.visualizer.CashFlowFrequency;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.JSON.JSONDateDeserialize;
import name.wexler.retirement.visualizer.JSON.JSONDateSerialize;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwexler on 7/9/16.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Annual.class, name = "annual"),
        @JsonSubTypes.Type(value = SemiAnnual.class, name = "semiannual"),
        @JsonSubTypes.Type(value = Biweekly.class, name = "biweekly"),
        @JsonSubTypes.Type(value = Monthly.class, name = "monthly"),
        @JsonSubTypes.Type(value = SemiMonthly.class, name = "semimonthly"),
        @JsonSubTypes.Type(value = Quarterly.class, name = "quarterly"),
        @JsonSubTypes.Type(value = VestingSchedule.class, name="vestingSchedule")
})
public abstract class CashFlowFrequency extends Entity {
    public interface SingleCashFlowGenerator {
        CashFlowInstance getSingleCashFlowAmount(CashFlowCalendar calendar, String cashFlowId,
                                                 LocalDate startAccrual, LocalDate endAccrual, LocalDate cashFlowDate,
                                                 BigDecimal percent, CashFlowInstance prevCashFlowInstance);
    }

    public class CashFlowPeriod {
        final LocalDate accrualStart;
        final LocalDate accrualEnd;
        final LocalDate cashFlowDate;
        final BigDecimal portion;

        public CashFlowPeriod(LocalDate accrualStart, LocalDate accrualEnd, LocalDate cashFlowDate, BigDecimal portion) {
            this.accrualStart = accrualStart;
            this.accrualEnd = accrualEnd;
            this.cashFlowDate = cashFlowDate;
            this.portion = portion;
        }
    }

    public enum ApportionmentPeriod { WHOLE_TERM, ANNUAL, EQUAL_MONTHLY }

    private  ApportionmentPeriod apportionmentPeriod;

    @JsonDeserialize(using= JSONDateDeserialize.class)
    @JsonSerialize(using= JSONDateSerialize.class)
    private LocalDate accrueStart;

    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate accrueEnd;

    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate firstPaymentDate;

    private static final String cashFlowsPath = "cashFlows.json";

    public static void readCashFlowFrequencies(Context context) throws IOException {
        context.fromJSONFileList(CashFlowFrequency[].class, cashFlowsPath);
    }

    public CashFlowFrequency(@JacksonInject("context") Context context,
                             @JsonProperty(value = "id", required = true) String id,
                             @JsonProperty(value = "accrueStart", required = true) LocalDate accrueStart,
                             @JsonProperty(value = "accrueEnd", required = true) LocalDate accrueEnd,
                             @JsonProperty(value = "firstPaymentDate", required = true) LocalDate firstPaymentDate,
                             @JsonProperty(value = "apportionmentPeriod", required = true) ApportionmentPeriod apportionmentPeriod)
            throws DuplicateEntityException {
        super(context, id, CashFlowFrequency.class);
        this.accrueStart = accrueStart;
        this.accrueEnd = accrueEnd;
        this.firstPaymentDate = firstPaymentDate;
        this.apportionmentPeriod = apportionmentPeriod;
    }

    @Override
    public String getName() {
        return getId();
    }
    abstract public LocalDate getFirstPeriodStart();

    public LocalDate getAccrueStart() {
        return accrueStart;
    }

    public LocalDate getAccrueEnd() {
        return accrueEnd;
    }

    public LocalDate getFirstPaymentDate() {
        return firstPaymentDate;
    }

    protected abstract List<CashFlowPeriod> getCashFlowPeriods();

    public List<CashFlowInstance> getFutureCashFlowInstances(CashFlowCalendar calendar, CashFlowEstimator cashFlowEstimator, SingleCashFlowGenerator generator) {
        ArrayList<CashFlowInstance> result = new ArrayList<>();

        BigDecimal totalDays = BigDecimal.valueOf(getAccrueStart().until(getAccrueEnd(), ChronoUnit.DAYS));
        List<CashFlowPeriod> cashFlowPeriods = getCashFlowPeriods();
        CashFlowInstance prevCashFlowInstance = null;
        for (CashFlowPeriod period : cashFlowPeriods) {
            CashFlowInstance cashFlowInstance = generator.getSingleCashFlowAmount(calendar, cashFlowEstimator.getId(),
                    period.accrualStart, period.accrualEnd, period.cashFlowDate, period.portion, prevCashFlowInstance);
            if (cashFlowInstance.getCashFlowDate().isAfter(LocalDate.now()))
                result.add(cashFlowInstance);
            prevCashFlowInstance = cashFlowInstance;
        }

        return result;
    }

    abstract public ChronoUnit getChronoUnit();
    abstract public BigDecimal getUnitMultiplier();
    abstract public BigDecimal unitsPerYear();
}
