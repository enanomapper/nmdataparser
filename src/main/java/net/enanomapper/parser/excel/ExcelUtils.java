package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ExcelUtils 
{
	public static class IndexInterval {
		public IndexInterval(int startIndex, int endIndex) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
		public int startIndex;
		public int endIndex;
	}
	
	
	public static TreeMap<Integer, String> getRowGroups(Sheet sheet, int startRowIndex, int endRowIndex, int keyColumnIndex)
	{	
		return getRowGroups(sheet, startRowIndex, endRowIndex, keyColumnIndex, true);
	}
	
	public static TreeMap<Integer, String> getRowGroups(Sheet sheet, int startRowIndex, int keyColumnIndex, 
			boolean recognizeGroupByNextNonEmpty)
	{
		return getRowGroups(sheet, startRowIndex, sheet.getLastRowNum(), keyColumnIndex, recognizeGroupByNextNonEmpty);
	}
	
	
	public static TreeMap<Integer, String> getRowGroups(Sheet sheet, int startRowIndex, int endRowIndex, int keyColumnIndex, 
				boolean recognizeGroupByNextNonEmpty)
	{
		int startRI = startRowIndex;
		int endRI = endRowIndex;
		
		//Index consistency check
		if (startRI < 0)
			startRI = 0;
		
		if (endRI > sheet.getLastRowNum())
			endRI = sheet.getLastRowNum();
		
		if (endRI < startRI)
			endRI = startRI;
		
		
		//TODO check keyColumnIndex
		
		
		TreeMap<Integer, String> groups = new TreeMap<Integer, String>();
		int curIndex = startRI;
		Row curRow = null;
		
		//Skip the starting empty rows
		while (curIndex <= endRI)
		{
			curRow = sheet.getRow(curIndex);
			if (isEmpty(curRow))
			{
				curIndex++;
				continue;
			}
			else
				break;
		}
		
		if (curIndex > endRI)
			return groups; //No groups are added. All rows are empty
		
		//Add first group info
		Cell c = curRow.getCell(keyColumnIndex);
		String value = getStringFromCell(c);
		String prevValue = value;
		groups.put(curIndex, value);
		curIndex++;
			
		while (curIndex <= endRI)
		{	
			curRow = sheet.getRow(curIndex); //Skip empty row
			if (isEmpty(curRow))
			{
				curIndex++;
				continue;
			}
			else
			{	
				c = curRow.getCell(keyColumnIndex);
				value = getStringFromCell(c);

				if (recognizeGroupByNextNonEmpty)
				{	
					if (value.equals(""))
					{
						curIndex++;
					}
					else
					{
						//Next group registration
						groups.put(curIndex, value);
						curIndex++;
					}
				}
				else //Recognize group as consequent rows that have the same values in the key column
				{	
					if (value.equals(prevValue))
					{
						curIndex++;
					}
					else
					{
						//Next group registration
						groups.put(curIndex, value);
						curIndex++;
						prevValue = value;
					}
				}
			}
		}
		
		return groups;
	}
	
	
	public static TreeMap<Integer, String> getRowGroups(ArrayList<Row> rows, int keyColumnIndex, boolean recognizeGroupByNextNonEmpty)
	{		
		TreeMap<Integer, String> groups = new TreeMap<Integer, String>();
		int curIndex = 0;
		Row curRow = null;

		//Skip the starting empty rows
		while (curIndex < rows.size())
		{	
			curRow = rows.get(curIndex);
			if (isEmpty(curRow))
			{
				curIndex++;
				continue;
			}
			else
				break;
		}

		if (curIndex >= rows.size())
			return groups; //No groups are added. All rows are empty

		//Add first group info
		Cell c = curRow.getCell(keyColumnIndex);
		String value = getStringFromCell(c);
		String prevValue = value;
		groups.put(curIndex, value);
		curIndex++;

		while (curIndex < rows.size())
		{	
			curRow = rows.get(curIndex);
			
			//Skip empty row
			if (isEmpty(curRow))
			{
				curIndex++;
				continue;
			}
			else
			{	
				c = curRow.getCell(keyColumnIndex);
				value = getStringFromCell(c);

				if (recognizeGroupByNextNonEmpty)
				{	
					if (value.equals(""))
					{
						curIndex++;
					}
					else
					{
						//Next group registration
						groups.put(curIndex, value);
						curIndex++;
					}
				}
				else //Recognize group as consequent rows that have the same values in the key column
				{	
					if (value.equals(prevValue))
					{
						curIndex++;
					}
					else
					{
						//Next group registration
						groups.put(curIndex, value);
						curIndex++;
						prevValue = value;
					}
				}
			}
		}
		return groups;
	}
	
	public static HashMap<String, IndexInterval> getGroupIndexIntervals(TreeMap<Integer, String> groups, int lastIndex)
	{
		HashMap<String, IndexInterval> grpIntervals = new HashMap<String, IndexInterval> ();
		if (groups == null)
			return grpIntervals;
		
		if (groups.isEmpty())
			return grpIntervals;
		
		String prevString = null;
		Integer prevInt = null;
		for (Entry<Integer, String> entry : groups.entrySet())
		{
			if (prevString != null)
			{
				IndexInterval interval = new IndexInterval(prevInt, entry.getKey()-1);
				grpIntervals.put(prevString, interval);
			}
			
			prevString = entry.getValue();
			prevInt = entry.getKey();
		}
		
		IndexInterval interval = new IndexInterval(prevInt, lastIndex);
		grpIntervals.put(prevString, interval);
		
		
		return grpIntervals;
	}
	
	public static ArrayList<Row> getNonEmptyRows(Sheet sheet, int startRow, int endRow)
	{
		ArrayList<Row> rows = new ArrayList<Row> ();
		for (int i = startRow; i <= endRow; i++ )
		{	
			Row row = sheet.getRow(i);
			if (!isEmpty(row))
				rows.add(row);
		}
		return rows;
	}
	
	
	
	
	public static boolean isEmpty (Row row)
	{
		if (row == null)
			return true;
		else
		{	
			for (int i = 0; i <= row.getLastCellNum(); i++)
			{
				Cell c = row.getCell(i);
				if (isEmpty(c))
					continue;
				else
					return false;
			}
			return true;
		}
	}
	
	public static boolean isEmpty (Cell cell)
	{
		if (cell == null)
			return true;
		else
		{	 
			if (cell.getCellType() == Cell.CELL_TYPE_BLANK)
				return true;
			//TODO eventually to check some other 'strange' cases of empty cells
			
			return false;
		}
	}
	
	public static String getStringFromCell(Cell c)
	{
		if (c == null)
			return "";
		
		switch (c.getCellType())
		{
		case Cell.CELL_TYPE_BLANK:
			return "";
		case Cell.CELL_TYPE_NUMERIC:
			return "" + c.getNumericCellValue();
		case Cell.CELL_TYPE_STRING:
			return c.getStringCellValue();
			//TODO
		}
		return "";
	}
	
	public static Object getObjectFromCell(Cell c)
	{
		if (c == null)
			return null;
		
		switch (c.getCellType())
		{
		case Cell.CELL_TYPE_BLANK:
			return null;
		case Cell.CELL_TYPE_NUMERIC:
			return new Double(c.getNumericCellValue());
		case Cell.CELL_TYPE_STRING:
			return c.getStringCellValue();
			//TODO
		}
		return null;
	}
	
	public static String rowToString(Row row)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++)
			sb.append(getStringFromCell(row.getCell(i))+ " ");
		
		return sb.toString();
	}
	
	
}
