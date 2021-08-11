package net.enanomapper.parser.excel;

import java.io.File;

public class ExcelAnalysisTask 
{
	public static enum TaskType {
		COMPARE, COUNT, UNDEFINED
	}
	
	public TaskType type = TaskType.UNDEFINED;
	public Object params[] = null;
	public File targetFile1 = null;
	public File targetFile21 = null;	
	
}
