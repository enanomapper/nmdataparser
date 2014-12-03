package net.enanomapper.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

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
import ambit2.core.io.IRawReader;


/**
 * 
 * @author nick
 *
 */
public class GenericExcelParser implements IRawReader<SubstanceRecord>
{
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
	
	public void init() throws Exception
	{
		curSheet = workbook.getSheetAt(curSheetNum);
		curRowNum = config.startRow;
		LOGGER.info("workSheet = " + curSheetNum +  "   starRow = " + curRowNum);
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
	public void close() throws IOException {
		input.close();
		input = null;
		workbook = null;
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
	public boolean hasNext() {
		
		if (FlagNextRecordLoaded)  //Next record is already read and loaded to the buffer
			return true;
		
		if (hasExcelDataForNextRecord())
		{
			//This is the actual reading of next substance record
			if (iterateExcel() >= 0)
			{	
				nextRecordBuffer = getSubstanceRecord();
				if (nextRecordBuffer == null)
					nextRecordBuffer = new SubstanceRecord();
					
				FlagNextRecordLoaded = true;
				return true;					
			}
			else
			{	
				//Empty record is returned
				FlagNextRecordLoaded = true;
				nextRecordBuffer = new SubstanceRecord();
				return true;
			}	
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
	public void remove() {
		// TODO Auto-generated method stub
	}


	@Override
	public SubstanceRecord nextRecord()
	{
		if (hasNext())
		{	
			SubstanceRecord result = nextRecordBuffer;
			//Invalidate (empty) the buffer the next record 
			nextRecordBuffer = null;
			FlagNextRecordLoaded = false;
			return result;
		}
		else
			return null;
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
				return curRowNum;
			curRowNum++;
		}
		
		return -1;
	}
	
	protected SubstanceRecord getSubstanceRecord()
	{
		switch (config.substanceIteration)
		{
		case ROW_SINGLE:			
			return readSR(curRow);
			
		case ROW_MULTI_FIXED:			
			return readSR(curRows);
			
		case ROW_MULTI_DYNAMIC:
			return readSR(curRows);
				
		default : 
			return null;
		}
	}
	
	protected SubstanceRecord readSR(Row row)
	{
		LOGGER.info("Reading row: " + (curRowNum+1));
		//LOGGER.info(row.toString());
		
		SubstanceRecord r = new SubstanceRecord ();
		
		ExcelDataLocation loc = config.locations.get("SubstanceRecord.companyName");
		if (loc != null)
		{	
			String s = getStringValue(row, loc);
			r.setCompanyName(s);
		}
		
		loc = config.locations.get("SubstanceRecord.ownerName");
		if (loc != null)
		{	
			String s = getStringValue(row, loc);
			r.setOwnerName(s);
		}
		
		loc = config.locations.get("SubstanceRecord.substanceType");
		if (loc != null)
		{	
			String s = getStringValue(row, loc);
			r.setSubstancetype(s);
		}
		
		
		//TODO
		
		//TODO handle errors
		return r;
	}
	
	protected SubstanceRecord readSR(ArrayList<Row> rows)
	{
		SubstanceRecord r = new SubstanceRecord ();
		//TODO
		return r;
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
	
}
