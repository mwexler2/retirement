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
import name.wexler.retirement.visualizer.Job;
import name.wexler.retirement.visualizer.Scenario;
import name.wexler.retirement.visualizer.Security;

/**
 * Created by mwexler on 7/5/16.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Person.class, name = "person"),
        @JsonSubTypes.Type(value = Company.class, name = "company"),
        @JsonSubTypes.Type(value = Job.class, name = "job"),
        @JsonSubTypes.Type(value = Security.class, name = "security"),
        @JsonSubTypes.Type(value = Scenario.class, name="scenario")})
public abstract class Entity {
    private Context context;

    public class DuplicateEntityException extends Exception {
        public DuplicateEntityException(String id) {
            super("Key" + id + "already exists.");
        }
    }
    private final String id;

    public Entity(Context context, @JsonProperty("id") String id, Class c) throws DuplicateEntityException {
        this.id = id;
        this.context = context;
         if (context.getById(c, id) != null)
            throw new DuplicateEntityException(id);
        context.put(c, id, this);
    }

    abstract public String getName ();

    public String getId() {
        return id;
    }

    public String getCategory() {
        return getClass().getSimpleName();
    }

    @JsonIgnore
    public Context getContext() {
        return this.context;
    }
}
