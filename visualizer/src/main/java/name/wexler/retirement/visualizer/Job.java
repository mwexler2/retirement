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

package name.wexler.retirement.visualizer;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Entity.Person;
import name.wexler.retirement.visualizer.JSON.JSONDateDeserialize;
import name.wexler.retirement.visualizer.JSON.JSONDateSerialize;

import java.io.IOException;
import java.time.LocalDate;
import java.time.MonthDay;

/**
 * Created by mwexler on 7/5/16.
 */

@JsonPropertyOrder({ "id", "startDate", "endDate", "employer", "employee" })
public class Job extends Entity implements CashFlowSource {
    @JsonDeserialize(using= JSONDateDeserialize.class)
    @JsonSerialize(using= JSONDateSerialize.class)
    private LocalDate startDate;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate endDate;
    private MonthDay bonusDay;
    private CashFlowSink defaultSink;

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

    public static void readJobs(Context context) throws IOException
    {
        context.fromJSONFileList(Job[].class, jobsPath);
    }

    @JsonCreator
    public Job(@JacksonInject("context") Context context,
               @JsonProperty(value = "id", required = true) String id,
               @JsonProperty(value = "employer", required = true) String employer,
               @JsonProperty(value = "employee", required = true) String employee,
               @JsonProperty(value = "defaultSink", required = true) String defaultSinkId) throws Exception {
        super(context, id, Job.class);
        this.employer = context.getById(Entity.class, employer);
        String employerName = this.employer.getName();
        context.put(Job.class, employerName, this);
        this.employee = context.getById(Entity.class, employee);
        this.defaultSink = context.getById(Asset.class, defaultSinkId);
    }

    public boolean isPayee(Entity payee) {
        return employee == payee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        if (!getId().equals(job.getId())) return false;
        if (!startDate.equals(job.startDate)) return false;
        if (!endDate.equals(job.endDate)) return false;
        if (!bonusDay.equals(job.bonusDay)) return false;
        if (!employer.equals(job.employer)) return false;
        return employee.equals(job.employee);

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + startDate.hashCode();
        result = 31 * result + endDate.hashCode();
        result = 31 * result + bonusDay.hashCode();
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

    public void sourceCashFlowInstance(CashFlowInstance cashFlowInstance) {}

    public CashFlowSink getDefaultSink() {
        return defaultSink;
    }

    public String getItemType() {
        return getClass().getSimpleName();
    }
}
