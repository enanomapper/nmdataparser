package net.enanomapper.parser;


import com.fasterxml.jackson.databind.JsonNode;

import net.enanomapper.parser.json.JsonUtilities;

public class SubstanceRecordMapLocation 
{
	public static final String ALLOWED_MAP_ELEMENTS[] = {"SUBSTANCE_NAME", "PUBLIC_NAME"};
	
	public String mapElement = null;
	
	public String substanceNameVariable = null;
	public String substanceNameArray[] = null;
	
	public String publicNameVariable = null;
	public String publicNameArray[] = null;
	
	
	public static SubstanceRecordMapLocation extractSubstanceRecordMapLocation(JsonNode node, ExcelParserConfigurator conf) 
	{
		SubstanceRecordMapLocation srml = new SubstanceRecordMapLocation();

		JsonUtilities jsonUtils = new JsonUtilities();
		
		//MAP_ELEMENT
		if (node.path(KEYWORD.MAP_ELEMENT.name()).isMissingNode()) {
			conf.addError("In section SUBSTANCE_RECORD_MAP, keyword " +KEYWORD.MAP_ELEMENT.name() + " is missing");
		}
		else {
			String s = jsonUtils.extractStringKeyword(node, KEYWORD.MAP_ELEMENT.name(), false);
			if (s == null)				
				conf.addError("In section SUBSTANCE_RECORD_MAP, keyword " +KEYWORD.MAP_ELEMENT.name() + ": " + jsonUtils.getError());
			else {	
				srml.mapElement = s;
				//TODO check allowed map elements
			}
		}
			
		
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
		
		// PUBLIC_NAME
		if (node.path(KEYWORD.PUBLIC_NAME.name()).isMissingNode()) {
			conf.addError("In section SUBSTANCE_RECORD_MAP, keyword " + KEYWORD.PUBLIC_NAME.name() + " is missing");
		}
		else {
			Object obj = extractKeyword(node, conf, KEYWORD.PUBLIC_NAME.name(), jsonUtils);
			if (obj != null) {
				if (obj instanceof String)
					srml.publicNameVariable = (String) obj;
				else
					srml.publicNameArray = (String[]) obj;
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
	
	public static String[] objectArrayToStringArray(Object obj[])
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
	
	public boolean checkConsistency(ExcelParserConfigurator conf) 
	{
		if (mapElement == null)
			return false;
		
		if (!isAllowed (mapElement))
			conf.addError("In section SUBSTANCE_RECORD_MAP, keyword " 
					+ KEYWORD.MAP_ELEMENT.name() + " value " + mapElement + " is not allowed value");
		return true;
	}
	
	boolean isAllowed(String mapEl) {
		for (int i = 0; i < ALLOWED_MAP_ELEMENTS.length; i++)
			if (ALLOWED_MAP_ELEMENTS[i].equals(mapEl))
				return true;
		return false;
	}
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		Object obj;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"SUBSTANCE_RECORD_MAP\" : \n" );
		sb.append(offset + "{\n");
		 
		
		if (mapElement != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");			
			sb.append(offset + "\t" + JsonUtilities.objectToJsonKeywordAndField(
					KEYWORD.MAP_ELEMENT.name(), mapElement));
			nFields++;
		}
		
		
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

		//PUBLIC_NAME
		obj = null;
		if (publicNameVariable != null)
			obj = publicNameVariable;
		else if (publicNameArray != null)
			obj = publicNameArray;
		
		if (obj != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");			
			sb.append(offset + "\t" + JsonUtilities.objectToJsonKeywordAndField(
					KEYWORD.PUBLIC_NAME.name(), obj));
			nFields++;
		}
		
		

		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");

		return sb.toString();
	}	

}
