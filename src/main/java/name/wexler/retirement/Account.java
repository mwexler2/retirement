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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by mwexler on 7/9/16.
 */
public class Account {
    private String accountId;
    private String accountName;
    private String institutionName;
    private Asset[] assets;

    public Account(String accountId, String accountName, String institutionName, Asset[] assets) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.institutionName = institutionName;
        this.assets = assets;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public Asset[] getAssets() {
        return assets;
    }

    public BigDecimal getAccountValue(LocalDate date) {
        BigDecimal result = BigDecimal.ONE.ZERO;

        for (Asset asset : this.assets) {
            result = result.add(asset.getAssetValue(date));
        }
        return result;
    }
}
