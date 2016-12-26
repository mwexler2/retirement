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

        LocalDate job1FirstBonusDay = LocalDate.of(2001, Month.MARCH, 15);
        BigDecimal job1BonusPct = new BigDecimal(.30);
        LocalDate job1FirstPaycheckDate = LocalDate.of(job1.getStartDate().getYear(), job1.getStartDate().getMonth(), 5);
        CashFlowType job1SalarySource = new SemiMonthly(context, "semi-monthly-salary1",
                job1.getStartDate(), job1.getEndDate(), job1FirstPaycheckDate, 5, 20);
        job1Salary = new Salary(context, "job1Salary", job1.getId(), job1SalarySource.getId());
        job1Salary.setBaseAnnualSalary(new BigDecimal(100000.00));
        Annual job1BonusSource = new Annual(context, "annual-bonus1", job1.getStartDate(), job1.getEndDate(), job1FirstBonusDay);
        job1Bonus = new BonusAnnualPct(context, "job1Bonus", "job1", "job1Salary", job1BonusPct, job1BonusSource.getId());;
        IncomeSource[] job1IS = {job1Salary, job1Bonus};

        LocalDate salary2FirstPaycheck = LocalDate.of(job1.getStartDate().getYear(), job1.getStartDate().getMonth(), 10);
        CashFlowType job1SalarySource2 = new SemiMonthly(context, "semi-monthly-salary2",
                job1.getStartDate(), job1.getEndDate(), salary2FirstPaycheck, 10, 25);
        job1Salary2 = new Salary(context, "job1Salary2", "job1", job1SalarySource2.getId());
        job1Salary2.setBaseAnnualSalary(BigDecimal.valueOf(100000.00));
        Annual job1BonusSource2 = new Annual(context, "annual-bonus2", job1FirstBonusDay,
                job1.getStartDate(), job1.getEndDate());
        job1Bonus2 = new BonusAnnualPct(context, "job1Bonus2", "job1", "job1Salary", job1BonusPct, job1BonusSource2.getId());

        LocalDate job2FirstPaycheck = LocalDate.of(2001, Month.JUNE, 19);
        CashFlowType job2SalarySource = new Biweekly(context, "biweekly-job2-salary", job2.getStartDate(), job2.getEndDate(), job2FirstPaycheck);
        job2Salary = new Salary(context, "job2Salary", job2.getId(), job2SalarySource.getId());
        job2Salary.setBaseAnnualSalary(BigDecimal.valueOf(80000.00));
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
        String job1Str = context.toJSON(job1);
        assertEquals("{\"id\":\"job1\",\"startDate\":\"2001-04-01\",\"endDate\":\"2002-08-15\",\"incomeSources\":[],\"employer\":\"comp1\",\"employee\":\"john1\"}", job1Str);

        String job2Str = context.toJSON(job2);
        assertEquals("{\"id\":\"job2\",\"startDate\":\"2001-06-15\",\"endDate\":\"2002-05-07\",\"incomeSources\":[],\"employer\":\"comp2\",\"employee\":\"jane1\"}", job2Str);
    }


    @Test
    public void deserialize() throws Exception {
        String job1aStr = "{\"id\":\"job1a\",\"startDate\":\"2001-04-01\",\"endDate\":\"2002-08-15\",\"incomeSources\":[],\"employer\":\"comp1\",\"employee\":\"john1\"}";
        String job2aStr = "{\"id\":\"job2a\",\"startDate\":\"2001-04-01\",\"endDate\":\"2002-08-15\",\"incomeSources\":[],\"employer\":\"comp1\",\"employee\":\"john1\"}";

        Job job1a = context.<Job>fromJSON(Job.class, job1aStr);
        assertEquals("job1a", job1a.getId());

        Job job2a = context.<Job>fromJSON(Job.class, job2aStr);
        assertEquals("job2a", job2a.getId());
    }

}