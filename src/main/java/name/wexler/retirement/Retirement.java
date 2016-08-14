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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.util.Locale;
import java.text.NumberFormat;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerationException;

import com.fasterxml.jackson.databind.ObjectWriter;
import name.wexler.retirement.CashFlow.*;


/**
 * Created by mwexler on 6/28/16.
 */
public class Retirement {
    private Scenario[] scenarios;
    private Job[] jobs;
    private Person[] people;

    public NumberFormat getCf() {
        return cf;
    }

    public NumberFormat getPf() {
        return pf;
    }

    private NumberFormat cf;
    private NumberFormat pf;

    Retirement() {
        Locale enUS = Locale.forLanguageTag("en-US");
        cf = NumberFormat.getCurrencyInstance(enUS);
        pf = NumberFormat.getPercentInstance();

        try {
            ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
            String userHome = System.getProperty("user.home");
            String resourceDir = userHome + "/.retirement/resources";
            
            String peoplePath = resourceDir + "/people.json";
            File peopleFile = new File(peoplePath);
            this.people = mapper.readValue(peopleFile, Person[].class);

            String jobsPath = resourceDir + "/jobs.json";
            File jobsFile = new File(jobsPath);
            this.jobs = mapper.readValue(jobsFile, Job[].class);

            String scenariosPath = resourceDir + "/scenarios.json";
            File scenariosFile = new File(scenariosPath);
            this.scenarios = mapper.readValue(scenariosFile, Scenario[].class);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Scenario[] getScenarios() {
        return scenarios;
    }

    public void setScenarios(Scenario[] scenarios) {
        this.scenarios = scenarios;
    }

    public Person[] getPeople() {
        return people;
    }

    public void setPeople(Person[] people) {
        this.people = people;
    }
}

