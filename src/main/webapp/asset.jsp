<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page isELIgnored="false" %>
<html>
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
            <caption>Cash Flows</caption>
            <tr>
                <th>Cash Flow Date</th>
                <th>Action</th>
                <th>Accrual Start</th>
                <th>Accrual End</th>
                <th>Amount</th>
                <th>Cash Balance</th>
                <th>Asset Balance</th>
                <th>Description</th>
                <th>Notes</th>
                <th>Labels </th>
                <th>Category</th>
            </tr>
            <c:forEach var="cashFlowInstance" items="${cashFlowInstances}">

                <tr>
                    <td>${cashFlowInstance.cashFlowDate}</td>
                    <td>${cashFlowInstance.action}</td>
                    <td>${cashFlowInstance.accrualStart}</td>
                    <td>${cashFlowInstance.accrualEnd}</td>
                    <td alignt="right">
                        <fmt:formatNumber value="${cashFlowInstance.amount}" type="currency" />
                    </td>
                    <td align="right">
                        <fmt:formatNumber value="${cashFlowInstance.cashBalance}" type="currency" />
                    </td>
                    <td align="right">
                        <fmt:formatNumber value="${cashFlowInstance.assetBalance}" type="currency" />
                    </td>
                    <td>${cashFlowInstance.description}</td>
                    <td>${cashFlowInstance.notes}</td>
                    <td>${cashFlowInstance.labels}</td>
                    <td>${cashFlowInstance.category}</td>
                </tr>
            </c:forEach>
        </table>
    </body>
</html>
