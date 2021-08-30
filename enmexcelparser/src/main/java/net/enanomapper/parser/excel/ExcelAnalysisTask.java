package net.enanomapper.parser.excel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;

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
	public ExcelScope excelScope = null;
	public Object params[] = null;
	public File target1 = null;
	public File target2 = null;
	public boolean flagVerboseResult = false;
		
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
			try {
				eaTask.excelScope = ExcelScope.parseFromString(scopeStr);
			}
			catch (Exception x) {
				errors.add("Incorrect excel scope: " + x.getMessage());
			}
		}
		
		//Excel params
		if (tokens.length < 3)
			errors.add("Missing parameters token!");
		else {	
			String paramsStr = tokens[2].trim();
			
			if (!paramsStr.equals("--") && !paramsStr.equals("no params"))
			{						
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
			String tok4 = tokens[4].trim();
			int specTokRes = checkForSpecialToken(tok4, eaTask,  errors);
			if (specTokRes == -1)
			{	
				//Token 4 is not a special key word token
				eaTask.target2 = new File(tok4);
				if (!eaTask.target2.exists())
					errors.add("Target2 file path: " + tok4 + " does not exists!");
			}
		}

		
		if (!errors.isEmpty()) {
			StringBuffer errBuffer = new StringBuffer();
			for (String err: errors)
				errBuffer.append(err + "\n");
			throw new Exception (errBuffer.toString());
		}		
		
		return eaTask;
	}
	
	public static int checkForSpecialToken(String token, ExcelAnalysisTask eaTask, List<String> errors) 
	{
		if (token.equalsIgnoreCase("verbose"))
		{	
			eaTask.flagVerboseResult = true;
			return 0;
		}
		
		return -1;
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
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Type: " + type + "\n");
		sb.append("Verbose: " + flagVerboseResult + "\n");
		sb.append("Excel Scope: ");
		for (CellRangeAddress cra: excelScope.cellRanges)
			sb.append(cra.formatAsString() + " ");
		sb.append("\n");
		
		if (params != null) {
			sb.append("Params: ");
			for (int i = 0; i < params.length; i++)
				sb.append(params[i] + " ");
			sb.append("\n");
		}		
		
		if (target1 != null)
			sb.append("Target1 :" + target1.getAbsolutePath() + "\n ");
		if (target2 != null)
			sb.append("Target2 :" + target2.getAbsolutePath() + "\n ");
		
		return sb.toString();
	}
}
