package phis.his.nu.logging;

import java.util.List;
import java.util.Map;

public interface LoggingNuService {


    // 문자열 제공하는 메소드 (모델 객체 반환)
    String getLogging(String log);
    
    // 문자열 파싱하여 분류 나누기 / 계층나누기
    List<Map<String, String>> parseLog(String log);

    // 쿼리 만드는 메소드
    Map<String, String> makeQuery(String log);
}
