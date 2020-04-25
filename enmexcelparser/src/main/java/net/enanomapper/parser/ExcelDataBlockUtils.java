package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelDataBlockUtils 
{
	protected Logger logger = null;
	protected Workbook workbook = null;
	protected ArrayList<Row> curRows = null;
	protected int curRowNum = 1;
	protected HashMap<String, Object> curVariables = null;
	protected HashMap<String, HashMap<Object, Object>> curVariableMappings = null;
	
		
	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public Workbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(Workbook workbook) {
		this.workbook = workbook;
	}


	public ArrayList<Row> getCurRows() {
		return curRows;
	}

	public void setCurRows(ArrayList<Row> curRows) {
		this.curRows = curRows;
	}

	public int getCurRowNum() {
		return curRowNum;
	}


	public void setCurRowNum(int curRowNum) {
		this.curRowNum = curRowNum;
	}


	public HashMap<String, Object> getCurVariables() {
		return curVariables;
	}

	public void setCurVariables(HashMap<String, Object> curVariables) {
		this.curVariables = curVariables;
	}

	public HashMap<String, HashMap<Object, Object>> getCurVariableMappings() {
		return curVariableMappings;
	}


	public void setCurVariableMappings(HashMap<String, HashMap<Object, Object>> curVariableMappings) {
		this.curVariableMappings = curVariableMappings;
	}


	
	protected Object getMappingValue(Object originalValue, String mapping) {
		HashMap<Object, Object> map = curVariableMappings.get(mapping);
		if (map == null)
			return null;
		// Original read value is used as a key to obtain the result value;		
		return map.get(originalValue);
	}
		
	
}
