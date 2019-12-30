<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ page isELIgnored="false" %>
<html>
<head>
    <script src="/js/retirement.js" type="application/javascript" ></script>
    <link rel="stylesheet" href="/css/retirement.css" />
</head>
    <body>
        <display:table uid="item" name="${command.cashFlows}" sort="external"
                       decorator="name.wexler.retirement.visualizer.Tables.MultilevelBigDecimalLinkTableDecorator">
            <display:caption>Cash Flows for ${command.scenarioId}/${command.assetId}/${command.year}</display:caption>
            <display:column property="cashFlowDate" />
            <display:column property="category" />
            <display:column property="description" />
            <display:column property="symbol" />
            <display:column property="units" />
            <display:column property="unitPrice" />
            <display:column property="amount" class="money" total="true"
                            decorator="name.wexler.retirement.visualizer.Tables.MoneyTableColumnDecorator"/>
            <display:column property="cashBalance" class="money"
                            decorator="name.wexler.retirement.visualizer.Tables.MoneyTableColumnDecorator"/>
            <display:column property="assetBalance" class="money"
                            decorator="name.wexler.retirement.visualizer.Tables.MoneyTableColumnDecorator"/>
        </display:table>
    </body>
</html>
