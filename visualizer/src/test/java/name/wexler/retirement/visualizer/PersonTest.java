package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Entity.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.time.LocalDate;
import java.time.Month;

/**
 * Created by mwexler on 8/13/16.
 */
public class PersonTest {
    private Person person1;
    private Person person2;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        context.setAssumptions(new Assumptions());
        person1 = new Person(context, "john1", LocalDate.of(1999, Month.DECEMBER, 25), 70);
        person1.setFirstName("John");
        person1.setLastName("Doe");
        person2 = new Person(context, "jane1", LocalDate.of(1969, Month.DECEMBER, 31), 40);
        person2.setFirstName("Jane");
        person2.setLastName("Doe");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void getName() {
        String name1 = person1.getName();
        assertEquals(name1, "John Doe");
        String name2 = person2.getName();
        assertEquals(name2, "Jane Doe");
    }

    @Test
    public void getFirstName() {
        String name1 = person1.getFirstName();
        assertEquals(name1, "John");
        String name2 = person2.getFirstName();
        assertEquals(name2, "Jane");
    }

    @Test
    public void setFirstName() {
        person1.setFirstName("Johnny");
        assertEquals(person1.getFirstName(), "Johnny");
    }

    @Test
    public void getLastName() {
        String name1 = person1.getLastName();
        assertEquals(name1, "Doe");
        String name2 = person2.getLastName();
        assertEquals(name2, "Doe");
    }

    @Test
    public void setLastName() {
        person1.setLastName("Dough");
        assertEquals(person1.getLastName(), "Dough");
    }

    @Test
    public void getBirthDate() {
        assertEquals(person1.getBirthDate(), LocalDate.of(1999, Month.DECEMBER, 25));
        assertEquals(person2.getBirthDate(), LocalDate.of(1969, Month.DECEMBER, 31));
    }

    @Test
    public void getRetirementAge() {
        assertEquals(person1.getRetirementAge(), 70);
        assertEquals(person2.getRetirementAge(), 40);

    }

    @Test
    public void equals() throws Exception {
        Person person1a = new Person(context, "john1a", LocalDate.of(1970, Month.JANUARY, 1), 65);
        person1a.setFirstName("John");
        person1a.setLastName("Doe");
        assertNotEquals(person1, person1a);
        assertNotEquals(person1, person2);
    }

    @Test
    public void deserialize() throws Exception {

        String person1Str = "{\"type\":\"person\",\"id\":\"john1a\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"birthDate\":\"1970-01-01\",\"retirementAge\":65}";
        String person2Str ="{\"type\":\"person\",\"id\":\"jane1a\",\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"birthDate\":\"1969-12-31\",\"retirementAge\":40}";

        Person person1a = (Person) context.<Entity>fromJSON(Entity.class, person1Str);
        assertEquals(person1a.getId(), "john1a");

        Person person2a = (Person) context.<Entity>fromJSON(Entity.class, person2Str);
        assertEquals(person2a.getId(), "jane1a");


    }

}