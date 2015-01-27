package net.enanomapper.parser;

import net.enanomapper.parser.ParserConstants.ElementDataType;

public class DynamicGrouping 
{
	public int groupingElementIndex = -1;
	public boolean FlagGroupingElementIndex = false;
		
	public ElementDataType groupCumulativeType = ElementDataType.SUBSTANCE; 
	public boolean FlagGroupCumulativeType = false;
	
	public ElementDataType rowType = null;
	//public ElementDataType columnType = null;
	
	//data synchronization --> TODO
	
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		

		sb.append(offset + "}");

		return sb.toString();
	}
	
}
