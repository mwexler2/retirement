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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;

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
    private String id;

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

    public String toJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        ObjectWriter writer = mapper.writer();
        String result = writer.writeValueAsString(this);
        return result;
    }

    static public Entity fromJSON(Context context,
                                        String json) throws Exception {
        ObjectMapper mapper = context.getObjectMapper();
        ObjectWriter writer = mapper.writer();
        Entity result = (Entity) mapper.readValue(json, Entity.class);
        return result;
    }

    static public Entity[] fromJSONFile(Context context, String filePath) throws IOException {
        File entityFile = new File(filePath);
        ObjectMapper incomeSourceMapper = context.getObjectMapper();
        Entity[] result = incomeSourceMapper.readValue(entityFile, Entity[].class);
        return result;
    }
}
