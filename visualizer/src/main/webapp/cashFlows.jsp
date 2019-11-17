<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ page isELIgnored="false" %>

<style type="text/css">
    td a {
        text-decoration-line: none;
    }
    th a {
        text-decoration-line: none;
    }
    table, thead, tbody, tr, th, td {
        border: solid thin;
        border-collapse: collapse;
    }
    tr.even {
        background-color: lightgray;
    }
    tr.subtotal-header {
        visibility: collapse;
    }
    td.money {
        text-align: right;
    }
</style>
<html>
<body>
 <display:table uid="item" name="${command.cashFlows}" sort="external"
                decorator="name.wexler.retirement.visualizer.Tables.MultilevelBigDecimalLinkTableDecorator">
        <display:caption>Cash Flows for ${command.scenarioId}/${command.category}</display:caption>
        <display:column property="cashFlowDate" />
        <display:column property="accrualStart" />
        <display:column property="accrualEnd" />
        <display:column property="category" />
        <display:column property="description" />
        <display:column property="labels" />
        <display:column property="notes" />
        <display:column property="amount" class="money" total="true"
                        decorator="name.wexler.retirement.visualizer.Tables.MoneyTableColumnDecorator"/>
    </display:table>
</body>
</html>
