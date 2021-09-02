package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.List;

public class SALogicalCondition 
{
	public static enum LogicalConditionType {
		VALUE, NULL, UNDEFINED;
		public static LogicalConditionType fromString(String s) {
			try {
				LogicalConditionType type = LogicalConditionType.valueOf(s);
				return (type);
			} catch (Exception e) {
				return LogicalConditionType.UNDEFINED;
			}
		}		
	}
	
	public static enum TargetType {
		SUBSTANCE, PROTOCOL_PARAMETER, EFFECT, CONDITION, UNDEFINED;
		public static TargetType fromString(String s) {
			try {
				TargetType type = TargetType.valueOf(s);
				return (type);
			} catch (Exception e) {
				return TargetType.UNDEFINED;
			}
		}		
	}
	
	public LogicalConditionType conditionType = LogicalConditionType.UNDEFINED;
	public TargetType targetType = TargetType.UNDEFINED;
	public String qualifier = null;
	public Object params[] = null;
	
	
	/*
	 * Parsing a SALogicalCondition from a string in the following format
	 * <log.cond. type> <qualifier> <params> <special keywords>
	 */
	public static SALogicalCondition parseFromString(String taskStr) throws Exception 
	{
		List<String> errors = new ArrayList<String>();
		SALogicalCondition saLogCond = new SALogicalCondition();		
		String tokens[] = taskStr.split(";");
				
		//SA Task type
		if (tokens.length < 1)
			errors.add("Missing task type token!");
		else {	
			saLogCond.conditionType = LogicalConditionType.fromString(tokens[0].trim());
			if (saLogCond.conditionType == LogicalConditionType.UNDEFINED)
				errors.add("Incorrect substance analysis task type: " + tokens[0]);
		}
		
		//TODO

		
		if (!errors.isEmpty()) {
			StringBuffer errBuffer = new StringBuffer();
			for (String err: errors)
				errBuffer.append(err + "\n");
			throw new Exception (errBuffer.toString());
		}		
		
		return saLogCond;
	}
	
	
	public boolean apply(Object target) {
		//TODO
		return false;
	}
	
	public boolean applyForSubstance() {
		//TODO
		return false;
	}
	
	public String toStringVerbose() {
		StringBuffer sb = new StringBuffer();
		sb.append("ConditionType: " + conditionType + "\n");
		
		if (qualifier != null) 
			sb.append("Qualifier: " + qualifier + "\n");
				
		if (params != null) {
			sb.append("Params: ");
			for (int i = 0; i < params.length; i++)
				sb.append(params[i] + " ");
			sb.append("\n");
		}
		
		sb.append("TargetType: " + targetType + "\n");		
		return sb.toString();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(conditionType + " ");
		
		if (qualifier != null) 
			sb.append(qualifier + "  ");
				
		if (params != null) {
			for (int i = 0; i < params.length; i++)
				sb.append(params[i] + " ");			
		}		
		return sb.toString();
	}
}
