package net.enanomapper.parser;

import java.util.List;

import net.enanomapper.parser.json.JsonUtilities;
import net.enanomapper.parser.recognition.ExpressionUtils;

import org.codehaus.jackson.JsonNode;

public class ExcelDataBlockLocation 
{
	
	private Object absoluteLocationValue = null;
	public String blockSectionName = null; 
	
	public ExcelDataLocation location = null;
	
	public Object numberOfRows = new Integer(1);
	public boolean FlagNumberOfRows = false;
	
	public int numberOfColumns = 1;
	public boolean Flag = false;
	
	public int rowSubblocks = 1;  //default: only one sub-block = entire block
	public boolean FlagRowSubblocks = false;
	
	public int columnSubblocks = 1;  //default: only one sub-block = entire block
	public boolean FlagColumnSubblocks = false;
	
	public List<BlockParameter> parameters = null;
	
	//Values definitions are relative to the sub-block beginning position (left upper corner)
	public Object valuesStartColumn = new Integer(0);  
	public boolean FlagValuesStartColumn = false;
	
	public Object valuesEndColumn = new Integer(0);
	public boolean FlagValuesEndColumn = false;
	
	public Object valuesStartRow = new Integer(0);  
	public boolean FlagValuesStartRow = false;
	
	public Object valuesEndRow = new Integer(0);
	public boolean FlagValuesEndRow = false;
	
	
	public static ExcelDataBlockLocation extractDataBlock(JsonNode node, ExcelParserConfigurator conf)
	{
		return extractDataBlock(node, null, conf);
	}
	
	
	public static ExcelDataBlockLocation extractDataBlock(JsonNode node, String jsonSection, ExcelParserConfigurator conf)
	{
		ExcelDataBlockLocation edbl = new ExcelDataBlockLocation();
		edbl.blockSectionName = jsonSection;
		
		JsonNode sectionNode;
		
		if (jsonSection == null)
			sectionNode = node; //The node itself is used
		else
		{	
			sectionNode = node.path(jsonSection);
			if (sectionNode.isMissingNode())
				return null;
		}
		
		
		ExcelDataLocation loc = ExcelDataLocation.extractDataLocation(sectionNode, "LOCATION", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				edbl.location = loc;
		}
		
		//NUMBER_OF_ROWS
		JsonNode nd = sectionNode.path("NUMBER_OF_ROWS");
		if (!nd.isMissingNode())
		{	
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null)
			{
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"NUMBER_OF_ROWS\" is incorrect!");
			}
			else
			{	
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null)
				{
					conf.configErrors.add("In JSON section \"" + jsonSection +
							"\", keyword \"NUMBER_OF_ROWS\" is incorrect expression: " 
							+ expr_error + " --> \"" + obj.toString() + "\"");
				}
				else
				{	
					edbl.numberOfRows = obj;
					edbl.FlagNumberOfRows = true;
				}	
			}	
		}		
		
		return edbl;
	}
	
	
	public String toJSONKeyWord(String offset, String blockName)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		
		String secName = blockName;
		if (secName == null)
			secName = blockSectionName;
		if (secName == null)
			secName = "NON_NAME_BLOCK";
		
		sb.append(offset + "\"" + secName + "\":\n");
		sb.append(offset + "{\n");
		
		if (location != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(location.toJSONKeyWord(offset+"\t"));
			nFields++;
		}
		
		if (FlagNumberOfRows)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"NUMBER_OF_ROWS\" : " + JsonUtilities.objectsToJsonField(numberOfRows));
			nFields++;
		}
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		
		return sb.toString();
	}
	
	public Object getAbsoluteLocationValue() {
		return absoluteLocationValue;
	}

	public void setAbsoluteLocationValue(Object absoluteLocationValue) {
		this.absoluteLocationValue = absoluteLocationValue;
	}
}
