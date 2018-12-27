package name.wexler.retirement;

import name.wexler.retirement.Asset.Account;
import name.wexler.retirement.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.Entity.Company;

import java.util.List;

public abstract class AccountReader {
    public static AccountReader factory(Company company) throws ClassNotFoundException {
        if (company.getId().equals("schwab")) {
            return new SchwabAccountReader();
        }
        throw new ClassNotFoundException("Can't find reader for " + company.getId());
    }

    public abstract void readCashFlowInstances(Context context, Company company);
}
