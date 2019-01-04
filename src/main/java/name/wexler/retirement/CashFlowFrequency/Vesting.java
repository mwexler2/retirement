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

package name.wexler.retirement.CashFlowFrequency;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Created by mwexler on 7/9/16.
 */
public class Vesting {
    private final int months;
    private final BigDecimal percent;

    private Vesting(@JsonProperty(value = "months", required = true) int months,
                    @JsonProperty(value = "percent", required = true) BigDecimal percent) {
        this.months = months;
        this.percent = percent;
    }

    public static Vesting of(int mths, BigDecimal pct) {
        Vesting result = new Vesting(mths, pct);
        return result;
    }

    public int getMonths() {
        return months;
    }

    public BigDecimal getPercent() {
        return percent;
    }
}

