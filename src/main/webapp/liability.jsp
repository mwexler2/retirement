<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page isELIgnored="false" %>
<html>
<body>
<h1>Valuations for ${scenarioId}/${assetId}</h1>



<table border="1">
    <caption>Valuations</caption>
    <tr>
        <th>Date</th>
        <th>Principal</th>
        <th>Interest</th>
        <th>Impounds</th>
        <th>Total</th>
        <th>Balance</th>
    </tr>
    <tr>
        <td>
            ${balances.get(0).balanceDate}
        </td><td align="right">
            <fmt:formatNumber value="0" type="currency" />
        </td><td align=""right">
            <fmt:formatNumber value="0" type="currency" />
        </td><td align="right">
            <fmt:formatNumber value="0" type="currency" />
        </td><td align="right">
            <fmt:formatNumber value="0" type="currency" />
        </td><td>
            <fmt:formatNumber value="${balances.get(0).value}" type="currency"/>
        </td>
    </tr>
    <c:forEach var="cashFlowInstance" items="${cashFlowInstances}">
        <tr>
            <th>${cashFlowInstance.cashFlowDate}</th>
            <td>
                <fmt:formatNumber value="${cashFlowInstance.principal}" type="currency" />
            </td>
            <td>
                <fmt:formatNumber value="${cashFlowInstance.interest}" type="currency" />
            </td>
            <td>
                <fmt:formatNumber value="${cashFlowInstance.impounds}" type="currency" />
            </td>
            <td>
                <fmt:formatNumber value="${cashFlowInstance.amount}" type="currency" />
            </td>
            <td>
                <fmt:formatNumber value="${cashFlowInstance.balance}" type="currency" />
            </td>
        </tr>
    </c:forEach>
    </table>
</body>
</html>
