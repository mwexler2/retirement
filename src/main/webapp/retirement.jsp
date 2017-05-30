<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page isELIgnored="false" %>
<html>
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
            <td><c:out value="${person.birthDate}"/></td>
            <td><c:out value="${person.retirementAge}"/></td>
        </tr>
    </c:forEach>
</table>

<c:forEach var="scenario" items="${command.scenarios}">
    <h2>${scenario.name}</h2>

<table border="1">
    <caption>Assumptions</caption>
    <tr>
        <th>Long Term Investment Return</th><td>${command.pf.format(scenario.assumptions.longTermInvestmentReturn)}</td>
    </tr><tr>
        <th>Short Term Investment Return</th><td>${command.pf.format(scenario.assumptions.shortTermInvestmentReturn)}</td>
    </tr><tr>
        <th>Inflation</th><td>${command.pf.format(scenario.assumptions.inflation)}</td>
    </tr><tr>
        <th>Years in short term</th><td>${scenario.assumptions.yearsInShortTerm}
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
        <c:forEach var="incomeSourceId" items="${scenario.incomeSourceIds}">
            <tr>
                <th>${scenario.getIncomeSourceName(incomeSourceId)}</th>
                <c:forEach var="year" items="${scenario.years}">
                    <td align="right">${command.cf.format(scenario.getAnnualIncome(incomeSourceId, year))}</td>
                </c:forEach>
            </tr>
        </c:forEach>
            <tr>
                <th>Total Income</th>
                <c:forEach var="year" items="${scenario.years}">
                    <td align="right">${command.cf.format(scenario.getAnnualIncome(year))}</td>
                </c:forEach>
            </tr>
        <th>Expense</th>
            <td colspan="${scenario.numYears}">&nbsp;</td>
        </tr>
        <c:forEach var="expenseSourceId" items="${scenario.expenseSourceIds}">
            <tr>
                <th>${scenario.getExpenseSourceName(expenseSourceId)}</th>
                <c:forEach var="year" items="${scenario.years}">
                    <td align="right">${command.cf.format(scenario.getAnnualExpense(expenseSourceId, year))}</td>
                </c:forEach>
            </tr>
        </c:forEach>
        <tr>
            <th>Total Expenses</th>
            <c:forEach var="year" items="${scenario.years}">
                <td align="right">${command.cf.format(scenario.getAnnualExpense(year))}</td>
            </c:forEach>
        </tr>
        <tr>
            <th>Net Income</th>
            <c:forEach var="year" items="${scenario.years}">
                <td align="right">${command.cf.format(scenario.getNetIncome(year))}</td>
            </c:forEach>
        </tr>
    </table>
</c:forEach>

</body>
</html>
