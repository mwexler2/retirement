package name.wexler.retirement.financeCrawler;


import com.webcohesion.ofx4j.client.impl.LocalResourceFIDataStore;
import com.webcohesion.ofx4j.client.impl.FinancialInstitutionImpl;
import com.webcohesion.ofx4j.domain.data.MessageSetType;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.ResponseMessage;
import com.webcohesion.ofx4j.domain.data.ResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.banking.AccountType;
import com.webcohesion.ofx4j.domain.data.banking.BankAccountDetails;
import com.webcohesion.ofx4j.domain.data.common.Payee;
import com.webcohesion.ofx4j.domain.data.common.Transaction;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardAccountDetails;
import com.webcohesion.ofx4j.client.net.OFXV1Connection;
import com.webcohesion.ofx4j.domain.data.investment.accounts.InvestmentAccountDetails;
import com.webcohesion.ofx4j.domain.data.investment.statements.InvestmentStatementResponse;
import com.webcohesion.ofx4j.domain.data.investment.statements.InvestmentStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.investment.transactions.*;
import com.webcohesion.ofx4j.domain.data.seclist.*;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import com.webcohesion.ofx4j.io.OFXParseException;
import name.wexler.retirement.datastore.AccountTable;
import name.wexler.retirement.datastore.PositionHistory;
import name.wexler.retirement.datastore.TxnHistory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OFXCrawler {
    private PositionHistory positionHistory;
    private AccountTable accountTable;
    private TxnHistory txnHistory;
    private static final String ofx = "OFX";
    private Map<String, String> securityIdToTicker = new HashMap<>();

    OFXCrawler(PositionHistory positionHistory, AccountTable accountTable, TxnHistory txnHistory) {
        super();
        this.positionHistory = positionHistory;
        this.accountTable = accountTable;
        this.txnHistory = txnHistory;
    }

    public void crawl() {
        final String[] serverList = {"schwab-alt", "vanguard", "netbenefits"};
        positionHistory.deleteAllRows();
        for (String server : serverList) {
            String cmd = "/usr/local/bin/ofxget stmt " + server;
            try {
                Process p = Runtime.getRuntime().exec(cmd);
                AggregateUnmarshaller<ResponseEnvelope> unmarshaller = new AggregateUnmarshaller<ResponseEnvelope>(ResponseEnvelope.class);
                ResponseEnvelope responseEnvelope = unmarshaller.unmarshal(p.getInputStream());
                processResponse(responseEnvelope);
            } catch (OFXParseException ope) {
                System.err.println(ope);
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        }
    }

    private void processResponse(ResponseEnvelope responseEnvelope) {
        processSecurities(responseEnvelope);
        List<ResponseMessage> messages = responseEnvelope.getMessageSet(MessageSetType.investment).getResponseMessages();

        for (ResponseMessage message : messages) {
            if (message instanceof InvestmentStatementResponseTransaction) {
                InvestmentStatementResponse response = ((InvestmentStatementResponseTransaction) message).getMessage();
                Date statementDate = response.getDateOfStatement();
                InvestmentAccountDetails account = response.getAccount();
                final String accountId = account.getAccountNumber();
                processPositions(response, accountId, statementDate);
                processTransactions(response, accountId, statementDate);
            } else {
                System.out.println(message.getResponseMessageName());
            }
        }
    }

    private void processSecurities(ResponseEnvelope responseEnvelope) {
        SecurityList securityList = ((SecurityListResponseMessageSet) responseEnvelope.getMessageSet(MessageSetType.investment_security)).getSecurityList();

        for (BaseSecurityInfo securityInfo : securityList.getSecurityInfos()) {
            String ticker = securityInfo.getTickerSymbol();
            if (ticker == null)
                ticker = securityInfo.getSecurityId().getUniqueId();
            securityIdToTicker.put(securityInfo.getSecurityId().getUniqueId(), ticker);
        }
    }

    private void processPositions(InvestmentStatementResponse response, String accountId, Date statementDate) {
        if (response.getPositionList() != null) {
            response.getPositionList().getPositions().
                    forEach(position -> {
                                String uniqueId = position.getSecurityId().getUniqueId();
                                positionHistory.insertRow(
                                        statementDate.getTime(),
                                        securityIdToTicker.getOrDefault(uniqueId, uniqueId),
                                        accountId,
                                        BigDecimal.valueOf(position.getUnits()),
                                        position.getPositionType(),
                                        BigDecimal.valueOf(position.getUnitPrice()),
                                        BigDecimal.valueOf(position.getMarketValue()));
                            }
                    );
        }
    }

    private void updateTicker(Map<String, Object> line, String securityId) {
        String ticker = securityIdToTicker.getOrDefault(securityId, securityId);
        line.put("symbol", ticker);
    }

    private void processTransactions(InvestmentStatementResponse response, String accountId, Date statementDate) {
        InvestmentTransactionList investmentTransactionList = response.getInvestmentTransactionList();
        List<BaseInvestmentTransaction> investmentTxns = investmentTransactionList.getInvestmentTransactions();
        if (investmentTxns != null)
            for (BaseInvestmentTransaction txn : investmentTxns) {
                Map<String, Object> line = new HashMap<>();
                InvestmentTransaction invTxn = txn.getInvestmentTransaction();
                TransactionType txnType = txn.getTransactionType();
                line.put("account", accountId);
                line.put("labels", "");
                line.put("txnType", txnType.toString());
                line.put("category", txn.getTransactionType().name());
                line.put("merchant", invTxn.getMemo());
                String memo = invTxn.getMemo();
                if (invTxn.getSettlementDate() == null) {
                    if (invTxn.getTradeDate() == null) {
                        System.err.println("No settlement date specified.");
                    } else {
                        line.put("odate", invTxn.getTradeDate().getTime());
                    }
                } else
                    line.put("odate", invTxn.getSettlementDate().getTime());
                line.put("tradeDate", invTxn.getTradeDate());
                line.put(TxnHistory.txnId, invTxn.getTransactionId());
                line.put("isBuy", false);
                line.put("isCheck", false);
                line.put("isChild", false);
                line.put("isDebit", false);
                line.put("isDuplicate", false);
                line.put("isEdited", false);
                line.put("isFirstDate", false);
                line.put("isLinkedToRule", false);
                line.put("isMatched", false);
                line.put("isPending", false);
                line.put("isPercent", false);
                line.put("isSell", false);
                line.put("isSpending", false);
                line.put("isTransfer", false);

                if (txnType.name().equals("BUY_OTHER")) {
                    BuyOtherTransaction buyOtherTxn = (BuyOtherTransaction) txn;
                    line.put("commission", buyOtherTxn.getCommission());
                    line.put("fees", buyOtherTxn.getFees());
                    updateTicker(line, buyOtherTxn.getSecurityId().getUniqueId());
                    line.put("amount", buyOtherTxn.getTotal());
                    line.put("shares", buyOtherTxn.getUnits());
                    line.put("sharePrice", buyOtherTxn.getUnitPrice());
                    line.put("load", buyOtherTxn.getLoad());
                    line.put("markup", buyOtherTxn.getMarkup());
                    line.put("taxes", buyOtherTxn.getTaxes());
                } else if (txnType.name().equals("BUY_DEBT")) {
                    BuyDebtTransaction buyInvestTxn = (BuyDebtTransaction) txn;
                    line.put("commission", buyInvestTxn.getCommission());
                    line.put("fees", buyInvestTxn.getFees());
                    updateTicker(line, buyInvestTxn.getSecurityId().getUniqueId());
                    line.put("amount", buyInvestTxn.getTotal());
                    line.put("shares", buyInvestTxn.getUnits());
                    line.put("sharePrice", buyInvestTxn.getUnitPrice());
                    line.put("load", buyInvestTxn.getLoad());
                    line.put("markup", buyInvestTxn.getMarkup());
                    line.put("taxes", buyInvestTxn.getTaxes());
                    line.put("maturityDate", getMaturityDateFromMemo(buyInvestTxn.getMemo()));
                } else if (txnType.name().equals("BUY_MUTUAL_FUND")) {
                    BuyMutualFundTransaction buyMutualFundTxn = (BuyMutualFundTransaction) txn;
                    line.put("commission", buyMutualFundTxn.getCommission());
                    line.put("fees", buyMutualFundTxn.getFees());
                    updateTicker(line, buyMutualFundTxn.getSecurityId().getUniqueId());
                    line.put("amount", buyMutualFundTxn.getTotal());
                    line.put("shares", buyMutualFundTxn.getUnits());
                    line.put("sharePrice", buyMutualFundTxn.getUnitPrice());
                    line.put("load", buyMutualFundTxn.getLoad());
                    line.put("markup", buyMutualFundTxn.getMarkup());
                    line.put("taxes", buyMutualFundTxn.getTaxes());
                } else if (txnType.name().equals("REINVEST_INCOME")) {
                    ReinvestIncomeTransaction reinvestIncomeTxn = (ReinvestIncomeTransaction) txn;
                    line.put("commission", reinvestIncomeTxn.getCommission());
                    line.put("fees", reinvestIncomeTxn.getFees());
                    updateTicker(line, reinvestIncomeTxn.getSecurityId().getUniqueId());
                    line.put("amount", reinvestIncomeTxn.getTotal());
                    line.put("shares", reinvestIncomeTxn.getUnits());
                    line.put("sharePrice", reinvestIncomeTxn.getUnitPrice());
                    line.put("load", reinvestIncomeTxn.getLoad());
                    line.put("401(k) source", reinvestIncomeTxn.get401kSource());
                    line.put("taxes", reinvestIncomeTxn.getTaxes());
                } else if (txnType.name().equals("SELL_STOCK")) {
                    SellStockTransaction sellStockTxn = (SellStockTransaction) txn;
                    line.put("commission", sellStockTxn.getCommission());
                    line.put("fees", sellStockTxn.getFees());
                    updateTicker(line, sellStockTxn.getSecurityId().getUniqueId());
                    line.put("amount", sellStockTxn.getTotal());
                    line.put("shares", sellStockTxn.getUnits());
                    line.put("sharePrice", sellStockTxn.getUnitPrice());
                    line.put("load", sellStockTxn.getLoad());
                    line.put("markup", sellStockTxn.getSellType());
                    line.put("taxes", sellStockTxn.getTaxes());
                } else if (txnType.name().equals("SELL_MUTUAL_FUND")) {
                    SellMutualFundTransaction sellMutualFundTxn = (SellMutualFundTransaction) txn;
                    line.put("commission", sellMutualFundTxn.getCommission());
                    line.put("fees", sellMutualFundTxn.getFees());
                    updateTicker(line, sellMutualFundTxn.getSecurityId().getUniqueId());
                    line.put("amount", sellMutualFundTxn.getTotal());
                    line.put("shares", sellMutualFundTxn.getUnits());
                    line.put("sharePrice", sellMutualFundTxn.getUnitPrice());
                    line.put("load", sellMutualFundTxn.getLoad());
                    line.put("markup", sellMutualFundTxn.getSellType());
                    line.put("taxes", sellMutualFundTxn.getTaxes());
                } else if (txnType.name().equals("INCOME")) {
                    IncomeTransaction incomeTxn = (IncomeTransaction) txn;
                    line.put("401(k) source", incomeTxn.get401kSource());
                    line.put("category", incomeTxn.getIncomeType());
                    line.put("amount", incomeTxn.getTotal());
                    updateTicker(line, incomeTxn.getSecurityId().getUniqueId());
                } else if (txnType.name().equals("MARGIN_INTEREST")) {
                    MarginInterestTransaction marginInterestTxn = (MarginInterestTransaction) txn;
                    line.put("amount", marginInterestTxn.getTotal());
                } else if (txnType.name().equals("TRANSFER")) {
                    TransferInvestmentTransaction transferTxn = (TransferInvestmentTransaction) txn;
                    line.put("shares", transferTxn.getUnits());
                    line.put("sharePrice", transferTxn.getUnitPrice());
                    updateTicker(line, transferTxn.getSecurityId().getUniqueId());
                    line.put("401(k) source", transferTxn.get401kSource());
                    line.put("category", transferTxn.getTransferAction());
                    line.put("amount", 0.00);
                } else {
                    System.err.println("Can't process txnType: " + txnType);
                }
                line.put(TxnHistory.source, ofx);
                txnHistory.insertRow(line);
            }
        List<InvestmentBankTransaction> investmentBankTxns = investmentTransactionList.getBankTransactions();
        if (investmentBankTxns != null)
            for (InvestmentBankTransaction txn : investmentBankTxns) {
                Map<String, Object> line = new HashMap<>();
                String subAccountFund = txn.getSubAccountFund();
                Transaction bankTxn = txn.getTransaction();
                BigDecimal amount = bankTxn.getBigDecimalAmount();
                BankAccountDetails bankAcctDetails = bankTxn.getBankAccountTo();
                String checkNumber = bankTxn.getCheckNumber();
                Date dateInitiated = bankTxn.getDateInitiated();
                Date datePosted = bankTxn.getDatePosted();
                String memo = bankTxn.getMemo();
                Payee payee = bankTxn.getPayee();
                com.webcohesion.ofx4j.domain.data.common.TransactionType txnType = bankTxn.getTransactionType();
                String txnRefNum = bankTxn.getReferenceNumber();
                line.put("odate", datePosted.getTime());
                line.put(TxnHistory.txnId, bankTxn.getId());
                line.put("checkNumber", checkNumber);
                line.put("omerchant", bankTxn.getPayeeId());
                line.put("amount", bankTxn.getAmount());
                line.put("txnType", txnType.name());
                line.put("account", accountId);
                line.put("labels", "");
                line.put("merchant", bankTxn.getMemo());
                line.put("isBuy", false);
                line.put("isCheck", false);
                line.put("isChild", false);
                line.put("isDebit", false);
                line.put("isDuplicate", false);
                line.put("isEdited", false);
                line.put("isFirstDate", false);
                line.put("isLinkedToRule", false);
                line.put("isMatched", false);
                line.put("isPending", false);
                line.put("isPercent", false);
                line.put("isSell", false);
                line.put("isSpending", false);
                line.put("isTransfer", false);
                line.put(TxnHistory.source, ofx);
                line.put("category", "Investment");
                txnHistory.insertRow(line);
            }
    }

    private Long getMaturityDateFromMemo(String memo) {
        // Example: "US TREASURY       BILL18U S T BILL  DUE 10/11/18"
        Pattern p1 = Pattern.compile(".* DUE +(\\d+)/(\\d+)/(\\d+).*", Pattern.CASE_INSENSITIVE);
        Matcher m = p1.matcher(memo);
        if (m.matches()) {
            int month = Integer.parseInt(m.group(1));
            int day = Integer.parseInt(m.group(2));
            int year = Integer.parseInt(m.group(3)) + 2000;
            LocalDate date = LocalDate.of(year, month, day);
            return date.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC);
        }
        return 0L;
    }
}
