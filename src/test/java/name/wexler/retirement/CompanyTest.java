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
public class CompanyTest {
    Context context;
    Company company1;
    Company company2;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        company1 = new Company(context, "comp1");
        company1.setCompanyName("IBM");
        company2 = new Company(context, "comp2");
        company2.setCompanyName("Xerox");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getName() throws Exception {
        String name1 = company1.getName();
        assertEquals(name1, "IBM");
        String name2 = company2.getName();
        assertEquals(name2, "Xerox");
    }

    @Test
    public void getCompanyName() throws Exception {
        String name1 = company1.getCompanyName();
        assertEquals(name1, "IBM");
        String name2 = company2.getCompanyName();
        assertEquals(name2, "Xerox");
    }

    @Test
    public void setCompanyName() throws Exception {
        company1.setCompanyName("International Business Machines");
        assertEquals(company1.getCompanyName(), "International Business Machines");
    }
    


    @Test
    public void equals() throws Exception {
        assertNotEquals(company1, company2);
    }

    @Test
    public void serialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        ObjectWriter writer = mapper.writer();
        String company1Str = writer.writeValueAsString(company1);
        assertEquals("{\"type\":\"company\",\"id\":\"comp1\",\"companyName\":\"IBM\"}", company1Str);

        String company2Str = writer.writeValueAsString(company2);
        assertEquals("{\"type\":\"company\",\"id\":\"comp2\",\"companyName\":\"Xerox\"}", company2Str);
    }


    @Test
    public void deserialize() throws Exception {
        String comp1aStr = "{\"type\":\"company\",\"id\":\"comp1a\",\"companyName\":\"IBM\"}";
        String comp2aStr = "{\"type\":\"company\",\"id\":\"comp2a\",\"companyName\":\"Xerox\"}";

        Company company1a = (Company) Company.fromJSON(context, comp1aStr);
        assertEquals("comp1a", company1a.getId());
        assertEquals("IBM", company1a.getCompanyName());

        Company company2a = (Company) Company.fromJSON(context, comp2aStr);
        assertEquals("comp2a", company2a.getId());
        assertEquals("Xerox", company2a.getCompanyName());
    }

}