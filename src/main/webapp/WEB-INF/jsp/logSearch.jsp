<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link rel="stylesheet" type="text/css" href="http://emr${instcd}edu.cmcnu.or.kr/webapps/css/trlog.css">
    <script language="javascript">
        function lmon(trid,ctx,node,logdt) { window.open('ulog.nu?trid='+trid+'&ctx='+ctx+'&node='+node+'&date='+logdt,'_blank','menubar=0,resizable=1,toolbar=0,location=0,scrollbars=1');}
        function sview(svcname) { window.open('svc.nu?name='+svcname,'_blank','menubar=0,resizable=1,toolbar=0,location=0,scrollbars=1');}
    </script>
</head>

<body>
<form id="req" action="/cmcnu/trlog.nu?instcd=${instcd}" method="GET">
    ip_addr: <input type="text" name="ip_addr" value="${logging.ip_addr}">
    svc_name: <input type="text" name="svc_name" value="${logging.svc_name}">
    user_id: <input type="text" name="user_id" value="${logging.user_id}">
    tr_id: <input type="text" name="tr_id" value="${logging.tr_id}">
    date: <input type="text" name="date" value="${logging.date}">
    svc_url: <input type="text" name="svc_url" value="${logging.svc_url}">
    succ_yn: <input type="text" name="succ_yn" value="${logging.succ_yn}">
    op_name: <input type="text" name="op_name" value="${logging.op_name}">
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
        <th>ret_count</th>
    </tr>
    </tbody>
</table>
</body>
</html>
