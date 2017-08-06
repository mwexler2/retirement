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

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import name.wexler.retirement.CashFlow.Balance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

/**
 * Created by mwexler on 7/9/16.
 */
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
                @JsonProperty("owner") String ownerId,
                @JsonProperty("initialBalance") BigDecimal initialBalance,
                @JsonDeserialize(using=JSONDateDeserialize.class)  @JsonProperty("initialBalanceDate") LocalDate initialBalanceDate,
                @JsonProperty("address") String[] address,
                @JsonProperty("city") String city,
                @JsonProperty("county") String county,
                @JsonProperty("state") String state,
                @JsonProperty("zipCode") String zipCode,
                @JsonProperty("country") String country) {
        super(context, id, ownerId, initialBalance, initialBalanceDate);
        this.address = address;
        this.city = city;
        this.county = county;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    @JsonIgnore
    public String getName() {
        return address[0];
    }

    public String[] getAddress() {
        return address;
    }

    public void setAddress(String[] address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public Balance getBalanceAtDate(LocalDate valueDate) {
        double annualRateOfReturn = 1.07;
        Balance initialBalance = getInitialBalance();
        return new Balance(valueDate, initialBalance.getBalanceAtDate(valueDate, annualRateOfReturn));
    }
}
