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
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.util.JSONPObject;
import name.wexler.retirement.CashFlow.CashFlowSource;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Created by mwexler on 7/9/16.
 */
@JsonPropertyOrder({ "type", "id", "source", "job", "baseAnnualSalary"})
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Salary.class, name = "salary"),
        @JsonSubTypes.Type(value = Bonus.class, name = "bonus") })
public abstract class IncomeSource {
    private String id;
    private CashFlowSource source;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IncomeSource that = (IncomeSource) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return source != null ? source.equals(that.source) : that.source == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }

    public IncomeSource(Context context, @JsonProperty("id") String id) throws Exception {
        this.id = id;
        if (context.getById(IncomeSource.class, id) != null)
            throw new Exception("Key " + id + " already exists");
        context.put(IncomeSource.class, id, this);
    }

    public BigDecimal getMonthlyCashFlow(YearMonth yearMonth, BigDecimal annualAmount) {
        return source.getMonthlyCashFlow(yearMonth, annualAmount);
    }

    public BigDecimal getMonthlyCashFlow(BigDecimal annualAmount) {
        return source.getMonthlyCashFlow(annualAmount);
    }

    public abstract BigDecimal getAnnualCashFlow(int year);

    public BigDecimal getAnnualCashFlow() {
        return getAnnualCashFlow(LocalDate.now().getYear());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CashFlowSource getSource() {
        return source;
    }

    public void setSource(CashFlowSource source) {
        this.source = source;
    }

    public String toJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        ObjectWriter writer = mapper.writer();
        String result = writer.writeValueAsString(this);
        return result;
    }

    static public IncomeSource fromJSON(Context context,
                                 String json) throws Exception {
        ObjectMapper mapper = context.getObjectMapper();
        ObjectWriter writer = mapper.writer();
        IncomeSource result = (IncomeSource) mapper.readValue(json, IncomeSource.class);
        return result;
    }

    static public IncomeSource[] fromJSONFile(Context context, String filePath) throws IOException {
        File incomeSourcesFile = new File(filePath);
        ObjectMapper incomeSourceMapper = context.getObjectMapper();
        IncomeSource[] result = incomeSourceMapper.readValue(incomeSourcesFile, IncomeSource[].class);
        return result;
    }
}
