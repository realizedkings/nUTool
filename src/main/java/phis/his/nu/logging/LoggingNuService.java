package phis.his.nu.logging;

import java.util.List;
import java.util.Map;

import phis.his.nu.logging.object.Logging;

public interface LoggingNuService {
    // 문자열 파싱
    List<Map<String, String>> parseLog(String log) throws Exception;
    
	void insertSubmitHistory(Logging logging);
	
	void insertDetailLogHistory(Logging logging);
	
	void insertErrorHistory(Logging logging);
}
