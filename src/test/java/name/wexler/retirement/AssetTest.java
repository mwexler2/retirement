package name.wexler.retirement;

import name.wexler.retirement.CashFlow.Balance;
import name.wexler.retirement.CashFlow.CashFlowType;
import name.wexler.retirement.CashFlow.Monthly;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by mwexler on 8/13/16.
 */
public class AssetTest {
    private Asset asset;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Context();
        Person owner = new Person(context, "owner1");
        List<String> ownerIds = Arrays.asList(owner.getId());
        String[] streetAddress = {"123 Main Street"};
        List<Balance> interimBalances = Arrays.asList(new Balance(LocalDate.of(2014, Month.JANUARY, 1), BigDecimal.valueOf(25334.02)));
        Balance initialBalance = new Balance(LocalDate.of(2010, Month.APRIL, 15), BigDecimal.valueOf(100000.00));
        asset = new RealProperty(context, "real-property1", ownerIds, initialBalance,
                streetAddress,
                "Anytown", "Count County", "AS", "01234", "US",
                interimBalances);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void getId() throws Exception {
        String name1 = asset.getId();
        assertEquals(name1, "real-property1");
    }


    @Test
    public void equals() throws Exception {
        assertEquals(asset, asset);
    }

    @Test
    public void toJSON() throws Exception {
        String asset1Str = context.toJSON(asset);
        assertEquals("{\"type\":\"real-property\",\"id\":\"real-property1\",\"owner\":\"owner1\",\"initialBalance\":{\"balanceDate\":\"2010-04-15\",\"value\":100000.0},\"address\":[\"123 Main Street\"],\"city\":\"Anytown\",\"county\":\"Count County\",\"state\":\"AS\",\"zipCode\":\"01234\",\"country\":\"US\"}", asset1Str);
    }


    @Test
    public void deserialize() throws Exception {
        String asset1aStr = "{\"type\":\"real-property\",\"id\":\"real-property1a\",\"owner\":\"owner1\",\"initialBalance\":{\"balanceDate\":\"2014-04-15\",\"value\":100000.0},\"interimBalances\":[],\"address\":[\"123 Main Street\"],\"city\":\"Anytown\",\"county\":\"Count County\",\"state\":\"AS\",\"zipCode\":\"01234\",\"country\":\"US\"}";
        Asset asset1a = context.fromJSON(Asset.class, asset1aStr);
        assertEquals("real-property1a", asset1a.getId());
    }

}