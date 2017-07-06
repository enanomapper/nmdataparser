package net.enanomapper.parser;

import com.fasterxml.jackson.databind.JsonNode;

import net.enanomapper.parser.ParserConstants.BlockParameterAssign;
import net.enanomapper.parser.json.JsonUtilities;
import net.enanomapper.parser.recognition.ExpressionUtils;

public class BlockParameter {
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
			JsonUtilities jsonUtils, int paramNum) {
		BlockParameter bp = new BlockParameter();

		// NAME
		if (node.path("NAME").isMissingNode()) {
			conf.addError("In JSON Section PARAMETERS[" + (paramNum + 1) + "], keyword \"NAME\" is missing!");
		} else {
			String keyword = jsonUtils.extractStringKeyword(node, "NAME", false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else {
				bp.name = keyword;
				bp.FlagName = true;
			}
		}

		// JSON_VALUE
		JsonNode nd = node.path("JSON_VALUE");
		if (!nd.isMissingNode()) {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
			} else {
				bp.jsonValue = obj;
				bp.FlagJsonValue = true;
			}
		}

		// ASSIGN
		if (node.path("ASSIGN").isMissingNode()) {
			if (!bp.FlagJsonValue)
				conf.addError("In JSON Section PARAMETERS[" + (paramNum + 1) + "], keyword \"ASSIGN\" is missing!");
		} else {
			String keyword = jsonUtils.extractStringKeyword(node, "ASSIGN", false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else {
				bp.assign = BlockParameterAssign.fromString(keyword);
				bp.FlagAssign = true;
				if (bp.assign == BlockParameterAssign.UNDEFINED)
					conf.addError("In JSON Section PARAMETERS[" + (paramNum + 1)
							+ "], keyword \"ASSIGN\" is incorrect or UNDEFINED!");
			}
		}

		// COLUMN_POS
		nd = node.path("COLUMN_POS");
		if (nd.isMissingNode()) {
			if (!bp.FlagJsonValue)
				conf.addError("In JSON Section PARAMETERS[" + (paramNum + 1) + "], keyword \"COLUMN_POS\" is missing!");
		} else {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError(
						"In JSON Section PARAMETERS[" + (paramNum + 1) + "], keyword \"COLUMN_POS\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON Section PARAMETERS[" + (paramNum + 1)
							+ "], keyword \"COLUMN_POS\" is incorrect expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					bp.columnPos = obj;
					bp.FlagColumnPos = true;
				}
			}
		}

		// ROW_POS
		nd = node.path("ROW_POS");
		if (nd.isMissingNode()) {
			if (!bp.FlagJsonValue)
				conf.addError("In JSON Section PARAMETERS[" + (paramNum + 1) + "], keyword \"ROW_POS\" is missing!");
		} else {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError("In JSON Section PARAMETERS[" + (paramNum + 1) + "], keyword \"ROW_POS\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON Section PARAMETERS[" + (paramNum + 1)
							+ "], keyword \"ROW_POS\" is incorrect expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					bp.rowPos = obj;
					bp.FlagRowPos = true;
				}
			}
		}

		// MAPPING
		if (!node.path("MAPPING").isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(node, "MAPPING", false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else {
				bp.mapping = keyword;
				bp.FlagMapping = true;
			}
		}

		// FIX_COLUMN_POS_TO_START_VALUE
		if (!node.path("FIX_COLUMN_POS_TO_START_VALUE").isMissingNode()) {
			Boolean b = jsonUtils.extractBooleanKeyword(node, "FIX_COLUMN_POS_TO_START_VALUE", false);
			if (b == null)
				conf.addError(jsonUtils.getError());
			else {
				bp.fixColumnPosToStartValue = b;
				bp.FlagFixColumnPosToStartValue = true;
			}
		}

		// FIX_ROW_POS_TO_START_VALUE
		if (!node.path("FIX_ROW_POS_TO_START_VALUE").isMissingNode()) {
			Boolean b = jsonUtils.extractBooleanKeyword(node, "FIX_ROW_POS_TO_START_VALUE", false);
			if (b == null)
				conf.addError(jsonUtils.getError());
			else {
				bp.fixRowPosToStartValue = b;
				bp.FlagFixRowPosToStartValue = true;
			}
		}

		// UNIT
		if (!node.path("UNIT").isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(node, "UNIT", false);
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
