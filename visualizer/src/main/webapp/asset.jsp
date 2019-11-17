<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ page isELIgnored="false" %>
<html>
<head>
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
</head>
    <body>
        <h1>Valuations &amp; Cash Flows for ${scenarioId}/${assetId}</h1>
        
        <table border="1">
            <caption>Valuations</caption>
            <tr>
                <th>Date</th>
                <th>Value</th>
            </tr>
            <c:forEach var="balance" items="${balances}">
                <tr>
                    <th>${balance.balanceDate}</th>
                    <td>
                        <fmt:formatNumber value="${balance.value}" type="currency" />
                    </td>
                </tr>
            </c:forEach>
        </table>

        <table border="1">


        <display:table uid="item" name="${command.cashFlows}" sort="external"
                       decorator="name.wexler.retirement.visualizer.Tables.MultilevelBigDecimalLinkTableDecorator">
            <display:caption>Cash Flows for ${command.scenarioId}/${command.assetId}/${command.year}</display:caption>
            <display:column property="cashFlowDate" />
            <display:column property="accrualStart" />
            <display:column property="accrualEnd" />
            <display:column property="category" />
            <display:column property="description" />
            <display:column property="labels" />
            <display:column property="notes" />
            <display:column property="amount" class="money" total="true"
                            decorator="name.wexler.retirement.visualizer.Tables.MoneyTableColumnDecorator"/>
            <display:column property="cashBalance" class="money" total="true"
                            decorator="name.wexler.retirement.visualizer.Tables.MoneyTableColumnDecorator"/>
            <display:column property="assetBalance" class="money" total="true"
                            decorator="name.wexler.retirement.visualizer.Tables.MoneyTableColumnDecorator"/>
        </display:table>
    </body>
</html>
