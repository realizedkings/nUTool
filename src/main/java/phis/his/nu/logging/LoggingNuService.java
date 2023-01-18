package phis.his.nu.logging;

import java.util.List;
import java.util.Map;

public interface LoggingNuService {
    // 문자열 파싱
    List<Map<String, String>> parseLog(String log);
}
