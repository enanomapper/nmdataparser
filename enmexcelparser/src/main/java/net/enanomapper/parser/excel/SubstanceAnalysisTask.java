package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.List;

import ambit2.base.data.SubstanceRecord;
import net.enanomapper.parser.excel.SALogicalCondition.ComparisonOperation;


public class SubstanceAnalysisTask 
{
	public static enum SATaskType {
		CHECK_EFFECT_VALUE, CHECK_PROTOCOL_PARAMETER_VALUE, CHECK_CONDITION_VALUE, 
		COUNT_RECORDS, COUNT_EFFECTS, COUNT_PROTOCOL_PARAMETERS, COUNT_CONDITIONS, UNDEFINED;
		
		public static SATaskType fromString(String s) {
			try {
				SATaskType type = SATaskType.valueOf(s);
				return (type);
			} catch (Exception e) {
				return SATaskType.UNDEFINED;
			}
		}		
	}
	
	public static final String SATaskSyntaxFormat = 
				"<task type>; <qualifier>; <params>; <Logical condition 1>; ...; <special keyword 1>...";
	
	public SATaskType type = SATaskType.UNDEFINED;
	List<SALogicalCondition> logicalConditions = new ArrayList<SALogicalCondition>();
	public String qualifier = null;
	public ComparisonOperation comparison = ComparisonOperation.UNDEFINED; //determined from the qualifier
	public Object params[] = null;
			
	public boolean flagVerboseResult = false;	
	public boolean flagConsoleOut = false;
	public boolean flagConsoleOutOnly = false;
	public boolean flagIgnoreCase = true;
	public List<String> analysisResult = new ArrayList<String>();
	public List<String> analysisErrors = new ArrayList<String>();
	public List<String> analysisWarnings = new ArrayList<String>();	
	public int analysisStatTotalOKNum = 0;
	public int analysisStatTotalProblemNum = 0;
	
	
	/*
	 * Parsing a SubstanceAnalysisTask from a string in the following format
	 * <task type>; <qualifier>; <params>; <Logical conditions>; <special keywords> ...
	 * 
	 * Qualifier and params checks are applied on the task level object.
	 * If applicable, Logical conditions checks are applied on lower level objects i.e. 
	 * elements (sub-objects) of the main task object
	 */
	public static SubstanceAnalysisTask parseFromString(String taskStr) throws Exception 
	{
		List<String> errors = new ArrayList<String>();
		SubstanceAnalysisTask saTask = new SubstanceAnalysisTask();		
		String tokens[] = taskStr.split(";");
				
		//SA Task type
		if (tokens.length < 1)
			errors.add("Missing task type token!");
		else {	
			saTask.type = SATaskType.fromString(tokens[0].trim());
			if (saTask.type == SATaskType.UNDEFINED)
				errors.add("Incorrect substance analysis task type: " + tokens[0]);
		}
		
		//Quilifier
		if (tokens.length < 2)
			errors.add("Missing qualifier token!");
		else {	
			saTask.qualifier = tokens[1].trim();
			if (!saTask.qualifier.isEmpty()) 
			{	
				saTask.comparison = SALogicalCondition.qualifierToComparisonOperation(saTask.qualifier);
				if (saTask.comparison == ComparisonOperation.UNDEFINED)
					errors.add("Incorrect qualifier: " + saTask.qualifier);
			}	
		}
		
		//Params
		if (tokens.length < 3)
			errors.add("Missing parameters token!");
		else {	
			String paramsStr = tokens[2].trim();

			if (!paramsStr.isEmpty() && !paramsStr.equals("--") && !paramsStr.equals("no params"))
			{						
				String paramTokens[] = paramsStr.split(",");			

				if (paramTokens.length > 0) 
				{
					saTask.params = new Object[paramTokens.length];

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
						saTask.params[i] = o;
					}
				}
			}
		}		
		
		//Logical conditions + special keywords parsing
		if (tokens.length >= 4)
			for (int i = 3; i < tokens.length; i++)	
			{	
				String lcStr = tokens[i].trim();				
				if (lcStr.isEmpty())
					continue;
				int specTokRes = checkForSpecialToken(lcStr, saTask,  errors);
				if (specTokRes == 0)
					continue; //The token is either a special keyword or a comment 

				try {
					SALogicalCondition logCond = SALogicalCondition.parseFromString(lcStr);
					saTask.logicalConditions.add(logCond);
				}
				catch (Exception x) {
					errors.add("Errors on logical conditions #" + (i-3) + " : " + x.getMessage());
				}
						
			}
		
						
		if (!errors.isEmpty()) {
			StringBuffer errBuffer = new StringBuffer();
			for (String err: errors)
				errBuffer.append(err + "\n");
			throw new Exception (errBuffer.toString());
		}		
		
		return saTask;
	}
	
	public static int checkForSpecialToken(String token, SubstanceAnalysisTask saTask, List<String> errors) 
	{
		if (token.startsWith("#"))
		{
			//This token is omitted ('#' symbol makes it a comment token)
			return 0;
		}
		
		if (token.equalsIgnoreCase("verbose"))
		{	
			saTask.flagVerboseResult = true;
			return 0;
		}		
		
		if (token.equalsIgnoreCase("console_out"))
		{	
			saTask.flagConsoleOut = true;
			return 0;
		}
		
		if (token.equalsIgnoreCase("console_out_only"))
		{	
			saTask.flagConsoleOutOnly = true;
			return 0;
		}
		
		return -1;
	}
	
	void outputLine(String line) {
		if (flagConsoleOutOnly)
			System.out.println(line);
		else
		{
			analysisResult.add(line);
			if (flagConsoleOut)
				System.out.println(line);
		}
	}
	
	public int run(List<SubstanceRecord> records) 
	{
		analysisResult.clear();
		analysisErrors.clear();
		analysisWarnings.clear();
		analysisStatTotalOKNum = 0;
		analysisStatTotalProblemNum = 0;
		
		if (records == null)
			return -1;
		
		for (int i = 0; i<records.size(); i++)
		{
			if (flagVerboseResult)
				outputLine("Record " + (i+1));
			
			switch (type) {
			case COUNT_EFFECTS:
				taskCountEffects(records.get(i), i);
				break;
			}
		}
		
		makeResultSummary();
		return 0;
	}
	
	public void makeResultSummary() 
	{
		String okLabel = "OK";
		String problemLabel = "Problem";
		
		
		switch (type) {
		case COUNT_EFFECTS:
			okLabel = "Effects OK: ";
			problemLabel = "Problematic effects: ";
			break;
		}
		
		outputLine(okLabel + analysisStatTotalOKNum);
		outputLine(problemLabel + analysisStatTotalProblemNum);
		
	}
	
	public void taskCountRecord(List<SubstanceRecord> records)
	{
		for (int i = 0; i<records.size(); i++)
		{
			if (flagVerboseResult)
				outputLine("Record " + (i+1));
			
			analysisStatTotalOKNum++;
			
			//TODO use logical conditions
		}	
	}
	
	public void taskCountEffects(SubstanceRecord record, int recordNum)
	{
		//TODO
	}
	
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SA Type: " + type + "\n");
				
		if (qualifier != null) 
			sb.append("Qualifier: " + qualifier + "\n");
				
		if (params != null) {
			sb.append("Params: ");
			for (int i = 0; i < params.length; i++)
				sb.append(params[i] + " ");
			sb.append("\n");
		}
		
		if (!logicalConditions.isEmpty()) {
			sb.append("Logical Conditions: \n");
			for (int i = 0; i < logicalConditions.size(); i++) {
				SALogicalCondition lc = logicalConditions.get(i);
				sb.append("  " + lc.toString());
				sb.append("\n");
			}			
		}
		
		sb.append("Verbose: " + flagVerboseResult + "\n");
				
		return sb.toString();
	}
}
