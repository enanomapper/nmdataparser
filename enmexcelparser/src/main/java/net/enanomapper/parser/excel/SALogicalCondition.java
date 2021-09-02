package net.enanomapper.parser.excel;

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
	
	LogicalConditionType conditionType = LogicalConditionType.UNDEFINED;
	TargetType targetType = TargetType.UNDEFINED;
	public Object params[] = null;
	
	
	public boolean apply(Object target) {
		//TODO
		return false;
	}
	
	public boolean applyForSubstance() {
		//TODO
		return false;
	}
}
