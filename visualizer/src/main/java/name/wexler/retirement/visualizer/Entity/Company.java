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
package name.wexler.retirement.visualizer.Entity;

import com.fasterxml.jackson.annotation.*;
import name.wexler.retirement.visualizer.Context;

import java.io.IOException;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonTypeName("company")
public class Company extends Entity {
    private String companyName;
    private static final String companyPath = "company.json";

    public static void readCompanies(Context context) throws IOException {
        context.fromJSONFileList(Entity[].class, companyPath);
    }

    @JsonCreator
    public Company(@JacksonInject("context") Context context,
                   @JsonProperty("id") String id,
                   @JsonProperty(value = "companyName", defaultValue = "") String companyName) throws DuplicateEntityException {
        super(context, id, Entity.class);
        this.companyName = companyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Company company = (Company) o;

        if (companyName != null ? !companyName.equals(company.companyName) : company.companyName != null) return false;
        return getId() != null ? getId().equals(company.getId()) : company.getId() == null;
    }

    @Override
    public int hashCode() {
        int result = companyName != null ? companyName.hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }


    @JsonIgnore
    @Override
    public String getName() {
        return companyName;
    }

    public String getCompanyName() {
        return companyName;
    }
}
