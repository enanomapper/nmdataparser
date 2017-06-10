package net.enanomapper.parser.recognition;

import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.json.JsonUtilities;

import com.fasterxml.jackson.databind.JsonNode;

public class EffectSetPattern 
{
	private static String sectionName = "PATTERN";
	
	public boolean skipSpaces = true;
	public boolean FlagSkipSpaces = false;
	
	public String effectSeparator = ";";
	public boolean FlagEffectSeparator = false;
	
	public String internalSeparator = "=";
	public boolean FlagInternalSeparator = false;
	
	public boolean endPointAtFirstPosition = true;
	public boolean FlagEndPointAtFirstPosition = false;
	
	public String endpointPrefix = null;
	public boolean FlagEndpointPrefix = false;
	
	public String endpointSuffix = null;
	public boolean FlagEndpointSuffix = false;
	
	public String valuePrefix = null;
	public boolean FlagValuePrefix = false;
	
	public String valueSuffix = null;
	public boolean FlagValueSuffix = false;
	
	public String unit = null;
	public boolean FlagUnit = false;
	
	
	public static EffectSetPattern extractEffectSetPattern(JsonNode node, ExcelParserConfigurator conf)
	{
		return extractEffectSetPattern(node, null, conf);
	}
	
	public static EffectSetPattern extractEffectSetPattern(JsonNode node, String jsonSection, ExcelParserConfigurator conf)
	{
		EffectSetPattern esp = new EffectSetPattern();
		
		JsonNode sectionNode;
		
		if (jsonSection == null)
			sectionNode = node; //The node itself is used
		else
		{	
			sectionNode = node.path(jsonSection);
			if (sectionNode.isMissingNode())
				return null;
		}
		
		JsonUtilities jsonUtils = new JsonUtilities();
		
		//SKIP_SPACES
		if (!sectionNode.path("SKIP_SPACES").isMissingNode())
		{
			Boolean b = jsonUtils.extractBooleanKeyword(sectionNode, "SKIP_SPACES", false);
			if (b ==  null)
			{	
				conf.configErrors.add("In JSON section \"" + 
						jsonSection + "\", keyword \"SKIP_SPACES\" : " + 
						jsonUtils.getError());
			}	
			else
			{	
				esp.skipSpaces = b;
				esp.FlagSkipSpaces = true;
			}	
		}
		
		//EFFECT_SEPARATOR
		if (!sectionNode.path("EFFECT_SEPARATOR").isMissingNode())
		{	
			String keyword =  jsonUtils.extractStringKeyword(sectionNode, "EFFECT_SEPARATOR", false);
			if (keyword == null)
			{	
				conf.configErrors.add("In JSON section \"" + 
						jsonSection + "\", keyword \"EFFECT_SEPARATOR\" : " + 
						jsonUtils.getError());
			}	
			else
			{	
				esp.FlagEffectSeparator = true;
				esp.effectSeparator = keyword;	
			}
		}
		
		//INTERNAL_SEPARATOR
		if (!sectionNode.path("INTERNAL_SEPARATOR").isMissingNode())
		{	
			String keyword =  jsonUtils.extractStringKeyword(sectionNode, "INTERNAL_SEPARATOR", false);
			if (keyword == null)
			{	
				conf.configErrors.add("In JSON section \"" + 
						jsonSection + "\", keyword \"INTERNAL_SEPARATOR\" : " + 
						jsonUtils.getError());
			}	
			else
			{	
				esp.FlagInternalSeparator = true;
				esp.internalSeparator = keyword;	
			}
		}

		//ENDPOINT_AT_FIRST_POSITION
		if (!sectionNode.path("ENDPOINT_AT_FIRST_POSITION").isMissingNode())
		{
			Boolean b = jsonUtils.extractBooleanKeyword(sectionNode, "ENDPOINT_AT_FIRST_POSITION", false);
			if (b ==  null)
			{	
				conf.configErrors.add("In JSON section \"" + 
						jsonSection + "\", keyword \"ENDPOINT_AT_FIRST_POSITION\" : " 
						+ jsonUtils.getError());
			}	
			else
			{	
				esp.endPointAtFirstPosition = b;
				esp.FlagEndPointAtFirstPosition = true;
			}	
		}
		
		//ENDPOINT_PREFIX
		if (!sectionNode.path("ENDPOINT_PREFIX").isMissingNode())
		{	
			String keyword =  jsonUtils.extractStringKeyword(sectionNode, "ENDPOINT_PREFIX", false);
			if (keyword == null)
			{	
				conf.configErrors.add("In JSON section \"" + 
						jsonSection + "\", keyword \"ENDPOINT_PREFIX\" : " + 
						jsonUtils.getError());
			}	
			else
			{	
				esp.FlagEndpointPrefix = true;
				esp.endpointPrefix = keyword;	
			}
		}
		
		//ENDPOINT_SUFFIX
		if (!sectionNode.path("ENDPOINT_SUFFIX").isMissingNode())
		{	
			String keyword =  jsonUtils.extractStringKeyword(sectionNode, "ENDPOINT_SUFFIX", false);
			if (keyword == null)
			{	
				conf.configErrors.add("In JSON section \"" + 
						jsonSection + "\", keyword \"ENDPOINT_SUFFIX\" : " + 
						jsonUtils.getError());
			}	
			else
			{	
				esp.FlagEndpointSuffix = true;
				esp.endpointSuffix = keyword;	
			}
		}
		
		//VALUE_PREFIX
		if (!sectionNode.path("VALUE_PREFIX").isMissingNode())
		{	
			String keyword =  jsonUtils.extractStringKeyword(sectionNode, "VALUE_PREFIX", false);
			if (keyword == null)
			{	
				conf.configErrors.add("In JSON section \"" + 
						jsonSection + "\", keyword \"VALUE_PREFIX\" : " + 
						jsonUtils.getError());
			}	
			else
			{	
				esp.FlagValuePrefix = true;
				esp.valuePrefix = keyword;	
			}
		}

		//VALUE_SUFFIX
		if (!sectionNode.path("VALUE_SUFFIX").isMissingNode())
		{	
			String keyword =  jsonUtils.extractStringKeyword(sectionNode, "VALUE_SUFFIX", false);
			if (keyword == null)
			{	
				conf.configErrors.add("In JSON section \"" + 
						jsonSection + "\", keyword \"VALUE_SUFFIX\" : " + 
						jsonUtils.getError());
			}	
			else
			{	
				esp.FlagValueSuffix = true;
				esp.valueSuffix = keyword;	
			}
		}
		

		//UNIT
		if (!sectionNode.path("UNIT").isMissingNode())
		{	
			String keyword =  jsonUtils.extractStringKeyword(sectionNode, "UNIT", false);
			if (keyword == null)
			{	
				conf.configErrors.add("In JSON section \"" + 
						jsonSection + "\", keyword \"UNIT\" : " + 
						jsonUtils.getError());
			}	
			else
			{	
				esp.FlagUnit = true;
				esp.unit = keyword;	
			}
		}

		
		//TODO
		
		
		
		return esp;
	}
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"" + sectionName + "\":\n");
		sb.append(offset + "{\n");
		
		if (FlagSkipSpaces)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"SKIP_SPACES\" : " + skipSpaces);
			nFields++;
		}
		
		if (FlagEffectSeparator)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"EFFECT_SEPARATOR\" : \"" + effectSeparator + "\"");
			nFields++;
		}
		
		if (FlagInternalSeparator)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"INTERNAL_SEPARATOR\" : \"" + internalSeparator + "\"");
			nFields++;
		}
		
		if (FlagEndPointAtFirstPosition)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ENDPOINT_AT_FIRST_POSITION\" : " + endPointAtFirstPosition);
			nFields++;
		}
		
		if (FlagEndpointPrefix)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ENDPOINT_PREFIX\" : \"" + endpointPrefix + "\"");
			nFields++;
		}
		
		if (FlagEndpointSuffix)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ENDPOINT_SUFFIX\" : \"" + endpointSuffix + "\"");
			nFields++;
		}
		
		if (FlagValuePrefix)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"VALUE_PREFIX\" : \"" + valuePrefix + "\"");
			nFields++;
		}
		
		if (FlagValueSuffix)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"VALUE_SUFFIX\" : \"" + valueSuffix + "\"");
			nFields++;
		}
		
		if (FlagUnit)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"UNIT\" : \"" + unit + "\"");
			nFields++;
		}
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
	}
}
