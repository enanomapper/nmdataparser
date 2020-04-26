package net.enanomapper.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.IChemObjectReaderErrorHandler;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.listener.IChemObjectIOListener;
import org.openscience.cdk.io.setting.IOSetting;

import ambit2.base.data.Property;
import ambit2.base.data.StructureRecord;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.study.ReliabilityParams;
import ambit2.base.data.study.Value;
import ambit2.base.data.substance.ExternalIdentifier;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.relation.composition.CompositionRelation;
import ambit2.base.relation.composition.Proportion;
import ambit2.core.io.IRawReader;
import net.enanomapper.parser.BlockValueGroupExtractedInfo.ParamInfo;
import net.enanomapper.parser.ParserConstants.BlockParameterAssign;
import net.enanomapper.parser.ParserConstants.DataInterpretation;
import net.enanomapper.parser.ParserConstants.DynamicIteration;
import net.enanomapper.parser.ParserConstants.IterationAccess;
import net.enanomapper.parser.excel.ExcelUtils;
import net.enanomapper.parser.exceptions.CellException;
import net.enanomapper.parser.exceptions.ExceptionAtLocation;
import net.enanomapper.parser.json.JsonUtilities;
import net.enanomapper.parser.recognition.RecognitionUtils;
import net.enanomapper.parser.recognition.RichValue;
import net.enanomapper.parser.recognition.RichValueParser;

/**
 * 
 * @author nick
 * 
 */
public class GenericExcelParser extends ExcelParserCore implements IRawReader<IStructureRecord> {

	
	protected RichValueParser rvParser = new RichValueParser();
	// protected ArrayList<String> parseErrors = new ArrayList<String>();
	protected InputStream input;
		
	protected boolean xlsxFormat = false;

	protected ExcelDataBlockUtils dataBlockUtils = null; 

	// Primary sheet
	protected Sheet primarySheet = null;
	protected int curRowNum = 1; // Used for iteration
	protected int curReadRowNum = 1; // This shows the actual read rows in
										// multi-dynamic iteration mode
	protected int curCellNum = 1;
	protected int iterationLastRowNum = 1;
	
	//protected Iterator<Row> rowIt = null;
	//protected Cell curCell = null;
	protected String primarySheetSynchKey = null;
	
	// Helpers for condition references
	protected HashMap<String, EffectRecord> referencesEffectRecord = new HashMap<String, EffectRecord>();
	protected ArrayList<String> addConditionRef = new ArrayList<String>();
	protected ArrayList<EffectRecord> addConditionTargetEffectRecord = new ArrayList<EffectRecord>();

	protected boolean FlagNextRecordLoaded = false; // This flag is true when
	// next object is iterated
	// and successfully read to
	// the buffer;
	protected SubstanceRecord basicSubstanceRecord = null;
	protected SubstanceRecord nextRecordBuffer = null;
	protected int nextRecordIndex = -1;

	protected ArrayList<SubstanceRecord> loadedRecordsBuffer = new ArrayList<SubstanceRecord>();
	
	protected boolean FlagVariableMappingLogging = true;
	// protected boolean FlagAddParserStringError = true; // This is used to
	// switch off errors in some cases
	
	
	/**
	 * 
	 * @param input
	 * @param jsonConfig
	 * @throws Exception
	 */
	public GenericExcelParser(InputStream input, File jsonConfig) throws Exception {
		this(input, jsonConfig, true);
	}

	/**
	 * 
	 * @param input
	 * @param jsonConfig
	 * @param xlsxFormat
	 * @throws Exception
	 */
	public GenericExcelParser(InputStream input, File jsonConfig, boolean xlsxFormat) throws Exception {
		this(input, jsonConfig, xlsxFormat, "XLSX");
	}

	public GenericExcelParser(InputStream input, File jsonConfig, boolean xlsxFormat, String prefix) throws Exception {
		super();
		this.xlsxFormat = xlsxFormat;
		this.input = input;

		config = ExcelParserConfigurator.loadFromJSON(jsonConfig);
		config.setPrefix(prefix);
		if (config.hasErrors())
			throw new Exception("GenericExcelParser configuration errors:\n" + config.getAllErrorsAsString());

		setReader(input);
		init();
	}

	public ExcelParserConfigurator getExcelParserConfigurator() {
		return config;
	}
	
	
	public boolean isFlagVariableMappingLogging() {
		return FlagVariableMappingLogging;
	}

	public void setFlagVariableMappingLogging(boolean flagVariableMappingLogging) {
		FlagVariableMappingLogging = flagVariableMappingLogging;
	}

	protected void init() throws Exception {
		handleConfigRecognitions();

		// Setting of the basic sheet work variables
		initBasicWorkSheet();
		
		//Initial setup of the ExcelDataBlockUtils
		initDataBlockUtils();

		// Setting the parallel sheets work variables
		if (!config.parallelSheets.isEmpty()) {
			initParallelSheets();
			handleParallelSheetIndices();
			if (!parallelSheetsErrors.isEmpty())
				throw new Exception(paralleSheetsErrorsToString());
		}

		initialIteration();
		FlagNextRecordLoaded = false;
		nextRecordBuffer = null;

	}

	protected void initBasicWorkSheet() throws Exception 
	{
		if (config.FlagSheetIndex)
			primarySheetNum = config.sheetIndex;
		else
			if (config.FlagSheetName)
			{
				//Sheet index is determined from the sheet name
				primarySheetNum = workbook.getSheetIndex(config.sheetName);
				if (primarySheetNum < 0)
				{
					throw new Exception("Primary excel sheet is not specified correctly via SHEET_NAME keyword. "
							+ "Sheet \"" + config.sheetName + "\" does not exist.");
				}	
			}
			else
			{	
				throw new Exception("Primary excel sheet is not specified. "
						+ "SHEET_INDEX or SHEET_NAME keyword is required in section DATA_ACCESS");
			}
		
		primarySheet = workbook.getSheetAt(primarySheetNum);
		curRowNum = config.startRow;

		iterationLastRowNum = primarySheet.getLastRowNum();
		if (config.FlagEndRow)
			if (config.endRow < iterationLastRowNum)
				iterationLastRowNum = config.endRow;

		logger.info("primarySheet# = " + (primarySheetNum + 1) + "   starRow# = " + (curRowNum + 1) + "\n"
				+ "Last row# = " + (primarySheet.getLastRowNum() + 1));
	}

	protected void initParallelSheets() throws Exception {
		parallelSheetStates = new ParallelSheetState[config.parallelSheets.size()];
		for (int i = 0; i < config.parallelSheets.size(); i++) {
			ExcelSheetConfiguration eshc = config.parallelSheets.get(i);
			parallelSheetStates[i] = new ParallelSheetState();
			// if (eshc.FlagSheetIndex) //this check should not be needed
			// because sheetNum must set via sheetName as well
			parallelSheetStates[i].sheetNum = eshc.sheetIndex;
			parallelSheetStates[i].synchronization = eshc.synchronization;
			parallelSheetStates[i].dynamicIteration = eshc.dynamicIteration;
			parallelSheetStates[i].dynamicIterationColumnIndex = eshc.dynamicIterationColumnIndex;

			if (0 <= parallelSheetStates[i].sheetNum && parallelSheetStates[i].sheetNum < workbook.getNumberOfSheets())
				parallelSheetStates[i].sheet = workbook.getSheetAt(parallelSheetStates[i].sheetNum);
			else {
				throw new Exception("Incorrect SHEET_INDEX " + (parallelSheetStates[i].sheetNum + 1)
						+ " in parallel sheet #" + (i + 1));
			}

			parallelSheetStates[i].curRowNum = eshc.startRow;
		}
	}

	protected void handleConfigRecognitions() {
		Set<Entry<String, ExcelDataLocation>> locEntries = config.substanceLocations.entrySet();
		for (Entry<String, ExcelDataLocation> entry : locEntries) {
			ExcelDataLocation loc = entry.getValue();
			handleRecognition(loc);
		}

		for (ProtocolApplicationDataLocation ploc : config.protocolAppLocations)
			handleRecognition(ploc);
	}

	protected void handleRecognition(ExcelDataLocation loc) {
		// TODO
	}

	protected void handleRecognition(ProtocolApplicationDataLocation paLocation) {
		// TODO
	}

	protected void handleRecognition(EffectRecordDataLocation efrdl) {
		// TODO
	}

	protected void handleParallelSheetIndices() {
		for (String key : config.substanceLocations.keySet()) {
			ExcelDataLocation loc = config.substanceLocations.get(key);
			setParallelSheet(loc);
		}

		for (ProtocolApplicationDataLocation padl : config.protocolAppLocations)
			setParallelSheets(padl);

		for (CompositionDataLocation cdl : config.composition)
			cdl.setParallelSheets(parallelSheetStates, primarySheetNum, parallelSheetsErrors);

		for (ExternalIdentifierDataLocation eidl : config.externalIdentifiers)
			eidl.setParallelSheets(parallelSheetStates, primarySheetNum, parallelSheetsErrors);
	}

	/**
	 * 
	 * @param loc
	 */
	protected void setParallelSheet(ExcelDataLocation loc) {
		if (loc.sheetIndex != primarySheetNum) {
			for (int i = 0; i < parallelSheetStates.length; i++)
				if (loc.sheetIndex == parallelSheetStates[i].sheetNum) {
					loc.setParallelSheetIndex(i);
					return;
				}

			if (loc.iteration != ParserConstants.IterationAccess.ABSOLUTE_LOCATION) // This
																					// iteration
																					// mode
																					// is
																					// not
																					// treated
																					// as
																					// error
				parallelSheetsErrors.add(
						"[" + locationStringForErrorMessage(loc) + "] Sheet number number not valid parallel sheet!");
		}
	}

	/**
	 * 
	 * @param padl
	 */
	protected void setParallelSheets(ProtocolApplicationDataLocation padl) {
		if (padl.citationOwner != null)
			setParallelSheet(padl.citationOwner);

		if (padl.citationTitle != null)
			setParallelSheet(padl.citationTitle);

		if (padl.citationOwner != null)
			setParallelSheet(padl.citationOwner);

		if (padl.protocolTopCategory != null)
			setParallelSheet(padl.protocolTopCategory);

		if (padl.protocolCategoryCode != null)
			setParallelSheet(padl.protocolCategoryCode);

		if (padl.protocolCategoryTitle != null)
			setParallelSheet(padl.protocolCategoryTitle);

		if (padl.protocolEndpoint != null)
			setParallelSheet(padl.protocolEndpoint);

		if (padl.protocolGuideline != null)
			for (ExcelDataLocation loc : padl.protocolGuideline)
				setParallelSheet(loc);

		if (padl.parameters != null)
			for (String param : padl.parameters.keySet()) {
				ExcelDataLocation loc = padl.parameters.get(param);
				setParallelSheet(loc);
			}

		if (padl.reliability_isRobustStudy != null)
			setParallelSheet(padl.reliability_isRobustStudy);

		if (padl.reliability_isUsedforClassification != null)
			setParallelSheet(padl.reliability_isUsedforClassification);

		if (padl.reliability_isUsedforMSDS != null)
			setParallelSheet(padl.reliability_isUsedforMSDS);

		if (padl.reliability_purposeFlag != null)
			setParallelSheet(padl.reliability_purposeFlag);

		if (padl.reliability_studyResultType != null)
			setParallelSheet(padl.reliability_studyResultType);

		if (padl.reliability_value != null)
			setParallelSheet(padl.reliability_value);

		if (padl.interpretationResult != null)
			setParallelSheet(padl.interpretationResult);

		if (padl.interpretationCriteria != null)
			setParallelSheet(padl.interpretationCriteria);

		if (padl.effects != null)
			for (EffectRecordDataLocation efrdl : padl.effects)
				setParallelSheets(efrdl);
	}

	protected String paralleSheetsErrorsToString() {
		StringBuffer sb = new StringBuffer();
		for (String s : parallelSheetsErrors)
			sb.append(s + "\n");
		return sb.toString();
	}

	/**
	 * 
	 * @param efrdl
	 */
	protected void setParallelSheets(EffectRecordDataLocation efrdl) {
		if (efrdl.sampleID != null)
			setParallelSheet(efrdl.sampleID);

		if (efrdl.endpoint != null)
			setParallelSheet(efrdl.endpoint);

		if (efrdl.conditions != null)
			for (String key : efrdl.conditions.keySet()) {
				ExcelDataLocation loc = efrdl.conditions.get(key);
				setParallelSheet(loc);
			}

		if (efrdl.unit != null)
			setParallelSheet(efrdl.unit);

		if (efrdl.loValue != null)
			setParallelSheet(efrdl.loValue);

		if (efrdl.loQualifier != null)
			setParallelSheet(efrdl.loQualifier);

		if (efrdl.upValue != null)
			setParallelSheet(efrdl.upValue);

		if (efrdl.upQualifier != null)
			setParallelSheet(efrdl.upQualifier);

		if (efrdl.textValue != null)
			setParallelSheet(efrdl.textValue);

		if (efrdl.errValue != null)
			setParallelSheet(efrdl.errValue);

		if (efrdl.errQualifier != null)
			setParallelSheet(efrdl.errQualifier);

		if (efrdl.value != null)
			setParallelSheet(efrdl.value);

	}
	
	protected void initDataBlockUtils()
	{
		//TODO add check whether there is a need for using dataBlockUtils 
		dataBlockUtils = new ExcelDataBlockUtils(); 
		dataBlockUtils.setLogger(logger);
		dataBlockUtils.setConfig(config);
		dataBlockUtils.setWorkbook(workbook);	
	}
	
	protected void setDataBlockUtils()
	{
		dataBlockUtils.setCurRowNum(curRowNum);
		dataBlockUtils.setCurRows(curRows);
		dataBlockUtils.setCurVariables(curVariables);
		dataBlockUtils.setCurVariableMappings(curVariableMappings);
	}

	/**
	 * 
	 */
	@Override
	public void setReader(Reader reader) throws CDKException {
		throw new CDKException("setReader(Reader arg0) - Not implemented");
	}

	@Override
	public void setReader(InputStream arg0) throws CDKException {
		try {
			if (xlsxFormat)
				workbook = new XSSFWorkbook(input);
			else
				workbook = new HSSFWorkbook(input);

		} catch (Exception x) {
			throw new CDKException(x.getMessage(), x);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			if (input != null)
				input.close();
		} catch (Exception x) {

		} finally {
			workbook = null;
			input = null;
		}

	}

	/**
	 * 
	 */
	@Override
	public boolean hasNext() {

		if (FlagNextRecordLoaded) // Next record is already read and loaded to
			// the buffer
			return true;

		if (nextRecordIndex != -1) {
			nextRecordIndex++;
			if (nextRecordIndex < loadedRecordsBuffer.size()) {
				nextRecordBuffer = loadedRecordsBuffer.get(nextRecordIndex);
				FlagNextRecordLoaded = true;
				return true;
			} else
				nextRecordIndex = -1; // Reached the end of loadedRecordsBuffer
		}

		if (nextRecordIndex == -1) // Loading new buffer
		{
			if (hasExcelDataForNextRecord()) {
				// This is the actual reading of next substance record/records
				try {
					readVariables();
					if (dataBlockUtils != null)
						setDataBlockUtils();

					loadSubstanceRecords();
					logger.info("#### Loaded " + loadedRecordsBuffer.size() + " substances into the buffer");
					if (loadedRecordsBuffer.isEmpty())
						FlagNextRecordLoaded = false;
					else {
						nextRecordIndex = 0;
						nextRecordBuffer = loadedRecordsBuffer.get(nextRecordIndex);
						FlagNextRecordLoaded = true;
					}

					// nextRecordBuffer = getBasicSubstanceRecord();
					// if (nextRecordBuffer == null)
					// nextRecordBuffer = new SubstanceRecord();
					// FlagNextRecordLoaded = true;

					if (config.FlagSkipRows)
						iterateExcel_skipRows();
					else
						iterateExcel();
					return FlagNextRecordLoaded;
				} catch (Exception x) {
					logger.log(Level.SEVERE, x.getMessage(), x);
					return false;
				}
			} else
				return false;
		} else
			return true;
	}

	private boolean hasExcelDataForNextRecord() {
		switch (config.substanceIteration) {
		case ROW_SINGLE:
		case ROW_MULTI_DYNAMIC:
			// Decision logic: at least one row must be left on the primary
			// sheet
			if (curRowNum <= iterationLastRowNum /*
													 * primarySheet.
													 * getLastRowNum()
													 */)
				return true;
			else
				return false;

		case ROW_MULTI_FIXED:
			// Decision logic: at least config.rowMultiFixedSize rows must be
			// left on the primary sheet
			if (curRowNum <= iterationLastRowNum - config.rowMultiFixedSize)
				return true;
			else
				return false;

		default:
			return false;
		}
	}

	/**
	 * 
	 */
	@Override
	public Object next() {
		return nextRecord();
	}

	/**
	 * 
	 */
	@Override
	public SubstanceRecord nextRecord() {
		if (hasNext()) {
			SubstanceRecord result = nextRecordBuffer;
			nextRecordBuffer = null; // Invalidate (empty) the buffer for the
			// next record
			FlagNextRecordLoaded = false; // Invalidate for the next record
			return result;
		} else
			return null;
	}

	protected void readVariables() throws Exception {
		curVariables.clear();
		if (config.variableLocations != null) {
			for (String var : config.variableLocations.keySet()) {
				// Treating source combination or array
				ExcelDataLocation loc = config.variableLocations.get(var);
				if (loc.sourceCombination) {
					// TODO source combination for variables should not be
					// needed
					continue;
				} else if (loc.isArray) {
					Object arrayObj[] = getArray(loc);
					if (arrayObj != null) {
						curVariables.put(var, arrayObj);

						String s = "";
						for (int i = 0; i < arrayObj.length; i++)
							if (arrayObj[i] != null)
								s += (" " + arrayObj[i].toString());
							else
								s += " null";

						logger.info("%%%%%%%%%%% Reading array variable " + s);

						continue;
					}
				}
				String s = null;
				try {
					s = getStringValue(loc);
				} catch (Exception x) {
					logger.log(Level.FINE, x.getMessage());
				}
				if (s != null)
					curVariables.put(var, s);
				else {
					Number d = getNumericValue(loc);
					if (d != null)
						curVariables.put(var, d);
				}
			}
		}

		for (ExcelSheetConfiguration eshc : config.parallelSheets) {
			if (eshc.variableLocations != null) {
				for (String var : eshc.variableLocations.keySet()) {
					ExcelDataLocation loc = eshc.variableLocations.get(var);
					// Treating source combination or array
					if (loc.sourceCombination) {
						// TODO source combination for variables should not be
						// needed
						continue;
					} else if (loc.isArray) {
						Object arrayObj[] = getArray(loc);
						if (arrayObj != null)
							curVariables.put(var, arrayObj);

						continue;
					}

					String s = null;
					try {
						s = getStringValue(loc);
					} catch (Exception x) {
						logger.log(Level.FINE, x.getMessage());
					}
					if (s != null)
						curVariables.put(var, s);
					else {
						Number d = getNumericValue(loc);
						if (d != null)
							curVariables.put(var, d);
					}
				}
			}
		}

		// Make variable mapping
		if (config.variableMappings != null) {
			curVariableMappings.clear();
			for (VariableMapping vm : config.variableMappings) {
				HashMap<Object, Object> map = makeMapping(vm);
				if (map != null)
					curVariableMappings.put(vm.name, map);
			}
		}

	}

	protected HashMap<Object, Object> makeMapping(VariableMapping varMapping) {
		// Set mapping keys
		Object varObj = curVariables.get(varMapping.keysVariable);
		if (varObj == null) {
			logger.info("---- Variable mapping error in mapping: \"" + varMapping.name + "\",  KEYS_VARIABLE \""
					+ varMapping.keysVariable + "\" is missing");
			return null;
		}

		Object keys[] = null;
		if ((varObj instanceof Double) || (varObj instanceof Double)) {
			keys = new Object[1];
			keys[0] = varObj;
		}

		if (varObj instanceof Object[]) {
			keys = (Object[]) varObj;
		}

		if (keys == null)
			return null;

		// Set mapping keys
		varObj = curVariables.get(varMapping.valuesVariable);
		if (varObj == null) {
			logger.info("---- Variable mapping error in mapping: \"" + varMapping.name + "\",  VALUES_VARIABLE \""
					+ varMapping.valuesVariable + "\" is missing");
			return null;
		}

		Object values[] = null;
		if ((varObj instanceof Double) || (varObj instanceof Double)) {
			values = new Object[1];
			values[0] = varObj;
		}

		if (varObj instanceof Object[]) {
			values = (Object[]) varObj;
		}

		if (values == null)
			return null;

		if (FlagVariableMappingLogging)
			logger.info("---- Variable mapping: " + varMapping.name);
		
		// Make mapping
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		for (int i = 0; i < keys.length; i++) {
			if (i >= values.length) // Reached the end of values array
				break;
			map.put(keys[i], values[i]);
			
			if (FlagVariableMappingLogging)
				logger.info("---- " + keys[i] + " --> " + values[i]);
		}

		return map;
	}

	protected void initialIteration() {
		switch (config.substanceIteration) {
		case ROW_SINGLE: {
			if (config.FlagSkipRows) {
				while (config.skipRowsIndexSet.contains(curRowNum) && curRowNum <= primarySheet.getLastRowNum())
					curRowNum++;
			}
			curRow = primarySheet.getRow(curRowNum);

			// Initial iteration for each parallel sheet
			if (parallelSheetStates != null)
				for (int i = 0; i < parallelSheetStates.length; i++) {
					parallelSheetStates[i].curRow = parallelSheetStates[i].sheet
							.getRow(parallelSheetStates[i].curRowNum);
				}
		}
			break;

		case ROW_MULTI_FIXED:
		case ROW_MULTI_DYNAMIC: {
			// Skip starting empty rows
			while (curRowNum <= primarySheet.getLastRowNum()) {
				curRow = primarySheet.getRow(curRowNum);
				if (isEmpty(curRow)) {
					// Empty row is skipped
					curRowNum++;
					continue;
				} else
					break;
			}

			readRowsMultiDynamic();

			// Initial iteration for each parallel sheet
			if (parallelSheetStates != null)
				for (int i = 0; i < parallelSheetStates.length; i++) {
					switch (config.parallelSheets.get(i).synchronization) {
					case NONE:
						parallelSheetStates[i].initialIterateToNextNonEmptyRow();
						parallelSheetStates[i].readRowsMultiDynamic(primarySheetSynchKey);
						break;

					case MATCH_KEY:
						parallelSheetStates[i].initialIterateToNextNonEmptyRow();
						boolean FlagNextNonEmpty = (config.parallelSheets
								.get(i).dynamicIteration == DynamicIteration.NEXT_NOT_EMPTY);
						parallelSheetStates[i].setRowGroups(config.parallelSheets.get(i).dynamicIterationColumnIndex,
								FlagNextNonEmpty);
						parallelSheetStates[i].readRowsMultiDynamic(primarySheetSynchKey);
						break;

					default:
					}
				}

			// TODO if needed check the first non empty row
		}

		default:
			break;
		}
	}

	/**
	 * 
	 * @return
	 */
	protected int iterateExcel() {
		switch (config.substanceIteration) {
		case ROW_SINGLE:
			if (config.Fl_SkipEmptyRows) {
				int res = iterateToNextNonEmptyRow();
				if (parallelSheetStates != null)
					for (int i = 0; i < parallelSheetStates.length; i++) {
						parallelSheetStates[i].iterateToNextNonEmptyRow();
					}
				return res;
			} else {
				curRowNum++;
				curRow = primarySheet.getRow(curRowNum);
				if (curRow == null) {
					// parseErrors.add("Row " + curRowNum + " is empty!");
					logger.log(Level.WARNING, String.format("Row %d is empty", curRowNum));
					return -1;
				} else
					return curRowNum;

				// TODO iterate parallel sheets
			}

		case ROW_MULTI_FIXED:
			for (int i = 0; i < config.rowMultiFixedSize; i++) {
				curRowNum++;
				// TODO
			}
			break;

		case ROW_MULTI_DYNAMIC:
			iterateRowMultiDynamic();
			if (parallelSheetStates != null)
				for (int i = 0; i < parallelSheetStates.length; i++)
					parallelSheetStates[i].iterateRowMultiDynamic(primarySheetSynchKey);
			break;

		default:
			curRowNum++;
		}

		return 0;
	}

	protected void iterateExcel_skipRows() {
		iterateExcel();
		while (config.skipRowsIndexSet.contains(curRowNum) && curRowNum <= primarySheet.getLastRowNum()) {
			iterateExcel();
		}
	}

	protected int iterateToNextNonEmptyRow() {
		curRowNum++;
		while (curRowNum <= primarySheet.getLastRowNum()) {
			curRow = primarySheet.getRow(curRowNum);
			if (!isEmpty(curRow)) {
				return curRowNum;
			}
			curRowNum++;
		}

		return -1;
	}

	/**
	 * 
	 */
	protected void iterateRowMultiDynamic() {
		curRowNum = curReadRowNum;
		readRowsMultiDynamic();
	}

	/**
	 * 
	 */
	protected void readRowsMultiDynamic() {
		logger.info("----- Primary Sheet - Reading at row: " + (curRowNum + 1));

		if (curRowNum <= primarySheet.getLastRowNum())
			curRows = new ArrayList<Row>();
		else {
			curRows = null;
			primarySheetSynchKey = null;
			logger.info("----- read no rows ");
			return;
		}

		// The first row is already checked to be non empty
		curRow = primarySheet.getRow(curRowNum);
		Cell c0 = curRow.getCell(config.dynamicIterationColumnIndex);
		primarySheetSynchKey = ExcelUtils.getStringFromCell(c0);
		logger.info("synch key: " + primarySheetSynchKey);

		curReadRowNum = curRowNum; // curRowNum is not changed here. It is
									// updated in iterateRowMultiDynamic()
		Row r = curRow;
		curRows.add(r);
		curReadRowNum++;

		switch (config.dynamicIteration) {
		case NEXT_NOT_EMPTY: {
			while (curReadRowNum <= primarySheet.getLastRowNum()) {
				r = primarySheet.getRow(curReadRowNum);
				if (isEmpty(r)) {
					// Empty row is skipped
					curReadRowNum++;
					continue;
				} else {
					Cell c = r.getCell(config.dynamicIterationColumnIndex);
					if (ExcelUtils.isEmpty(c)) {
						curRows.add(r);
						curReadRowNum++;
					} else {
						logger.info("****  read " + curRows.size() + " rows /  next key: " + c);
						return; // Reached next record
					}
				}
			} // end of while
			logger.info(" read " + curRows.size() + " rows");
		}
			break;

		case NEXT_DIFFERENT_VALUE: {
			while (curReadRowNum <= primarySheet.getLastRowNum()) {
				r = primarySheet.getRow(curReadRowNum);
				if (isEmpty(r)) {
					// Empty row is skipped
					curReadRowNum++;
					continue;
				} else {
					Cell c = r.getCell(config.dynamicIterationColumnIndex);
					String sval = ExcelUtils.getStringFromCell(c);
					if ((sval != null) && sval.equals(primarySheetSynchKey)) {
						curRows.add(r);
						curReadRowNum++;
					} else {
						logger.info("****  read " + curRows.size() + " rows /  next key: " + c);
						return; // Reached next record
					}
				}
			} // end of while
			logger.info(" read " + curRows.size() + " rows");
		}
			break;

		case ROW_LIST: {
			// TODO
		}
			break;

		default:
			break;
		}
	}

	protected boolean isEmpty(Row row) {
		if (row == null)
			return true;
		else {
			if (config.Fl_FullCheckForEmptyColumnsAndRows) {
				for (int i = 0; i <= row.getLastCellNum(); i++) {
					Cell c = row.getCell(i);
					if (ExcelUtils.isEmpty(c))
						continue;
					else
						return false;
				}
				return true;
			}

			return false;
		}
	}

	/*
	 * protected boolean isEmpty (Column col) { if (col == null) return true;
	 * else { //TODO - to check whether this Column is really empty return
	 * false; } }
	 */

	/**
	 * This function uses a generic approach (the generic variants of the helper
	 * functions) The iteration access mode is handled in the specific overloads
	 * of the functions.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected SubstanceRecord getBasicSubstanceRecord() throws Exception {
		if (config.substanceIteration == IterationAccess.ROW_SINGLE)
			logger.info("Reading row: " + (curRowNum + 1));

		SubstanceRecord r = new SubstanceRecord();
		clearReferencesInfo();

		// Typically substanceUUID is not set from the excel file but it is
		// possible if needed.
		ExcelDataLocation loc = config.substanceLocations.get("SubstanceRecord.substanceUUID");
		if (loc != null) {
			String s = getString(loc);
			if (s != null && !"".equals(s.trim())) {
				r.setSubstanceUUID(ExcelParserConfigurator.generateUUID(config.getPrefix(), s));
			}
		}

		loc = config.substanceLocations.get("SubstanceRecord.referenceSubstanceUUID");
		if (loc != null) {
			String s = getString(loc);
			if (s != null)
				r.setReferenceSubstanceUUID(ExcelParserConfigurator.generateUUID(config.getPrefix(), s));
		}

		loc = config.substanceLocations.get("SubstanceRecord.substanceName");
		if (loc != null) {
			String s = getString(loc);
			if (s != null)
				r.setSubstanceName(s);
		}

		// Typically ownerUUID is not set from the excel file but it is possible
		// if needed.
		loc = config.substanceLocations.get("SubstanceRecord.ownerUUID");
		if (loc != null) {
			String s = getString(loc);
			if (s != null)
				r.setOwnerUUID(s);
		}

		loc = config.substanceLocations.get("SubstanceRecord.ownerName");
		if (loc != null) {
			String s = getString(loc);
			if (s != null)
				r.setOwnerName(s);
		}

		loc = config.substanceLocations.get("SubstanceRecord.substanceType");
		if (loc != null) {
			String s = getString(loc);
			if (s != null)
				r.setSubstancetype(s);
		}

		loc = config.substanceLocations.get("SubstanceRecord.publicName");
		if (loc != null) {
			String s = getString(loc);
			if (s != null)
				r.setPublicName(s);
		}

		loc = config.substanceLocations.get("SubstanceRecord.idSubstance");
		if (loc != null) {
			Number v = getNumericValue(loc);
			if (v != null)
				r.setIdsubstance(v.intValue());
		}

		List<ProtocolApplication> measurements = readProtocolApplications();
		r.setMeasurements(measurements);

		putSRInfoToProtocolApplications(r);

		for (CompositionDataLocation cdl : config.composition) {
			CompositionRelation relation = readCompositionRelation(cdl, r);
			r.addStructureRelation(relation);
		}

		if (!config.externalIdentifiers.isEmpty()) {
			List<ExternalIdentifier> ids = new ArrayList<ExternalIdentifier>();
			for (ExternalIdentifierDataLocation eidl : config.externalIdentifiers) {
				String id = null;
				String type = null;
				if (eidl.id != null)
					id = getString(eidl.id);
				if (eidl.type != null)
					type = getString(eidl.type);

				if ((id != null) && (type != null))
					ids.add(new ExternalIdentifier(type, id));
			}
			r.setExternalids(ids);
		}

		// Condition definition by references (if present)
		setupReferenceInfo();

		return r;
	}

	/**
	 * 
	 * @throws Exception
	 */
	protected void loadSubstanceRecords() throws Exception {
		loadedRecordsBuffer.clear();

		if (config.basicIterationLoadSubstanceRecord) {
			basicSubstanceRecord = getBasicSubstanceRecord();
			loadedRecordsBuffer.add(basicSubstanceRecord);
		} else {
			basicSubstanceRecord = null;
		}

		/*
		if (!config.FlagDynamicSpan)
			return;
				
		// Handle dynamic span
		DIOSynchronization dioSynch = new DIOSynchronization(basicSubstanceRecord, config.dynamicSpanInfo, config);

		if (config.dynamicIterationSpan != null) {
			switch (config.substanceIteration) {
			case ROW_MULTI_FIXED:
			case ROW_MULTI_DYNAMIC:
				DynamicIterationObject dio = config.dynamicIterationSpan.getDynamicIterationObjectFromRows(curRows);
				dioSynch.addDIO(config.dynamicIterationSpan, dio);
				break;
			}
		}
		

		if (parallelSheetStates != null)
			for (int i = 0; i < parallelSheetStates.length; i++) {
				switch (config.parallelSheets.get(i).iteration) {
				case ROW_MULTI_FIXED:
				case ROW_MULTI_DYNAMIC:
					DynamicIterationObject dio = config.parallelSheets.get(i).dynamicIterationSpan
							.getDynamicIterationObjectFromRows(parallelSheetStates[i].curRows);
					dioSynch.addDIO(config.parallelSheets.get(i).dynamicIterationSpan, dio);
					break;
				}
			}

		ArrayList<SubstanceRecord> records = dioSynch.synchronize();
		loadedRecordsBuffer.addAll(records);
		*/
	}

	protected void putSRInfoToProtocolApplications(SubstanceRecord record) {
		for (ProtocolApplication pa : record.getMeasurements()) {
			// pa.setCompanyName(record.getCompanyName());
			pa.setSubstanceUUID(record.getSubstanceUUID());
			pa.setCompanyName(record.getOwnerName());
			pa.setCompanyUUID(record.getOwnerUUID());
			// TODO
		}
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	protected List<ProtocolApplication> readProtocolApplications() throws Exception {
		List<ProtocolApplication> protApps = new ArrayList<ProtocolApplication>();
		for (ProtocolApplicationDataLocation padl : config.protocolAppLocations) {
			ProtocolApplication pa = readProtocolApplication(padl);
			protApps.add(pa);
		}
		return protApps;
	}

	/**
	 * 
	 * @param padl
	 * @return
	 * @throws Exception
	 */
	protected ProtocolApplication readProtocolApplication(ProtocolApplicationDataLocation padl) throws Exception {
		logger.log(Level.FINE, "Reading protocol application ...");
		Protocol protocol = readProtocol(padl);
		ProtocolApplication pa = new ProtocolApplication(protocol);

		if (padl.protocolApplicationUUID != null) {
			String s = getString(padl.protocolApplicationUUID);
			if (s != null && !"".equals(s.trim())) {
				String docUUID = ExcelParserConfigurator.generateUUID(config.getPrefix(), s);
				pa.setDocumentUUID(docUUID);
			}
		}

		if (padl.investigationUUID != null) {
			String s = getString(padl.investigationUUID);
			if (s != null && !"".equals(s.trim())) {
				String investUUID = ExcelParserConfigurator.generateUUID(config.getPrefix(), s);
				pa.setInvestigationUUID(investUUID);
			}
		}

		if (padl.assayUUID != null) {
			String s = getString(padl.assayUUID);
			if (s != null && !"".equals(s.trim())) {
				pa.setAssayUUID(s); // this function automatically generates
									// valid UUID from string
			}
		}

		if (padl.citationTitle != null) {
			String s = getString(padl.citationTitle);
			if (s != null)
				pa.setReference(s); // title is the reference 'itself'
		}

		if (padl.citationOwner != null) {
			String s = getString(padl.citationOwner);
			if (s != null)
				pa.setReferenceOwner(s);
		}

		if (padl.citationYear != null) {
			// Enforcing parameter to be read as date
			String year = null;
			if (padl.citationYear.dataInterpretation == DataInterpretation.AS_DATE) {
				Date date = getDate(padl.citationYear);
				//see Javadoc for 1900
				if (date!=null)
					year = Integer.toString(date.getYear()+1900);
				else //date parsing failed for whatever reason
					year = getString(padl.citationYear);
			} else 
				year = getString(padl.citationYear);
			if (year != null)
				pa.setReferenceYear(year);
		}

		if (padl.interpretationCriteria != null) {
			String s = getString(padl.interpretationCriteria);
			if (s != null)
				pa.setInterpretationCriteria(s);
		}

		if (padl.interpretationResult != null) {
			String s = getString(padl.interpretationResult);
			if (s != null)
				pa.setInterpretationResult(s);
		}

		// Handle reliability info
		ReliabilityParams reliability = null;

		if (padl.reliability_value != null) {
			String s = getString(padl.reliability_value);
			if (s != null) {
				if (reliability == null)
					reliability = new ReliabilityParams();

				reliability.setValue(s);
			}
		}

		if (padl.reliability_isRobustStudy != null) {
			String s = getString(padl.reliability_isRobustStudy);
			if (s != null) {
				if (reliability == null)
					reliability = new ReliabilityParams();

				reliability.setIsRobustStudy(s);
			}
		}

		if (padl.reliability_isUsedforClassification != null) {
			String s = getString(padl.reliability_isUsedforClassification);
			if (s != null) {
				if (reliability == null)
					reliability = new ReliabilityParams();

				reliability.setIsUsedforClassification(s);
			}
		}

		if (padl.reliability_isUsedforMSDS != null) {
			String s = getString(padl.reliability_isUsedforMSDS);
			if (s != null) {
				if (reliability == null)
					reliability = new ReliabilityParams();

				reliability.setIsUsedforMSDS(s);
			}
		}

		if (padl.reliability_purposeFlag != null) {
			String s = getString(padl.reliability_purposeFlag);
			if (s != null) {
				if (reliability == null)
					reliability = new ReliabilityParams();

				reliability.setPurposeFlag(s);
			}
		}

		if (padl.reliability_studyResultType != null) {
			String s = getString(padl.reliability_studyResultType);
			if (s != null) {
				if (reliability == null)
					reliability = new ReliabilityParams();

				reliability.setStudyResultType(s);
			}
		}

		if (reliability != null)
			pa.setReliability(reliability);

		// Read parameters
		if (padl.parameters != null) {
			IParams params = new Params();
			for (String param : padl.parameters.keySet()) {
				readParameter(param, padl.parameters.get(param), params);
			}
			pa.setParameters(params);
		}

		// Read effects array
		if (padl.effects != null) {
			for (int i = 0; i < padl.effects.size(); i++)
				try {
					EffectRecordDataLocation erdl = padl.effects.get(i);
					EffectRecord effect = readEffect(erdl);
					if (config.clearEmptyEffectRecords) {
						if (effect.isEmpty())
							continue;
					}
					pa.addEffect(effect);

					// Register effect reference
					if (erdl.reference != null)
						referencesEffectRecord.put(erdl.reference, effect);
					// Register condition additions by reference
					if (erdl.addConditionsByRef != null)
						for (int k = 0; k < erdl.addConditionsByRef.length; k++) {
							addConditionRef.add(erdl.addConditionsByRef[k]);
							addConditionTargetEffectRecord.add(effect);
						}
				} catch (Exception x) {
					logger.log(Level.SEVERE, x.getMessage());
					throw x;
				}
		}

		// Read effects from EFFECTS_BLOCK
		if (padl.effectsBlock != null) {
			for (int i = 0; i < padl.effectsBlock.size(); i++) {
				ExcelDataBlockLocation excelEffectBlock = padl.effectsBlock.get(i);
				try {
					List<DataBlockElement> effDataBlock = getDataBlock(excelEffectBlock);
					for (DataBlockElement dbe : effDataBlock) {
						EffectRecord effect = dbe.generateEffectRecord();
						pa.addEffect(effect);
					}
				} catch (Exception x) {
					throw new Exception("Excpetion on getting Effect Block [" + (i + 1) + "]" + " in protocol "
							+ protocol.toString() + "  " + x.toString() + "\nCheck EFFECT_BLOCK expressions!");
				}
			}
		}

		return pa;
	}

	void clearReferencesInfo() {
		referencesEffectRecord.clear();
		addConditionRef.clear();
		addConditionTargetEffectRecord.clear();
	}

	void setupReferenceInfo() {
		// Setup conditions
		for (int i = 0; i < addConditionRef.size(); i++) {
			String ref = addConditionRef.get(i);
			EffectRecord sourceEff = referencesEffectRecord.get(ref);
			addConditions(sourceEff, addConditionTargetEffectRecord.get(i));
		}
	}

	void addConditions(EffectRecord sourceEff, EffectRecord targetEff) {
		if ((sourceEff == null) || (targetEff == null))
			return;
		if (sourceEff.getConditions() == null)
			return;

		IParams sourceCond = (IParams) sourceEff.getConditions();
		if (targetEff.getConditions() == null)
			targetEff.setConditions(sourceCond);
		else {
			IParams targetCond = (IParams) targetEff.getConditions();
			Set<String> sourceKeys = sourceCond.keySet();
			for (String key : sourceKeys) {
				targetCond.put(key, sourceCond.get(key));
			}
		}
	}

	void readParameter(String param, ExcelDataLocation loc, IParams destinationParams) throws Exception {
		String parameterName = param;

		// Handle parameter name from other excel data location
		if (loc.otherLocationFields != null) {
			ExcelDataLocation pNameLoc = loc.otherLocationFields.get("NAME");
			if (pNameLoc != null) {
				try {
					String nameString = getStringValue(pNameLoc);
					if (nameString != null)
						parameterName = nameString;
				} catch (Exception x) {
					logger.log(Level.FINE, String.format("%s\t%s\t%s", param, x.getMessage(), pNameLoc.toString()));
				}
			}
		}

		// Enforcing parameter to be read as string
		if (loc.dataInterpretation == DataInterpretation.AS_TEXT) {
			String s = getString(loc);
			if (s != null) {
				String unitString = null;
				if (loc.otherLocationFields != null) {
					ExcelDataLocation pUnitLoc = loc.otherLocationFields.get("UNIT");
					if (pUnitLoc != null) {
						try {
							unitString = getStringValue(pUnitLoc);
						} catch (Exception x) {
							logger.log(Level.FINE,
									String.format("%s\t%s\t%s", param, x.getMessage(), pUnitLoc.toString()));
						}
					}
				}

				if (unitString != null)
					destinationParams.put(parameterName, s + " " + unitString);
				else
					destinationParams.put(parameterName, s);
			}
			return;
		}

		// Enforcing parameter to be read as date
		if (loc.dataInterpretation == DataInterpretation.AS_DATE) {
			Date d = getDate(loc);
			if (d != null)
				destinationParams.put(parameterName, d);
			return;
		}

		Value pVal = null;
		String paramStringValue = null;

		// Parameter is allowed to be Rich Value, String or Numeric object
		try {
			paramStringValue = getStringValue(loc);
		} catch (Exception x) {
			logger.log(Level.FINE, String.format("%s\t%s\t%s", param, x.getMessage(), loc.toString()));
		}
		if (paramStringValue != null) {
			RichValue rv = rvParser.parse(paramStringValue);
			String rv_error = rvParser.getAllErrorsAsString();

			if (rv_error == null) {
				pVal = new Value();
				if (rv.unit != null)
					pVal.setUnits(rv.unit);
				if (rv.loValue != null)
					pVal.setLoValue(rv.loValue);
				if (rv.loQualifier != null)
					pVal.setLoQualifier(rv.loQualifier);
				if (rv.upValue != null)
					pVal.setUpValue(rv.upValue);
				if (rv.upQualifier != null)
					pVal.setUpQualifier(rv.upQualifier);
			}
		} else
			try {
				Number paramDoubleValue = getNumericValue(loc);
				if (paramDoubleValue != null) {
					pVal = new Value();
					pVal.setLoValue(paramDoubleValue);
				}
			} catch (Exception x) {
				logger.log(Level.FINE, String.format("%s\t%s\t%s", param, x.getMessage(), loc.toString()));
			}

		if (pVal != null) // Parameters is stored as a Value object
		{
			// Handle parameter unit from other excel data location

			// System.out.println(parameterName + "\t" + pVal + "\t" + loc +
			// "\t" + loc.otherLocationFields);
			if (loc.otherLocationFields != null) {
				ExcelDataLocation pUnitLoc = loc.otherLocationFields.get("UNIT");
				if (pUnitLoc != null) {
					try {
						String unitString = getStringValue(pUnitLoc);
						if (unitString != null)
							pVal.setUnits(unitString);
					} catch (Exception x) {
						logger.log(Level.FINE, String.format("%s\t%s\t%s", param, x.getMessage(), pUnitLoc.toString()));
					}
				}
			}
			destinationParams.put(parameterName, pVal);
		} else {
			// Parameter is stored as a string
			if (paramStringValue != null)
				destinationParams.put(parameterName, paramStringValue);
		}
	}

	/**
	 * 
	 * @param padl
	 * @return
	 * @throws Exception
	 */
	protected Protocol readProtocol(ProtocolApplicationDataLocation padl) throws Exception {
		String endpoint = null;
		if (padl.protocolEndpoint != null)
			endpoint = getString(padl.protocolEndpoint);

		Protocol protocol = new Protocol(endpoint);

		if (padl.protocolTopCategory != null) {
			String s = getString(padl.protocolTopCategory);
			if (s != null)
				protocol.setTopCategory(s);
		}

		if (padl.protocolCategoryCode != null) {
			String s = getString(padl.protocolCategoryCode);
			if (s != null)
				protocol.setCategory(s);
		}

		/*
		 * Category title is currently not handled: padl.protocolCategoryTitle
		 */

		if (padl.protocolGuideline != null) {
			List<String> guide = new ArrayList<String>();
			for (int i = 0; i < padl.protocolGuideline.size(); i++) {
				String s = getString(padl.protocolGuideline.get(i));
				if (s != null)
					guide.add(s);
			}
			protocol.setGuideline(guide);
		}

		return protocol;
	}

	/**
	 * 
	 * @param efrdl
	 * @return
	 * @throws Exception
	 */
	protected EffectRecord readEffect(EffectRecordDataLocation efrdl) throws Exception {
		logger.log(Level.FINE, "Reading effect record");
		EffectRecord effect = new EffectRecord();

		if (efrdl.sampleID != null) {
			String s = getString(efrdl.sampleID);
			if (s != null)
				effect.setSampleID(s);
		}

		if (efrdl.endpoint != null) {
			String s = getString(efrdl.endpoint);
			if (s != null)
				effect.setEndpoint(s.trim().toUpperCase());
		}

		if (efrdl.endpointType != null) {
			String s = getString(efrdl.endpointType);
			if (s != null)
				effect.setEndpointType(s.trim().toUpperCase());
		}

		if (efrdl.loQualifier != null) {
			String s = getString(efrdl.loQualifier);
			if (s != null) {
				if (ExcelParserConfigurator.isValidQualifier(s))
					effect.setLoQualifier(s);
				else {
					// parseErrors.add("[" +
					// locationStringForErrorMessage(efrdl.loQualifier,
					// primarySheetNum) + "] Lo Qualifier \"" + s +
					// "\" is incorrect!");

					logger.log(Level.WARNING, String.format("[%s ] Lo Qualifier \"%s\"",
							locationStringForErrorMessage(efrdl.loQualifier, primarySheetNum), s));
				}
			}
		}

		if (efrdl.loValue != null) {
			Number d = null;
			if (config.Fl_AllowQualifierInValueCell) {
				try {
					String qstring = getStringValue(efrdl.loValue);
					if (qstring != null) {
						RecognitionUtils.QualifierValue qv = RecognitionUtils.extractQualifierValue(qstring);
						if (qv.value == null)
							logger.log(Level.WARNING,
									String.format("[%s] %s Lo Value/Qualifier error: %s",
											locationStringForErrorMessage(efrdl.loQualifier, primarySheetNum), qstring,
											qv.errorMsg));
						// parseErrors.add("[" +
						// locationStringForErrorMessage(efrdl.loQualifier,
						// primarySheetNum) + "] "+ qstring +
						// " Lo Value/Qualifier error: " + qv.errorMsg);
						else {
							d = qv.value;
							if (qv.qualifier != null)
								effect.setLoQualifier(qv.qualifier); // this
																		// qualifier
																		// takes
																		// precedence
																		// (if
																		// already
																		// set)
						}
					}
				} catch (Exception x) {
					// if it is not string value, try numeric
					d = getNumericValue(efrdl.loValue);
				}

			} else
				d = getNumericValue(efrdl.loValue);

			if (d != null)
				effect.setLoValue(d.doubleValue());
		}

		if (efrdl.upQualifier != null) {
			String s = getString(efrdl.upQualifier);
			if (s != null) {
				if (ExcelParserConfigurator.isValidQualifier(s))
					effect.setUpQualifier(s);
				else
					logger.log(Level.WARNING, String.format("[%s] Up Qualifier \"%s\" is incorrect!",
							locationStringForErrorMessage(efrdl.upQualifier, primarySheetNum), s));
				// parseErrors.add("[" +
				// locationStringForErrorMessage(efrdl.upQualifier,
				// primarySheetNum) + "] Up Qualifier \"" + s +
				// "\" is incorrect!");
			}
		}

		if (efrdl.upValue != null) {
			Number d = null;
			if (config.Fl_AllowQualifierInValueCell) {
				String qstring = null;
				try {
					qstring = getStringValue(efrdl.upValue);
				} catch (Exception x) {
					logger.log(Level.FINE, x.getMessage());
				}

				if (qstring != null) {
					RecognitionUtils.QualifierValue qv = RecognitionUtils.extractQualifierValue(qstring);
					if (qv.value == null)
						logger.log(Level.WARNING,
								String.format("[%s] %s Up Value/Qualifier error: %s",
										locationStringForErrorMessage(efrdl.upQualifier, primarySheetNum), qstring,
										qv.errorMsg));
					// parseErrors.add("[" +
					// locationStringForErrorMessage(efrdl.upQualifier,
					// primarySheetNum) + "] " + qstring +
					// " Up Value/Qualifier error: " + qv.errorMsg);
					else {
						d = qv.value;
						if (qv.qualifier != null)
							effect.setUpQualifier(qv.qualifier); // this
																	// qualifier
																	// takes
																	// precedence
																	// (if
																	// already
																	// set)
					}
				} else
					d = getNumericValue(efrdl.upValue);
			} else
				d = getNumericValue(efrdl.upValue);

			if (d != null)
				effect.setUpValue(d.doubleValue());
		}

		if (efrdl.textValue != null)
			try {

				String s = getString(efrdl.textValue);
				if (s != null)
					effect.setTextValue(s);
			} catch (Exception x) {
				logger.log(Level.WARNING, x.getMessage());
			}

		if (efrdl.errQualifier != null) {
			String s = getString(efrdl.errQualifier);
			if (s != null) {
				if (ExcelParserConfigurator.isValidQualifier(s))
					effect.setErrQualifier(s);
				else
					logger.log(Level.WARNING, String.format("[%s] Err Qualifier '%s' is incorrect!",
							locationStringForErrorMessage(efrdl.errQualifier, primarySheetNum), s));
				// parseErrors.add("[" +
				// locationStringForErrorMessage(efrdl.errQualifier,
				// primarySheetNum) + "] Err Qualifier \"" + s +
				// "\" is incorrect!");
			}
		}

		if (efrdl.errValue != null) {
			Number d = null;
			if (config.Fl_AllowQualifierInValueCell) {
				// errors
				String qstring = null;
				try {
					qstring = getStringValue(efrdl.errValue);
				} catch (Exception x) {
					logger.log(Level.FINE, x.getMessage());
				}
				if (qstring != null) {
					RecognitionUtils.QualifierValue qv = RecognitionUtils.extractQualifierValue(qstring);
					if (qv.value == null)
						logger.log(Level.WARNING,
								String.format("[%s] %s Err Value/Qualifier error: %s",
										locationStringForErrorMessage(efrdl.errQualifier, primarySheetNum), qstring,
										qv.errorMsg));
					// parseErrors.add("[" +
					// locationStringForErrorMessage(efrdl.errQualifier,
					// primarySheetNum) + "] " + qstring +
					// " Err Value/Qualifier error: " + qv.errorMsg);
					else {
						d = qv.value;
						if (qv.qualifier != null)
							effect.setErrQualifier(qv.qualifier); // this
																	// qualifier
																	// takes
																	// precedence
																	// (if
																	// already
																	// set)
					}
				} else
					d = getNumericValue(efrdl.errValue);
			} else
				d = getNumericValue(efrdl.errValue);

			if (d != null)
				effect.setErrorValue(d.doubleValue());
		}

		if (efrdl.unit != null) {
			String s = getString(efrdl.unit);
			if (s != null)
				effect.setUnit(s);
		}

		if (efrdl.value != null) {
			// If present this takes precedence over the up/lo/err values and
			// qualifiers and unit
			// These are intelligently recognized

			Number d = null;
			// errors
			String richValueString = null;
			try {
				richValueString = getStringValue(efrdl.value);
			} catch (Exception x) {
				logger.log(Level.FINE, x.getMessage());
			}

			if (richValueString != null) {
				// EffectRecord can handle error that is why
				// representPlusMinusAsInterval = false
				RichValue rv = rvParser.parse(richValueString, false);
				String rv_error = rvParser.getAllErrorsAsString();

				if (rv_error == null) {
					if (rv.unit != null)
						effect.setUnit(rv.unit);
					if (rv.loValue != null)
						effect.setLoValue(rv.loValue);
					if (rv.loQualifier != null)
						effect.setLoQualifier(rv.loQualifier);
					if (rv.upValue != null)
						effect.setUpValue(rv.upValue);
					if (rv.upQualifier != null)
						effect.setUpQualifier(rv.upQualifier);
					if (rv.errorValue != null)
						effect.setErrorValue(rv.errorValue);
					if (rv.errorValueQualifier != null)
						effect.setErrQualifier(rv.errorValueQualifier);
				} else {
					// The string is not recognized as a valid rich value
					if (efrdl.value.dataInterpretation == DataInterpretation.AS_VALUE_OR_TEXT)
						effect.setTextValue(richValueString);
					else
						logger.log(Level.WARNING,
								String.format("[%s] %s Value error: %s",
										locationStringForErrorMessage(efrdl.value, primarySheetNum), richValueString,
										rv_error));
					// parseErrors.add("[" +
					// locationStringForErrorMessage(efrdl.value,
					// primarySheetNum) + "] " + richValueString +
					// " Value error: " + rv_error);
				}
			} else
				try {
					d = getNumericValue(efrdl.value);
				} catch (CellException x) {
					logger.log(Level.WARNING, x.getMessage());
					effect.setTextValue(getString(efrdl.value));
				}

			if (d != null)
				effect.setLoValue(d.doubleValue()); // This is the default
													// behavior if the
			// cell is of type numeric
		}

		if (efrdl.conditions != null) {
			IParams conditions = readConditions(efrdl.conditions);
			effect.setConditions(conditions);
		}

		return effect;
	}

	IParams readConditions(HashMap<String, ExcelDataLocation> conditionsInfo) throws Exception {
		IParams conditions = new Params();

		Set<Entry<String, ExcelDataLocation>> locEntries = conditionsInfo.entrySet();
		for (Entry<String, ExcelDataLocation> entry : locEntries) {
			// Conditions are read in the same way as parameters are read
			readParameter(entry.getKey(), entry.getValue(), conditions);

			// This is the old simple approach: condition is always read as text
			// String value = getString(entry.getValue());
			// conditions.put(entry.getKey(), value);
		}

		return conditions;
	}

	/**
	 * 
	 * @param cdl
	 * @param record
	 * @return
	 * @throws Exception
	 */
	protected CompositionRelation readCompositionRelation(CompositionDataLocation cdl, SubstanceRecord record)
			throws Exception {
		IStructureRecord structure = new StructureRecord();
		Proportion proportion = new Proportion();

		if (cdl.content != null) {
			String s = getString(cdl.content);
			structure.setContent(s);
		}

		if (cdl.format != null) {
			String s = getString(cdl.format);
			structure.setFormat(s);
		}

		if (cdl.formula != null) {
			String s = getString(cdl.formula);
			structure.setFormula(s);
		}

		if (cdl.smiles != null) {
			String s = getString(cdl.smiles);
			structure.setSmiles(s);
		}

		if (cdl.inchi != null) {
			String s = getString(cdl.inchi);
			structure.setInchi(s);
		}

		if (cdl.inchiKey != null) {
			String s = getString(cdl.inchiKey);
			structure.setInchiKey(s);
		}

		if (cdl.properties != null) {
			for (String propName : cdl.properties.keySet()) {
				ExcelDataLocation loc = cdl.properties.get(propName);
				Object propObj = null;

				if (loc.dataInterpretation == DataInterpretation.AS_TEXT)
					try {
						propObj = getString(loc);
					} catch (Exception x) {
						logger.log(Level.FINE, x.getMessage());
					}

				try {

					propObj = getStringValue(loc);
					if (propObj != null)
						propObj = propObj.toString().trim();

				} catch (Exception x) {
					logger.log(Level.FINE, x.getMessage());
				}

				if (propObj == null)
					try {
						propObj = getNumericValue(loc);
					} catch (Exception x) {
						// we might just have empty cell
						logger.log(Level.WARNING, String.format("%s %s", x.getMessage(), loc.toString()));
					}

				if (propObj != null) {
					String sameas = Property.guessLabel(propName);

					if (Property.opentox_IUCLID5_UUID.equals(sameas)) {
						Property property = Property.getI5UUIDInstance();
						propObj = ExcelParserConfigurator.generateUUID(config.getPrefix(), propObj.toString());
						structure.setRecordProperty(property, propObj);
					} else {
						Property property = new Property(propName, "", "");
						property.setLabel(sameas);
						structure.setRecordProperty(property, propObj);
					}
				}
			}
		}

		if (cdl.proportion != null) {
			if (cdl.proportion.function != null) // TODO eventually to check a
			// list of predefined functions
			{
				String s = getString(cdl.proportion.function);
				proportion.setFunction(s);
			}

			if (cdl.proportion.typical_precision != null) {
				String s = getString(cdl.proportion.typical_precision);
				proportion.setTypical(s);
			}

			if (cdl.proportion.typical_value != null) {
				Number d = null;
				try {
					d = getNumericValue(cdl.proportion.typical_value);
				} catch (Exception x) {

				}
				if (d != null)
					proportion.setTypical_value(d.doubleValue());
			}

			if (cdl.proportion.typical_unit != null) {
				String s = getString(cdl.proportion.typical_unit);
				proportion.setTypical_unit(s);
			}

			/*
			 * if (cdl.proportion.real_value != null) { Double d =
			 * getNumericValue(cdl.proportion.real_value);
			 * proportion.setReal_value(d); }
			 */

			if (cdl.proportion.real_lower_precision != null) {
				String s = getString(cdl.proportion.real_lower_precision);
				proportion.setReal_lower(s);
			}

			if (cdl.proportion.real_lower_value != null)
				try {
					Number d = getNumericValue(cdl.proportion.real_lower_value);
					proportion.setReal_lowervalue(d.doubleValue());
				} catch (Exception x) {
					// x.printStackTrace();
				}

			if (cdl.proportion.real_upper_precision != null) {
				String s = getString(cdl.proportion.real_upper_precision);
				proportion.setReal_upper(s);
			}

			if (cdl.proportion.real_upper_value != null)
				try {
					Number d = getNumericValue(cdl.proportion.real_upper_value);
					if (d != null)
						proportion.setReal_uppervalue(d.doubleValue());
				} catch (Exception x) {
					// x.printStackTrace();
				}

			if (cdl.proportion.real_unit != null) {
				String s = getString(cdl.proportion.real_unit);
				proportion.setReal_unit(s);
			}

		} // end of proportion

		CompositionRelation relation = new CompositionRelation(record, structure, cdl.structureRelation, proportion);
		return relation;
	}

	
	

	/**
	 * 
	 * @param exdb_loc
	 * @return
	 */
	protected List<DataBlockElement> getDataBlock(ExcelDataBlockLocation exdb_loc) {
		switch (exdb_loc.location.iteration) {
		case ROW_SINGLE:
			List<Row> rList = new ArrayList<Row>();
			List<DataBlockElement> listDBEl0 = getDataBlockFromRowList(rList, exdb_loc);
			return listDBEl0;

		case ROW_MULTI_FIXED: // Both treated the same way
		case ROW_MULTI_DYNAMIC:
			List<DataBlockElement> listDBEl = getDataBlockFromRowList(curRows, exdb_loc);
			return listDBEl;

		case ABSOLUTE_LOCATION: {
			Object value = exdb_loc.getAbsoluteLocationValue();
			if (value == null) {
				value = getDataBlockFromAbsolutePosition(exdb_loc);
				exdb_loc.setAbsoluteLocationValue(value);
			}
			if (value != null)
				return (List<DataBlockElement>) value;
			return null;
		}

		default:
			return null;

		}
	}

	protected List<DataBlockElement> getDataBlockFromRowList(List<Row> rowList, ExcelDataBlockLocation exdb_loc) {
		Integer rowSubblocks = dataBlockUtils.getIntegerFromExpression(exdb_loc.rowSubblocks);
		Integer columnSubblocks = dataBlockUtils.getIntegerFromExpression(exdb_loc.columnSubblocks);
		Integer sbSizeRows = dataBlockUtils.getIntegerFromExpression(exdb_loc.subblockSizeRows);
		Integer sbSizeColumns = dataBlockUtils.getIntegerFromExpression(exdb_loc.subblockSizeColumns);
		
		logger.info("------ getDataBlockFromRowList:");
		logger.info("   --- rowSubblocks = " + rowSubblocks);
		logger.info("   --- columnSubblocks = " + columnSubblocks);
		logger.info("   --- subblockSizeRows = " + sbSizeRows);
		logger.info("   --- subblockSizeColumns = " + sbSizeColumns);

		if (rowSubblocks == null || columnSubblocks == null || sbSizeRows == null || sbSizeColumns == null) {
			return null;
		}

		if (exdb_loc.location == null)
			return null;

		// Constructing the cell matrix
		int n = rowSubblocks * sbSizeRows;
		int m = columnSubblocks * sbSizeColumns;
		// startRow is assumed to be the first row from rowList
		// exdb_loc.location.rowIndex is not used;
		int startColumn = exdb_loc.location.columnIndex;

		Cell cells[][] = new Cell[n][m];
		// exdb_loc.location.sheetIndex is not used

		for (int i = 0; i < n; i++) {
			if (i >= rowList.size()) {
				for (int k = 0; k < m; k++)
					cells[i][k] = null;
				continue;
			}

			Row row = rowList.get(i);
			// String s = "";
			for (int k = 0; k < m; k++)
				try {
					Cell c = row.getCell(startColumn + k);
					cells[i][k] = c;
					// s += (" " + ExcelUtils.getObjectFromCell(c));
				} catch (Exception x) {
					cells[i][k] = null;
					logger.warning(x.getMessage());
				}
			// logger.info(">>>> " + s);
		}

		return dataBlockUtils.getDataBlockFromCellMatrix(cells, rowSubblocks, columnSubblocks, sbSizeRows, sbSizeColumns, exdb_loc);
	}

	protected List<DataBlockElement> getDataBlockFromAbsolutePosition(ExcelDataBlockLocation exdb_loc) {
		Integer rowSubblocks = dataBlockUtils.getIntegerFromExpression(exdb_loc.rowSubblocks);
		Integer columnSubblocks = dataBlockUtils.getIntegerFromExpression(exdb_loc.columnSubblocks);
		Integer sbSizeRows = dataBlockUtils.getIntegerFromExpression(exdb_loc.subblockSizeRows);
		Integer sbSizeColumns = dataBlockUtils.getIntegerFromExpression(exdb_loc.subblockSizeColumns);

		logger.info("------ getDataBlockFromAbsolutePosition:");
		logger.info("   --- rowSubblocks = " + rowSubblocks);
		logger.info("   --- columnSubblocks = " + columnSubblocks);
		logger.info("   --- subblockSizeRows = " + sbSizeRows);
		logger.info("   --- subblockSizeColumns = " + sbSizeColumns);

		if (rowSubblocks == null || columnSubblocks == null || sbSizeRows == null || sbSizeColumns == null) {
			return null;
		}

		if (exdb_loc.location == null)
			return null;

		// Constructing the cell matrix
		int n = rowSubblocks * sbSizeRows;
		int m = columnSubblocks * sbSizeColumns;
		int startRow = exdb_loc.location.rowIndex;
		int startColumn = exdb_loc.location.columnIndex;

		Cell cells[][] = new Cell[n][m];
		
		int sheetIndex = exdb_loc.location.sheetIndex;
		//try {
		//	sheetIndex = exdb_loc.location.getSheetIndex(workbook, primarySheetNum);
		//} catch (Exception x) {}
		Sheet sheet = workbook.getSheetAt(sheetIndex);

		for (int i = 0; i < n; i++) {
			Row row = sheet.getRow(startRow + i);
			// String s = "";
			for (int k = 0; k < m; k++)
				try {
					Cell c = row.getCell(startColumn + k);
					cells[i][k] = c;
					// s += (" " + ExcelUtils.getObjectFromCell(c));
				} catch (Exception x) {
					cells[i][k] = null;
					logger.warning(x.getMessage());
				}
			// logger.info(">>>> " + s);
		}

		return dataBlockUtils.getDataBlockFromCellMatrix(cells, rowSubblocks, columnSubblocks, sbSizeRows, sbSizeColumns, exdb_loc);
	}

		
	
	/*
	 * public boolean hasErrors() { return (!parseErrors.isEmpty()); }
	 * 
	 * public String errorsToString() { StringBuffer sb = new StringBuffer();
	 * for (int i = 0; i < parseErrors.size(); i++) sb.append("Error #" + (i +
	 * 1) + "\n" + parseErrors.get(i) + "\n"); return sb.toString(); }
	 */
	@Override
	public void handleError(String arg0) throws CDKException {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleError(String arg0, Exception arg1) throws CDKException {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleError(String arg0, int arg1, int arg2, int arg3) throws CDKException {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleError(String arg0, int arg1, int arg2, int arg3, Exception arg4) throws CDKException {
		// TODO Auto-generated method stub
	}

	@Override
	public void setErrorHandler(IChemObjectReaderErrorHandler arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setReaderMode(Mode arg0) {
	}

	@Override
	public boolean accepts(Class<? extends IChemObject> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addChemObjectIOListener(IChemObjectIOListener arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public IResourceFormat getFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IOSetting[] getIOSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeChemObjectIOListener(IChemObjectIOListener arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public <S extends IOSetting> S addSetting(IOSetting arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSettings(Collection<IOSetting> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<IChemObjectIOListener> getListeners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends IOSetting> S getSetting(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends IOSetting> S getSetting(String arg0, Class<S> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IOSetting> getSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSetting(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 
	 * - dynamic (automatic) recognition of SubtsanceRecord elements from Excel:
	 * protocol applications, effects, endpoints, conditions keyword suggestion
	 * DYNAMIC_SPAN
	 * 
	 * - Check the consistency of the ExcelDataLocation (loc variables) and the
	 * global data access/parallel sheet access, ...
	 * 
	 * - Error messages to be logged out /option to switch off memory storage of
	 * the error messages
	 * 
	 */

}
