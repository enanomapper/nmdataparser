package net.enanomapper.parser;

import net.enanomapper.parser.ParserConstants.ElementDataType;

public class DynamicGrouping 
{
	public int groupingElementIndex = -1;
	public boolean FlagGroupingElementIndex = false;
		
	public ElementDataType groupCumulativeType = null; 
	public boolean FlagGroupCumulativeType = false;
	
	public ElementDataType rowType = null;
	public boolean FlagRowType = false;
	//public ElementDataType columnType = null;
	
	//element/data synchronization --> TODO
	
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		if (FlagGroupingElementIndex)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"GROUPING_ELEMENT_INDEX\" : " + (groupingElementIndex + 1));
			nFields++;
		}
		
		if (FlagGroupCumulativeType)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"GROUP_CUMULATIVE_TYPE\" : \"" + groupCumulativeType.toString() + "\"");
			nFields++;
		}
		
		if (FlagRowType)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ROW_TYPE\" : \"" + rowType.toString() + "\"");
			nFields++;
		}
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");

		return sb.toString();
	}
	
	public boolean checkConsistency()
	{
		if (FlagRowType)
			if (!rowType.isElementOf(groupCumulativeType))
				return false;
		return true;
	}
	
	
}
