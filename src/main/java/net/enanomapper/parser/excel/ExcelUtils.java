package net.enanomapper.parser.excel;

import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Sheet;

public class ExcelUtils 
{
	public static TreeMap<Integer, String> getRowGroups(Sheet sheet, int startRowIndex, int endRowIndex, int keyColumnIndex)
	{	
		return getRowGroups(sheet, startRowIndex, endRowIndex, keyColumnIndex, true);
	}
	
	
	public static TreeMap<Integer, String> getRowGroups(Sheet sheet, int startRowIndex, int endRowIndex, int keyColumnIndex, 
				boolean recognizeGroupByNextNonEmpty)
	{
		TreeMap<Integer, String> groups = new TreeMap<Integer, String>();
		
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
