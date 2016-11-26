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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by mwexler on 7/9/16.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RealProperty.class, name = "real-property") })
public abstract class Asset {
    private BigDecimal initialAssetValue;
    @JsonDeserialize(using=JSONDateDeserialize.class)
    @JsonSerialize(using=JSONDateSerialize.class)
    private LocalDate initialAssetValueDate;
    private Entity owner;

    public String getId() {
        return id;
    }

    private String id;

    public Asset() {

    }

    protected Asset(String id, Entity owner, BigDecimal initialAssetValue, LocalDate initialAssetValueDate) {
        this.id = id;
        this.setInitialAssetValue(initialAssetValue);
        this.setInitialAssetValueDate(initialAssetValueDate);
    }

    abstract public String getName();

    public BigDecimal getAssetValue(LocalDate valueDate) {
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal assetValue = initialAssetValue.add(gains);
        return assetValue;
    }

    public BigDecimal getInitialAssetValue() {
        return initialAssetValue;
    }

    public void setInitialAssetValue(BigDecimal initialAssetValue) {
        this.initialAssetValue = initialAssetValue;
    }

    public LocalDate getInitialAssetValueDate() {
        return initialAssetValueDate;
    }

    public void setInitialAssetValueDate(LocalDate initialAssetValueDate) {
        this.initialAssetValueDate = initialAssetValueDate;
    }

    public Entity getOwner() {
        return owner;
    }

    public void setOwner(Entity owner) {
        this.owner = owner;
    }

    public String toJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        ObjectWriter writer = mapper.writer();
        String result = writer.writeValueAsString(this);
        return result;
    }

    static private ObjectMapper getObjectMapper(Context context){
        ObjectMapper mapper = context.getObjectMapper();
        return mapper;
    }
    static public Asset fromJSON(Context context,
                                         String json) throws Exception {
        ObjectMapper mapper = getObjectMapper(context);
        ObjectWriter writer = mapper.writer();
        Asset result = (Asset) mapper.readValue(json, Asset.class);
        return result;
    }

    static public Asset[] fromJSONFile(Context context, String filePath) throws IOException {
        File file = new File(filePath);
        ObjectMapper mapper = getObjectMapper(context);
        Asset[] result = mapper.readValue(file, Asset[].class);
        return result;
    }
}
