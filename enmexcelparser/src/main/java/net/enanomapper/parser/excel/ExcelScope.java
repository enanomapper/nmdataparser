package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.util.CellRangeAddress;


public class ExcelScope 
{
	public static enum ScopeType {
		ROW, COLUMN, ROW_SET, COLUM_SET, CELL, CELL_SET, BLOCK, UNDEFINED		
	}
	
	public ScopeType type = ScopeType.UNDEFINED;
	public List<CellRangeAddress> cellRanges = new ArrayList<CellRangeAddress>();
	public List<Integer> sheetNums = new ArrayList<Integer>();
	public List<String> sheetNames = new ArrayList<String>();
		
	
	public static ExcelScope parseFromString(String scopeStr) throws Exception 
	{
		List<String> errors = new ArrayList<String>();
		ExcelScope scope = new ExcelScope();
		String rangeTokens[] = scopeStr.split(",");
		
		for (int i = 0; i < rangeTokens.length; i++) 
		{
			try{
				CellRangeAddress cra = CellRangeAddress.valueOf(rangeTokens[i].trim());
				scope.cellRanges.add(cra);
			}
			catch (Exception x) {
				errors.add("In cell range #" + (i+1) + " : " + x.getMessage());
			}
		}
		
		if (!errors.isEmpty()) {
			StringBuffer errBuffer = new StringBuffer();
			for (String err: errors)
				errBuffer.append(err + "\n");
			throw new Exception (errBuffer.toString());
		}	
		
		return scope;
	}	
	
	
	public void recognizeScopeType() {
		//TODO
	}
}
