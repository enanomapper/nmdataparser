package net.enanomapper.parser;

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
		String strKeyword;

		// SUBSTANCE_NAME
		if (node.path(KEYWORD.SUBSTANCE_NAME.name()).isMissingNode()) {
			
		}
		else {
			strKeyword = jsonUtils.extractStringKeyword(node, KEYWORD.SUBSTANCE_NAME.name(), false);
			if (strKeyword == null)
				conf.addError("In section SUBSTANCE_RECORD_MAP, keyword SUBSTANCE_NAME: " + jsonUtils.getError());
			else {
				srml.substanceNameVariable = strKeyword;
			}
		}
		
		return srml;
	}	

}
