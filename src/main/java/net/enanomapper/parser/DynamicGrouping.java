package net.enanomapper.parser;

import net.enanomapper.parser.ParserConstants.DataElementType;

public class DynamicGrouping 
{
	public int groupingElementIndex = -1;
	public DataElementType groupDataType = DataElementType.SUBSTANCE; 
	
	public DataElementType rowType = null;
	//public DataElementType columnType = null;
	
	//data synchronization
	
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		

		sb.append(offset + "}");

		return sb.toString();
	}
	
}
