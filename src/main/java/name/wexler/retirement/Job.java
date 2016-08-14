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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Job {
    private String id;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate startDate;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate endDate;
    private MonthDay bonusDay;
    private List<IncomeSource> incomeSources;
    private Entity employer;
    private Person employee;


    public Job() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        if (!id.equals(job.id)) return false;
        if (!startDate.equals(job.startDate)) return false;
        if (!endDate.equals(job.endDate)) return false;
        if (!bonusDay.equals(job.bonusDay)) return false;
        if (!incomeSources.equals(job.incomeSources)) return false;
        if (!employer.equals(job.employer)) return false;
        return employee.equals(job.employee);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + startDate.hashCode();
        result = 31 * result + endDate.hashCode();
        result = 31 * result + bonusDay.hashCode();
        result = 31 * result + incomeSources.hashCode();
        result = 31 * result + employer.hashCode();
        result = 31 * result + employee.hashCode();
        return result;
    }

    @JsonIgnore
    public String getName() {
        return employer.getName();
    }

    public void setEmployer(Entity employer) {
        this.employer = employer;
    }

    public void setEmployee(Person employee) {
        this.employee = employee;
    }

    public List<IncomeSource> getIncomeSources() {
        return incomeSources;
    }

    public void setIncomeSources(List<IncomeSource> incomeSources) {
        this.incomeSources = incomeSources;
    }

    public Entity getEmployer() {
        return employer;
    }

    public Person getEmployee() {
        return employee;
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
