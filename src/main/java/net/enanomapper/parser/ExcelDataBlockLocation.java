package net.enanomapper.parser;

import org.codehaus.jackson.JsonNode;

public class ExcelDataBlockLocation 
{
	public ExcelDataLocation location = null;
	
	public int numberOfRows = 1;
	public boolean FlagNumberOfRows = false;
	
	public int numberOfColumns = 1;
	public boolean Flag = false;
	
	public int rowSubblocks = 0;
	public boolean FlagRowSubblocks = false;
	
	public int columnSubblocks = 0;
	public boolean FlagColumnSubblocks = false;
	
	//TODO add sub-blocks definition, parameters and values
	
	
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
