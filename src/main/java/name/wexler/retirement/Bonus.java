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
import name.wexler.retirement.CashFlow.CashFlowSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.YearMonth;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bonus extends IncomeSource {
    private String id;
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Job job;
    @JsonIdentityReference(alwaysAsId = true)
    private Salary salary;
    private BigDecimal bonusPct;
    @JsonDeserialize(using=JSONMonthDayDeserialize.class)
    @JsonSerialize(using=JSONMonthDaySerialize.class)
    private MonthDay bonusDay;

    public Bonus() {
    }

    @Override
    public BigDecimal getMonthlyCashFlow(YearMonth yearMonth, BigDecimal annualAmount) {
        BigDecimal bonusAmount = BigDecimal.ZERO;

        LocalDate bonusDate = yearMonth.atDay(bonusDay.getDayOfMonth());
        if (bonusDate.compareTo(job.getEndDate()) <= 0 &&
                yearMonth.getMonth() == bonusDay.getMonth()) {
            bonusAmount = salary.getAnnualCashFlow(yearMonth.getYear() - 1).multiply(bonusPct);
        }
        return bonusAmount;
    }

    public BigDecimal getAnnualCashFlow(int year) {
        BigDecimal annualBonusAmount = BigDecimal.ZERO;
        for (Month month : Month.values()) {
            YearMonth yearMonth =  YearMonth.of(year, month);
            annualBonusAmount = annualBonusAmount.add(this.getMonthlyCashFlow(yearMonth, salary.getAnnualCashFlow()));
        }
        return annualBonusAmount;
    }

    @JsonIgnore
    public BigDecimal getAnnualCashFlow() {
        return getAnnualCashFlow(LocalDate.now().getYear());
    }

    @JsonIgnore
    public String getName() {
        return job.getName() + " Bonus";
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Salary getSalary() {
        return salary;
    }

    public void setSalary(Salary salary) {
        this.salary = salary;
    }

    public BigDecimal getBonusPct() {
        return bonusPct;
    }

    public void setBonusPct(BigDecimal bonusPct) {
        this.bonusPct = bonusPct;
    }

    public MonthDay getBonusDay() {
        return bonusDay;
    }

    public void setBonusDay(MonthDay bonusDay) {
        this.bonusDay = bonusDay;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
