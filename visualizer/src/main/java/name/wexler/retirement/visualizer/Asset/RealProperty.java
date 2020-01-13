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

package name.wexler.retirement.visualizer.Asset;

import com.fasterxml.jackson.annotation.*;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Scenario;
import name.wexler.retirement.visualizer.CashFlowFrequency.Balance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwexler on 7/9/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "type", "id", "owners", "initialBalance", "address", "city", "county", "state", "zipCode", "country" })
public class RealProperty extends Asset {
    private String[] address;
    private String city;
    private String county;
    private String state;
    private String zipCode;
    private String country;

    @JsonCreator
    public RealProperty(
                @JacksonInject("context") Context context,
                @JsonProperty("id") String id,
                @JsonProperty("owners") List<String> ownerIds,
                @JsonProperty("initialBalance") CashBalance initialBalance,
                @JsonProperty("address") String[] address,
                @JsonProperty("city") String city,
                @JsonProperty("county") String county,
                @JsonProperty("state") String state,
                @JsonProperty("zipCode") String zipCode,
                @JsonProperty("country") String country,
                @JsonProperty("interimBalances") List<CashBalance> interimBalances)
    throws DuplicateEntityException {
        super(context, id, ownerIds, initialBalance, interimBalances);
        this.address = address;
        this.city = city;
        this.county = county;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    @Override
    public void setStartingBalance() {
    }

    @JsonIgnore
    public String getName() {
        return address[0];
    }

    public String[] getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getCounty() {
        return county;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCountry() {
        return country;
    }
}
