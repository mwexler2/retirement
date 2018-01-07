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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Bonus extends IncomeSource {
    @JsonIgnore
    private Job job;

    public Bonus(@JacksonInject("context") Context context,
                 @JsonProperty(value = "id", required = true) String id,
                 @JsonProperty(value = "job", required = true) String jobId,
                 @JsonProperty(value = "cashFlow", required = true) String cashFlowId)
            throws Exception {
        super(context, id, cashFlowId);
        this.setJobId(context, jobId);
    }


    @JsonIgnore
    public String getName() {
        return job.getName() + " Bonus";
    }

    @JsonProperty(value = "job")
    public String getJobId() {
        return job.getId();
    }
    private void setJobId(@JacksonInject("context") Context context, @JsonProperty(value = "job", required = true) String jobId) {
        this.job = context.getById(Job.class, jobId);
    }

}
