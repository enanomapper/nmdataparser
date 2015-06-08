package net.enanomapper.parser;

import org.codehaus.jackson.JsonNode;

public class ExcelDataBlockLocation 
{
	public ExcelDataLocation location = null;
	
	
	
	public static ExcelDataBlockLocation extractDataBlock(JsonNode node, ExcelParserConfigurator conf)
	{
		ExcelDataBlockLocation edbl = new ExcelDataBlockLocation();
		
		
		ExcelDataLocation loc = ExcelDataLocation.extractDataLocation(node, "LOCATION", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				edbl.location = loc;
		}
		
		
		
		return edbl;
	}
	
	
	public String toJSONKeyWord(String offset, String blockName)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"" + blockName + "\":\n");
		sb.append(offset + "{\n");
		
		if (location != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(location.toJSONKeyWord(offset+"\t"));
			nFields++;
		}
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		
		return sb.toString();
	}
}
