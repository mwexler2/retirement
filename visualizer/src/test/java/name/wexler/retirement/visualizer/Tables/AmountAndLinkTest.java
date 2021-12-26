package name.wexler.retirement.visualizer.Tables;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class AmountAndLinkTest {
    private static BigDecimal sampleAmount = BigDecimal.TEN;
    private static String sampleLink = "/foo/bar";
    private AmountAndLink amountAndLink;

    @Before
    public void setUp() throws Exception {
        amountAndLink = new AmountAndLink(sampleAmount, sampleLink);
    }

    @Test
    public void getAmount() {
        assertEquals(sampleAmount, amountAndLink.getAmount());
    }

    @Test
    public void getLink() {
        assertEquals(sampleLink, amountAndLink.getLink());
    }
}