package phis.his.nu.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import phis.his.nu.logging.mapper.LoggingNuMapper;
import phis.his.nu.logging.object.Logging;

@Service
public class LoggingNuServiceImpl implements LoggingNuService {
	
	@Autowired
	private LoggingNuMapper loggingNuMapper;
	
	private Logger log = LoggerFactory.getLogger(phis.his.nu.logging.LoggingNuServiceImpl.class);
	
	// 로그에서 쿼리,프로시저 등 DML문을 만들어서 반환 
	private Map<String, String> makeSQL(String queryText, Map<String, String[]> batchQueryParams, int layer) {
		Map<String, String> queryInfo = null;
		StringBuilder bindingQuery = null;
		String[] params = null;
		
		try {
			queryText = queryText.replace("\r", "\\r").replace("\n", "\\n"); // 정규표현식 매칭 시 개행문자 일시 제외
			
			Pattern queryParsePattern = Pattern.compile("([a-zA-Z0-9\\._]+)\\|([a-z]+)\\|([0-9]+) msec\\|param=\\[(.*)\\]\\|([0-9]+ records)\\|(/\\*\\s.+\\.xml [a-zA-Z0-9_]+ \\*/)(.+)\\|.+msec.*");
			Pattern updateParsePattern = Pattern.compile("([a-zA-Z0-9\\._]+)\\|([a-z\\s]+)\\|([0-9]+) msec\\|param=\\[(.+)\\]\\|([([0-9]+ records)|([0-9]+ insert)|([0-9]+ returned),\\s]+)\\|(.+)(/\\* .+\\.xml [a-zA-Z0-9_]+ \\*/) \\|.+msec.*");
			Pattern batchParsePattern = Pattern.compile("([a-zA-Z0-9\\._]+)\\|([a-z]+)\\|([0-9]+) msec\\|([0-9]+ sqls)\\|(.+)(/\\* .+\\.xml [a-zA-Z0-9_]+ \\*/) \\|[0-9]+ msec.*");
			Pattern procedureParsePattern = Pattern.compile("([a-zA-Z0-9\\._]+)\\|([a-z\\.\\s]+)\\|([0-9]+) msec\\|param=(.+),(.*call\\s[a-zA-Z0-9_]+\\.[a-zA-Z0-9_]+\\(.+\\)).*");
			
			// 일반 쿼리(execute query)
			Matcher matcher = queryParsePattern.matcher(queryText);
			if (matcher.matches()) {
				queryInfo = new HashMap<String, String>();
				
				queryInfo.put("packageName", matcher.group(1));
				queryInfo.put("dmlflag", matcher.group(2));
				queryInfo.put("runTime", matcher.group(3));
				queryInfo.put("records", matcher.group(5));
				queryInfo.put("statement", matcher.group(6));
				queryInfo.put("layer", layer + "");
				
				bindingQuery = new StringBuilder(matcher.group(7).replace("\\r", "\r").replace("\\n", "\n")); // 개행문자 복원
				params = "".equals(matcher.group(4)) 
					   ? new String[0]
					   : matcher.group(4).split(", ");
			}
			
			// update (execute update || execute update and return keys)
			matcher = updateParsePattern.matcher(queryText);
			if (matcher.matches()) {
				queryInfo = new HashMap<String, String>();
				queryInfo.put("packageName", matcher.group(1));
				queryInfo.put("dmlflag", matcher.group(2));
				queryInfo.put("runTime", matcher.group(3));
				queryInfo.put("records", matcher.group(5));
				queryInfo.put("statement", matcher.group(7));
				queryInfo.put("layer", layer + "");
				
				bindingQuery = new StringBuilder(matcher.group(6).replace("\\r", "\r").replace("\\n", "\n")); // 개행문자 복원
				params = "".equals(matcher.group(4)) 
						   ? new String[0]
						   : matcher.group(4).split(", ");
			}
			
			// batch (execute batch)
			matcher = batchParsePattern.matcher(queryText);
			if (matcher.matches()) {
				queryInfo = new HashMap<String, String>();
				queryInfo.put("packageName", matcher.group(1));
				queryInfo.put("dmlflag", matcher.group(2));
				queryInfo.put("runTime", matcher.group(3));
				queryInfo.put("records", matcher.group(4));
				queryInfo.put("statement", matcher.group(6));
				queryInfo.put("layer", layer + "");
				
				StringBuilder bindingQueryTemp = new StringBuilder();
				for (int i = 0; i < batchQueryParams.size(); i++) {
					bindingQuery = new StringBuilder(matcher.group(5).replace("\\r", "\r").replace("\\n", "\n")); // 개행문자 복원
    				int bindNumber = bindingQuery.indexOf("?");
    				params = batchQueryParams.get(i + "");

    				// 바인딩 할 개수 일치 시에만
    				if ((bindingQuery.length() - bindingQuery.toString().replace("?", "").length())
    						== params.length) {
    					for (String param : params) {
    						bindingQuery.deleteCharAt(bindNumber);
    						
    						param = "null".equals(param) ? param 
        							: "'" + param + "'";
    						bindingQuery.insert(bindNumber, param);
    						
    						bindNumber = bindingQuery.indexOf("?");
    					}
    				} else {
    					bindingQuery.insert(0, "Parameter 에 \", \" 가 포함되어 있어 바인딩이 취소되었습니다.");
    					bindingQuery.append("\r\n 파라미터    : " + Arrays.toString(params));
    					bindingQuery.append("\r\n 물음표 개수 : " + (bindingQuery.length() - bindingQuery.toString().replace("?", "").length()));
    				}
    				
    				bindingQueryTemp.append("-- " + (i+1) + " 번째 배치 \r\n" + queryInfo.get("statement") + "\r\n" + bindingQuery.toString() + ";\r\n");
				}
				
				queryInfo.put("isQuery", "Y");
				queryInfo.put("query", bindingQueryTemp.toString());
				
				return queryInfo; // 배치는 여기까지만
			}
			
			// 프로시저인 경우
			matcher = procedureParsePattern.matcher(queryText);
			if (matcher.matches()) {
				StringBuilder bindingProcedure = null;
				queryInfo = new HashMap<String, String>();
				
				queryInfo.put("packageName", matcher.group(1));
				queryInfo.put("dmlflag", "call procedure");
				queryInfo.put("runTime", matcher.group(3));
				queryInfo.put("records", "");
				queryInfo.put("layer", layer + "");
				
				Pattern callPattern = Pattern.compile("(.*)call\\s([a-zA-Z0-9_]+\\.[a-zA-Z0-9_]+)(\\(.+\\)).*");
				Matcher subMatcher = callPattern.matcher(matcher.group(5));
				
				if (!subMatcher.matches()) {
					return null;
				} 
				
				// 1. 파라미터에 IN 만 있는경우 call 붙여서 그냥 뿌리기
				// 2. 파라미터에 OUT도 있는 경우 PLSQL 형태로 만들어주기 그리고 call 지우기
				String preText = subMatcher.group(1).replace("\\r", "\r").replace("\\n", "\n");
				String procedureName = subMatcher.group(2).replace("\\r", "\r").replace("\\n", "\n");
				String procedureContext = subMatcher.group(3).replace("\\r", "\r").replace("\\n", "\n");
				
				bindingProcedure = new StringBuilder(procedureContext); // 프로시저 파라미터 작업용 
				String[] parameters = matcher.group(4).split(",");		// 파라미터 목록
				int bindNumber = bindingProcedure.indexOf("?");			// 교체할 위치
				boolean outFlag = false;								// OUT파라미터가 존재하는지 여부
				
				if (parameters.length 
						== bindingProcedure.length() - bindingProcedure.toString().replace("?", "").length()) {
					Map<String, String> outParams = new HashMap<String, String>();
					
					for (int k = 0; k < parameters.length; k++) {
						String param = parameters[k];
						
						matcher = Pattern.compile("(.+):([0-9]+):([0-9]+)(.*)").matcher(param);
						if (matcher.matches()) {
							String paramName = matcher.group(1);
							String inoutFlag = matcher.group(2);
							String dataType  = matcher.group(3);
							String paramContext = matcher.group(4).replace("[", "").replace("]", "");
							String replaceText = "";
							
							if ("2".equals(inoutFlag)) { // OUT
								replaceText = paramName + " /* " + paramName + ":OUT */ "; 
								
								if ("12".equals(dataType)) { // VARCHAR2(4000)
									outParams.put(paramName, paramName + " VARCHAR2(4000);");
								} else if ("4".equals(dataType)) { // INTEGER(38)
									outParams.put(paramName, paramName + " INTEGER(38);");
								}
								
								outFlag = true; // 한 번이라도 OUT이 존재하면 플래그 설정
							} else if ("1".equals(inoutFlag)) { // IN
								paramContext = "null".equals(paramContext) ? paramContext : "'" + paramContext + "'";
								replaceText = paramContext + " /* " + paramName + ":IN */ ";
							}
							
							if (k == parameters.length - 1) {
								replaceText = replaceText.substring(0, replaceText.length() - 1);
							}
							
							bindingProcedure.deleteCharAt(bindNumber);
							bindingProcedure.insert(bindNumber, replaceText);
							
							bindNumber = bindingProcedure.indexOf("?");
						}
					}
					
					// OUT파라미터가 존재할 때 
					if (outFlag) {
						StringBuilder procedureTemp = new StringBuilder();
						
						procedureTemp.append("DECLARE \r\n");
						
						Iterator<String> outParamKeys = outParams.keySet().iterator();
						while (outParamKeys.hasNext()) {
							String outParamName = outParamKeys.next();
							String outParamDeclare = outParams.get(outParamName);
							
							procedureTemp.append("    " + outParamDeclare + "\r\n");
						}
						
						procedureTemp.append("    BEGIN " + preText + procedureName + bindingProcedure + ";\r\n");
						
						outParamKeys = outParams.keySet().iterator();
						while (outParamKeys.hasNext()) {
							String outParamName = outParamKeys.next();
							
							procedureTemp.append("DBMS_OUTPUT.PUT_LINE(" + "'" + outParamName + " : ' || " + outParamName + ");\r\n");
						}
						procedureTemp.append("END;");
						
						bindingProcedure = procedureTemp;
					} else {	// IN만 존재 시 
						bindingProcedure.insert(0, preText + "call " + procedureName);
					}
				} else {
					bindingProcedure.insert(0,  "Parameter 에 \", \" 가 포함되어 있어 바인딩이 취소되었습니다.");
				}
				
				queryInfo.put("statement", procedureName);
				queryInfo.put("isQuery", "Y");
				queryInfo.put("query", bindingProcedure.toString());
				
				return queryInfo;
			}
			
			// 쿼리 내용 없는경우 그냥 종료
			if (bindingQuery == null) {
				log.debug(queryText.replace("\r", "\\r").replace("\n", "\\n"));
				return null;
			}
    		
			// 바인딩 할 개수 일치 시에만
			int bindNumber = bindingQuery.indexOf("?");
			if (((bindingQuery.length() - bindingQuery.toString().replace("?", "").length())
					== params.length) || params.length == 0) {
				for (String param : params) {
					bindingQuery.deleteCharAt(bindNumber);
					
					param = "null".equals(param) ? param 
							: "'" + param + "'";
					bindingQuery.insert(bindNumber, param);
					
					bindNumber = bindingQuery.indexOf("?");
				}
			} else {
				bindingQuery.insert(0, "Parameter 에 \", \" 가 포함되어 있어 바인딩이 취소되었습니다.");
				bindingQuery.append("\r\n 파라미터    : " + Arrays.toString(params));
				bindingQuery.append("\r\n 물음표 개수 : " + (bindingQuery.length() - bindingQuery.toString().replace("?", "").length()));
			}
			
			queryInfo.put("isQuery", "Y");
			queryInfo.put("query", queryInfo.get("statement") + "\r\n" + bindingQuery.toString());
		} catch (Exception e) {
			log.debug(e.getMessage());
			
			return null;
		}
		
		return queryInfo;
	}
	
	@Override
	public List<Map<String, String>> parseLog(String text) throws Exception {
        String[] allLines = text.split("\n");
        List<Map<String, String>> totalReturns = new ArrayList<>();
        
        Map<String, String> queryInfo = null;
        Map<String, String[]> batchQueryParams = new HashMap<>();    // 미리생성해 둬야함
        Map<String, String> errorInfo = null; // 에러 발생시 사용 변수
        StringBuilder queryString = null;
        StringBuilder errorString = null;

        int layer = 0;
        boolean queryStartFlag = false;
        boolean errorFlag = false;
        
        // 패턴나열 
        Pattern basicPattern = Pattern.compile("([0-9]{1,9})\\[([a-zA-Z0-9=\\.:\\s]+)\\]\\[([0-9\\.:\\s]+)\\]\\s\\[([a-zA-Z0-9=\\.:\\s]+)\\]\\s\\[([a-zA-Z0-9=\\.:\\s_]+)\\]\\s(.+)");
        Pattern methodStartPattern = Pattern.compile("^(.+\\(\\))\\sstarts.$");
        Pattern methodEndPattern = Pattern.compile("^(.+\\(\\))\\sends.\\(([0-9]+\\smsecs)\\}");
        Pattern queryStartPattern = Pattern.compile("^execute\\s(query|update and return keys|batch|update|call succeeded.)\\s(.+)");
        Pattern queryBatchStartPattern = Pattern.compile("^execute\\s(batch)\\sparam\\[([0-9]+)\\]=\\[(.*)\\]");
        
        // 전체 로그 1개 line씩 for문 시작 
        for (int i = 0; i < allLines.length; i++) {
            String line = allLines[i];
            Matcher matcher = basicPattern.matcher(line);
            Matcher subMatcher = null;
            
            // 쿼리 저장 시작 
            if (queryStartFlag) {
            	queryString.append(line + "\r\n");
            	
            	// 다음행이 일반 로그 정보일 때
            	matcher = basicPattern.matcher(allLines[i + 1]);
            	if (matcher.matches()) {
            		queryInfo = null;
            		
            		// 쿼리로그 전체, 배치인경우 파라미터, 계층
            		queryInfo = makeSQL(queryString.toString(), batchQueryParams, layer);
            		
            		if (queryInfo == null) { 
            			throw new Exception();
            		}
                    
            		totalReturns.add(queryInfo);
            		
            		batchQueryParams = new HashMap<>();
            		queryString = null;
            		queryStartFlag = false;
            		layer--;
            		
            		continue;
            	}
            	
            	continue;
            } 
            
            //// 에러처리 /////
            if (errorFlag) {
            	errorString.append(line + "\r\n");
            	
            	// 마지막행 이거나 다음 행이 일반 로그일 경우 에러수집 정지
            	matcher = basicPattern.matcher(allLines[i + 1]);
            	if ((i == allLines.length - 1) 
            			|| matcher.matches()) {
            		errorInfo.put("methodName", errorString.toString());
            		totalReturns.add(errorInfo);
            		errorFlag = false;
            	}
            }
            
            //// 일반 로그 파싱 및 판별 영역 ////
            // 일반 로그 시작 
            if (matcher.matches()) {
            	String transactionId = matcher.group(1);
            	String nodeInfo = matcher.group(2);
            	String timeInfo = matcher.group(3);
            	String logState = matcher.group(4);
            	String packageName = matcher.group(5);
            	String logContext = matcher.group(6);
            	
            	// 메소드 시작
            	matcher = methodStartPattern.matcher(logContext);
            	if (matcher.matches()) {
            		String methodName = matcher.group(1);
            		
            		if ("getConnection()".equals(methodName.trim())
                            || "toString()".equals(methodName.trim())) continue;
            		
            		HashMap<String, String> methodInfo = new HashMap<>();
                    methodInfo.put("methodName", methodName);
                    methodInfo.put("packageName", packageName);
                    methodInfo.put("layer", layer + "");
                    methodInfo.put("isQuery", "N");

                    if (i == 0) {
                        methodInfo.put("startTime", timeInfo);
                    }
                    
                    totalReturns.add(methodInfo);
            		
            		layer++;
            	}
            	
            	// 메소드 종료
            	matcher = methodEndPattern.matcher(logContext);
            	if (matcher.matches()) {
            		String methodName = matcher.group(1); 
            		String runTime = matcher.group(2);
            		
            		if ("getConnection()".equals(methodName.trim())
                            || "toString()".equals(methodName.trim())) {
                        continue;
                    }  // 커넥션 보기 싫어서 페스

                    // 시작 정보에 종료 정보 중 수행 시간만 삽입
                    for (int j = totalReturns.size() - 1; j >= 0; j--) {
                        Map<String, String> element = totalReturns.get(j);

                        if ("N".equals(element.get("isQuery")) && element.get("methodName").equals(methodName)
                                        && element.get("layer").equals((layer - 1) + "")) {
                            element.put("runTime", runTime.replace("msecs", "").trim());
                            element.put("endTime", timeInfo);
                            break;
                        }
                    }

                    layer--;
            	}
            	
            	// 쿼리 시작
            	subMatcher = queryBatchStartPattern.matcher(logContext);	// 배치 쿼리인지
            	matcher = queryStartPattern.matcher(logContext);			// 그외 쿼리인지 
            	
            	// 쿼리면서, 배치쿼리 아닌 것
            	if (matcher.matches() && !subMatcher.matches()  // 일반쿼리, 업데이트 쿼리 정보 입력, 
            			&& !logContext.matches(".*ORA-[0-9]+.*")) { // SQL 오류 아닐 때 
            		String dmlFlag = matcher.group(1);			// query, update, batch, call .. etc
            		String queryStartText = matcher.group(2);   // parameters info, records, etc.. 
            		
            		queryString = new StringBuilder();
            		queryString.append(packageName + "|" 
            						   + dmlFlag + "" + queryStartText + "\r\n");
            		
            		queryStartFlag = true;
            		
            		layer++;
            	} else if (subMatcher.matches()) {	// 배치쿼리 시작 및 파라미터 정보 입력 
            		String paramCount = subMatcher.group(2);
            		String[] parameters = subMatcher.group(3).split("(,\\s)");
            		
            		batchQueryParams.put(paramCount, parameters);
            	}
            	
            	// 에러 발생 정보 
            	if ("ERROR".equals(logState.replace(" ", ""))) {
                    errorInfo = new HashMap<>();
                    errorInfo.put("methodName", logContext);
                    errorInfo.put("packageName", packageName);
                    errorInfo.put("layer", layer + "");
                    errorInfo.put("isQuery", "E");  // 에러

                    errorString = new StringBuilder();
                    errorString.append(line);
                    
                    errorFlag = true;
                    
                    layer++;
            	}
            } // 일반 로그 if문 종료 
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
                printMethod.put("methodName", "execute " + printMethod.get("dmlflag") + " (" + printMethod.get("records") + ")\r\n"
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
	
	public void insertSubmitHistory(Logging logging) {
		loggingNuMapper.insertSubmitHistory(logging);
	}
	
	public void insertDetailLogHistory(Logging logging) {
		loggingNuMapper.insertDetailLogHistory(logging);
	}
	
	public void insertErrorHistory(Logging logging) {
		loggingNuMapper.insertErrorHistory(logging);
	}
}
