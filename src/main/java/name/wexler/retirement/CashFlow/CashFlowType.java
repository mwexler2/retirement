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

package name.wexler.retirement.CashFlow;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
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
        @JsonSubTypes.Type(value = Biweekly.class, name = "biweekly"),
        @JsonSubTypes.Type(value = Monthly.class, name = "monthly"),
        @JsonSubTypes.Type(value = SemiMonthly.class, name = "semimonthly"),
        @JsonSubTypes.Type(value = Quarterly.class, name = "quarterly")
})
public abstract class   CashFlowType {
    private final String id;

    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate accrueStart;

    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate accrueEnd;

    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate firstPaymentDate;

    public CashFlowType(@JacksonInject("context") Context context,
                        @JsonProperty(value = "id", required = true) String id,
                        @JsonProperty(value = "accrueStart", required = true) LocalDate accrueStart,
                        @JsonProperty(value = "accrueEnd", required = true) LocalDate accrueEnd,
                        @JsonProperty(value = "firstPaymentDate", required = true) LocalDate firstPaymentDate)
            throws Exception {
        this.id = id;
        if (context.getById(CashFlowType.class, id) != null)
            throw new Exception("Key " + id + " already exists");
        context.put(CashFlowType.class, id, this);
        this.accrueStart = accrueStart;
        this.accrueEnd = accrueEnd;
        this.firstPaymentDate = firstPaymentDate;
    }

    public String getId() {
        return this.id;
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

    abstract public List<CashFlowInstance> getCashFlowInstances(BigDecimal singleFlowAmount);

    abstract public BigDecimal getPeriodsPerYear();
}
