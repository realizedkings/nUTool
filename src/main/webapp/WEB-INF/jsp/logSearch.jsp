<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link rel="stylesheet" type="text/css" href="webapps/css/trlog.css">
    <script language="javascript">
        function lmon(trid,ctx,node,logdt) { window.open('ulog.nu?trid='+trid+'&ctx='+ctx+'&node='+node+'&date='+logdt,'_blank','menubar=0,resizable=1,toolbar=0,location=0,scrollbars=1');}
        function sview(svcname) { window.open('svc.nu?name='+svcname,'_blank','menubar=0,resizable=1,toolbar=0,location=0,scrollbars=1');}
    </script>
</head>

<body>
<form id="req" action="/cmcnu/trlog.nu" method="GET">
    ip_addr: <input type="text" name="ip_addr" value="10.110.14.161">
    svc_name: <input type="text" name="svc_name" value="">
    user_id: <input type="text" name="user_id" value="">
    tr_id: <input type="text" name="tr_id" value="">
    date: <input type="text" name="date" value="2022122315">
    svc_url: <input type="text" name="svc_url" value="">
    succ_yn: <input type="text" name="succ_yn" value="">
    op_name: <input type="text" name="op_name" value="">
    <input type="submit" value="submit">
</form>

<table border="1">
    <tbody>
    <tr>
        <th>time</th>
        <th>node</th>
        <th>tr_id</th>
        <th>user_id</th>
        <th>ip_addr</th>
        <th>screen_id</th>
        <th>svc_url</th>
        <th>total_msec</th>
        <th>svc_name</th>
        <th>op_name</th>
        <th>svc_msec</th>
        <th>succ_yn</th>
        <th>ret_count</th></tr>
    <tr><td>2022.12.23 15:02:55</td>
        <td>null_</td>
        <td class="link" onclick="lmon(&quot;68000898&quot;,&quot;&quot;,&quot;null_&quot;,&quot;1671775375000&quot;)">68000898</td>
        <td></td><td>10.110.14.161</td>
        <td>trlog.nu</td><td>null</td>
        <td>1</td>
        <td class="link" onclick="sview(&quot;null&quot;)">null</td>
        <td>null</td>
        <td>null</td>
        <td>T</td>
        <td>null</td>
    </tr>
    </tbody>
</table>
</body>
</html>
