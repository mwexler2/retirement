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
public class BonusTest {
    Salary salary;
    Bonus bonus;
    Context context;


    @Before
    public void setUp() throws Exception {
        context = new Context();

        Company employer = new Company(context, "employer1");
        Person employee = new Person(context, "employee1");
        Job job1 = new Job(context, "job1", employer, employee);
        job1.setStartDate(LocalDate.of(2015, Month.MAY, 1));
        job1.setEndDate(LocalDate.of(2016, Month.DECEMBER, 31));
        salary = new Salary(context, "salary1", "job1");
        salary.setBaseAnnualSalary(BigDecimal.valueOf(100000.00));
        bonus = new Bonus(context,  "bonus1", "job1", "salary1");
        bonus.setBonusPct(BigDecimal.valueOf(10.0));
        MonthDay foo = MonthDay.of(Month.JANUARY, 2);
        bonus.setBonusDay(MonthDay.of(Month.JUNE, 6));
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void getId() throws Exception {
        String name1 = salary.getId();
        assertEquals(name1, "salary1");
        String name2 = bonus.getId();
        assertEquals(name2, "bonus1");
    }


    @Test
    public void equals() throws Exception {
        assertNotEquals(salary, bonus);
    }

    @Test
    public void toJSON() throws Exception {
        String salary1Str = context.toJSON(bonus);
        assertEquals("{\"type\":\"bonus\",\"id\":\"bonus1\",\"source\":null,\"job\":\"job1\",\"salary\":\"salary1\",\"bonusPct\":10.0,\"bonusDay\":\"--06-06\"}", salary1Str);
    }


    @Test
    public void fromJSON() throws Exception {
        String bonus1Str = "{\"type\":\"bonus\",\"id\":\"bonus1a\",\"source\":null,\"job\":\"job1\",\"salary\":\"is1\",\"bonusPct\":10.0,\"bonusDay\":\"--06-06\"}";

        IncomeSource incomeSource2a = context.fromJSON(Bonus.class, bonus1Str);
        assertEquals("bonus1a", incomeSource2a.getId());
    }

}