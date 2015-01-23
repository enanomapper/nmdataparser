package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.Iterator;

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
	
	
	public int iterateToNextNonEmptyRow()
	{	
		curRowNum++;
		while (curRowNum <= sheet.getLastRowNum())
		{
			curRow = sheet.getRow(curRowNum);
			if (curRow != null)
			{	
				//TODO - to check whether this row is really empty 
				//sometimes row looks empty but it is not treated as empty ...
				return curRowNum;
			}	
			curRowNum++;
		}
		
		curRow = null;  //This is for the cases when the parallel sheet has less rows than the primary sheet
		return -1;
	}
	
	

}
