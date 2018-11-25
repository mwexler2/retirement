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
</body>
</html>
