package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Logger;

import net.enanomapper.parser.ParserConstants.DynamicIteration;
import net.enanomapper.parser.ParserConstants.SheetSynchronization;
import net.enanomapper.parser.excel.ExcelUtils;
import net.enanomapper.parser.excel.ExcelUtils.IndexInterval;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ParallelSheetState 
{	
	private final static Logger logger = Logger.getLogger(ParallelSheetState.class.getName());
	
	public int sheetNum = 0;
	public Sheet sheet = null;
	public int curRowNum = 1;
	public int curCellNum = 1;	
	public Row curRow = null;
	public ArrayList<Row> curRows = null;
	public Iterator<Row> rowIt = null; 
	public Cell curCell = null;
	public int dynamicIterationColumnIndex = 0;
	public DynamicIteration dynamicIteration = DynamicIteration.NEXT_NOT_EMPTY;
	SheetSynchronization synchronization = SheetSynchronization.NONE; 
	public TreeMap<Integer, String> rowGroups = null;
	public HashMap<String, IndexInterval> groupRows = null;
	public ArrayList<String> errors = new ArrayList<String>();
	
	
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
	
	public int iterateRowMultiDynamic(String synchKey)
	{
		switch (synchronization)
		{
		case NONE:
			return iterateRowMultuDynamic_NoSynch();
			
		case MATCH_KEY:
			if (synchKey == null)
				curRows = null;
			else
			{
				IndexInterval intr = groupRows.get(synchKey);
				if (intr == null)
					errors.add("Syncronization key " + synchKey + " not found!");
				else
				{
					curRows = new ArrayList<Row>();
					for (int i = intr.startIndex; i <= intr.endIndex; i++)
					{
						Row r = sheet.getRow(i);
						if (r != null)
							curRows.add(r);
					}
				}
			}
			break;
			
		default:
			break;
		}
		return 0;
	}
	
	protected int iterateRowMultuDynamic_NoSynch()
	{
		logger.info("----- Parallel sheet #" + (sheetNum + 1) + " - Reading at row: " + (curRowNum+1));
		
		switch (dynamicIteration)
		{
		case NEXT_NOT_EMPTY: {	
			if (curRowNum <= sheet.getLastRowNum())
				curRows = new ArrayList<Row>();
			else
			{
				curRows = null;
				return -1;
			}
			
			//The first row is already checked to be non empty 
			curRow = sheet.getRow(curRowNum);
			curRows.add(curRow);
			curRowNum++;
			
			Cell c0 = curRow.getCell(dynamicIterationColumnIndex);
			String key = ExcelUtils.getStringFromCell(c0);
			logger.info("parallel key: " + key);
						
			while (curRowNum <= sheet.getLastRowNum())
			{
				curRow = sheet.getRow(curRowNum);
				if (ExcelUtils.isEmpty(curRow))
				{	
					//Empty row is skipped
					curRowNum++;
					continue;
				}
				else
				{
					Cell c = curRow.getCell(dynamicIterationColumnIndex);
					if (ExcelUtils.isEmpty(c))
					{
						curRows.add(curRow);
						curRowNum++;
					}
					else
					{
						logger.info("**** Parallel sheet #" + (sheetNum + 1) +  "  next " + c.toString() + "   read " + curRows.size() + " rows");
						return 0; //Reached next record
					}
				}				
			} //end of while
			
		}
		break;
		
		case NEXT_DIFFERENT_VALUE:
		{
			//TODO
		}
		break;
		
		default:
			curRowNum++;
			curRows = null;
		}
		return 0;
	}
	
	public int setRowGroups(int keyColumnIndex, boolean recognizeGroupByNextNonEmpty)
	{
		rowGroups = ExcelUtils.getRowGroups(sheet, curRowNum, keyColumnIndex, recognizeGroupByNextNonEmpty);
		groupRows = ExcelUtils.getGroupIndexIntervals(rowGroups, sheet.getLastRowNum());
		
		logger.info("Set Row Groups for Parallel sheet #" + (sheetNum + 1) + "\n" + rowGroupsToString());
		return 0;
	}
	
	public String rowGroupsToString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Row groups:\n");
		for (Integer key : rowGroups.keySet())
			sb.append("  " + (key + 1) + "  " + rowGroups.get(key) + "\n");
		sb.append("Group rows:\n");
		for (String key : groupRows.keySet())
		{	
			IndexInterval intr = groupRows.get(key);
			sb.append("  " + key + "  " + (intr.startIndex + 1) + "  " + (intr.endIndex + 1) +  "\n");
		}	
		
		return sb.toString();
	}

}
