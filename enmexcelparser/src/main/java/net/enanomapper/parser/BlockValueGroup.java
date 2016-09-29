package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.List;

import net.enanomapper.parser.json.JsonUtilities;
import net.enanomapper.parser.recognition.ExpressionUtils;

import org.codehaus.jackson.JsonNode;


/**
 * This class defines how a group of values (with associated parameters for each value)
 * is extracted from particular sub-block
 * @author nick
 *
 */
public class BlockValueGroup 
{	
	public String name = null;
	public boolean FlagName = false;
	
	public String unit = null;
	public boolean FlagUnit = false;
	
	//Values definitions are in the context of sub-block
	public Object startColumn = null;  
	public boolean FlagStartColumn = false;

	public Object endColumn = null;
	public boolean FlagEndColumn = false;

	public Object startRow = null;  
	public boolean FlagStartRow = false;

	public Object endRow = null;
	public boolean FlagEndRow = false;

	//The shifts are relative to the corresponding value position ('value' by default is considered to be lo-value or text-value)
	public Object qualifierColumnShift = new Integer(0); 
	public boolean FlagQualifierColumnShift = false;

	public Object qualifierRowShift = new Integer(0); 
	public boolean FlagQualifierRowShift = false;

	public Object upValueColumnShift = new Integer(0); 
	public boolean FlagUpValueColumnShift = false;

	public Object upValueRowShift = new Integer(0); 
	public boolean FlagUpValueRowShift = false;

	public Object upQualifierColumnShift = new Integer(0); 
	public boolean FlagUpQualifierColumnShift = false;

	public Object upQualifierRowShift = new Integer(0); 
	public boolean FlagUpQualifierRowShift = false;

	public Object errorColumnShift = new Integer(0); 
	public boolean FlagErrorColumnShift = false;

	public Object errorRowShift = new Integer(0); 
	public boolean FlagErrorRowShift = false;
	
	public List<BlockParameter> parameters = null;
	
	
	public static BlockValueGroup extractValueGroup(JsonNode node, ExcelParserConfigurator conf, int valueGroupNum)
	{
		BlockValueGroup bvg = new BlockValueGroup();
		
		JsonUtilities jsonUtils = new JsonUtilities();
		
		//NAME
		String keyword = jsonUtils.extractStringKeyword(node, "NAME", false);
		if (keyword == null)
			conf.configErrors.add(jsonUtils.getError());
		else
		{	
			bvg.name = keyword;
			bvg.FlagName = true;
		}
		
		//UNIT
		keyword = jsonUtils.extractStringKeyword(node, "UNIT", false);
		if (keyword == null)
			conf.configErrors.add(jsonUtils.getError());
		else
		{	
			bvg.unit = keyword;
			bvg.FlagUnit = true;
		}
		
		//START_COLUMN
		JsonNode nd = node.path("START_COLUMN");
		if (!nd.isMissingNode())
		{	
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null)
			{
				conf.configErrors.add("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)  +
						"], keyword \"START_COLUMN\" is incorrect!");
			}
			else
			{	
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null)
				{
					conf.configErrors.add("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)  +
							"], keyword \"START_COLUMN\" is incorrect! expression: " 
							+ expr_error + " --> \"" + obj.toString() + "\"");
				}
				else
				{	
					bvg.startColumn = obj;
					bvg.FlagStartColumn = true;
				}	
			}	
		}
		
		//END_COLUMN
		nd = node.path("END_COLUMN");
		if (!nd.isMissingNode())
		{	
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null)
			{
				conf.configErrors.add("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)  +
						"], keyword \"END_COLUMN\" is incorrect!");
			}
			else
			{	
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null)
				{
					conf.configErrors.add("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)  +
							"], keyword \"END_COLUMN\" is incorrect! expression: " 
							+ expr_error + " --> \"" + obj.toString() + "\"");
				}
				else
				{	
					bvg.endColumn = obj;
					bvg.FlagEndColumn = true;
				}	
			}	
		}
		

		//START_ROW
		nd = node.path("START_ROW");
		if (!nd.isMissingNode())
		{	
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null)
			{
				conf.configErrors.add("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)  +
						"], keyword \"START_ROW\" is incorrect!");
			}
			else
			{	
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null)
				{
					conf.configErrors.add("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)  +
							"], keyword \"START_ROW\" is incorrect! expression: " 
							+ expr_error + " --> \"" + obj.toString() + "\"");
				}
				else
				{	
					bvg.startRow = obj;
					bvg.FlagStartRow = true;
				}	
			}	
		}

		//END_ROW
		nd = node.path("END_ROW");
		if (!nd.isMissingNode())
		{	
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null)
			{
				conf.configErrors.add("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)  +
						"], keyword \"END_ROW\" is incorrect!");
			}
			else
			{	
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null)
				{
					conf.configErrors.add("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)  +
							"], keyword \"END_ROW\" is incorrect! expression: " 
							+ expr_error + " --> \"" + obj.toString() + "\"");
				}
				else
				{	
					bvg.endRow = obj;
					bvg.FlagEndRow = true;
				}	
			}	
		}	
		
		//TODO
		
		
		//PARAMETERS
		JsonNode parNode = node.path("PARAMETERS");
		if (!parNode.isMissingNode())
		{
			if (!parNode.isArray())
			{	
				conf.configErrors.add("PARAMETERS section is not of type array!");
			}

			bvg.parameters = new ArrayList<BlockParameter>();

			for (int i = 0; i < parNode.size(); i++)
			{	
				BlockParameter bp = BlockParameter.extractBlockParameter(parNode.get(i), conf, jsonUtils, i);
				bvg.parameters.add(bp);
			}	
		}
		
		return bvg;
	}
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		
		sb.append(offset + "{\n");
		
		if (FlagName)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"NAME\" : " + JsonUtilities.objectToJsonField(name));
			nFields++;
		}
		
		if (FlagUnit)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"UNIT\" : " + JsonUtilities.objectToJsonField(unit));
			nFields++;
		}
		
		if (FlagStartColumn)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"START_COLUMN\" : " + JsonUtilities.objectToJsonField(startColumn));
			nFields++;
		}
		
		if (FlagEndColumn)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"END_COLUMN\" : " + JsonUtilities.objectToJsonField(endColumn));
			nFields++;
		}
		
		if (FlagStartRow)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"START_ROW\" : " + JsonUtilities.objectToJsonField(startRow));
			nFields++;
		}
		
		if (FlagEndRow)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"END_ROW\" : " + JsonUtilities.objectToJsonField(endRow));
			nFields++;
		}
		
		if (parameters != null)
		{	
			if (nFields > 0)
				sb.append(",\n\n");

			sb.append(offset + "\t\"PARAMETERS\":\n");
			sb.append(offset + "\t[\n");
			for (int i = 0; i < parameters.size(); i++)
			{	
				sb.append(parameters.get(i).toJSONKeyWord(offset + "\t\t"));			
				if (i < parameters.size()-1) 
					sb.append(",\n");
				sb.append("\n");
			}
			sb.append(offset + "\t]"); 
		}
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
		
	}
	
}
