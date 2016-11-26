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


import com.fasterxml.jackson.databind.InjectableValues;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;
import java.util.Arrays;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.DayOfWeek;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.ObjectWriter;

import name.wexler.retirement.CashFlow.*;

/**
 * Created by mwexler on 8/13/16.
 */
public class JobTest {
    Job job1;
    Job job2;
    Company company1;
    Company company2;
    Person person1;
    Person person2;
    Salary job1Salary;
    Bonus job1Bonus;
    Salary job1Salary2;
    Bonus job1Bonus2;
    Salary job2Salary;
    Context context;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        company1 = new Company(context, "comp1");
        company1.setCompanyName("IBM");
        company2 = new Company(context, "comp2");
        company2.setCompanyName("Xerox");

        person1 = new Person(context, "john1");
        person1.setFirstName("John");
        person1.setLastName("Doe");
        person1.setBirthDate(LocalDate.of(1970, Month.JANUARY, 1));
        person1.setRetirementAge(65);
        person2 = new Person(context, "jane1");
        person2.setFirstName("Jane");
        person2.setLastName("Doe");
        person2.setBirthDate(LocalDate.of(1969, Month.DECEMBER, 31));
        person2.setRetirementAge(40);

        job1 = new Job(context, "job1", company1, person1);
        job1.setStartDate(LocalDate.of(2001, Month.APRIL, 1));
        job1.setEndDate(LocalDate.of(2002, Month.AUGUST, 15));
        job2 = new Job(context, "job2", company2, person2);
        job2.setStartDate(LocalDate.of(2001, Month.JUNE, 15));
        job2.setEndDate(LocalDate.of(2002, Month.MAY, 7));

        MonthDay job1BonusDay = MonthDay.of(Month.MARCH, 15);
        BigDecimal job1BonusPct = new BigDecimal(.30);
        CashFlowSource job1SalarySource = new SemiMonthly("semi-monthly-salary1", 5, 20, job1.getStartDate(), job1.getEndDate());
        job1Salary = new Salary(context, "job1Salary", "job1");
        job1Salary.setBaseAnnualSalary(new BigDecimal(100000.00));
        job1Salary.setSource(job1SalarySource);
        Annual job1BonusSource = new Annual("annual-bonus1", job1BonusDay,
                job1.getStartDate().getYear(), job1.getEndDate().getYear());
        job1Bonus = new Bonus(context, "job1Bonus", "job1", "job1Salary");
        job1Bonus.setSalary(job1Salary);
        job1Bonus.setBonusPct(job1BonusPct);
        job1Bonus.setBonusDay(job1BonusDay);
        job1Bonus.setSource(job1BonusSource);
        IncomeSource[] job1IS = {job1Salary, job1Bonus};

        CashFlowSource job1SalarySource2 = new SemiMonthly("semi-monthly-salary2", 10, 25, job1.getStartDate(), job1.getEndDate());
        job1Salary2 = new Salary(context, "job1Salary2", "job1");
        job1Salary2.setBaseAnnualSalary(BigDecimal.valueOf(100000.00));
        job1Salary2.setSource(job1SalarySource2);
        Annual job1BonusSource2 = new Annual("annual-bonus2", job1BonusDay,
                job1.getStartDate().getYear(), job1.getEndDate().getYear());
        job1Bonus2 = new Bonus(context, "job1Bonus2", "job1", "job1Salary");
        job1Bonus2.setSalary(job1Salary2);
        job1Bonus2.setBonusPct(job1BonusPct);
        job1Bonus2.setBonusDay(job1BonusDay);
        job1Bonus2.setSource(job1BonusSource2);

        CashFlowSource job2SalarySource = new Biweekly("biweekly-job2-salary", DayOfWeek.FRIDAY, job2.getStartDate(), job2.getEndDate());
        job2Salary = new Salary(context, "job2Salary", "job2");
        job2Salary.setBaseAnnualSalary(BigDecimal.valueOf(80000.00));
        job2Salary.setSource(job2SalarySource);
        IncomeSource[] job2IS = {job1Salary2, job1Bonus2, job2Salary};
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getName() throws Exception {
        String name1 = job1.getName();
        assertEquals(name1, "IBM");
        String name2 = job2.getName();
        assertEquals(name2, "Xerox");
    }

    @Test
    public void getEmployer() throws Exception {
        Entity company1a = job1.getEmployer();
        assertEquals(company1, company1a);
        Entity company2a = job2.getEmployer();
        assertEquals(company2, company2a);
    }

    @Test
    public void setEmployer() throws Exception {
        job1.setEmployer(company2);
        assertEquals(job1.getEmployer(), company2);
    }

    @Test
    public void getStartDate() throws Exception {
        LocalDate startDate1 = job1.getStartDate();
        assertEquals(LocalDate.of(2001, Month.APRIL, 1), startDate1);
        LocalDate startDate2 = job2.getStartDate();
        assertEquals(LocalDate.of(2001, Month.JUNE, 15), startDate2);
    }

    @Test
    public void setEndDate() throws Exception {
        LocalDate newYearsEve = LocalDate.of(1999, Month.DECEMBER, 31);
        job1.setEndDate(newYearsEve);
        assertEquals(newYearsEve, job1.getEndDate());
    }

    @Test
    public void getEndDate() throws Exception {
        LocalDate endDate1 = job1.getEndDate();
        assertEquals(LocalDate.of(2002, Month.AUGUST, 15), endDate1);
        LocalDate endDate2 = job2.getEndDate();
        assertEquals(LocalDate.of(2002, Month.MAY, 7), endDate2);
    }

    @Test
    public void setStartDate() throws Exception {
        LocalDate newYearsEve = LocalDate.of(1999, Month.DECEMBER, 31);
        job1.setStartDate(newYearsEve);
        assertEquals(newYearsEve, job1.getStartDate());
    }

    @Test
    public void getIncomeSources() throws Exception {
        List<IncomeSource> incomeSources1 = job1.getIncomeSources();
        List<IncomeSource> incomeSources2 = job2.getIncomeSources();
    }

    @Test
    public void setIncomeSources() {
        IncomeSource[] job2IS = {job1Salary2, job2Salary};

        job2.setIncomeSources(Arrays.asList(job2IS));
    }

    @Test
    public void equals() throws Exception {
        assertNotEquals(job1, job2);
    }

    @Test
    public void serialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        ObjectWriter writer = mapper.writer();
        String job1Str = writer.writeValueAsString(job1);
        assertEquals("{\"id\":\"job1\",\"startDate\":\"2001-04-01\",\"endDate\":\"2002-08-15\",\"incomeSources\":[],\"employer\":\"comp1\",\"employee\":\"john1\"}", job1Str);

        String job2Str = writer.writeValueAsString(job2);
        assertEquals("{\"id\":\"job2\",\"startDate\":\"2001-06-15\",\"endDate\":\"2002-05-07\",\"incomeSources\":[],\"employer\":\"comp2\",\"employee\":\"jane1\"}", job2Str);
    }


    @Test
    public void deserialize() throws Exception {
        String job1aStr = "{\"id\":\"job1a\",\"startDate\":\"2001-04-01\",\"endDate\":\"2002-08-15\",\"incomeSources\":[],\"employer\":\"comp1\",\"employee\":\"john1\"}";
        String job2aStr = "{\"id\":\"job2a\",\"startDate\":\"2001-04-01\",\"endDate\":\"2002-08-15\",\"incomeSources\":[],\"employer\":\"comp1\",\"employee\":\"john1\"}";

        Job job1a = Job.fromJSON(context, job1aStr);
        assertEquals("job1a", job1a.getId());

        Job job2a = job1.fromJSON(context, job2aStr);
        assertEquals("job2a", job2a.getId());
    }

}