package net.enanomapper.parser.recognition;

import com.fasterxml.jackson.databind.JsonNode;

import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.json.JsonUtilities;

public class TokenRegion 
{
	public static enum Type {
		FIXED_NUM_OF_CHARACTERS, SUBSTRING, 
		NUMBER_SEQUENCE, ALPH_SEQUENCE, ALPHANUMERIC_SEQUENCE, 
		UNDEFINED;  
		
		public static Type fromString(String s) {
			try {
				Type type = Type.valueOf(s);
				return (type);
			} catch (Exception e) {
				return Type.UNDEFINED;
			}
		}
	}
	
	public Type regionType = null;
	public int numChars = -1;
	public int beginIndex = -1;
	public int endIndex = -1;
	
	public static TokenRegion extractTokenRegion(JsonNode node, ExcelParserConfigurator conf, 
			JsonUtilities jsonUtils) 
	{
		TokenRegion reg = new TokenRegion();
		
		String keyword;
		
		return reg;
	}
	
}
