package name.wexler.retirement.visualizer.Tables;

import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.Budget;
import name.wexler.retirement.visualizer.CashFlowEstimator.Liability;
import name.wexler.retirement.visualizer.Assumptions;
import name.wexler.retirement.visualizer.CashFlowFrequency.ShareBalance;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowInstance.LiabilityCashFlowInstance;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.CashFlowSink;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Scenario;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by mwexler on 12/30/16.
 */
public class CashFlowCalendar {
    public enum ITEM_TYPE {INCOME, EXPENSE, TRANSFER}

    public interface CashFlowChecker {
        boolean check(CashFlowInstance source);
    }

    private List<Budget> budgets;
    private final Map<String, Asset> _assets;
    private final Map<String, Liability> _liabilities;
    private final List<CashFlowInstance> cashFlowInstances = new ArrayList<>();
    private final Assumptions _assumptions;
    private final Scenario _scenario;

    /**
     *
     * @param scenario
     * @param assumptions
     */
    public CashFlowCalendar(Scenario scenario, Assumptions assumptions) {
        _scenario = scenario;
        _assumptions = assumptions;
        _assets = new HashMap<>();
        _liabilities = new HashMap<>();
    }

    public Collection<Asset> getAssets() {
        return _assets.values();
    }

    public BigDecimal getAssetValue(String assetId, Integer year) {
        Asset asset = this._scenario.getContext().getById(Asset.class, assetId);
        Optional<CashFlowInstance> finalInstanceForYear =
                this.getCashFlowInstances().stream().
                        filter(instance -> instance.getCashFlowSink() == asset).
                        filter(instance -> instance.getCashFlowDate().isBefore(LocalDate.of(year + 1, Month.JANUARY, 1))).
                        reduce((first, second) -> second);
        BigDecimal finalBalance = BigDecimal.ZERO;
        if (finalInstanceForYear.isPresent())
            finalBalance = finalInstanceForYear.get().getCashBalance().add(finalInstanceForYear.get().getAssetBalance());
        return finalBalance;
    }

    public Map<String, Liability> getLiabilities() {
        return _liabilities;
    }

    public BigDecimal getLiabilityAmount(String id, Integer year) {
        return this.getCashFlowInstances().stream().
                filter(instance->instance.getCashFlowSource() instanceof Liability).
                filter(instance->instance.getCashFlowSourceId() == id).
                filter(instance->instance.getYear() == year).
                sorted((a,b) -> a.getCashFlowDate().compareTo(b.getCashFlowDate())).
                collect(Collectors.mapping(instance -> instance.getAmount(),
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)));
    }

    public void addCashFlowInstances(List<CashFlowInstance> cashFlowInstances) {
        this.cashFlowInstances.addAll(cashFlowInstances);
    }

    public void addBudgets(List<Budget> budgets) {
        this.budgets = budgets;
    }

    public List<Budget> getBudgets() {
        return budgets;
    }

    /**
     * We assume that before we have been called each of the accounts has had its current balance retrieved.
     * We then iterate backwards skipping estimates which, by definition, are in the future negating each transaction
     * from both cash balances and positions. When we get to the end we have starting balances.
     * Then we need to iterate forward over the estimated transactions to get the ending balances.
     */
    public void computeBalances() {
        // First we sort all the cash flow instances into date order.
        cashFlowInstances.sort(Comparator.comparing(CashFlowInstance::getCashFlowDate));

        // Then we iterate backward
        ListIterator<CashFlowInstance> listIterator = cashFlowInstances.listIterator(cashFlowInstances.size());

        Set<CashFlowSink> cashFlowSinks = new HashSet<>();
        while (listIterator.hasPrevious()) {
            CashFlowInstance instance = listIterator.previous();
            if (instance.isEstimate() && instance.getCashFlowDate().isAfter(LocalDate.now()))
                continue;   // We are counting back from actual balance, skip estimates
            CashFlowSink sink = instance.getCashFlowSink();
            cashFlowSinks.add(sink);
            sink.updateRunningTotal(instance, true);
        }
        cashFlowSinks.forEach(sink -> sink.setStartingBalance());

        listIterator = cashFlowInstances.listIterator();
        while (listIterator.hasNext()) {
            CashFlowInstance instance = listIterator.next();
            if (!instance.isEstimate() || instance.getCashFlowDate().isBefore(LocalDate.now()))
                continue;   // We are counting forward from actual balance, already applied non-estimates
            CashFlowSink sink = instance.getCashFlowSink();
            sink.updateRunningTotal(instance, false);
        }
    }

    public void addAssets(List<Asset> assets) {
        assets.forEach(item-> _assets.put(item.getId(), item));
    }

    public void addLiabilities(List<Liability> liabilities) {
        liabilities.forEach(item-> _liabilities.put(item.getId(), item));
    }

    public BigDecimal sumMatchingCashFlowForPeriod(LocalDate accrualStart, LocalDate accrualEnd, CashFlowChecker checker) {
        BigDecimal sum = BigDecimal.ZERO;
        for (CashFlowInstance cashFlowInstance : this.cashFlowInstances) {
            cashFlowInstance.getCashFlowSource();
            if (cashFlowInstance.isPaidInDateRange(accrualStart, accrualEnd)) {
                if (checker.check(cashFlowInstance)) {
                    sum = sum.add(cashFlowInstance.getAmount());
                }
            }
        }
        return sum;
    }

    public Assumptions getAssumptions() {
        return _assumptions;
    }

    public List<CashFlowInstance> getCashFlowsBySink(String cashFlowId) {
        List<CashFlowInstance> cashFlows =
                cashFlowInstances.stream().
                        filter(instance -> instance.getCashFlowSinkId().equals(cashFlowId)).
                        collect(Collectors.toList());
        return cashFlows;
    }

    public List<CashFlowInstance> getCashFlowsBySink(String cashFlowId, Integer year) {
        List<CashFlowInstance> cashFlows =
                cashFlowInstances.stream().
                        filter(instance -> instance.getCashFlowSinkId().equals(cashFlowId)).
                        filter(instance -> instance.getYear() == year).
                        collect(Collectors.toList());
        return cashFlows;
    }

    public List<LiabilityCashFlowInstance> getLiabilityCashFlowInstances(String liabilityId) {
        return (List<LiabilityCashFlowInstance>) (List<?>) getCashFlowsBySink(liabilityId);
    }

    public List<LiabilityCashFlowInstance> getLiabilityCashFlowInstances(String liabilityId, int year) {
        return (List<LiabilityCashFlowInstance>) (List<?>) getCashFlowsBySink(liabilityId, year);
    }

    public List<CashFlowInstance> getCashFlowInstances() {
        return cashFlowInstances;
    }
}
