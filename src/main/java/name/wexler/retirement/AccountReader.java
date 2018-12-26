package name.wexler.retirement;

import name.wexler.retirement.Asset.Account;
import name.wexler.retirement.CashFlowInstance.CashFlowInstance;

import java.util.List;

public abstract class AccountReader {
    public static AccountReader factory(Account account) throws ClassNotFoundException {
        if (account.getCompany().getId().equals("schwab")) {
            return new SchwabAccountReader();
        }
        throw new ClassNotFoundException("Can't find reader for " + account.getCompany().getId());
    }

    public abstract List<CashFlowInstance> readCashFlowInstances(Account account);
}
