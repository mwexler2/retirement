package name.wexler.retirement;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import name.wexler.retirement.CashFlow.CashFlowSource;
import name.wexler.retirement.CashFlow.Monthly;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by mwexler on 8/13/16.
 */
public class DebtTest {
    Debt debt;

    @Before
    public void setUp() throws Exception {
        EntityManager<Entity> entityManager = new EntityManager<Entity>();
        Company lender = new Company(entityManager, "lender1");
        Person borrower = new Person(entityManager, "borrower1");
        String[] streetAddress = {"123 Main Street"};
        Asset asset = new RealProperty("real-property1", borrower, BigDecimal.valueOf(100000.00), LocalDate.of(2010, Month.APRIL, 15),
                streetAddress,
                "Anytown", "Count County", "AS", "01234", "US");
        Person[] borrowers = { borrower };
        CashFlowSource monthly = new Monthly("monthly-debt1", 14, LocalDate.of(2011, Month.MAY, 1), LocalDate.of(2031, Month.APRIL, 1));
        debt = new Debt("debt1", lender, borrowers, asset,
                LocalDate.of(2014, Month.OCTOBER, 10), 30 * 12, BigDecimal.valueOf(3.875/12), BigDecimal.valueOf(50000.0),
                BigDecimal.valueOf(500.00), monthly);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void getId() throws Exception {
        String name1 = debt.getId();
        assertEquals(name1, "debt1");
    }


    @Test
    public void equals() throws Exception {
        assertEquals(debt, debt);
    }

    @Test
    public void serialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");

        ObjectWriter writer = mapper.writer();
        String expenseSource1Str = writer.writeValueAsString(debt);
        assertEquals("{\"type\":\"debt\",\"id\":\"debt1\",\"source\":\"monthly-debt1\",\"lender\":\"lender1\",\"borrowers\":[\"borrower1\"],\"security\":\"real-property1\",\"startDate\":\"2014-10-10\",\"term\":360,\"interestRate\":0.3229166666666667,\"startingBalance\":50000.0,\"paymentAmount\":500.0}", expenseSource1Str);
    }


    @Test
    public void deserialize() throws Exception {
        String expenseSource1aStr = "{\"type\":\"debt\",\"id\":\"debt1\",\"source\":null,\"job\":\"job1\",\"baseAnnualSalary\":100000.0}";

        ObjectMapper mapper = new ObjectMapper().enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
        ObjectWriter writer = mapper.writer();

        ExpenseSource expenseSource1a = mapper.readValue(expenseSource1aStr, ExpenseSource.class);
        assertEquals("debt1", expenseSource1a.getId());
    }

}