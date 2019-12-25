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
        String cmd = "/usr/local/bin/ofxget stmt schwab-alt";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            AggregateUnmarshaller<ResponseEnvelope> unmarshaller = new AggregateUnmarshaller<ResponseEnvelope>(ResponseEnvelope.class);
            ResponseEnvelope responseEnvelope = unmarshaller.unmarshal(p.getInputStream());

            SecurityList securityList = ((SecurityListResponseMessageSet) responseEnvelope.getMessageSet(MessageSetType.investment_security)).getSecurityList();
            Map<String, String> securityIdToTicker = new HashMap<>();
            for (BaseSecurityInfo securityInfo : securityList.getSecurityInfos()) {
                securityIdToTicker.put(securityInfo.getSecurityId().getUniqueId(), securityInfo.getTickerSymbol());
            }
            List<ResponseMessage> messages = responseEnvelope.getMessageSet(MessageSetType.investment).getResponseMessages();
            positionHistory.deleteAllRows();
            for (ResponseMessage message : messages) {
                if (message instanceof InvestmentStatementResponseTransaction) {
                    InvestmentStatementResponse response = ((InvestmentStatementResponseTransaction) message).getMessage();
                    Date date = response.getDateOfStatement();
                    InvestmentAccountDetails account = response.getAccount();
                    final String accountId = account.getAccountNumber();
                    if (response.getPositionList() != null) {
                        response.getPositionList().getPositions().
                                forEach(position -> positionHistory.insertRow(
                                        date,
                                        securityIdToTicker.get(position.getSecurityId().getUniqueId()),
                                        accountId,
                                        BigDecimal.valueOf(position.getUnits()),
                                        position.getPositionType(),
                                        BigDecimal.valueOf(position.getUnitPrice()),
                                        BigDecimal.valueOf(position.getMarketValue())));
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
