package net.enanomapper.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;

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
	
	protected ArrayList<String> parseErrors = new ArrayList<String> ();
	protected ExcelParserConfigurator config = null;
	protected  InputStream input;
	
	protected Workbook workbook;
	protected boolean xlsxFormat = false;
	
	//Helper variables for excel file iteration
	protected int curSheetNum = 0;
	protected int curRowNum = 0;
	protected int curCellNum = 0;	
	protected Sheet curSheet = null;
	protected Row curRow = null;
	protected Cell curCell = null;
		
	
	public GenericExcelParser(InputStream input, String jsonConfig) throws Exception
	{
		this(input, jsonConfig, true);
	}
	
	public GenericExcelParser(InputStream input, String jsonConfig, boolean xlsxFormat) throws Exception
	{
		super();
		this.xlsxFormat = xlsxFormat;
		
		config = ExcelParserConfigurator.loadFromJSON(jsonConfig);
		if (config.configErrors.size() > 0)		
			throw new Exception("GenericExcelParser configuration errors:\n" + config.getAllErrorsAsString());
		
		setReader(input);
		init();
	}
	
	public void init() throws Exception
	{
		//TODO
		
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
		throw new CDKException("Not implemented");
	}


	@Override
	public void setReader(InputStream arg0) throws CDKException {
		try {
			workbook = xlsxFormat?new XSSFWorkbook(input):new HSSFWorkbook(input);
		} catch (Exception x) {
			throw new CDKException(x.getMessage(),x);
		}
	}


	@Override
	public void setReaderMode(Mode arg0) {
		// TODO Auto-generated method stub
		
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
		switch (config.substanceIteration)
		{
		case ROW_SINGLE:
		case ROW_MULTI_FIXED:
		case ROW_MULTI_DYNAMIC:	
			if (curRowNum < curSheet.getLastRowNum())
				return true;
			else
				return false;
			
		}
		return false;
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
	public SubstanceRecord nextRecord() {
		if (!hasNext())// TODO Auto-generated method stub
			return null;
		
		SubstanceRecord r = new SubstanceRecord ();
		//TODO
		return r;
	}
	
	
}
