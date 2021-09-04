<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ page isELIgnored="false" %>

<link href="<%=request.getContextPath()%>/css/retirement.css" rel="stylesheet" />
<script type="application/javascript" src="<%=request.getContextPath()%>/js/retirement.js" ></script>
<html>
<body>
 <display:table uid="item" name="${command.cashFlows}" sort="external"
                decorator="name.wexler.retirement.visualizer.Tables.MultilevelBigDecimalLinkTableDecorator">
        <display:caption>Cash Flows for ${command.scenarioId}/${command.category}/{$command.year}</display:caption>
        <display:column property="itemType" group="1" />
        <display:column property="cashFlowDate" />
        <display:column property="accrualStart" />
        <display:column property="accrualEnd" />
        <display:column property="category" />
        <display:column property="cashFlowSinkId" />
        <display:column property="description" />
        <display:column property="labels" />
        <display:column property="notes" />
        <display:column property="amount" class="money" total="true"
                        decorator="name.wexler.retirement.visualizer.Tables.MoneyTableColumnDecorator"/>
    </display:table>
</body>
</html>
