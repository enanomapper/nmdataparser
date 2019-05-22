package net.enanomapper.parser.recognition;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import net.enanomapper.parser.ExcelDataLocation;
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
		
	
	public static Tokenize extractTokenizer(JsonNode node, ExcelParserConfigurator conf, JsonUtilities jsonUtils) 
	{
		Tokenize tok = new Tokenize();

		String keyword;
		
		//NAME
		if (!node.path("NAME").isMissingNode()) {
			keyword = jsonUtils.extractStringKeyword(node, "NAME", false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else 
				tok.name = keyword;
		}
		
		// MODE
		if (!node.path("MODE").isMissingNode()) {
			keyword = jsonUtils.extractStringKeyword(node, "MODE", false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else {
				tok.mode = Mode.fromString(keyword);
				if (tok.mode == Mode.UNDEFINED)
					conf.addError("keyword \"MODE\" is incorrect or UNDEFINED!");
			}
		}
		
		//SPLITTER
		if (!node.path("SPLITTER").isMissingNode()) {
			keyword = jsonUtils.extractStringKeyword(node, "SPLITTER", false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else 
				tok.splitter = keyword;
		}
		
		// REGIONS
		JsonNode regNode = node.path("REGIONS");
		if (!regNode.isMissingNode()) {
			if (!regNode.isArray()) {
				conf.addError("REGIONS section is not of type array!");
			}

			tok.regions = new ArrayList<TokenRegion>();

			for (int i = 0; i < regNode.size(); i++) {
				TokenRegion reg = TokenRegion.extractTokenRegion(regNode.get(i), conf, jsonUtils);
				tok.regions.add(reg);
			}
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
		
		if (mode != null) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"MODE\" : " + "\"" + mode + "\"");
			nFields++;
		}
		
		if (splitter != null) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"SPLITTER\" : " + JsonUtilities.objectToJsonField(splitter));
			nFields++;
		}
		
		if (regions != null) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\"REGIONS\" : [");
			
			for (TokenRegion reg : regions) {
				if (nFields > 0)
					sb.append(",\n");
				sb.append(reg.toJSONKeyWord(offset + "\t"));
				nFields++;
			}
			sb.append("]");
			nFields++;
		}
				
		if (nFields > 0)
			sb.append("\n");

		sb.append(offset + "}");

		return sb.toString();
		
	}	

}
