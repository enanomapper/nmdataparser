package net.enanomapper.parser.excel;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
//import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.enanomapper.parser.excel.SALogicalCondition.ComparisonOperation;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

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
	
	public static interface IHandleExcelAddress {
		public void handle(CellAddress cellAddr, Sheet sheet) throws Exception;
		public void handle(CellRangeAddress cellRangeAddr, Sheet sheet) throws Exception;
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
			if (cell.getCellType() == CellType.BLANK)
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
			if (cell.getCellType() == CellType.BLANK)
				return true;
			
			if (FlagTrim) 
				if ( (cell.getCellType() == CellType.STRING))
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
		case BLANK:
			return "";
		case NUMERIC:
		{	
			if (c.getCellStyle().getDataFormatString().contains("%"))
				return "" + (c.getNumericCellValue()*100.0) + "%";
			else
				return "" + c.getNumericCellValue();
		}	
		case STRING:
			return c.getStringCellValue();
		case FORMULA: {	
			if (c.getCachedFormulaResultType() == CellType.NUMERIC)
			{	
				if (c.getCellStyle().getDataFormatString().contains("%"))
					return "" + (c.getNumericCellValue()*100.0) + "%";
				else
					return "" + c.getNumericCellValue();
			}	
			if (c.getCachedFormulaResultType() == CellType.STRING)
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
		case BLANK:
			return null;
			
		case NUMERIC:
		{	
			Double v = new Double(c.getNumericCellValue());
			if (c.getCellStyle().getDataFormatString().contains("%"))
				v = 100.0 * v;
			return v;
		}	
			
		case BOOLEAN:
			return new Boolean(c.getBooleanCellValue());	
			
		case STRING:
			return c.getStringCellValue();
			
		case FORMULA: {	
			if (c.getCachedFormulaResultType() == CellType.NUMERIC)
			{	
				if (c.getCellStyle().getDataFormatString().contains("%"))
					return new Double(c.getNumericCellValue()*100.0);
				else
					return new Double(c.getNumericCellValue());
			}	
			if (c.getCachedFormulaResultType() == CellType.BOOLEAN)
				return new Boolean(c.getBooleanCellValue());
			if (c.getCachedFormulaResultType() == CellType.STRING)
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
		case BLANK:
		{	
			if (FlagTreatBlankAs0)
				return new Double(0.0);
			else
				return null;
		}	
		case NUMERIC:
		{	
			Double v = new Double(c.getNumericCellValue());
			if (c.getCellStyle().getDataFormatString().contains("%"))
				v = 100.0 * v;
			return v;
		}	
		case FORMULA: {	
			if (c.getCachedFormulaResultType() == CellType.NUMERIC)
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
		case BLANK:
		{	
			return null;
		}	
		case NUMERIC:
		{	
			try {
				SimpleDateFormat format = new SimpleDateFormat(c.getCellStyle().getDataFormatString());
				return c.getDateCellValue();
			} catch (Exception x) {
				return null;
				//if format parsing fails, then it is not valid date format
			}
			/*
			//this is not necessarily right, date format may be very different and not always include "/" 
			//better use date format functions ... 
			if (format.indexOf('/') > 0 || format.indexOf('y') > 0 
					|| format.indexOf('m') > 0 || format.indexOf('d') > 0)
				return c.getDateCellValue();
			return null;
			*/
		}	
		case FORMULA: 
		{	
			if (c.getCachedFormulaResultType() == CellType.NUMERIC)
			{	
				try {
					SimpleDateFormat format = new SimpleDateFormat(c.getCellStyle().getDataFormatString());
					return c.getDateCellValue();
				} catch (Exception x) {
					
					return null;
				}
				/*
				String format = c.getCellStyle().getDataFormatString();
				if (format.indexOf('/') > 0 || format.indexOf('y') > 0 
						|| format.indexOf('m') > 0 || format.indexOf('d') > 0)
					return c.getDateCellValue();
					*/
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
	
	
	public static Workbook getExcelReader(InputStream input, boolean xlsxFormat) throws Exception 
	{
		Workbook workbook = null;

		if (xlsxFormat)
			workbook = new XSSFWorkbook(input);
		else
			workbook = new HSSFWorkbook(input);

		return workbook;
	}
	
	
	public static boolean isXLSXFormat(File file) {
		if (file.getAbsolutePath().toLowerCase().endsWith("xlsx"))
			return true;
		return false;
	}
	
	public static Sheet getSheet(Workbook workbook, Integer sheetIndex, String sheetName) 
	{
		if (sheetIndex != null) //sheet index takes precedence
			return workbook.getSheetAt(sheetIndex);
		else {
			if (sheetName != null)
				return workbook.getSheet(sheetName);
			else
				return null;
		}	
	}
	
	public static void iterateExcelScope(ExcelScope scope, Workbook workbook, 
			IHandleExcelAddress excHandler) throws Exception 
	{
		for (int i = 0; i < scope.cellRanges.size(); i++)
		{
			Sheet sheet = getSheet(workbook, scope.sheetIndices.get(i),scope.sheetNames.get(i));
			CellRangeAddress cra = scope.cellRanges.get(i);
			//pre-handling  
			excHandler.handle(cra, sheet);
			
			//Handling each cell from the range
			for (CellAddress cellAddr : cra)
				excHandler.handle(cellAddr, sheet);
		}
	}
	
	
	public static boolean checkConditionForCell(Cell cell, String qualifier, boolean ignoreCase, Object param) 
	{
		ComparisonOperation comparison = SALogicalCondition.qualifierToComparisonOperation(qualifier);
		if (comparison != ComparisonOperation.UNDEFINED)
			return checkConditionForCell(cell, comparison, ignoreCase, param);
		else
			return false;
	}
	
	public static boolean checkConditionForCell(Cell cell, ComparisonOperation comparison, 
			boolean ignoreCase, Object params) 
	{		
		if (comparison == ComparisonOperation.IS_EMPTY) {
			return isEmpty(cell);
		}
		
		if (comparison == ComparisonOperation.NOT_EMPTY) {
			return !isEmpty(cell);
		}
		
		//Null cell is not valid for other cases
		if (cell == null)
			return false;
		
		if (params == null)
		{
			return false;
		}
		
		//Handle input parameters as generic array of objects
		if (params instanceof Object[]) 
		{
			Object[] objects = (Object[]) params;
			if (objects.length == 1)
			{
				if (objects[0] instanceof String)
					return checkConditionForCellComparedToString(cell, comparison, 
							ignoreCase, (String) objects[0]);
				
				if (objects[0] instanceof Double) {
					//TODO
				}
			}
		}	
		
		//Handle specific case of input parameters
		if (params instanceof String)
			return checkConditionForCellComparedToString(cell, comparison, ignoreCase, (String) params);
		
		if (params instanceof Double)
		{
			//TODO
		}
		
	
		return false;
	}
	
	
	public static boolean checkConditionForCellComparedToString(Cell cell, 
			ComparisonOperation comparison, boolean ignoreCase, String param)
	{
		String cellStr = getStringFromCell(cell);
		if (cellStr == null)
			return false;
		
		int compareRes = 0;
		if (ignoreCase)
			compareRes = cellStr.compareToIgnoreCase(param);
		else
			compareRes = cellStr.compareTo(param);
		
		//System.out.println("Comparing " + cellStr + " with " + param);
		//System.out.println("compareRes = " + compareRes);
		
		switch (comparison) 
		{
		case EQUAL:
			return (compareRes == 0);
		case GREATER:
			return (compareRes > 0);
		case GREATER_OR_EQUAL:
			return (compareRes >= 0);
		case LESS:
			return (compareRes < 0);
		case LESS_OR_EQUAL:
			return (compareRes <= 0);
		case NOT_EQUAL:
			return (compareRes != 0);
		case IN_SET:
			//The set contains only one element (i.e. the param itself)
			return (compareRes == 0);
		case INTERVAL:
			//With a single parameter, the INTERVAL comparison 
			//is treated as [param,...] i.e. equavalent to GREATER_OR_EQUAL 
			return (compareRes >= 0);
		}
		
		//All other cases are not applicable here
		return false;
	}
	
	
	public static Object extractParamsForLogicalConditionCheck(ComparisonOperation comparison, Object params[], int checkIndex )
	{
		if (params == null)
			return null;
		if (params.length == 0)
			return null;
		
		if (comparison == ComparisonOperation.UNDEFINED 
				|| comparison == ComparisonOperation.IS_EMPTY
				|| comparison == ComparisonOperation.NOT_EMPTY)
			return null;
		
		if (comparison == ComparisonOperation.IN_SET
				|| comparison == ComparisonOperation.INTERVAL)
			return params;
		
		//Handling standard comparison case: 
		//EQUAL, LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL
		if (checkIndex == -1)
			return null;		
		if (checkIndex >= params.length)
			return params[params.length-1]; //last element is returned		
		return params[checkIndex];
	}
	
}
