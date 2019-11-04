package name.wexler.retirement.visualizer.CashFlowFrequency;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

import javax.servlet.jsp.PageContext;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class MoneyTableColumnDecorator implements DisplaytagColumnDecorator {
    private DecimalFormat moneyFormat = new DecimalFormat("$#,###,##0.00");

    public MoneyTableColumnDecorator() {

    }

    /**
     * transform the given object into a String representation. The object is supposed to be a BigDecimal.
     * @see org.displaytag.decorator.DisplaytagColumnDecorator#decorate(Object, PageContext, MediaTypeEnum)
     */
    @Override
    public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException
    {
        if (columnValue == null) {
            return "";
        }
        if (columnValue instanceof BigDecimal) {
            BigDecimal money = (BigDecimal) columnValue;
            return this.moneyFormat.format(money);
        } else if (columnValue instanceof CashFlowCalendar.AmountAndLink) {
            CashFlowCalendar.AmountAndLink amountAndLink = (CashFlowCalendar.AmountAndLink) columnValue;
            return "<a href='" + amountAndLink.getLink() + "'>" +
                    this.moneyFormat.format(amountAndLink.getAmount()) + "</a>";
        }
        return "";
    }
}

