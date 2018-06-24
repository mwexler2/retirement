<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page isELIgnored="false" %>
<html>
<body>
<h1>Cash Flows for ${scenarioId}/${cashFlowId}</h1>



<table border="1">
    <caption>Cash Flows</caption>
    <tr>
        <th>Date</th>
        <th>Accrual Start</th>
        <th>Accrual End</th>
        <th>Amount</th>
    </tr>
    <c:forEach var="cashFlow" items="${cashFlows}">
        <tr>
            <th>${cashFlow.cashFlowDate}</th>
            <td>${cashFlow.accrualStart}</td>
            <td>${cashFlow.accrualEnd}</td>
            <td>
                <fmt:formatNumber value="${cashFlow.amount}" type="currency" />
            </td>
        </tr>
    </c:forEach>
    </table>
</body>
</html>
