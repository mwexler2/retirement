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
import name.wexler.retirement.CashFlow.CashFlowSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.YearMonth;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Salary extends IncomeSource {
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonIgnore
    private Job job;
    private BigDecimal baseAnnualSalary;
    private static BigDecimal monthsPerYear = new BigDecimal(12);

    public Salary(@JacksonInject("context") Context context,
                  @JsonProperty("id") String id,
                  @JsonProperty("job") String jobId) throws Exception {
        super(context, id);
        this.setJobId(context, jobId);
    }


    public BigDecimal getMonthlyCashFlow(YearMonth yearMonth) {
        BigDecimal monthlySalary = BigDecimal.ZERO;
        LocalDate startDate = job.getStartDate();
        LocalDate endDate = job.getEndDate();

        YearMonth startYearMonth = YearMonth.of(startDate.getYear(), startDate.getMonth());
        YearMonth endYearMonth = YearMonth.of(endDate.getYear(), endDate.getMonth());
        if (yearMonth.isBefore(startYearMonth) || yearMonth.isAfter(endYearMonth)) {
            monthlySalary = new BigDecimal(0.0);
        } else if (yearMonth.isAfter(startYearMonth) && yearMonth.isBefore(endYearMonth)) {
            monthlySalary = baseAnnualSalary.divide(this.monthsPerYear, RoundingMode.HALF_UP);
        } else {
            LocalDate firstDateInMonth = yearMonth.atDay(1);
            LocalDate lastDateInMonth = yearMonth.atEndOfMonth();
            if (yearMonth == startYearMonth) {
                firstDateInMonth = yearMonth.atDay(startDate.getDayOfMonth());
            } else if (yearMonth == endYearMonth) {
                lastDateInMonth = yearMonth.atDay(endDate.getDayOfMonth());
            }
            BigDecimal days = new BigDecimal(firstDateInMonth.until(lastDateInMonth).getDays());
            monthlySalary = baseAnnualSalary.multiply(days).divide(new BigDecimal(startDate.lengthOfYear()), RoundingMode.HALF_UP);
        }
        return monthlySalary;
    }

    public BigDecimal getAnnualCashFlow(int year) {
        BigDecimal annualSalaryAmount = BigDecimal.ZERO;
        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(year, month);
            annualSalaryAmount = annualSalaryAmount.add(this.getMonthlyCashFlow(yearMonth));
        }
        return annualSalaryAmount;
    }

    @JsonIgnore
    public BigDecimal getAnnualCashFlow() {
        return getAnnualCashFlow(LocalDate.now().getYear());
    }

    @JsonIgnore
    public String getName() {
        if (job == null)
            return "Unkonwn job salary";
        return job.getName() + " Salary";
    }

    @JsonProperty(value = "job")
    public void setJobId(@JacksonInject("context") Context context,
                         @JsonProperty(value="job", required=true) String jobId) {
        this.job = context.<Job>getById(Job.class, jobId);
    }

    @JsonProperty(value = "job")
    public String getJobId() {
        return job.getId();
    }

    public Job getJob() {
        return job;
    }


    public void setJob(Job job) {
        this.job = job;
    }

    public BigDecimal getBaseAnnualSalary() {
        return baseAnnualSalary;
    }

    public void setBaseAnnualSalary(BigDecimal baseAnnualSalary) {
        this.baseAnnualSalary = baseAnnualSalary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Salary salary = (Salary) o;

        if (this.getId() != null ? !this.getId().equals(salary.getId()) : salary.getId() != null) return false;
        if (job != null ? !job.equals(salary.job) : salary.job != null) return false;
        return baseAnnualSalary != null ? baseAnnualSalary.equals(salary.baseAnnualSalary) : salary.baseAnnualSalary == null;

    }

    @Override
    public int hashCode() {
        int result = this.getId() != null ? this.getId().hashCode() : 0;
        result = 31 * result + (job != null ? job.hashCode() : 0);
        result = 31 * result + (baseAnnualSalary != null ? baseAnnualSalary.hashCode() : 0);
        return result;
    }
}
