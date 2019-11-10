package name.wexler.retirement.visualizer;

import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowFrequency.*;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AccountTest {
    private AssetAccount a1;
    private Scenario scenario;

    @Before
    public void setUp() throws Exception {
        Assumptions assumptions = new Assumptions();

        Context context = new Context();
        context.setAssumptions(assumptions);
        List<String> payees = new ArrayList<>();
        List<String> payors = new ArrayList<>();
        new Monthly(context, "a1", LocalDate.now(), LocalDate.now(), LocalDate.now(),
                CashFlowFrequency.ApportionmentPeriod.EQUAL_MONTHLY);
        String[] cashFlowSources = new String[0];
        String[] assets = new String[0];
        String[] liabilities = new String[0];
        String[] accounts = new String[0];
        scenario = new Scenario(context, "MyScenario", "My Scenario", cashFlowSources, assets, liabilities, accounts, assumptions);
        List<String> owners = Collections.singletonList("o1");
        CashBalance initialBalance = new CashBalance(LocalDate.of(2017, Month.APRIL, 30), BigDecimal.ZERO);
        List<CashBalance> interimBalances = Collections.singletonList(new CashBalance(LocalDate.of(2010, 12, 31), BigDecimal.valueOf(5.45)));
        List<ShareBalance> securities = Collections.singletonList(new ShareBalance(context, LocalDate.of(2014, 10, 31), BigDecimal.ONE, BigDecimal.TEN, "s1"));

        a1 = new AssetAccount(context, "a1", owners, initialBalance,  interimBalances, "Test AssetAccount",
                "Bank of Banking", null);
    }

    @Test
    public void getId() {
        assertEquals("a1", a1.getId());
    }

    public void getName() {
        assertEquals("Test AssetAccount", a1.getName());
    }


    public void getAccountValue() {
        Balance accountBalance = a1.getBalanceAtDate(scenario, LocalDate.of(2015, 10, 1));
        assertEquals(BigDecimal.ZERO, accountBalance.getValue());
    }
}