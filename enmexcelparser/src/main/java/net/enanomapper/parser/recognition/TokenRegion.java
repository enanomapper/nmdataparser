package net.enanomapper.parser.recognition;

import com.fasterxml.jackson.databind.JsonNode;

import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.json.JsonUtilities;

public class TokenRegion 
{
	public static enum Type {
		FIXED_NUM_OF_CHARACTERS, SUBSTRING, 
		NUMBER_SEQUENCE, ALPHA_SEQUENCE, ALPHANUMERIC_SEQUENCE, 
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
	
	public Integer numOfChars = -1;
	public boolean FlagNumOfChars = false;
	
	public int beginIndex = -1;
	public boolean FlagBeginIndex = false;
	
	public int endIndex = -1;
	public boolean FlagEndIndex = false;
	
	public static TokenRegion extractTokenRegion(JsonNode node, ExcelParserConfigurator conf, 
			JsonUtilities jsonUtils) 
	{
		TokenRegion reg = new TokenRegion();
		
		String keyword;
		
		return reg;
	}
	
	public String toJSONKeyWord(String offset) {
		int nFields = 0;
		StringBuffer sb = new StringBuffer();

		sb.append(offset + "{\n");

		if (regionType != null) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"REGION_TYPE\" : " + JsonUtilities.objectToJsonField(regionType));
			nFields++;
		}
		
		if (FlagNumOfChars) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"NUM_OF_CHARS\" : " + JsonUtilities.objectToJsonField(numOfChars));
			nFields++;
		}
		
		if (FlagBeginIndex) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"BEGIN_INDEX\" : " + JsonUtilities.objectToJsonField(beginIndex));
			nFields++;
		}
		
		if (FlagEndIndex) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"END_INDEX\" : " + JsonUtilities.objectToJsonField(endIndex));
			nFields++;
		}
		
		
		if (nFields > 0)
			sb.append("\n");

		sb.append(offset + "}");

		return sb.toString();
		
	}	

	
}
