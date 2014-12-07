package net.enanomapper.parser;

import java.util.HashMap;

public class ProtocolApplicationDataLocation 
{
	public ExcelDataLocation locUUID = null;
	public HashMap<String, ExcelDataLocation> protocolLocations = new HashMap<String, ExcelDataLocation>();
	public HashMap<String, ExcelDataLocation> parametersLocations = new HashMap<String, ExcelDataLocation>();
	
	
	public String toJSONKeyWord(String offset)
	{	
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		return sb.toString();
	}	
	
}
