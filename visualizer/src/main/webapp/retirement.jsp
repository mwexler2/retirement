<%@ page import="name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowCalendar" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="f"   uri="http://retirement.wexler.name.visualizer/functions" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>


<%@ page isELIgnored="false" %>
<html>
<head>
    <title>Retirement Calculator</title>
    <style>
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
    </style>
</head>
<body>
<h1>Retirement Calculator</h1>


<display:table name="${command.people}" >
    <display:caption>People</display:caption>
    <display:column property="firstName" title="First Name" />
    <display:column property="lastName" title="Last Name"/>
    <display:column property="birthDate" title="Birth Date" />
    <display:column property="retirementAge" title="Retirement Age"/>
</display:table>


<c:forEach var="scenario" items="${command.scenarios}">
    <h2>${scenario.name}</h2>


<table border="1">
    <caption>Assumptions</caption>
    <tr>
        <th>Long Term Investment Return</th>
        <td>
            <fmt:formatNumber value="${scenario.assumptions.longTermInvestmentReturn}" type="percent"
                              minFractionDigits="2" maxFractionDigits="2" />
        </td>
    </tr><tr>
        <th>Short Term Investment Return</th>
        <td>
            <fmt:formatNumber value="${scenario.assumptions.shortTermInvestmentReturn}" type="percent"
                              minFractionDigits="2" maxFractionDigits="2" />
        </td>
    </tr><tr>
        <th>Inflation</th>
        <td>
            <fmt:formatNumber value="${scenario.assumptions.inflation}" type="percent"
                              minFractionDigits="2" maxFractionDigits="2" />
        </td>
    </tr><tr>
        <th>Years in short term</th><td>${scenario.assumptions.yearsInShortTerm}
    </tr>
</table>


    <c:set var="assetsAndLiabilities" scope="page" value="${scenario.assetsAndLiabilities}"></c:set>
    <display:table uid="item" name="${assetsAndLiabilities}" decorator="name.wexler.retirement.visualizer.CashFlowFrequency.MultilevelBigDecimalLinkTableDecorator"
        sort="external">
        <display:caption>Assets & Liabilities</display:caption>
        <display:column property="itemType" group="1" />
        <display:column property="itemClass" group="2" />
        <c:forEach var="col" items="${assetsAndLiabilities.getColumnDefinitions()}">
            <display:column title="${col.name}" href="${col.href}" paramProperty="${col.paramProperty}"
                            property="${col.property}" decorator="${col.decorator}" style="text-align: right"
                            total="${col.total}" />
        </c:forEach>

    </display:table>

    <c:set var="cashFlows" scope="page" value="${scenario.cashFlows}"></c:set>
    <display:table uid="cashFlow" name="${cashFlows}" decorator="name.wexler.retirement.visualizer.CashFlowFrequency.MultilevelBigDecimalLinkTableDecorator"
                   sort="external">
        <display:caption>Income and Expenses --- displaytag</display:caption>
        <display:column property="itemType" group="1" />
        <display:column property="itemClass" group="2" />
        <c:forEach var="col" items="${cashFlows.getColumnDefinitions()}">
            <display:column title="${col.name}" href="${col.href}" paramProperty="${col.paramProperty}"
                            property="${col.property}" decorator="${col.decorator}" style="text-align: right"
                            total="${col.total}" />
        </c:forEach>

    </display:table>
</c:forEach>
</body>
</html>
