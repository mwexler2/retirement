<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page isELIgnored="false" %>
<html>
<body>
<h1>Cash Flows for ${command.scenarioId}/${command.cashFlowId}</h1>



<table border="1">
    <caption>Cash Flows</caption>
    <tr>
        <th>Cash Flow Id</th>
        <th>accrualStart</th>
        <th>accrualEnd</th>
        <th>cashFlowDate</th>
        <th>amount</th>
    </tr>
    <c:forEach var="cashFlow" items="${command.cashFlows}">
        <tr>
            <td>${cashFlow.cashFlowId}</td>
            <td>${cashFlow.accrualStart}</td>
            <td>${cashFlow.accrualEnd}</td>
            <td>${cashFlow.cashFlowDate}</td>
            <td>${cashFlow.amount}</td>
        </tr>
    </c:forEach>
    </table>
</body>
</html>
