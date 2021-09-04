<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page isELIgnored="false" %>
<html>
<head>
    <title>${year} Cash Flows for ${cashFlowId}</title>
    <link href="<%=request.getContextPath()%>/css/retirement.css" rel="stylesheet" />
    <script type="application/javascript" src="<%=request.getContextPath()%>/js/retirement.js" ></script>
</head>
<body>
<h1>${scenarioId}</h1>

<table border="1">
    <caption>${year} Cash Flows for ${cashFlowId}</caption>
    <tr>
        <th>Date</th>
        <th>Accrual Start</th>
        <th>Accrual End</th>
        <th>Amount</th>
        <th>YTD</th>
    </tr>
<c:set var="ytd" value="${0}" />

    <c:forEach var="cashFlow" items="${cashFlows}">
        <c:set var="ytd" value="${ytd + cashFlow.amount}" />
        <tr>
            <th>${cashFlow.cashFlowDate}</th>
            <td>${cashFlow.accrualStart}</td>
            <td>${cashFlow.accrualEnd}</td>
            <td>
                <fmt:formatNumber value="${cashFlow.amount}" type="currency" />
            </td>
            <td>
                <fmt:formatNumber value="${ytd}" type="currency" />
            </td>
        </tr>
    </c:forEach>
    </table>
</body>
</html>
