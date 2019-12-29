<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ page isELIgnored="false" %>

<script src="/js/retirement.js" type="application/javascript" ></script>
<link rel="stylesheet" href="/css/retirement.css" />

<html>
<body>
 <display:table uid="item" name="${command.shareBalances}" sort="external"
                decorator="name.wexler.retirement.visualizer.Tables.MultilevelBigDecimalLinkTableDecorator">
        <display:caption>Share Balances for ${command.scenarioId}</display:caption>
        <display:column property="ticker" group="1" />
        <display:column property="accountCompany" group="2" />
        <display:column property="accountName" />
        <display:column property="balanceDate" />
        <display:column property="shares" total="true" />
        <display:column property="sharePrice" class="money"
                        decorator="name.wexler.retirement.visualizer.Tables.MoneyTableColumnDecorator"/>
         <display:column property="shareValue" class="money" total="true"
                     decorator="name.wexler.retirement.visualizer.Tables.MoneyTableColumnDecorator"/>
    </display:table>
</body>
</html>
