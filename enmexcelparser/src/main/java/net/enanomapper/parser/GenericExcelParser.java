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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

/**
 * 
 * @author nick
 * 
 */
public class GenericExcelParser implements IRawReader<IStructureRecord> {

	public final static Logger logger = Logger
			.getLogger(GenericExcelParser.class.getName());

	protected RichValueParser rvParser = new RichValueParser();
	// protected ArrayList<String> parseErrors = new ArrayList<String>();
	protected ArrayList<String> parallelSheetsErrors = new ArrayList<String>();

	protected ExcelParserConfigurator config = null;
	protected InputStream input;
	protected JexlEngine jexlEngine = null;

	protected Workbook workbook;
	protected boolean xlsxFormat = false;

	// Helper variables for excel file iteration
	protected ParallelSheetState parallelSheetStates[] = null;

	// Primary sheet
	protected int primarySheetNum = 0;
	protected Sheet primarySheet = null;
	protected int curRowNum = 1; // This is used by the iteration logic
	protected int curReadRowNum = 1; // This shows the actual read rows in
	// multi-dynamic iteration mode
	protected int curCellNum = 1;
	protected int iterationLastRowNum = 1;
	protected Row curRow = null;
	protected ArrayList<Row> curRows = null;
	protected Iterator<Row> rowIt = null;
	protected Cell curCell = null;
	protected String primarySheetSynchKey = null;

	// All variables read from the primary sheet and all parallel sheets
	protected HashMap<String, Object> curVariables = new HashMap<String, Object>();
	protected HashMap<String, HashMap<Object, Object>> curVariableMappings = new HashMap<String, HashMap<Object, Object>>();

	protected boolean FlagNextRecordLoaded = false; // This flag is true when
	// next object is iterated
	// and successfully read to
	// the buffer;
	protected SubstanceRecord basicSubstanceRecord = null;
	protected SubstanceRecord nextRecordBuffer = null;
	protected int nextRecordIndex = -1;

	protected ArrayList<SubstanceRecord> loadedRecordsBuffer = new ArrayList<SubstanceRecord>();

	// protected boolean FlagAddParserStringError = true; // This is used to
	// switch

	// off errors in some
	// cases
	/**
	 * 
	 * @param input
	 * @param jsonConfig
	 * @throws Exception
	 */
	public GenericExcelParser(InputStream input, File jsonConfig)
			throws Exception {
		this(input, jsonConfig, true);
	}

	/**
	 * 
	 * @param input
	 * @param jsonConfig
	 * @param xlsxFormat
	 * @throws Exception
	 */
	public GenericExcelParser(InputStream input, File jsonConfig,
			boolean xlsxFormat) throws Exception {
		super();
		this.xlsxFormat = xlsxFormat;
		this.input = input;

		config = ExcelParserConfigurator.loadFromJSON(jsonConfig);
		if (config.configErrors.size() > 0)
			throw new Exception("GenericExcelParser configuration errors:\n"
					+ config.getAllErrorsAsString());

		setReader(input);
		init();
	}

	public ExcelParserConfigurator getExcelParserConfigurator() {
		return config;
	}

	protected void init() throws Exception {
		handleConfigRecognitions();

		// Setting of the basic sheet work variables
		initBasicWorkSheet();

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

	protected void initBasicWorkSheet() {
		primarySheetNum = config.sheetIndex;
		primarySheet = workbook.getSheetAt(primarySheetNum);
		curRowNum = config.startRow;

		iterationLastRowNum = primarySheet.getLastRowNum();
		if (config.FlagEndRow)
			if (config.endRow < iterationLastRowNum)
				iterationLastRowNum = config.endRow;

		logger.info("primarySheet# = " + (primarySheetNum + 1)
				+ "   starRow# = " + (curRowNum + 1) + "\n" + "Last row# = "
				+ (primarySheet.getLastRowNum() + 1));
	}

	protected void initParallelSheets() throws Exception {
		parallelSheetStates = new ParallelSheetState[config.parallelSheets
				.size()];
		for (int i = 0; i < config.parallelSheets.size(); i++) {
			ExcelSheetConfiguration eshc = config.parallelSheets.get(i);
			parallelSheetStates[i] = new ParallelSheetState();
			// if (eshc.FlagSheetIndex) //this check should not be needed
			// because sheetNum must set via sheetName as well
			parallelSheetStates[i].sheetNum = eshc.sheetIndex;
			parallelSheetStates[i].synchronization = eshc.synchronization;
			parallelSheetStates[i].dynamicIteration = eshc.dynamicIteration;
			parallelSheetStates[i].dynamicIterationColumnIndex = eshc.dynamicIterationColumnIndex;

			if (0 <= parallelSheetStates[i].sheetNum
					&& parallelSheetStates[i].sheetNum < workbook
							.getNumberOfSheets())
				parallelSheetStates[i].sheet = workbook
						.getSheetAt(parallelSheetStates[i].sheetNum);
			else {
				throw new Exception("Incorrect SHEET_INDEX "
						+ (parallelSheetStates[i].sheetNum + 1)
						+ " in parallel sheet #" + (i + 1));
			}

			parallelSheetStates[i].curRowNum = eshc.startRow;
		}
	}

	protected void handleConfigRecognitions() {
		Set<Entry<String, ExcelDataLocation>> locEntries = config.substanceLocations
				.entrySet();
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
			cdl.setParallelSheets(parallelSheetStates, primarySheetNum,
					parallelSheetsErrors);

		for (ExternalIdentifierDataLocation eidl : config.externalIdentifiers)
			eidl.setParallelSheets(parallelSheetStates, primarySheetNum,
					parallelSheetsErrors);
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
				parallelSheetsErrors.add("["
						+ locationStringForErrorMessage(loc)
						+ "] Sheet number number not valid parallel sheet!");
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

					loadSubstanceRecords();
					logger.info("#### Loaded " + loadedRecordsBuffer.size()
							+ " substances into the buffer");
					if (loadedRecordsBuffer.isEmpty())
						FlagNextRecordLoaded = false;
					else {
						nextRecordIndex = 0;
						nextRecordBuffer = loadedRecordsBuffer
								.get(nextRecordIndex);
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
			if (curRowNum <= iterationLastRowNum /* primarySheet.getLastRowNum() */)
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
					// TODO
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
						// TODO
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
			logger.info("---- Variable mapping error in mapping: \""
					+ varMapping.name + "\",  KEYS_VARIABLE \""
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
			logger.info("---- Variable mapping error in mapping: \""
					+ varMapping.name + "\",  VALUES_VARIABLE \""
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

		logger.info("---- Variable mapping: " + varMapping.name);
		// Make mapping
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		for (int i = 0; i < keys.length; i++) {
			if (i >= values.length) // Reached the end of values array
				break;
			map.put(keys[i], values[i]);
			logger.info("---- " + keys[i] + " --> " + values[i]);
		}

		return map;
	}

	protected void initialIteration() {
		switch (config.substanceIteration) {
		case ROW_SINGLE: {
			if (config.FlagSkipRows)
			{	
				while (config.skipRowsIndexSet.contains(curRowNum) 
						&& curRowNum <= primarySheet.getLastRowNum()) 
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
						parallelSheetStates[i]
								.initialIterateToNextNonEmptyRow();
						parallelSheetStates[i]
								.readRowsMultiDynamic(primarySheetSynchKey);
						break;

					case MATCH_KEY:
						parallelSheetStates[i]
								.initialIterateToNextNonEmptyRow();
						boolean FlagNextNonEmpty = (config.parallelSheets
								.get(i).dynamicIteration == DynamicIteration.NEXT_NOT_EMPTY);
						parallelSheetStates[i]
								.setRowGroups(
										config.parallelSheets.get(i).dynamicIterationColumnIndex,
										FlagNextNonEmpty);
						parallelSheetStates[i]
								.readRowsMultiDynamic(primarySheetSynchKey);
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
					logger.log(Level.WARNING,
							String.format("Row %d is empty", curRowNum));
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
					parallelSheetStates[i]
							.iterateRowMultiDynamic(primarySheetSynchKey);
			break;

		default:
			curRowNum++;
		}

		return 0;
	}
	
	protected void iterateExcel_skipRows()
	{
		iterateExcel();
		while (config.skipRowsIndexSet.contains(curRowNum) 
				&& curRowNum <= primarySheet.getLastRowNum()) {
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

		switch (config.dynamicIteration) {
		case NEXT_NOT_EMPTY: {
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
			// updated by the iteration functions
			Row r = curRow;
			curRows.add(r);
			curReadRowNum++;

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
						logger.info("****  read " + curRows.size()
								+ " rows /  next key: " + c.toString());
						return; // Reached next record
					}
				}
			} // end of while

			logger.info(" read " + curRows.size() + " rows");
		}

		case NEXT_DIFFERENT_VALUE: {
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

		// Typically substanceUUID is not set from the excel file but it is
		// possible if needed.
		ExcelDataLocation loc = config.substanceLocations
				.get("SubstanceRecord.substanceUUID");
		if (loc != null) {
			String s = getString(loc);
			if (s != null && !"".equals(s.trim()))
				r.setSubstanceUUID("XLSX-"
						+ UUID.nameUUIDFromBytes(s.getBytes()).toString());
		}

		loc = config.substanceLocations
				.get("SubstanceRecord.referenceSubstanceUUID");
		if (loc != null) {
			String s = getString(loc);
			if (s != null)
				r.setReferenceSubstanceUUID("XLSX-"
						+ UUID.nameUUIDFromBytes(s.getBytes()).toString());
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

		if (!config.FlagDynamicSpan)
			return;

		// Handle dynamic span
		DIOSynchronization dioSynch = new DIOSynchronization(
				basicSubstanceRecord, config.dynamicSpanInfo);

		if (config.dynamicIterationSpan != null) {
			switch (config.substanceIteration) {
			case ROW_MULTI_FIXED:
			case ROW_MULTI_DYNAMIC:
				DynamicIterationObject dio = config.dynamicIterationSpan
						.getDynamicIterationObjectFromRows(curRows);
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
					dioSynch.addDIO(
							config.parallelSheets.get(i).dynamicIterationSpan,
							dio);
					break;
				}
			}

		ArrayList<SubstanceRecord> records = dioSynch.synchronize();
		loadedRecordsBuffer.addAll(records);
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
	protected List<ProtocolApplication> readProtocolApplications()
			throws Exception {
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
	protected ProtocolApplication readProtocolApplication(
			ProtocolApplicationDataLocation padl) throws Exception {
		logger.log(Level.FINE, "Reading protocol application ...");
		Protocol protocol = readProtocol(padl);
		ProtocolApplication pa = new ProtocolApplication(protocol);

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
			String s = getString(padl.citationYear);
			if (s != null)
				pa.setReferenceYear(s);
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
		if (padl.parameters != null) 
		{
			IParams params = new Params();
			for (String param : padl.parameters.keySet()) 
			{	
				readParameter(param, padl.parameters.get(param), params);
			}
			pa.setParameters(params);
		}

		// Read effects array
		if (padl.effects != null) {
			for (int i = 0; i < padl.effects.size(); i++)
				try {
					EffectRecord effect = readEffect(padl.effects.get(i));
					pa.addEffect(effect);
				} catch (Exception x) {
					logger.log(Level.SEVERE, x.getMessage());
					throw x;
				}
		}

		// Read effects from EFFECTS_BLOCK
		if (padl.effectsBlock != null) 
		{
			for (ExcelDataBlockLocation excelEffectBlock : padl.effectsBlock)
			{	
				List<DataBlockElement> effDataBlock = getDataBlock(excelEffectBlock);
				for (DataBlockElement dbe : effDataBlock) {
					EffectRecord effect = dbe.generateEffectRecord();
					// TODO (2) set unit
					pa.addEffect(effect);
				}
			}	
		}

		return pa;
	}
	
	void readParameter(String param, ExcelDataLocation loc, IParams destinationParams) throws Exception
	{	
		String parameterName = param;
		
		//Handle parameter name from other excel data location
		if (loc.otherLocationFields != null)
		{
			ExcelDataLocation pNameLoc = loc.otherLocationFields.get("NAME");
			if (pNameLoc != null)
			{
				try {
					String nameString = getStringValue(pNameLoc);
					if (nameString != null)
						parameterName = nameString;
				} catch (Exception x) {
					logger.log(Level.FINE,String.format("%s\t%s\t%s", param,x.getMessage(),pNameLoc.toString()));
				}	
			}
		}
		
		//Enforcing parameter to be read as string
		if (loc.dataInterpretation == DataInterpretation.AS_TEXT)
		{
			String s = getString(loc);
			if (s != null)
				destinationParams.put(parameterName, s);
			return;			
		}
				
		//Enforcing parameter to be read as date
		if (loc.dataInterpretation == DataInterpretation.AS_DATE)
		{
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
			logger.log(Level.FINE,String.format("%s\t%s\t%s", param,x.getMessage(),loc.toString()));	
		}
		if (paramStringValue != null)
		{	
			RichValue rv = rvParser.parse(paramStringValue);
			String rv_error = rvParser.getAllErrorsAsString();

			if (rv_error == null) 
			{
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
		}	
		else try 
		{
			Number paramDoubleValue = getNumericValue(loc);
			if (paramDoubleValue != null)
			{	
				pVal = new Value();
				pVal.setLoValue(paramDoubleValue);
			}	
		} catch (Exception x) {
			logger.log(Level.FINE,String.format("%s\t%s\t%s", param,x.getMessage(),loc.toString()));	
		}
		
		
		if (pVal != null) //Parameters is stored as a Value object
		{	
			//Handle parameter unit from other excel data location
			ExcelDataLocation pUnitLoc = loc.otherLocationFields.get("UNIT");
			if (pUnitLoc != null)
			{
				try {
					String unitString = getStringValue(pUnitLoc);
					if (unitString != null)
						pVal.setUnits(unitString);
				} catch (Exception x) {
					logger.log(Level.FINE,String.format("%s\t%s\t%s", param,x.getMessage(),pUnitLoc.toString()));
				}	
			}
			
			destinationParams.put(parameterName, pVal);
		}
		else
		{
			//Parameter is stored as a string
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
	protected Protocol readProtocol(ProtocolApplicationDataLocation padl)
			throws Exception {
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
	protected EffectRecord readEffect(EffectRecordDataLocation efrdl)
			throws Exception {
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

					logger.log(Level.WARNING, String.format(
							"[%s ] Lo Qualifier \"%s\"",
							locationStringForErrorMessage(efrdl.loQualifier,
									primarySheetNum), s));
				}
			}
		}

		if (efrdl.loValue != null) {
			Number d = null;
			if (config.Fl_AllowQualifierInValueCell) {
				try {
					String qstring = getStringValue(efrdl.loValue);
					if (qstring != null) {
						RecognitionUtils.QualifierValue qv = RecognitionUtils
								.extractQualifierValue(qstring);
						if (qv.value == null)
							logger.log(
									Level.WARNING,
									String.format(
											"[%s] %s Lo Value/Qualifier error: %s",
											locationStringForErrorMessage(
													efrdl.loQualifier,
													primarySheetNum), qstring,
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
					logger.log(Level.WARNING, String.format(
							"[%s] Up Qualifier \"%s\" is incorrect!",
							locationStringForErrorMessage(efrdl.upQualifier,
									primarySheetNum), s));
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
					RecognitionUtils.QualifierValue qv = RecognitionUtils
							.extractQualifierValue(qstring);
					if (qv.value == null)
						logger.log(Level.WARNING, String.format(
								"[%s] %s Up Value/Qualifier error: %s",
								locationStringForErrorMessage(
										efrdl.upQualifier, primarySheetNum),
								qstring, qv.errorMsg));
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
					logger.log(Level.WARNING, String.format(
							"[%s] Err Qualifier '%s' is incorrect!",
							locationStringForErrorMessage(efrdl.errQualifier,
									primarySheetNum), s));
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
					RecognitionUtils.QualifierValue qv = RecognitionUtils
							.extractQualifierValue(qstring);
					if (qv.value == null)
						logger.log(Level.WARNING, String.format(
								"[%s] %s Err Value/Qualifier error: %s",
								locationStringForErrorMessage(
										efrdl.errQualifier, primarySheetNum),
								qstring, qv.errorMsg));
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
			// If present this takes precedence over the up/lo values and
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
				RichValue rv = rvParser.parse(richValueString);
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
				} else {
					logger.log(Level.WARNING, String
							.format("[%s] %s Value error: %s",
									locationStringForErrorMessage(efrdl.value,
											primarySheetNum), richValueString,
									rv_error));
					// parseErrors.add("[" +
					// locationStringForErrorMessage(efrdl.value,
					// primarySheetNum) + "] " + richValueString +
					// " Value error: " + rv_error);
				}
			} else try {
				d = getNumericValue(efrdl.value);
			} catch (CellException x) {
				logger.log(Level.WARNING,x.getMessage());
				effect.setTextValue(getString(efrdl.value));
			}

			if (d != null)
				effect.setLoValue(d.doubleValue()); // This is the default
													// behavior if the
			// cell is of type numeric
		}

		if (efrdl.conditions != null) {
			IParams conditions = readConditions (efrdl.conditions);
			effect.setConditions(conditions);
		}

		return effect;
	}
	
	IParams readConditions(HashMap<String, ExcelDataLocation> conditionsInfo) throws Exception
	{
		IParams conditions = new Params();

		Set<Entry<String, ExcelDataLocation>> locEntries = conditionsInfo
				.entrySet();
		for (Entry<String, ExcelDataLocation> entry : locEntries) 
		{
			//Conditions are read in the same way as parameters are read 
			readParameter(entry.getKey(), entry.getValue(), conditions);
			
			//This is the old simple approach: condition is always read as text
			//String value = getString(entry.getValue());
			//conditions.put(entry.getKey(), value);
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
	protected CompositionRelation readCompositionRelation(
			CompositionDataLocation cdl, SubstanceRecord record)
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
					try{
						propObj = getString(loc);
					}catch (Exception x) {
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
						logger.log(
								Level.WARNING,
								String.format("%s %s", x.getMessage(),
										loc.toString()));
					}

				if (propObj != null) {
					String sameas = Property.guessLabel(propName);
					Property property = new Property(propName, "", "");
					property.setLabel(sameas);
					structure.setRecordProperty(property, propObj);
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
				Number d = getNumericValue(cdl.proportion.typical_value);
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

			if (cdl.proportion.real_lower_value != null) {
				Number d = getNumericValue(cdl.proportion.real_lower_value);
				proportion.setReal_lowervalue(d.doubleValue());
			}

			if (cdl.proportion.real_upper_precision != null) {
				String s = getString(cdl.proportion.real_upper_precision);
				proportion.setReal_upper(s);
			}

			if (cdl.proportion.real_upper_value != null) {
				Number d = getNumericValue(cdl.proportion.real_upper_value);
				proportion.setReal_uppervalue(d.doubleValue());
			}

			if (cdl.proportion.real_unit != null) {
				String s = getString(cdl.proportion.real_unit);
				proportion.setReal_unit(s);
			}

		}// end of proportion

		CompositionRelation relation = new CompositionRelation(record,
				structure, cdl.structureRelation, proportion);
		return relation;
	}

	/**
	 * Generic function (regardless of the iteration access) Reads a string
	 * value from a cell which is of type String If cell is not of type 'String'
	 * error is generated
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected String getStringValue(ExcelDataLocation loc) throws Exception {
		switch (loc.iteration) {
		case ROW_SINGLE:
			if (loc.isFromParallelSheet()) {
				Row row = parallelSheetStates[loc.getParallelSheetIndex()].curRow;
				if (row != null)
					return getStringValue(row, loc);
				else
					return null;
			} else
				return getStringValue(curRow, loc); // from basic sheet

		case ROW_MULTI_FIXED:
		case ROW_MULTI_DYNAMIC:
			// Taking the value from the first row
			ArrayList<Row> rows;
			if (loc.isFromParallelSheet())
				rows = parallelSheetStates[loc.getParallelSheetIndex()].curRows;
			else
				rows = curRows;
			if (rows != null)
				if (!rows.isEmpty())
					return getStringValue(rows.get(0), loc);
			return null;

		case ABSOLUTE_LOCATION: {
			Object value = loc.getAbsoluteLocationValue();
			if (value == null) {
				value = getStringValueFromAbsoluteLocation(loc);
				loc.setAbsoluteLocationValue(value);
			}
			if (value != null)
				return value.toString();
			return null;
		}

		case JSON_VALUE: {
			Object value = loc.getJsonValue();
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else {
					throw new ExceptionAtLocation(loc, -1, "JSON_VALUE",
							" is not of type STRING!");
					//
				}
			return null;
		}

		case JSON_REPOSITORY: {
			String key = loc.getJsonRepositoryKey();
			Object value = config.jsonRepository.get(key);
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else {
					String msg = String
							.format("[%s] JSON_REPOSITORY value for key %s is not of type STRING!",
									locationStringForErrorMessage(loc));
					throw new Exception(msg);

				}
			return null;
		}

		case VARIABLE: {
			String key = loc.getVariableKey();
			Object value = curVariables.get(key);
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else {
					String msg = String
							.format("[%s] JSON_REPOSITORY value for key %s is not of type STRING!",
									locationStringForErrorMessage(loc));
					throw new Exception(msg);
				}
			return null;
		}

		default:
			return null;
		}
	}

	/**
	 * Generic function (regardless of the iteration access)
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected String getString(ExcelDataLocation loc) throws Exception {
		switch (loc.iteration) {
		case ROW_SINGLE:
			if (loc.isFromParallelSheet()) {
				Row row = parallelSheetStates[loc.getParallelSheetIndex()].curRow;
				if (row != null)
					return getString(row, loc);
				else
					return null;
			} else
				return getString(curRow, loc); // from basic sheet

		case ROW_MULTI_FIXED:
		case ROW_MULTI_DYNAMIC:
			// Taking the value from the first row
			ArrayList<Row> rows;
			if (loc.isFromParallelSheet())
				rows = parallelSheetStates[loc.getParallelSheetIndex()].curRows;
			else
				rows = curRows;
			if (rows != null)
				if (!rows.isEmpty())
					return getString(rows.get(0), loc);
			return null;

		case ABSOLUTE_LOCATION: {
			Object value = loc.getAbsoluteLocationValue();
			if (value == null) {
				value = getStringFromAbsoluteLocation(loc);
				loc.setAbsoluteLocationValue(value);
			}
			if (value != null)
				return value.toString();
			return null;
		}

		case JSON_VALUE: {
			Object value = loc.getJsonValue();
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else {
					String msg = String.format(
							"[%s] JSON_VALUE is not of type STRING!",
							locationStringForErrorMessage(loc));
					throw new Exception(msg);
				}
			return null;
		}

		case JSON_REPOSITORY: {
			String key = loc.getJsonRepositoryKey();
			Object value = config.jsonRepository.get(key);
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else {
					String msg = String
							.format("[%s] JSON_REPOSITORY value for key %s is not of type STRING!",
									locationStringForErrorMessage(loc));
					throw new Exception(msg);
				}
			return null;
		}

		case VARIABLE: {
			String key = loc.getVariableKey();
			Object value = curVariables.get(key);
			if (value != null)
				return value.toString();
			return null;
		}

		default:
			return null;
		}
	}
	
	/**
	 * Generic function (regardless of the iteration access)
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected Date getDate(ExcelDataLocation loc) throws Exception {
		switch (loc.iteration) {
		case ROW_SINGLE:
			if (loc.isFromParallelSheet()) {
				Row row = parallelSheetStates[loc.getParallelSheetIndex()].curRow;
				if (row != null)
					return getDate(row, loc);
				else
					return null;
			} else
				return getDate(curRow, loc); // from basic sheet
		
	case ROW_MULTI_FIXED:
	case ROW_MULTI_DYNAMIC:
		// Taking the value from the first row
		ArrayList<Row> rows;
		if (loc.isFromParallelSheet())
			rows = parallelSheetStates[loc.getParallelSheetIndex()].curRows;
		else
			rows = curRows;
		if (rows != null)
			if (!rows.isEmpty())
				return getDate(rows.get(0), loc);
		return null;
	
	case ABSOLUTE_LOCATION: {
		Object value = loc.getAbsoluteLocationValue();
		if (value == null) {
			value = getDateFromAbsoluteLocation(loc);
			loc.setAbsoluteLocationValue(value);
		}
		if (value != null && (value instanceof Date))
			return (Date) value;
		return null;
	}

	case JSON_VALUE: {
		Object value = loc.getJsonValue();
		if (value != null)
			if (value instanceof String)
			{	
				try
				{
					SimpleDateFormat formatter = new SimpleDateFormat(loc.dateFormat);
					Date d = formatter.parse((String) value);
					if (d != null)
						return d;
				}
				catch (Exception e){
					String msg = "JSON_VALUE value \"" 
							+ value + "\" is not correclty formatted date! ";
					throw new Exception(msg);
				}
				
			}
		return null;
	}

	case JSON_REPOSITORY: {
		String key = loc.getJsonRepositoryKey();
		Object value = config.jsonRepository.get(key);
		//TODO
		return null;
	}

	case VARIABLE: {
		//TODO
		return null;
	}

	default:
		return null;
	}	
}

	

	/**
	 * Generic function (regardless of the iteration access)
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected Number getNumericValue(ExcelDataLocation loc) throws Exception {
		switch (loc.iteration) {
		case ROW_SINGLE:
			if (loc.isFromParallelSheet()) {
				Row row = parallelSheetStates[loc.getParallelSheetIndex()].curRow;
				if (row != null)
					return getNumericValue(row, loc);
				else
					return null;
			} else
				return getNumericValue(curRow, loc);

		case ROW_MULTI_FIXED: // Both treated the same way
		case ROW_MULTI_DYNAMIC:
			// Taking the value from the first row
			ArrayList<Row> rows;
			if (loc.isFromParallelSheet())
				rows = parallelSheetStates[loc.getParallelSheetIndex()].curRows;
			else
				rows = curRows;
			if (rows != null)
				if (!rows.isEmpty())
					return getNumericValue(rows.get(0), loc);
			return null;

		case ABSOLUTE_LOCATION: {
			Object value = loc.getAbsoluteLocationValue();
			if (value == null) {
				value = getNumericFromAbsoluteLocation(loc);
				loc.setAbsoluteLocationValue(value);
			}
			if (value != null && (value instanceof Number))
				return (Number) value;
			return null;
		}

		case JSON_VALUE: {
			Object value = loc.getJsonValue();
			if (value != null) {
				if (value instanceof Number)
					return (Number) value;
				else {
					String msg = String.format(
							"[%s] JSON_VALUE is not of type NUMERIC!",
							locationStringForErrorMessage(loc));
					logger.log(Level.WARNING, msg);
					throw new Exception(msg);
				}
			}
			return null;
		}

		case JSON_REPOSITORY: {
			String key = loc.getJsonRepositoryKey();
			Object value = config.jsonRepository.get(key);
			if (value != null)
				if (value instanceof Number)
					return (Number) value;
				else {
					String msg = String
							.format("[%s] JSON_REPOSITORY value for key '%s' is not of type NUMERIC!",
									locationStringForErrorMessage(loc), key);
					logger.log(Level.WARNING, msg);
					throw new Exception(msg);
				}
			return null;
		}

		case VARIABLE: {
			String key = loc.getVariableKey();
			Object value = curVariables.get(key);
			if (value != null)
				if (value instanceof Number)
					return (Number) value;
				else {
					String msg = String
							.format("[%s] JSON_REPOSITORY value for key '%s' is not of type NUMERIC!",
									locationStringForErrorMessage(loc), key);
					logger.log(Level.WARNING, msg);
					throw new Exception(msg);
				}

			return null;
		}

		default:
			return null;
		}
	}

	/**
	 * 
	 * @param loc
	 * @return
	 */
	protected Object[] getArray(ExcelDataLocation loc) throws Exception
	{
		switch (loc.iteration) {
		case ROW_SINGLE:
			// TODO
			return null;

		case ROW_MULTI_FIXED: // Both treated the same way
		case ROW_MULTI_DYNAMIC:
			// TODO
			return null;

		case ABSOLUTE_LOCATION: {
			Object value = loc.getAbsoluteLocationValue();
			if (value == null) {
				value = getArrayFromAbsoluteLocation(loc);
				loc.setAbsoluteLocationValue(value);
			}
			if (value != null)
				return (Object[]) value;
			return null;
		}

		case JSON_VALUE: {
			Object value = loc.getJsonValue(); 
			if (value != null) 
			{	
				if (value instanceof Object[]) 
					return (Object[]) value; 
				else
					throw new ExceptionAtLocation(loc, -1, "JSON_VALUE",
							" is not an array!");
			}
			return null;
		}

		case JSON_REPOSITORY: {
			// TODO
			return null;
		}

		case VARIABLE: {
			/*
			 * String key = loc.getVariableKey(); Object value =
			 * curVariables.get(key); if (value != null) if (value instanceof
			 * Object[]) return (Object[]) value; else parseErrors.add("[" +
			 * locationStringForErrorMessage(loc) +
			 * "] variable value for key \"" + key + "\" is not an array!");
			 */
			return null;
		}

		default:
			return null;
		}
	}

	/**
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected String getStringValueFromAbsoluteLocation(ExcelDataLocation loc)
			throws Exception {
		Sheet sheet = workbook.getSheetAt(loc.sheetIndex);
		if (sheet != null) {
			Row r = sheet.getRow(loc.rowIndex);
			if (r == null)
				return null;

			Cell c = r.getCell(loc.columnIndex);
			if (c != null) {
				if (c.getCellType() != Cell.CELL_TYPE_STRING) {
					String msg = String.format(
							"[ %s ]: Cell is not of type STRING!",
							locationStringForErrorMessage(loc));
					throw new Exception(msg);
				}
				return c.getStringCellValue();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param loc
	 * @return
	 */
	protected String getStringFromAbsoluteLocation(ExcelDataLocation loc) {
		Sheet sheet = workbook.getSheetAt(loc.sheetIndex);
		if (sheet != null) {
			Row r = sheet.getRow(loc.rowIndex);
			if (r == null)
				return null;

			Cell c = r.getCell(loc.columnIndex);
			return ExcelUtils.getStringFromCell(c);
		}
		return null;
	}

	/**
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected Number getNumericFromAbsoluteLocation(ExcelDataLocation loc)
			throws Exception {
		Sheet sheet = workbook.getSheetAt(loc.sheetIndex);
		if (sheet != null) {
			Row r = sheet.getRow(loc.rowIndex);
			if (r == null)
				return null;

			Cell c = r.getCell(loc.columnIndex);

			Number d = ExcelUtils.getNumericValue(c);
			if (d != null)
				return d;
			else {
				String msg = String.format(
						"[%s]: Cell is not of type NUMERIC!",
						locationStringForErrorMessage(loc));
				logger.log(Level.WARNING, msg);
				throw new Exception(msg);
				// return null;
			}

			/*
			 * if (c != null) { if (c.getCellType() != Cell.CELL_TYPE_NUMERIC) {
			 * parseErrors.add("[" + locationStringForErrorMessage(loc) +
			 * "]: Cell is not of type NUMERIC!"); return null; } return
			 * c.getNumericCellValue(); }
			 */
		}
		return null;
	}
	
	/**
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected Date getDateFromAbsoluteLocation(ExcelDataLocation loc)
			throws Exception {
		Sheet sheet = workbook.getSheetAt(loc.sheetIndex);
		if (sheet != null) {
			Row r = sheet.getRow(loc.rowIndex);
			if (r == null)
				return null;

			Cell c = r.getCell(loc.columnIndex);

			Date d = ExcelUtils.getDateFromCell(c);
			if (d != null)
				return d;
			else {
				String msg = String.format(
						"[%s]: Cell is not of formatted as date!",
						locationStringForErrorMessage(loc));
				logger.log(Level.WARNING, msg);
				throw new Exception(msg);
				// return null;
			}			
		}
		return null;
	}

	/**
	 * 
	 * @param loc
	 * @return
	 */
	protected Object[] getArrayFromAbsoluteLocation(ExcelDataLocation loc) {
		Sheet sheet = workbook.getSheetAt(loc.sheetIndex);

		// The array is formed from a matrix defined by rows (and columns)
		// The rows are stored sequentially
		int rows[];
		int columns[];

		if (loc.rowIndices != null) {
			// rowIndices take precedence over rowIndex
			rows = loc.rowIndices;
		} else {
			rows = new int[1];
			rows[0] = loc.rowIndex;
		}

		if (loc.columnIndices != null) {
			// columnIndices takes precedence over columnsIndex
			columns = loc.columnIndices;
		} else {
			columns = new int[1];
			columns[0] = loc.columnIndex;
		}

		// Array size = the number of 'matrix' elements
		int n = rows.length * columns.length;
		Object objects[] = new Object[n];

		for (int i = 0; i < rows.length; i++) {
			Row r = sheet.getRow(rows[i]);
			if (r == null) {
				for (int k = 0; k < columns.length; k++)
					objects[i * columns.length + k] = null;
				continue;
			}

			for (int k = 0; k < columns.length; k++) {
				Cell c = r.getCell(columns[k]);
				Object o = ExcelUtils.getObjectFromCell(c);
				objects[i * columns.length + k] = o;
			}
		}

		if (loc.trimArray)
			return trimObjects(objects, rows.length, columns.length);

		return objects;
	}

	/**
	 * 
	 * @param objects
	 * @param nRows
	 * @param nColumns
	 * @return
	 */
	protected Object[] trimObjects(Object objects[], int nRows, int nColumns) {
		// The array is treated as a virtual matrix
		// Each dimension is trimmed to the maximal index that represents a
		// non-null object
		// correct values for nRows and nColumns are expected

		int maxRow = -1;
		int maxColumn = -1;
		for (int i = 0; i < nRows; i++) {
			for (int k = 0; k < nColumns; k++) {
				if (objects[i * nColumns + k] != null) {
					maxRow = i;
					if (maxColumn < k)
						maxColumn = k;
				}
			}
		}

		// constructing new 'virtual' matrix
		int nRows1 = maxRow + 1;
		int nColumns1 = maxColumn + 1;

		Object objects1[] = new Object[nRows1 * nColumns1];
		for (int i = 0; i < nRows1; i++)
			for (int k = 0; k < nColumns1; k++)
				objects1[i * nColumns1 + k] = objects[i * nColumns + k];

		return objects1;
	}

	/**
	 * 
	 * @param row
	 * @param loc
	 * @return Returns null if cell is not of string type (i.e. numerics are
	 *         treated as errors)
	 * @throws Exception
	 */
	protected String getStringValue(Row row, ExcelDataLocation loc)
			throws Exception {
		Cell c = row.getCell(loc.columnIndex);

		if (c == null) {
			if (loc.allowEmpty) {
				return "";
			} else {
				// if (FlagAddParserStringError)
				throw new CellException(loc.sectionName, (primarySheetNum + 1),
						(row.getRowNum() + 1), (loc.columnIndex + 1),
						"  is empty!");
			}

		}

		if (c.getCellType() != Cell.CELL_TYPE_STRING) {

			throw new CellException(
					loc.sectionName,
					(primarySheetNum + 1),
					(row.getRowNum() + 1),
					(loc.columnIndex + 1),
					String.format(
							"Found cell type %d, expected %d (CELL_TYPE_STRING)",
							c.getCellType(), Cell.CELL_TYPE_STRING));

		}

		return c.getStringCellValue();
	}

	/**
	 * 
	 * @param row
	 * @param loc
	 * @return
	 */
	protected String getString(Row row, ExcelDataLocation loc) {
		Cell c = row.getCell(loc.columnIndex);
		return ExcelUtils.getStringFromCell(c);
	}
	
	/**
	 * 
	 * @param row
	 * @param loc
	 * @return
	 */
	protected Date getDate(Row row, ExcelDataLocation loc) {
		Cell c = row.getCell(loc.columnIndex);
		return ExcelUtils.getDateFromCell(c);
	}

	/**
	 * 
	 * @param row
	 * @param loc
	 * @return
	 * @throws CellException
	 */
	protected Number getNumericValue(Row row, ExcelDataLocation loc)
			throws CellException {
		Cell c = row.getCell(loc.columnIndex);

		if (c == null) {
			if (loc.allowEmpty) { // why empty should be zero ?
				return null;
			} else {
				String msg = String.format(CellException.msg_format,
						loc.sectionName, (primarySheetNum + 1),
						(row.getRowNum() + 1), (loc.columnIndex + 1),
						" is empty!");
				logger.log(Level.WARNING, msg);

				return null;
			}
		}

		Number d = ExcelUtils.getNumericValue(c);

		if (d != null)
			return d;
		if (loc.allowEmpty && (Cell.CELL_TYPE_BLANK == c.getCellType()))
			return null;
		else {
			throw new CellException(
					loc.sectionName,
					(primarySheetNum + 1),
					(row.getRowNum() + 1),
					(loc.columnIndex + 1),
					String.format(
							"Found cell type %d, expected %d (CELL_TYPE_NUMERIC)",
							c.getCellType(), Cell.CELL_TYPE_NUMERIC));
		}

		/*
		 * if (c.getCellType() != Cell.CELL_TYPE_NUMERIC) {
		 * parseErrors.add("JSON Section " + loc.sectionName + ", sheet " +
		 * (primarySheetNum + 1) + ", row " + (row.getRowNum() + 1) + " cell " +
		 * (loc.columnIndex + 1) + " is not of type NUMERIC!"); return null; }
		 * 
		 * return c.getNumericCellValue();
		 */
	}

	/**
	 * 
	 * @param exdb_loc
	 * @return
	 */
	protected List<DataBlockElement> getDataBlock(
			ExcelDataBlockLocation exdb_loc) {
		switch (exdb_loc.location.iteration) {
		case ROW_SINGLE:
			// TODO
			return null;

		case ROW_MULTI_FIXED: // Both treated the same way
		case ROW_MULTI_DYNAMIC:
			// TODO
			return null;

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

	protected List<DataBlockElement> getDataBlockFromAbsolutePosition(
			ExcelDataBlockLocation exdb_loc) {
		Integer rowSubblocks = getIntegerFromExpression(exdb_loc.rowSubblocks);
		Integer columnSubblocks = getIntegerFromExpression(exdb_loc.columnSubblocks);
		Integer sbSizeRows = getIntegerFromExpression(exdb_loc.subblockSizeRows);
		Integer sbSizeColumns = getIntegerFromExpression(exdb_loc.subblockSizeColumns);

		logger.info("------------ getDataBlockFromAbsolutePosition");
		logger.info("--- rowSubblocks = " + rowSubblocks);
		logger.info("--- columnSubblocks = " + columnSubblocks);
		logger.info("--- subblockSizeRows = " + sbSizeRows);
		logger.info("--- subblockSizeColumns = " + sbSizeColumns);

		if (rowSubblocks == null || columnSubblocks == null
				|| sbSizeRows == null || sbSizeColumns == null) {
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

		Sheet sheet = workbook.getSheetAt(exdb_loc.location.sheetIndex);

		for (int i = 0; i < n; i++) {
			Row row = sheet.getRow(startRow + i);
			String s = "";
			for (int k = 0; k < m; k++)
				try {
					Cell c = row.getCell(startColumn + k);
					cells[i][k] = c;
					// s += ("  " + ExcelUtils.getObjectFromCell(c));
				} catch (Exception x) {
					cells[i][k] = null;
					logger.warning(x.getMessage());
				}
			// logger.info(">>>> " + s);
		}

		return getDataBlockFromCellMatrix(cells, rowSubblocks, columnSubblocks,
				sbSizeRows, sbSizeColumns, exdb_loc);
	}

	protected List<DataBlockElement> getDataBlockFromCellMatrix(Cell cells[][],
			int rowSubblocks, int columnSubblocks, int subblockSizeRows,
			int subblockSizeColumns, ExcelDataBlockLocation exdb_loc) {
		List<DataBlockElement> dbeList = new ArrayList<DataBlockElement>();

		if (exdb_loc.valueGroups == null)
			return dbeList;

		// Analyze value groups
		List<BlockValueGroupExtractedInfo> bvgExtrInfo = new ArrayList<BlockValueGroupExtractedInfo>();
		for (BlockValueGroup bvg : exdb_loc.valueGroups) {
			BlockValueGroupExtractedInfo bvgei = extractBlockValueGroup(bvg);
			if (bvgei.getErrors().isEmpty())
				bvgExtrInfo.add(bvgei);
			else {
				logger.info("--- Value Group " + bvg.name + "errors:");
				for (String err : bvgei.getErrors())
					logger.info("--- " + err);
			}
		}

		// Iterating all sub-blocks
		for (int sbRow = 0; sbRow < rowSubblocks; sbRow++)
			for (int sbColumn = 0; sbColumn < columnSubblocks; sbColumn++) {
				// Upper left corner of the current sub-block
				int row0 = sbRow * subblockSizeRows;
				int column0 = sbColumn * subblockSizeColumns;

				for (BlockValueGroupExtractedInfo bvgei : bvgExtrInfo) {
					if (bvgei.FlagValues) {
						// Shifted by -1 to make it 0-based indexing
						for (int i = bvgei.startRow - 1; i <= bvgei.endRow - 1; i++)
							for (int k = bvgei.startColumn - 1; k <= bvgei.endColumn - 1; k++) {
								Object o = ExcelUtils
										.getObjectFromCell(cells[row0 + i][column0 + k]);
								DataBlockElement dbEl = new DataBlockElement();
								dbEl.blockValueGroup = bvgei.name;
								dbEl.unit = bvgei.unit; // The unit may be
														// overriden by the
														// setValue() function
								dbEl.setValue(o, rvParser);
								if (bvgei.errorColumnShift != 0 || bvgei.errorRowShift != 0)
								{	
									Number d  = (Double)ExcelUtils.getNumericValue(
												cells[row0 + i + bvgei.errorRowShift]
												[column0 + k + bvgei.errorColumnShift]);
									if (d != null)
										dbEl.error = d.doubleValue();
								}	

								// Handle parameters
								if (bvgei.paramInfo != null)
									if (!bvgei.paramInfo.isEmpty()) {
										dbEl.params = new Params();
										for (BlockValueGroupExtractedInfo.ParamInfo pi : bvgei.paramInfo) {
											if (pi.jsonValue != null)
											{
												//json value takes precedence over ASSIGN
												Object value = pi.jsonValue;

												if (pi.mapping != null)													
													value = getMappingValue(
															pi.jsonValue,
															pi.mapping);

												value = RichValue
														.recognizeRichValueFromObject(
																value,
																pi.unit,
																rvParser);
												dbEl.params.put(pi.name,value);
												continue;
											}
											
											
											Cell c = null;
											switch (pi.assign) {
											case ASSIGN_TO_BLOCK:
												c = cells[pi.rowPos - 1][pi.columnPos - 1]; // -1
																							// for
																							// 0-based
																							// indexing
												break;
											case ASSIGN_TO_SUBBLOCK:
												c = cells[row0 + pi.rowPos - 1][column0
														+ pi.columnPos - 1]; // (rowPos,columnPos)
																				// are
																				// the
																				// sub-block
																				// position
																				// -1
																				// for
																				// 0-based
																				// indexing
												break;
											case ASSIGN_TO_VALUE:
												c = cells[row0 + i + pi.rowPos][column0
														+ k + pi.columnPos]; // (rowPos,columnPos)
																				// are
																				// used
																				// as
																				// shifts
												break;
											case UNDEFINED:
												// nothing is done
												break;
											}

											if (c != null) {
												Object value = ExcelUtils
														.getObjectFromCell(c);

												if (value != null) {
													if (pi.mapping != null)
														value = getMappingValue(
																value,
																pi.mapping);

													value = RichValue
															.recognizeRichValueFromObject(
																	value,
																	pi.unit,
																	rvParser);
													dbEl.params.put(pi.name,
															value);
												}
											}
										}
									}

								dbeList.add(dbEl);
							}
					}

				}

			} // iterating all sub-blocks

		return dbeList;
	}

	protected BlockValueGroupExtractedInfo extractBlockValueGroup(
			BlockValueGroup bvg) {
		BlockValueGroupExtractedInfo bvgei = new BlockValueGroupExtractedInfo();

		if (bvg.name != null) {
			bvgei.name = getStringFromExpression(bvg.name);
			if (bvgei.name == null)
				bvgei.errors
						.add("VALUE_GROUPS: \"NAME\" is an incorrect expression: "
								+ bvg.name);
		}

		if (bvg.unit != null) {
			bvgei.unit = getStringFromExpression(bvg.unit);
			if (bvgei.name == null)
				bvgei.errors
						.add("VALUE_GROUPS: \"UNIT\" is an incorrect expression: "
								+ bvg.unit);
		}

		// Handle values
		bvgei.startColumn = getIntegerFromExpression(bvg.startColumn);
		bvgei.endColumn = getIntegerFromExpression(bvg.endColumn);
		bvgei.startRow = getIntegerFromExpression(bvg.startRow);
		bvgei.endRow = getIntegerFromExpression(bvg.endRow);
		
		bvgei.errorColumnShift = getIntegerFromExpression(bvg.errorColumnShift);
		bvgei.errorRowShift = getIntegerFromExpression(bvg.errorRowShift);

		logger.info("--- Extracting inffo for value group: " + bvg.name);
		logger.info("--- startColumn " + bvgei.startColumn);
		logger.info("--- endColumn " + bvgei.endColumn);
		logger.info("--- startRow " + bvgei.startRow);
		logger.info("--- endRow " + bvgei.endRow);

		bvgei.FlagValues = true;

		if (bvgei.startColumn == null) {
			bvgei.errors.add("START_COLUMN:  incorrect result for expression: "
					+ bvg.startColumn);
			bvgei.FlagValues = false;
		}

		if (bvgei.endColumn == null) {
			bvgei.errors.add("END_COLUMN:  incorrect result for expression: "
					+ bvg.endColumn);
			bvgei.FlagValues = false;
		}

		if (bvgei.startRow == null) {
			bvgei.errors.add("START_ROW:  incorrect result for expression: "
					+ bvg.startRow);
			bvgei.FlagValues = false;
		}

		if (bvgei.endRow == null) {
			bvgei.errors.add("END_ROW:  incorrect result for expression: "
					+ bvg.endRow);
			bvgei.FlagValues = false;
		}

		if (bvgei.FlagValues) {
			if (bvgei.startColumn > bvgei.endColumn) {
				bvgei.errors.add("START_COLUMN > END_COLUMN");
				bvgei.FlagValues = false;
			}

			if (bvgei.startRow > bvgei.endRow) {
				bvgei.errors.add("START_ROW > END_ROW");
				bvgei.FlagValues = false;
			}
			
			if (bvgei.errorColumnShift == null)
			{	
				bvgei.errors.add("ERROR_COLUMN_SHIFT:  incorrect result for expression: "
						+ bvg.errorColumnShift);
				bvgei.FlagValues = false;
			}
			
			if (bvgei.errorRowShift == null)
			{	
				bvgei.errors.add("ERROR_ROW_SHIFT:  incorrect result for expression: "
						+ bvg.errorRowShift);
				bvgei.FlagValues = false;
			}
		}

		if (bvg.parameters != null)
			if (!bvg.parameters.isEmpty()) {
				bvgei.paramInfo = new ArrayList<BlockValueGroupExtractedInfo.ParamInfo>();

				for (int i = 0; i < bvg.parameters.size(); i++) {
					boolean FlagParamOK = true;
					BlockParameter bp = bvg.parameters.get(i);
					BlockValueGroupExtractedInfo.ParamInfo pi = new BlockValueGroupExtractedInfo.ParamInfo();
					if (bp.name == null) {
						bvgei.errors.add("Parameter " + (i + 1)
								+ ": NAME is missing!");
						FlagParamOK = false;
					} else
						pi.name = bp.name;
					
					if (bp.jsonValue != null)
						pi.jsonValue = bp.jsonValue;

					if (bp.assign == BlockParameterAssign.UNDEFINED) {
						bvgei.errors.add("Parameter " + (i + 1)
								+ ": ASSIGN is UNDEFINED!");
						FlagParamOK = false;
					} else
						pi.assign = bp.assign;

					Integer intVal = getIntegerFromExpression(bp.columnPos);
					if (intVal == null) {
						bvgei.errors.add("Parameter " + (i + 1)
								+ ": COLUMN_POS is incorrect!");
						FlagParamOK = false;
					} else
						pi.columnPos = intVal;

					intVal = getIntegerFromExpression(bp.rowPos);
					if (intVal == null) {
						bvgei.errors.add("Parameter " + (i + 1)
								+ ": ROW_POS is incorrect!");
						FlagParamOK = false;
					} else
						pi.rowPos = intVal;

					if (bp.mapping != null)
						pi.mapping = bp.mapping;

					String strUnit = getStringFromExpression(bp.unit);
					if (strUnit != null)
						pi.unit = strUnit;

					if (FlagParamOK) {
						// TODO some additional checks for the positions if
						// needed
					}

					if (FlagParamOK)
						bvgei.paramInfo.add(pi);
				}
			}

		return bvgei;
	}

	protected Integer getIntegerFromExpression(Object obj) {
		if (obj == null)
			return null;

		if (obj instanceof Integer)
			return (Integer) obj;

		if (obj instanceof String) {
			String s = (String) obj;
			if (s.startsWith("=")) {
				s = s.substring(1);
				try {
					Object res = evaluateExpression(s);
					if (res != null) {
						// logger.info("Expression result: " + res +
						// "   class name " + res.getClass().getName());

						if (res instanceof Integer)
							return (Integer) res;

						if (res instanceof Double)
							return ((Double) res).intValue();

						if (res instanceof Long)
							return ((Long) res).intValue();

					}

				} catch (Exception e) {
					logger.info("Expression error: " + e.getMessage());
				}
			} else {
				try {
					Integer res = Integer.parseInt(s);
					return res;
				} catch (Exception e) {
					logger.info("Expression error: " + e.getMessage());
				}
			}
		}

		return null;
	}

	protected String getStringFromExpression(Object obj) {
		if (obj == null)
			return null;

		if (obj instanceof Number)
			return obj.toString();

		if (obj instanceof String) {
			String s = (String) obj;
			if (s.startsWith("=")) {
				s = s.substring(1);
				try {
					Object res = evaluateExpression(s);
					if (res != null) {
						if (res instanceof Number)
							return res.toString();

						if (res instanceof String)
							return res.toString();
					}

				} catch (Exception e) {
					logger.info("Expression error: " + e.getMessage());
				}
			} else
				return s;
		}

		return null;
	}

	protected Object evaluateExpression(String expression) throws Exception {
		JexlEngine jexl = getJexlEngine();
		Expression e = jexl.createExpression(expression);

		// Create context from the variables
		JexlContext variableContext = getContextFromVariables();

		Object result = e.evaluate(variableContext);
		return result;
	}

	protected JexlContext getContextFromVariables() {
		JexlContext context = new MapContext();
		Set<String> keys = curVariables.keySet();

		// logger.info("variables:");

		for (String key : keys) {
			context.set(key, curVariables.get(key));

			/*
			 * //Logging the variables values Object v = curVariables.get(key);
			 * String s = ""; if (v instanceof Object[]) { Object v1[] =
			 * (Object[]) v; for (int i = 0; i < v1.length; i++) s += (" " +
			 * v1[i]); } else s = v.toString(); logger.info(key + " : " + s);
			 */
		}

		return context;
	}

	protected JexlEngine getJexlEngine() {
		if (jexlEngine == null) {
			jexlEngine = new JexlEngine();
			jexlEngine.setCache(512);
			jexlEngine.setLenient(false);
			jexlEngine.setSilent(false);
		}
		return jexlEngine;
	}

	protected Object getMappingValue(Object originalValue, String mapping) {
		HashMap<Object, Object> map = curVariableMappings.get(mapping);
		if (map == null)
			return null;
		// Original read value is used as a key to obtain the result value;
		return map.get(originalValue);
	}

	private String locationStringForErrorMessage(ExcelDataLocation loc) {
		return String.format("Col %d Row %d", loc.columnIndex, loc.rowIndex);
	}

	private String locationStringForErrorMessage(ExcelDataLocation loc,
			int sheet) {
		return String.format("[Sheet %d Col %d Row %d", sheet, loc==null?null:loc.columnIndex,
				loc==null?null:loc.rowIndex);
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
	public void handleError(String arg0, int arg1, int arg2, int arg3)
			throws CDKException {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleError(String arg0, int arg1, int arg2, int arg3,
			Exception arg4) throws CDKException {
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
	 * Planned Implementation Tasks:
	 * 
	 * - Recognition modes: BY_NAME, BY_INDEX_AND_NAME
	 * 
	 * - in ExcelDataLocation class separate 'recognitions' for the Sheet, Row
	 * and Column
	 * 
	 * - Iteration modes: ROW_MULTI_FIXED, ROW_MULTI_DYNAMIC, COLUMN_* ...
	 * 
	 * - List of all allowed JSON keywords + optional checking
	 * 
	 * - Read information from variables into the class ExcelDataLocation
	 * 
	 * - Reading on particular sheet + linked parallel reading on several sheets
	 * (e.g. ModNanoTox should need this (eventually more work variables of the
	 * kind: curSheet... would be needed) + synchronization ...
	 * 
	 * - Definition of an 'END of reading" region i.e. after that point the
	 * excel data is not considered. This idea can be further developed to a
	 * substance record filtration utility - may be a special class for
	 * filtration...
	 * 
	 * - dynamic (automatic) recognition of SubtsanceRecord elements from Excel:
	 * protocol applications, effects, endpoints, conditions keyword suggestion
	 * DYNAMIC_SPAN
	 * 
	 * - handle ParserConfiguration flags: by JSON + Java Method
	 * 
	 * - add to ExcelDataLocation keywords for UUID generation flag + possible
	 * processing of data from that location
	 * 
	 * - conditions to be read as RichValue and stored as Value object
	 * 
	 * - String_or_Double Excel/Json utils
	 * 
	 * - Check the consistency of the ExcelDataLocation (loc variables) and the
	 * global data access/parallel sheet access, ...
	 * 
	 * - dynamic iteration in mode NEXT_DIFFERENT_VALUE (both for primary and
	 * parallel sheets)
	 * 
	 * - Move some of the "extraction" functions from class
	 * ExcelParserConfiguration to the corresponding *DataLocation class -
	 * analogously: Move handling of parallel sheets to the corresponding
	 * classes + move some functionality as static to ExcelParserUtils
	 * 
	 * - Error messages to be logged out /option to switch off memory storage of
	 * the error messages
	 * 
	 */

}
