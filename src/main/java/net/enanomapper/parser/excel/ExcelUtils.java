package net.enanomapper.parser.excel;

import java.util.HashMap;

import org.apache.poi.ss.usermodel.Sheet;

public class ExcelUtils 
{
	public static HashMap<String, Integer> getRowGroups(Sheet sheet, int startRowIndex, int endRowIndex, int keyColumnIndex)
	{	
		return getRowGroups(sheet, startRowIndex, endRowIndex, keyColumnIndex, true);
	}
	
	
	public static HashMap<String, Integer> getRowGroups(Sheet sheet, int startRowIndex, int endRowIndex, int keyColumnIndex, 
				boolean recognizeGroupByNextNonEmpty)
	{
		HashMap<String, Integer> groups = new HashMap<String, Integer>();
		
		if (recognizeGroupByNextNonEmpty)
		{
			//TODO
		}
		else
		{
			//TODO
		}
		
		return groups;
	}
	
	
}
