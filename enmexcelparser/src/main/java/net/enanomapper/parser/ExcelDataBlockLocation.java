package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import net.enanomapper.parser.json.JsonUtilities;
import net.enanomapper.parser.recognition.ExpressionUtils;

public class ExcelDataBlockLocation {

	private Object absoluteLocationValue = null;
	public String blockSectionName = null;

	public ExcelDataLocation location = null;

	public Object rowSubblocks = new Integer(1); // default: only one sub-block
													// = entire block
	public boolean FlagRowSubblocks = false;

	public Object columnSubblocks = new Integer(1); // default: only one
													// sub-block = entire block
	public boolean FlagColumnSubblocks = false;

	public Object subblockSizeRows = new Integer(1);
	public boolean FlagSubblockSizeRows = false;

	public Object subblockSizeColumns = new Integer(1);
	public boolean FlagSubblockSizeColumns = false;

	public List<BlockValueGroup> valueGroups = null;

	public static ExcelDataBlockLocation extractDataBlock(JsonNode node, ExcelParserConfigurator conf) {
		return extractDataBlock(node, null, conf);
	}

	public static ExcelDataBlockLocation extractDataBlock(JsonNode node, String jsonSection,
			ExcelParserConfigurator conf) {
		ExcelDataBlockLocation edbl = new ExcelDataBlockLocation();
		edbl.blockSectionName = jsonSection;

		JsonNode sectionNode;

		if (jsonSection == null)
			sectionNode = node; // The node itself is used
		else {
			sectionNode = node.path(jsonSection);
			if (sectionNode.isMissingNode())
				return null;
		}

		if (sectionNode.path(KEYWORD.LOCATION.name()).isMissingNode()) {
			conf.addError("In JSON section \"" + jsonSection + "\", keyword \"LOCATION\" is missing!");
		} else {
			ExcelDataLocation loc = ExcelDataLocation.extractDataLocation(sectionNode, KEYWORD.LOCATION.name(), conf);
			if (loc != null) {
				if (loc.nErrors == 0)
					edbl.location = loc;
			}
		}

		// ROW_SUBBLOCKS
		JsonNode nd = sectionNode.path(KEYWORD.ROW_SUBBLOCKS.name());
		if (nd.isMissingNode()) {
			conf.addError("In JSON section \"" + jsonSection + "\", keyword \"ROW_SUBBLOCKS\" is missing!");
		} else {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError("In JSON section \"" + jsonSection + "\", keyword \"ROW_SUBBLOCKS\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON section \"" + jsonSection
							+ "\", keyword \"ROW_SUBBLOCKS\" is incorrect expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					edbl.rowSubblocks = obj;
					edbl.FlagRowSubblocks = true;
				}
			}
		}

		// COLUMN_SUBBLOCKS
		nd = sectionNode.path(KEYWORD.COLUMN_SUBBLOCKS.name());
		if (nd.isMissingNode()) {
			conf.addError("In JSON section \"" + jsonSection + "\", keyword \"COLUMN_SUBBLOCKS\" is missing!");
		} else {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError("In JSON section \"" + jsonSection + "\", keyword \"COLUMN_SUBBLOCKS\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON section \"" + jsonSection
							+ "\", keyword \"COLUMN_SUBBLOCKS\" is incorrect expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					edbl.columnSubblocks = obj;
					edbl.FlagColumnSubblocks = true;
				}
			}
		}

		// SUBBLOCK_SIZE_ROWS
		nd = sectionNode.path(KEYWORD.SUBBLOCK_SIZE_ROWS.name());
		if (nd.isMissingNode()) {
			conf.addError("In JSON section \"" + jsonSection + "\", keyword \"SUBBLOCK_SIZE_ROWS\" is missing!");
		} else {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError("In JSON section \"" + jsonSection + "\", keyword \"SUBBLOCK_SIZE_ROWS\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON section \"" + jsonSection
							+ "\", keyword \"SUBBLOCK_SIZE_ROWS\" is incorrect expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					edbl.subblockSizeRows = obj;
					edbl.FlagSubblockSizeRows = true;
				}
			}
		}

		// SUBBLOCK_SIZE_COLUMNS
		nd = sectionNode.path(KEYWORD.SUBBLOCK_SIZE_COLUMNS.name());
		if (nd.isMissingNode()) {
			conf.addError("In JSON section \"" + jsonSection + "\", keyword \"SUBBLOCK_SIZE_COLUMNS\" is missing!");
		} else {
			Object obj = JsonUtilities.extractObject(nd);
			if (obj == null) {
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"SUBBLOCK_SIZE_COLUMNS\" is incorrect!");
			} else {
				String expr_error = ExpressionUtils.checkExpressionAsInteger(obj);
				if (expr_error != null) {
					conf.addError("In JSON section \"" + jsonSection
							+ "\", keyword \"SUBBLOCK_SIZE_COLUMNS\" is incorrect expression: " + expr_error + " --> \""
							+ obj.toString() + "\"");
				} else {
					edbl.subblockSizeColumns = obj;
					edbl.FlagSubblockSizeColumns = true;
				}
			}
		}

		// VALUE_GROUPS
		JsonNode vgNode = sectionNode.path(KEYWORD.VALUE_GROUPS.name());
		if (vgNode.isMissingNode()) {
			conf.addError("In JSON section \"" + jsonSection + "\", keyword \"VALUE_GROUPS\" is missing!");
		} else {
			if (!vgNode.isArray())
				conf.addError("VALUE_GROUPS section is not of type array!");
			else {
				edbl.valueGroups = new ArrayList<BlockValueGroup>();
				for (int i = 0; i < vgNode.size(); i++) {
					BlockValueGroup bvg = BlockValueGroup.extractValueGroup(vgNode.get(i), conf, i);
					edbl.valueGroups.add(bvg);
				}
			}
		}

		return edbl;
	}

	public String toJSONKeyWord(String offset, String blockName, boolean isBlockPartOfArray) {
		int nFields = 0;
		StringBuffer sb = new StringBuffer();

		String secName = blockName;
		if (secName == null)
			secName = blockSectionName;
		if (secName == null)
			secName = "NON_NAME_BLOCK";

		if (!isBlockPartOfArray)
			sb.append(offset + "\"" + secName + "\":\n");
		sb.append(offset + "{\n");

		if (location != null) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(location.toJSONKeyWord(offset + "\t"));
			nFields++;
		}

		if (FlagRowSubblocks) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ROW_SUBBLOCKS\" : " + JsonUtilities.objectToJsonField(rowSubblocks));
			nFields++;
		}

		if (FlagColumnSubblocks) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"COLUMN_SUBBLOCKS\" : " + JsonUtilities.objectToJsonField(columnSubblocks));
			nFields++;
		}

		if (FlagSubblockSizeRows) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"SUBBLOCK_SIZE_ROWS\" : " + JsonUtilities.objectToJsonField(subblockSizeRows));
			nFields++;
		}

		if (FlagSubblockSizeColumns) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"SUBBLOCK_SIZE_COLUMNS\" : " + JsonUtilities.objectToJsonField(subblockSizeColumns));
			nFields++;
		}

		if (valueGroups != null) {
			if (nFields > 0)
				sb.append(",\n\n");

			sb.append(offset + "\t\"VALUE_GROUPS\":\n");
			sb.append(offset + "\t[\n");
			for (int i = 0; i < valueGroups.size(); i++) {
				sb.append(valueGroups.get(i).toJSONKeyWord(offset + "\t\t"));
				if (i < valueGroups.size() - 1)
					sb.append(",\n");
				sb.append("\n");
			}
			sb.append(offset + "\t]");
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
