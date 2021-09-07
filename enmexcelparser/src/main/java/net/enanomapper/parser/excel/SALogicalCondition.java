package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.List;

public class SALogicalCondition 
{
	public static enum LogicalConditionType {
		VALUE, UNDEFINED;
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
	
	public static final String ALLOWED_QUALIFIERS[] = {
		"=", "<", "<=", ">", ">=", "in", "interval", "is_null"	
	};
	
	public String targetLabel = null;
	public LogicalConditionType conditionType = LogicalConditionType.UNDEFINED;
	public TargetType targetType = TargetType.UNDEFINED;
	public String qualifier = null;
	public Object params[] = null;
	
	
	/*
	 * Parsing a SALogicalCondition from a string in the following format
	 * <target type> <target label> <log.cond.type> <qualifier> <params> <special keywords>
	 */
	public static SALogicalCondition parseFromString(String taskStr) throws Exception 
	{
		List<String> errors = new ArrayList<String>();
		SALogicalCondition saLogCond = new SALogicalCondition();
		
		String workStr = taskStr.trim();
		if (workStr.isEmpty())
			throw new Exception ("Empty input string!");
						
		//Token trimming is not needed. Empty tokens are allowed and skipped
		//Get all non-empty tokens
		String initialTokens[] = workStr.split(" ");
		List<String> tokens = new ArrayList<String>();
		for (int i = 0; i  <initialTokens.length; i++) {
			if (!initialTokens[i].isEmpty())
				tokens.add(initialTokens[i]);		}
		
		//Target type
		//it is the first token and is guaranteed because of the check for empty input string
		saLogCond.targetType = TargetType.fromString(tokens.get(0));
		if (saLogCond.targetType == TargetType.UNDEFINED)
			errors.add("Incorrect logical condition target type: " + tokens.get(0));
		
		//Label 
		if (tokens.size() < 2)
			errors.add("Missing qualifier token!");
		else {
			if (!tokens.get(1).equalsIgnoreCase("NO_LABEL"))
				saLogCond.targetLabel = tokens.get(1);
		}
		
		//SA Task type 
		if (tokens.size() < 3)
			errors.add("Missing logical condition type!");
		else {
			saLogCond.conditionType = LogicalConditionType.fromString(tokens.get(2));
			if (saLogCond.conditionType == LogicalConditionType.UNDEFINED)
				errors.add("Incorrect logical condition type: " + tokens.get(2));
		}
		
		if (tokens.size() < 4)
			errors.add("Missing qualifier token!");
		else {	
			saLogCond.qualifier = tokens.get(3);
			if (checkQualifier (saLogCond.qualifier) == -1)
				errors.add("Incorrect qualifier: " + tokens.get(3));
		}
		
		//Handle params and special keywords
		if (tokens.size() >= 5)
		{
			List<Object> paramObjects = new ArrayList<Object>();
			for (int i = 4; i < tokens.size(); i++)
			{	
				String par = tokens.get(i);
				//TODO check for special keyword token
				
				Object o = par;
				try {
					Double d = Double.parseDouble(par);
					o = d;
				}
				catch (Exception x) {
				}
				
				paramObjects.add(o);
			}
			
			if (!paramObjects.isEmpty())
				saLogCond.params = paramObjects.toArray();
		}		
				
		if (!errors.isEmpty()) {
			StringBuffer errBuffer = new StringBuffer();
			for (String err: errors)
				errBuffer.append(err + "\n");
			throw new Exception (errBuffer.toString());
		}		
		
		return saLogCond;
	}
	
	public static int checkQualifier(String qual) {
		for (int i = 0; i< ALLOWED_QUALIFIERS.length; i++)
			if (qual.equals(ALLOWED_QUALIFIERS[i]))
				return i;
		return -1;
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
		
		sb.append(targetType + " ");
		
		if (targetLabel == null)
			sb.append("NO_LABEL");
		else
			sb.append(targetLabel + " ");
		
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
