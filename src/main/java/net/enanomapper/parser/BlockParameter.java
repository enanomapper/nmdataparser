package net.enanomapper.parser;

import org.codehaus.jackson.JsonNode;

import net.enanomapper.parser.ParserConstants.BlockParameterAssign;
import net.enanomapper.parser.ParserConstants.IterationAccess;
import net.enanomapper.parser.json.JsonUtilities;
import net.enanomapper.parser.recognition.ExpressionUtils;

public class BlockParameter 
{	
	public String name = null;
	public boolean FlagName = false;
	
	public BlockParameterAssign assign = BlockParameterAssign.ASSIGN_TO_BLOCK;
	public boolean FlagAssign = false;
	
	//The positions are relative to the beginning of 
	//block (0,0), sub-block (0,0) or value - here negative values make sense as well 
	public Object columnPos = new Integer(0); 
	public boolean FlagColumnPos = false;
	
	public Object rowPos = new Integer(0);
	public boolean FlagRowPos = false;	
	
	
	public static BlockParameter extractBlockParameter(JsonNode node, ExcelParserConfigurator conf, 
											JsonUtilities jsonUtils, int paramNum )
	{
		BlockParameter bp = new BlockParameter();
		
		//NAME
		if (node.path("NAME").isMissingNode())
		{
			conf.configErrors.add("In JSON Section PARAMETERS[" + (paramNum + 1) +  "], keyword \"NAME\" is missing!");
		}
		else
		{	
			String keyword =  jsonUtils.extractStringKeyword(node, "NAME", false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
			{	
				bp.name = keyword;
				bp.FlagName = true;
			}
		}
		
		//ASSIGN
		if (node.path("ASSIGN").isMissingNode())
		{
			conf.configErrors.add("In JSON Section PARAMETERS[" + (paramNum + 1) +  "], keyword \"ASSIGN\" is missing!");
		}
		else
		{	
			String keyword =  jsonUtils.extractStringKeyword(node, "ASSIGN", false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
			{	
				bp.assign = BlockParameterAssign.fromString(keyword);
				bp.FlagAssign = true;
				if (bp.assign == BlockParameterAssign.UNDEFINED)
					conf.configErrors.add("In JSON Section PARAMETERS[" + (paramNum + 1) + 
							"], keyword \"ASSIGN\" is incorrect or UNDEFINED!");
			}
		}
		
		
		//COLUMN_POS
		JsonNode nd = node.path("COLUMN_POS");
		if (nd.isMissingNode())
		{
			conf.configErrors.add("In JSON Section PARAMETERS[" + (paramNum + 1) + 
					"], keyword \"COLUMN_POS\" is missing!");
		}
		else
		{	
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null)
			{
				conf.configErrors.add("In JSON Section PARAMETERS[" + (paramNum + 1) + 
						"], keyword \"COLUMN_POS\" is incorrect!");
			}
			else
			{	
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null)
				{
					conf.configErrors.add("In JSON Section PARAMETERS[" + (paramNum + 1) + 
							"], keyword \"COLUMN_POS\" is incorrect expression: " 
							+ expr_error + " --> \"" + obj.toString() + "\"");
				}
				else
				{	
					bp.columnPos = obj;
					bp.FlagColumnPos = true;
				}	
			}	
		}
		
		//ROW_POS
		nd = node.path("ROW_POS");
		if (nd.isMissingNode())
		{
			conf.configErrors.add("In JSON Section PARAMETERS[" + (paramNum + 1) + 
					"], keyword \"ROW_POS\" is missing!");
		}
		else
		{	
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null)
			{
				conf.configErrors.add("In JSON Section PARAMETERS[" + (paramNum + 1) + 
						"], keyword \"ROW_POS\" is incorrect!");
			}
			else
			{	
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null)
				{
					conf.configErrors.add("In JSON Section PARAMETERS[" + (paramNum + 1) + 
							"], keyword \"ROW_POS\" is incorrect expression: " 
							+ expr_error + " --> \"" + obj.toString() + "\"");
				}
				else
				{	
					bp.rowPos = obj;
					bp.FlagRowPos = true;
				}	
			}	
		}

		
		return bp;
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
		
		if (FlagAssign)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"ASSIGN\" : \"" + assign.toString() + "\"");
			nFields++;
		}
		
		if (FlagColumnPos)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"COLUMN_POS\" : " + JsonUtilities.objectToJsonField(columnPos));
			nFields++;
		}
		
		if (FlagRowPos)
		{
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"ROW_POS\" : " + JsonUtilities.objectToJsonField(rowPos));
			nFields++;
		}
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
	}	
}
