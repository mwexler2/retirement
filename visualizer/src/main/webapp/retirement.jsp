<%@ page import="name.wexler.retirement.visualizer.Tables.CashFlowCalendar" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="f"   uri="http://retirement.wexler.name.visualizer/functions" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>


<%@ page isELIgnored="false" %>
<html>
<head>
    <title>Retirement Calculator</title>
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
        tr.details {
            visibility: collapse;
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

    <script type="application/javascript">
        function displayDetails(event) {
            children = document.getElementsByClassName(event.currentTarget.id);
            for (let child of children) {
                if (child.style.visibility == "visible")
                    child.style.visibility = "collapse";
                else
                    child.style.visibility = "visible";
            }
        }
        window.addEventListener("DOMContentLoaded", function() {
            var subTotals = document.getElementsByClassName("subtotal");
            for (let subTotal of subTotals) {
                subTotal.onclick = displayDetails;
            }
        }, false);
    </script>
</head>
<!--

Then your normal row would look something like:

<tr id='row1'><td> .... </td><td><a href="#" onClick="displayDetails('row1')">Details</a><
-->

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


    <c:set var="assetsAndLiabilities" scope="page" value="${scenario.cashFlowCalendar.assetsAndLiabilities}"></c:set>
    <display:table uid="item" name="${assetsAndLiabilities}" decorator="name.wexler.retirement.visualizer.Tables.MultilevelBigDecimalLinkTableDecorator"
        sort="external">
        <display:caption>Assets & Liabilities</display:caption>
        <display:column property="itemType" group="1" />
        <display:column property="itemCategory" group="2" />
        <c:forEach var="col" items="${assetsAndLiabilities.getColumnDefinitions()}">
            <display:column title="${col.name}" href="${col.href}" paramProperty="${col.paramProperty}"
                            property="${col.property}" decorator="${col.decorator}" class="money"
                            total="${col.total}" />
        </c:forEach>

    </display:table>

    <c:set var="cashFlows" scope="page" value="${scenario.cashFlowCalendar.cashFlows}"></c:set>
    <display:table uid="cashFlow" name="${cashFlows}" decorator="name.wexler.retirement.visualizer.Tables.MultilevelBigDecimalLinkTableDecorator"
                   sort="external">
        <display:caption>Income and Expenses --- displaytag</display:caption>
        <display:column property="itemType" group="1" />
        <display:column property="itemCategory" group="2" />
        <c:forEach var="col" items="${cashFlows.getColumnDefinitions()}">
            <display:column title="${col.name}" href="${col.href}" paramProperty="${col.paramProperty}"
                            property="${col.property}" decorator="${col.decorator}" style="text-align: right"
                            total="${col.total}" />
        </c:forEach>

    </display:table>
</c:forEach>
</body>
</html>
