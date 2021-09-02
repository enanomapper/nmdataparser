package net.enanomapper.parser.excel;

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
	
	LogicalConditionType type = LogicalConditionType.UNDEFINED;
}
