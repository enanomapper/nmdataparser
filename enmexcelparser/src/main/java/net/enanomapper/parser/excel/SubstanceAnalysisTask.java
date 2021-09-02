package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.List;

import ambit2.base.data.SubstanceRecord;

public class SubstanceAnalysisTask 
{
	public static enum SATaskType {
		CHECK_EFFECT_VALUE, CHECK_PARAMETER_VALUE, CHECK_CONDITION_VALUE, 
		COUNT_RECORDS, COUNT_EFFECTS, COUNT_PARAMETERS, COUNT_CONDITIONS, UNDEFINED;
		
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
		
	public boolean flagVerboseResult = false;	
	public List<String> analysisResult = new ArrayList<String>();
	
	
	public int run(List<SubstanceRecord> records) 
	{
		//TODO
		return 0;
	}
}
