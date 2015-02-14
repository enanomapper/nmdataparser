package net.enanomapper.parser;

import org.codehaus.jackson.JsonNode;

import net.enanomapper.parser.ParserConstants.ObjectType;
import net.enanomapper.parser.json.JsonUtilities;

public class DynamicGrouping 
{
	public int groupingElementIndex = -1;
	public boolean FlagGroupingElementIndex = false;
		
	public ObjectType groupCumulativeType = null; 
	public boolean FlagGroupCumulativeType = false;
	
	public ObjectType rowType = null;
	public boolean FlagRowType = false;
	
	//public ElementDataType columnType = null;
	
	//element/data synchronization ??? --> TODO
	
	
	public static DynamicGrouping  extractDynamicGrouping(JsonNode node, ExcelParserConfigurator conf, 
			String masterSection, int groupNum)
	{
		DynamicGrouping dyngrp = new DynamicGrouping();
		JsonUtilities jsonUtils = new JsonUtilities();
		
		//GROUPING_ELEMENT_INDEX
		if(node.path("GROUPING_ELEMENT_INDEX").isMissingNode())
		{
			conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
					+" subsection GROUP_LEVEL [" +(groupNum +1) + "], keyword \"GROUPING_ELEMENT_INDEX\" is missing!");
		}
		else
		{
			//Index is extracted as column index (but it may be a row as well)
			int col_index = ExcelParserUtils.extractColumnIndex(node.path("GROUPING_ELEMENT_INDEX"));
			if (col_index == -1)
			{
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection GROUP_LEVEL [" +(groupNum +1) + "], keyword \"GROUPING_ELEMENT_INDEX\" is incorrect!");
			}
			else
			{	
				dyngrp.groupingElementIndex = col_index;
				dyngrp.FlagGroupingElementIndex = true;
			}
		}
		
		//GROUP_CUMULATIVE_TYPE
		if(node.path("GROUP_CUMULATIVE_TYPE").isMissingNode())
		{
			conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
					+" subsection GROUP_LEVEL [" +(groupNum +1) + "], keyword \"GROUP_CUMULATIVE_TYPE\" is missing!");
		}
		else
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "GROUP_CUMULATIVE_TYPE", false);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection GROUP_LEVEL [" +(groupNum +1) + "], keyword \"GROUP_CUMULATIVE_TYPE\" :" + jsonUtils.getError());
			else
			{	
				dyngrp.groupCumulativeType = ObjectType.fromString(keyword);
				if (dyngrp.groupCumulativeType == ObjectType.UNDEFINED)
					conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
							+" subsection GROUP_LEVEL [" +(groupNum +1) + "], keyword \"GROUP_CUMULATIVE_TYPE\" is incorrect or UNDEFINED!  -->"  + keyword);
				else
					dyngrp.FlagGroupCumulativeType = true;
			}	
		}
		
		//ROW_TYPE
		if(!node.path("ROW_TYPE").isMissingNode())
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "ROW_TYPE", false);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection GROUP_LEVEL [" +(groupNum +1) + "], keyword \"ROW_TYPE\" :" + jsonUtils.getError());
			else
			{	
				dyngrp.rowType = ObjectType.fromString(keyword);
				if (dyngrp.rowType == ObjectType.UNDEFINED)
					conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
							+" subsection GROUP_LEVEL [" +(groupNum +1) + "], keyword \"ROW_TYPE\" is incorrect or UNDEFINED!  -->"  + keyword);
				else
					dyngrp.FlagRowType = true;
			}	
		}

		//TODO - some other fields ...
		return dyngrp;
	}
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		if (FlagGroupingElementIndex)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"GROUPING_ELEMENT_INDEX\" : " + (groupingElementIndex + 1));
			nFields++;
		}
		
		if (FlagGroupCumulativeType)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"GROUP_CUMULATIVE_TYPE\" : \"" + groupCumulativeType.toString() + "\"");
			nFields++;
		}
		
		if (FlagRowType)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ROW_TYPE\" : \"" + rowType.toString() + "\"");
			nFields++;
		}
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");

		return sb.toString();
	}
	
	public boolean checkConsistency()
	{
		if (FlagRowType)
			if (!rowType.isElementOf(groupCumulativeType))
				return false;
		return true;
	}
	
	
}
