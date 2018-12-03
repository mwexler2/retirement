<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="f"   uri="http://retirement.wexler.name/functions" %>

<p>Date is: </p>
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
    </style>
</head>
<body>
<h1>Retirement Calculator</h1>


<table border="1" >
    <caption>People</caption>
    <tr>
        <th>First Name</th>
        <th>Last Name</th>
        <th>Birth Date</th>
        <th>Retirement Age</th>
    </tr>
    <c:forEach var="person" items="${command.people}">
        <tr>
            <td><c:out value="${person.firstName}"/></td>
            <td><c:out value="${person.lastName}"/></td>
            <td>${f:formatLocalDate(person.birthDate, 'dd.MM.yyyy')}</td>
            <td>${person.retirementAge}</td>
        </tr>
    </c:forEach>
</table>

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


    <table border="1">
        <caption>Assets and Liabilities</caption>
        <tr>
            <th>&nbsp;</th>
    <c:forEach var="year" items="${scenario.years}">
        <th>${year}</th>
    </c:forEach>
        </tr>
        <tr>
        <th>Assets</th>
            <td colspan="${scenario.numYears}">&nbsp;</td>
        </tr>
        <c:forEach var="assetId" items="${scenario.assetIds}">
            <tr>
                <th><a href="scenario/${scenario.getId()}/asset/${assetId}">
                        ${scenario.getAssetName(assetId)}
                </a></th>
                <c:forEach var="year" items="${scenario.years}">
                    <td align="right">
                        <a href="scenario/${scenario.getId()}/asset/${assetId}/year/${year}">
                          <fmt:formatNumber value="${scenario.getAssetValue(assetId, year)}" type="currency" />
                        </a>
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>
            <tr>
                <th>Total Assets</th>
                <c:forEach var="year" items="${scenario.years}">
                    <td align="right">
                        <fmt:formatNumber value="${scenario.getAssetValue(year)}" type="currency" />
                    </td>
                </c:forEach>
            </tr>
        <tr>
            <th>Liabilities</th>
            <td colspan="${scenario.numYears}">&nbsp;</td>
        </tr>
        <c:forEach var="liabilityId" items="${scenario.liabilityIds}">
            <tr>
                <th><a href="scenario/${scenario.getId()}/liability/${liabilityId}">
                        ${scenario.getLiabilityName(liabilityId)}
                </a></th>
                <c:forEach var="year" items="${scenario.years}">
                    <td align="right">
                        <fmt:formatNumber value="${scenario.getLiabilityAmount(liabilityId, year)}" type="currency" />
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>
        <tr>
            <th>Total Liabilities</th>
            <c:forEach var="year" items="${scenario.years}">
                <td align="right">
                    <fmt:formatNumber value="${scenario.getLiabilityAmount(year)}" type="currency" />
                </td>
            </c:forEach>
        </tr>
        <tr>
            <th>Net Worth</th>
            <c:forEach var="year" items="${scenario.years}">
                <td align="right">
                    <fmt:formatNumber value="${scenario.getNetWorth(year)}" type="currency" />
                </td>
            </c:forEach>
        </tr>
    </table>

    <table border="1">
        <caption>Income & Expenses</caption>
        <tr>
            <th>&nbsp;</th>
            <c:forEach var="year" items="${scenario.years}">
                <th>${year}</th>
            </c:forEach>
        </tr>
        <tr>
            <th>Income</th>
            <td colspan="${scenario.numYears}">&nbsp;</td>
        </tr>
        <c:forEach var="cashFlowSourceId" items="${scenario.cashFlowSourceIds}">
            <tr>
                <th>
                    <a href="scenario/${scenario.getId()}/cashflow/${cashFlowSourceId}">
                        ${scenario.getCashFlowSourceName(cashFlowSourceId)}
                    </a>
                </th>
                <c:forEach var="year" items="${scenario.years}">
                    <td align="right">
                        <a href="scenario/${scenario.getId()}/cashflow/${cashFlowSourceId}/year/${year}">
                            <fmt:formatNumber value="${scenario.getAnnualIncome(cashFlowSourceId, year)}" type="currency" />
                        </a>
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>
        <tr>
            <th>Total Cash Flow</th>
            <c:forEach var="year" items="${scenario.years}">
                <td align="right">
                    <fmt:formatNumber value="${scenario.getAnnualIncome(year)}" type="currency" />
                </td>
            </c:forEach>
        </tr>
    </table>
</c:forEach>

</body>
</html>
