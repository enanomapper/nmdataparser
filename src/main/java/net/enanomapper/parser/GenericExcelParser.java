package net.enanomapper.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import net.enanomapper.parser.ParserConstants.DynamicIteration;
import net.enanomapper.parser.ParserConstants.IterationAccess;
import net.enanomapper.parser.ParserConstants.Recognition;
import net.enanomapper.parser.ParserConstants.SheetSynchronization;
import net.enanomapper.parser.excel.ExcelUtils;
import net.enanomapper.parser.recognition.RecognitionUtils;
import net.enanomapper.parser.recognition.RichValue;
import net.enanomapper.parser.recognition.RichValueParser;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.IChemObjectReaderErrorHandler;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.listener.IChemObjectIOListener;
import org.openscience.cdk.io.setting.IOSetting;

import ambit2.base.data.StructureRecord;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.relation.composition.CompositionRelation;
import ambit2.base.relation.composition.Proportion;
import ambit2.core.io.IRawReader;


/**
 * 
 * @author nick
 *
 */
public class GenericExcelParser implements IRawReader<SubstanceRecord>
{	
	
	private final static Logger logger = Logger.getLogger(GenericExcelParser.class.getName());
	
	public static String key00 = "";
	protected RichValueParser rvParser = new RichValueParser ();
	protected ArrayList<String> parseErrors = new ArrayList<String> ();
	protected ArrayList<String> parallelSheetsErrors = new ArrayList<String> ();
	
	
	protected ExcelParserConfigurator config = null;
	protected  InputStream input;
	
	protected Workbook workbook;
	protected boolean xlsxFormat = false;
	
	//Helper variables for excel file iteration
	protected ParallelSheetState parallelSheetStates[] = null;
	
	//Primary sheet
	protected int primarySheetNum = 0;
	protected Sheet primarySheet = null;
	protected int curRowNum = 1;     //This is used by the iteration logic
	protected int curReadRowNum = 1; //This shows the actual read rows in multi-dynamic iteration mode
	protected int curCellNum = 1;	
	protected Row curRow = null;
	protected ArrayList<Row> curRows = null;
	protected Iterator<Row> rowIt = null; 
	protected Cell curCell = null;
	protected String primarySheetSynchKey = null;
	
	//All variables read from the primary sheet and all parallel sheets
	protected HashMap<String, Object> curVariables = new HashMap<String, Object>();
	
	private boolean FlagNextRecordLoaded = false; //This flag is true when next object is iterated and successfully read to the buffer; 
	private SubstanceRecord basicSubstanceRecord = null;
	private SubstanceRecord nextRecordBuffer = null;
	private int nextRecordIndex = -1;
	
	private ArrayList<SubstanceRecord> loadedRecordsBuffer = new ArrayList<SubstanceRecord>();
	
	private boolean FlagAddParserStringError = true;  //This is used to switch off errors in some cases
	
	public GenericExcelParser(InputStream input, String jsonConfig) throws Exception
	{
		this(input, jsonConfig, true);
	}
	
	public GenericExcelParser(InputStream input, String jsonConfig, boolean xlsxFormat) throws Exception
	{
		super();
		this.xlsxFormat = xlsxFormat;
		this.input = input;
		
		config = ExcelParserConfigurator.loadFromJSON(jsonConfig);
		if (config.configErrors.size() > 0)		
			throw new Exception("GenericExcelParser configuration errors:\n" + config.getAllErrorsAsString());
		
		setReader(input);
		init();
	}
	
	public ExcelParserConfigurator getExcelParserConfigurator() 
	{
		return config;
	}
	
	protected void init() throws Exception
	{
		handleConfigRecognitions();
		
		//Setting of the basic sheet work variables
		initBasicWorkSheet();
		
		//Setting the parallel sheets work variables 
		if (!config.parallelSheets.isEmpty())
		{	
			initParallelSheets();
			handleParallelSheetIndices();
			if (!parallelSheetsErrors.isEmpty())
				throw new Exception (paralleSheetsErrorsToString());
		}	
		
		initialIteration();		
		FlagNextRecordLoaded = false;
		nextRecordBuffer = null;
		
		
	}
	
	protected void initBasicWorkSheet()
	{
		primarySheetNum = config.sheetIndex;
		primarySheet = workbook.getSheetAt(primarySheetNum);
		curRowNum = config.startRow;
		logger.info("primarySheet# = " + (primarySheetNum + 1) + "   starRow# = " + (curRowNum + 1) + "\n" + "Last row# = " + (primarySheet.getLastRowNum() + 1));
	}
	
	protected void initParallelSheets() throws Exception
	{
		parallelSheetStates = new ParallelSheetState[config.parallelSheets.size()];
		for (int i = 0; i < config.parallelSheets.size(); i++)
		{
			ExcelSheetConfiguration eshc = config.parallelSheets.get(i);
			parallelSheetStates[i] = new ParallelSheetState();
			//if (eshc.FlagSheetIndex) //this check should not be needed because sheetNum must set via sheetName as well
			parallelSheetStates[i].sheetNum = eshc.sheetIndex;
			parallelSheetStates[i].synchronization = eshc.synchronization;
			parallelSheetStates[i].dynamicIteration = eshc.dynamicIteration;
			parallelSheetStates[i].dynamicIterationColumnIndex = eshc.dynamicIterationColumnIndex;
			
			if (0 <= parallelSheetStates[i].sheetNum && parallelSheetStates[i].sheetNum < workbook.getNumberOfSheets())
				parallelSheetStates[i].sheet = workbook.getSheetAt(parallelSheetStates[i].sheetNum);
			else
			{	
				throw new Exception("Incorrect SHEET_INDEX " + (parallelSheetStates[i].sheetNum +1)+ " in parallel sheet #" + (i+1));
			}	
			
			parallelSheetStates[i].curRowNum = eshc.startRow;
		}
	}
	
	protected  void handleConfigRecognitions()
	{
		Set<Entry<String, ExcelDataLocation>> locEntries = config.substanceLocations.entrySet();
		for (Entry<String, ExcelDataLocation> entry : locEntries )
		{
			ExcelDataLocation loc = entry.getValue();
			handleRecognition(loc);
		}
		
		for (ProtocolApplicationDataLocation ploc : config.protocolAppLocations)
			handleRecognition(ploc);
	}
	
	
	protected void handleRecognition(ExcelDataLocation loc)
	{
		//TODO
	}
	
	protected void handleRecognition(ProtocolApplicationDataLocation paLocation)
	{
		//TODO
	}
	
	protected void handleRecognition(EffectRecordDataLocation efrdl)
	{
		//TODO
	}
	
	protected void handleParallelSheetIndices()
	{
		for (String key : config.substanceLocations.keySet())
		{
			ExcelDataLocation loc = config.substanceLocations.get(key);
			setParallelSheet(loc);
		}
		
		for (ProtocolApplicationDataLocation padl : config.protocolAppLocations)
			setParallelSheets(padl);
		
		for (CompositionDataLocation cdl : config.composition)
			cdl.setParallelSheets(parallelSheetStates, primarySheetNum, parallelSheetsErrors);
	}
	
	protected void setParallelSheet(ExcelDataLocation loc)
	{
		if (loc.sheetIndex != primarySheetNum)
		{
			for (int i = 0; i < parallelSheetStates.length; i ++)
				if (loc.sheetIndex == parallelSheetStates[i].sheetNum)
				{
					loc.setParallelSheetIndex(i);
					return;
				}
			
			if (loc.iteration != ParserConstants.IterationAccess.ABSOLUTE_LOCATION) //This iteration mode is not treated as error
				parallelSheetsErrors.add("["+ locationStringForErrorMessage(loc) +  "] Sheet number number not valid parallel sheet!");
		}
	}
	
	protected void setParallelSheets(ProtocolApplicationDataLocation padl)
	{
		if (padl.citationOwner != null)
			setParallelSheet(padl.citationOwner);
		
		if (padl.citationTitle != null)
			setParallelSheet(padl.citationTitle);
			
		if (padl.citationOwner != null)
			setParallelSheet(padl.citationOwner);
		
		if (padl.protocolTopCategory  != null)
			setParallelSheet(padl.protocolTopCategory );
		
		if (padl.protocolCategoryCode  != null)
			setParallelSheet(padl.protocolCategoryCode );
		
		if (padl.protocolCategoryTitle  != null)
			setParallelSheet(padl.protocolCategoryTitle );
		
		if (padl.protocolEndpoint != null)
			setParallelSheet(padl.protocolEndpoint );
		
		if (padl.protocolGuideline != null)
			for (ExcelDataLocation loc : padl.protocolGuideline)
				setParallelSheet(loc);
		
		if (padl.parameters != null)
		for (String param : padl.parameters.keySet())
		{
			ExcelDataLocation loc = padl.parameters.get(param);
			setParallelSheet(loc);
		}
		
		if (padl.reliability_isRobustStudy != null)
			setParallelSheet(padl.reliability_isRobustStudy );
		
		if (padl.reliability_isUsedforClassification != null)
			setParallelSheet(padl.reliability_isUsedforClassification );
		
		if (padl.reliability_isUsedforMSDS != null)
			setParallelSheet(padl.reliability_isUsedforMSDS );
		
		if (padl.reliability_purposeFlag != null)
			setParallelSheet(padl.reliability_purposeFlag );
		
		if (padl.reliability_studyResultType != null)
			setParallelSheet(padl.reliability_studyResultType );
		
		if (padl.reliability_value != null)
			setParallelSheet(padl.reliability_value );
		
		if (padl.interpretationResult != null)
			setParallelSheet(padl.interpretationResult );
		
		if (padl.interpretationCriteria != null)
			setParallelSheet(padl.interpretationCriteria );
		
		if (padl.effects != null)
			for (EffectRecordDataLocation efrdl : padl.effects)
				setParallelSheets(efrdl);
	}
	
	protected String paralleSheetsErrorsToString()
	{
		StringBuffer sb = new StringBuffer();
		for (String s : parallelSheetsErrors)
			sb.append(s + "\n");
		return sb.toString();
	}
	
	
	protected void setParallelSheets(EffectRecordDataLocation efrdl)
	{
		if (efrdl.sampleID != null)
			setParallelSheet(efrdl.sampleID);
		
		if (efrdl.endpoint!= null)
			setParallelSheet(efrdl.endpoint);
		
		if (efrdl.conditions != null)
			for (String key : efrdl.conditions.keySet())
			{
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

	@Override
	public void setReader(Reader arg0) throws CDKException {
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
			throw new CDKException(x.getMessage(),x);
		}
	}


	@Override
	public void close() throws IOException {
		input.close();
		input = null;
		workbook = null;
	}



	@Override
	public boolean hasNext() {
		
		if (FlagNextRecordLoaded)  //Next record is already read and loaded to the buffer
			return true;
		
		
		if (nextRecordIndex != -1)
		{
			nextRecordIndex++;
			if (nextRecordIndex < loadedRecordsBuffer.size())
			{	
				nextRecordBuffer = loadedRecordsBuffer.get(nextRecordIndex);
				FlagNextRecordLoaded = true;
				return true;
			}
			else
				nextRecordIndex = -1; //Reached the end of loadedRecordsBuffer
		}
		
		
		if (nextRecordIndex == -1)  //Loading new buffer
		{
			if (hasExcelDataForNextRecord())
			{
				//This is the actual reading of next substance record/records
				readVariables();
				loadSubstanceRecords();
				logger.info("#### Loaded " + loadedRecordsBuffer.size() + " substances into the buffer");
				if (loadedRecordsBuffer.isEmpty())
					FlagNextRecordLoaded = false;
				else
				{
					nextRecordIndex = 0;
					nextRecordBuffer = loadedRecordsBuffer.get(nextRecordIndex);
					FlagNextRecordLoaded = true;
				}
					
				//nextRecordBuffer = getBasicSubstanceRecord();
				//if (nextRecordBuffer == null)
				//	nextRecordBuffer = new SubstanceRecord();
				//FlagNextRecordLoaded = true;
					
				iterateExcel();
				return FlagNextRecordLoaded;
			}
			else
				return false;
		}
		else
			return true;
	}
	
	
	private boolean hasExcelDataForNextRecord()
	{
		switch (config.substanceIteration)
		{
		case ROW_SINGLE:
		case ROW_MULTI_DYNAMIC:	
			//Decision logic: at least one row must be left on the primary sheet
			if (curRowNum <= primarySheet.getLastRowNum())
				return true;
			else
				return false;
			
		case ROW_MULTI_FIXED:
			//Decision logic: at least config.rowMultiFixedSize rows must be left on the primary sheet
			if (curRowNum <= primarySheet.getLastRowNum() - config.rowMultiFixedSize)
				return true;
			else
				return false;
			
		default:	
			return false;
		}
	}


	@Override
	public Object next() {
		return nextRecord();
	}


	@Override
	public SubstanceRecord nextRecord()
	{
		if (hasNext())
		{	
			SubstanceRecord result = nextRecordBuffer;
			nextRecordBuffer = null;        //Invalidate (empty) the buffer for the next record 
			FlagNextRecordLoaded = false;   //Invalidate for the next record
			return result;
		}
		else
			return null;
	}
	
	protected void readVariables()
	{
		curVariables.clear();
		if (config.variableLocations != null)
		{
			for (String var : config.variableLocations.keySet())
			{
				ExcelDataLocation loc = config.variableLocations.get(var);
				FlagAddParserStringError = false;
				String s = getStringValue(loc);
				FlagAddParserStringError  = true;
				if (s != null)
					curVariables.put(var, s);
				else
				{
					Double d = getNumericValue(loc);
					if (d != null)
						curVariables.put(var, d);
				}	
			}
		}
		
		for (ExcelSheetConfiguration eshc : config.parallelSheets)
		{
			if (eshc.variableLocations != null)
			{	
				for (String var : eshc.variableLocations.keySet())
				{
					ExcelDataLocation loc = eshc.variableLocations.get(var);
					FlagAddParserStringError = false;
					String s = getStringValue(loc);
					FlagAddParserStringError  = true;
					if (s != null)
						curVariables.put(var, s);
					else
					{
						Double d = getNumericValue(loc);
						if (d != null)
							curVariables.put(var, d);
					}	
				}
			}	
		}
		
	}
	
	protected void initialIteration()
	{	
		switch (config.substanceIteration)
		{
		case ROW_SINGLE :
		{	
			curRow = primarySheet.getRow(curRowNum);

			//Initial iteration for each parallel sheet 
			if (parallelSheetStates != null)
				for (int i = 0; i < parallelSheetStates.length; i++)
				{
					parallelSheetStates[i].curRow = parallelSheetStates[i].sheet.getRow(parallelSheetStates[i].curRowNum);
				}
		}
		break;
		
		case ROW_MULTI_FIXED :
		case ROW_MULTI_DYNAMIC :
		{
			//Skip starting empty rows
			while (curRowNum <= primarySheet.getLastRowNum())
			{	
				curRow = primarySheet.getRow(curRowNum);
				if (isEmpty(curRow))
				{	
					//Empty row is skipped
					curRowNum++;
					continue;
				}
				else
					break;
			}	
			
			readRowsMultiDynamic();
			
			//Initial iteration for each parallel sheet
			if (parallelSheetStates != null)
				for (int i = 0; i < parallelSheetStates.length; i++)
				{
					switch (config.parallelSheets.get(i).synchronization)
					{
					case NONE:
						parallelSheetStates[i].initialIterateToNextNonEmptyRow();
						parallelSheetStates[i].readRowsMultiDynamic(primarySheetSynchKey);
						break;	

					case MATCH_KEY:
						parallelSheetStates[i].initialIterateToNextNonEmptyRow();
						boolean FlagNextNonEmpty = (config.parallelSheets.get(i).dynamicIteration == DynamicIteration.NEXT_NOT_EMPTY);
						parallelSheetStates[i].setRowGroups(config.parallelSheets.get(i).dynamicIterationColumnIndex, FlagNextNonEmpty);
						parallelSheetStates[i].readRowsMultiDynamic(primarySheetSynchKey);
						break;
						
					default:
					}
				}
			
			//TODO if needed check the first non empty row
		}
		
		default:
			break;
		}
	}
		
	protected int iterateExcel()
	{	
		switch (config.substanceIteration)
		{
		case ROW_SINGLE:
			if (config.Fl_SkipEmptyRows)
			{	
				int res = iterateToNextNonEmptyRow();
				if (parallelSheetStates != null)
					for (int i = 0; i < parallelSheetStates.length; i++)
					{
						parallelSheetStates[i].iterateToNextNonEmptyRow();
					}	
				return res;
			}
			else
			{	
				curRowNum++;
				curRow = primarySheet.getRow(curRowNum);
				if (curRow == null)
				{	
					parseErrors.add("Row " + curRowNum + " is empty!");
					return -1;
				}
				else
					return curRowNum;
				
				//TODO iterate parallel sheets
			}
			
			
		case ROW_MULTI_FIXED:
			for (int i = 0; i < config.rowMultiFixedSize; i++)
			{
				curRowNum++;
				//TODO
			}
			break;
			
		case ROW_MULTI_DYNAMIC:
			iterateRowMultiDynamic();
			if (parallelSheetStates != null)
				for (int i = 0; i < parallelSheetStates.length; i++)
					parallelSheetStates[i].iterateRowMultiDynamic(primarySheetSynchKey);				
			break;	
				
		default : 
			curRowNum++;
		}
		
		return 0;
	}
	
	protected int iterateToNextNonEmptyRow()
	{	
		curRowNum++;
		while (curRowNum <= primarySheet.getLastRowNum())
		{
			curRow = primarySheet.getRow(curRowNum);
			if (!isEmpty(curRow))
			{
				return curRowNum;
			}	
			curRowNum++;
		}
		
		return -1;
	}
	
	
	protected void iterateRowMultiDynamic()
	{	
		curRowNum = curReadRowNum;
		readRowsMultiDynamic();
	}
	
	protected void readRowsMultiDynamic()
	{
		logger.info("----- Primary Sheet - Reading at row: " + (curRowNum+1));
		
		switch (config.dynamicIteration)
		{
		case NEXT_NOT_EMPTY:
		{
			if (curRowNum <= primarySheet.getLastRowNum())
				curRows = new ArrayList<Row>();
			else
			{
				curRows = null;
				primarySheetSynchKey = null;
				logger.info("----- read no rows ");
				return;
			}
			
			//The first row is already checked to be non empty 
			curRow = primarySheet.getRow(curRowNum);
			Cell c0 = curRow.getCell(config.dynamicIterationColumnIndex);
			primarySheetSynchKey = ExcelUtils.getStringFromCell(c0);
			logger.info("synch key: " + primarySheetSynchKey);
			key00 = primarySheetSynchKey;
			
			curReadRowNum = curRowNum;  //curRowNum is not changed here. It is updated by the iteration functions
			Row r = curRow;
			curRows.add(r);
			curReadRowNum++;
						
			while (curReadRowNum <= primarySheet.getLastRowNum())
			{
				r = primarySheet.getRow(curReadRowNum);
				if (isEmpty(r))
				{	
					//Empty row is skipped
					curReadRowNum++;
					continue;
				}
				else
				{
					Cell c = r.getCell(config.dynamicIterationColumnIndex);
					if (ExcelUtils.isEmpty(c))
					{
						curRows.add(r);
						curReadRowNum++;
					}
					else
					{
						logger.info("****  read " + curRows.size() + " rows /  next key: " + c.toString());
						return; //Reached next record
					}
				}				
			} //end of while
			
			logger.info(" read " + curRows.size() + " rows");
		}

		case NEXT_DIFFERENT_VALUE:
		{
			//TODO
		}
		break;

		default:
			break;
		}
	}
	
	
	protected boolean isEmpty (Row row)
	{
		if (row == null)
			return true;
		else
		{	
			if (config.Fl_FullCheckForEmptyColumnsAndRows)
			{	
				for (int i = 0; i <= row.getLastCellNum(); i++)
				{
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
	protected boolean isEmpty (Column  col)
	{
		if (col == null)
			return true;
		else
		{
			//TODO - to check whether this Column is really empty 
			return false;
		}
	}
	*/
	
	
	
	//This function uses a generic approach (the generic variants of the helper functions)
	//The iteration access mode is handled in the specific overloads of the functions.
	protected SubstanceRecord getBasicSubstanceRecord()
	{
		if (config.substanceIteration == IterationAccess.ROW_SINGLE) 
			logger.info("Reading row: " + (curRowNum+1));
				
		SubstanceRecord r = new SubstanceRecord ();
		
		//Typically companyUUID is not set from the excel file but it is possible if needed.
		ExcelDataLocation loc = config.substanceLocations.get("SubstanceRecord.companyUUID");
		if (loc != null)
		{	
			String s = getStringValue(loc);
			if (s != null)
				r.setCompanyUUID("XLSX-"+UUID.nameUUIDFromBytes(s.getBytes()).toString());
		}
		
		loc = config.substanceLocations.get("SubstanceRecord.companyName");
		if (loc != null)
		{	
			String s = getStringValue(loc);
			if (s != null)
				r.setCompanyName(s);
		}
		
		//Typically ownerUUID is not set from the excel file but it is possible if needed.
		loc = config.substanceLocations.get("SubstanceRecord.ownerUUID");  
		if (loc != null)
		{	
			String s = getStringValue(loc);
			if (s != null)
				r.setOwnerUUID(s);
		}
		
		loc = config.substanceLocations.get("SubstanceRecord.ownerName");
		if (loc != null)
		{	
			String s = getStringValue(loc);
			if (s != null)
				r.setOwnerName(s);
		}
		
		loc = config.substanceLocations.get("SubstanceRecord.substanceType");
		if (loc != null)
		{	
			String s = getStringValue(loc);
			if (s != null)
				r.setSubstancetype(s);
		}
		
		loc = config.substanceLocations.get("SubstanceRecord.publicName");
		if (loc != null)
		{	
			String s = getStringValue(loc);
			if (s != null)
				r.setPublicName(s);
		}
		
		loc = config.substanceLocations.get("SubstanceRecord.idSubstance");
		if (loc != null)
		{	
			Double v = getNumericValue(loc);
			if (v != null)
				r.setIdsubstance(v.intValue());
		}
		
		List<ProtocolApplication> measurements = readProtocolApplications();
		r.setMeasurements(measurements);
				
		putSRInfoToProtocolApplications(r);
		
		for (CompositionDataLocation cdl : config.composition)
		{	
			CompositionRelation relation = readCompositionRelation(cdl, r);
			r.addStructureRelation(relation);
		}
		
		return r;
	}
	
	protected void loadSubstanceRecords()
	{
		loadedRecordsBuffer.clear();
		
		if (config.basicIterationLoadSubstanceRecord)
		{	
			basicSubstanceRecord = getBasicSubstanceRecord();
			loadedRecordsBuffer.add(basicSubstanceRecord);
		}
		else
		{	
			basicSubstanceRecord = null;
		}	
		
		if (!config.FlagDynamicSpan)
			return;
		
		//Handle dynamic span
		ArrayList<DynamicIterationObject> dios = new ArrayList<DynamicIterationObject>();
		
		if (config.dynamicIterationSpan != null)
		{
			switch (config.substanceIteration)
			{
			case ROW_MULTI_FIXED:
			case ROW_MULTI_DYNAMIC:
				DynamicIterationObject dio = config.dynamicIterationSpan.getDynamicIterationObjectFromRows(curRows);
				dios.add(dio);
				break;
			}
		}
		
		if (parallelSheetStates != null)
			for (int i = 0; i < parallelSheetStates.length; i++)
			{
				switch (config.parallelSheets.get(i).iteration)
				{
				case ROW_MULTI_FIXED:
				case ROW_MULTI_DYNAMIC:
					DynamicIterationObject dio = 
						config.parallelSheets.get(i).dynamicIterationSpan.getDynamicIterationObjectFromRows(parallelSheetStates[i].curRows);
					dios.add(dio);
					break;
				}
			}
		
		ArrayList<SubstanceRecord> records = DynamicIterationObject.synchronize(dios, basicSubstanceRecord);
		loadedRecordsBuffer.addAll(records);
	}
	
		
	protected void putSRInfoToProtocolApplications(SubstanceRecord record)
	{
		for (ProtocolApplication pa : record.getMeasurements())
		{
			//pa.setCompanyName(record.getCompanyName());
			pa.setSubstanceUUID(record.getCompanyUUID());
			pa.setCompanyName(record.getOwnerName());
			pa.setCompanyUUID(record.getOwnerUUID());
			//TODO 
		}
	}
	
	
	protected List<ProtocolApplication> readProtocolApplications()
	{
		List<ProtocolApplication> protApps = new ArrayList<ProtocolApplication>();
		for (ProtocolApplicationDataLocation padl : config.protocolAppLocations)
		{	
			ProtocolApplication pa = readProtocolApplication(padl);
			protApps.add(pa);
		}
		return protApps;
	}
	
	protected ProtocolApplication readProtocolApplication(ProtocolApplicationDataLocation padl)
	{
		Protocol protocol = readProtocol(padl);
		ProtocolApplication pa = new ProtocolApplication(protocol);
		
		if (padl.citationTitle != null)
		{	
			String s = getStringValue(padl.citationTitle);
			if (s != null)
				pa.setReference(s);  //title is the reference 'itself'
		}
		
		if (padl.citationOwner != null)
		{	
			String s = getStringValue(padl.citationOwner);
			if (s != null)
				pa.setReferenceOwner(s);
		}
		
		if (padl.citationYear != null)
		{	
			String s = getStringValue(padl.citationYear);
			if (s != null)
				pa.setReferenceYear(s);
		}
		
		
		if (padl.interpretationCriteria != null)
		{	
			String s = getStringValue(padl.interpretationCriteria);
			if (s != null)
				pa.setInterpretationCriteria(s);
		}
		
		
		if (padl.interpretationResult != null)
		{	
			String s = getStringValue(padl.interpretationResult);
			if (s != null)	
				pa.setInterpretationResult(s);
		}
		
		//Read parameters
		IParams params = new Params();
		for (String param : padl.parameters.keySet())
		{	
			ExcelDataLocation loc = padl.parameters.get(param);
			//Param is allowed to be String or Numeric object
			FlagAddParserStringError = false;
			String paramStringValue = getStringValue(loc);
			FlagAddParserStringError = true;
			if (paramStringValue != null)
				params.put(param, paramStringValue);
			else
			{
				Double paramDoubleValue = getNumericValue(loc);
				if (paramDoubleValue != null)
					params.put(param, paramDoubleValue);
			}
		}
		pa.setParameters(params);
		
		
		//Read effects array
		if (padl.effects != null)
		{
			for (int i = 0; i < padl.effects.size(); i++)
			{	
				EffectRecord effect = readEffect(padl.effects.get(i));
				pa.addEffect(effect);
			}	
		}
		
		return pa;
	}
	
	protected Protocol readProtocol(ProtocolApplicationDataLocation padl)
	{	
		String endpoint = null;
		if (padl.protocolEndpoint != null)
			endpoint = getStringValue(padl.protocolEndpoint);
		
		Protocol protocol = new Protocol(endpoint);
		
		if (padl.protocolTopCategory != null)
		{	
			String s = getStringValue(padl.protocolTopCategory);
			if (s != null)
				protocol.setTopCategory(s);
		}
		
		if (padl.protocolCategoryCode != null)
		{	
			String s = getStringValue(padl.protocolCategoryCode);
			if (s != null)
				protocol.setCategory(s);
		}
		
		/* 
		 * Category title is currently not handled: padl.protocolCategoryTitle 
		 */
		
		if (padl.protocolGuideline != null)
		{	
			List<String> guide = new ArrayList<String>();
			for (int i = 0; i < padl.protocolGuideline.size(); i++)
			{	
				String s = getStringValue(padl.protocolGuideline.get(i));
				if (s != null)
					guide.add(s);
			}	
			protocol.setGuideline(guide);
		}
		
		return protocol;
	}
	
	protected EffectRecord readEffect(EffectRecordDataLocation efrdl)
	{
		EffectRecord effect = new EffectRecord();
		
		if (efrdl.sampleID != null)
		{	
			String s = getStringValue(efrdl.sampleID);
			if (s != null)
				effect.setSampleID(s);
		}
		
		if (efrdl.endpoint != null)
		{	
			String s = getStringValue(efrdl.endpoint);
			if (s != null)
				effect.setEndpoint(s);
		}
		
		if (efrdl.loQualifier != null)
		{	
			String s = getStringValue(efrdl.loQualifier);
			if (s != null)
			{	
				if (ExcelParserConfigurator.isValidQualifier(s))
					effect.setLoQualifier(s);
				else
					parseErrors.add("["+ locationStringForErrorMessage(efrdl.loQualifier, primarySheetNum) +  "] Lo Qualifier \""+ s + "\" is incorrect!");
			}
		}
		
		if (efrdl.loValue != null)
		{	
			Double d = null;
			if (config.Fl_AllowQualifierInValueCell)
			{
				FlagAddParserStringError = false; //Temporary switch off parser errors
				String qstring = getStringValue(efrdl.loValue);
				FlagAddParserStringError = true;
				if (qstring != null)
				{
					RecognitionUtils.QualifierValue qv =  RecognitionUtils.extractQualifierValue(qstring);
					if (qv.value == null)
						parseErrors.add("["+ locationStringForErrorMessage(efrdl.loQualifier, primarySheetNum) +  "] " 
								+ qstring + " Lo Value/Qualifier error: " + qv.errorMsg);
					else
					{
						d = qv.value;
						if (qv.qualifier != null)
							effect.setLoQualifier(qv.qualifier);  //this qualifier takes precedence (if already set)
					}
				}
				else
					d = getNumericValue(efrdl.loValue);
			}
			else
				d = getNumericValue(efrdl.loValue);
			
			if (d!=null)
				effect.setLoValue(d);
		}
		
		
		if (efrdl.upQualifier != null)
		{	
			String s = getStringValue(efrdl.upQualifier);
			if (s != null)
			{	
				if (ExcelParserConfigurator.isValidQualifier(s))
					effect.setUpQualifier(s);
				else
					parseErrors.add("["+ locationStringForErrorMessage(efrdl.upQualifier, primarySheetNum) +  "] Up Qualifier \""+ s + "\" is incorrect!");
			}
		}
		
		if (efrdl.upValue != null)
		{	
			Double d = null;
			if (config.Fl_AllowQualifierInValueCell)
			{
				FlagAddParserStringError = false; //Temporary switch off parser errors
				String qstring = getStringValue(efrdl.upValue);
				FlagAddParserStringError = true;
				if (qstring != null)
				{
					RecognitionUtils.QualifierValue qv =  RecognitionUtils.extractQualifierValue(qstring);
					if (qv.value == null)
						parseErrors.add("["+ locationStringForErrorMessage(efrdl.upQualifier, primarySheetNum) +  "] " 
								+ qstring + " Up Value/Qualifier error: " + qv.errorMsg);
					else
					{
						d = qv.value;
						if (qv.qualifier != null)
							effect.setUpQualifier(qv.qualifier);  //this qualifier takes precedence (if already set)
					}
				}
				else
					d = getNumericValue(efrdl.upValue);
			}
			else
				d = getNumericValue(efrdl.upValue);
			
			if (d!=null)
				effect.setUpValue(d);
		}
		
		if (efrdl.textValue != null)
		{	
			String s = getStringValue(efrdl.textValue);
			if (s != null)
				effect.setTextValue(s);
		}
		
		if (efrdl.errQualifier != null)
		{	
			String s = getStringValue(efrdl.errQualifier);
			if (s != null)
			{
				if (ExcelParserConfigurator.isValidQualifier(s))
					effect.setErrQualifier(s);
				else
					parseErrors.add("["+ locationStringForErrorMessage(efrdl.errQualifier, primarySheetNum) +  "] Err Qualifier \""+ s + "\" is incorrect!");
			}
		}
		
		if (efrdl.errValue != null)
		{	
			Double d = null;
			if (config.Fl_AllowQualifierInValueCell)
			{
				FlagAddParserStringError = false; //Temporary switch off parser errors
				String qstring = getStringValue(efrdl.errValue);
				FlagAddParserStringError = true;
				if (qstring != null)
				{
					RecognitionUtils.QualifierValue qv =  RecognitionUtils.extractQualifierValue(qstring);
					if (qv.value == null)
						parseErrors.add("["+ locationStringForErrorMessage(efrdl.errQualifier, primarySheetNum) +  "] " 
								+ qstring + " Err Value/Qualifier error: " + qv.errorMsg);
					else
					{
						d = qv.value;
						if (qv.qualifier != null)
							effect.setErrQualifier(qv.qualifier);  //this qualifier takes precedence (if already set)
					}
				}
				else
					d = getNumericValue(efrdl.errValue);
			}
			else
				d = getNumericValue(efrdl.errValue);
			
			if (d!=null)
				effect.setErrorValue(d);
		}
		
		if (efrdl.unit != null)
		{	
			String s = getStringValue(efrdl.unit);
			if (s != null)
				effect.setUnit(s);  
		}
		
		if (efrdl.value != null)
		{	
			//If present this takes precedence over the up/lo values and qualifiers and unit
			//These are intelligently recognized
			
			Double d = null;
			FlagAddParserStringError = false; //Temporary switch off parser errors
			String richValueString = getStringValue(efrdl.value);
			FlagAddParserStringError = true;
			if (richValueString != null)
			{
				RichValue rv = rvParser.parse(richValueString);
				String rv_error = rvParser.getAllErrorsAsString(); 
				
				if (rv_error == null)
				{
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
				}
				else
				{	
					parseErrors.add("["+ locationStringForErrorMessage(efrdl.value, primarySheetNum) +  "] " 
							+ richValueString + " Value error: " + rv_error);
				}
			}
			else
				d = getNumericValue(efrdl.value);
			
			if (d!=null)
				effect.setLoValue(d); //This is the default behavior if the cell is of type numeric
		}
		
		
		
		if (efrdl.conditions != null)
		{	
			IParams params = new Params();
			
			Set<Entry<String, ExcelDataLocation>> locEntries = efrdl.conditions.entrySet();
			for (Entry<String, ExcelDataLocation> entry : locEntries )
			{	
				//String value = getStringValue(entry.getValue());
				//params.put(entry.getKey(), value);
				
				ExcelDataLocation loc = entry.getValue();
				FlagAddParserStringError = false;
				String condStrValue = getStringValue(loc);
				FlagAddParserStringError = true;
				if (condStrValue != null)
					params.put(entry.getKey(), condStrValue);
				else
				{
					Double condDoubleValue = getNumericValue(loc);
					if (condDoubleValue != null)
						params.put(entry.getKey(), condDoubleValue);
				}
			}
			
			effect.setConditions(params);
		}
		
		return effect;
	}
	
	protected CompositionRelation readCompositionRelation(CompositionDataLocation cdl, SubstanceRecord record)
	{	
		IStructureRecord structure = new StructureRecord();
		Proportion proportion = new Proportion();
		
		if (cdl.content != null)
		{
			String s = getStringValue(cdl.content);
			structure.setContent(s);
		}
		
		if (cdl.format != null)
		{
			String s = getStringValue(cdl.format);
			structure.setFormat(s);
		}
		
		if (cdl.formula != null)
		{
			String s = getStringValue(cdl.formula);
			structure.setFormula(s);
		}
		
		if (cdl.smiles != null)
		{
			String s = getStringValue(cdl.smiles);
			structure.setSmiles(s);
		}
		
		if (cdl.inchi != null)
		{
			String s = getStringValue(cdl.inchi);
			structure.setInchi(s);
		}
		
		if (cdl.inchiKey != null)
		{
			String s = getStringValue(cdl.inchiKey);
			structure.setInchiKey(s);
		}
		
		
		CompositionRelation relation = new CompositionRelation(record, structure, cdl.structureRelation, proportion);
		return relation;
	}
	
	/*
	 * Generic function (regardless of the iteration access)
	 */
	protected String getStringValue(ExcelDataLocation loc)
	{
		switch (loc.iteration)
		{
		case ROW_SINGLE:
			if (loc.isFromParallelSheet())
			{	
				Row row = parallelSheetStates[loc.getParallelSheetIndex()].curRow;
				if (row != null)
					return getStringValue(row, loc);
				else
					return null;
			}	
			else
				return getStringValue(curRow, loc);  //from basic sheet
			
		case ROW_MULTI_FIXED:
		case ROW_MULTI_DYNAMIC:
			//Taking the value from the first row
			ArrayList<Row> rows;
			if (loc.isFromParallelSheet())
				rows = parallelSheetStates[loc.getParallelSheetIndex()].curRows;
			else	
				rows = curRows;
			if (rows != null)
				if (!rows.isEmpty())
				return getStringValue(rows.get(0), loc);
			return null;
		
		case ABSOLUTE_LOCATION: 
		{	
			Object value = loc.getAbsoluteLocationValue();
			if (value == null)
			{	
				value = getStringFromAbsoluteLocation (loc);
				loc.setAbsoluteLocationValue(value);
			}
			if (value != null)
				return (String) value;
			return null;
		}
		
		case JSON_VALUE:
		{	
			Object value = loc.getJsonValue();
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else
					if (FlagAddParserStringError)
						parseErrors.add("["+ locationStringForErrorMessage(loc) +  "] JSON_VALUE is not of type STRING!");
			return null;
		}
		
		case JSON_REPOSITORY:
		{	
			String key = loc.getJsonRepositoryKey();
			Object value = config.jsonRepository.get(key);
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else
					if (FlagAddParserStringError)
						parseErrors.add("["+ locationStringForErrorMessage(loc) +  "] JSON_REPOSITORY value for key \"" + key + "\" is not of type STRING!");
			return null;
		}
		
		case VARIABLE:
		{
			String key = loc.getVariableKey();
			Object value = curVariables.get(key);
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else
					if (FlagAddParserStringError)
						parseErrors.add("["+ locationStringForErrorMessage(loc) +  "] JSON_REPOSITORY value for key \"" + key + "\" is not of type STRING!");
			return null;
		}
		
		default : 
			return null;
		}
	}
	
	protected Double getNumericValue(ExcelDataLocation loc)
	{
		switch (loc.iteration)
		{
		case ROW_SINGLE:
			if (loc.isFromParallelSheet())
			{	
				Row row = parallelSheetStates[loc.getParallelSheetIndex()].curRow;
				if (row != null)
					return getNumericValue(row, loc);
				else
					return null;
			}	
			else
				return getNumericValue(curRow, loc);
			
			
		case ROW_MULTI_FIXED:	//Both treated the same way
		case ROW_MULTI_DYNAMIC:  
			//Taking the value from the first row
			ArrayList<Row> rows;
			if (loc.isFromParallelSheet())
				rows = parallelSheetStates[loc.getParallelSheetIndex()].curRows;
			else	
				rows = curRows;
			if (rows != null)
				if (!rows.isEmpty())
				return getNumericValue(rows.get(0), loc);
			return null;
			
		case ABSOLUTE_LOCATION: 
		{	
			Object value = loc.getAbsoluteLocationValue();
			if (value == null)
			{	
				value = getNumericFromAbsoluteLocation (loc);
				loc.setAbsoluteLocationValue(value);
			}
			if (value != null)
				return (Double) value;
			return null;
		}
		
		case JSON_VALUE:
		{	
			Object value = loc.getJsonValue();
			if (value != null)
				if (value instanceof Double)
					return (Double) value;
				else
					if (value instanceof Integer)
						return new Double((Integer)value); 
					else 	
						parseErrors.add("["+ locationStringForErrorMessage(loc) +  "] JSON_VALUE is not of type NUMERIC!");
			return null;
		}
		
		case JSON_REPOSITORY:
		{	
			String key = loc.getJsonRepositoryKey();
			Object value = config.jsonRepository.get(key);
			if (value != null)
				if (value instanceof Double)
					return (Double) value;
				else
					if (value instanceof Integer)
						return new Double((Integer)value); 
					else 	
						parseErrors.add("["+ locationStringForErrorMessage(loc) +  "] JSON_REPOSITORY value for key \"" + key + "\" is not of type NUMERIC!");
			return null;
		}
		
		case VARIABLE:
		{
			String key = loc.getVariableKey();
			Object value = curVariables.get(key);
			if (value != null)
				if (value instanceof Double)
					return (Double) value;
				else
					if (value instanceof Integer)
						return new Double((Integer)value); 
					else 	
						parseErrors.add("["+ locationStringForErrorMessage(loc) +  "] JSON_REPOSITORY value for key \"" + key + "\" is not of type NUMERIC!");
			return null;
		}	
			
		default : 
			return null;
		}
	}
	
	
	protected String getStringFromAbsoluteLocation(ExcelDataLocation loc)
	{	
		Sheet sheet = workbook.getSheetAt(loc.sheetIndex);
		if (sheet != null)
		{	
			Row r = sheet.getRow(loc.rowIndex);
			if (r== null)
				return null;
			
			Cell c = r.getCell(loc.columnIndex);
			if (c!=null)
			{
				if (c.getCellType() != Cell.CELL_TYPE_STRING)
				{
					if (FlagAddParserStringError)
						parseErrors.add("["+locationStringForErrorMessage(loc) + "]: Cell is not of type STRING!"); 
					return null;
				}
				return c.getStringCellValue();
			}
		}			
		return null;
	}
	
	protected Double getNumericFromAbsoluteLocation(ExcelDataLocation loc)
	{
		Sheet sheet = workbook.getSheetAt(loc.sheetIndex);
		if (sheet != null)
		{	
			Row r = sheet.getRow(loc.rowIndex);
			if (r== null)
				return null;
			
			Cell c = r.getCell(loc.columnIndex);
			if (c!=null)
			{
				if (c.getCellType() != Cell.CELL_TYPE_NUMERIC)
				{	
					parseErrors.add("["+locationStringForErrorMessage(loc) + "]: Cell is not of type NUMERIC!"); 
					return null;
				}
				return c.getNumericCellValue();
			}
		}			
		return null;
	}
	
	protected String getStringValue(Row row, ExcelDataLocation loc)
	{
		Cell c = row.getCell(loc.columnIndex);
		
		if (c == null)
		{	
			if (loc.allowEmpty)
			{
				return "";
			}
			else
			{
				if (FlagAddParserStringError)
					parseErrors.add("JSON Section " + loc.sectionName + ", sheet " + (primarySheetNum + 1) + 
						", row " + (row.getRowNum() + 1) + " cell " + (loc.columnIndex + 1) + " is empty!"); 
				return null;
			}
			
		}	
		
		if (c.getCellType() != Cell.CELL_TYPE_STRING)
		{
			if (FlagAddParserStringError)
				parseErrors.add("JSON Section " + loc.sectionName + ", sheet " + (primarySheetNum + 1) + 
					", row " + (row.getRowNum() + 1) + " cell " + (loc.columnIndex + 1) + " is not of type STRING!"); 
			return null;
		}
		
		return c.getStringCellValue();
	}
	
	
	
	protected Double getNumericValue(Row row, ExcelDataLocation loc)
	{
		Cell c = row.getCell(loc.columnIndex);
		
		if (c == null)
		{	
			if (loc.allowEmpty)
			{
				return 0.0;
			}
			else
			{
				parseErrors.add("JSON Section " + loc.sectionName + ", sheet " + (primarySheetNum + 1) + 
						", row " + (row.getRowNum() + 1) + " cell " + (loc.columnIndex + 1) + " is empty!"); 
				return null;
			}
		}
		
		if (c.getCellType() != Cell.CELL_TYPE_NUMERIC)
		{
			parseErrors.add("JSON Section " + loc.sectionName + ", sheet " + (primarySheetNum + 1) + 
					", row " + (row.getRowNum() + 1) + " cell " + (loc.columnIndex + 1) + " is not of type NUMERIC!"); 
			return null;
		}
		
		return c.getNumericCellValue();
	}
	
	
	private String locationStringForErrorMessage(ExcelDataLocation loc)
	{
		//TODO
		return "";
	}
	
	
	
	private String locationStringForErrorMessage(ExcelDataLocation loc, int sheet)
	{
		//TODO 
		return "";
	}
	
	public boolean hasErrors()
	{
		return (!parseErrors.isEmpty());
	}
	
	public String errorsToString()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < parseErrors.size(); i++)
			sb.append("Error #" + (i+1) + "\n" + parseErrors.get(i) + "\n");
		return sb.toString();
	}
	
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
	
	/**
	 * Planned Implementation Tasks:
	 * 
	 * - Recognition modes: BY_NAME, BY_INDEX_AND_NAME 
	 * 
	 * - in ExcelDataLocation class separate 'recognitions' for the Sheet, Row and Column
	 * 
	 * - Iteration modes: ROW_MULTI_FIXED, ROW_MULTI_DYNAMIC, COLUMN_* ...
	 * 
	 * - List of all allowed JSON keywords + optional checking
	 * 
	 * - Reading on particular sheet + linked parallel reading on several sheets (e.g. ModNanoTox should need this
	 * 		(eventually more work variables of the kind: curSheet... would be needed) + synchronization ...		
	 * 
	 * - Definition of an 'END of reading" region i.e. after that point the excel data is not considered.
	 *   This idea can be further developed to a substance record filtration utility - may be a special class for filtration... 
	 *  
	 * - dynamic (automatic) recognition of SubtsanceRecord elements from Excel: protocol applications, effects, endpoints, conditions
	 *   keyword suggestion DYNAMIC_SPAN
	 *  
	 * - handle ParserConfiguration flags: by JSON + Java Method
	 * 
	 * - add to ExcelDataLocation keywords for UUID generation flag + possible processing of data from that location
	 * 
	 * - Read composition !!
	 * 
	 * - conditions to be read as RichValue and stored as Value object 
	 * 
	 * - String_or_Double Excel/Json utils 
	 * 
	 * - Check the consistency of the ExcelDataLocation (loc variables) and the global data access/parallel sheet access, ... 
	 * 
	 * - dynamic iteration in mode NEXT_DIFFERENT_VALUE (both for primary and parallel sheets)
	 * 
	 * - Move some of the "extraction" functions from class ExcelParserConfiguration to the corresponding *DataLocation class 
	 * - analogously: Move handling of parallel sheets to the corresponding classes +  move some functionality as static to ExcelParserUtils
	 * 
	 * - add function getString(ExcelDataLocation) and replace most of the function calls of getStringValue() so that numeric cell would not give error
	 * 
	 */
	
}
