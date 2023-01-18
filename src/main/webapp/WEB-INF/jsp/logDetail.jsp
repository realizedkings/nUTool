<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<%--    <meta charset="euc-kr">--%>
<%--    <meta http-equiv="X-UA-Compatible" content="IE=edge">--%>
    <script type="text/ecmascript" src="http://logview.cmcnu.or.kr/himed/webapps/logview/js/jquery-1.12.4.min.js"></script>
    <script type="text/ecmascript" src="http://logview.cmcnu.or.kr/himed/webapps/logview/js/axios.min.js"></script>
    <script type="text/ecmascript" src="http://logview.cmcnu.or.kr/himed/webapps/logview/js/polyfill.min.js"></script>
    <script type="text/ecmascript" src="http://logview.cmcnu.or.kr/himed/webapps/logview/js/jquery.jqGrid.min.js"></script>
    <script type="text/ecmascript" src="http://logview.cmcnu.or.kr/himed/webapps/logview/js/i18n/grid.locale-en.js"></script>
    <script type="text/ecmascript" src="http://logview.cmcnu.or.kr/himed/webapps/logview/js/bootstrap.min.js"></script>

    <link rel="stylesheet" type="text/css" media="screen" href="http://logview.cmcnu.or.kr/himed/webapps/logview/css/jquery-ui.min.css">
    <link rel="stylesheet" type="text/css" media="screen" href="http://logview.cmcnu.or.kr/himed/webapps/logview/css/ui.jqgrid.css">
    <link rel="stylesheet" type="text/css" media="screen" href="http://logview.cmcnu.or.kr/himed/webapps/logview/css/bootstrap.min.css">


    <title>nU LogView</title>
    <script>
        function copy(index) {
            var query = document.getElementById("query" + index);
            query.setAttribute("style", "");
            query.select();
            document.execCommand('copy');
            query.setAttribute("style", "display: none;");

            alert("클립보드에 복사되었습니다.");
        }
    </script>
    <script language="javascript">
        function why(url) {
            window.open(url + "",'_blank','menubar=0,resizable=1,toolbar=0,location=0,scrollbars=1');
        }
    </script>
    <style>
        #my_modal {
            position: absolute;
            display: none;
            width: 1000px;
            height: 100%;
            max-height: 1000px;
            padding: 20px 60px;
            background-color: #fefefe;
            border: 1px solid #888;
            border-radius: 3px;
        }

        #my_modal .modal_close_btn {
            position: absolute;
            top: 10px;
            right: 10px;
        }

        #my_modal .box {
            position: absolute;
            top: 0px;
            right: 0px;
            bottom: 0px;
            left: 0px;
            padding: 5px 10px;
            overflow: scroll;
            overflow-y: auto;
            height: 100%;
            max-height: 1000px;
            -webkit-overflow-scrolling: touch;
        }
    </style>
</head>
<body style="margin:5px;">
<div id="basicInfo" style="width:1300px;margin-bottom:10px">
    <span style="margin-right:20px"><b>User ID</b></span><input id="userId" type="text" disabled="" style="margin-right:60px">
    <span style="margin-right:20px"><b>IP Address</b></span><input id="ipAddr" type="text" disabled="" style="margin-right:60px">
    <span style="margin-right:20px"><b>Start Time</b></span><input id="startTime" value="${logs[0].startTime}" type="text" disabled="" style="margin-right:60px">
    <span style="margin-right:20px"><b>End Time</b></span><input id="endTime" value="${logs[0].endTime}" type="text" disabled="">
    <br><a href="${originalUrl}" target="">원본 로그</a>
<%--    <button type="button" onclick="why(${originalUrl})">원본 로그</button>--%>
</div>
<div id="my_modal">
        <pre class="box" id="sqlViewer"></pre>
        <a class="modal_close_btn">닫기</a>
</div>

<%--<div class="container">--%>
<%--    <div class="modal fade" id="myModal" role="dialog">--%>
<%--        <div class="modal-dialog modal-lg">--%>
<%--            <div class="modal-content">--%>
<%--                <div class="modal-body" style="overflow-y: scroll;">--%>
<%--                    <pre id="sqlViewer"></pre></div>--%>
<%--                <div class="modal-footer">--%>
<%--                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </div>--%>
<%--    </div>--%>
<%--</div>--%>
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
                                    <td role="gridcell" style="" title="${logs.methodName}" aria-describedby="tree_dispMethodNm">${logs.methodName}<br><textarea style="display: none;" id="query${logs.no}">${logs.query}</textarea><button type="button" onclick="copy(${logs.no})">Literal</button><button onclick="onModal(${logs.no})" id="popup_open_btn">View</button></td>
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

<script>
    function modal(id) {
        var zIndex = 9999;
        var modal = document.getElementById(id);

        // 모달 div 뒤에 희끄무레한 레이어
        var bg = document.createElement('div');
        bg.setStyle({
            position: 'fixed',
            zIndex: zIndex,
            left: '0px',
            top: '0px',
            width: '100%',
            height: '100%',
            overflow: 'auto',
            // 레이어 색갈은 여기서 바꾸면 됨
            backgroundColor: 'rgba(0,0,0,0.4)'
        });
        bg.onclick = close;
        document.body.append(bg);

        // 닫기 버튼 처리, 시꺼먼 레이어와 모달 div 지우기
        modal.querySelector('.modal_close_btn').addEventListener('click', function() {
            bg.remove();
            modal.style.display = 'none';
        });

        function close() {
            bg.remove();
            modal.style.display = 'none';
        }

        modal.setStyle({
            position: 'fixed',
            display: 'block',
            boxShadow: '0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19)',

            // 시꺼먼 레이어 보다 한칸 위에 보이기
            zIndex: zIndex + 1,

            // div center 정렬
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            msTransform: 'translate(-50%, -50%)',
            webkitTransform: 'translate(-50%, -50%)'
        });
    }

    // Element 에 style 한번에 오브젝트로 설정하는 함수 추가
    Element.prototype.setStyle = function(styles) {
        for (var k in styles) this.style[k] = styles[k];
        return this;
    };

    function onModal(index) {
        var viewr = document.getElementById("sqlViewer");
        var query = document.getElementById("query" + index);
        viewr.innerText = query.innerText;

        modal('my_modal');
    }
</script>

</body>
</html>
