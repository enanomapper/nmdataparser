package net.enanomapper.parser;

import org.codehaus.jackson.JsonNode;

public class ExcelDataBlockLocation 
{
	
	
	public static ExcelDataBlockLocation extractDataBlock(JsonNode node, ExcelParserConfigurator conf)
	{
		ExcelDataBlockLocation edbl = new ExcelDataBlockLocation();
		
		
		return edbl;
	}
	
	
	public String toJSONKeyWord(String offset, String blockName)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"" + blockName + "\":\n");
		sb.append(offset + "{\n");
		
		
		/*
		if (FlagIsArray)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"IS_ARRAY\" : " + isArray);
			nFields++;
		}
		*/
				
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		
		
		return sb.toString();
	}
}
