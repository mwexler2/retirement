package name.wexler.retirement;

import com.fasterxml.jackson.databind.InjectableValues;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.time.LocalDate;
import java.time.Month;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Created by mwexler on 8/13/16.
 */
public class PersonTest {
    Person person1;
    Person person2;
    EntityManager entityManager;

    @Before
    public void setUp() throws Exception {
        this.entityManager = new EntityManager();
        person1 = new Person(entityManager, "john1");
        person1.setFirstName("John");
        person1.setLastName("Doe");
        person1.setBirthDate(LocalDate.of(1970, Month.JANUARY, 1));
        person1.setRetirementAge(65);
        person2 = new Person(entityManager, "jane1");
        person2.setFirstName("Jane");
        person2.setLastName("Doe");
        person2.setBirthDate(LocalDate.of(1969, Month.DECEMBER, 31));
        person2.setRetirementAge(40);
    }

    @After
    public void tearDown() throws Exception {
        entityManager.removeAllEntities();
    }

    @Test
    public void getName() throws Exception {
        String name1 = person1.getName();
        assertEquals(name1, "John Doe");
        String name2 = person2.getName();
        assertEquals(name2, "Jane Doe");
    }

    @Test
    public void getFirstName() throws Exception {
        String name1 = person1.getFirstName();
        assertEquals(name1, "John");
        String name2 = person2.getFirstName();
        assertEquals(name2, "Jane");
    }

    @Test
    public void setFirstName() throws Exception {
        person1.setFirstName("Johnny");
        assertEquals(person1.getFirstName(), "Johnny");
    }

    @Test
    public void getLastName() throws Exception {
        String name1 = person1.getLastName();
        assertEquals(name1, "Doe");
        String name2 = person2.getLastName();
        assertEquals(name2, "Doe");
    }

    @Test
    public void setLastName() throws Exception {
        person1.setLastName("Dough");
        assertEquals(person1.getLastName(), "Dough");
    }

    @Test
    public void getBirthDate() throws Exception {
        assertEquals(person1.getBirthDate(), LocalDate.of(1970, Month.JANUARY, 1));
        assertEquals(person2.getBirthDate(), LocalDate.of(1969, Month.DECEMBER, 31));
    }

    @Test
    public void setBirthDate() throws Exception {
        person1.setBirthDate(LocalDate.of(2000, Month.APRIL, 4));
        assertEquals(person1.getBirthDate(), LocalDate.of(2000, Month.APRIL, 4));
    }

    @Test
    public void getRetirementAge() throws Exception {
        assertEquals(person1.getRetirementAge(), 65);
        assertEquals(person2.getRetirementAge(), 40);

    }

    @Test
    public void setRetirementAge() throws Exception {
        person1.setRetirementAge(100);
        assertEquals(person1.getRetirementAge(), 100);
    }


    @Test
    public void equals() throws Exception {
        Person person1a = new Person(entityManager, "john1a");
        person1a.setFirstName("John");
        person1a.setLastName("Doe");
        person1a.setBirthDate(LocalDate.of(1970, Month.JANUARY, 1));
        person1a.setRetirementAge(65);
        assertNotEquals(person1, person1a);
        assertNotEquals(person1, person2);
    }

    @Test
    public void serialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");

        ObjectWriter writer = mapper.writer();
        String person1Str = writer.writeValueAsString(person1);
        assertEquals("{\"type\":\"person\",\"id\":\"john1\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"birthDate\":\"1970-01-01\",\"retirementAge\":65}", person1Str);

        String person2Str = writer.writeValueAsString(person2);
        assertEquals("{\"type\":\"person\",\"id\":\"jane1\",\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"birthDate\":\"1969-12-31\",\"retirementAge\":40}", person2Str);
    }


    @Test
    public void deserialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        InjectableValues injects = new InjectableValues.Std().addValue("entityManager", entityManager);
        mapper.setInjectableValues(injects);

        String person1Str = "{\"type\":\"person\",\"id\":\"john1a\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"birthDate\":\"1970-01-01\",\"retirementAge\":65}";
        String person2Str ="{\"type\":\"person\",\"id\":\"jane1a\",\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"birthDate\":\"1969-12-31\",\"retirementAge\":40}";

        Person person1a = mapper.readValue(person1Str, Person.class);
        assertEquals(person1a.getId(), "john1a");

        Person person2a = mapper.readValue(person2Str, Person.class);
        assertEquals(person2a.getId(), "jane1a");


    }

}