package net.enanomapper.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import net.enanomapper.parser.ExcelDataLocation.IterationAccess;
import net.enanomapper.parser.ExcelDataLocation.Recognition;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.core.io.IRawReader;


/**
 * 
 * @author nick
 *
 */
public class GenericExcelParser implements IRawReader<SubstanceRecord>
{	
	public boolean FlagAllowQualifierInValueCell = true;
	public boolean FlagCellValueQualifierTakesPrecedence = true;  
	
	private final static Logger LOGGER = Logger.getLogger(GenericExcelParser.class.getName());
	
	protected ArrayList<String> parseErrors = new ArrayList<String> ();
	protected ExcelParserConfigurator config = null;
	protected  InputStream input;
	
	protected Workbook workbook;
	protected boolean xlsxFormat = false;
	
	//Helper variables for excel file iteration
	protected int curSheetNum = 0;
	protected int curRowNum = 1;
	protected int curCellNum = 1;	
	protected Sheet curSheet = null;
	protected Row curRow = null;
	protected ArrayList<Row> curRows = null;
	protected Iterator<Row> rowIt = null; 
	protected Cell curCell = null;
	 
	private boolean FlagNextRecordLoaded = false; //This flag is true when next object is iterated and successfully read to the buffer; 
	private SubstanceRecord nextRecordBuffer = null;
	
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
		
		curSheet = workbook.getSheetAt(curSheetNum);
		curRowNum = config.startRow;
		initialIteration();
		FlagNextRecordLoaded = false;
		nextRecordBuffer = null;
		
		LOGGER.info("workSheet# = " + (curSheetNum + 1) + "   starRow# = " + (curRowNum + 1));
		LOGGER.info("Last row# = " + (curSheet.getLastRowNum() + 1));
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
		
		if (hasExcelDataForNextRecord())
		{
			//This is the actual reading of next substance record
			nextRecordBuffer = getSubstanceRecord();
			if (nextRecordBuffer == null)
				nextRecordBuffer = new SubstanceRecord();
				
			FlagNextRecordLoaded = true;
			iterateExcel();
			return true;
		}
		else
			return false;
	}
	
	
	private boolean hasExcelDataForNextRecord()
	{
		switch (config.substanceIteration)
		{
		case ROW_SINGLE:
		case ROW_MULTI_DYNAMIC:	
			//Decision logic: at least one row must be left
			if (curRowNum <= curSheet.getLastRowNum())
				return true;
			else
				return false;
			
		case ROW_MULTI_FIXED:
			//Decision logic: at least config.rowMultiFixedSize rows must be left
			if (curRowNum <= curSheet.getLastRowNum() - config.rowMultiFixedSize)
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
	
	protected void initialIteration()
	{
		//TODO - just temporary code
		curRow = curSheet.getRow(curRowNum);
	}
		
	protected int iterateExcel()
	{	
		switch (config.substanceIteration)
		{
		case ROW_SINGLE:
			if (config.FlagSkipEmptyRows)
			{	
				return iterateToNextNonEmptyRow();
			}
			else
			{	
				curRowNum++;
				curRow = curSheet.getRow(curRowNum);
				if (curRow == null)
				{	
					parseErrors.add("Row " + curRowNum + " is empty!");
					return -1;
				}
				else
					return curRowNum;
			}
			
			
		case ROW_MULTI_FIXED:
			for (int i = 0; i < config.rowMultiFixedSize; i++)
			{
				curRowNum++;
				//TODO
			}
			break;
			
		case ROW_MULTI_DYNAMIC:
			//TODO
			curRowNum++;
			break;	
				
		default : 
			curRowNum++;
		}
		
		return 0;
	}
	
	protected int iterateToNextNonEmptyRow()
	{	
		curRowNum++;
		while (curRowNum <= curSheet.getLastRowNum())
		{
			curRow = curSheet.getRow(curRowNum);
			if (curRow != null)
			{	
				//TODO - to check whether this row is really empty 
				//sometimes row looks empty but it is not treated as empty ...
				return curRowNum;
			}	
			curRowNum++;
		}
		
		return -1;
	}
	
	//This function uses a generic approach (the generic variants of the helper functions)
	//The iteration access mode is handles in the specific overloads of the functions.
	protected SubstanceRecord getSubstanceRecord()
	{
		if (config.substanceIteration == IterationAccess.ROW_SINGLE) 
			LOGGER.info("Reading row: " + (curRowNum+1));
		//LOGGER.info(row.toString());
		
		SubstanceRecord r = new SubstanceRecord ();
		
		//Typically companyUUID is not set from the excel file but it is possible if needed.
		ExcelDataLocation loc = config.substanceLocations.get("SubstanceRecord.companyUUID");
		if (loc != null)
		{	
			String s = getStringValue(loc);
			r.setCompanyUUID(s);
		}
		
		loc = config.substanceLocations.get("SubstanceRecord.companyName");
		if (loc != null)
		{	
			String s = getStringValue(loc);
			r.setCompanyName(s);
		}
		
		//Typically ownerUUID is not set from the excel file but it is possible if needed.
		loc = config.substanceLocations.get("SubstanceRecord.ownerUUID");  
		if (loc != null)
		{	
			String s = getStringValue(loc);
			r.setOwnerUUID(s);
		}
		
		loc = config.substanceLocations.get("SubstanceRecord.ownerName");
		if (loc != null)
		{	
			String s = getStringValue(loc);
			r.setOwnerName(s);
		}
		
		loc = config.substanceLocations.get("SubstanceRecord.substanceType");
		if (loc != null)
		{	
			String s = getStringValue(loc);
			r.setSubstancetype(s);
		}
		
		loc = config.substanceLocations.get("SubstanceRecord.publicName");
		if (loc != null)
		{	
			String s = getStringValue(loc);
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
		
		return r;
	}
	
		
	protected void putSRInfoToProtocolApplications(SubstanceRecord record)
	{
		for (ProtocolApplication pa : record.getMeasurements())
		{
			pa.setCompanyName(record.getCompanyName());
			pa.setCompanyUUID(record.getCompanyUUID());
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
			pa.setReference(s);  //title is the reference 'itself'
		}
		
		if (padl.citationOwner != null)
		{	
			String s = getStringValue(padl.citationOwner);
			pa.setReferenceOwner(s);
		}
		
		if (padl.citationYear != null)
		{	
			String s = getStringValue(padl.citationYear);
			pa.setReferenceYear(s);
		}
		
		
		if (padl.interpretationCriteria != null)
		{	
			String s = getStringValue(padl.interpretationCriteria);
			pa.setInterpretationCriteria(s);
		}
		
		
		if (padl.interpretationResult != null)
		{	
			String s = getStringValue(padl.interpretationResult);
			pa.setInterpretationResult(s);
		}
		
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
		String endpoint = "";
		if (padl.protocolEndpoint != null)
			endpoint = getStringValue(padl.protocolEndpoint);
		
		Protocol protocol = new Protocol(endpoint);
		
		if (padl.protocolTopCategory != null)
		{	
			String s = getStringValue(padl.protocolTopCategory);
			protocol.setTopCategory(s);
		}
		
		if (padl.protocolCategoryCode != null)
		{	
			String s = getStringValue(padl.protocolCategoryCode);
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
			effect.setSampleID(s);
		}
		
		if (efrdl.endpoint != null)
		{	
			String s = getStringValue(efrdl.endpoint);
			effect.setEndpoint(s);
		}
		
		if (efrdl.loValue != null)
		{	
			Double d = getNumericValue(efrdl.loValue);
			if (d!=null)
				effect.setLoValue(d);
		}
		
		if (efrdl.upValue != null)
		{	
			Double d = getNumericValue(efrdl.upValue);
			if (d!=null)
				effect.setUpValue(d);
		}
		
		if (efrdl.textValue != null)
		{	
			String s = getStringValue(efrdl.textValue);
			effect.setTextValue(s);
		}
		
		if (efrdl.errValue != null)
		{	
			Double d = getNumericValue(efrdl.errValue);
			if (d!=null)
				effect.setErrorValue(d);
		}
		
		if (efrdl.unit != null)
		{	
			String s = getStringValue(efrdl.unit);
			effect.setUnit(s);  
		}
		
		if (efrdl.conditions != null)
		{	
			IParams params = new Params();
			
			Set<Entry<String, ExcelDataLocation>> locEntries = efrdl.conditions.entrySet();
			for (Entry<String, ExcelDataLocation> entry : locEntries )
			{	
				String value = getStringValue(entry.getValue());
				params.put(entry.getKey(), value);
			}
			
			effect.setConditions(params);
		}
		
		return effect;
	}
	
	/*
	 * Generic function (regardless of the iteration access)
	 */
	protected String getStringValue(ExcelDataLocation loc)
	{
		switch (config.substanceIteration)
		{
		case ROW_SINGLE:			
			return getStringValue(curRow, loc);
			
		case ROW_MULTI_FIXED:
			//TODO
			return null;
			
		case ROW_MULTI_DYNAMIC:
			//TODO
			return null;
				
		default : 
			return null;
		}
	}
	
	/*
	 * Generic function (regardless of the iteration access)
	 */
	protected Double getNumericValue(ExcelDataLocation loc)
	{
		switch (config.substanceIteration)
		{
		case ROW_SINGLE:			
			return getNumericValue(curRow, loc);
			
		case ROW_MULTI_FIXED:
			//TODO
			return null;
			
		case ROW_MULTI_DYNAMIC:
			//TODO
			return null;
				
		default : 
			return null;
		}
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
				parseErrors.add("JSON Section " + loc.sectionName + ", sheet " + (curSheetNum + 1) + 
						", row " + (row.getRowNum() + 1) + " cell " + (loc.columnIndex + 1) + " is empty!"); 
				return null;
			}
			
		}	
		
		if (c.getCellType() != Cell.CELL_TYPE_STRING)
		{
			parseErrors.add("JSON Section " + loc.sectionName + ", sheet " + (curSheetNum + 1) + 
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
				parseErrors.add("JSON Section " + loc.sectionName + ", sheet " + (curSheetNum + 1) + 
						", row " + (row.getRowNum() + 1) + " cell " + (loc.columnIndex + 1) + " is empty!"); 
				return null;
			}
		}
		
		if (c.getCellType() != Cell.CELL_TYPE_NUMERIC)
		{
			parseErrors.add("JSON Section " + loc.sectionName + ", sheet " + (curSheetNum + 1) + 
					", row " + (row.getRowNum() + 1) + " cell " + (loc.columnIndex + 1) + " is not of type NUMERIC!"); 
			return null;
		}
		
		return c.getNumericCellValue();
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
	 * - Add parsing and reading of some other missing fields in the effect record: upValue ... etc.
	 * 
	 * - Define information directly by the JSON config file
	 *      1. By particular "JSON repository"
	 *      2. Directly the the current JSON keyword
	 * 
	 * - List of all allowed JSON keywords + optional checking
	 * 
	 * - Reading on particular sheet + linked parallel reading on several sheets (e.g. ModNanoTox should need this
	 * 		(eventually more work variables of the kind: curSheet... would be needed)		
	 * 
	 * - Definition of an 'END of reading" region i.e. after that point the excel data is not considered 
	 * 
	 * - Eventually the EffectRecord qualifiers to be read (mainly) by the JSON file itself. Also default values to be attached to them 
	 * 
	 * - automatic recognition of the qualifiers: <, >, <=, >=, ca., ...
	 * 
	 * - define column index in EXCEL fashion as well e.g. "B", "C", "AB"
	 * 
	 */
	
}
