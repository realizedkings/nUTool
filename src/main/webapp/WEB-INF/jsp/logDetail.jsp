<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<%--    <meta charset="euc-kr">--%>
<%--    <meta http-equiv="X-UA-Compatible" content="IE=edge">--%>

    <script>
        function copy(index) {
            window.navigator.clipboard.writeText("복사될 텍스트").then(() => {
                // 복사가 완료되면 이 부분이 호출된다.
                alert("복사 완료!");
            });
        }
    </script>
<%--    <script type="text/ecmascript" src="http://logview.cmcnu.or.kr/himed/webapps/logview/js/jquery-1.12.4.min.js"></script>--%>
<%--    <script type="text/ecmascript" src="http://logview.cmcnu.or.kr/himed/webapps/logview/js/axios.min.js"></script>--%>
<%--    <script type="text/ecmascript" src="http://logview.cmcnu.or.kr/himed/webapps/logview/js/polyfill.min.js"></script>--%>
<%--    <script type="text/ecmascript" src="http://logview.cmcnu.or.kr/himed/webapps/logview/js/jquery.jqGrid.min.js"></script>--%>
<%--    <script type="text/ecmascript" src="http://logview.cmcnu.or.kr/himed/webapps/logview/js/i18n/grid.locale-en.js"></script>--%>
<%--    <script type="text/ecmascript" src="http://logview.cmcnu.or.kr/himed/webapps/logview/js/bootstrap.min.js"></script>--%>

    <link rel="stylesheet" type="text/css" media="screen" href="http://logview.cmcnu.or.kr/himed/webapps/logview/css/jquery-ui.min.css">
    <link rel="stylesheet" type="text/css" media="screen" href="http://logview.cmcnu.or.kr/himed/webapps/logview/css/ui.jqgrid.css">
    <link rel="stylesheet" type="text/css" media="screen" href="http://logview.cmcnu.or.kr/himed/webapps/logview/css/bootstrap.min.css">


    <title>nU LogView</title>

</head>
<body style="margin:5px;">
<button type="button" onclick="copy(1)" value="asdf">dfsdfsf</button>
<div id="basicInfo" style="width:1250px;margin-bottom:10px">
    <span style="margin-right:20px"><b>User ID</b></span><input id="userId" type="text" disabled="" style="margin-right:60px">
    <span style="margin-right:20px"><b>IP Address</b></span><input id="ipAddr" type="text" disabled="" style="margin-right:60px">
    <span style="margin-right:20px"><b>Start Time</b></span><input id="startTime" type="text" disabled="" style="margin-right:60px">
    <span style="margin-right:20px"><b>End Time</b></span><input id="endTime" type="text" disabled="">
</div>

<div class="container">
    <div class="modal fade" id="myModal" role="dialog">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-body" style="overflow-y: scroll;">
                    <pre id="sqlViewer">					</pre></div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="ui-jqgrid ui-widget ui-widget-content ui-corner-all" id="gbox_tree" dir="ltr" style="width: 1250px;">
    <div class="jqgrid-overlay ui-widget-overlay" id="lui_tree"></div>
    <div class="loading ui-state-default ui-state-active" id="load_tree" style="display: none;">Loading...</div>

    <div class="ui-jqgrid-view " role="grid" id="gview_tree" style="width: 1250px;">
        <div class="ui-jqgrid-titlebar ui-jqgrid-caption ui-widget-header ui-corner-top ui-helper-clearfix" style="display: none;">
            <a role="link" class="ui-jqgrid-titlebar-close HeaderButton ui-corner-all" title="Toggle Expand Collapse Grid" style="right: 0px;">
                <span class="ui-jqgrid-headlink ui-icon ui-icon-circle-triangle-n"></span></a><span class="ui-jqgrid-title"></span>
        </div>

        <div class="ui-jqgrid-hdiv ui-state-default ui-corner-top" style="width: 1250px;">
            <div class="ui-jqgrid-hbox">
                <table class="ui-jqgrid-htable ui-common-table " style="width:1250px" role="presentation" aria-labelledby="gbox_tree">
                    <thead>
                    <tr class="ui-jqgrid-labels" role="row">
                        <th id="tree_index" role="columnheader" class="ui-th-column ui-th-ltr ui-state-default" style="width: 30px;">
                            <span class="ui-jqgrid-resize ui-jqgrid-resize-ltr" style="cursor: col-resize;">&nbsp;</span>
                            <div class="ui-th-div ui-jqgrid-sortable" id="jqgh_tree_index">No<span class="s-ico" style="display:none">
                                <span sort="asc" class="ui-grid-ico-sort ui-icon-asc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-n"></span>
                                <span sort="desc" class="ui-grid-ico-sort ui-icon-desc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-s"></span>
                            </span>
                            </div>
                        </th>
                        <th id="tree_packageNm" role="columnheader" class="ui-th-column ui-th-ltr ui-state-default" style="width: 500px;">
                            <span class="ui-jqgrid-resize ui-jqgrid-resize-ltr" style="cursor: col-resize;">&nbsp;</span>
                            <div class="ui-th-div ui-jqgrid-sortable" id="jqgh_tree_packageNm">Package
                                <span class="s-ico" style="display:none">
                                    <span sort="asc" class="ui-grid-ico-sort ui-icon-asc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-n"></span>
                                    <span sort="desc" class="ui-grid-ico-sort ui-icon-desc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-s"></span>
                                </span>
                            </div>
                        </th>
                        <th id="tree_dispMethodNm" role="columnheader" class="ui-th-column ui-th-ltr ui-state-default" style="width: 650px;">
                            <span class="ui-jqgrid-resize ui-jqgrid-resize-ltr" style="cursor: col-resize;">&nbsp;</span>
                            <div class="ui-th-div ui-jqgrid-sortable" id="jqgh_tree_dispMethodNm">Method
                                <span class="s-ico" style="display:none">
                                    <span sort="asc" class="ui-grid-ico-sort ui-icon-asc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-n"></span>
                                    <span sort="desc" class="ui-grid-ico-sort ui-icon-desc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-s"></span>
                                </span>
                            </div>
                        </th>
                        <th id="tree_executeTime" role="columnheader" class="ui-th-column ui-th-ltr ui-state-default" style="width: 70px;">
                            <span class="ui-jqgrid-resize ui-jqgrid-resize-ltr" style="cursor: col-resize;">&nbsp;</span>
                            <div class="ui-th-div ui-jqgrid-sortable" id="jqgh_tree_executeTime">Time(msec)
                                <span class="s-ico" style="display:none">
                                    <span sort="asc" class="ui-grid-ico-sort ui-icon-asc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-n"></span>
                                    <span sort="desc" class="ui-grid-ico-sort ui-icon-desc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-s"></span>
                                </span>
                            </div>
                        </th>
                        <th id="tree_isError" role="columnheader" class="ui-th-column ui-th-ltr ui-state-default" style="width: 150px; display: none;">
                            <span class="ui-jqgrid-resize ui-jqgrid-resize-ltr" style="cursor: col-resize;">&nbsp;</span>
                            <div class="ui-th-div ui-jqgrid-sortable" id="jqgh_tree_isError">isError
                                <span class="s-ico" style="display:none">
                                    <span sort="asc" class="ui-grid-ico-sort ui-icon-asc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-n"></span>
                                    <span sort="desc" class="ui-grid-ico-sort ui-icon-desc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-s"></span>
                                </span>
                            </div>
                        </th>
                        <th id="tree_isSql" role="columnheader" class="ui-th-column ui-th-ltr ui-state-default" style="width: 150px; display: none;">
                            <span class="ui-jqgrid-resize ui-jqgrid-resize-ltr" style="cursor: col-resize;">&nbsp;</span>
                            <div class="ui-th-div ui-jqgrid-sortable" id="jqgh_tree_isSql">isSql
                                <span class="s-ico" style="display:none">
                                    <span sort="asc" class="ui-grid-ico-sort ui-icon-asc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-n"></span>
                                    <span sort="desc" class="ui-grid-ico-sort ui-icon-desc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-s"></span>
                                </span>
                            </div>
                        </th>
                        <th id="tree_isProcedure" role="columnheader" class="ui-th-column ui-th-ltr ui-state-default" style="width: 150px; display: none;">
                            <span class="ui-jqgrid-resize ui-jqgrid-resize-ltr" style="cursor: col-resize;">&nbsp;</span>
                            <div class="ui-th-div ui-jqgrid-sortable" id="jqgh_tree_isProcedure">isProcedure
                                <span class="s-ico" style="display:none">
                                    <span sort="asc" class="ui-grid-ico-sort ui-icon-asc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-n"></span>
                                    <span sort="desc" class="ui-grid-ico-sort ui-icon-desc ui-sort-ltr ui-state-disabled ui-icon ui-icon-triangle-1-s"></span>
                                </span>
                            </div>
                        </th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>

        <div class="ui-jqgrid-bdiv" style="height: auto; width: 1250px;">

            <div style="position:relative;">
                <div>

                </div>
                <table id="tree" tabindex="0" role="presentation" aria-multiselectable="false" aria-labelledby="gbox_tree" class="ui-jqgrid-btable ui-common-table" style="width: 1250px;">
                    <tbody>
                        <tr class="jqgfirstrow" role="row">
                            <td role="gridcell" style="height:0px;width:30px;"></td>
                            <td role="gridcell" style="height:0px;width:500px;"></td>
                            <td role="gridcell" style="height:0px;width:650px;"></td>
                            <td role="gridcell" style="height:0px;width:70px;"></td>
                            <td role="gridcell" style="height:0px;width:150px;display:none;"></td>
                            <td role="gridcell" style="height:0px;width:150px;display:none;"></td>
                            <td role="gridcell" style="height:0px;width:150px;display:none;"></td>
                        </tr>
                        <c:forEach var="logs" items="${logs}" varStatus="status">
                            <c:if test="${logs.isQuery eq 'Y'}">
                                <tr role="row" id="${status.index}" tabindex="-1" class="jqgrow ui-row-ltr ui-widget-content" style="color:green">
                                    <td role="gridcell" style="text-align:right;" title="${logs.no}" aria-describedby="tree_index">${logs.no}</td>
                                    <td role="gridcell" style="" title="${logs.packageName}" aria-describedby="tree_packageNm">${logs.packageName}</td>
                                    <td role="gridcell" style="" title="${logs.methodName}" aria-describedby="tree_dispMethodNm">${logs.methodName}<br><input id="query${logs.no}" value="${logs.query}" style="display:none;" /><button type="button" onclick="copy(${logs.no})">Literal</button><button type="button" onclick="sql_click( 'B', 2);">Bind</button><button type="button" onclick="sql_click( 'S', 2);">Script</button><button type="button" onclick="sql_click( 'V', 2);">View</button></td>
                                    <td role="gridcell" style="text-align:right;" title="${logs.runTime}" aria-describedby="tree_executeTime">${logs.runTime}</td>
                                    <td role="gridcell" style="display:none;" title="false" aria-describedby="tree_isError">false</td>
                                    <td role="gridcell" style="display:none;" title="false" aria-describedby="tree_isSql">false</td>
                                    <td role="gridcell" style="display:none;" title="false" aria-describedby="tree_isProcedure">false</td>
                                </tr>
                            </c:if>
                            <c:if test="${logs.isQuery eq 'N'}">
                                <tr role="row" id="${status.index}" tabindex="-1" class="jqgrow ui-row-ltr ui-widget-content">
                                    <td role="gridcell" style="text-align:right;" title="${logs.no}" aria-describedby="tree_index">${logs.no}</td>
                                    <td role="gridcell" style="" title="${logs.packageName}" aria-describedby="tree_packageNm">${logs.packageName}</td>
                                    <td role="gridcell" style="" title="${logs.methodName}" aria-describedby="tree_dispMethodNm">${logs.methodName}</td>
                                    <td role="gridcell" style="text-align:right;" title="${logs.runTime}" aria-describedby="tree_executeTime">${logs.runTime}</td>
                                    <td role="gridcell" style="display:none;" title="false" aria-describedby="tree_isError">false</td>
                                    <td role="gridcell" style="display:none;" title="false" aria-describedby="tree_isSql">false</td>
                                    <td role="gridcell" style="display:none;" title="false" aria-describedby="tree_isProcedure">false</td>
                                </tr>
                            </c:if>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <div class="ui-jqgrid-resize-mark" id="rs_mtree">&nbsp;</div>
</div>
</body>
</html>
