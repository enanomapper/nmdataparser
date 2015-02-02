package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import net.enanomapper.parser.ParserConstants.SheetSynchronization;
import net.enanomapper.parser.excel.ExcelUtils;
import net.enanomapper.parser.excel.ExcelUtils.IndexInterval;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ParallelSheetState 
{	
	public int sheetNum = 0;
	public Sheet sheet = null;
	public int curRowNum = 1;
	public int curCellNum = 1;	
	public Row curRow = null;
	public ArrayList<Row> curRows = null;
	public Iterator<Row> rowIt = null; 
	public Cell curCell = null;
	SheetSynchronization synchronization = SheetSynchronization.NONE; 
	public TreeMap<Integer, String> rowGroups = null;
	public HashMap<String, IndexInterval> groupRows = null;
	
	
	public int initialIterateToNextNonEmptyRow()
	{	
		while (curRowNum <= sheet.getLastRowNum())
		{
			curRow = sheet.getRow(curRowNum);
			if (ExcelUtils.isEmpty(curRow))
			{	
				curRowNum++;
			}
			else
				return curRowNum;
		}
		
		curRow = null;  //This is for the cases when the parallel sheet has less rows than the primary sheet
		return -1;
	}
	
	
	public int iterateToNextNonEmptyRow()
	{	
		curRowNum++;
		while (curRowNum <= sheet.getLastRowNum())
		{
			curRow = sheet.getRow(curRowNum);
			if (ExcelUtils.isEmpty(curRow))
			{	
				curRowNum++;
			}
			else
				return curRowNum;
		}
		
		curRow = null;  //This is for the cases when the parallel sheet has less rows than the primary sheet
		return -1;
	}
	
	public int iterateRowMultiDynamic()
	{
		//TODO
		return 0;
	}
	
	public int setRowGroups(int keyColumnIndex, boolean recognizeGroupByNextNonEmpty)
	{
		rowGroups = ExcelUtils.getRowGroups(sheet, curRowNum, keyColumnIndex, recognizeGroupByNextNonEmpty);
		groupRows = ExcelUtils.getGroupIndexIntervals(rowGroups, sheet.getLastRowNum());
		return 0;
	}

}
