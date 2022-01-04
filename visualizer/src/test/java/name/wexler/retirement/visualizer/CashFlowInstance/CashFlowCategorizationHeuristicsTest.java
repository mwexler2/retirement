package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.AccountReader;
import name.wexler.retirement.visualizer.CashFlowEstimator.Salary;
import name.wexler.retirement.visualizer.CashFlowSource;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Category;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Expense.Spending;
import name.wexler.retirement.visualizer.Job;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.stream.JsonParserFactory;
import javax.json.stream.JsonParsingException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CashFlowCategorizationHeuristicsTest {
    private static JsonParserFactory jsonParserFactory = Json.createParserFactory(Collections.emptyMap());
    private AccountReader accountReader = new AccountReader();
    private Context context = new Context(accountReader);
    private Spending spending = mock(Spending.class);
    private CashFlowSource cashFlowSource = mock(CashFlowSource.class);
    private Account account = mock(Account.class);
    private ResultSet rs = mock(ResultSet.class);
    private Job job = mock(Job.class);
    private final static String BLANK = "";

    @Before
    public void setUp() throws Exception {
        when(rs.getString("parent")).thenReturn(BLANK);
        when(rs.getString("category")).thenReturn(BLANK);
        when(rs.getString("txn_type")).thenReturn(BLANK);
        when(rs.getLong("Date")).thenReturn(0L);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("fi")).thenReturn(BLANK);
        when(rs.getString("notes")).thenReturn(BLANK);
        when(rs.getString("labels")).thenReturn(BLANK);
        when(rs.getString("itemType")).thenReturn(BLANK);
        when(rs.getString("symbol")).thenReturn(null);
        when(rs.getString("shares")).thenReturn(null);
    }

    @Test(expected = NullPointerException.class)
    public void getInstanceNullDescription() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {

        CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, null, job);
    }

    @Test
    public void getInstanceNullParentCategory() throws
            SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {

        when(rs.getString("parent")).thenReturn(null);
        when(rs.getString("category")).thenReturn(BLANK);
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, "", job);
        assertEquals(Category.UNKNOWN, instance.getParentCategory());
    }

    @Test(expected = NullPointerException.class)
    public void getInstanceNullCategory() throws
            SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {

        when(rs.getString("category")).thenReturn(null);
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, "", job);
    }

    @Test
    public void getInstanceNullTxnType() throws
            SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {

        when(rs.getString("txnType")).thenReturn(null);
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, "", job);
        assert(instance != null);
    }

    @Test
    public void getInstanceInterestIncome() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "Bank Int 123", job);
        assertEquals(Category.INTEREST_INCOME, instance.getCategory());
        assertEquals(Category.INVESTMENT, instance.getParentCategory());
        assertEquals(Category.INCOME_ITEM_TYPE, instance.getItemType());

        instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "Interest 2345", job);
        assertEquals(Category.INTEREST_INCOME, instance.getCategory());
        assertEquals(Category.INVESTMENT, instance.getParentCategory());
        assertEquals(Category.INCOME_ITEM_TYPE, instance.getItemType());

        instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "My Bank ACCOUNT INTEREST", job);
        assertEquals(Category.INTEREST_INCOME, instance.getCategory());
        assertEquals(Category.INVESTMENT, instance.getParentCategory());
        assertEquals(Category.INCOME_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstanceTransfer() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "Tfr 123", job);
        assertEquals(Category.TRANSFER_CATEGORY, instance.getCategory());
        assertEquals(Category.TRANSFER_CATEGORY, instance.getParentCategory());
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());

        when(rs.getString("category")).thenReturn(Category.TRANSFER_CATEGORY);
        instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assertEquals(Category.TRANSFER_CATEGORY, instance.getCategory());
        assertEquals(Category.TRANSFER_CATEGORY, instance.getParentCategory());
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstanceJournal() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "JOURNAL 123", job);
        assertEquals(Category.JOURNAL_CATEGORY, instance.getCategory());
        assertEquals(Category.TRANSFER_CATEGORY, instance.getParentCategory());
        assertEquals(Category.INCOME_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstanceOverdraft() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "Overdraft 123", job);
        assertEquals(Category.OVERDRAFT_CATEGORY, instance.getCategory());
        assertEquals(Category.TRANSFER_CATEGORY, instance.getParentCategory());
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstanceCheck() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "Check: 123", job);
        assertEquals(Category.CHECK_CATEGORY, instance.getCategory());
        assertEquals(Category.UNCATEGORIZED_CATEGORY, instance.getParentCategory());
        assertEquals(Category.EXPENSE_ITEM_TYPE, instance.getItemType());

        when(rs.getString("txn_type")).thenReturn("CHECK");
        instance = CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assertEquals(Category.CHECK_CATEGORY, instance.getCategory());
        assertEquals(Category.UNCATEGORIZED_CATEGORY, instance.getParentCategory());
        assertEquals(Category.EXPENSE_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstanceInvestmentTransfer() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "Olink Tid 17", job);
        assertEquals(Category.TRANSFER_CATEGORY, instance.getCategory());
        assertEquals(Category.INVESTMENT, instance.getParentCategory());
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstancePayrollContribution() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "Payroll Contribution", job);
        assertEquals(Category.PAYROLL_CONTRIBUTION_CATEGORY, instance.getCategory());
        assertEquals(Category.INVESTMENT, instance.getParentCategory());
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstanceFundsReceived() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "FUNDS RECEIVED blah blah blah", job);
        assertEquals(Category.FUNDS_RECEIVED_CATEGORY, instance.getCategory());
        assertEquals(Category.TRANSFER_CATEGORY, instance.getParentCategory());
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstancePlanContribution() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "Foo Bank - Plan Contribution", job);
        assertEquals(Category.CONTRIBUTION_CATEGORY, instance.getCategory());
        assertEquals(Category.RETIREMENT_CATEGORY, instance.getParentCategory());
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstanceBankFee() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "WAIVE the fees for being a nice person.", job);
        assertEquals(Category.BANK_FEE_CATEGORY, instance.getCategory());
        assertEquals(Category.FEES_AND_CHARGES_CATEGORY, instance.getParentCategory());
        assertEquals(Category.EXPENSE_ITEM_TYPE, instance.getItemType());

        when(rs.getString("txn_type")).thenReturn("FEE");
        instance = CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assertEquals(Category.BANK_FEE_CATEGORY, instance.getCategory());
        assertEquals(Category.FEES_AND_CHARGES_CATEGORY, instance.getParentCategory());
        assertEquals(Category.EXPENSE_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstancePrincipal() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "US TREASURY FOO MATURED", job);
        assertEquals(Category.PRINCIPAL_CATEGORY, instance.getCategory());
        assertEquals(Category.INVESTMENT, instance.getParentCategory());
        assertEquals(Category.INCOME_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstanceDividendAndCapitalGains() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "Here is your CASH DIV, thank you very much.", job);
        assertEquals(Category.DIVIDENDS_AND_CAPITAL_GAINS_CATEGORY, instance.getCategory());
        assertEquals(Category.INVESTMENT, instance.getParentCategory());
        assertEquals(Category.INCOME_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstanceItemTypeNull() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        when(rs.getString("itemType")).thenReturn(null);

        when(rs.getString("txn_type")).thenReturn("DEBIT");
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assertEquals(Category.UNCATEGORIZED_CATEGORY, instance.getCategory());
        assertEquals(Category.UNCATEGORIZED_CATEGORY, instance.getParentCategory());
        assertEquals(Category.EXPENSE_ITEM_TYPE, instance.getItemType());

        when(rs.getString("txn_type")).thenReturn("CREDIT");
        instance = CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assertEquals(Category.UNCATEGORIZED_CATEGORY, instance.getCategory());
        assertEquals(Category.UNCATEGORIZED_CATEGORY, instance.getParentCategory());
        assertEquals(Category.INCOME_ITEM_TYPE, instance.getItemType());

        when(rs.getString("txn_type")).thenReturn("OTHER_RANDOM_NONSENSE");
        instance = CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assertEquals(Category.UNCATEGORIZED_CATEGORY, instance.getCategory());
        assertEquals(Category.UNCATEGORIZED_CATEGORY, instance.getParentCategory());
        assertEquals(Category.EXPENSE_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstanceSalary() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "Twist Bioscience", job);
        assertEquals(Category.SALARY_CATEGORY, instance.getCategory());
        assertEquals(Category.SALARY_CATEGORY, instance.getParentCategory());
        assertEquals(Category.INCOME_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstanceTransferItemType() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "This is Morgan Stanley ACH haha!", job);
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());

        instance = CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                        "Its a MoneyLink!", job);
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());
    }

    @Test
    public void getInstanceInvestmentTransfers() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        when(rs.getString("txn_type")).thenReturn("transfer");
        when(rs.getString("category")).thenReturn("Investments");

        when(rs.getBigDecimal("shares")).thenReturn(BigDecimal.ONE);
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());
        assertEquals(Category.INVESTMENT, instance.getParentCategory());
        assertEquals(Category.REINVEST_SHARES_CATEGORY, instance.getCategory());

        when(rs.getBigDecimal("shares")).thenReturn(null);
        instance = CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                "REINVESTMENT in foreign doodads", job);
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());
        assertEquals(Category.INVESTMENT, instance.getParentCategory());
        assertEquals(Category.REINVEST_DIVIDENDS_CATEGORY, instance.getCategory());

        when(rs.getBigDecimal("shares")).thenReturn(null);
        instance = CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                "blah blah blah", job);
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());
        assertEquals(Category.INVESTMENT, instance.getParentCategory());
        assertEquals(Category.CONTRIBUTION_CATEGORY, instance.getCategory());

        when(rs.getBigDecimal("shares")).thenReturn(BigDecimal.ZERO);
        when(rs.getString("category")).thenReturn("Foo bar");
        when(rs.getString("symbol")).thenReturn("IBM");
        instance = CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource,
                "blah blah blah", job);
        assertEquals(Category.TRANSFER_ITEM_TYPE, instance.getItemType());
        assertEquals(Category.INVESTMENT, instance.getParentCategory());
        assertEquals(Category.REINVEST_DIVIDENDS_CATEGORY, instance.getCategory());
    }

    @Test(expected = RuntimeException.class)
    public void getInstanceTxnAmountNotNumber() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        when(rs.getBigDecimal("amount")).thenThrow(new NumberFormatException("foo is not a number"));
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assertEquals(null, instance);
    }

    @Test(expected = CashFlowCategorizationHeuristics.AccountNotFoundException.class)
    public void getInstanceDebitNullCompany() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        when(rs.getBoolean("isDebit")).thenReturn(true);
        when(rs.getString("txn_type")).thenReturn("EXPENSE");
        when(account.getCompany()).thenReturn(null);
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assertEquals(null, instance);
    }

    @Test(expected = CashFlowCategorizationHeuristics.AccountNotFoundException.class)
    public void getInstancePaycheckNullCompany() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        when(rs.getBoolean("isDebit")).thenReturn(false);
        when(rs.getString("txn_type")).thenReturn("EXPENSE");
        when(account.getCompany()).thenReturn(null);
        when(rs.getString("category")).thenReturn("Paycheck");

        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, null, BLANK, job);
        assertEquals(spending, instance.getCashFlowSource());
        assert(instance instanceof PaymentInstance);
    }

    @Test(expected = CashFlowCategorizationHeuristics.AccountNotFoundException.class)
    public void getInstanceReibumrsementNullCompany() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        when(rs.getBoolean("isDebit")).thenReturn(false);
        when(rs.getString("txn_type")).thenReturn("EXPENSE");
        when(account.getCompany()).thenReturn(null);
        when(rs.getString("category")).thenReturn("Reimbursement");

        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, null, BLANK, job);
        assertEquals(spending, instance.getCashFlowSource());
        assert(instance instanceof PaymentInstance);
    }

    @Test
    public void getInstanceDebits() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        when(rs.getBoolean("isDebit")).thenReturn(true);
        when(rs.getString("txn_type")).thenReturn("EXPENSE");
        when(account.getCompany()).thenReturn(mock(Entity.class));
        when(rs.getBigDecimal("amount")).thenReturn(BigDecimal.TEN);

        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, null, BLANK, job);
        assertEquals(spending, instance.getCashFlowSource());
        assert(instance instanceof PaymentInstance);

        instance = CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assert(instance instanceof PaymentInstance);
    }

    @Test
    public void getInstancePaycheck() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        when(rs.getBoolean("isDebit")).thenReturn(false);
        when(rs.getString("txn_type")).thenReturn("EXPENSE");
        when(account.getCompany()).thenReturn(mock(Entity.class));
        when(rs.getString("category")).thenReturn("Paycheck");
        when(rs.getBigDecimal("amount")).thenReturn(BigDecimal.TEN);

        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assert(instance instanceof PaycheckInstance);
    }

    @Test
    public void getInstanceReimbursement() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        when(rs.getBoolean("isDebit")).thenReturn(false);
        when(rs.getString("txn_type")).thenReturn("EXPENSE");
        when(account.getCompany()).thenReturn(mock(Entity.class));
        when(rs.getString("category")).thenReturn("Reimbursement");
        when(rs.getBigDecimal("amount")).thenReturn(BigDecimal.TEN);

        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assert(instance instanceof ReimbursementInstance);
    }

    @Test
    public void getInstanceLabels() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        when(rs.getBoolean("isDebit")).thenReturn(false);
        when(rs.getString("txn_type")).thenReturn("EXPENSE");
        when(account.getCompany()).thenReturn(mock(Entity.class));
        when(rs.getString("category")).thenReturn("Reimbursement");

        when(rs.getString("labels")).thenReturn("[]");
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assertEquals(0, instance.getLabels().size());

        when(rs.getString("labels")).thenReturn("[{\"name\": \"label-name\"}]");
        instance = CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assertEquals(1, instance.getLabels().size());
    }

    @Test(expected = JsonParsingException.class)
    public void getInstanceLabelsInvalidJSON() throws SQLException, CashFlowCategorizationHeuristics.AccountNotFoundException {
        when(rs.getBoolean("isDebit")).thenReturn(false);
        when(rs.getString("txn_type")).thenReturn("EXPENSE");
        when(account.getCompany()).thenReturn(mock(Entity.class));
        when(rs.getString("category")).thenReturn("Reimbursement");

        when(rs.getString("labels")).thenReturn("bad-json!");
        CashFlowInstance instance =
                CashFlowCategorizationHeuristics.getInstance(context, account, spending, rs, cashFlowSource, BLANK, job);
        assertEquals(0, instance.getLabels().size());

    }
}