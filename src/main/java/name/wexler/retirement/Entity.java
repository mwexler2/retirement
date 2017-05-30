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

/**
 * Created by mwexler on 7/5/16.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Person.class, name = "person"),
        @JsonSubTypes.Type(value = Company.class, name = "company") })
public abstract class Entity {
    private final String id;

    public Entity(Context context, @JsonProperty("id") String id) throws Exception {
        this.id = id;
         if (context.getById(Entity.class, id) != null)
            throw new Exception("Key " + id + " already exists");
        context.put(Entity.class, id, this);
    }

    abstract public String getName ();

    public String getId() {
        return id;
    }
}
