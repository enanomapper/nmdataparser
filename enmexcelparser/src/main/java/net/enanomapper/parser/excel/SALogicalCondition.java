package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.List;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.ProtocolApplication;
import net.enanomapper.parser.excel.SALogicalCondition.ComparisonOperation;

public class SALogicalCondition 
{
	public static double eps = 1.0e-30;
	
	public static enum LogicalConditionType {
		VALUE, LABEL, UNDEFINED;
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
		SUBSTANCE, PROTOCOL, PROTOCOL_PARAMETER, EFFECT, CONDITION, UNDEFINED;
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
	
	public static class SAConditionResult {
		public SAConditionResult(boolean applicationStatus, boolean booleanResult, String info) {
			this.applicationStatus = applicationStatus;
			this.booleanResult = booleanResult;
			this.info = info;
		}
		public boolean applicationStatus = false;
		public boolean booleanResult = false;
		public String info = null;
	}
	
	public String targetLabel = null;
	public String targetLabelParam = null;
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
			if (!tok.equalsIgnoreCase("NO_LABEL") && !tok.isEmpty())
			{	
				int ind = tok.indexOf(":"); 
				if (ind == -1)
					saLogCond.targetLabel = tok;
				else
				{
					saLogCond.targetLabelParam = tok.substring(ind+1).trim();
					saLogCond.targetLabel = tok.substring(0,ind).trim();
					if (saLogCond.targetLabel.isEmpty())
						errors.add("Label is empty!");
				}
			}	
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
	
	
	public SAConditionResult apply(Object target) {
		
		if (target instanceof SubstanceRecord)
			return applyForSubstance((SubstanceRecord) target);
		if (target instanceof ProtocolApplication)
			return applyForProtocolApplication((ProtocolApplication) target);
		if (target instanceof EffectRecord)
			return applyForEffect((EffectRecord) target);
		if (target instanceof Object[]) {
			Object obj[] = (Object[]) target;
			if (obj.length > 1)
				return applyForKeyValue(obj[0], obj[1]);
		}	
		
		return new SAConditionResult(false, false, "Not applicable for " + target.getClass().getName());
	}
	
	public SAConditionResult applyForSubstance(SubstanceRecord subst) {
		//TODO
		return null;
	}
	
	public SAConditionResult applyForProtocolApplication(ProtocolApplication pa) {
		//TODO
		return null;
	}
	
	public SAConditionResult applyForEffect(EffectRecord effect) {
		//TODO
		return null;
	}
	
	public SAConditionResult applyForKeyValue(Object key, Object value) {
		//TODO
		return null;
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
		else {
			sb.append(", " + targetLabel);
			if (targetLabelParam != null)
				sb.append(":" + targetLabelParam);
		}
		
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
	
	
	public static boolean checkConditionForStringTarget(String targetStr, 
			ComparisonOperation comparison, boolean ignoreCase, Object params[])
	{
		//Check comparison cases without parameters
		if (comparison == ComparisonOperation.UNDEFINED)
			return true; //No comparison is performed then results is true;
		
		if (comparison == ComparisonOperation.IS_EMPTY) {
			if ((targetStr == null) || targetStr.isEmpty())
				return true;
			else
				return false;
		}
		
		if (comparison == ComparisonOperation.NOT_EMPTY) {
			if ((targetStr == null) || targetStr.isEmpty())
				return false;
			else
				return true;
		}
		
		//Check for empty input params
		if ((params == null) || (params.length == 0) )
			return false;
				
		if (targetStr == null)
			return false;
		
		int compareRes;
		if (ignoreCase)
			compareRes = targetStr.compareToIgnoreCase(params[0].toString());
		else
			compareRes = targetStr.compareTo(params[0].toString());
		
		
		switch (comparison) 
		{
		case EQUAL:
			return (compareRes == 0);
		case GREATER:
			return (compareRes > 0);
		case GREATER_OR_EQUAL:
			return (compareRes >= 0);
		case LESS:
			return (compareRes < 0);
		case LESS_OR_EQUAL:
			return (compareRes <= 0);
		case NOT_EQUAL:
			return (compareRes != 0);
		case IN_SET:			
			if (compareRes == 0)
				return true;
			for (int i = 1; i < params.length; i++)
			{
				int compareRes_i;
				if (ignoreCase)
					compareRes_i = targetStr.compareToIgnoreCase(params[i].toString());
				else
					compareRes_i = targetStr.compareTo(params[i].toString());
				
				if (compareRes_i == 0)
					return true;	
			}
			return false;
		case INTERVAL:
			if (params.length == 1) {
				//With a single parameter, the INTERVAL comparison 
				//is treated as [param,...] i.e. equavalent to GREATER_OR_EQUAL 
				return (compareRes >= 0);
			}
			else {
				//params.length > 1
				int compareRes1;
				if (ignoreCase)
					compareRes1 = targetStr.compareToIgnoreCase(params[1].toString());
				else
					compareRes1 = targetStr.compareTo(params[1].toString());
				
				return (compareRes >= 0 && compareRes1 <= 0);
			}
		}
		
		//All other cases are not applicable here
		return false;
	}
	
	
	public static boolean checkConditionForDoubleTarget(Double d, 
			ComparisonOperation comparison, double params[])
	{	
		if (comparison == ComparisonOperation.UNDEFINED)
			return true; //No comparison is performed then results is true;
		
		if (comparison == ComparisonOperation.IS_EMPTY) {
			if (d == null)
				return true;
			else
				return false;
		}
		
		if (comparison == ComparisonOperation.NOT_EMPTY) {
			if (d == null)
				return false;
			else
				return true;
		}
		
		//Check for empty input params
		if ((params == null) || (params.length == 0) )
			return false;
		
		if (d == null)
			return false;
		
		switch (comparison) 
		{
		case EQUAL:
			return (equal(d, params[0]));
		case GREATER:
			return (d > params[0]);
		case GREATER_OR_EQUAL:
			return (d >= params[0]);
		case LESS:
			return (d < params[0]);
		case LESS_OR_EQUAL:
			return (d <= params[0]);
		case NOT_EQUAL:
			return (d != params[0]);
		case IN_SET:
			for (int i = 0; i < params.length; i++)
				if (equal(d, params[i]))
					return true;
			return false;
		case INTERVAL:
			if (params.length == 1)
			{	
				//With a single parameter, the INTERVAL comparison 
				//is treated as [params[0],...] i.e. it equivalent to GREATER_OR_EQUAL
				return (d >= params[0]);
			}	
			else
				return ((d >= params[0]) && (d <= params[1]));
		}
		
		return false;
	}
	
	public static boolean equal(double x1, double x2)
	{
		return equal(x1, x2, eps);
	}
	
	public static boolean equal(double x1, double x2, double e)
	{
		return (Math.abs(x1-x2) < e);
	}
	
	public static Object extractParamsForLogicalConditionCheck(ComparisonOperation comparison, Object params[], int checkIndex )
	{
		if (params == null)
			return null;
		if (params.length == 0)
			return null;
		
		if (comparison == ComparisonOperation.UNDEFINED 
				|| comparison == ComparisonOperation.IS_EMPTY
				|| comparison == ComparisonOperation.NOT_EMPTY)
			return null;
		
		if (comparison == ComparisonOperation.IN_SET
				|| comparison == ComparisonOperation.INTERVAL)
			return params;
		
		//Handling standard comparison case: 
		//EQUAL, LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL
		if (checkIndex == -1)
			return null;		
		if (checkIndex >= params.length)
			return params[params.length-1]; //last element is returned		
		return params[checkIndex];
	}
	
}
