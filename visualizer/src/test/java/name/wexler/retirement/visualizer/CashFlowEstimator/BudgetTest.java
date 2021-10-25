package name.wexler.retirement.visualizer.CashFlowEstimator;

import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.Asset.RealProperty;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.visualizer.CashFlowFrequency.Monthly;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Entity.Category;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Person;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class BudgetTest {
    Budget budget;
    Context context = new Context();
    CashFlowCalendar calendar;
    Person payer = new Person(context, "payer1",
            LocalDate.of(1980, Month.FEBRUARY, 29), 80,
            "Borrower", "Doe");
    Person notPayer = new Person(context, "notpayer1",
            LocalDate.of(1980, Month.FEBRUARY, 29), 80,
            "Borrower", "Roe");
    LocalDate endDate = LocalDate.of(2014, Month.OCTOBER, 10);

    public BudgetTest() throws Exception {}

    @Before
    public void setUp() throws Exception {
        context.setAssumptions(new Assumptions());
        calendar = mock(CashFlowCalendar.class);

        LocalDate accrueStart = LocalDate.of(2011, Month.MAY, 1);
        LocalDate accrueEnd = LocalDate.of(2031, Month.APRIL, 1);
        LocalDate firstPaymentDate = LocalDate.of(accrueStart.getYear(), accrueStart.getMonth(), 14);
        CashFlowFrequency monthly =
                new Monthly(context, "monthly-budget", accrueStart, accrueEnd, firstPaymentDate,
                        CashFlowFrequency.ApportionmentPeriod.ANNUAL);
        Company bank = new Company(context, "bank1", "Bank #1");
        CashFlowSource defaultSource = new AssetAccount(context,
                "checking1",
                Arrays.asList(payer.getId()),
                "Checking account 1",
                bank.getId(),
                Collections.emptyList(),
                null,
                AccountReader.mintTxnSource);
        budget = new Budget(context,
                "budget1",
                endDate,
                BigDecimal.TEN,
                monthly.getId(),
                defaultSource.getId(),
                Collections.emptyList(),
                Collections.emptyList());
    }

    @Test
    public void getEstimatedFutureCashFlows() {
        List<CashFlowInstance> cashFlows = budget.getEstimatedFutureCashFlows(calendar);
        assertEquals(0, cashFlows.size());
    }

    @Test
    public void getName() {
        assertEquals(budget.getClass().getSimpleName(), budget.getName());
    }

    @Test
    public void getSourceId() {
        assertEquals("monthly-budget", budget.getSourceId());
    }

    @Test
    public void getEndDate() {
        assertEquals(endDate, budget.getEndDate());
    }

    @Test
    public void getItemType() {
        assertEquals(Category.INCOME, budget.getItemType());
    }

    @Test
    public void isOwner() {
        assertEquals(true, budget.isOwner(payer));
        assertEquals(false, budget.isOwner(notPayer));
    }
}