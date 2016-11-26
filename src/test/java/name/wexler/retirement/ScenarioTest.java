package name.wexler.retirement;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by mwexler on 8/13/16.
 */
public class ScenarioTest {
    Scenario scenario1;
    Scenario scenario2;
    Context context;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        Person mike = new Person(context, "mike");
        Company yahoo = new Company(context, "yahoo1");
        Job job1 = new Job(context, "job1", "yahoo", "mike");
        Salary salary1 = new Salary(context, "salary1", "job1");
        Company bankOfNowhere = new Company(context, "bon1");
        Debt debt1 = new Debt(context, "debt1", "bon1");
        String[] is = {"salary1"};
        String[] es = {"debt1"};
        scenario1 = new Scenario(context, "scenario1", is, es);
        scenario2 = new Scenario(context, "scenario2", is, es);
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void getName() throws Exception {
        String name1 = scenario1.getName();
        assertEquals(name1, "scenario1");
        String name2 = scenario2.getName();
        assertEquals(name2, "scenario2");
    }


    @Test
    public void equals() throws Exception {
        assertNotEquals(scenario1, scenario2);
    }

    @Test
    public void serialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        ObjectWriter writer = mapper.writer();
        String scenario1Str = scenario1.toJSON();
        assertEquals("{\"assumptions\":null,\"name\":\"scenario1\",\"incomeSources\":[\"salary1\"],\"expenseSources\":[\"debt1\"],\"numYears\":51}", scenario1Str);

        String scenario2Str = scenario2.toJSON();
        assertEquals("{\"assumptions\":null,\"name\":\"scenario2\",\"incomeSources\":[\"salary1\"],\"expenseSources\":[\"debt1\"],\"numYears\":51}", scenario2Str);
    }


    @Test
    public void deserialize() throws Exception {
        String scenario1aStr = "{\"assumptions\":null,\"incomeSources\":[],\"name\":\"scenario1a\",\"expenseSources\":[],\"numYears\":51}";
        String scenario2aStr = "{\"assumptions\":null,\"incomeSources\":[],\"name\":\"scenario2a\",\"expenseSources\":[],\"numYears\":51}";

        Scenario scenario1a = Scenario.fromJSON(context, scenario1aStr);
        assertEquals("scenario1a", scenario1a.getName());

        Scenario sceanrio2a = Scenario.fromJSON(context, scenario2aStr);
        assertEquals("scenario2a", sceanrio2a.getName());
    }

}