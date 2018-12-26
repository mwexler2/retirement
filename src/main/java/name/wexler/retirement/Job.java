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
import name.wexler.retirement.CashFlowSource.CashFlowSource;
import name.wexler.retirement.Entity.Company;
import name.wexler.retirement.Entity.Entity;
import name.wexler.retirement.Entity.Person;
import name.wexler.retirement.JSON.JSONDateDeserialize;
import name.wexler.retirement.JSON.JSONDateSerialize;

import java.io.IOException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mwexler on 7/5/16.
 */

@JsonPropertyOrder({ "id", "startDate", "endDate", "incomeSources", "employer", "employee" })
public class Job {
    private String id;
    @JsonDeserialize(using= JSONDateDeserialize.class)
    @JsonSerialize(using= JSONDateSerialize.class)
    private LocalDate startDate;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate endDate;
    private MonthDay bonusDay;

    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "@id")
    @JsonIdentityReference(alwaysAsId = true)
    private List<CashFlowSource> incomeSources;

    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Entity employer;

    @JsonIdentityInfo(
            generator=ObjectIdGenerators.PropertyGenerator.class,
            property="id")
    @JsonIdentityReference(alwaysAsId = true)
    private Entity employee;

    private static final String jobsPath = "jobs.json";

    public static List<Job> readJobs(Context context) throws IOException
    {
        return context.fromJSONFileList(Job[].class, jobsPath);
    }

    @JsonCreator
    public Job(@JacksonInject("context") Context context,
               @JsonProperty(value = "id", required = true) String id,
               @JsonProperty(value = "employer", required = true) String employer,
               @JsonProperty(value = "employee", required = true) String employee) throws Exception {
        this.id = id;
        this.employer = context.getById(Entity.class, employer);
        this.employee = context.getById(Entity.class, employee);
        this.init(context);
    }

    public Job(Context context,
               String id,
               Company employer,
               Person employee) throws Exception {
        this.id = id;
        this.employer = employer;
        this.employee = employee;
        this.init(context);
    }

    private void init(Context context) throws Exception {
        if (context.getById(Job.class, id) != null)
            throw new Exception("Key " + id + " already exists");
        context.put(Job.class, id, this);
        incomeSources = new ArrayList<>();
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

    public List<CashFlowSource> getIncomeSources() {
        return incomeSources;
    }

    public void setIncomeSources(List<CashFlowSource> incomeSources) {
        this.incomeSources = incomeSources;
    }

    public Entity getEmployer() {
        return employer;
    }

    public Entity getEmployee() {
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
