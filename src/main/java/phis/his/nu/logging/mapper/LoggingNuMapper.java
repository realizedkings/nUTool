package phis.his.nu.logging.mapper;

import org.apache.ibatis.annotations.Mapper;

import phis.his.nu.logging.object.Logging;
import phis.his.nu.logging.object.TestObject;

@Mapper

public interface LoggingNuMapper {
	TestObject getTest();
	
	void insertSubmitHistory(Logging logging);
	
	void insertDetailLogHistory(Logging logging);
	
	void insertErrorHistory(Logging logging);
}
