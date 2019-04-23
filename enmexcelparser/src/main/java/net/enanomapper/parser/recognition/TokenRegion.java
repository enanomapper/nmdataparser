package net.enanomapper.parser.recognition;

import com.fasterxml.jackson.databind.JsonNode;

import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.json.JsonUtilities;

public class TokenRegion 
{
	public static enum Type {
		FIXED_NUM_OF_CHARACTERS, SUBSTRING, NUMBER, ALPHABET, ALPHANUMERIC, UNDEFINED  
	}
	
	public Type regionType = null;
	public int numChars = -1;
	public int beginIndex = -1;
	public int endIndex = -1;
	
	public static TokenRegion extractTokenRegion(JsonNode node, ExcelParserConfigurator conf) 
	{
		TokenRegion reg = new TokenRegion();

		JsonUtilities jsonUtils = new JsonUtilities();
		String keyword;
		
		return reg;
	}
	
}
