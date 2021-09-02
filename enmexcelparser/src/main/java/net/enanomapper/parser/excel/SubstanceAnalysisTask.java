package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.List;

import ambit2.base.data.SubstanceRecord;


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
	
	
	public SATaskType type = SATaskType.UNDEFINED;
	List<SALogicalCondition> logicalConditions = new ArrayList<SALogicalCondition>();
		
	public boolean flagVerboseResult = false;	
	public List<String> analysisResult = new ArrayList<String>();
	
	/*
	 * Parsing a SubstanceAnalysisTask from a string in the following format
	 * <task type>; <Logical conditions>; <qualifier>; <params>; <special keywords>
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
		
		//TODO

		
		if (!errors.isEmpty()) {
			StringBuffer errBuffer = new StringBuffer();
			for (String err: errors)
				errBuffer.append(err + "\n");
			throw new Exception (errBuffer.toString());
		}		
		
		return saTask;
	}
	
	public int run(List<SubstanceRecord> records) 
	{
		//TODO
		return 0;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SA Type: " + type + "\n");
		
		if (!logicalConditions.isEmpty()) {
			sb.append("logicalConditions: " + "\n");
			for (SALogicalCondition lc: logicalConditions)
				sb.append(lc.toString()  + "\n");
		}
		
		sb.append("Verbose: " + flagVerboseResult + "\n");
				
		return sb.toString();
	}
}
