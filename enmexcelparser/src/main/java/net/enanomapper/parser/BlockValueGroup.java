package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import net.enanomapper.parser.BlockParameter.Usage;
import net.enanomapper.parser.ParserConstants.BlockParameterAssign;
import net.enanomapper.parser.json.JsonUtilities;
import net.enanomapper.parser.recognition.ExpressionUtils;

/**
 * This class defines how a group of values (with associated parameters for each
 * value) is extracted from particular sub-block
 * 
 * @author nick
 *
 */
public class BlockValueGroup {
	public boolean isActive = true;
	public boolean FlagIsActive = false;

	// Value group name is used as endpoint if FlagEndpointAssign = false
	public String name = null;
	public boolean FlagName = false;

	// Assigning of endpoint name to particular element (block/sub-block/value)
	// This is analogous to the block parameters assignment
	public BlockParameterAssign endpointAssign = BlockParameterAssign.UNDEFINED;
	public boolean FlagEndpointAssign = false;

	public Object endpointColumnPos = new Integer(0);
	public boolean FlagEndpointColumnPos = false;

	public Object endpointRowPos = new Integer(0);
	public boolean FlagEndpointRowPos = false;
	
	public boolean fixEndpointColumnPosToStartValue = false;
	public boolean FlagFixEndpointColumnPosToStartValue = false;

	public boolean fixEndpointRowPosToStartValue = false;
	public boolean FlagFixEndpointRowPosToStartValue = false;

	public String endpointMapping = null;
	public boolean FlagEndpointMapping = false;

	public boolean addValueGroupToEndpointName = false;
	public boolean FlagAddValueGroupToEndpointName = false;

	public boolean addValueGroupAsPrefix = false;
	public boolean FlagAddValueGroupAsPrefix = false;

	public String separator = " "; // used when adding name to endpoint
	public boolean FlagSeparator = false;

	public BlockParameter unit = null; //unit defined as a BlockParameter
	public String unitString = null; //unit defined as a string directly from JSON
	
	// Values definitions are in the context of sub-block
	public Object startColumn = null;
	public boolean FlagStartColumn = false;

	public Object endColumn = null;
	public boolean FlagEndColumn = false;

	public Object startRow = null;
	public boolean FlagStartRow = false;

	public Object endRow = null;
	public boolean FlagEndRow = false;

	// The shifts are relative to the corresponding value position.
	// 'value' by default is considered to be lo-value, rich-value or text-value
		
	/*
	public Object upValueColumnShift = new Integer(0);
	public boolean FlagUpValueColumnShift = false;

	public Object upValueRowShift = new Integer(0);
	public boolean FlagUpValueRowShift = false;

	public Object upQualifierColumnShift = new Integer(0);
	public boolean FlagUpQualifierColumnShift = false;

	public Object upQualifierRowShift = new Integer(0);
	public boolean FlagUpQualifierRowShift = false;
	*/

	public Object errorColumnShift = new Integer(0);
	public boolean FlagErrorColumnShift = false;

	public Object errorRowShift = new Integer(0);
	public boolean FlagErrorRowShift = false;
	
	public List<BlockParameter> parameters = null;
	
	public BlockParameter endpointType = null; //endpointType defined as a BlockParameter
	public String endpointTypeString = null; //endpointType defined as a string directly from JSON
		
	public BlockParameter valueQualifier = null; //value Qualifier defined as a BlockParameter
	public String valueQualifierString = null; //value Qualifier defined as a string directly from JSON
	
	public BlockParameter errorQualifier = null; //error Qualifier defined as a BlockParameter
	public String errorQualifierString = null; //error Qualifier defined as a string directly from JSON
	
	public BlockParameter substanceRecordMap = null; //Substance Record Map defined as a BlockParameter
	public String substanceRecordMapString = null; //Substance Record defined as a string directly from JSON
	

	public static BlockValueGroup extractValueGroup(JsonNode node, ExcelParserConfigurator conf, int valueGroupNum) {
		BlockValueGroup bvg = new BlockValueGroup();

		JsonUtilities jsonUtils = new JsonUtilities();
		String keyword;

		//IS_ACTIVE
		if (!node.path("IS_ACTIVE").isMissingNode()) {
			Boolean boolValue = jsonUtils.extractBooleanKeyword(node, "IS_ACTIVE", false);
			if (boolValue == null)
				conf.addError(jsonUtils.getError());
			else {
				bvg.isActive = boolValue;
				bvg.FlagIsActive = true;
			}
		}
		
		// NAME
		if (!node.path("NAME").isMissingNode()) {
			keyword = jsonUtils.extractStringKeyword(node, "NAME", false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else {
				bvg.name = keyword;
				bvg.FlagName = true;
			}
		}

		// ENDPOINT_ASSIGN
		if (!node.path("ENDPOINT_ASSIGN").isMissingNode()) {
			keyword = jsonUtils.extractStringKeyword(node, "ENDPOINT_ASSIGN", false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else {
				bvg.endpointAssign = BlockParameterAssign.fromString(keyword);
				if (bvg.endpointAssign == BlockParameterAssign.UNDEFINED)
					conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
							+ "], keyword \"ENDPOINT_ASSIGN\" is incorrect or UNDEFINED!");
				else
					bvg.FlagEndpointAssign = true;
			}
		}

		// ENDPOINT_COLUMN_POS
		JsonNode nd = node.path("ENDPOINT_COLUMN_POS");
		if (nd.isMissingNode()) {
			if (bvg.FlagEndpointAssign)
				conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
						+ "], keyword \"ENDPOINT_COLUMN_POS\" is required when ENDPOINT_ASSIGN is set!");
		} else {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
						+ "], keyword \"ENDPOINT_COLUMN_POS\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
							+ "], keyword \"ENDPOINT_COLUMN_POS\" is incorrect expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					bvg.endpointColumnPos = obj;
					bvg.FlagEndpointColumnPos = true;
				}
			}
		}

		// ENDPOINT_ROW_POS
		nd = node.path("ENDPOINT_ROW_POS");
		if (nd.isMissingNode()) {
			if (bvg.FlagEndpointAssign)
				conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
						+ "], keyword \"ENDPOINT_ROW_POS\" is required when ENDPOINT_ASSIGN is set!");
		} else {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
						+ "], keyword \"ENDPOINT_ROW_POS\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
							+ "], keyword \"ENDPOINT_ROW_POS\" is incorrect expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					bvg.endpointRowPos = obj;
					bvg.FlagEndpointRowPos = true;
				}
			}
		}

		// FIX_ENDPOINT_COLUMN_POS_TO_START_VALUE
		if (!node.path("FIX_ENDPOINT_COLUMN_POS_TO_START_VALUE").isMissingNode()) {
			Boolean b = jsonUtils.extractBooleanKeyword(node, "FIX_ENDPOINT_COLUMN_POS_TO_START_VALUE", false);
			if (b == null)
				conf.addError(jsonUtils.getError());
			else {
				bvg.fixEndpointColumnPosToStartValue = b;
				bvg.FlagFixEndpointColumnPosToStartValue = true;
			}
		}

		// FIX_ENDPOINT_ROW_POS_TO_START_VALUE
		if (!node.path("FIX_ENDPOINT_ROW_POS_TO_START_VALUE").isMissingNode()) {
			Boolean b = jsonUtils.extractBooleanKeyword(node, "FIX_ENDPOINT_ROW_POS_TO_START_VALUE", false);
			if (b == null)
				conf.addError(jsonUtils.getError());
			else {
				bvg.fixEndpointRowPosToStartValue = b;
				bvg.FlagFixEndpointRowPosToStartValue = true;
			}
		}

		// ENDPOINT_MAPPING
		if (!node.path("ENDPOINT_MAPPING").isMissingNode()) {
			keyword = jsonUtils.extractStringKeyword(node, "ENDPOINT_MAPPING", false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else {
				bvg.endpointMapping = keyword;
				bvg.FlagEndpointMapping = true;
			}
		}

		// ADD_VALUE_GROUP_TO_ENDPOINT_NAME
		if (!node.path("ADD_VALUE_GROUP_TO_ENDPOINT_NAME").isMissingNode()) {
			Boolean b = jsonUtils.extractBooleanKeyword(node, "ADD_VALUE_GROUP_TO_ENDPOINT_NAME", false);
			if (b == null)
				conf.addError(jsonUtils.getError());
			else {
				bvg.addValueGroupToEndpointName = b;
				bvg.FlagAddValueGroupToEndpointName = true;
			}
		}

		// ADD_VALUE_GROUP_AS_PREFIX
		if (!node.path("ADD_VALUE_GROUP_AS_PREFIX").isMissingNode()) {
			Boolean b = jsonUtils.extractBooleanKeyword(node, "ADD_VALUE_GROUP_AS_PREFIX", false);
			if (b == null)
				conf.addError(jsonUtils.getError());
			else {
				bvg.addValueGroupAsPrefix = b;
				bvg.FlagAddValueGroupAsPrefix = true;
			}
		}

		// SEPARATOR
		if (!node.path("SEPARATOR").isMissingNode()) {
			keyword = jsonUtils.extractStringKeyword(node, "SEPARATOR", false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else {
				bvg.separator = keyword;
				bvg.FlagSeparator = true;
			}
		}

		// UNIT
		nd = node.path("UNIT");
		if (!nd.isMissingNode()) 
		{
			if (nd.isTextual()) {
				//Extracting as a string
				bvg.unitString = nd.asText();
			}
			else if (nd.isObject()) {
				//Extracting as a block parameter
				BlockParameter bp = BlockParameter.extractBlockParameter(nd, conf, jsonUtils, -1, Usage.UNIT);
				bvg.unit = bp;
			}
			else 
			{
				conf.addError("In Value group, UNIT section is not TEXTUAL or parameter-style object!");				
			}	
		}

		// START_COLUMN
		nd = node.path("START_COLUMN");
		if (!nd.isMissingNode()) {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
						+ "], keyword \"START_COLUMN\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
							+ "], keyword \"START_COLUMN\" is incorrect! expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					bvg.startColumn = obj;
					bvg.FlagStartColumn = true;
				}
			}
		}

		// END_COLUMN
		nd = node.path("END_COLUMN");
		if (!nd.isMissingNode()) {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
						+ "], keyword \"END_COLUMN\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
							+ "], keyword \"END_COLUMN\" is incorrect! expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					bvg.endColumn = obj;
					bvg.FlagEndColumn = true;
				}
			}
		}

		// START_ROW
		nd = node.path("START_ROW");
		if (!nd.isMissingNode()) {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
						+ "], keyword \"START_ROW\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
							+ "], keyword \"START_ROW\" is incorrect! expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					bvg.startRow = obj;
					bvg.FlagStartRow = true;
				}
			}
		}

		// END_ROW
		nd = node.path("END_ROW");
		if (!nd.isMissingNode()) {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
						+ "], keyword \"END_ROW\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
							+ "], keyword \"END_ROW\" is incorrect! expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					bvg.endRow = obj;
					bvg.FlagEndRow = true;
				}
			}
		}

		// ERROR_COLUMN_SHIFT
		nd = node.path("ERROR_COLUMN_SHIFT");
		if (!nd.isMissingNode()) {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
						+ "], keyword \"ERROR_COLUMN_SHIFT\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
							+ "], keyword \"ERROR_COLUMN_SHIFT\" is incorrect! expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					bvg.errorColumnShift = obj;
					bvg.FlagErrorColumnShift = true;
				}
			}
		}

		// ERROR_ROW_SHIFT
		nd = node.path("ERROR_ROW_SHIFT");
		if (!nd.isMissingNode()) {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
						+ "], keyword \"ERROR_ROW_SHIFT\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON section \"VALUE_GROUPS\", element[" + (valueGroupNum + 1)
							+ "], keyword \"ERROR_ROW_SHIFT\" is incorrect! expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					bvg.errorRowShift = obj;
					bvg.FlagErrorRowShift = true;
				}
			}
		}

		// PARAMETERS
		JsonNode parNode = node.path("PARAMETERS");
		if (!parNode.isMissingNode()) {
			if (!parNode.isArray()) {
				conf.addError("PARAMETERS section is not of type array!");
			}

			bvg.parameters = new ArrayList<BlockParameter>();

			for (int i = 0; i < parNode.size(); i++) {
				BlockParameter bp = BlockParameter.extractBlockParameter(parNode.get(i), conf, jsonUtils, i);
				bvg.parameters.add(bp);
			}
		}
		
		//ENDPOINT_TYPE
		nd = node.path("ENDPOINT_TYPE");
		if (!nd.isMissingNode()) 
		{
			if (nd.isTextual()) {
				//Extracting as a string
				bvg.endpointTypeString = nd.asText();
			}
			else if (nd.isObject()) {
				//Extracting as a block parameter
				BlockParameter bp = BlockParameter.extractBlockParameter(nd, conf, jsonUtils, -1, Usage.ENDPOINT_TYPE);
				bvg.endpointType = bp;
			}
			else 
			{
				conf.addError("In Value group, ENDPOINT_TYPE section is not TEXTUAL or parameter-style object!");				
			}	
		}
		
		//VALUE_QUALIFIER
		nd = node.path("VALUE_QUALIFIER");
		if (!nd.isMissingNode()) 
		{
			if (nd.isTextual()) {
				//Extracting as a string
				bvg.valueQualifierString = nd.asText();
			}
			else if (nd.isObject()) {
				//Extracting as a block parameter
				BlockParameter bp = BlockParameter.extractBlockParameter(nd, conf, jsonUtils, -1, Usage.ENDPOINT_QUALIFIER);
				bvg.valueQualifier = bp;
			}
			else 
			{
				conf.addError("In Value group, ENDPOINT_QUALIFIER section is not TEXTUAL or parameter-style object!");				
			}	
		}
		
		//ERROR_QUALIFIER
		nd = node.path("ERROR_QUALIFIER");
		if (!nd.isMissingNode()) 
		{
			if (nd.isTextual()) {
				//Extracting as a string
				bvg.errorQualifierString = nd.asText();
			}
			else if (nd.isObject()) {
				//Extracting as a block parameter
				BlockParameter bp = BlockParameter.extractBlockParameter(nd, conf, jsonUtils, -1, Usage.ERROR_QUALIFIER);
				bvg.errorQualifier = bp;
			}
			else 
			{
				conf.addError("In Value group, ERROR_QUALIFIER section is not TEXTUAL or parameter-style object!");				
			}	
		}
		
		//SUBSTANCE_RECORD_MAP
		nd = node.path(KEYWORD.SUBSTANCE_RECORD_MAP.name());
		if (!nd.isMissingNode()) 
		{
			if (nd.isTextual()) {
				//Extracting as a string
				bvg.substanceRecordMapString = nd.asText();
			}
			else if (nd.isObject()) {
				//Extracting as a block parameter
				BlockParameter bp = BlockParameter.extractBlockParameter(nd, conf, jsonUtils, -1, Usage.ERROR_QUALIFIER);
				bvg.substanceRecordMap = bp;
			}
			else 
			{
				conf.addError("In Value group, SUBSTANCE_RECORD_MAP section is not TEXTUAL or parameter-style object!");				
			}	
		}

		return bvg;
	}

	public String toJSONKeyWord(String offset) {
		int nFields = 0;
		StringBuffer sb = new StringBuffer();

		sb.append(offset + "{\n");

		if (FlagIsActive) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"IS_ACTIVE\" : " + isActive);
			nFields++;
		}
		
		if (FlagName) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"NAME\" : " + JsonUtilities.objectToJsonField(name));
			nFields++;
		}

		if (FlagEndpointAssign) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ENDPOINT_ASSIGN\" : \"" + endpointAssign.toString() + "\"");
			nFields++;
		}

		if (FlagEndpointColumnPos) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ENDPOINT_COLUMN_POS\" : " + JsonUtilities.objectToJsonField(endpointColumnPos));
			nFields++;
		}

		if (FlagEndpointRowPos) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ENDPOINT_ROW_POS\" : " + JsonUtilities.objectToJsonField(endpointRowPos));
			nFields++;
		}

		if (FlagFixEndpointColumnPosToStartValue) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"FIX_ENDPOINT_COLUMN_POS_TO_START_VALUE\" : " + fixEndpointColumnPosToStartValue);
			nFields++;
		}

		if (FlagFixEndpointRowPosToStartValue) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"FIX_ENDPOINT_ROW_POS_TO_START_VALUE\" : " + fixEndpointRowPosToStartValue);
			nFields++;
		}

		if (FlagEndpointMapping) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ENDPOINT_MAPPING\" : " + JsonUtilities.objectToJsonField(endpointMapping));
			nFields++;
		}

		if (FlagAddValueGroupToEndpointName) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ADD_VALUE_GROUP_TO_ENDPOINT_NAME\" : " + addValueGroupToEndpointName);
			nFields++;
		}

		if (FlagAddValueGroupAsPrefix) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ADD_VALUE_GROUP_AS_PREFIX\" : " + addValueGroupAsPrefix);
			nFields++;
		}

		if (FlagSeparator) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"SEPARATOR\" : " + JsonUtilities.objectToJsonField(separator));
			nFields++;
		}
		
		if (unitString != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"UNIT\" : " + JsonUtilities.objectToJsonField(unitString));
		}
		else
		{
			if (unit != null)
			{
				if (nFields > 0)
					sb.append(",\n");
				sb.append(offset + "\t\"UNIT\" : \n" );
				sb.append(unit.toJSONKeyWord(offset + "\t\t"));
			}
		}

		if (FlagStartColumn) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"START_COLUMN\" : " + JsonUtilities.objectToJsonField(startColumn));
			nFields++;
		}

		if (FlagEndColumn) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"END_COLUMN\" : " + JsonUtilities.objectToJsonField(endColumn));
			nFields++;
		}

		if (FlagStartRow) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"START_ROW\" : " + JsonUtilities.objectToJsonField(startRow));
			nFields++;
		}

		if (FlagEndRow) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"END_ROW\" : " + JsonUtilities.objectToJsonField(endRow));
			nFields++;
		}

		if (FlagErrorColumnShift) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ERROR_COLUMN_SHIFT\" : " + JsonUtilities.objectToJsonField(errorColumnShift));
			nFields++;
		}

		if (FlagErrorRowShift) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ERROR_ROW_SHIFT\" : " + JsonUtilities.objectToJsonField(errorRowShift));
			nFields++;
		}
		
		if (endpointTypeString != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ENDPOINT_TYPE\" : " + JsonUtilities.objectToJsonField(endpointTypeString));
		}
		else
		{
			if (endpointType != null)
			{
				if (nFields > 0)
					sb.append(",\n");
				sb.append(offset + "\t\"ENDPOINT_TYPE\" : \n" );
				sb.append(endpointType.toJSONKeyWord(offset + "\t\t"));
			}
		}
		
		if (valueQualifierString != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"VALUE_QUALIFIER\" : " + JsonUtilities.objectToJsonField(valueQualifierString));
		}
		else
		{
			if (valueQualifier != null)
			{
				if (nFields > 0)
					sb.append(",\n");
				sb.append(offset + "\t\"VALUE_QUALIFIER\" : \n" );
				sb.append(valueQualifier.toJSONKeyWord(offset + "\t\t"));
			}
		}
		
		if (errorQualifierString != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ERROR_QUALIFIER\" : " + JsonUtilities.objectToJsonField(errorQualifierString));
		}
		else
		{
			if (errorQualifier != null)
			{
				if (nFields > 0)
					sb.append(",\n");
				sb.append(offset + "\t\"ERROR_QUALIFIER\" : \n" );
				sb.append(errorQualifier.toJSONKeyWord(offset + "\t\t"));
			}
		}
		
		if (substanceRecordMapString != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"SUBSTANCE_RECORD_MAP\" : " + JsonUtilities.objectToJsonField(substanceRecordMapString));
		}
		else
		{
			if (substanceRecordMap != null)
			{
				if (nFields > 0)
					sb.append(",\n");
				sb.append(offset + "\t\"SUBSTANCE_RECORD_MAP\" : \n" );
				sb.append(substanceRecordMap.toJSONKeyWord(offset + "\t\t"));
			}
		}

		if (parameters != null) {
			if (nFields > 0)
				sb.append(",\n\n");

			sb.append(offset + "\t\"PARAMETERS\":\n");
			sb.append(offset + "\t[\n");
			for (int i = 0; i < parameters.size(); i++) {
				sb.append(parameters.get(i).toJSONKeyWord(offset + "\t\t"));
				if (i < parameters.size() - 1)
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
