package name.wexler.retirement.visualizer.CashFlowEstimator;

import com.fasterxml.jackson.annotation.*;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "id", "taxTable" })
public class TaxTable {
    static public class TaxYearTable {
        static public class TaxBracket implements Comparable {
            BigDecimal startOfBracket;
            BigDecimal marginalRate;

            @JsonCreator
            public TaxBracket(
                    @JsonProperty(value = "startOfBracket", required = true) BigDecimal startOfBracket,
                    @JsonProperty(value = "marginalRate", required = true) BigDecimal marginalRate
            ) {
                this.startOfBracket = startOfBracket;
                this.marginalRate = marginalRate;
            }

            /**
             * Compares this object with the specified object for order.  Returns a
             * negative integer, zero, or a positive integer as this object is less
             * than, equal to, or greater than the specified object.
             *
             * <p>The implementor must ensure
             * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
             * for all {@code x} and {@code y}.  (This
             * implies that {@code x.compareTo(y)} must throw an exception iff
             * {@code y.compareTo(x)} throws an exception.)
             *
             * <p>The implementor must also ensure that the relation is transitive:
             * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
             * {@code x.compareTo(z) > 0}.
             *
             * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
             * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
             * all {@code z}.
             *
             * <p>It is strongly recommended, but <i>not</i> strictly required that
             * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
             * class that implements the {@code Comparable} interface and violates
             * this condition should clearly indicate this fact.  The recommended
             * language is "Note: this class has a natural ordering that is
             * inconsistent with equals."
             *
             * <p>In the foregoing description, the notation
             * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
             * <i>signum</i> function, which is defined to return one of {@code -1},
             * {@code 0}, or {@code 1} according to whether the value of
             * <i>expression</i> is negative, zero, or positive, respectively.
             *
             * @param o the object to be compared.
             * @return a negative integer, zero, or a positive integer as this object
             * is less than, equal to, or greater than the specified object.
             * @throws NullPointerException if the specified object is null
             * @throws ClassCastException   if the specified object's type prevents it
             *                              from being compared to this object.
             */
            @Override
            public int compareTo(@NotNull Object o) {
                if (o instanceof TaxBracket) {
                    TaxBracket otherBracket = (TaxBracket) o;
                    return -this.startOfBracket.compareTo(otherBracket.startOfBracket);
                }
                return -1;
            }
        }

        private List<TaxBracket> taxBracketList;

        @JsonCreator
        public TaxYearTable(
                        @JsonProperty(value = "taxBracketList", required = true) List<TaxBracket> taxBracketList
        ) {
            this.taxBracketList = taxBracketList;
            taxBracketList.sort(null);
        }

        public BigDecimal computeTax(BigDecimal income) {
            BigDecimal tax = BigDecimal.ZERO;
            BigDecimal remainingIncome = income;

            for (Iterator<TaxBracket> bracketIterator = taxBracketList.iterator(); bracketIterator.hasNext(); ) {
                TaxBracket bracket = bracketIterator.next();

                BigDecimal marginalIncome = remainingIncome.subtract(bracket.startOfBracket);
                if (marginalIncome.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal marginalTax = marginalIncome.multiply(bracket.marginalRate);
                    tax = tax.add(marginalTax);
                    remainingIncome = remainingIncome.subtract(marginalIncome);
                }
            }
            return tax;
        }
    }
    Map<Integer, TaxYearTable> taxRateMap;  // Map year to a TaxYearTable

    @JsonCreator
    public TaxTable(
                     @JsonProperty(value = "taxRateMap", required = true) Map<Integer, TaxYearTable> taxRateMap
    ) throws Entity.DuplicateEntityException {
        this.taxRateMap = taxRateMap;
    }

    public BigDecimal computeTax(int year, BigDecimal income) {
        return taxRateMap.get(year).computeTax(income);
    }
}
