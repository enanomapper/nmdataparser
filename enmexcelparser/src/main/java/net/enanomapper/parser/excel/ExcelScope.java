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
				String tok0 = rangeTokens[i].trim();
				int pos = tok0.indexOf("!");				
				if (pos == -1) {
					CellRangeAddress cra = CellRangeAddress.valueOf(tok0);
					scope.cellRanges.add(cra);
					//default sheet 0 (first sheet)
					scope.sheetNums.add(0); 
					scope.sheetNames.add(null);
				}
				else {
					//Handle sheet number/name
					String sheetTok = tok0.substring(0,pos).trim();
					if (sheetTok.isEmpty())
						errors.add("In cell range #" + (i+1) + " : missing/incorrect sheet info before '!'");
					else {
						Integer shNum = null;
						try {
							shNum = Integer.parseInt(sheetTok);
						}
						catch (Exception e) {}
						
						scope.sheetNums.add(shNum);
						if (shNum == null)  
							scope.sheetNames.add(sheetTok);
						else
							scope.sheetNames.add(null);
					}
					
					//Handle address
					String tok = tok0.substring(pos+1).trim();
					if (tok.isEmpty())
						errors.add("In cell range #" + (i+1) + " : missing/incorrect  cell range after '!'");
					else {
						CellRangeAddress cra = CellRangeAddress.valueOf(tok0);
						scope.cellRanges.add(cra);
					}					
				}
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
