<%@ page import="name.wexler.retirement.visualizer.Tables.CashFlowCalendar" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="f"   uri="http://retirement.wexler.name.visualizer/functions" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>


<%@ page isELIgnored="false" %>
<html>
<head>
    <title>Retirement Calculator</title>
    <link href="<%=request.getContextPath()%>/css/retirement.css" rel="stylesheet" />
    <script type="application/javascript" src="<%=request.getContextPath()%>/js/retirement.js" ></script>
</head>


<body>

<h1>Retirement Calculator</h1>

<display:table name="${people}" >
    <display:caption>People</display:caption>
    <display:column property="firstName" title="First Name" />
    <display:column property="lastName" title="Last Name"/>
    <display:column property="birthDate" title="Birth Date" />
    <display:column property="retirementAge" title="Retirement Age"/>
</display:table>


<h2>${scenario.name}</h2>


<table border="1">
    <caption>Assumptions</caption>
    <tr>
        <th>Long Term Investment Return</th>
        <td>
            <fmt:formatNumber value="${assumptions.longTermInvestmentReturn}" type="percent"
                              minFractionDigits="2" maxFractionDigits="2" />
        </td>
    </tr><tr>
        <th>Short Term Investment Return</th>
        <td>
            <fmt:formatNumber value="${assumptions.shortTermInvestmentReturn}" type="percent"
                              minFractionDigits="2" maxFractionDigits="2" />
        </td>
    </tr><tr>
        <th>Inflation</th>
        <td>
            <fmt:formatNumber value="${assumptions.inflation}" type="percent"
                              minFractionDigits="2" maxFractionDigits="2" />
        </td>
    </tr><tr>
        <th>Years in short term</th><td>${assumptions.yearsInShortTerm}
    </tr>
</table>


    <c:set var="assetsAndLiabilities" scope="page" value="${assetsAndLiabilities}"></c:set>
    <div class="table-scroll">
        <display:table uid="item" name="${assetsAndLiabilities}" decorator="name.wexler.retirement.visualizer.Tables.MultilevelBigDecimalLinkTableDecorator"
            sort="external">
            <display:caption>Assets & Liabilities</display:caption>
            <display:column property="itemType" group="1" class="fixed-column" headerClass="fixed-column" />
            <display:column property="itemCategory" group="2" class="fixed-column" headerClass="fixed-column" />
            <c:forEach var="col" items="${assetsAndLiabilities.getColumnDefinitions()}">
                <display:column title="${col.name}" href="${col.href}" paramProperty="${col.paramProperty}"
                                property="${col.property}" decorator="${col.decorator}" class="${col.className}"
                                headerClass="${col.headerClassName}"
                                total="${col.total}" />
            </c:forEach>

        </display:table>
    </div>

    <c:set var="cashFlows" scope="page" value="${cashFlows}"></c:set>
    <div class="table-scroll">
        <display:table uid="cashFlow" name="${cashFlows}" decorator="name.wexler.retirement.visualizer.Tables.MultilevelBigDecimalLinkTableDecorator"
                       sort="external">
            <display:caption>Income and Expenses --- displaytag</display:caption>
            <display:column property="itemType" title="Type" group="1" class="fixed-column" headerClass="fixed-column" />
            <display:column property="parentCategory" title="Parent" group="2" class="fixed-column"  headerClass="fixed-column" />
            <c:forEach var="col" items="${cashFlows.getColumnDefinitions()}">
                <display:column title="${col.name}" href="${col.href}" paramProperty="${col.paramProperty}"
                                property="${col.property}" decorator="${col.decorator}" style="text-align: right"
                                class="${col.className}" headerClass="${col.headerClassName}" total="${col.total}" />
            </c:forEach>

        </display:table>
    </div>
</body>
</html>
