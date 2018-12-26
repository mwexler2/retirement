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

package name.wexler.retirement.Entity;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.Context;
import name.wexler.retirement.JSON.JSONDateDeserialize;
import name.wexler.retirement.JSON.JSONDateSerialize;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonPropertyOrder({ "type", "id", "firstName", "lastName", "birthDate", "retirementAge"})
public class Person extends Entity {
    private String firstName;
    private String lastName;
    @JsonDeserialize(using= JSONDateDeserialize.class)
    @JsonSerialize(using= JSONDateSerialize.class)
    private LocalDate birthDate;
    private int retirementAge;
    private static final String peoplePath = "people.json";

    public static List<Person> readPeople(Context context) throws IOException {
        return context.fromJSONFileList(Entity[].class, peoplePath);
    }

    public Person(@JacksonInject("context") Context context,
                  @JsonProperty("id") String id,
                  @JsonProperty(value="birthDate", required=true) LocalDate birthDate,
                  @JsonProperty(value="retirementAge", required=true) int retirementAge) throws Exception {
        super(context, id);
        this.birthDate = birthDate;
        this.retirementAge = retirementAge;
    }

    @JsonIgnore
    @Override
    public String getName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public int getRetirementAge() {
        return retirementAge;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (retirementAge != person.retirementAge) return false;
        String thisId = getId();
        String thatId = person.getId();
        if ((thisId != null) ? !thisId.equals(thatId) : (thatId != null)) return false;
        if (firstName != null ? !firstName.equals(person.firstName) : person.firstName != null) return false;
        if (lastName != null ? !lastName.equals(person.lastName) : person.lastName != null) return false;
        return birthDate != null ? birthDate.equals(person.birthDate) : person.birthDate == null;

    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (birthDate != null ? birthDate.hashCode() : 0);
        result = 31 * result + retirementAge;
        return result;
    }
}
