package name.wexler.retirement.visualizer.CashFlowEstimator;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public enum CASH_ESTIMATE_PASS implements
        Comparable<CASH_ESTIMATE_PASS>,
        Iterable<CASH_ESTIMATE_PASS> {
    BASE_CASH_FLOWS(1),
    DERIVED_INCOME(2),
    DERIVED_EXPENSES(3),
    TAXES(4);

    private int ordinal;

    CASH_ESTIMATE_PASS(int ordinal) {
        this.ordinal = ordinal;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public Iterator<CASH_ESTIMATE_PASS> iterator() {
        return new CashEstimateIterator(this);
    }

    private class CashEstimateIterator implements Iterator<CASH_ESTIMATE_PASS> {
        private CASH_ESTIMATE_PASS current;

        public CashEstimateIterator(CASH_ESTIMATE_PASS currentPass) {
            this.current = currentPass;
        }

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return current != null;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public CASH_ESTIMATE_PASS next() {
            final CASH_ESTIMATE_PASS cur = this.current;
            if (current == BASE_CASH_FLOWS)
                this.current = DERIVED_INCOME;
            else if (current == DERIVED_INCOME)
                this.current = DERIVED_EXPENSES;
            else if (current == DERIVED_EXPENSES)
                this.current = TAXES;
            else
                this.current = null;
            return cur;
        }
    }
}
