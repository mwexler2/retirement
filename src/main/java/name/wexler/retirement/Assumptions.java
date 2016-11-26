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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;

/**
 * Created by mwexler on 6/28/16.
 */
public class Assumptions {
    private double longTermInvestmentReturn  = 7.0/100.0;
    private double shortTermInvestmentReturn	= 3.0/100.0;
    private double inflation                 = 4.0/100.0;
    private int alimonyEndAge               = 65;
    private int yearsInShortTerm            = 10;

    public double getLongTermInvestmentReturn() {
        return longTermInvestmentReturn;
    }

    public void setLongTermInvestmentReturn(double longTermInvestmentReturn) {
        this.longTermInvestmentReturn = longTermInvestmentReturn;
    }

    public double getShortTermInvestmentReturn() {
        return shortTermInvestmentReturn;
    }

    public void setShortTermInvestmentReturn(double shortTermInvestmentReturn) {
        this.shortTermInvestmentReturn = shortTermInvestmentReturn;
    }

    public double getInflation() {
        return inflation;
    }

    public void setInflation(double inflation) {
        this.inflation = inflation;
    }

    public int getYearsInShortTerm() {
        return yearsInShortTerm;
    }

    public void setYearsInShortTerm(int yearsInShortTerm) {
        this.yearsInShortTerm = yearsInShortTerm;
    }

}
