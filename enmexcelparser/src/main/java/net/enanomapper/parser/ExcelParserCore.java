package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Core functionality for extracting data
 * from excel file
 * 
 * @author nick
 *
 */

public class ExcelParserCore 
{
	public final static Logger logger = Logger.getLogger(ExcelParserCore.class.getName());
	
	protected ArrayList<String> parallelSheetsErrors = new ArrayList<String>();
	protected ExcelParserConfigurator config = null;
	protected Workbook workbook;
	
	protected int primarySheetNum = 0;
	
	protected Row curRow = null;
	protected ArrayList<Row> curRows = null;
	
	//All variables read from the primary sheet and all parallel sheets
	protected HashMap<String, Object> curVariables = new HashMap<String, Object>();
	protected HashMap<String, HashMap<Object, Object>> curVariableMappings = new HashMap<String, HashMap<Object, Object>>();


}
