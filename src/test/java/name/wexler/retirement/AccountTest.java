package name.wexler.retirement;

import name.wexler.retirement.Asset.Account;
import name.wexler.retirement.CashFlow.Balance;
import name.wexler.retirement.CashFlow.CashBalance;
import name.wexler.retirement.CashFlow.ShareBalance;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AccountTest {
    private Context context;
    private Account a1;
    private Assumptions assumptions;

    @Before
    public void setUp() throws Exception {
        assumptions = new Assumptions();
        context = new Context();
        context.setAssumptions(assumptions);
        List<String> owners = Arrays.asList("o1");
        CashBalance initialBalance = new CashBalance(LocalDate.of(2017, 04, 30), BigDecimal.ZERO);
        List<CashBalance> interimBalances = Arrays.asList(new CashBalance(LocalDate.of(2010, 12, 31), BigDecimal.valueOf(5.45)));
        List<ShareBalance> securities = Arrays.asList(new ShareBalance(context, LocalDate.of(2014, 10, 31), BigDecimal.ONE, BigDecimal.TEN, "s1"));

        a1 = new Account(context, "a1", owners, initialBalance,  interimBalances, "Test Account", "Bank of Banking");
    }

    @Test
    public void getId() {
        assertEquals("a1", a1.getId());
    }

    public void getName() {
        assertEquals("Test Account", a1.getName());
    }

    public void getAccountName() {
        assertEquals("Test Account", a1.getAccountName());
    }

    public void getInstitutionName() {
        assertEquals("Bank of Banking", a1.getInstitutionName());
    }

    public void getSecurities() {
        List<ShareBalance> securites = a1.getSecurities();
        assertEquals(0, securites.size());
    }

    public void getAccountValue() {
        Balance accountBalance = a1.getBalanceAtDate(LocalDate.of(2015, 10, 1));
        assertEquals(BigDecimal.ZERO, ((Balance) accountBalance).getValue());
    }
}