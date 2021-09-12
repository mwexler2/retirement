package name.wexler.retirement.visualizer.Asset;

import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.Asset.RealProperty;
import name.wexler.retirement.visualizer.Assumptions;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
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
        context.setAssumptions(new Assumptions());
        Person owner = new Person(context, "owner1", LocalDate.of(1980, Month.APRIL, 15), 75,
                "Owner", "1");
        List<String> ownerIds = Collections.singletonList(owner.getId());
        String[] streetAddress = {"123 Main Street"};
        List<CashBalance> interimBalances = Collections.singletonList(new CashBalance(LocalDate.of(2014, Month.JANUARY, 1), BigDecimal.valueOf(25334.02)));
        CashBalance initialBalance = new CashBalance(LocalDate.of(2010, Month.APRIL, 15), BigDecimal.valueOf(100000.00));
        asset = new RealProperty(context, "real-property1", ownerIds, initialBalance,
                streetAddress,
                "Anytown", "Count County", "AS", "01234", "US",
                interimBalances);
    }

    @After
    public void tearDown() {
    }


    @Test
    public void getId() {
        String name1 = asset.getId();
        assertEquals(name1, "real-property1");
    }


    @Test
    public void equals() {
        assertEquals(asset, asset);
    }


    @Test
    public void deserialize() throws Exception {
        String asset1aStr = "{\"type\":\"real-property\",\"id\":\"real-property1a\",\"owners\":[\"owner1\"],\"initialBalance\":{\"balanceDate\":\"2014-04-15\",\"value\":100000.0},\"interimBalances\":[],\"address\":[\"123 Main Street\"],\"city\":\"Anytown\",\"county\":\"Count County\",\"state\":\"AS\",\"zipCode\":\"01234\",\"country\":\"US\"}";
        Asset asset1a = context.fromJSON(Asset.class, asset1aStr);
        assertEquals("real-property1a", asset1a.getId());
    }

}