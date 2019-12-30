package name.wexler.retirement.financeCrawler;


import com.webcohesion.ofx4j.client.impl.LocalResourceFIDataStore;
import com.webcohesion.ofx4j.client.impl.FinancialInstitutionImpl;
import com.webcohesion.ofx4j.domain.data.MessageSetType;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.ResponseMessage;
import com.webcohesion.ofx4j.domain.data.ResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.banking.AccountType;
import com.webcohesion.ofx4j.domain.data.banking.BankAccountDetails;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardAccountDetails;
import com.webcohesion.ofx4j.client.net.OFXV1Connection;
import com.webcohesion.ofx4j.domain.data.investment.accounts.InvestmentAccountDetails;
import com.webcohesion.ofx4j.domain.data.investment.statements.InvestmentStatementResponse;
import com.webcohesion.ofx4j.domain.data.investment.statements.InvestmentStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.seclist.*;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import com.webcohesion.ofx4j.io.OFXParseException;
import name.wexler.retirement.datastore.AccountTable;
import name.wexler.retirement.datastore.PositionHistory;
import name.wexler.retirement.datastore.TxnHistory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.*;

public class OFXCrawler {
    private PositionHistory positionHistory;
    private AccountTable accountTable;

    OFXCrawler(PositionHistory positionHistory, AccountTable accountTable) {
        super();
        this.positionHistory = positionHistory;
        this.accountTable = accountTable;
    }

    public void crawl() {
        final String[] serverList = {"schwab-alt", "vanguard", "netbenefits"};
        positionHistory.deleteAllRows();
        for (String server: serverList) {
            String cmd = "/usr/local/bin/ofxget stmt " + server;
            try {
                Process p = Runtime.getRuntime().exec(cmd);
                AggregateUnmarshaller<ResponseEnvelope> unmarshaller = new AggregateUnmarshaller<ResponseEnvelope>(ResponseEnvelope.class);
                ResponseEnvelope responseEnvelope = unmarshaller.unmarshal(p.getInputStream());

                SecurityList securityList = ((SecurityListResponseMessageSet) responseEnvelope.getMessageSet(MessageSetType.investment_security)).getSecurityList();
                Map<String, String> securityIdToTicker = new HashMap<>();
                for (BaseSecurityInfo securityInfo : securityList.getSecurityInfos()) {
                    String ticker = securityInfo.getTickerSymbol();
                    if (ticker == null)
                        ticker = securityInfo.getSecurityId().getUniqueId();
                    securityIdToTicker.put(securityInfo.getSecurityId().getUniqueId(), ticker);
                }
                List<ResponseMessage> messages = responseEnvelope.getMessageSet(MessageSetType.investment).getResponseMessages();

                for (ResponseMessage message : messages) {
                    if (message instanceof InvestmentStatementResponseTransaction) {
                        InvestmentStatementResponse response = ((InvestmentStatementResponseTransaction) message).getMessage();
                        Date date = response.getDateOfStatement();
                        InvestmentAccountDetails account = response.getAccount();
                        final String accountId = account.getAccountNumber();
                        if (response.getPositionList() != null) {
                            response.getPositionList().getPositions().
                                    forEach(position -> {
                                        String uniqueId = position.getSecurityId().getUniqueId();
                                        positionHistory.insertRow(
                                                        date.getTime(),
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
                }
            } catch (OFXParseException ope) {
                System.err.println(ope);
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        }
    }
}
