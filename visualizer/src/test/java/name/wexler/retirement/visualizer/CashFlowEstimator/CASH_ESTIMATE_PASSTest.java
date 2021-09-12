package name.wexler.retirement.visualizer.CashFlowEstimator;

import org.junit.Test;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CASH_ESTIMATE_PASSTest {
    private CASH_ESTIMATE_PASS pass = CASH_ESTIMATE_PASS.BASE_CASH_FLOWS;


    @Test
    public void iterator() {
        Iterator<CASH_ESTIMATE_PASS> iterator = pass.iterator();
        assertEquals(true, iterator.hasNext());
        assertEquals(CASH_ESTIMATE_PASS.BASE_CASH_FLOWS,
                iterator.next());
        assertEquals(true, iterator.hasNext());
        assertEquals(CASH_ESTIMATE_PASS.DERIVED_INCOME,
                iterator.next());
        assertEquals(true, iterator.hasNext());
        assertEquals(CASH_ESTIMATE_PASS.DERIVED_EXPENSES,
                iterator.next());
        assertEquals(true, iterator.hasNext());
        assertEquals(CASH_ESTIMATE_PASS.TAXES,
                iterator.next());
        assertEquals(false, iterator.hasNext());
    }

    @Test
    public void values() {
        CASH_ESTIMATE_PASS[] values = pass.values();
        assertEquals(4, values.length);
        assertEquals(CASH_ESTIMATE_PASS.BASE_CASH_FLOWS, values[0]);
        assertEquals(CASH_ESTIMATE_PASS.DERIVED_INCOME, values[1]);
        assertEquals(CASH_ESTIMATE_PASS.DERIVED_EXPENSES, values[2]);
        assertEquals(CASH_ESTIMATE_PASS.TAXES, values[3]);
    }

    @Test
    public void valueOf() {
        assertEquals(CASH_ESTIMATE_PASS.DERIVED_INCOME,pass.valueOf("DERIVED_INCOME"));
    }
}