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
	public String qualifier = null;
	public Object params[] = null;
	
		
	public boolean flagVerboseResult = false;	
	public List<String> analysisResult = new ArrayList<String>();
	
	/*
	 * Parsing a SubstanceAnalysisTask from a string in the following format
	 * <task type>; <qualifier>; <params>; <Logical conditions>; <special keywords>
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
				if (SALogicalCondition.checkQualifier (saTask.qualifier) == -1)
					errors.add("Incorrect qualifier: " + saTask.qualifier);
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
		
		//Logical conditions
		if (tokens.length < 4)
			errors.add("Missing logical conditions token!");
		else {	
			String lcStr = tokens[3].trim();
			if (!lcStr.isEmpty()  && !lcStr.equals("--") && !lcStr.equals("no conditions"))
			{						
				String lcTokens[] = lcStr.split(",");	
				for (int i = 0; i < lcTokens.length; i++) 
				{
					try {
						SALogicalCondition logCond = SALogicalCondition.parseFromString(lcTokens[i]);
						saTask.logicalConditions.add(logCond);
					}
					catch (Exception x) {
						errors.add("Errors on logical conditions #" + (i+1) + " : " + x.getMessage());
					}
				}
			}			
		}
		
		//TODO special keywords parsing		
				
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
		analysisResult.clear();
		for (int i = 0; i<records.size(); i++)
		{
			if (flagVerboseResult)
				analysisResult.add("Record " + (i+1));
			
			switch (type) {
			case COUNT_EFFECTS:
				taskCountEffects(records.get(i), i);
				break;
			}
		}
		
		return 0;
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
