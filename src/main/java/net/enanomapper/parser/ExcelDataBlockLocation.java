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
	
	
	
	public Object rowSubblocks = new Integer(1);  //default: only one sub-block = entire block
	public boolean FlagRowSubblocks = false;
	
	public Object columnSubblocks = new Integer(1);  //default: only one sub-block = entire block
	public boolean FlagColumnSubblocks = false;
	
	public Object subblockSizeRows = new Integer(1);
	public boolean FlagSubblockSizeRows = false;
	
	public Object subblockSizeColumns = new Integer(1);
	public boolean FlagSubblockSizeColumns = false;
	
	public List<BlockParameter> parameters = null;
	
	//Values definitions are relative to the sub-block beginning position (left upper corner)
	public Object valuesStartColumn = new Integer(1);  
	public boolean FlagValuesStartColumn = false;
	
	public Object valuesEndColumn = new Integer(1);
	public boolean FlagValuesEndColumn = false;
	
	public Object valuesStartRow = new Integer(1);  
	public boolean FlagValuesStartRow = false;
	
	public Object valuesEndRow = new Integer(1);
	public boolean FlagValuesEndRow = false;
	
	//The shifts are relative to the corresponding value position (value by default is treated as lo-value)
	public Object qualifierColumnPosShift = new Integer(0); 
	public boolean FlagQualifierColumnPosShift = false;
	
	public Object qualifierRowPosShift = new Integer(0); 
	public boolean FlagQualifierRowPosShift = false;
	
	public Object upValueColumnPosShift = new Integer(0); 
	public boolean FlagUpValueColumnPosShift = false;
	
	public Object upValueRowPosShift = new Integer(0); 
	public boolean FlagUpValueRowPosShift = false;
	
	public Object upQualifierColumnPosShift = new Integer(0); 
	public boolean FlagUpQualifierColumnPosShift = false;
	
	public Object upQualifierRowPosShift = new Integer(0); 
	public boolean FlagUpQualifierRowPosShift = false;
	
	public Object errorColumnPosShift = new Integer(0); 
	public boolean FlagErrorColumnPosShift = false;
	
	public Object errorRowPosShift = new Integer(0); 
	public boolean FlagErrorRowPosShift = false;
	
	
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
		
		
	
		//ROW_SUBBLOCKS
		JsonNode nd = sectionNode.path("ROW_SUBBLOCKS");
		if (!nd.isMissingNode())
		{	
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null)
			{
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"ROW_SUBBLOCKS\" is incorrect!");
			}
			else
			{	
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null)
				{
					conf.configErrors.add("In JSON section \"" + jsonSection +
							"\", keyword \"ROW_SUBBLOCKS\" is incorrect expression: " 
							+ expr_error + " --> \"" + obj.toString() + "\"");
				}
				else
				{	
					edbl.rowSubblocks = obj;
					edbl.FlagRowSubblocks = true;
				}	
			}	
		}
	
		//COLUMN_SUBBLOCKS
		nd = sectionNode.path("COLUMN_SUBBLOCKS");
		if (!nd.isMissingNode())
		{	
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null)
			{
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"COLUMN_SUBBLOCKS\" is incorrect!");
			}
			else
			{	
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null)
				{
					conf.configErrors.add("In JSON section \"" + jsonSection +
							"\", keyword \"COLUMN_SUBBLOCKS\" is incorrect expression: " 
							+ expr_error + " --> \"" + obj.toString() + "\"");
				}
				else
				{	
					edbl.columnSubblocks = obj;
					edbl.FlagColumnSubblocks = true;
				}	
			}	
		}
		
		//SUBBLOCK_SIZE_ROWS
		nd = sectionNode.path("SUBBLOCK_SIZE_ROWS");
		if (!nd.isMissingNode())
		{	
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null)
			{
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"SUBBLOCK_SIZE_ROWS\" is incorrect!");
			}
			else
			{	
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null)
				{
					conf.configErrors.add("In JSON section \"" + jsonSection +
							"\", keyword \"SUBBLOCK_SIZE_ROWS\" is incorrect expression: " 
							+ expr_error + " --> \"" + obj.toString() + "\"");
				}
				else
				{	
					edbl.subblockSizeRows = obj;
					edbl.FlagSubblockSizeRows = true;
				}	
			}	
		}

		//SUBBLOCK_SIZE_COLUMNS
		nd = sectionNode.path("SUBBLOCK_SIZE_COLUMNS");
		if (!nd.isMissingNode())
		{	
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null)
			{
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"SUBBLOCK_SIZE_COLUMNS\" is incorrect!");
			}
			else
			{	
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null)
				{
					conf.configErrors.add("In JSON section \"" + jsonSection +
							"\", keyword \"SUBBLOCK_SIZE_COLUMNS\" is incorrect expression: " 
							+ expr_error + " --> \"" + obj.toString() + "\"");
				}
				else
				{	
					edbl.subblockSizeColumns = obj;
					edbl.FlagSubblockSizeColumns = true;
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
		
		
		
		if (FlagRowSubblocks)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"ROW_SUBBLOCKS\" : " + JsonUtilities.objectsToJsonField(rowSubblocks));
			nFields++;
		}
		
		if (FlagColumnSubblocks)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"COLUMN_SUBBLOCKS\" : " + JsonUtilities.objectsToJsonField(columnSubblocks));
			nFields++;
		}
		
		if (FlagSubblockSizeRows)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"SUBBLOCK_SIZE_ROWS\" : " + JsonUtilities.objectsToJsonField(subblockSizeRows));
			nFields++;
		}
		
		if (FlagSubblockSizeColumns)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"SUBBLOCK_SIZE_COLUMNS\" : " + JsonUtilities.objectsToJsonField(subblockSizeColumns));
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
