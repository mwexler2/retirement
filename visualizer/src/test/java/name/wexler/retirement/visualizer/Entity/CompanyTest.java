package name.wexler.retirement.visualizer.Entity;

import name.wexler.retirement.visualizer.AccountReader;
import name.wexler.retirement.visualizer.Assumptions;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Entity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by mwexler on 8/13/16.
 */
public class CompanyTest {
    private Context context;
    private Company company1;
    private Company company2;

    @Before
    public void setUp() throws Exception {
        AccountReader accountReader = mock(AccountReader.class);
        context = new Context(accountReader);
        context.setAssumptions(new Assumptions());
        company1 = new Company(context, "comp1", "Company #1");
        company2 = new Company(context, "comp2", "Company #2");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void getName() {
        String name1 = company1.getName();
        assertEquals(name1, "Company #1");
        String name2 = company2.getName();
        assertEquals(name2, "Company #2");
    }

    @Test
    public void getCompanyName() {
        String name1 = company1.getCompanyName();
        assertEquals(name1, "Company #1");
        String name2 = company2.getCompanyName();
        assertEquals(name2, "Company #2");
    }

    @Test
    public void equals() {
        assertNotEquals(company1, company2);
    }


    @Test
    public void deserialize() throws Exception {
        String comp1aStr = "{\"type\":\"company\",\"id\":\"comp1a\",\"companyName\":\"IBM\"}";
        String comp2aStr = "{\"type\":\"company\",\"id\":\"comp2a\",\"companyName\":\"Xerox\"}";

        Company company1a = context.fromJSON(Entity.class, comp1aStr);
        assertEquals("comp1a", company1a.getId());
        assertEquals("IBM", company1a.getCompanyName());

        Company company2a = context.fromJSON(Entity.class, comp2aStr);
        assertEquals("comp2a", company2a.getId());
        assertEquals("Xerox", company2a.getCompanyName());
    }

}