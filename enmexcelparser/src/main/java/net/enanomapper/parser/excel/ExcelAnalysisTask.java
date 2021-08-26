package net.enanomapper.parser.excel;

import java.io.File;

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
	public File targetFile1 = null;
	public File targetFile2 = null;	
	
}
