package net.enanomapper.parser;

import java.util.ArrayList;

import org.codehaus.jackson.JsonNode;

public class ExternalIdentifierDataLocation 
{
	public ExcelDataLocation type = null;
	public ExcelDataLocation id = null;
	
	public static ExternalIdentifierDataLocation extractExternalIdentifier(JsonNode node, ExcelParserConfigurator conf)
	{
		ExternalIdentifierDataLocation eidl = new ExternalIdentifierDataLocation();
		
		//TYPE
		ExcelDataLocation loc = ExcelParserConfigurator.extractDataLocation(node,"TYPE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				eidl.type = loc;
		}
		
		//ID
		loc = ExcelParserConfigurator.extractDataLocation(node,"ID", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				eidl.id = loc;
		}
		
		return eidl;
	}
	
	public void setParallelSheets(ParallelSheetState parSheets[], int primarySheetNum, ArrayList<String> errors)
	{
		if (type != null)
			ExcelParserUtils.setParallelSheet(type, parSheets, primarySheetNum, errors);
		
		if (id != null)
			ExcelParserUtils.setParallelSheet(id, parSheets, primarySheetNum, errors);
	}
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		if (type != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(type.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (id != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(id.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");

		return sb.toString();
	}	
	
}
