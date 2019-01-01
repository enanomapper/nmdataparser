package net.enanomapper.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import net.enanomapper.parser.ParserConstants.DataInterpretation;
import net.enanomapper.parser.ParserConstants.DataType;
import net.enanomapper.parser.ParserConstants.IterationAccess;
import net.enanomapper.parser.ParserConstants.Recognition;
import net.enanomapper.parser.json.JsonUtilities;

public class ExcelDataLocation {
	private boolean FlagExtractValueQualifier = false;
	private boolean FlagExtractAsRichValue = false;
	private int parallelSheetIndex = -1; // This is not the sheetIndex. This is
											// the index within array
											// GenericExcelParser.parallelSheets[]

	private Object absoluteLocationValue = null;
	private Object jsonValue = null;
	private String jsonRepositoryKey = null;
	private String variableKey = null;

	public boolean sourceCombination = false;
	public boolean FlagSourceCombination = false;

	public boolean isArray = false;
	public boolean FlagIsArray = false;

	public boolean trimArray = false; // The array size/dimensions are trimmed
										// according to the non null element
										// with maximal dimension indices
	public boolean FlagTrimArray = false;

	public int nErrors = 0;
	public String sectionName = "";

	public DataType dataType = DataType.CELL;
	public boolean FlagDataType = false;

	public Recognition recognition = Recognition.BY_INDEX;
	public boolean FlagRecognition = false;

	public DataInterpretation dataInterpretation = DataInterpretation.DEFAULT;
	public boolean FlagDataInterpretation = false;

	public String dateFormat = "yyyy-MM-dd"; // default date format
	public boolean FlagDateFormat = false;

	// public CellType cellType = CellType.STRING;
	// public boolean FlagCellType = false;

	public IterationAccess iteration = IterationAccess.ROW_SINGLE;
	public boolean FlagIteration = false;

	public boolean allowEmpty = true;
	public boolean FlagAllowEmpty = false;

	public int sheetIndex = 0;
	public boolean FlagSheetIndex = false;

	public String sheetName = null;
	public boolean FlagSheetName = false;

	public int columnIndex = 0;
	public boolean FlagColumnIndex = false;

	public String columnName = null;
	public boolean FlagColumnName = false;

	public int rowIndex = 0;
	public boolean FlagRowIndex = false;

	public String rowName = null;
	public boolean FlagRowName = false;

	public int columnIndices[] = null; // Used only in mode
										// IterationAccess.COMBINATION

	public int rowIndices[] = null; // Used only in mode
									// IterationAccess.COMBINATION

	public String variableKeys[] = null; // Used only in mode
											// IterationAccess.COMBINATION

	// This is a recursive approach used for other fields defined by excel data
	// location
	public Map<String, ExcelDataLocation> otherLocationFields = null;

	public static ExcelDataLocation extractDataLocation(JsonNode node, ExcelParserConfigurator conf) {
		return extractDataLocation(node, null, conf, null, true);
	}

	public static ExcelDataLocation extractDataLocation(JsonNode node, String jsonSection,
			ExcelParserConfigurator conf) {
		return extractDataLocation(node, jsonSection, conf, null, true);
	}

	public static ExcelDataLocation extractDataLocation(JsonNode node, String jsonSection, ExcelParserConfigurator conf,
			String otherLocationFieldNames[], boolean allowShortJSONValueDefinition) {
		// Error messages are stored globally in 'conf' variable and are
		// counted locally in return variable 'loc'

		JsonNode sectionNode;

		if (jsonSection == null)
			sectionNode = node; // The node itself is used
		else {
			sectionNode = node.path(jsonSection);
			if (sectionNode.isMissingNode())
				return null;
		}
		JsonUtilities jsonUtils = new JsonUtilities();

		ExcelDataLocation loc = new ExcelDataLocation();
		loc.sectionName = jsonSection;

		if (allowShortJSONValueDefinition) {
			// Short JSON value definitions
			// The textual and numeric values are interpreted as data locations
			// with ITERATION = JSON_VALUE
			if (sectionNode.isTextual()) {
				loc.iteration = ParserConstants.IterationAccess.JSON_VALUE;
				loc.jsonValue = sectionNode.textValue();
				return loc;
			}
			if (sectionNode.isNumber()) {
				loc.iteration = ParserConstants.IterationAccess.JSON_VALUE;
				loc.jsonValue = sectionNode.numberValue();
				return loc;
			}
		}

		// SOURCE_COMBINATION
		if (!sectionNode.path(KEYWORD.SOURCE_COMBINATION.name()).isMissingNode()) {
			Boolean b = jsonUtils.extractBooleanKeyword(sectionNode, "SOURCE_COMBINATION", false);
			if (b == null) {
				conf.addError("In JSON section \"" + jsonSection + "\", keyword \"SOURCE_COMBINATION\" : "
						+ jsonUtils.getError());
				loc.nErrors++;
			} else {
				loc.sourceCombination = b;
				loc.FlagSourceCombination = true;
			}
		}

		// IS_ARRAY
		if (!sectionNode.path(KEYWORD.IS_ARRAY.name()).isMissingNode()) {
			Boolean b = jsonUtils.extractBooleanKeyword(sectionNode, "IS_ARRAY", false);
			if (b == null) {
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"IS_ARRAY\" : " + jsonUtils.getError());
				loc.nErrors++;
			} else {
				loc.isArray = b;
				loc.FlagIsArray = true;
			}
		}

		// TRIM_ARRAY
		if (!sectionNode.path(KEYWORD.TRIM_ARRAY.name()).isMissingNode()) {
			Boolean b = jsonUtils.extractBooleanKeyword(sectionNode, KEYWORD.TRIM_ARRAY.name(), false);
			if (b == null) {
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"TRIM_ARRAY\" : " + jsonUtils.getError());
				loc.nErrors++;
			} else {
				loc.trimArray = b;
				loc.FlagTrimArray = true;
			}
		}

		// ITERATION
		if (sectionNode.path(KEYWORD.ITERATION.name()).isMissingNode()) {
			loc.iteration = conf.substanceIteration; // default value is taken
														// form global config
		} else {
			String keyword = jsonUtils.extractStringKeyword(sectionNode, KEYWORD.ITERATION.name(), false);
			if (keyword == null) {
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"ITERATION\" : " + jsonUtils.getError());
				loc.nErrors++;
			} else {
				loc.FlagIteration = true;
				loc.iteration = IterationAccess.fromString(keyword);
				if (loc.iteration == IterationAccess.UNDEFINED) {
					conf.addError("In JSON section \"" + jsonSection
							+ "\", keyword \"ITERATION\" is incorrect or UNDEFINED!");
					loc.nErrors++;
				}
			}
		}

		// DATA_INTERPRETATION
		if (!sectionNode.path(KEYWORD.DATA_INTERPRETATION.name()).isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(sectionNode, KEYWORD.DATA_INTERPRETATION.name(), false);
			if (keyword == null) {
				conf.addError("In JSON section \"" + jsonSection + "\", keyword \"DATA_INTERPRETATION\" : "
						+ jsonUtils.getError());
				loc.nErrors++;
			} else {
				loc.FlagDataInterpretation = true;
				loc.dataInterpretation = DataInterpretation.fromString(keyword);
				if (loc.dataInterpretation == DataInterpretation.UNDEFINED) {
					conf.addError("In JSON section \"" + jsonSection
							+ "\", keyword \"DATA_INTERPRETATION\" is incorrect or UNDEFINED!");
					loc.nErrors++;
				}
			}
		}

		// DATE_FORMAT
		if (!sectionNode.path(KEYWORD.DATE_FORMAT.name()).isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(sectionNode, KEYWORD.DATE_FORMAT.name(), false);
			if (keyword == null) {
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"DATE_FORMAT\" : " + jsonUtils.getError());
				loc.nErrors++;
			} else {
				loc.FlagDateFormat = true;
				loc.dateFormat = keyword;
			}
		}

		// RECOGNITION
		if (sectionNode.path(KEYWORD.RECOGNITION.name()).isMissingNode()) {
			loc.recognition = conf.recognition; // default value is taken form
												// global config
		} else {
			String keyword = jsonUtils.extractStringKeyword(sectionNode, KEYWORD.RECOGNITION.name(), false);
			if (keyword == null) {
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"RECOGNITION\" : " + jsonUtils.getError());
				loc.nErrors++;
			} else {
				loc.FlagRecognition = true;
				loc.recognition = Recognition.fromString(keyword);
				if (loc.recognition == Recognition.UNDEFINED) {
					conf.addError("In JSON section \"" + jsonSection
							+ "\", keyword \"RECOGNITION\" is incorrect or UNDEFINED!");
					loc.nErrors++;
				}
			}
		}

		// COLUMN_INDEX
		if (sectionNode.path(KEYWORD.COLUMN_INDEX.name()).isMissingNode()) {
			if (loc.iteration.isColumnInfoRequired()) {
				if (loc.recognition == Recognition.BY_INDEX) {
					conf.addError("In JSON section \"" + jsonSection + "\", keyword \"COLUMN_INDEX\" is missing!");
					loc.nErrors++;
				}

				if (loc.recognition == Recognition.BY_INDEX_AND_NAME
						&& (sectionNode.path("COLUMN_NAME").isMissingNode())) {
					conf.addError("In JSON section \"" + jsonSection
							+ "\", both keywords \"COLUMN_INDEX\" and \"COLUMN_NAME\" are missing. "
							+ "At least one is required for RECOGNITION mode BY_INDEX_AND_NAME!");
					loc.nErrors++;
				}
			}
		} else {
			int col_index = ExcelParserUtils.extractColumnIndex(sectionNode.path("COLUMN_INDEX"));
			if (col_index == -1) {
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"COLUMN_INDEX\" : " + jsonUtils.getError());
				loc.nErrors++;
			} else {
				loc.FlagColumnIndex = true;
				loc.columnIndex = col_index;
			}
		}

		// COLUMN_INDICES
		JsonNode colIndices = sectionNode.path(KEYWORD.COLUMN_INDICES.name());
		if (!colIndices.isMissingNode()) {
			if (colIndices.isArray()) {
				loc.columnIndices = new int[colIndices.size()];
				for (int i = 0; i < colIndices.size(); i++) {
					JsonNode colNode = colIndices.get(i);
					int col_index = ExcelParserUtils.extractColumnIndex(colNode);

					if (col_index == -1) {
						conf.addError("In JSON section \"" + jsonSection + "\", keyword COLUMN_INDICES[" + (i + 1)
								+ "] is incorrect: " + jsonUtils.getError());
						loc.nErrors++;
					} else
						loc.columnIndices[i] = col_index;
				}
			} else {
				conf.addError("In JSON section \"" + jsonSection + "\", keyword COLUMN_INDICES  is not an array!");
			}
		}

		// COLUMN_NAME
		if (sectionNode.path(KEYWORD.COLUMN_NAME.name()).isMissingNode()) {
			if (loc.iteration.isColumnInfoRequired())
				if (loc.recognition == Recognition.BY_NAME) {
					conf.addError("In JSON section \"" + jsonSection + "\", keyword \"COLUMN_NAME\" is missing!");
					loc.nErrors++;
				}
			// Case loc.recognition == Recognition.BY_INDEX_AND_NAME is treated
			// in COLUMN_INDEX
		} else {
			String stringValue = jsonUtils.extractStringKeyword(sectionNode, KEYWORD.COLUMN_NAME.name(), false);
			if (stringValue == null)
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"COLUMN_NAME\" : " + jsonUtils.getError());
			else {
				loc.FlagColumnName = true;
				loc.columnName = stringValue;
			}
		}

		// ROW_INDEX
		if (sectionNode.path(KEYWORD.ROW_INDEX.name()).isMissingNode()) {
			if (loc.iteration.isRowInfoRequired()) {
				if (loc.recognition == Recognition.BY_INDEX) {
					conf.addError("In JSON section \"" + jsonSection + "\", keyword \"ROW_INDEX\" is missing!");
					loc.nErrors++;
				}

				if (loc.recognition == Recognition.BY_INDEX_AND_NAME
						&& (sectionNode.path("ROW_NAME").isMissingNode())) {
					conf.addError("In JSON section \"" + jsonSection
							+ "\", both keywords \"ROW_INDEX\" and \"ROW_NAME\" are missing. "
							+ "At least one is required for RECOGNITION mode BY_INDEX_AND_NAME!");
					loc.nErrors++;
				}
			}
		} else {
			Integer intValue = jsonUtils.extractIntKeyword(sectionNode, KEYWORD.ROW_INDEX.name(), true);
			if (intValue == null) {
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"ROW_INDEX\" : " + jsonUtils.getError());
				loc.nErrors++;
			} else {
				loc.FlagRowIndex = true;
				loc.rowIndex = intValue - 1; // 1-based --> 0-based
			}
		}

		// ROW_INDICES
		JsonNode rowIndices = sectionNode.path(KEYWORD.ROW_INDICES.name());
		if (!rowIndices.isMissingNode()) {
			if (rowIndices.isArray()) {
				loc.rowIndices = new int[rowIndices.size()];
				for (int i = 0; i < rowIndices.size(); i++) {
					JsonNode rowNode = rowIndices.get(i);
					if (rowNode.isInt()) {
						int row_ind = rowNode.asInt();

						if (row_ind <= 0) {
							conf.addError("In JSON section \"" + jsonSection + "\", keyword ROW_INDICES[" + (i + 1)
									+ "] is incorrect: " + jsonUtils.getError());
							loc.nErrors++;
						} else
							loc.rowIndices[i] = row_ind;
					} else {
						conf.addError("In JSON section \"" + jsonSection + "\", keyword ROW_INDICES[" + (i + 1)
								+ "] is not integer!");
						loc.nErrors++;
					}
				}
			} else {
				conf.addError("In JSON section \"" + jsonSection + "\", keyword ROW_INDICES  is not an array!");
			}
		}

		// ROW_NAME
		if (sectionNode.path(KEYWORD.ROW_NAME.name()).isMissingNode()) {
			if (loc.iteration.isRowInfoRequired())
				if (loc.recognition == Recognition.BY_NAME) {
					conf.addError("In JSON section \"" + jsonSection + "\", keyword \"ROW_NAME\" is missing!");
					loc.nErrors++;
				}
			// Case loc.recognition == Recognition.BY_INDEX_AND_NAME is treated
			// in ROW_INDEX
		} else {
			String stringValue = jsonUtils.extractStringKeyword(sectionNode, KEYWORD.ROW_NAME.name(), false);
			if (stringValue == null)
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"ROW_NAME\" : " + jsonUtils.getError());
			else {
				loc.FlagRowName = true;
				loc.rowName = stringValue;
			}
		}

		// SHEET_INDEX
		if (!sectionNode.path(KEYWORD.SHEET_INDEX.name()).isMissingNode()) {
			Integer intValue = jsonUtils.extractIntKeyword(sectionNode, KEYWORD.SHEET_INDEX.name(), false);
			if (intValue == null) {
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"SHEET_INDEX\" : " + jsonUtils.getError());
				loc.nErrors++;
			} else {
				loc.FlagSheetIndex = true;
				loc.sheetIndex = intValue - 1; // 1-based --> 0-based
			}
		}

		// SHEET_NAME
		if (!sectionNode.path(KEYWORD.SHEET_NAME.name()).isMissingNode()) {
			String stringValue = jsonUtils.extractStringKeyword(sectionNode, KEYWORD.SHEET_NAME.name(), false);
			if (stringValue == null)
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"SHEET_NAME\" : " + jsonUtils.getError());
			else {
				loc.FlagSheetName = true;
				loc.sheetName = stringValue;
			}
		}

		// JSON_VALUE
		if (sectionNode.path(KEYWORD.JSON_VALUE.name()).isMissingNode()) {
			if (loc.iteration == IterationAccess.JSON_VALUE) {
				conf.addError("In JSON section \"" + jsonSection + "\", keyword \"JSON_VALUE\" is missing!");
				loc.nErrors++;
			}
		} else {
			Object jsonValue = JsonUtilities.extractObject(sectionNode.path(KEYWORD.JSON_VALUE.name()));
			loc.setJsonValue(jsonValue);
		}

		// JSON_REPOSITORY_KEY
		if (sectionNode.path(KEYWORD.JSON_REPOSITORY_KEY.name()).isMissingNode()) {
			if (loc.iteration == IterationAccess.JSON_REPOSITORY) {
				conf.addError("In JSON section \"" + jsonSection + "\", keyword \"JSON_REPOSITORY_KEY\" is missing!");
				loc.nErrors++;
			}
		} else {
			String stringValue = jsonUtils.extractStringKeyword(sectionNode, KEYWORD.JSON_REPOSITORY_KEY.name(), true);
			if (stringValue == null)
				conf.addError("In JSON section \"" + jsonSection + "\", keyword \"JSON_REPOSITORY_KEY\" : "
						+ jsonUtils.getError());
			else {
				loc.setJsonRepositoryKey(stringValue);
			}
		}

		// VARIABLE_KEY
		if (sectionNode.path(KEYWORD.VARIABLE_KEY.name()).isMissingNode()) {
			if (loc.iteration == IterationAccess.VARIABLE) {
				conf.addError("In JSON section \"" + jsonSection + "\", keyword \"VARIABLE_KEY\" is missing!");
				loc.nErrors++;
			}
		} else {
			String stringValue = jsonUtils.extractStringKeyword(sectionNode, KEYWORD.VARIABLE_KEY.name(), false);
			if (stringValue == null)
				conf.addError(
						"In JSON section \"" + jsonSection + "\", keyword \"VARIABLE_KEY\" : " + jsonUtils.getError());
			else {
				loc.setVariableKey(stringValue);
			}
		}

		// VARIABLE_KEYS
		JsonNode vkeys = sectionNode.path(KEYWORD.VARIABLE_KEYS.name());
		if (!vkeys.isMissingNode()) {
			if (vkeys.isArray()) {
				loc.variableKeys = new String[vkeys.size()];
				for (int i = 0; i < vkeys.size(); i++) {
					JsonNode keyNode = vkeys.get(i);
					if (keyNode.isTextual()) {
						String keyword = keyNode.asText();
						if (keyword == null)
							conf.addError("In JSON section \"" + jsonSection + "\", keyword VARIABLE_KEYS [" + (i + 1)
									+ "]: is incorrect!");
						else
							loc.variableKeys[i] = keyword;
					} else {
						conf.addError("In JSON section \"" + jsonSection + "\", keyword VARIABLE_KEYS [" + (i + 1)
								+ "]: is not textual!");
					}
				}
			} else {
				conf.addError("In JSON section \"" + jsonSection + "\", keyword VARIABLE_KEYS  is not an array!");
			}
		}

		if (otherLocationFieldNames != null) {
			loc.otherLocationFields = new HashMap<String, ExcelDataLocation>();
			for (String otherField : otherLocationFieldNames) {
				JsonNode otherFieldNode = sectionNode.path(otherField);
				if (otherFieldNode.isMissingNode())
					continue;
				// recursion
				ExcelDataLocation otherLocField = extractDataLocation(otherFieldNode, null, conf, null, true);
				otherLocField.sectionName = otherField;
				loc.otherLocationFields.put(otherField, otherLocField);
			}
		}

		return loc;
	}

	public String toJSONKeyWord(String offset) {
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"" + sectionName + "\":\n");
		sb.append(offset + "{\n");

		if (FlagSourceCombination) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"SOURCE_COMBINATION\" : " + sourceCombination);
			nFields++;
		}

		if (FlagIsArray) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"IS_ARRAY\" : " + isArray);
			nFields++;
		}

		if (FlagTrimArray) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"TRIM_ARRAY\" : " + trimArray);
			nFields++;
		}

		if (FlagDataType) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"DATA_TYPE\" : \"" + dataType.toString() + "\"");
			nFields++;
		}

		if (FlagDataInterpretation) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"DATA_INTERPRETATION\" : \"" + dataInterpretation.toString() + "\"");
			nFields++;
		}

		if (FlagDateFormat) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"DATE_FORMAT\" : \"" + dateFormat + "\"");
			nFields++;
		}

		if (FlagRecognition) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"RECOGNITION\" : \"" + recognition.toString() + "\"");
			nFields++;
		}

		if (FlagIteration) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ITERATION\" : \"" + iteration.toString() + "\"");
			nFields++;
		}

		if (FlagAllowEmpty) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ALLOW_EMPTY\" : " + allowEmpty);
			nFields++;
		}

		if (FlagSheetIndex) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"SHEET_INDEX\" : " + (sheetIndex + 1)); // 0
																			// -->
																			// 1-based
			nFields++;
		}

		if (FlagSheetName) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"SHEET_NAME\" : \"" + sheetName + "\"");
			nFields++;
		}

		if (FlagColumnIndex) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"COLUMN_INDEX\" : " + (columnIndex + 1)); // 0
																				// -->
																				// 1-based
			nFields++;
		}

		if (columnIndices != null) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"COLUMN_INDICES\" : [");
			for (int i = 0; i < columnIndices.length; i++) {
				sb.append(columnIndices[i] + 1);
				if (i < (columnIndices.length - 1))
					sb.append(", ");
			}
			sb.append("]");
			nFields++;
		}

		if (FlagColumnName) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"COLUMN_NAME\" : \"" + columnName + "\"");
			nFields++;
		}

		if (FlagRowIndex) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ROW_INDEX\" : " + (rowIndex + 1)); // 0 -->
																		// 1-based
			nFields++;
		}

		if (rowIndices != null) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ROW_INDICES\" : [");
			for (int i = 0; i < rowIndices.length; i++) {
				sb.append(rowIndices[i] + 1);
				if (i < (rowIndices.length - 1))
					sb.append(", ");
			}
			sb.append("]");
			nFields++;
		}

		if (FlagRowName) {
			if (nFields > 0)
				sb.append(",\n");

			sb.append(offset + "\t\"ROW_NAME\" : \"" + rowName + "\"");
			nFields++;
		}

		if (jsonValue != null) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"JSON_VALUE\" : " + JsonUtilities.objectToJsonField(jsonValue));
			nFields++;
		}

		// TODO output some other missing info

		if (variableKeys != null) {
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"VARIABLE_KEYS\" : [");
			for (int i = 0; i < variableKeys.length; i++) {
				sb.append("\"" + variableKeys[i] + "\"");
				if (i < (variableKeys.length - 1))
					sb.append(", ");
			}
			sb.append("]");
			nFields++;
		}

		if (otherLocationFields != null) {
			Set<String> fieldNames = otherLocationFields.keySet();
			for (String field : fieldNames) {
				if (nFields > 0)
					sb.append(",\n");

				ExcelDataLocation fedl = otherLocationFields.get(field);
				sb.append(fedl.toJSONKeyWord(offset + "\t"));
				nFields++;
			}
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

	public Object getJsonValue() {
		return jsonValue;
	}

	public void setJsonValue(Object jsonValue) {
		this.jsonValue = jsonValue;
	}

	public String getJsonRepositoryKey() {
		return jsonRepositoryKey;
	}

	public void setJsonRepositoryKey(String jsonRepositoryKey) {
		this.jsonRepositoryKey = jsonRepositoryKey;
	}

	public String getVariableKey() {
		return variableKey;
	}

	public void setVariableKey(String variableKey) {
		this.variableKey = variableKey;
	}

	public boolean isFlagExtractValueQualifier() {
		return FlagExtractValueQualifier;
	}

	public void setFlagExtractValueQualifier(boolean flagExtractValueQualifier) {
		FlagExtractValueQualifier = flagExtractValueQualifier;
	}

	public boolean isFlagExtractAsRichValue() {
		return FlagExtractAsRichValue;
	}

	public void setFlagExtractAsRichValue(boolean flagExtractAsRichValue) {
		FlagExtractAsRichValue = flagExtractAsRichValue;
	}

	public int getParallelSheetIndex() {
		return parallelSheetIndex;
	}

	public void setParallelSheetIndex(int parallelSheetIndex) {
		this.parallelSheetIndex = parallelSheetIndex;
	}

	public boolean isFromParallelSheet() {
		return (parallelSheetIndex >= 0);
	}

	@Override
	public String toString() {
		return String.format("Col %d Row %d", columnIndex, rowIndex);
	}
}
