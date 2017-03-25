package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.hssf.util.CellReference;
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
	
	public static final String NULL_POINTER_CLUSTER = "___NULL_POINTER_CLUSTER___";
	
	
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
	
	
	public static TreeMap<String, ArrayList<Integer>> getRowClusters(ArrayList<Row> rows, int clusteringColumnIndex)
	{
		TreeMap<String, ArrayList<Integer>> clusters = new TreeMap<String, ArrayList<Integer>>();
		for (int i = 0; i < rows.size(); i++)
		{
			Object obj = getObject(clusteringColumnIndex, rows.get(i));
			
			String key;
			if (obj == null)
				key = NULL_POINTER_CLUSTER;
			else
				key = obj.toString().trim();
			
			ArrayList<Integer> cluster = clusters.get(key);
			if (cluster == null)
			{
				cluster = new ArrayList<Integer>();
				clusters.put(key, cluster);
			}
			
			cluster.add(i);
		}
		
		return clusters;
	}
	
	public static TreeMap<String, ArrayList<Integer>> getRowClusters(ArrayList<Row> rows, int clusteringColumnIndices[])
	{
		return  getRowClusters(rows, clusteringColumnIndices, " ", true);  //default separator " "
	}
	
	
	public static TreeMap<String, ArrayList<Integer>> getRowClusters(ArrayList<Row> rows, int clusteringColumnIndices[], String separator, boolean FlagTrim)
	{
		TreeMap<String, ArrayList<Integer>> clusters = new TreeMap<String, ArrayList<Integer>>();
		for (int i = 0; i < rows.size(); i++)
		{
			Object obj = getUnifiedStringObject(clusteringColumnIndices, rows.get(i), separator, FlagTrim);
			
			String key;
			if (obj == null)
				key = NULL_POINTER_CLUSTER;
			else
				key = obj.toString().trim();
			
			ArrayList<Integer> cluster = clusters.get(key);
			if (cluster == null)
			{
				cluster = new ArrayList<Integer>();
				clusters.put(key, cluster);
			}
			
			cluster.add(i);
		}
		
		return clusters;
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
			
			/*
			if ( (cell.getCellType() == Cell.CELL_TYPE_STRING))
				if("".equals(cell.getStringCellValue().trim()))
					return true;
			*/
			return false;
		}
	}
	
	public static boolean isEmpty (Cell cell, boolean FlagTrim)
	{
		if (cell == null)
			return true;
		else
		{	 
			if (cell.getCellType() == Cell.CELL_TYPE_BLANK)
				return true;
			
			if (FlagTrim) 
				if ( (cell.getCellType() == Cell.CELL_TYPE_STRING))
					if("".equals(cell.getStringCellValue().trim()))
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
		{	
			if (c.getCellStyle().getDataFormatString().contains("%"))
				return "" + (c.getNumericCellValue()*100.0) + "%";
			else
				return "" + c.getNumericCellValue();
		}	
		case Cell.CELL_TYPE_STRING:
			return c.getStringCellValue();
		case Cell.CELL_TYPE_FORMULA: {	
			if (c.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC)
			{	
				if (c.getCellStyle().getDataFormatString().contains("%"))
					return "" + (c.getNumericCellValue()*100.0) + "%";
				else
					return "" + c.getNumericCellValue();
			}	
			if (c.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING)
				return c.getStringCellValue();
		}	
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
		{	
			Double v = new Double(c.getNumericCellValue());
			if (c.getCellStyle().getDataFormatString().contains("%"))
				v = 100.0 * v;
			return v;
		}	
			
		case Cell.CELL_TYPE_BOOLEAN:
			return new Boolean(c.getBooleanCellValue());	
			
		case Cell.CELL_TYPE_STRING:
			return c.getStringCellValue();
			
		case Cell.CELL_TYPE_FORMULA: {	
			if (c.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC)
			{	
				if (c.getCellStyle().getDataFormatString().contains("%"))
					return new Double(c.getNumericCellValue()*100.0);
				else
					return new Double(c.getNumericCellValue());
			}	
			if (c.getCachedFormulaResultType() == Cell.CELL_TYPE_BOOLEAN)
				return new Boolean(c.getBooleanCellValue());
			if (c.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING)
				return c.getStringCellValue();
			
		}
			
			//TODO some other types ???
		}
		return null;
	}
	
	public static Number getNumericValue(Cell c)
	{	
		return getNumericValue(c, false);
	}
	
	public static Number getNumericValue(Cell c, boolean FlagTreatBlankAs0)
	{
		if (c == null)
			return null;
		
		switch (c.getCellType())
		{
		case Cell.CELL_TYPE_BLANK:
		{	
			if (FlagTreatBlankAs0)
				return new Double(0.0);
			else
				return null;
		}	
		case Cell.CELL_TYPE_NUMERIC:
		{	
			Double v = new Double(c.getNumericCellValue());
			if (c.getCellStyle().getDataFormatString().contains("%"))
				v = 100.0 * v;
			return v;
		}	
		case Cell.CELL_TYPE_FORMULA: {	
			if (c.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC)
			{	
				if (c.getCellStyle().getDataFormatString().contains("%"))
					return new Double(c.getNumericCellValue()*100.0);
				else
					return new Double(c.getNumericCellValue());
			}	
		}	
		}
		
		return null;
	}
	
	public static Date getDateFromCell(Cell c)
	{
		if (c == null)
			return null;
		
		switch (c.getCellType())
		{
		case Cell.CELL_TYPE_BLANK:
		{	
			return null;
		}	
		case Cell.CELL_TYPE_NUMERIC:
		{	
			String format = c.getCellStyle().getDataFormatString();
			if (format.indexOf('/') > 0 || format.indexOf('y') > 0 
					|| format.indexOf('m') > 0 || format.indexOf('d') > 0)
				return c.getDateCellValue();
			return null;
		}	
		case Cell.CELL_TYPE_FORMULA: 
		{	
			if (c.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC)
			{	
				String format = c.getCellStyle().getDataFormatString();
				if (format.indexOf('/') > 0 || format.indexOf('y') > 0 
						|| format.indexOf('m') > 0 || format.indexOf('d') > 0)
					return c.getDateCellValue();
			}	
			return null;
		}	
		}
		return null;
	}
	
	
	public static Object getObject(int column, Row row)
	{	
		Cell c = row.getCell(column);
		return ExcelUtils.getObjectFromCell(c);
	}
	
	public static Object getObject(String column, Row row)
	{
		int col = CellReference.convertColStringToIndex(column);
		Cell c = row.getCell(col);
		return ExcelUtils.getObjectFromCell(c);
	}
	
	public static Object getUnifiedStringObject(int columns[], Row row, String separator, boolean FlagTrim)
	{	
		if (columns == null)
			return null;
		
		if (columns.length == 0)
			return null;
		
		StringBuffer sb = new StringBuffer();
		int nNonNull = 0;
		
		for (int i = 0; i < columns.length; i++)
		{
			Object obj = getObject(columns[i], row);
			if (obj != null)
			{
				if (nNonNull > 0)
					sb.append(separator);
				nNonNull++;
				if (FlagTrim)
					sb.append(obj.toString().trim());
				else
					sb.append(obj.toString());
			}
		}
		
		if (nNonNull > 0)
			return sb.toString();
		else
			return null;
		
	}
	
	public static List<TreeMap<Integer,Object>> fillCellGaps(List<Row> rows, String fillColumns[])
	{
		if (fillColumns == null)
			return null;
		
		int columns[] = new int[fillColumns.length];
		for (int i = 0; i < fillColumns.length; i++)
			columns[i] = CellReference.convertColStringToIndex(fillColumns[i]);
		
		return fillCellGaps(rows, columns, -1); //default: no criterion is observed / all cells from fillColumns are filled
	}
	
	public static List<TreeMap<Integer,Object>> fillCellGaps(List<Row> rows, String fillColumns[], String criterionColumn)
	{
		if (fillColumns == null)
			return null;
		
		int columns[] = new int[fillColumns.length];
		for (int i = 0; i < fillColumns.length; i++)
			columns[i] = CellReference.convertColStringToIndex(fillColumns[i]);
		
		int crColumn = CellReference.convertColStringToIndex(criterionColumn);
		
		return fillCellGaps(rows, columns, crColumn); //default: no criterion is observed / all cells from fillColumns are filled
	}
	
	public static List<TreeMap<Integer,Object>> fillCellGaps(List<Row> rows, int fillColumns[])
	{
		 return fillCellGaps(rows, fillColumns, -1); //default: no criterion is observed / all cells from fillColumns are filled 
	}
	
	
	/**
	 * Applies strategy to filling missing cell values.
	 * @param rows a bloch of excel rows where the gaps (empty cells) are filled
	 * @param fillColumns
	 * @param criterionColumn
	 * @return returns a list with the missing Cell values for each row 
	 */
	public static List<TreeMap<Integer,Object>> fillCellGaps(List<Row> rows, int fillColumns[], int criterionColumn)
	{
		if (fillColumns == null)
			return null;
		
		List<TreeMap<Integer,Object>> fillInfo = new ArrayList<TreeMap<Integer,Object>>();
		fillInfo.add(new TreeMap<Integer,Object>()); //and empty map is added in the first row 
		
		for (int i = 1; i < rows.size(); i++)  //For the first row (index 0) gaps are not filled
		{	
			TreeMap<Integer,Object> rowFill =  new TreeMap<Integer,Object>();
			boolean FlagFillGaps = true;
			if (criterionColumn >= 0)
				FlagFillGaps = isEmpty(rows.get(i).getCell(criterionColumn), true);
			
			for (int k = 0; k < fillColumns.length; k++)
			{
				int col = fillColumns[k];
				if (isEmpty(rows.get(i).getCell(col), true))
				{
					int fullRow = findFirstNonEmptyUpInColumn(rows, i, col);
					if (fullRow >= 0)
					{	
						//rows.get(i).createCell.getCell(col).setCellComment(arg0);
						//TODO
					}	
				}
			}
		}
		
		return fillInfo;
	}
	
	public static List<TreeMap<Integer,Object>> fillCellGapsByTargetRow(ArrayList<Row> rows, int fillColumns[], int targetRowNum)
	{
		if (fillColumns == null)
			return null;
		
		if (targetRowNum < 0)
			return null;
		
		if (targetRowNum >= rows.size())
			return null;
		
		List<TreeMap<Integer,Object>> fillInfo = new ArrayList<TreeMap<Integer,Object>>();
		Row targetRow = rows.get(targetRowNum);
		
		for (int i = 0; i < rows.size(); i++)
		{
			
			
			if (i == targetRowNum)
			{
				fillInfo.add(new TreeMap<Integer,Object>()); //and empty map is added for the target line 
				continue;
			}
			
			Row row = rows.get(i);
			TreeMap<Integer,Object> omap = new TreeMap<Integer,Object>();
			
			for (int k = 0; k < fillColumns.length; k++)
			{
				int col = fillColumns[k];
				if (isEmpty(row.getCell(col), true))
				{
					Object obj =  getObject(col, targetRow);
					omap.put(col, obj);
				}
			}
			
			fillInfo.add(omap);
		}
		
		return fillInfo;
	}
		
	public static int findFirstNonEmptyUpInColumn(List<Row> rows, int startRow, int column)
	{	
		for (int i = startRow; i >= 0; i--)
		{	
			Cell c = rows.get(i).getCell(column);
			if (!isEmpty(c, true))
				return i;
		}		
		return -1;
	}
	
	
	public static String rowToString(Row row)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++)
			sb.append(getStringFromCell(row.getCell(i))+ " ");
		
		return sb.toString();
	}
	
	public static String rowClustersToString(TreeMap<String, ArrayList<Integer>> clusters)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Row clusters =  " + clusters.size() + "\n");
		for (String key : clusters.keySet())
		{	
			ArrayList<Integer> cluster = clusters.get(key);
			sb.append(key + ": ");
			for (int i = 0; i < cluster.size(); i++)
				sb.append(" " + cluster.get(i));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	
	
	
}
