package net.enanomapper.parser;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import net.enanomapper.parser.json.JsonUtilities;

public class SubstanceRecordMapLocation 
{
	public String substanceNameVariable = null;
	public String substanceNameArray[] = null;
	
	
	public static SubstanceRecordMapLocation extractSubstanceRecordMapLocation(JsonNode node, ExcelParserConfigurator conf) 
	{
		SubstanceRecordMapLocation srml = new SubstanceRecordMapLocation();

		JsonUtilities jsonUtils = new JsonUtilities();
		
		// SUBSTANCE_NAME
		if (node.path(KEYWORD.SUBSTANCE_NAME.name()).isMissingNode()) {
			conf.addError("In section SUBSTANCE_RECORD_MAP, keyword " + KEYWORD.SUBSTANCE_NAME.name() + " is missing");
		}
		else {
			Object obj = extractKeyword(node, conf, KEYWORD.SUBSTANCE_NAME.name(), jsonUtils);
			if (obj != null) {
				if (obj instanceof String)
					srml.substanceNameVariable = (String) obj;
				else
					srml.substanceNameArray = (String[]) obj;
			}		
		}
		
		return srml;
	}
	
	static Object extractKeyword(JsonNode node, ExcelParserConfigurator conf, String keyword, JsonUtilities jsonUtils)
	{
		if (node.path(keyword).isTextual()) {
			String s = jsonUtils.extractStringKeyword(node, keyword, false);
			if (s == null)
			{	
				conf.addError("In section SUBSTANCE_RECORD_MAP, keyword " + keyword + ": " + jsonUtils.getError());
				return null;
			}
			return s;
		}
		else if (node.path(keyword).isArray()) {
			Object obj = JsonUtilities.extractObject(node.path(keyword));
			if (obj == null)
			{	
				conf.addError("In section SUBSTANCE_RECORD_MAP, keyword " + keyword + ": " + jsonUtils.getError());
				return null;
			}
			String s[] = objectArrayToStringArray((Object[])obj);
			if (s == null)
				conf.addError("In section SUBSTANCE_RECORD_MAP, keyword " + keyword + ": is not correct array of strings!");
			return s;
		}else {
			conf.addError("In section SUBSTANCE_RECORD_MAP, keyword " + keyword + ": must be TEXTUAL or ARRAY");
			return null;
		}	
	}
	
	static String[] objectArrayToStringArray(Object obj[])
	{
		String s[] = new String[obj.length];
		for (int i = 0; i < obj.length; i++)
		{
			if (obj[i] instanceof String)
				s[i] = (String) obj[i];
			else
				return null;
		}
		return s;
	}
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		Object obj;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"PROPORTION\" : \n" );
		sb.append(offset + "{\n");
		 
		
		//SUBSTANCE_NAME
		obj = null;
		if (substanceNameVariable != null)
			obj = substanceNameVariable;
		else if (substanceNameArray != null)
			obj = substanceNameArray;
		
		if (obj != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");			
			sb.append(offset + "\t" + JsonUtilities.objectToJsonKeywordAndField(
					KEYWORD.SUBSTANCE_NAME.name(), obj));
			nFields++;
		}
		

		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");

		return sb.toString();
	}	

}
