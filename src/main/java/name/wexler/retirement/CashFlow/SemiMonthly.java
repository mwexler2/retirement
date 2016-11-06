/*
*
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

import java.time.LocalDate;

/**
 * Created by mwexler on 7/9/16.
 */
public class SemiMonthly extends Monthly {
    private int firstDayOfMonth;
    private int secondDayOfMonth;

    public SemiMonthly(String id, int firstDayOfMonth, int secondDayOfMonth, LocalDate startDate, LocalDate endDate) {
        super(id, firstDayOfMonth, startDate, endDate);
        this.firstDayOfMonth = firstDayOfMonth;
        this.secondDayOfMonth = this.secondDayOfMonth;
    }
}
