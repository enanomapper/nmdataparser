package net.enanomapper.parser;

import net.enanomapper.parser.ParserConstants.ElementDataType;
import net.enanomapper.parser.ParserConstants.ElementField;


public class DynamicElement 
{
	public ElementDataType dataType =  null;
	public boolean FlagDataType = false;
	
	public ElementField fieldType = ElementField.NONE;
	public boolean FlagFieldType = false;
	
	public int index = -1;
	public boolean FlagIndex = false;
	
	public String jsonInfo = null;
	
	public boolean infoFromHeader = true;
	public boolean FlagInfoFromHeader = false;
	
	public int subElementIndices[] = null;
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		if (FlagDataType)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"DATA_TYPE\" : \"" + dataType.toString() + "\"");
			nFields++;
		}
		
		if (FlagFieldType)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"FIELD_TYPE\" : \"" + fieldType.toString() + "\"");
			nFields++;
		}
		
		if (FlagIndex)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"INDEX\" : " + (index + 1));
			nFields++;
		}
		
		if (jsonInfo != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"JSON_INFO\" : \"" + jsonInfo + "\"");
			nFields++;
		}
		
		if (FlagInfoFromHeader)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"INFO_FROM_HEADER\" : " + infoFromHeader + "");
			nFields++;
		}
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");

		return sb.toString();
	}
	
}
