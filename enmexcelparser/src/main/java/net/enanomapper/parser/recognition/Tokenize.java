package net.enanomapper.parser.recognition;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.json.JsonUtilities;


public class Tokenize 
{
	public static enum Mode {
		REGIONS, SPLIT, UNDEFINED;
		
		public static Mode fromString(String s) {
			try {
				Mode mode = Mode.valueOf(s);
				return (mode);
			} catch (Exception e) {
				return Mode.UNDEFINED;
			}
		}
	}
	
	public String name = null;
	public Mode mode = null;	
	public String splitter = null;
	public List<TokenRegion> regions = null;
		
	
	public static Tokenize extractTokenizer(JsonNode node, ExcelParserConfigurator conf) 
	{
		Tokenize tok = new Tokenize();

		JsonUtilities jsonUtils = new JsonUtilities();
		String keyword;
		
		//NAME
		if (!node.path("NAME").isMissingNode()) {
			keyword = jsonUtils.extractStringKeyword(node, "NAME", false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else 
				tok.name = keyword;
		}

		return tok;
	}
	
	public String toJSONKeyWord(String offset) {
		int nFields = 0;
		StringBuffer sb = new StringBuffer();

		sb.append(offset + "{\n");

		if (name != null) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"NAME\" : " + JsonUtilities.objectToJsonField(name));
			nFields++;
		}
		
		if (nFields > 0)
			sb.append("\n");

		sb.append(offset + "}");

		return sb.toString();
		
	}	

}
