package phis.his.nu.logging;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LoggingNuServiceImpl implements LoggingNuService {
    public List<Map<String, String>> parseLog(String text) throws Exception {
        String[] allLines = text.split("\n");
        List<Map<String, String>> totalReturns = new ArrayList<>();

        Map<String, String> queryInfo = null;
        Map<String, Object> batchQueryInfo = null;
        Map<String, String> batchQueryParams = new HashMap<>();    // 미리생성해 둬야함
        Map<String, String> errorInfo = null; // 에러 발생시 사용 변수
        StringBuilder queryString = null;
        StringBuilder errorString = null;

        int layer = 0;
        boolean queryFlag = false;
        boolean updateFlag = false;
        boolean batchFlag = false;
        boolean errorFlag = false;

        for (int i = 0; i < allLines.length; i++) {
            String line = allLines[i];

            // 쿼리문 셋팅 시작
            if (queryFlag && queryInfo != null) { // select 쿼리문 정보
                queryString.append(line + "\r\n"); // 쿼리문 삽입
                // 다음 행에 쿼리 종료 시 전체 리스트에 넣고 종료
                if (i != allLines.length - 2
                        && (allLines[i + 2].length() - allLines[i + 2].replace("]", "").length()) >= 4
                        && (allLines[i + 2].length() - allLines[i + 2].replace("[", "").length()) >= 4) {
                    StringBuilder bindingQuery = new StringBuilder(queryString.toString());
                    String[] params = queryInfo.get("param").split(", ");
                    int bindNumber = bindingQuery.indexOf("?");

                    // 바인딩 할 개수 일치 시에만
                    if ((bindingQuery.length() - bindingQuery.toString().replace("?", "").length())
                            == params.length) {
                        for (String param : params) {
                            bindingQuery.deleteCharAt(bindNumber);
                            bindingQuery.insert(bindNumber, "'" + param + "'");

                            bindNumber = bindingQuery.indexOf("?");
                        }
                    } else {
                        bindingQuery.insert(0, "Parameter 에 \", \" 가 포함되어 있어 바인딩이 취소되었습니다.");
                    }

                    queryInfo.put("query", queryInfo.get("statement") + "\r\n" + bindingQuery.toString());
                    queryInfo.put("isQuery", "Y");

                    queryString = null;
                    queryFlag = false;
                    layer--;

                    totalReturns.add(queryInfo);
                }
            } else if (updateFlag && queryInfo != null) {
                // 다음 행에 쿼리 종료 시 전체 리스트에 넣고 종료
                if (i != allLines.length - 2
                        && (allLines[i + 1].length() - allLines[i + 1].replace("]", "").length()) >= 4
                        && (allLines[i + 1].length() - allLines[i + 1].replace("[", "").length()) >= 4) {
                    StringBuilder bindingQuery = new StringBuilder(queryString.toString());
                    String[] params = queryInfo.get("param").split(", ");
                    int bindNumber = bindingQuery.indexOf("?");

                    String statement = line.split("[|]")[0];
                    queryInfo.put("statement", statement);

                    // 바인딩 할 개수 일치 시에만
                    if ((bindingQuery.length() - bindingQuery.toString().replace("?", "").length())
                            == params.length) {
                        for (String param : params) {
                            bindingQuery.deleteCharAt(bindNumber);
                            bindingQuery.insert(bindNumber, "'" + param + "'");

                            bindNumber = bindingQuery.indexOf("?");
                        }
                    } else {
                        bindingQuery.insert(0, "Parameter 에 \", \" 가 포함되어 있어 바인딩이 취소되었습니다.");
                    }

                    queryInfo.put("query", queryInfo.get("statement") + "\r\n" + bindingQuery.toString());
                    queryInfo.put("isQuery", "Y");

                    totalReturns.add(queryInfo);

                    queryString = null;
                    updateFlag = false;
                    layer--;

                    continue;
                }

                queryString.append(line + "\r\n"); // 쿼리문 삽입
            } else if (batchFlag) { // batch 쿼리 삽입
                // 다음 행에 쿼리 종료 시 전체 리스트에 넣고 종료
                if (i != allLines.length - 2
                        && (allLines[i + 1].length() - allLines[i + 1].replace("]", "").length()) >= 4
                        && (allLines[i + 1].length() - allLines[i + 1].replace("[", "").length()) >= 4) {
                    StringBuilder bindingQuery = null;
                    StringBuilder batchBindingQuery = new StringBuilder();
                    queryInfo = new HashMap<>();

                    Map<String, String> batchParams = (Map) batchQueryInfo.get("param");

                    String statement = line.split("[|]")[0];
                    queryInfo.put("statement", statement);

                    for (int j = 0; j < batchParams.size(); j++) {
                        bindingQuery = new StringBuilder(queryString.toString());
                        String[] params = batchParams.get(j + "").split(", ");

                        int bindNumber = bindingQuery.indexOf("?");

                        // 바인딩 할 개수 일치 시에만
                        if ((bindingQuery.length() - bindingQuery.toString().replace("?", "").length())
                                == params.length) {
                            for (String param : params) {
                                bindingQuery.deleteCharAt(bindNumber);
                                bindingQuery.insert(bindNumber, "'" + param + "'");

                                bindNumber = bindingQuery.indexOf("?");
                            }
                        } else {
                            bindingQuery.insert(0, "Parameter 에 \", \" 가 포함되어 있어 바인딩이 취소되었습니다.");
                        }

                        batchBindingQuery.append("-- " + (j+1) + " 번째 배치 \r\n" + queryInfo.get("statement") + "\r\n" + bindingQuery.toString() + ";\r\n");
                    }

                    queryInfo.put("query", batchBindingQuery.toString());
                    queryInfo.put("isQuery", "Y");
                    queryInfo.put("records", (String) batchQueryInfo.get("records"));
                    queryInfo.put("runTime", (String) batchQueryInfo.get("runTime"));
                    queryInfo.put("packageName", (String) batchQueryInfo.get("packageName"));
                    queryInfo.put("layer", (String) batchQueryInfo.get("layer"));

                    totalReturns.add(queryInfo);

                    queryString = null;
                    batchQueryInfo = null;
                    queryInfo = null;
                    batchFlag = false;
                    layer--;

                    continue;
                }

                queryString.append(line + "\r\n"); // 쿼리문 삽입
            }
            // 쿼리문 셋팅 종료

            // 에러 셋팅 start
            if (errorFlag) {
                errorString.append(line + "\r\n");

                // 다음행 행 종료시 바인딩 하고 끝
                if (i < allLines.length - 1
                        && (allLines[i + 1].length() - allLines[i + 1].replace("]", "").length()) >= 4
                        && (allLines[i + 1].length() - allLines[i + 1].replace("[", "").length()) >= 4) {
                    errorInfo.put("methodName", errorString.toString());

                    totalReturns.add(errorInfo);

                    errorFlag = false;
                    continue;
                }
            }
            // 에러 셋팅 end


            // 일반 로그
            if ((line.length() - line.replace("]", "").length()) >= 4
                    && (line.length() - line.replace("[", "").length()) >= 4) {
                int start = 0;
                int end = 0;

                start = line.indexOf("[", start);
                end = line.indexOf("]", end);
                String nodeInfo = line.substring(start + 1, end);

                start = line.indexOf("[", start + 1);
                end = line.indexOf("]", end + 1);
                String timeInfo = line.substring(start + 1, end);

                start = line.indexOf("[", start + 1);
                end = line.indexOf("]", end + 1);
                String printInfo = line.substring(start + 1, end).trim();

                start = line.indexOf("[", start + 1);
                end = line.indexOf("]", end + 1);
                String packageInfo = line.substring(start + 1, end).trim();

                // 에러 정보
                if (printInfo != null && printInfo.indexOf("ERROR") != -1) {
                    String methodName = line.substring(end + 1);

                    HashMap<String, String> methodInfo = new HashMap<>();
                    methodInfo.put("methodName", methodName);
                    methodInfo.put("packageName", packageInfo);
                    methodInfo.put("layer", layer + "");
                    methodInfo.put("isQuery", "E");  // 에러

                    if ("".equals(methodName.trim())) {
                        errorFlag = true;
                        errorString = new StringBuilder();
                        errorInfo = methodInfo;
                    } else {
                        totalReturns.add(methodInfo);
                    }
                }

                // 메소드 시작 정보
                if (line.indexOf("starts.", end) != -1) {
                    String methodName = line.substring(end + 1, line.indexOf("starts.", end));

                    if ("getConnection()".equals(methodName.trim())
                            || "toString()".equals(methodName.trim())) continue;

                    HashMap<String, String> methodInfo = new HashMap<>();
                    methodInfo.put("methodName", methodName);
                    methodInfo.put("packageName", packageInfo);
                    methodInfo.put("layer", layer + "");
                    methodInfo.put("isQuery", "N");

                    if (i == 0) {
                        methodInfo.put("startTime", timeInfo);
                    }

                    totalReturns.add(methodInfo);

                    layer++;
                }

                // 메소드 종료 정보
                if (line.indexOf("ends.", end) != -1) {
                    String methodName = line.substring(end + 1, line.indexOf("ends.", end));
                    String runTime = line.substring(line.lastIndexOf("(") + 1, line.length()).replace("}", "");

                    if ("getConnection()".equals(methodName.trim())
                            || "toString()".equals(methodName.trim())) {
                        continue;
                    }  // 커넥션 보기 싫어서 페스

//                    HashMap<String, String> methodInfo = new HashMap<>();
//                    methodInfo.put("methodName", methodName);
//                    methodInfo.put("runTime", runTime.replace("msecs", "").trim());
//                    methodInfo.put("package", packageInfo);
//                    methodInfo.put("layer", (layer - 1) + "");
//                    methodInfo.put("isQuery", "N");
//
//                    totalReturns.add(methodInfo);

                    // 시작 정보에 종료 정보 중 수행 시간만 삽입
                    for (int j = totalReturns.size() - 1; j >= 0; j--) {
                        Map element = totalReturns.get(j);

                        if ("N".equals(element.get("isQuery")) && element.get("methodName").equals(methodName)
                                        && element.get("layer").equals((layer - 1) + "")) {
                            element.put("runTime", runTime.replace("msecs", "").trim());
                            element.put("endTime", timeInfo);
                            break;
                        }
                    }

                    layer--;
                }

                // 쿼리 시작 정보
                if (line.indexOf("execute query", end) != -1) {
                    String[] queryParts = line.substring(line.indexOf("execute query", end) + 14).split("[|]");
                    queryString = new StringBuilder();
                    queryInfo = new HashMap<String, String>();

                    String param = queryParts[2];
                    param = param.substring(param.indexOf("[") + 1, param.indexOf("]"));

                    // 쿼리에서 오류 발생했을 때
                    if (queryParts[3].indexOf("ORA-") != -1) {
                        queryInfo.put("records", "ERROR");
                        queryInfo.put("statement", allLines[i + 1] + " " + queryParts[3]);
                    } else {
                        queryInfo.put("records", queryParts[3]);
                        queryInfo.put("statement", queryParts[4]);
                    }

                    queryInfo.put("id", queryParts[0]);
                    queryInfo.put("runTime", queryParts[1].replace("msec", "").trim());
                    queryInfo.put("param", param);
                    queryInfo.put("packageName", packageInfo);
                    queryInfo.put("layer", layer + "");

                    queryFlag = true;
                    layer++;
                } else if (line.indexOf("execute update", end) != -1) {
                    String[] queryParts = line.substring(line.indexOf("execute update", end) + 14).split("[|]");
                    queryString = new StringBuilder();
                    queryInfo = new HashMap<String, String>();

                    String param = queryParts[2];
                    param = param.substring(param.indexOf("[") + 1, param.indexOf("]"));

                    queryInfo.put("id", queryParts[0]);
                    queryInfo.put("runTime", queryParts[1].replace("msec", "").trim());
                    queryInfo.put("param", param);
                    queryInfo.put("records", queryParts[3]);
                    queryInfo.put("packageName", packageInfo);
                    queryInfo.put("layer", layer + "");

                    updateFlag = true;
                    layer++;
                } else if (line.indexOf("execute batch", end) != -1) {
                    String batchInfo = line.substring(line.indexOf("execute batch", end) + 14);

                    if (batchInfo.indexOf("param[") != 0) {
                        batchQueryInfo = new HashMap<>();
                        queryString = new StringBuilder();

                        batchQueryInfo.put("records", batchInfo.split("[|]")[2]);
                        batchQueryInfo.put("runTime", batchInfo.split("[|]")[1].replace("msec", "").trim());
                        batchQueryInfo.put("param", batchQueryParams);
                        batchQueryInfo.put("packageName", packageInfo);
                        batchQueryInfo.put("layer", layer + "");

                        batchQueryParams = new HashMap<>(); // params 넘겼으니 초기화
                        batchFlag = true;
                        layer++;

                        continue;
                    }

                    String paramNumber = batchInfo.substring(6, batchInfo.indexOf("]"));
                    String paramInfo = batchInfo.substring(batchInfo.indexOf("=") + 2, batchInfo.length() - 1);
                    batchQueryParams.put(paramNumber, paramInfo);

                } else if (line.indexOf("execute call succeeded") != -1) {

                } // 쿼리 시작 정보 종료
            } // 일반로그 종료
        } // for 문 종료

        // 문자열 다듬기
        for (int i = 0; i < totalReturns.size(); i++) {
            Map<String, String> printMethod = totalReturns.get(i);
            StringBuilder tabText = new StringBuilder(); // 계층 띄어쓰기

            printMethod.put("no", i + "");

            for (int j = 0; j < Integer.parseInt(printMethod.get("layer")); j++) {
                tabText.append("     ");
            }

            if ("Y".equals(printMethod.get("isQuery"))) {
                printMethod.put("methodName", "execute query (" + printMethod.get("records") + ")\r\n"
                        + printMethod.get("statement"));
                printMethod.put("packageName", tabText + "○" + printMethod.get("packageName"));
            } else if ("N".equals(printMethod.get("isQuery"))) {
                printMethod.put("packageName", tabText + "▼" + printMethod.get("packageName"));
            } else { // "E".equals(printMethod.get("isQuery"))
                printMethod.put("packageName", tabText + "○" + printMethod.get("packageName"));
            }

            // 만약 오류가 발생했거나 하는 이유로 실행시간이 없다면 셋팅
            String runTime = printMethod.get("runTime");
            if (runTime == null || "".equals(runTime)) {
                printMethod.put("runTime", "NOT DONE");
            }
        }

        return totalReturns;
    }
}
