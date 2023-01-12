package phis.his.nu.logging;

import java.util.List;
import java.util.Map;

public interface LoggingNuService {
    // 문자열 파싱하여 분류 나누기 / 계층나누기
    List<Map<String, String>> parseLog(String log);
}
