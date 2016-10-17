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
public class IncomeSourceTest {
    Salary incomeSource1;
    Bonus incomeSource2;
    EntityManager<Job> jobManager;

    @Before
    public void setUp() throws Exception {
        EntityManager<Entity> entityManager = new EntityManager<Entity>();
        this.jobManager = new EntityManager<Job>();
        Company employer = new Company(entityManager, "employer1");
        Person employee = new Person(entityManager, "employee1");
        Job job1 = new Job(jobManager, "job1", employer, employee);
        job1.setStartDate(LocalDate.of(2015, Month.MAY, 1));
        job1.setEndDate(LocalDate.of(2016, Month.DECEMBER, 31));
        incomeSource1 = new Salary();
        incomeSource1.setId("is1");
        incomeSource1.setBaseAnnualSalary(BigDecimal.valueOf(100000.00));
        incomeSource1.setJob(job1);
        incomeSource2 = new Bonus();
        incomeSource2.setId("is2");
        incomeSource2.setSalary(incomeSource1);
        incomeSource2.setBonusPct(BigDecimal.valueOf(10.0));
        MonthDay foo = MonthDay.of(Month.JANUARY, 2);
        incomeSource2.setBonusDay(MonthDay.of(Month.JUNE, 6));
        incomeSource2.setJob(job1);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void getId() throws Exception {
        String name1 = incomeSource1.getId();
        assertEquals(name1, "is1");
        String name2 = incomeSource2.getId();
        assertEquals(name2, "is2");
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
        assertEquals("{\"type\":\"salary\",\"id\":\"is1\",\"source\":null,\"job\":\"job1\",\"baseAnnualSalary\":100000.0}", incomeSource1Str);

        String incomeSource2Str = writer.writeValueAsString(incomeSource2);
        assertEquals("{\"type\":\"bonus\",\"id\":\"is2\",\"source\":null,\"job\":\"job1\",\"salary\":\"is1\",\"bonusPct\":10.0,\"bonusDay\":\"--06-06\"}", incomeSource2Str);
    }


    @Test
    public void deserialize() throws Exception {
        String incomeSource1aStr = "{\"type\":\"salary\",\"id\":\"is1\",\"source\":null,\"job\":\"job1\",\"baseAnnualSalary\":100000.0}";
        String incomeSource2aStr = "{\"type\":\"bonus\",\"id\":\"is2\",\"source\":null,\"job\":\"job1\",\"salary\":\"is1\",\"bonusPct\":10.0,\"bonusDay\":\"--06-06\"}";

        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        InjectableValues injects = new InjectableValues.Std().addValue("jobManager", jobManager);
        mapper.setInjectableValues(injects);
        ObjectWriter writer = mapper.writer();

        IncomeSource incomeSource1a = mapper.readValue(incomeSource1aStr, IncomeSource.class);
        assertEquals("is1a", incomeSource1a.getId());

        IncomeSource incomeSource2a = mapper.readValue(incomeSource2aStr, IncomeSource.class);
        assertEquals("is2a", incomeSource2a.getId());
    }

}