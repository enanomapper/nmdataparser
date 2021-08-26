package net.enanomapper.parser.excel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExcelAnalysisTask 
{
	public static enum TaskType {
		COMPARE_FILES, CHECK_VALUE, COUNT, UNDEFINED;
		
		public static TaskType fromString(String s) {
			try {
				TaskType type = TaskType.valueOf(s);
				return (type);
			} catch (Exception e) {
				return TaskType.UNDEFINED;
			}
		}		
	}
	
	public TaskType type = TaskType.UNDEFINED;
	public Object params[] = null;
	public File target1 = null;
	public File target2 = null;	
	
	
	/*
	 * Parsing an ExcelAnalysisTask from a string in the following format
	 * <task type>; <scope>; <params>; <target1>; <target>
	 */
	public static ExcelAnalysisTask parseFromString() throws Exception 
	{
		List<String> errors = new ArrayList<String>();
		ExcelAnalysisTask eaTask = new ExcelAnalysisTask(); 
		
		//TODO
		
		if (!errors.isEmpty()) {
			StringBuffer errBuffer = new StringBuffer();
			for (String err: errors)
				errBuffer.append(err + "\n");
			throw new Exception (errBuffer.toString());
		}
		else
			return eaTask;
	}
	
	public List<String> run() 
	{
		switch (type) {
		case COMPARE_FILES:
			return compareFiles();
		case CHECK_VALUE:
			return checkValue();
			
			//TODO
		}
		return null;
	}
	
	List<String> compareFiles() {
		List<String> result = new ArrayList<String>();
		//TODO
		return result;
	}
	
	List<String> checkValue() {
		List<String> result = new ArrayList<String>();
		//TODO
		return result;
	}
	
}
