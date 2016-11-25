package name.wexler.retirement;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by mwexler on 8/13/16.
 */
public class SalaryTest {
    Salary incomeSource1;
    Bonus incomeSource2;
    EntityManager<Job> jobManager;
    EntityManager<IncomeSource> incomeSourceManager;

    @Before
    public void setUp() throws Exception {
        EntityManager<Entity> entityManager = new EntityManager<Entity>();
        this.incomeSourceManager = new EntityManager<>();
        this.jobManager = new EntityManager<Job>();
        Company employer = new Company(entityManager, "employer1");
        Person employee = new Person(entityManager, "employee1");
        Job job1 = new Job(jobManager, "job1", employer, employee);
        job1.setStartDate(LocalDate.of(2015, Month.MAY, 1));
        job1.setEndDate(LocalDate.of(2016, Month.DECEMBER, 31));
        incomeSource1 = new Salary(incomeSourceManager, jobManager, "salary1", "job1");
        incomeSource1.setBaseAnnualSalary(BigDecimal.valueOf(100000.00));
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void getId() throws Exception {
        String name1 = incomeSource1.getId();
        assertEquals(name1, "salary1");
    }


    @Test
    public void equals() throws Exception {
        assertNotEquals(incomeSource1, incomeSource2);
    }

    @Test
    public void serialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");

        ObjectWriter writer = mapper.writer();
        String incomeSource1Str = writer.writeValueAsString(incomeSource1);
        assertEquals("{\"type\":\"salary\",\"id\":\"salary1\",\"source\":null,\"job\":\"job1\",\"baseAnnualSalary\":100000.0}", incomeSource1Str);
    }


    @Test
    public void deserialize() throws Exception {
        String incomeSource1aStr = "{\"type\":\"salary\",\"id\":\"salary1a\",\"source\":null,\"job\":\"job1\",\"baseAnnualSalary\":100000.0}";

        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        InjectableValues injects = new InjectableValues.Std().
                addValue("jobManager", jobManager).
                addValue("incomeSourceManager", incomeSourceManager);
        mapper.setInjectableValues(injects);
        ObjectWriter writer = mapper.writer();

        IncomeSource incomeSource1a = mapper.readValue(incomeSource1aStr, IncomeSource.class);
        assertEquals("salary1a", incomeSource1a.getId());
    }

}