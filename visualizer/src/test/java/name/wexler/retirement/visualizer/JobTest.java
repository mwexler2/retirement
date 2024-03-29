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

package name.wexler.retirement.visualizer;


import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowFrequency.*;
import name.wexler.retirement.visualizer.CashFlowEstimator.Bonus;
import name.wexler.retirement.visualizer.CashFlowEstimator.BonusAnnualPct;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.CashFlowEstimator.Salary;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Entity.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Arrays;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

/**
 * Created by mwexler on 8/13/16.
 */
public class JobTest {
    private Job job1;
    private Job job2;
    private Company company1;
    private Company company2;
    private Salary job1Salary2;
    private Salary job2Salary;
    private Context context;

    @Before
    public void setUp() throws Exception {
        Assumptions assumptions = new Assumptions();
        AccountReader accountReader = mock(AccountReader.class);
        context = new Context(accountReader);
        context.setAssumptions(assumptions);
        company1 = new Company(context, "comp1", "Company #1");
        company2 = new Company(context, "comp2", "Company #2");

        Person person1 = new Person(context, "john1", LocalDate.of(1900, 12, 25),
                67, "John", "Doe");
        Person person2 = new Person(context, "jane1", LocalDate.of(1969, Month.DECEMBER, 31), 40,
                "Jane", "Doe");


        Company bank = new Company(context, "bank1", "Bank #1");
        CashFlowSink defaultSink = new AssetAccount(context, "checking1", Arrays.asList(person1.getId()),
                "Checking account 1", bank.getId(), Collections.emptyList(), null, AccountReader.mintTxnSource);
        job1 = new Job(context, "job1", company1.getId(), person1.getId(), defaultSink.getId());
        job1.setStartDate(LocalDate.of(2001, Month.APRIL, 1));
        job1.setEndDate(LocalDate.of(2002, Month.AUGUST, 15));
        job2 = new Job(context, "job2", company2.getId(), person2.getId(), defaultSink.getId());
        job2.setStartDate(LocalDate.of(2001, Month.JUNE, 15));
        job2.setEndDate(LocalDate.of(2002, Month.MAY, 7));

        LocalDate job1FirstBonusDay = LocalDate.of(2001, Month.MARCH, 15);
        BigDecimal job1BonusPct = new BigDecimal(.30);
        LocalDate job1FirstPaycheckDate = LocalDate.of(job1.getStartDate().getYear(), job1.getStartDate().getMonth(), 5);
        CashFlowFrequency job1SalarySource =
                new SemiMonthly(context, "semi-monthly-salary1",
                job1.getStartDate(), job1.getEndDate(), job1FirstPaycheckDate, 5, 20,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        Salary job1Salary = new Salary(context, "job1Salary", job1.getId(), job1SalarySource.getId(),
                BigDecimal.valueOf(100000.00));
        Annual job1BonusSource =
                new Annual(context, "annual-bonus1", job1.getStartDate(), job1.getEndDate(), job1FirstBonusDay,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        Bonus job1Bonus = new BonusAnnualPct(context, "job1Bonus", "job1", "job1Salary", job1BonusPct, job1BonusSource.getId());

        LocalDate salary2FirstPaycheck = LocalDate.of(job1.getStartDate().getYear(), job1.getStartDate().getMonth(), 10);
        CashFlowFrequency job1SalarySource2 =
                new SemiMonthly(context, "semi-monthly-salary2",
                job1.getStartDate(), job1.getEndDate(), salary2FirstPaycheck, 10, 25,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        job1Salary2 = new Salary(context, "job1Salary2", "job1", job1SalarySource2.getId(),
                BigDecimal.valueOf(100000.00));
        Annual job1BonusSource2 =
                new Annual(context, "annual-bonus2", job1FirstBonusDay,
                job1.getStartDate(), job1.getEndDate(),
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        Bonus job1Bonus2 = new BonusAnnualPct(context, "job1Bonus2", "job1", "job1Salary", job1BonusPct, job1BonusSource2.getId());

        LocalDate job2FirstPaycheck = LocalDate.of(2001, Month.JUNE, 22);
        LocalDate job2FirstPeriodStart = LocalDate.of(2001, Month.JUNE, 9);
        CashFlowFrequency job2SalarySource =
                new Biweekly(context, "biweekly-job2-salary", job2FirstPeriodStart, job2.getStartDate(),
                        job2.getEndDate(), job2FirstPaycheck,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        job2Salary = new Salary(context, "job2Salary", job2.getId(), job2SalarySource.getId(),
                BigDecimal.valueOf(80000.00));
        CashFlowEstimator[] job2IS = {job1Salary2, job1Bonus2, job2Salary};
    }

    @After
    public void tearDown() {
    }

    @Test
    public void getName() {
        String name1 = job1.getName();
        assertEquals(name1, "Company #1");
        String name2 = job2.getName();
        assertEquals(name2, "Company #2");
    }

    @Test
    public void getEmployer() {
        Entity company1a = job1.getEmployer();
        assertEquals(company1, company1a);
        Entity company2a = job2.getEmployer();
        assertEquals(company2, company2a);
    }

    @Test
    public void setEmployer() {
        job1.setEmployer(company2);
        assertEquals(job1.getEmployer(), company2);
    }

    @Test
    public void getStartDate() {
        LocalDate startDate1 = job1.getStartDate();
        assertEquals(LocalDate.of(2001, Month.APRIL, 1), startDate1);
        LocalDate startDate2 = job2.getStartDate();
        assertEquals(LocalDate.of(2001, Month.JUNE, 15), startDate2);
    }

    @Test
    public void setEndDate() {
        LocalDate newYearsEve = LocalDate.of(1999, Month.DECEMBER, 31);
        job1.setEndDate(newYearsEve);
        assertEquals(newYearsEve, job1.getEndDate());
    }

    @Test
    public void getEndDate() {
        LocalDate endDate1 = job1.getEndDate();
        assertEquals(LocalDate.of(2002, Month.AUGUST, 15), endDate1);
        LocalDate endDate2 = job2.getEndDate();
        assertEquals(LocalDate.of(2002, Month.MAY, 7), endDate2);
    }

    @Test
    public void setStartDate() {
        LocalDate newYearsEve = LocalDate.of(1999, Month.DECEMBER, 31);
        job1.setStartDate(newYearsEve);
        assertEquals(newYearsEve, job1.getStartDate());
    }

    @Test
    public void equals() {
        assertNotEquals(job1, job2);
    }

    @Test
    public void deserialize() throws Exception {
        String job1aStr = "{\"type\":\"job\",\"id\":\"job1a\",\"startDate\":\"2001-04-01\",\"endDate\":\"2002-08-15\",\"employer\":\"comp1\",\"employee\":\"john1\",\"defaultSink\":\"sunk\"}";
        String job2aStr = "{\"type\":\"job\",\"id\":\"job2a\",\"startDate\":\"2001-04-01\",\"endDate\":\"2002-08-15\",\"employer\":\"comp1\",\"employee\":\"john1\",\"defaultSink\":\"sunk\"}";

        Job job1a = context.fromJSON(Job.class, job1aStr);
        assertEquals("job1a", job1a.getId());

        Job job2a = context.fromJSON(Job.class, job2aStr);
        assertEquals("job2a", job2a.getId());
    }

}