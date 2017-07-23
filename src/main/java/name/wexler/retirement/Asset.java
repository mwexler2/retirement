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

import name.wexler.retirement.CashFlow.AssetValue;

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
    private BigDecimal initialAssetValue;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate initialAssetValueDate;
    private Entity owner;

    public String getId() {
        return id;
    }

    private final String id;

    @JsonCreator
    protected Asset(@JacksonInject("context") Context context,
                @JsonProperty("id") String id,
                @JsonProperty("owner") Entity owner,
                @JsonProperty("initialAssetValue") BigDecimal initialAssetValue,
                @JsonProperty("initialAssetValueDate") LocalDate initialAssetValueDate) {
        this.id = id;
        this.setInitialAssetValue(initialAssetValue);
        this.setInitialAssetValueDate(initialAssetValueDate);
        context.put(Asset.class, id, this);
    }

    abstract public String getName();

    public AssetValue getAssetValue(LocalDate valueDate) {
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal value = initialAssetValue.add(gains);
        return new AssetValue(valueDate, value);
    }

    public BigDecimal getInitialAssetValue() {
        return initialAssetValue;
    }

    public void setInitialAssetValue(BigDecimal initialAssetValue) {
        this.initialAssetValue = initialAssetValue;
    }

    public LocalDate getInitialAssetValueDate() {
        return initialAssetValueDate;
    }

    public void setInitialAssetValueDate(LocalDate initialAssetValueDate) {
        this.initialAssetValueDate = initialAssetValueDate;
    }

    public Entity getOwner() {
        return owner;
    }

    public void setOwner(Entity owner) {
        this.owner = owner;
    }
}
