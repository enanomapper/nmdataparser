package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.List;

import net.enanomapper.parser.excel.SALogicalCondition.ComparisonOperation;

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
		"=", "<", "<=", ">", ">=", "!=", "in_set", "interval", "is_empty", "not_empty"	
	};
	
	public static enum ComparisonOperation {
		EQUAL, LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL, NOT_EQUAL, 
		IN_SET, INTERVAL, IS_EMPTY, NOT_EMPTY, UNDEFINED 
	}
	
	public String targetLabel = null;
	public LogicalConditionType conditionType = null;
	public TargetType targetType = TargetType.UNDEFINED;
	public String qualifier = null;
	public ComparisonOperation comparison; //determined from the qualifier
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
			
		String tokens[] = workStr.split(",");
		
		//Target type
		//it is the first token and is guaranteed because of the check for empty input string
		String tok0 = tokens[0].trim();
		saLogCond.targetType = TargetType.fromString(tok0);
		if (saLogCond.targetType == TargetType.UNDEFINED)
			errors.add("Incorrect logical condition target type: " + tok0);
		
		//Label 
		if (tokens.length >= 2) {
			String tok = tokens[1].trim();
			if (!tok.equalsIgnoreCase("NO_LABEL"))
				saLogCond.targetLabel = tok;
		}
		
		//SA Task type 
		if (tokens.length >= 3) {
			String tok = tokens[2].trim();
			saLogCond.conditionType = LogicalConditionType.fromString(tok);
			if (saLogCond.conditionType == LogicalConditionType.UNDEFINED)
				errors.add("Incorrect logical condition type: " + tok);
		}
		
		if (tokens.length >= 4) {	
			saLogCond.qualifier = tokens[3].trim();			
			saLogCond.comparison = SALogicalCondition.qualifierToComparisonOperation(saLogCond.qualifier);
			if (saLogCond.comparison == ComparisonOperation.UNDEFINED)
				errors.add("Incorrect qualifier: " + saLogCond.qualifier);
		}
		
		//Handle params and special keywords
		if (tokens.length >= 5)
		{
			List<Object> paramObjects = new ArrayList<Object>();
			for (int i = 4; i < tokens.length; i++)
			{	
				String par = tokens[i].trim();
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
	
	/*
	public static int checkQualifier(String qual) {
		for (int i = 0; i< ALLOWED_QUALIFIERS.length; i++)
			if (qual.equals(ALLOWED_QUALIFIERS[i]))
				return i;
		return -1;
	}
	*/	
	
	public static ComparisonOperation qualifierToComparisonOperation(String qual) 
	{
		if (qual.equals("=") ||qual.equals("==") )
			return ComparisonOperation.EQUAL;
		if (qual.equals("<"))
			return ComparisonOperation.LESS;
		if (qual.equals("<="))
			return ComparisonOperation.LESS_OR_EQUAL;
		if (qual.equals(">"))
			return ComparisonOperation.GREATER;
		if (qual.equals(">="))
			return ComparisonOperation.GREATER_OR_EQUAL;
		if (qual.equals("!="))
			return ComparisonOperation.NOT_EQUAL;
		if (qual.equalsIgnoreCase("in_set"))
			return ComparisonOperation.IN_SET;
		if (qual.equalsIgnoreCase("interval"))
			return ComparisonOperation.INTERVAL;
		if (qual.equalsIgnoreCase("is_empty"))
			return ComparisonOperation.IS_EMPTY;
		if (qual.equalsIgnoreCase("not_empty"))
			return ComparisonOperation.NOT_EMPTY;
			
		return ComparisonOperation.UNDEFINED;
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
		if (conditionType != null)
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
		
		sb.append(targetType);
		
		if (targetLabel == null)
			sb.append(", NO_LABEL");
		else
			sb.append(", " + targetLabel);
		
		if (conditionType != null)
			sb.append(", " + conditionType);
		
		if (qualifier != null) 
			sb.append(", " + qualifier);
				
		if (params != null) {
			for (int i = 0; i < params.length; i++)
				sb.append(", " + params[i]);			
		}		
		return sb.toString();
	}
}
