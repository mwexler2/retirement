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

    @Before
    public void setUp() throws Exception {
        IncomeSource[] is = new IncomeSource[0];
        ExpenseSource[] es = new ExpenseSource[0];
        scenario1 = new Scenario("salary", is, es);
        scenario2 = new Scenario("bonus", is, es);
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void getName() throws Exception {
        String name1 = scenario1.getName();
        assertEquals(name1, "salary");
        String name2 = scenario2.getName();
        assertEquals(name2, "bonus");
    }


    @Test
    public void equals() throws Exception {
        assertNotEquals(scenario1, scenario2);
    }

    @Test
    public void serialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        ObjectWriter writer = mapper.writer();
        String scenario1Str = writer.writeValueAsString(scenario1);
        assertEquals("{\"assumptions\":null,\"incomeSources\":[],\"name\":\"salary\",\"years\":[2016,2017,2018,2019,2020,2021,2022,2023,2024,2025,2026,2027,2028,2029,2030,2031,2032,2033,2034,2035,2036,2037,2038,2039,2040,2041,2042,2043,2044,2045,2046,2047,2048,2049,2050,2051,2052,2053,2054,2055,2056,2057,2058,2059,2060,2061,2062,2063,2064,2065,2066],\"expenseSources\":[],\"numYears\":51}", scenario1Str);

        String scenario2Str = writer.writeValueAsString(scenario2);
        assertEquals("{\"assumptions\":null,\"incomeSources\":[],\"name\":\"bonus\",\"years\":[2016,2017,2018,2019,2020,2021,2022,2023,2024,2025,2026,2027,2028,2029,2030,2031,2032,2033,2034,2035,2036,2037,2038,2039,2040,2041,2042,2043,2044,2045,2046,2047,2048,2049,2050,2051,2052,2053,2054,2055,2056,2057,2058,2059,2060,2061,2062,2063,2064,2065,2066],\"expenseSources\":[],\"numYears\":51}", scenario2Str);
    }


    @Test
    public void deserialize() throws Exception {
        String scenario1aStr = "{\"assumptions\":null,\"incomeSources\":[],\"name\":\"scenario1a\",\"years\":[2016,2017,2018,2019,2020,2021,2022,2023,2024,2025,2026,2027,2028,2029,2030,2031,2032,2033,2034,2035,2036,2037,2038,2039,2040,2041,2042,2043,2044,2045,2046,2047,2048,2049,2050,2051,2052,2053,2054,2055,2056,2057,2058,2059,2060,2061,2062,2063,2064,2065,2066],\"expenseSources\":[],\"numYears\":51}";
        String scenario2aStr = "{\"assumptions\":null,\"incomeSources\":[],\"name\":\"scenario2a\",\"years\":[2016,2017,2018,2019,2020,2021,2022,2023,2024,2025,2026,2027,2028,2029,2030,2031,2032,2033,2034,2035,2036,2037,2038,2039,2040,2041,2042,2043,2044,2045,2046,2047,2048,2049,2050,2051,2052,2053,2054,2055,2056,2057,2058,2059,2060,2061,2062,2063,2064,2065,2066],\"expenseSources\":[],\"numYears\":51}";

        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        /* InjectableValues injects = new InjectableValues.Std().addValue("entityManager", entityManager);
        mapper.setInjectableValues(injects); */
        ObjectWriter writer = mapper.writer();

       Scenario scenario1a = mapper.readValue(scenario1aStr, Scenario.class);
        assertEquals("scenario1a", scenario1a.getName());

        Scenario sceanrio2a = mapper.readValue(scenario2aStr, Scenario.class);
        assertEquals("scenario2a", sceanrio2a.getName());
    }

}