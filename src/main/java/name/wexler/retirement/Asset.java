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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import name.wexler.retirement.CashFlow.Balance;

/**
 * Created by mwexler on 7/9/16.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RealProperty.class, name = "real-property") })
public abstract class Asset {
    private final Balance _initialBalance;
    private final List<Balance> _interimBalances;


    private Entity _owner;

    public String getId() {
        return _id;
    }

    private final String _id;

    @JsonCreator
    protected Asset(@JacksonInject("context") Context context,
                @JsonProperty("id") String id,
                @JsonProperty("owner") String ownerId,
                    @JsonProperty("initialBalance") BigDecimal initialBalance,
                    @JsonDeserialize(using=JSONDateDeserialize.class)  @JsonProperty("initialBalanceDate") LocalDate initialBalanceDate,
                    @JsonProperty("interimBalances") List<Balance> interimBalances) {
        this._id = id;
        this._owner = context.getById(Entity.class, ownerId);
        this._initialBalance = new Balance(initialBalanceDate, initialBalance);
        interimBalances.sort(Comparator.comparing(Balance::getBalanceDate));
        _interimBalances = interimBalances;
        context.put(Asset.class, id, this);
    }

    abstract public String getName();

    public Balance getBalanceAtDate(LocalDate valueDate, Assumptions assumptions) {
        Balance recentBalance = _initialBalance;
        int i =  Collections.binarySearch(_interimBalances, new Balance(valueDate, BigDecimal.ZERO), Comparator.comparing(Balance::getBalanceDate));
        if (i >= 0) {
            recentBalance = _interimBalances.get(i);
        } else if (i < -1) {
            recentBalance = _interimBalances.get(-i - 2);
        }
        return recentBalance;
    }


    public Balance getInitialBalance() {
        return _initialBalance;
    }

    @JsonProperty("initialBalance")
    public BigDecimal getInitialBalanceAmount() {
        return _initialBalance.getValue();
    }


    @JsonProperty("initialBalanceDate")
    @JsonSerialize(using=JSONDateSerialize.class)
    public LocalDate getStartDate() {
        return _initialBalance.getBalanceDate();
    }

    public Entity getOwner() {
        return _owner;
    }

    @JsonProperty(value = "owner")
    public String getOwnerId() {

        String result = _owner.getId();
        return result;
    }
}
