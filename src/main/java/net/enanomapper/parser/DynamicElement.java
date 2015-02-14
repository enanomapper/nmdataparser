package net.enanomapper.parser;

import org.codehaus.jackson.JsonNode;

import ambit2.base.relation.STRUCTURE_RELATION;
import net.enanomapper.parser.ParserConstants.ElementDataType;
import net.enanomapper.parser.ParserConstants.ElementField;
import net.enanomapper.parser.ParserConstants.ElementPosition;
import net.enanomapper.parser.ParserConstants.ElementSynchronization;
import net.enanomapper.parser.json.JsonUtilities;


public class DynamicElement 
{
	public ElementDataType dataType =  null;
	public boolean FlagDataType = false;

	public ElementField fieldType = ElementField.NONE;
	public boolean FlagFieldType = false;
	
	public ElementSynchronization synchType = ElementSynchronization.NONE;
	public boolean FlagSynchType = false;

	public ElementPosition position = ElementPosition.ANY_ROW;
	public boolean FlagPosition = false;

	public int index = -1;
	public boolean FlagIndex = false;

	//public int arrayNum = -1;             //This is used only for array elements
	//public boolean FlagArrayNum = false;

	public String jsonInfo = null;

	public boolean infoFromHeader = true;
	public boolean FlagInfoFromHeader = false;

	public String variableKeys[] = null; //The information is taken (constructed) form the variables defined by their keys

	public int childElements[] = null; //The information is taken from another element

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
		
		//SYNCH_TYPE
		if(!node.path("SYNCH_TYPE").isMissingNode())
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "SYNCH_TYPE", false);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"SYNCH_TYPE\": " + jsonUtils.getError());
			else
			{	
				element.synchType = ElementSynchronization.fromString(keyword);
				if (element.synchType == ElementSynchronization.UNDEFINED)
					conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
							+" subsection ELEMENT [" +(elNum +1) + "], keyword \"SYNCH_TYPE\" is incorrect or UNDEFINED!  -->"  + keyword);
				else
				{	
					element.FlagSynchType = true;
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
			//It is not treated as an error
			//conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
			//		+" subsection ELEMENT [" +(elNum +1) + "], keyword \"INDEX\" is missing!");
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

		//VARIABLE_KEYS
		JsonNode vkeys = node.path("VARIABLE_KEYS");
		if(!vkeys.isMissingNode())
		{
			if (vkeys.isArray())
			{	
				element.variableKeys = new String[vkeys.size()];
				for (int i = 0; i < vkeys.size(); i++)
				{	
					JsonNode keyNode = vkeys.get(i);
					if (keyNode.isTextual())
					{	
						String keyword =  keyNode.asText();
						if (keyword == null)
							conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
									+" subsection ELEMENT [" +(elNum +1) + "], keyword VARIABLE_KEYS [" + (i+1)+"]: is incorrect!");
						else
							element.variableKeys[i] = keyword;
					}
					else
					{	
						conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
								+" subsection ELEMENT [" +(elNum +1) + "], keyword VARIABLE_KEYS [" + (i+1)+"]: is not textual!");
					}
				}
			}
			else
			{
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"VARIABLE_KEYS\" is not an array!");
			}
		}

		//CHILD_ELEMENTS
		JsonNode children = node.path("CHILD_ELEMENTS");
		if(!children.isMissingNode())
		{
			if (children.isArray())
			{	
				element.childElements = new int [children.size()];
				for (int i = 0; i < children.size(); i++)
				{	
					JsonNode chNode = children.get(i);
					if (chNode.isInt())
					{	
						int intVal =  chNode.asInt();
						if (intVal <= 0)
							conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
									+" subsection ELEMENT [" +(elNum +1) + "], keyword CHILD_ELEMENTS [" + (i+1)+"]: is incorrect! --> " + intVal);
						else
							element.childElements[i] = intVal -1; //1-based --> 0-based
					}
					else
					{	
						conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
								+" subsection ELEMENT [" +(elNum +1) + "], keyword CHILD_ELEMENTS [" + (i+1)+"]: is not integer!");
					}
				}
			}
			else
			{
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"CHILD_ELEMENTS\" is not an array!");
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
		
		if (FlagSynchType)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"SYNCH_TYPE\" : \"" + synchType.toString() + "\"");
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

		if (variableKeys != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"VARIABLE_KEYS\" : [" );
			for (int i = 0; i < variableKeys.length; i++)
			{	
				sb.append("\"" + variableKeys[i] + "\"");
				if (i < (variableKeys.length -1))
					sb.append(", ");
			}	
			sb.append("]");
			nFields++;
		}

		if (childElements != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"CHILD_ELEMENTS\" : [" );
			for (int i = 0; i < childElements.length; i++)
			{	
				sb.append((childElements[i] + 1));    //0-based --> 1-based
				if (i < (childElements.length -1))
					sb.append(", ");
			}	
			sb.append("]");
			nFields++;
		}

		if (nFields > 0)
			sb.append("\n");

		sb.append(offset + "}");

		return sb.toString();
	}
	
	
	public void putElementObjectInRow(Object elObj, RowObject rowObj)
	{
		
	}

}
