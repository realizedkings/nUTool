package phis.his.nu.logging;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@Service
public class LoggingNuServiceImpl implements LoggingNuService {
    @Override
    public List<Map<String, String>> parseLog(String text) {
        String[] allLines = text.split("\n");
        List<Map<String, String>> totalReturns = new ArrayList<>();

        Map<String, String> queryInfo = null;
        StringBuilder queryString = null;

        int layer = 0;
        boolean queryFlag = false;

        for (int i = 0; i < allLines.length; i++) {
            String line = allLines[i];

            // 쿼리 문 정보
            if (queryFlag && queryInfo != null) {
                queryString.append(line + "\r\n"); // 쿼리문 삽입

                // 다음 행에 쿼리 종료 시 전체 리스트에 넣고 종료
                if (i != allLines.length -1
                            && allLines[i+1].indexOf("[") == 9 && allLines[i+1].indexOf("node=") == 10) {
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

                    queryInfo.put("query", bindingQuery.toString());
                    queryInfo.put("isQuery", "Y");
                    totalReturns.add(queryInfo);

                    queryString = null;
                    queryFlag = false;
                    layer--;
                }
            }

            // 일반 로그
            if (line.indexOf("[") == 9 && line.indexOf("node=") == 10) {
                int start = 0;
                int end   = 0;

                start = line.indexOf("[", start);
                end   = line.indexOf("]", end);
                String nodeInfo = line.substring(start + 1, end);

                start = line.indexOf("[", start + 1);
                end   = line.indexOf("]", end + 1);
                String timeInfo = line.substring(start + 1, end);

                start = line.indexOf("[", start + 1);
                end   = line.indexOf("]", end + 1);
                String printInfo = line.substring(start + 1, end).trim();

                start = line.indexOf("[", start + 1);
                end   = line.indexOf("]", end + 1);
                String packageInfo = line.substring(start + 1, end).trim();

                // 메소드 시작 정보
                if (line.indexOf("starts.", end) != -1) {
                    String methodName = line.substring(end + 1, line.indexOf("starts.", end));

                    if ("getConnection()".equals(methodName.trim())) continue;

                    HashMap<String, String> methodInfo = new HashMap<>();
                    methodInfo.put("methodName", methodName);
                    methodInfo.put("package", packageInfo);
                    methodInfo.put("layer", layer + "");
                    methodInfo.put("isQuery", "N");

                    totalReturns.add(methodInfo);

                    layer++;
                }

                // 메소드 종료 정보
                if (line.indexOf("ends.", end) != -1) {
                    String methodName = line.substring(end + 1, line.indexOf("ends.", end));
                    String runTime = line.substring(line.lastIndexOf("(") + 1, line.length()).replace("}", "");

                    if ("getConnection()".equals(methodName.trim())) { continue; }  // 커넥션 보기 싫어서 페스

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

                        if ("N".equals(element.get("isQuery")) && element.get("methodName").equals(methodName)) {
                            element.put("runTime", runTime.replace("msecs", "").trim());

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

                    queryInfo.put("id", queryParts[0]);
                    queryInfo.put("runTime", queryParts[1].replace("[|]", "").replace("msecs", "").trim());
                    queryInfo.put("param", param);
                    queryInfo.put("records", queryParts[3]);
                    queryInfo.put("statement", queryParts[4]);
                    queryInfo.put("package", packageInfo);
                    queryInfo.put("layer", layer + "");

                    queryFlag = true;
                    layer++;
                }
            } // 일반로그 종료
        } // for 문 종료

        // 문자열 다듬기
        for (int i = 0; i < totalReturns.size(); i++) {
            Map<String, String> printMethod = totalReturns.get(i);
            StringBuilder tabText = new StringBuilder(); // 계층 띄어쓰기

            printMethod.put("no", i + "");

            for (int j = 0; j < Integer.parseInt(printMethod.get("layer")); j++) {
                tabText.append("    ");
            }

            if ("Y".equals(printMethod.get("isQuery"))) {
                printMethod.put("methodName", tabText + "execute query (" + printMethod.get("records") + ")\r\n"
                                            + tabText + printMethod.get("statement"));
                printMethod.put("package", tabText + "○" + printMethod.get("package"));
            } else {
                printMethod.put("package", tabText + "▼" + printMethod.get("package"));
            }

            System.out.print(printMethod.get("no") + "  ");
            System.out.print(printMethod.get("package") + "  ");
            System.out.print(printMethod.get("methodName") + "  ");
            System.out.print(printMethod.get("runTime") + "  \r\n");
        }

        return totalReturns;
    }
}
