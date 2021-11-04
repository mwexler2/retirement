package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowFrequency.*;
import name.wexler.retirement.visualizer.Entity.Company;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class AccountTest {
    private AssetAccount a1;

    @Before
    public void setUp() throws Exception {
        Assumptions assumptions = new Assumptions();

        Context context = new Context();
        context.setAssumptions(assumptions);

        new Monthly(context, "a1", LocalDate.now(), LocalDate.now(), LocalDate.now(),
                CashFlowFrequency.ApportionmentPeriod.EQUAL_MONTHLY);
        List<String> owners = Collections.singletonList("o1");

        Company bob = new Company(context, "BoB", "Bank of Banking");
        a1 = new AssetAccount(context, "a1", owners, "Test AssetAccount",
                bob.getId(), Collections.emptyList(), null, AccountReader.mintTxnSource);
    }

    @Test
    public void getId() {
        assertEquals("a1", a1.getId());
    }

    public void getName() {
        assertEquals("Test AssetAccount", a1.getName());
    }

}