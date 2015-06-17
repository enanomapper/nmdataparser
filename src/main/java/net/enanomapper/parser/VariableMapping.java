package net.enanomapper.parser;

import net.enanomapper.parser.json.JsonUtilities;

import org.codehaus.jackson.JsonNode;

public class VariableMapping 
{
	public String name = null;
	public String keysVariable = null;
	public String valuesVariable = null;
	
	
	public static VariableMapping extractVariableMapping(JsonNode node, ExcelParserConfigurator conf, 
			JsonUtilities jsonUtils, int mappingNum )
	{
		VariableMapping vm = new VariableMapping();
		
		//NAME
		if (node.path("NAME").isMissingNode())
		{
			conf.configErrors.add("In JSON Section VARIABLE_MAPPINGS[" + (mappingNum + 1) +  "], keyword \"NAME\" is missing!");
		}
		else
		{	
			String keyword =  jsonUtils.extractStringKeyword(node, "NAME", false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				vm.name = keyword;
		}
		
		//KEYS_VARIABLE
		if (node.path("KEYS_VARIABLE").isMissingNode())
		{
			conf.configErrors.add("In JSON Section VARIABLE_MAPPINGS[" + (mappingNum + 1) +  "], keyword \"KEYS_VARIABLE\" is missing!");
		}
		else
		{	
			String keyword =  jsonUtils.extractStringKeyword(node, "KEYS_VARIABLE", false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				vm.keysVariable = keyword;
		}
		
		//VALUES_VARIABLE
		if (node.path("VALUES_VARIABLE").isMissingNode())
		{
			conf.configErrors.add("In JSON Section VARIABLE_MAPPINGS[" + (mappingNum + 1) +  "], keyword \"VALUES_VARIABLE\" is missing!");
		}
		else
		{	
			String keyword =  jsonUtils.extractStringKeyword(node, "VALUES_VARIABLE", false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				vm.valuesVariable = keyword;
		}
		
		return vm;
	}
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		
		sb.append(offset + "{\n");
		
		if (name != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"NAME\" : " + JsonUtilities.objectToJsonField(name));
			nFields++;
		}
		
		if (keysVariable != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"KEYS_VARIABLE\" : " + JsonUtilities.objectToJsonField(keysVariable));
			nFields++;
		}
		
		if (valuesVariable != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"VALUES_VARIABLE\" : " + JsonUtilities.objectToJsonField(valuesVariable));
			nFields++;
		}
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
		
	}	
	
}
