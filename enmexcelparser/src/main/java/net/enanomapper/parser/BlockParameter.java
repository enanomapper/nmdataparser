package net.enanomapper.parser;

import com.fasterxml.jackson.databind.JsonNode;

import net.enanomapper.parser.ParserConstants.BlockParameterAssign;
import net.enanomapper.parser.json.JsonUtilities;
import net.enanomapper.parser.recognition.ExpressionUtils;

public class BlockParameter {
	
	public static enum Usage {
		PARAMETER, ENDPOINT_TYPE
	}
	
	public String name = null;
	public boolean FlagName = false;

	public BlockParameterAssign assign = BlockParameterAssign.ASSIGN_TO_BLOCK;
	public boolean FlagAssign = false;

	// The positions are relative to the beginning of
	// block (1,1) position, sub-block (x,y) position
	// or 'value' position - in this case negative values make sense as well

	public Object columnPos = new Integer(0);
	public boolean FlagColumnPos = false;

	public Object rowPos = new Integer(0);
	public boolean FlagRowPos = false;

	public boolean fixColumnPosToStartValue = false;
	public boolean FlagFixColumnPosToStartValue = false;

	public boolean fixRowPosToStartValue = false;
	public boolean FlagFixRowPosToStartValue = false;

	public String mapping = null;
	public boolean FlagMapping = false;

	public String unit = null;
	public boolean FlagUnit = false;

	public Object jsonValue = null;
	public boolean FlagJsonValue = false;
	
	public static BlockParameter extractBlockParameter(JsonNode node, ExcelParserConfigurator conf,
			JsonUtilities jsonUtils, int paramNum)
	{
		return extractBlockParameter(node, conf, jsonUtils, paramNum, Usage.PARAMETER);
	}

	public static BlockParameter extractBlockParameter(JsonNode node, ExcelParserConfigurator conf,
			JsonUtilities jsonUtils, int paramNum, Usage paramUse) {
		BlockParameter bp = new BlockParameter();
		
		String errorPrefix = null;
		switch (paramUse) {
		case ENDPOINT_TYPE:
			errorPrefix = "In JSON Section ENDPOINT_TYPE";
			break;
		case PARAMETER:
			errorPrefix = "In JSON Section PARAMETERS[" + (paramNum + 1) + "]";
			break;
		}

		// NAME
		if (node.path(KEYWORD.NAME.name()).isMissingNode()) 
		{
			if (paramUse == Usage.PARAMETER)
				conf.addError(errorPrefix + ", keyword \"NAME\" is missing!");
		} 
		else {
			String keyword = jsonUtils.extractStringKeyword(node, KEYWORD.NAME.name(), false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else {
				bp.name = keyword;
				bp.FlagName = true;
			}
		}

		// JSON_VALUE
		JsonNode nd = node.path(KEYWORD.JSON_VALUE.name());
		if (!nd.isMissingNode()) {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
			} else {
				bp.jsonValue = obj;
				bp.FlagJsonValue = true;
			}
		}

		// ASSIGN
		if (node.path(KEYWORD.ASSIGN.name()).isMissingNode()) {
			if (!bp.FlagJsonValue)
				conf.addError(errorPrefix + ", keyword \"ASSIGN\" is missing!");
		} else {
			String keyword = jsonUtils.extractStringKeyword(node, KEYWORD.ASSIGN.name(), false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else {
				bp.assign = BlockParameterAssign.fromString(keyword);
				bp.FlagAssign = true;
				if (bp.assign == BlockParameterAssign.UNDEFINED)
					conf.addError(errorPrefix + ", keyword \"ASSIGN\" is incorrect or UNDEFINED!");
			}
		}

		// COLUMN_POS
		nd = node.path(KEYWORD.COLUMN_POS.name());
		if (nd.isMissingNode()) {
			if (!bp.FlagJsonValue)
				conf.addError(errorPrefix + ", keyword \"COLUMN_POS\" is missing!");
		} else {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError(errorPrefix + ", keyword \"COLUMN_POS\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError(errorPrefix + 
							", keyword \"COLUMN_POS\" is incorrect expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					bp.columnPos = obj;
					bp.FlagColumnPos = true;
				}
			}
		}

		// ROW_POS
		nd = node.path(KEYWORD.ROW_POS.name());
		if (nd.isMissingNode()) {
			if (!bp.FlagJsonValue)
				conf.addError(errorPrefix + ", keyword \"ROW_POS\" is missing!");
		} else {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError(errorPrefix + ", keyword \"ROW_POS\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError(errorPrefix + 
							", keyword \"ROW_POS\" is incorrect expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					bp.rowPos = obj;
					bp.FlagRowPos = true;
				}
			}
		}

		// MAPPING
		if (!node.path(KEYWORD.MAPPING.name()).isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(node, KEYWORD.MAPPING.name(), false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else {
				bp.mapping = keyword;
				bp.FlagMapping = true;
			}
		}

		// FIX_COLUMN_POS_TO_START_VALUE
		if (!node.path(KEYWORD.FIX_COLUMN_POS_TO_START_VALUE.name()).isMissingNode()) {
			Boolean b = jsonUtils.extractBooleanKeyword(node, KEYWORD.FIX_COLUMN_POS_TO_START_VALUE.name(), false);
			if (b == null)
				conf.addError(jsonUtils.getError());
			else {
				bp.fixColumnPosToStartValue = b;
				bp.FlagFixColumnPosToStartValue = true;
			}
		}

		// FIX_ROW_POS_TO_START_VALUE
		if (!node.path(KEYWORD.FIX_ROW_POS_TO_START_VALUE.name()).isMissingNode()) {
			Boolean b = jsonUtils.extractBooleanKeyword(node, KEYWORD.FIX_ROW_POS_TO_START_VALUE.name(), false);
			if (b == null)
				conf.addError(jsonUtils.getError());
			else {
				bp.fixRowPosToStartValue = b;
				bp.FlagFixRowPosToStartValue = true;
			}
		}

		// UNIT
		if (!node.path(KEYWORD.UNIT.name()).isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(node, KEYWORD.UNIT.name(), false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else {
				bp.unit = keyword;
				bp.FlagUnit = true;
			}
		}

		return bp;
	}

	public String toJSONKeyWord(String offset) {
		int nFields = 0;
		StringBuffer sb = new StringBuffer();

		sb.append(offset + "{\n");

		if (FlagName) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"NAME\" : " + JsonUtilities.objectToJsonField(name));
			nFields++;
		}

		if (FlagAssign) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ASSIGN\" : \"" + assign.toString() + "\"");
			nFields++;
		}

		if (FlagColumnPos) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"COLUMN_POS\" : " + JsonUtilities.objectToJsonField(columnPos));
			nFields++;
		}

		if (FlagRowPos) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ROW_POS\" : " + JsonUtilities.objectToJsonField(rowPos));
			nFields++;
		}

		if (FlagFixColumnPosToStartValue) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"FIX_COLUMN_POS_TO_START_VALUE\" : " + fixColumnPosToStartValue);
			nFields++;
		}

		if (FlagFixRowPosToStartValue) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"FIX_ROW_POS_TO_START_VALUE\" : " + fixRowPosToStartValue);
			nFields++;
		}

		if (FlagMapping) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"MAPPING\" : " + JsonUtilities.objectToJsonField(mapping));
			nFields++;
		}

		if (FlagUnit) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"UNIT\" : " + JsonUtilities.objectToJsonField(unit));
			nFields++;
		}

		if (FlagJsonValue) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"JSON_VALUE\" : " + JsonUtilities.objectToJsonField(jsonValue));
			nFields++;
		}

		if (nFields > 0)
			sb.append("\n");

		sb.append(offset + "}");

		return sb.toString();
	}
}
