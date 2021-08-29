package net.enanomapper.parser.excel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExcelAnalysisTask 
{
	public static final String mainSplitter = ";";
	public static final String secondarySplitter = ",";
	
	
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
	 * <task type>; <scope>; <params>; <target1>; <target2>
	 */
	public static ExcelAnalysisTask parseFromString(String taskStr) throws Exception 
	{
		List<String> errors = new ArrayList<String>();
		ExcelAnalysisTask eaTask = new ExcelAnalysisTask();		
		String tokens[] = taskStr.split(mainSplitter);
		
		//Task type
		if (tokens.length < 1)
			errors.add("Missing task type token!");
		else {	
			eaTask.type = TaskType.fromString(tokens[0].trim());
			if (eaTask.type == TaskType.UNDEFINED)
				errors.add("Incorrect excel analysis task type: " + tokens[0]);
		}
		
		//Excel Scope
		if (tokens.length < 2)
			errors.add("Missing excel scope token!");
		else {	
			String scopeStr = tokens[1].trim();
			//TODO
		}
		
		//Excel params
		if (tokens.length < 3)
			errors.add("Missing parameters token!");
		else {	
			String paramsStr = tokens[2].trim();
			String paramTokens[] = paramsStr.split(secondarySplitter);
			
			if (paramTokens.length > 0) 
			{
				eaTask.params = new Object[paramTokens.length];

				for (int i = 0; i < paramTokens.length; i++) {
					String par = paramTokens[i].trim();
					if (par.isEmpty())
						errors.add("Parameter #" + (i+1) + " is empty!");

					Object o = par;
					try {
						Double d = Double.parseDouble(par);
						o = d;
					}
					catch (Exception x) {
					}
					eaTask.params[i] = o;
				}
			}
		}
		
		//Target1 (file path)		
		if (tokens.length < 4)
			errors.add("Missing target 1 token!");
		else {	
			String path = tokens[3].trim();
			eaTask.target1 = new File(path);
			if (!eaTask.target1.exists())
				errors.add("Target1 file path: " + path + " does not exists!");
		}
		
		//Target2 (file path)		
		if (tokens.length >=5 )
		{	
			String path = tokens[4].trim();
			eaTask.target2 = new File(path);
			if (!eaTask.target2.exists())
				errors.add("Target2 file path: " + path + " does not exists!");
		}

		
		if (!errors.isEmpty()) {
			StringBuffer errBuffer = new StringBuffer();
			for (String err: errors)
				errBuffer.append(err + "\n");
			throw new Exception (errBuffer.toString());
		}		
		
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
