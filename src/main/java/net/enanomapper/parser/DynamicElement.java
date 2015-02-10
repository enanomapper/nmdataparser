package net.enanomapper.parser;

import org.codehaus.jackson.JsonNode;

import net.enanomapper.parser.ParserConstants.ElementDataType;
import net.enanomapper.parser.ParserConstants.ElementField;
import net.enanomapper.parser.ParserConstants.ElementPosition;
import net.enanomapper.parser.json.JsonUtilities;


public class DynamicElement 
{
	public ElementDataType dataType =  null;
	public boolean FlagDataType = false;
	
	public ElementField fieldType = ElementField.NONE;
	public boolean FlagFieldType = false;
	
	public ElementPosition position = ElementPosition.ANY_ROW;
	public boolean FlagPosition = false;
	
	public int index = -1;
	public boolean FlagIndex = false;
	
	public int id = -1;
	public boolean FlagId = false;  
	
	public String jsonInfo = null;
	
	public boolean infoFromHeader = true;
	public boolean FlagInfoFromHeader = false;
	
	public String infoFromVariables[] = null; //The information is constructed form the variables defined by their keys
	
	public int childElementIds[] = null;
	
	public static DynamicElement  extractDynamicElement(JsonNode node, ExcelParserConfigurator conf, 
			String masterSection, int elNum)
{
	DynamicElement element = new DynamicElement();
	JsonUtilities jsonUtils = new JsonUtilities();
	
	//DATA_TYPE
	if(node.path("DATA_TYPE").isMissingNode())
	{
		if (node.path("FIELD_TYPE").isMissingNode())
			conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
					+" subsection ELEMENT [" +(elNum +1) + "], keyword + \"DATA_TYPE\" and \"FIELD_TYPE\" are missing!"
							+ " At least one must be specified.");
	}
	else
	{
		String keyword =  jsonUtils.extractStringKeyword(node, "DATA_TYPE", false);
		if (keyword == null)
			conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
					+" subsection ELEMENT [" +(elNum +1) + "], keyword \"DATA_TYPE\": " + jsonUtils.getError());
		else
		{	
			element.dataType = ElementDataType.fromString(keyword);
			if (element.dataType == ElementDataType.UNDEFINED)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"DATA_TYPE\" is incorrect or UNDEFINED!  -->"  + keyword);
			else
				element.FlagDataType = true;
		}	
	}
	
	//FIELD_TYPE
	if(!node.path("FIELD_TYPE").isMissingNode())
	{
		String keyword =  jsonUtils.extractStringKeyword(node, "FIELD_TYPE", false);
		if (keyword == null)
			conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
					+" subsection ELEMENT [" +(elNum +1) + "], keyword \"FIELD_TYPE\": " + jsonUtils.getError());
		else
		{	
			element.fieldType = ElementField.fromString(keyword);
			if (element.fieldType == ElementField.UNDEFINED)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"FIELD_TYPE\" is incorrect or UNDEFINED!  -->"  + keyword);
			else
			{	
				element.FlagFieldType = true;
				// Setting dataType / Checking field compatibility with dataType
				if (element.dataType == null)
					element.dataType = element.fieldType.getElement();
				else
				{
					if (element.dataType != element.fieldType.getElement())
						conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
								+" subsection ELEMENT [" +(elNum +1) + "], FIELD_TYPE \"" + element.fieldType.toString() + 
								"\" is incompatible with DATA_TYPE \"" + element.dataType.toString() + "\"");
				}
			}	
		}	
	}
	
	
	//POSITION
	if(!node.path("POSITION").isMissingNode())
	{
		String keyword =  jsonUtils.extractStringKeyword(node, "POSITION", false);
		if (keyword == null)
			conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
					+" subsection ELEMENT [" +(elNum +1) + "], keyword \"POSITION\": " + jsonUtils.getError());
		else
		{	
			element.position = ElementPosition.fromString(keyword);
			if (element.position == ElementPosition.UNDEFINED)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"POSITION\" is incorrect or UNDEFINED!  -->"  + keyword);
			else
				element.FlagPosition = true;
		}	
	}


	//INDEX
	if(node.path("INDEX").isMissingNode())
	{
		conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
				+" subsection ELEMENT [" +(elNum +1) + "], keyword \"INDEX\" is missing!");
	}
	else
	{
		//Index is extracted as column index (but it may be a row as well)
		int col_index = ExcelParserUtils.extractColumnIndex(node.path("INDEX"));
		if (col_index == -1)
		{
			conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
					+" subsection ELEMENT [" +(elNum +1) + "], keyword  \"INDEX\" is incorrect!");
		}
		else
		{	
			element.index = col_index;
			element.FlagIndex = true;
		}
	}
	
	
	//JSON_INFO
	if(!node.path("JSON_INFO").isMissingNode())
	{
		String keyword =  jsonUtils.extractStringKeyword(node, "JSON_INFO", false);
		if (keyword == null)
			conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
					+" subsection ELEMENT [" +(elNum +1) + "], keyword \"JSON_INFO\": " + jsonUtils.getError());
		else
		{	
			element.jsonInfo = keyword;
		}	
	}
	
	//INFO_FROM_HEADER
	if(!node.path("INFO_FROM_HEADER").isMissingNode())
	{
		Boolean b =  jsonUtils.extractBooleanKeyword(node, "INFO_FROM_HEADER", true);
		if (b == null)
			conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
					+" subsection ELEMENT [" +(elNum +1) + "], keyword \"INFO_FROM_HEADER\": " + jsonUtils.getError());
		else
		{	
			element.infoFromHeader = b;
			element.FlagInfoFromHeader = true;
		}	
	}	
	
	return element;
}
	
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		if (FlagDataType)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"DATA_TYPE\" : \"" + dataType.toString() + "\"");
			nFields++;
		}
		
		if (FlagFieldType)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"FIELD_TYPE\" : \"" + fieldType.toString() + "\"");
			nFields++;
		}
		
		if (FlagPosition)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"POSITION\" : \"" + position.toString() + "\"");
			nFields++;
		}
		
		if (FlagIndex)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"INDEX\" : " + (index + 1));
			nFields++;
		}
		
		if (jsonInfo != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"JSON_INFO\" : \"" + jsonInfo + "\"");
			nFields++;
		}
		
		if (FlagInfoFromHeader)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"INFO_FROM_HEADER\" : " + infoFromHeader + "");
			nFields++;
		}
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");

		return sb.toString();
	}
	
}
