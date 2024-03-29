package net.enanomapper.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.enanomapper.parser.ParserConstants.DynamicIteration;
import net.enanomapper.parser.ParserConstants.ElementField;
import net.enanomapper.parser.ParserConstants.IterationAccess;
import net.enanomapper.parser.ParserConstants.Recognition;
import net.enanomapper.parser.ParserConstants.SheetSynchronization;
//import net.enanomapper.parser.dynamicspan.ColumnSpan;
//import net.enanomapper.parser.dynamicspan.DynamicIterationSpan;
//import net.enanomapper.parser.dynamicspan.RowSpan;
import net.enanomapper.parser.json.JsonUtilities;
import net.enanomapper.parser.recognition.IndexSet;
import net.enanomapper.parser.recognition.RecognitionUtils;
import net.enanomapper.parser.recognition.Tokenize;

/**
 * 
 * @author nick Internally all indices/numbers/ of rows, columns and sheets are
 *         represented as 0-based integers while in the JSON configuration and
 *         error messages they are 1-based represented (user-friendly style) The
 *         conversion from 1-based to 0-based and vice versa is done on
 *         "parsing", error message generation and toJSON() procedures
 *         respectively.
 */
public class ExcelParserConfigurator {

	private final static Logger logger = Logger.getLogger(ExcelParserConfigurator.class.getName());
	private static final int numGuideLinesToCheck = 5;
	private static final String guideLineJSONField = "guideline";
	protected String prefix = "XLSX";
	private ArrayList<String> configErrors = new ArrayList<String>();
	public ArrayList<String> configWarnings = new ArrayList<String>();

	// Configuration flags
	public boolean Fl_FullCheckForEmptyColumnsAndRows = true;
	public boolean Fl_AllowQualifierInValueCell = true;
	public boolean Fl_SkipEmptyRows = true;

	// Template info variables
	public String templateName = null;
	public String templateVersion = null;
	public int templateType = 1;

	// Global configuration for the data access of the primary sheet
	public boolean basicIterationLoadSubstanceRecord = true;
	public boolean FlagBasicIterationLoadSubstanceRecord = false;

	public IterationAccess substanceIteration = IterationAccess.ROW_SINGLE;
	public boolean FlagSubstanceIteration = false;

	public int rowMultiFixedSize = 1;
	public boolean FlagRowMultiFixedSize = false;

	public int startRow = 2;
	public boolean FlagStartRow = false;

	public int endRow = -1;
	public boolean FlagEndRow = false;

	public int sheetIndex = 0;
	public boolean FlagSheetIndex = false;

	public String sheetName = null;
	public boolean FlagSheetName = false;

	public int startHeaderRow = 0;
	public boolean FlagStartHeaderRow = false;

	public int endHeaderRow = 0;
	public boolean FlagEndHeaderRow = false;

	public boolean allowEmpty = true;
	public boolean FlagAllowEmpty = false;

	public Recognition recognition = Recognition.BY_INDEX;
	public boolean FlagRecognition = false;

	public DynamicIteration dynamicIteration = DynamicIteration.NEXT_NOT_EMPTY;
	public boolean FlagDynamicIteration = false;

	public int dynamicIterationColumnIndex = 0;
	public boolean FlagDynamicIterationColumnIndex = false;

	public String dynamicIterationColumnName = null;
	public boolean FlagDynamicIterationColumnName = false;

	// public Object skipRows = null;
	public boolean FlagSkipRows = false;
	public IndexSet skipRowsIndexSet = null;

	public boolean clearEmptyEffectRecords = false;
	public boolean FlagClearEmptyEffectRecords = false;

	// Specific data locations
	public ArrayList<ExcelSheetConfiguration> parallelSheets = new ArrayList<ExcelSheetConfiguration>();
	public HashMap<String, ExcelDataLocation> substanceLocations = new HashMap<String, ExcelDataLocation>();
	public ArrayList<ProtocolApplicationDataLocation> protocolAppLocations = new ArrayList<ProtocolApplicationDataLocation>();
	public HashMap<String, Object> jsonRepository = new HashMap<String, Object>();
	public ArrayList<CompositionDataLocation> composition = new ArrayList<CompositionDataLocation>();
	public ArrayList<ExternalIdentifierDataLocation> externalIdentifiers = new ArrayList<ExternalIdentifierDataLocation>();
	public SubstanceRecordMapLocation substanceRecordMap = null;
	
	// Read data as variables
	public HashMap<String, ExcelDataLocation> variableLocations = null;
	public ArrayList<VariableMapping> variableMappings = null;
	public HashMap<String, Tokenize> tokenizers = null;

	/*
	// Handling locations dynamically
	public boolean FlagDynamicSpan = false;
	public boolean FlagDynamicSpanOnSubtsanceLevel = false;
	public DynamicIterationSpan dynamicIterationSpan = null;
	public DynamicSpanInfo dynamicSpanInfo = null; // All dynamic span
													// information is analyzed
													// and stored here
	public ColumnSpan columnSpan = null;
	public RowSpan rowSpan = null;
	*/

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void addError(String error) {
		configErrors.add(error);
	}

	public boolean hasErrors() {
		return configErrors.size() > 0;
	}

	public static ExcelParserConfigurator loadFromJSON(File jsonConfig) throws FileNotFoundException, IOException, JsonProcessingException {
		return loadFromJSON(new FileInputStream(jsonConfig));
	}
	public static ExcelParserConfigurator loadFromJSON(InputStream jsonConfig) throws FileNotFoundException, IOException, JsonProcessingException {
	
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = null;

		try {
			root = mapper.readTree(jsonConfig);
		} catch (JsonProcessingException x) {
			throw x;			
		} catch (IOException x) {
			throw x;
		} finally {
			try {
				jsonConfig.close();
			} catch (Exception x) {
			}
		}

		JsonUtilities jsonUtils = new JsonUtilities();
		ExcelParserConfigurator conf = new ExcelParserConfigurator();

		// Handle Json Repository
		JsonNode curNode = root.path(KEYWORD.REPOSITORY.name());
		if (!curNode.isMissingNode())
			extractJsonRepository(curNode, conf);

		// Handle template info
		curNode = root.path(KEYWORD.TEMPLATE_INFO.name());
		if (curNode.isMissingNode())
			conf.configWarnings.add(String.format("JSON Section `%s` is missing!",KEYWORD.TEMPLATE_INFO.name()));
		else {
			// NAME
			String keyword = jsonUtils.extractStringKeyword(curNode, KEYWORD.NAME.name(), false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else
				conf.templateName = keyword;
			// VERSION
			keyword = jsonUtils.extractStringKeyword(curNode, KEYWORD.VERSION.name(), false);
			if (keyword == null)
				conf.addError(jsonUtils.getError());
			else
				conf.templateVersion = keyword;
			// TYPE
			Integer intValue = jsonUtils.extractIntKeyword(curNode, KEYWORD.TYPE.name(), true);
			if (intValue == null)
				conf.addError(jsonUtils.getError());
			else
				conf.templateType = intValue;
		}

		// Handle global data access
		curNode = root.path(KEYWORD.DATA_ACCESS.name());
		if (curNode.isMissingNode())
			conf.configErrors.add(String.format("JSON Section '%s' is missing!",KEYWORD.DATA_ACCESS.name()));
		else {
			// BASIC_ITERATION_LOAD_SUBSTANCE_RECORD
			if (!curNode.path(KEYWORD.BASIC_ITERATION_LOAD_SUBSTANCE_RECORD.name()).isMissingNode()) {
				Boolean b = jsonUtils.extractBooleanKeyword(curNode, KEYWORD.BASIC_ITERATION_LOAD_SUBSTANCE_RECORD.name(), false);
				if (b == null)
					conf.addError(jsonUtils.getError());
				else {
					conf.basicIterationLoadSubstanceRecord = b;
					conf.FlagBasicIterationLoadSubstanceRecord = true;
				}
			}

			// ITERATION
			if (!curNode.path( KEYWORD.ITERATION.name()).isMissingNode()) {
				String keyword = jsonUtils.extractStringKeyword(curNode, KEYWORD.ITERATION.name(), false);
				if (keyword == null)
					conf.addError(jsonUtils.getError());
				else {
					conf.substanceIteration = IterationAccess.fromString(keyword);
					conf.FlagSubstanceIteration = true;
					if (conf.substanceIteration == IterationAccess.UNDEFINED)
						conf.addError(
								String.format("In JSON Section '%s', the keyword '%s' is incorrect or UNDEFINED!",KEYWORD.DATA_ACCESS.name(),KEYWORD.ITERATION.name()));
				}
			}

			// SHEET_INDEX
			if (!curNode.path( KEYWORD.SHEET_INDEX.name()).isMissingNode()) {
				Integer intValue = jsonUtils.extractIntKeyword(curNode, KEYWORD.SHEET_INDEX.name(), false);
				if (intValue == null)
					conf.addError(jsonUtils.getError());
				else {
					conf.sheetIndex = intValue - 1; // 1-based --> 0-based
					conf.FlagSheetIndex = true;
				}
			}

			// SHEET_NAME
			if (!curNode.path(KEYWORD.SHEET_NAME.name()).isMissingNode()) {
				String keyword = jsonUtils.extractStringKeyword(curNode, KEYWORD.SHEET_NAME.name(), false);
				if (keyword == null)
					conf.configErrors.add(jsonUtils.getError());
				else {
					conf.sheetName = keyword;
					conf.FlagSheetName = true;
				}
			}

			// START_ROW
			if (!curNode.path(KEYWORD.START_ROW.name()).isMissingNode()) {
				Integer intValue = jsonUtils.extractIntKeyword(curNode, KEYWORD.START_ROW.name(), false);
				if (intValue == null)
					conf.configErrors.add(jsonUtils.getError());
				else {
					conf.startRow = intValue - 1; // 1-based --> 0-based
					conf.FlagStartRow = true;
				}
			}

			// END_ROW
			if (!curNode.path(KEYWORD.END_ROW.name()).isMissingNode()) {
				Integer intValue = jsonUtils.extractIntKeyword(curNode, KEYWORD.END_ROW.name(), false);
				if (intValue == null)
					conf.configErrors.add(jsonUtils.getError());
				else {
					conf.endRow = intValue - 1; // 1-based --> 0-based
					conf.FlagEndRow = true;
				}
			}

			// START_HEADER_ROW
			if (!curNode.path(KEYWORD.START_HEADER_ROW.name()).isMissingNode()) {
				Integer intValue = jsonUtils.extractIntKeyword(curNode, KEYWORD.START_HEADER_ROW.name(), false);
				if (intValue == null)
					conf.configErrors.add(jsonUtils.getError());
				else {
					conf.startHeaderRow = intValue - 1; // 1-based --> 0-based
					conf.FlagStartHeaderRow = true;
				}

			}

			// END_HEADER_ROW
			if (!curNode.path(KEYWORD.END_HEADER_ROW.name()).isMissingNode()) {
				Integer intValue = jsonUtils.extractIntKeyword(curNode, KEYWORD.END_HEADER_ROW.name(), false);
				if (intValue == null)
					conf.configErrors.add(jsonUtils.getError());
				else {
					conf.endHeaderRow = intValue - 1; // 1-based --> 0-based
					conf.FlagEndHeaderRow = true;
				}
			}

			// ALLOW_EMPTY
			if (!curNode.path(KEYWORD.ALLOW_EMPTY.name()).isMissingNode()) {
				Boolean boolValue = jsonUtils.extractBooleanKeyword(curNode, KEYWORD.ALLOW_EMPTY.name(), false);
				if (boolValue == null)
					conf.configErrors.add(jsonUtils.getError());
				else {
					conf.allowEmpty = boolValue;
					conf.FlagAllowEmpty = true;
				}
			}

			// RECOGNITION
			if (!curNode.path(KEYWORD.RECOGNITION.name()).isMissingNode()) {
				String keyword = jsonUtils.extractStringKeyword(curNode, KEYWORD.RECOGNITION.name(), true);
				if (keyword == null)
					conf.configErrors.add(jsonUtils.getError());
				else {
					conf.recognition = Recognition.fromString(keyword);
					conf.FlagRecognition = true;
					if (conf.recognition == Recognition.UNDEFINED)
						conf.configErrors.add(
								String.format("In JSON Section '%s', the keyword '%s' is incorrect or UNDEFINED!",KEYWORD.DATA_ACCESS.name(),KEYWORD.RECOGNITION.name()));
				}
			}

			// DYNAMIC_ITERATION
			if (!curNode.path(KEYWORD.DYNAMIC_ITERATION.name()).isMissingNode()) {
				String keyword = jsonUtils.extractStringKeyword(curNode, KEYWORD.DYNAMIC_ITERATION.name(), true);
				if (keyword == null)
					conf.configErrors.add(jsonUtils.getError());
				else {
					conf.dynamicIteration = DynamicIteration.fromString(keyword);
					conf.FlagDynamicIteration = true;
					if (conf.dynamicIteration == DynamicIteration.UNDEFINED)
						conf.configErrors.add(
								String.format("In JSON Section '%s', the keyword '%s' is incorrect or UNDEFINED!",KEYWORD.DATA_ACCESS.name(),KEYWORD.DYNAMIC_ITERATION.name()));						
				}
			}

			// DYNAMIC_ITERATION_COLUMN_INDEX
			if (!curNode.path(KEYWORD.DYNAMIC_ITERATION_COLUMN_INDEX.name()).isMissingNode()) {
				int col_index = ExcelParserUtils.extractColumnIndex(curNode.path(KEYWORD.DYNAMIC_ITERATION_COLUMN_INDEX.name()));
				if (col_index == -1) {
					conf.configErrors.add(
							String.format("In JSON Section '%s', the keyword '%s' is incorrect or UNDEFINED!",KEYWORD.DATA_ACCESS.name(),KEYWORD.DYNAMIC_ITERATION_COLUMN_INDEX.name()));
				} else {
					conf.dynamicIterationColumnIndex = col_index;
					conf.FlagDynamicIterationColumnIndex = true;
				}
			}

			// DYNAMIC_ITERATION_COLUMN_NAME
			if (!curNode.path(KEYWORD.DYNAMIC_ITERATION_COLUMN_NAME.name()).isMissingNode()) {
				String keyword = jsonUtils.extractStringKeyword(curNode, KEYWORD.DYNAMIC_ITERATION_COLUMN_NAME.name(), false);
				if (keyword == null)
					conf.configErrors.add(jsonUtils.getError());
				else {
					conf.dynamicIterationColumnName = keyword;
					conf.FlagDynamicIterationColumnName = true;
				}
			}

			// SKIP_ROWS
			JsonNode skipRowsNode = curNode.path(KEYWORD.SKIP_ROWS.name());
			if (!skipRowsNode.isMissingNode()) {
				try {
					conf.skipRowsIndexSet = IndexSet.getFromJsonNode(skipRowsNode);
					if (conf.skipRowsIndexSet != null)
						conf.FlagSkipRows = true;
				} catch (Exception x) {
					conf.configErrors.add(
							String.format("In JSON Section '%s', the keyword '%s' is incorrectly defined!",KEYWORD.DATA_ACCESS.name(),KEYWORD.SKIP_ROWS.name()));

				}
			}

			// CLEAR_EMPTY_EFFECT_RECORDS
			if (!curNode.path(KEYWORD.CLEAR_EMPTY_EFFECT_RECORDS.name()).isMissingNode()) {
				Boolean b = jsonUtils.extractBooleanKeyword(curNode, KEYWORD.CLEAR_EMPTY_EFFECT_RECORDS.name(), false);
				if (b == null)
					conf.configErrors.add(jsonUtils.getError());
				else {
					conf.clearEmptyEffectRecords = b;
					conf.FlagClearEmptyEffectRecords = true;
				}
			}

			// VARIABLES
			JsonNode varNode = curNode.path(KEYWORD.VARIABLES.name());
			if (!varNode.isMissingNode()) {
				conf.variableLocations = extractDynamicSection(varNode, conf, null);
			}

			// VARIABLE_MAPPINGS
			JsonNode mapsNode = curNode.path(KEYWORD.VARIABLE_MAPPINGS.name());
			if (!mapsNode.isMissingNode()) {
				if (!mapsNode.isArray())
					conf.configErrors.add(
							String.format("In JSON Section '%s', the keyword '%s' is not of type array!",KEYWORD.DATA_ACCESS.name(),KEYWORD.VARIABLE_MAPPINGS.name()));					

				else {
					conf.variableMappings = new ArrayList<VariableMapping>();
					for (int i = 0; i < mapsNode.size(); i++) {
						VariableMapping vm = VariableMapping.extractVariableMapping(mapsNode.get(i), conf, jsonUtils,
								i);
						conf.variableMappings.add(vm);
					}
				}
			}
			
			// TOKENIZERS
			JsonNode tokenizeNode = curNode.path("TOKENIZERS");
			if (!tokenizeNode.isMissingNode()) {
				if (!tokenizeNode.isArray())
					conf.configErrors.add(
							String.format("In JSON Section '%s', the keyword 'TOKENIZERS' is not of type array!"
									,KEYWORD.DATA_ACCESS.name()));
				else {
					conf.tokenizers = new HashMap<String, Tokenize>();
					for (int i = 0; i < tokenizeNode.size(); i++) {
						Tokenize tknz = Tokenize.extractTokenizer(tokenizeNode.get(i), conf, jsonUtils,i);
						conf.tokenizers.put(tknz.name, tknz);	
					}
				}
			}
			
			/*	
			// DYNAMIC_ITERATION_SPAN
			if (!curNode.path(KEYWORD.DYNAMIC_ITERATION_SPAN.name()).isMissingNode()) {
				DynamicIterationSpan span = DynamicIterationSpan
						.extractDynamicIterationSpan(curNode.path(KEYWORD.DYNAMIC_ITERATION_SPAN.name()), conf, KEYWORD.DATA_ACCESS.name());
				conf.dynamicIterationSpan = span;
				conf.dynamicIterationSpan.isPrimarySheet = true;
			}

			// COLUMN_SPAN
			if (!curNode.path(KEYWORD.COLUMN_SPAN.name()).isMissingNode()) {
				ColumnSpan span = extractColumnSpan(curNode.path(KEYWORD.COLUMN_SPAN.name()), conf, KEYWORD.DATA_ACCESS.name());
				conf.columnSpan = span;
			}

			// ROW_SPAN
			if (!curNode.path(KEYWORD.ROW_SPAN.name()).isMissingNode()) {
				RowSpan span = extractRowSpan(curNode.path(KEYWORD.ROW_SPAN.name()), conf, KEYWORD.DATA_ACCESS.name());
				conf.rowSpan = span;
			}
			*/

		}

		// Handle SubstanceRecord data locations
		curNode = root.path(KEYWORD.SUBSTANCE_RECORD.name());
		if (curNode.isMissingNode())
		{	
			//SUBSTANCE_RECORD is not needed in iteration mode: SUBSTANCE_RECORD_MAP
			if (conf.substanceIteration != IterationAccess.SUBSTANCE_RECORD_MAP)
				conf.configErrors.add(String.format("JSON Section '%s' is missing!",KEYWORD.SUBSTANCE_RECORD.name()));			
		}	
		else {
			// SUBSTANCE_NAME
			ExcelDataLocation loc = ExcelDataLocation.extractDataLocation(curNode, KEYWORD.SUBSTANCE_NAME.name(), conf);
			if (loc != null) {
				if (loc.nErrors == 0)
					conf.substanceLocations.put("SubstanceRecord.substanceName", loc);
				// error messages are already added to conf (this is valid for
				// all other location extractions)
			} else {
				// deprecated syntax COMPANY_NAME used for the same purposes
				loc = ExcelDataLocation.extractDataLocation(curNode, "COMPANY_NAME", conf);
				if (loc != null) {
					if (loc.nErrors == 0)
						conf.substanceLocations.put("SubstanceRecord.substanceName", loc);
					// error messages are already added to conf (this is valid
					// for all other location extractions)
				} else {
					// Missing section is not counted as an error. Same
					// treatment for the other sections
				}
			}

			// REFERENCE_SUBSTANCE_UUID
			loc = ExcelDataLocation.extractDataLocation(curNode, KEYWORD.REFERENCE_SUBSTANCE_UUID.name(), conf);
			if (loc != null) {
				if (loc.nErrors == 0)
					conf.substanceLocations.put("SubstanceRecord.referenceSubstanceUUID", loc);
			}

			// SUBSTANCE_UUID
			loc = ExcelDataLocation.extractDataLocation(curNode, KEYWORD.SUBSTANCE_UUID.name(), conf);
			if (loc != null) {
				if (loc.nErrors == 0)
					conf.substanceLocations.put("SubstanceRecord.substanceUUID", loc);
			} else {
				// deprecated syntax COMPANY_UUID used for the same purpose
				loc = ExcelDataLocation.extractDataLocation(curNode, "COMPANY_UUID", conf);
				if (loc != null) {
					if (loc.nErrors == 0)
						conf.substanceLocations.put("SubstanceRecord.substanceUUID", loc);
				}
			}

			// OWNER_NAME
			loc = ExcelDataLocation.extractDataLocation(curNode, KEYWORD.OWNER_NAME.name(), conf);
			if (loc != null) {
				if (loc.nErrors == 0)
					conf.substanceLocations.put("SubstanceRecord.ownerName", loc);
			}

			// OWNER_UUID
			loc = ExcelDataLocation.extractDataLocation(curNode, KEYWORD.OWNER_UUID.name(), conf);
			if (loc != null) {
				if (loc.nErrors == 0)
					conf.substanceLocations.put("SubstanceRecord.ownerUUID", loc);
			}

			// SUBSTANCE_TYPE
			loc = ExcelDataLocation.extractDataLocation(curNode, KEYWORD.SUBSTANCE_TYPE.name(), conf);
			if (loc != null) {
				if (loc.nErrors == 0)
					conf.substanceLocations.put("SubstanceRecord.substanceType", loc);
			}

			// PUBLIC_NAME
			loc = ExcelDataLocation.extractDataLocation(curNode, KEYWORD.PUBLIC_NAME.name(), conf);
			if (loc != null) {
				if (loc.nErrors == 0)
					conf.substanceLocations.put("SubstanceRecord.publicName", loc);
			}

			// ID_SUBSTANCE
			loc = ExcelDataLocation.extractDataLocation(curNode, "ID_SUBSTANCE", conf);
			if (loc != null) {
				if (loc.nErrors == 0)
					conf.substanceLocations.put("SubstanceRecord.idSubstance", loc);
			}

			// COMPOSITION
			JsonNode composNode = curNode.path(KEYWORD.COMPOSITION.name());
			if (!composNode.isMissingNode()) {
				if (composNode.isArray()) {
					for (int i = 0; i < composNode.size(); i++) {
						CompositionDataLocation compDL = extractCompositionDataLocation(composNode.get(i), conf, i);
						conf.composition.add(compDL);
					}
				} else
					conf.configErrors.add(String.format("Section '%s' is not an array!",KEYWORD.COMPOSITION.name()));
			}

			// EXTERNAL_IDENTIFIERS
			JsonNode extIdNode = curNode.path(KEYWORD.EXTERNAL_IDENTIFIERS.name());
			if (!extIdNode.isMissingNode()) {
				if (extIdNode.isArray()) {
					for (int i = 0; i < extIdNode.size(); i++) {
						ExternalIdentifierDataLocation eidl = ExternalIdentifierDataLocation
								.extractExternalIdentifier(extIdNode.get(i), conf);
						conf.externalIdentifiers.add(eidl);
					}
				} else
					conf.configErrors.add(String.format("Section '%s' is not an array!",KEYWORD.EXTERNAL_IDENTIFIERS.name()));
			}
		} //end of SUBSTANCE_RECORD section
		

		// Handle Parallel Sheets
		curNode = root.path(KEYWORD.PARALLEL_SHEETS.name());
		if (curNode.isMissingNode()) {
			// Nothing is done. Missing PARALLEL_SHEETS is not an error.
		} else {
			if (!curNode.isArray()) {
				conf.configErrors.add(String.format("Section '%s' is not an array!",KEYWORD.PARALLEL_SHEETS.name()));
				return conf;
			}

			for (int i = 0; i < curNode.size(); i++) {
				ExcelSheetConfiguration eshc = extractParallelSheet(curNode.get(i), i, conf);
				if (eshc == null)
					return conf;
				else
					conf.parallelSheets.add(eshc);
			}
		}

		// Handle Protocol Applications (Measurements)
		curNode = root.path(KEYWORD.PROTOCOL_APPLICATIONS.name());
		if (curNode.isMissingNode())
			conf.configErrors.add(String.format("JSON Section '%s' is missing!",KEYWORD.PROTOCOL_APPLICATIONS.name()));
		else {
			if (!curNode.isArray()) {
				conf.configErrors.add(String.format("Section '%s' is not an array!",KEYWORD.PROTOCOL_APPLICATIONS.name()));
				return conf;
			}

			for (int i = 0; i < curNode.size(); i++) {
				ProtocolApplicationDataLocation padl = extractProtocolApplicationDataLocations(curNode.get(i), i, conf);
				if (padl == null)
					return conf;
				else
					conf.protocolAppLocations.add(padl);
			}
		}
		
		//SUBSTANCE_RECORD_MAP
		curNode = root.path(KEYWORD.SUBSTANCE_RECORD_MAP.name());
		if (!curNode.isMissingNode())
		{	
			conf.substanceRecordMap = SubstanceRecordMapLocation.extractSubstanceRecordMapLocation(curNode, conf);
			conf.substanceRecordMap.checkConsistency(conf);
		}	
		

		//conf.checkDynamicConfiguration();

		return conf;
	}

	public String toJSONString() {
		ExcelDataLocation loc;
		int n;
		StringBuffer sb = new StringBuffer();
		sb.append("{\n");

		sb.append("\t\"TEMPLATE_INFO\" : \n");
		sb.append("\t{\n");
		if (templateName != null)
			sb.append("\t\t\"NAME\" : \"" + templateName + "\",\n");
		if (templateVersion != null)
			sb.append("\t\t\"VERSION\" : \"" + templateVersion + "\",\n");
		sb.append("\t\t\"TYPE\" : " + templateType + "\n");
		sb.append("\t},\n\n");

		sb.append("\t\"DATA_ACCESS\" : \n");
		sb.append("\t{\n");
		int nDAFields = 0;

		if (FlagBasicIterationLoadSubstanceRecord) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"BASIC_ITERATION_LOAD_SUBSTANCE_RECORD\" : " + basicIterationLoadSubstanceRecord);
			nDAFields++;
		}
		if (FlagSubstanceIteration) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"ITERATION\" : \"" + substanceIteration.toString() + "\"");
			nDAFields++;
		}
		if (FlagRowMultiFixedSize) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"ROW_MULTI_FIXED_SIZE\" : " + rowMultiFixedSize);
			nDAFields++;
		}
		if (FlagSheetIndex) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"SHEET_INDEX\" : " + (sheetIndex + 1)); // 0-based
																	// -->
																	// 1-based
			nDAFields++;
		}
		if (FlagSheetName) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"SHEET_NAME\" : \"" + sheetName + "\"");
			nDAFields++;
		}
		if (FlagStartRow) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"START_ROW\" : " + (startRow + 1)); // 0-based -->
																// 1-based
			nDAFields++;
		}
		if (FlagEndRow) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"END_ROW\" : " + (endRow + 1)); // 0-based -->
															// 1-based
			nDAFields++;
		}
		if (FlagStartHeaderRow) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"START_HEADER_ROW\" : " + (startHeaderRow + 1)); // 0-based
																				// -->
																				// 1-based
			nDAFields++;
		}
		if (FlagEndHeaderRow) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"END_HEADER_ROW\" : " + (endHeaderRow + 1)); // 0-based
																			// -->
																			// 1-based
			nDAFields++;
		}
		if (FlagAllowEmpty) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"ALLOW_EMPTY\" : \"" + allowEmpty + "\"");
			nDAFields++;
		}
		if (FlagRecognition) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"RECOGNITION\" : \"" + recognition.toString() + "\"");
			nDAFields++;
		}
		if (FlagDynamicIteration) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"DYNAMIC_ITERATION\" : \"" + dynamicIteration.toString() + "\"");
			nDAFields++;
		}
		if (FlagDynamicIterationColumnIndex) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"DYNAMIC_ITERATION_COLUMN_INDEX\" : " + (dynamicIterationColumnIndex + 1)); // 0-based
																										// -->
																										// 1-based
			nDAFields++;
		}
		if (FlagDynamicIterationColumnName) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"DYNAMIC_ITERATION_COLUMN_NAME\" : \"" + dynamicIterationColumnName + "\"");
			nDAFields++;
		}
		if (FlagSkipRows) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"SKIP_ROWS\" : " + skipRowsIndexSet.toJSONKeyWord());
			nDAFields++;
		}

		if (FlagClearEmptyEffectRecords) {
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"CLEAR_EMPTY_EFFECT_RECORDS\" : " + clearEmptyEffectRecords);
			nDAFields++;
		}

		if (variableLocations != null) {

			if (nDAFields > 0)
				sb.append(",\n\n");
			sb.append("\t\t\"VARIABLES\" : \n");
			sb.append("\t\t{\n");

			int nParams = 0;
			for (String var : variableLocations.keySet()) {
				loc = variableLocations.get(var);
				sb.append(loc.toJSONKeyWord("\t\t\t"));

				if (nParams < variableLocations.size())
					sb.append(",\n\n");
				else
					sb.append("\n");
				nParams++;
			}
			sb.append("\t\t}");
			nDAFields++;
		}

		// Variable mappings
		if (variableMappings != null) {
			if (nDAFields > 0)
				sb.append(",\n\n");

			sb.append("\t\t\"VARIABLE_MAPPINGS\":\n");
			sb.append("\t\t[\n");
			for (int i = 0; i < variableMappings.size(); i++) {
				sb.append(variableMappings.get(i).toJSONKeyWord("\t\t\t"));
				if (i < variableMappings.size() - 1)
					sb.append(",\n");
				sb.append("\n");
			}

			sb.append("\t\t]\n");
			nDAFields++;
		}

		/*
		// Dynamic locations
		if (dynamicIterationSpan != null) {
			if (nDAFields > 0)
				sb.append(",\n\n");
			sb.append(dynamicIterationSpan.toJSONKeyWord("\t\t"));
			nDAFields++;
		}

		if (columnSpan != null) {
			if (nDAFields > 0)
				sb.append(",\n\n");
			sb.append(columnSpan.toJSONKeyWord("\t\t"));
			nDAFields++;
		}

		if (rowSpan != null) {
			if (nDAFields > 0)
				sb.append(",\n\n");
			sb.append(rowSpan.toJSONKeyWord("\t\t"));
			nDAFields++;
		}
		*/

		if (nDAFields > 0)
			sb.append("\n");

		sb.append("\t},\n\n"); // end of DATA_ACCESS section

		if (!parallelSheets.isEmpty()) {
			sb.append("\t\"PARALLEL_SHEETS\":\n");
			sb.append("\t[\n");
			for (int i = 0; i < parallelSheets.size(); i++) {
				sb.append(parallelSheets.get(i).toJSONKeyWord("\t\t"));
				if (i < parallelSheets.size() - 1)
					sb.append(",\n");
				sb.append("\n");
			}
			sb.append("\t],\n\n");
		}

		sb.append("\t\"SUBSTANCE_RECORD\" : \n");
		sb.append("\t{\n");
		n = 0;

		loc = substanceLocations.get("SubstanceRecord.referenceSubstanceUUID");
		if (loc != null) {
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}

		loc = substanceLocations.get("SubstanceRecord.companyName");
		if (loc != null) {
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}

		loc = substanceLocations.get("SubstanceRecord.companyUUID");
		if (loc != null) {
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}

		loc = substanceLocations.get("SubstanceRecord.ownerName");
		if (loc != null) {
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}

		loc = substanceLocations.get("SubstanceRecord.ownerUUID");
		if (loc != null) {
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}

		loc = substanceLocations.get("SubstanceRecord.substanceType");
		if (loc != null) {
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}

		loc = substanceLocations.get("SubstanceRecord.publicName");
		if (loc != null) {
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}

		loc = substanceLocations.get("SubstanceRecord.idSubstance");
		if (loc != null) {
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}

		if (!externalIdentifiers.isEmpty()) {
			if (n > 0)
				sb.append(",\n\n");
			sb.append("\t\t\"EXTERNAL_IDENTIFIERS\":\n");
			sb.append("\t\t[\n");
			for (int i = 0; i < externalIdentifiers.size(); i++) {
				sb.append(externalIdentifiers.get(i).toJSONKeyWord("\t\t\t"));
				if (i < externalIdentifiers.size() - 1)
					sb.append(",\n");
				sb.append("\n");
			}
			sb.append("\t\t]");
		}

		if (!composition.isEmpty()) {
			if (n > 0)
				sb.append(",\n\n");
			sb.append("\t\t\"COMPOSITION\":\n");
			sb.append("\t\t[\n");
			for (int i = 0; i < composition.size(); i++) {
				sb.append(composition.get(i).toJSONKeyWord("\t\t\t"));
				if (i < composition.size() - 1)
					sb.append(",\n");
				sb.append("\n");
			}
			sb.append("\t\t]");
		}

		if (n > 0)
			sb.append("\n");

		sb.append("\t},\n\n"); // end of SUBSTANCE_RECORD
		
		if (substanceRecordMap != null)
		{	
			sb.append(substanceRecordMap.toJSONKeyWord("\t"));
			sb.append(",\n\n");
		}	
		
		
		sb.append("\t\"PROTOCOL_APPLICATIONS\":\n");
		sb.append("\t[\n");
		for (int i = 0; i < protocolAppLocations.size(); i++) {
			sb.append(protocolAppLocations.get(i).toJSONKeyWord("\t\t"));
			if (i < protocolAppLocations.size() - 1)
				sb.append(",\n");
			sb.append("\n");
		}
		sb.append("\t]"); // end of PROTOCOL_APPLICATIONS array

		if (!jsonRepository.isEmpty()) {
			sb.append(",\n\n");
			sb.append("\t\"REPOSITORY\":\n");
			sb.append("\t{\n");

			int nRepElements = 0;
			for (String key : jsonRepository.keySet()) {
				sb.append("\t\t\"" + key + "\" : ");
				Object o = jsonRepository.get(key);
				if (o instanceof Integer)
					sb.append(o.toString());
				else if (o instanceof Double)
					sb.append(o.toString());
				else if (o instanceof String)
					sb.append("\"" + o.toString() + "\"");
				else
					sb.append("\"***NOT_SUPPORTED_OBJECT***\""); // This line
																	// should
																	// not be
																	// reached

				if (nRepElements < jsonRepository.size() - 1)
					sb.append(",\n");
				else
					sb.append("\n");
				nRepElements++;
			}

			sb.append("\t}");
		}

		sb.append("\n\n");

		sb.append("}\n"); // end of JSON
		return sb.toString();
	}

	public String getAllErrorsAsString() {
		if (configErrors.isEmpty())
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < configErrors.size(); i++)
			sb.append(configErrors.get(i) + "\n");
		return sb.toString();
	}

	public static ProtocolApplicationDataLocation extractProtocolApplicationDataLocations(JsonNode node,
			int protocolNum, ExcelParserConfigurator conf) {
		ProtocolApplicationDataLocation padl = new ProtocolApplicationDataLocation();

		// PROTOCOL_APPLICATION_UUID
		ExcelDataLocation loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.PROTOCOL_APPLICATION_UUID.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.protocolApplicationUUID = loc;
		}

		// INVESTIGATION_UUID
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.INVESTIGATION_UUID.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.investigationUUID = loc;
		}
		
		// ASSAY_UUID
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.ASSAY_UUID.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.assayUUID = loc;
		}

		// CITATION_TITLE
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.CITATION_TITLE.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.citationTitle = loc;
		}

		// CITATION_YEAR
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.CITATION_YEAR.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.citationYear = loc;
		}

		// CITATION_OWNER
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.CITATION_OWNER.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.citationOwner = loc;
		}

		// PROTOCOL_TOP_CATEGORY
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.PROTOCOL_TOP_CATEGORY.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.protocolTopCategory = loc;
		}

		// PROTOCOL_CATEGORY_CODE
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.PROTOCOL_CATEGORY_CODE.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.protocolCategoryCode = loc;
		}

		// PROTOCOL_CATEGORY_TITLE
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.PROTOCOL_CATEGORY_TITLE.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.protocolCategoryTitle = loc;
		}

		// PROTOCOL_ENDPOINT
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.PROTOCOL_ENDPOINT.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.protocolEndpoint = loc;
		}

		// PROTOCOL_GUIDELINE
		JsonNode pglNode = node.path(KEYWORD.PROTOCOL_GUIDELINE.name());
		if (!pglNode.isMissingNode()) {
			ArrayList<ExcelDataLocation> protGuidline = new ArrayList<ExcelDataLocation>();
			HashMap<String, ExcelDataLocation> pglLocs = extractDynamicSection(pglNode, conf, null);
			for (int i = 1; i < numGuideLinesToCheck; i++) {
				ExcelDataLocation pglLoc = pglLocs.get(guideLineJSONField + i);
				if (pglLoc != null)
					protGuidline.add(pglLoc);
			}

			padl.protocolGuideline = protGuidline;
		}

		// PARAMETERS
		JsonNode parNode = node.path(KEYWORD.PARAMETERS.name());
		if (!parNode.isMissingNode()) {
			String otherFields[] = { KEYWORD.UNIT.name(), KEYWORD.NAME.name() };
			padl.parameters = extractDynamicSection(parNode, conf, otherFields);
		}

		// RELIABILITY_IS_ROBUST_STUDY
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.RELIABILITY_IS_ROBUST_STUDY.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.reliability_isRobustStudy = loc;
		}

		// RELIABILITY_IS_USED_FOR_CLASSIFICATION
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.RELIABILITY_IS_USED_FOR_CLASSIFICATION.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.reliability_isUsedforClassification = loc;
		}

		// RELIABILITY_IS_USED_FOR_MSDS
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.RELIABILITY_IS_USED_FOR_MSDS.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.reliability_isUsedforMSDS = loc;
		}

		// RELIABILITY_PURPOSE_FLAG
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.RELIABILITY_PURPOSE_FLAG.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.reliability_purposeFlag = loc;
		}

		// RELIABILITY_STUDY_RESULT_TYPE
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.RELIABILITY_STUDY_RESULT_TYPE.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.reliability_studyResultType = loc;
		}

		// RELIABILITY_VALUE
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.RELIABILITY_VALUE.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.reliability_value = loc;
		}

		// INTERPRETATION_RESULT
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.INTERPRETATION_RESULT.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.interpretationResult = loc;
		}

		// INTERPRETATION_CRITERIA
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.INTERPRETATION_CRITERIA.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				padl.interpretationCriteria = loc;
		}

		// EFFECTS
		JsonNode effectsNode = node.path(KEYWORD.EFFECTS.name());
		if (!effectsNode.isMissingNode()) {
			if (!effectsNode.isArray()) {
				conf.configErrors.add("EFFECTS section is not of type array!");
				return padl;
			}

			padl.effects = new ArrayList<EffectRecordDataLocation>();

			for (int i = 0; i < effectsNode.size(); i++) {
				EffectRecordDataLocation efrdl = extractEffectDataLocation(effectsNode.get(i), conf);
				padl.effects.add(efrdl);
			}
		}

		// EFFECTS_BLOCK
		JsonNode effectsBlockNode = node.path(KEYWORD.EFFECTS_BLOCK.name());
		if (!effectsBlockNode.isMissingNode()) {
			padl.effectsBlock = new ArrayList<ExcelDataBlockLocation>();
			if (effectsBlockNode.isArray()) {
				for (int i = 0; i < effectsBlockNode.size(); i++) {
					ExcelDataBlockLocation block = ExcelDataBlockLocation.extractDataBlock(effectsBlockNode.get(i),
							null, conf);
					padl.effectsBlock.add(block);
				}
			} else {
				ExcelDataBlockLocation block = ExcelDataBlockLocation.extractDataBlock(node, KEYWORD.EFFECTS_BLOCK.name(), conf);
				padl.effectsBlock.add(block);
			}
		}

		return padl;
	}

	public static EffectRecordDataLocation extractEffectDataLocation(JsonNode node, ExcelParserConfigurator conf) {
		EffectRecordDataLocation efrdl = new EffectRecordDataLocation();

		
		// SIMPLE_EFFECT_BLOCK
		if (!node.path("SIMPLE_EFFECT_BLOCK").isMissingNode()) {
			JsonUtilities jsonUtils = new JsonUtilities();
			Boolean b = jsonUtils.extractBooleanKeyword(node, "SIMPLE_EFFECT_BLOCK", false);
			if (b == null) {
				conf.addError("In Effects element, Keyword \"SIMPLE_EFFECT_BLOCK\" : "
						+ jsonUtils.getError());
			} else {
				efrdl.simpleEffectBlock = b;
				efrdl.FlagSimpleEffectBlock = true;
			}
		}
		
		// ENDPOINT
		ExcelDataLocation loc = ExcelDataLocation.extractDataLocation(node, ElementField.ENDPOINT.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				efrdl.endpoint = loc;
		}
		
		// ENDPOINT_TYPE
		loc = ExcelDataLocation.extractDataLocation(node, ElementField.ENDPOINT_TYPE.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				efrdl.endpointType = loc;
		}

		// SAMPLE_ID
		loc = ExcelDataLocation.extractDataLocation(node, ElementField.SAMPLE_ID.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				efrdl.sampleID = loc;
		}

		// UNIT
		loc = ExcelDataLocation.extractDataLocation(node, ElementField.UNIT.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				efrdl.unit = loc;
		}

		// LO_VALUE
		loc = ExcelDataLocation.extractDataLocation(node, ElementField.LO_VALUE.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0) {
				efrdl.loValue = loc;
				if (conf.Fl_AllowQualifierInValueCell)
					efrdl.loValue.setFlagExtractValueQualifier(true);
			}
		}

		// LO_QUALIFIER
		loc = ExcelDataLocation.extractDataLocation(node, ElementField.LO_QUALIFIER.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				efrdl.loQualifier = loc;
		}

		// UP_VALUE
		loc = ExcelDataLocation.extractDataLocation(node, ElementField.UP_VALUE.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0) {
				efrdl.upValue = loc;
				if (conf.Fl_AllowQualifierInValueCell)
					efrdl.upValue.setFlagExtractValueQualifier(true);
			}
		}

		// UP_QUALIFIER
		loc = ExcelDataLocation.extractDataLocation(node, ElementField.UP_QUALIFIER.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				efrdl.upQualifier = loc;
		}

		// TEXT_VALUE
		loc = ExcelDataLocation.extractDataLocation(node, ElementField.TEXT_VALUE.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				efrdl.textValue = loc;
		}

		// ERR_VALUE
		loc = ExcelDataLocation.extractDataLocation(node, ElementField.ERR_VALUE.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0) {
				efrdl.errValue = loc;
				if (conf.Fl_AllowQualifierInValueCell)
					efrdl.errValue.setFlagExtractValueQualifier(true);
			}
		}

		// ERR_QUALIFIER
		loc = ExcelDataLocation.extractDataLocation(node, ElementField.ERR_QUALIFIER.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				efrdl.errQualifier = loc;
		}

		// VALUE
		loc = ExcelDataLocation.extractDataLocation(node, ElementField.VALUE.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				efrdl.value = loc;
		}

		// CONDITIONS
		JsonNode effCondNode = node.path(KEYWORD.CONDITIONS.name());
		if (!effCondNode.isMissingNode()) {
			String otherFields[] = { KEYWORD.UNIT.name(), KEYWORD.NAME.name() };
			efrdl.conditions = extractDynamicSection(effCondNode, conf, otherFields);
		}

		JsonUtilities jsonUtils = null;

		// REFERENCE
		if (!node.path(KEYWORD.REFERENCE.name()).isMissingNode()) {
			jsonUtils = new JsonUtilities();
			String keyword = jsonUtils.extractStringKeyword(node, KEYWORD.REFERENCE.name(), false);
			if (keyword == null)
				conf.configErrors.add("Incorrect effect REFERENCE : " + jsonUtils.getError());
			else {
				efrdl.reference = keyword;
			}
		}

		// ADD_CONDITIONS_BY_REF
		JsonNode addCondNode = node.path(KEYWORD.ADD_CONDITIONS_BY_REF.name());
		if (!addCondNode.isMissingNode()) {
			if (addCondNode.isArray()) {
				if (jsonUtils == null)
					jsonUtils = new JsonUtilities();

				efrdl.addConditionsByRef = new String[addCondNode.size()];
				for (int i = 0; i < addCondNode.size(); i++) {
					if (addCondNode.get(i).isTextual())
						efrdl.addConditionsByRef[i] = addCondNode.get(i).asText();
					else
						conf.configErrors.add(String.format("Incorrect %s [%d] : not textual ",KEYWORD.ADD_CONDITIONS_BY_REF.name(),(i + 1)));
				}
			} else {
				conf.configErrors.add(String.format("%s is not an array!",KEYWORD.ADD_CONDITIONS_BY_REF.name()));

			}
		}

		return efrdl;
	}

	public static ExcelSheetConfiguration extractParallelSheet(JsonNode node, int jsonArrayIndex,
			ExcelParserConfigurator conf) {
		ExcelSheetConfiguration eshc = new ExcelSheetConfiguration();
		JsonUtilities jsonUtils = new JsonUtilities();

		// ITERATION
		if (!node.path(KEYWORD.ITERATION.name()).isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(node, KEYWORD.ITERATION.name(), false);
			if (keyword == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.ITERATION.name(),jsonUtils.getError()));
			else {
				eshc.iteration = IterationAccess.fromString(keyword);
				if (eshc.iteration == IterationAccess.UNDEFINED)
					conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s':  is incorrect or UNDEFINED!",
							KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.ITERATION.name()));
				else
					eshc.FlagIteration = true;
			}
		}

		// SHEET_INDEX
		if (!node.path(KEYWORD.SHEET_INDEX.name()).isMissingNode()) {
			Integer intValue = jsonUtils.extractIntKeyword(node, KEYWORD.SHEET_INDEX.name(), false);
			if (intValue == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.SHEET_INDEX.name(),jsonUtils.getError()));

			else {
				eshc.sheetIndex = intValue - 1; // 1-based --> 0-based
				eshc.FlagSheetIndex = true;
			}
		}

		// SHEET_NAME
		if (!node.path(KEYWORD.SHEET_NAME.name()).isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(node, KEYWORD.SHEET_NAME.name(), false);
			if (keyword == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.SHEET_NAME.name(),jsonUtils.getError()));
			else {
				eshc.sheetName = keyword;
				eshc.FlagSheetName = true;
			}
		}

		// ROW_MULTI_FIXED_SIZE
		if (!node.path(KEYWORD.ROW_MULTI_FIXED_SIZE.name()).isMissingNode()) {
			Integer intValue = jsonUtils.extractIntKeyword(node, "ROW_MULTI_FIXED_SIZE", false);
			if (intValue == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.ROW_MULTI_FIXED_SIZE.name(),jsonUtils.getError()));				
			else {
				eshc.rowMultiFixedSize = intValue;
				eshc.FlagRowMultiFixedSize = true;
			}
		}

		// START_ROW
		if (!node.path(KEYWORD.START_ROW.name()).isMissingNode()) {
			Integer intValue = jsonUtils.extractIntKeyword(node, KEYWORD.START_ROW.name(), false);
			if (intValue == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.START_ROW.name(),jsonUtils.getError()));				
			else {
				eshc.startRow = intValue - 1; // 1-based --> 0-based
				eshc.FlagStartRow = true;
			}
		}

		// END_ROW
		if (!node.path(KEYWORD.END_ROW.name()).isMissingNode()) {
			Integer intValue = jsonUtils.extractIntKeyword(node, KEYWORD.END_ROW.name(), false);
			if (intValue == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.END_ROW.name(),jsonUtils.getError()));
			else {
				eshc.endRow = intValue - 1; // 1-based --> 0-based
				eshc.FlagEndRow = true;
			}
		}

		// START_HEADER_ROW
		if (!node.path(KEYWORD.START_HEADER_ROW.name()).isMissingNode()) {
			Integer intValue = jsonUtils.extractIntKeyword(node, KEYWORD.START_HEADER_ROW.name(), false);
			if (intValue == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.START_HEADER_ROW.name(),jsonUtils.getError()));
			else {
				eshc.startHeaderRow = intValue - 1; // 1-based --> 0-based
				eshc.FlagStartHeaderRow = true;
			}
		}

		// END_HEADER_ROW
		if (!node.path(KEYWORD.END_HEADER_ROW.name()).isMissingNode()) {
			Integer intValue = jsonUtils.extractIntKeyword(node, KEYWORD.END_HEADER_ROW.name(), false);
			if (intValue == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.END_HEADER_ROW.name(),jsonUtils.getError()));
			else {
				eshc.endHeaderRow = intValue - 1; // 1-based --> 0-based
				eshc.FlagEndHeaderRow = true;
			}
		}

		// ALLOW_EMPTY
		if (!node.path(KEYWORD.ALLOW_EMPTY.name()).isMissingNode()) {
			Boolean boolValue = jsonUtils.extractBooleanKeyword(node, KEYWORD.ALLOW_EMPTY.name(), false);
			if (boolValue == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.ALLOW_EMPTY.name(),jsonUtils.getError()));
			else {
				eshc.allowEmpty = boolValue;
				eshc.FlagAllowEmpty = true;
			}
		}

		// RECOGNITION
		if (!node.path(KEYWORD.RECOGNITION.name()).isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(node, KEYWORD.RECOGNITION.name(), true);
			if (keyword == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.RECOGNITION.name(),jsonUtils.getError()));
			else {
				eshc.recognition = Recognition.fromString(keyword);
				if (conf.recognition == Recognition.UNDEFINED)
					conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " + (jsonArrayIndex + 1)
							+ " keyword \"RECOGNITION\" is incorrect or UNDEFINED!");
			}
		}

		// DYNAMIC_ITERATION
		if (!node.path(KEYWORD.DYNAMIC_ITERATION.name()).isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(node, KEYWORD.DYNAMIC_ITERATION.name(), true);
			if (keyword == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.DYNAMIC_ITERATION.name(),jsonUtils.getError()));
			else {
				eshc.dynamicIteration = DynamicIteration.fromString(keyword);
				if (eshc.dynamicIteration == DynamicIteration.UNDEFINED)
					conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s':  is incorrect or UNDEFINED!",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.DYNAMIC_ITERATION.name()));				
			}
		}

		// DYNAMIC_ITERATION_COLUMN_INDEX
		if (!node.path(KEYWORD.DYNAMIC_ITERATION_COLUMN_INDEX.name()).isMissingNode()) {
			int col_index = ExcelParserUtils.extractColumnIndex(node.path((KEYWORD.DYNAMIC_ITERATION_COLUMN_INDEX.name())));
			if (col_index == -1) {
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.DYNAMIC_ITERATION_COLUMN_INDEX.name(),jsonUtils.getError()));
			} else {
				eshc.dynamicIterationColumnIndex = col_index;
				eshc.FlagDynamicIterationColumnIndex = true;
			}
		}

		// DYNAMIC_ITERATION_COLUMN_NAME
		if (!node.path(KEYWORD.DYNAMIC_ITERATION_COLUMN_NAME.name()).isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(node, KEYWORD.DYNAMIC_ITERATION_COLUMN_NAME.name(), false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else {
				eshc.dynamicIterationColumnName = keyword;
				eshc.FlagDynamicIterationColumnName = true;
			}
		}

		// SYNCHRONIZATION
		if (!node.path(KEYWORD.SYNCHRONIZATION.name()).isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(node, KEYWORD.SYNCHRONIZATION.name(), true);
			if (keyword == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
						KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.SYNCHRONIZATION.name(),jsonUtils.getError()));
			else {
				eshc.synchronization = SheetSynchronization.fromString(keyword);
				
				if (eshc.synchronization == SheetSynchronization.UNDEFINED)
					conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s':  is incorrect or UNDEFINED!",
							KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.SYNCHRONIZATION.name()));
			}
		}

		// VARIABLES
		JsonNode varNode = node.path(KEYWORD.VARIABLES.name());
		if (!varNode.isMissingNode()) {
			eshc.variableLocations = extractDynamicSection(varNode, conf, null);
		}
		
		/*
		// DYNAMIC_ITERATION_SPAN
		if (!node.path(KEYWORD.DYNAMIC_ITERATION_SPAN.name()).isMissingNode()) {
			DynamicIterationSpan span = DynamicIterationSpan.extractDynamicIterationSpan(
					node.path(KEYWORD.DYNAMIC_ITERATION_SPAN.name()), conf, "PARALLEL_SHEET[" + (jsonArrayIndex + 1) + "]");
			eshc.dynamicIterationSpan = span;
		}

		// COLUMN_SPAN
		if (!node.path(KEYWORD.COLUMN_SPAN.name()).isMissingNode()) {
			ColumnSpan span = extractColumnSpan(node.path(KEYWORD.COLUMN_SPAN.name()), conf,
					"PARALLEL_SHEET[" + (jsonArrayIndex + 1) + "]");
			eshc.columnSpan = span;
		}

		// ROW_SPAN
		if (!node.path(KEYWORD.ROW_SPAN.name()).isMissingNode()) {
			RowSpan span = extractRowSpan(node.path(KEYWORD.ROW_SPAN.name()), conf, "PARALLEL_SHEET[" + (jsonArrayIndex + 1) + "]");
			eshc.rowSpan = span;
		}
		*/

		return eshc;
	}

	public static HashMap<String, ExcelDataLocation> extractDynamicSection(JsonNode node, ExcelParserConfigurator conf,
			String otherLocationFieldNames[]) {
		HashMap<String, ExcelDataLocation> hmap = new HashMap<String, ExcelDataLocation>();

		Iterator<Entry<String, JsonNode>> it = node.fields();
		while (it.hasNext()) {
			Entry<String, JsonNode> entry = it.next();
			ExcelDataLocation loc = ExcelDataLocation.extractDataLocation(entry.getValue(), null, conf,
					otherLocationFieldNames, true);
			loc.sectionName = entry.getKey();
			hmap.put(entry.getKey(), loc);
		}

		return hmap;
	}

	public static CompositionDataLocation extractCompositionDataLocation(JsonNode node, ExcelParserConfigurator conf,
			int jsonArrayIndex) {
		CompositionDataLocation cdl = new CompositionDataLocation();
		JsonUtilities jsonUtils = new JsonUtilities();

		// STRUCTURE_RELATION
		ExcelDataLocation loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.STRUCTURE_RELATION.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				cdl.structureRelation = loc;
		}
		
		/* old approach allowed only direct JSON value from enum list
		if (!node.path(KEYWORD.STRUCTURE_RELATION.name()).isMissingNode()) {
			String keyword = jsonUtils.extractStringKeyword(node, KEYWORD.STRUCTURE_RELATION.name(), true);
			if (keyword == null)
				conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
					KEYWORD.PARALLEL_SHEETS.name(),(jsonArrayIndex + 1), KEYWORD.STRUCTURE_RELATION.name(),jsonUtils.getError()));			
			else {
				cdl.structureRelation = CompositionDataLocation.structureRelationFromString(keyword);
				if (cdl.structureRelation == null)
					conf.configErrors.add(String.format("In JSON Section '%s', the array element %d keyword '%s': %s",
							KEYWORD.SUBSTANCE_RECORD.name(),(jsonArrayIndex + 1), KEYWORD.STRUCTURE_RELATION.name(),keyword));
				
				else
					cdl.FlagStructureRelation = true;
			}
		}
		*/

		// CONTENT
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.CONTENT.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				cdl.content = loc;
		}

		if (!node.path(KEYWORD.CONTENT.name()).isMissingNode()) {
			if (node.path(KEYWORD.FORMAT.name()).isMissingNode())
				conf.configErrors.add(String.format("In JSON Section '%s', subsection '%s', in the array element %d the keyword `%s` is missing",
						KEYWORD.SUBSTANCE_RECORD.name(),KEYWORD.COMPOSITION.name(),(jsonArrayIndex + 1), KEYWORD.FORMAT.name()));
		}

		// FORMAT
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.FORMAT.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				cdl.format = loc;
		}

		// INCHI_KEY
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.INCHI_KEY.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				cdl.inchiKey = loc;
		}

		// INCHI
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.INCHI.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				cdl.inchi = loc;
		}

		// FORMULA
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.FORMULA.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				cdl.formula = loc;
		}

		// SMILES
		loc = ExcelDataLocation.extractDataLocation(node, KEYWORD.SMILES.name(), conf);
		if (loc != null) {
			if (loc.nErrors == 0)
				cdl.smiles = loc;
		}

		// PROPERTIES
		JsonNode propNode = node.path(KEYWORD.PROPERTIES.name());
		if (!propNode.isMissingNode()) {
			cdl.properties = extractDynamicSection(propNode, conf, null);
		}

		// PROPORTION
		JsonNode proportionNode = node.path(KEYWORD.PROPORTION.name());
		if (!proportionNode.isMissingNode()) {
			cdl.proportion = ProportionDataLocation.extractProportion(proportionNode, conf);
		}

		return cdl;
	}
	
	/*
	public static ColumnSpan extractColumnSpan(JsonNode node, ExcelParserConfigurator conf, String masterSection) {
		// TODO
		return null;
	}

	public static RowSpan extractRowSpan(JsonNode node, ExcelParserConfigurator conf, String masterSection) {
		// TODO
		return null;
	}
	*/

	public static void extractJsonRepository(JsonNode node, ExcelParserConfigurator conf) {
		Iterator<Entry<String, JsonNode>> it = node.fields();
		while (it.hasNext()) {
			Entry<String, JsonNode> entry = it.next();
			JsonNode nd = entry.getValue();
			Object o = JsonUtilities.extractObject(nd);
			if (o != null)
				conf.jsonRepository.put(entry.getKey(), o);
		}
	}

	public static boolean isValidQualifier(String qualifier) {
		if (qualifier == null)
			return true;
		String _qualifier = qualifier.trim().toLowerCase();
		for (String q : RecognitionUtils.qualifiers)
			if (q.equals(_qualifier))
				return true;
		return false;
	}

	/*
	public void checkDynamicConfiguration() {
		FlagDynamicSpan = haveDynamicSpan();
		if (!FlagDynamicSpan) {
			if (!basicIterationLoadSubstanceRecord)
				configErrors.add("\"BASIC_ITERATION_LOAD_SUBSTANCE_RECORD\" is set to FALSE "
						+ "and no DYNAMIC_ITERATION_SPAN is present on SUBSTANCE level!");
			return; // No other checks are needed
		}

		analyzeDynamicSpanInfo();

		FlagDynamicSpanOnSubtsanceLevel = haveDynamicSpanOnSubstanceLevel();

		if (!basicIterationLoadSubstanceRecord) {
			if (!FlagDynamicSpanOnSubtsanceLevel)
				configErrors.add("\"BASIC_ITERATION_LOAD_SUBSTANCE_RECORD\" is set to FALSE "
						+ "and no DYNAMIC_ITERATION_SPAN is present on SUBSTANCE level!");

		} else {
			if (FlagDynamicSpanOnSubtsanceLevel)
				configErrors.add("\"BASIC_ITERATION_LOAD_SUBSTANCE_RECORD\" is set to TRUE "
						+ "and DYNAMIC_ITERATION_SPAN is present on SUBSTANCE level!");
		}

		// Checking the consistency of each dynamic section
		if (dynamicIterationSpan != null) {
			dynamicIterationSpan.checkConsistency();

			if (!dynamicIterationSpan.errors.isEmpty())
				for (int i = 0; i < dynamicIterationSpan.errors.size(); i++)
					configErrors.add("Section DATA_ACCESS, subsection DYNAMIC_ITERATION_SPAN " + "incosistency error: "
							+ dynamicIterationSpan.errors.get(i));
		}

		for (int k = 0; k < parallelSheets.size(); k++) {
			DynamicIterationSpan dis = parallelSheets.get(k).dynamicIterationSpan;
			if (dis != null) {
				dis.checkConsistency();

				if (!dis.errors.isEmpty())
					for (int i = 0; i < dis.errors.size(); i++)
						configErrors.add("Section PARALLEL_SHEETS[" + (k + 1) + "], subsection DYNAMIC_ITERATION_SPAN "
								+ "incosistency error: " + dis.errors.get(i));
			}
		}

		// Set default DIS-ids and check for duplicating or empty ids
		ArrayList<String> ids = new ArrayList<String>();
		if (dynamicIterationSpan != null) {
			if (dynamicIterationSpan.FlagId) {
				if (dynamicIterationSpan.id.isEmpty())
					configErrors.add("Section DATA_ACCESS, subsection DYNAMIC_ITERATION_SPAN " + "ID is empty!");
				else
					ids.add(dynamicIterationSpan.id);
			} else {
				dynamicIterationSpan.id = "DIS0";
				ids.add(dynamicIterationSpan.id);
			}
		}

		for (int k = 0; k < parallelSheets.size(); k++) {
			DynamicIterationSpan dis = parallelSheets.get(k).dynamicIterationSpan;
			if (dis != null) {
				if (dis.FlagId) {
					if (dis.id.isEmpty()) {
						configErrors.add("Section PARALLEL_SHEETS[" + (k + 1) + "], subsection DYNAMIC_ITERATION_SPAN "
								+ "ID is empty!");
						continue;
					}
				} else
					dis.id = "DIS" + (k + 1); // default id

				// Check for id duplication
				for (String id : ids) {
					if (id.equals(dis.id)) {
						configErrors.add("Section PARALLEL_SHEETS[" + (k + 1) + "], subsection DYNAMIC_ITERATION_SPAN "
								+ "ID " + dis.id + " is duplicated!");
						break;
					}
				}

				// Register new id
				ids.add(dis.id);
			}
		}

		// Checking the interrelated consistency of all dynamic sections
		// together
		// TODO
	}

	public boolean haveDynamicSpanOnSubstanceLevel() {

		logger.info("dynamicSpanInfo:\n" + dynamicSpanInfo.toString());

		if ((dynamicSpanInfo.substanceArray_Index != DynamicSpanInfo.INDEX_NONE)
				|| (dynamicSpanInfo.substance_Indices != null))
			return true;

		return false;
	}

	public boolean haveDynamicSpan() {
		if (dynamicIterationSpan != null)
			return true;

		for (int i = 0; i < parallelSheets.size(); i++)
			if (parallelSheets.get(i).dynamicIterationSpan != null)
				return true;

		return false;
	}

	public void analyzeDynamicSpanInfo() {
		dynamicSpanInfo = new DynamicSpanInfo();

		ArrayList<Integer> substanceArray_Indices = new ArrayList<Integer>();
		ArrayList<DynamicIterationSpan> substanceArray_DS = new ArrayList<DynamicIterationSpan>();

		ArrayList<Integer> substance_Indices = new ArrayList<Integer>();
		ArrayList<DynamicIterationSpan> substance_DS = new ArrayList<DynamicIterationSpan>();

		if (dynamicIterationSpan != null) {
			switch (dynamicIterationSpan.cumulativeObjectType) {
			case SUBSTANCE_ARRAY:
				substanceArray_Indices.add(DynamicSpanInfo.INDEX_PRIMARY_SHEET);
				substanceArray_DS.add(dynamicIterationSpan);
				break;
			case SUBSTANCE:
				substance_Indices.add(DynamicSpanInfo.INDEX_PRIMARY_SHEET);
				substance_DS.add(dynamicIterationSpan);
				break;

			default:
				break;
			}

		}

		for (int i = 0; i < parallelSheets.size(); i++)
			if (parallelSheets.get(i).dynamicIterationSpan != null) {
				switch (parallelSheets.get(i).dynamicIterationSpan.cumulativeObjectType) {
				case SUBSTANCE_ARRAY:
					substanceArray_Indices.add(i);
					substanceArray_DS.add(parallelSheets.get(i).dynamicIterationSpan);
					break;
				case SUBSTANCE:
					substance_Indices.add(i);
					substance_DS.add(parallelSheets.get(i).dynamicIterationSpan);
					break;

				default:
					break;
				}
			}

		if (!substanceArray_Indices.isEmpty()) {
			if (substanceArray_Indices.size() == 1) {
				dynamicSpanInfo.substanceArray_Index = substanceArray_Indices.get(0);
				dynamicSpanInfo.substanceArray_DS = substanceArray_DS.get(0);
			} else {
				configErrors.add("Dynamic span on SUBSTANCE_ARRAY level is duplicated in following sheets: "
						+ DynamicSpanInfo.indicesToSheetMessageString(substanceArray_Indices));
			}
		}

		if (!substance_Indices.isEmpty()) {
			dynamicSpanInfo.substance_Indices = new int[substance_Indices.size()];
			dynamicSpanInfo.substance_DS = new DynamicIterationSpan[substance_Indices.size()];
			for (int i = 0; i < substance_Indices.size(); i++) {
				dynamicSpanInfo.substance_Indices[i] = substance_Indices.get(i);
				dynamicSpanInfo.substance_DS[i] = substance_DS.get(i);
			}
		}

	}
	*/

	public String generateUUID(String s) {
		return generateUUID(prefix, s);
	}

	/**
	 * 
	 * @param s
	 * @param prefix
	 * @return s, if "prefix-valid_UUID" , otherwise
	 *         "prefix-generated_uuid_from_string"
	 */
	public static String generateUUID(String prefix, String s) {
		String uuid = s;
		if (s != null) {
			if (prefix.length() < uuid.length())
				try {
					UUID.fromString(s.substring(prefix.length() + 1, uuid.length()));
					// if this parses without error, then it is the right format
					return uuid;
				} catch (IllegalArgumentException x) {
					// not the right format, will generate UUID from string then
					logger.log(Level.FINE, x.getMessage());

				}
			return String.format("%s-%s", prefix, UUID.nameUUIDFromBytes(uuid.getBytes()).toString().trim());
		} else
			return null;

	}
}
