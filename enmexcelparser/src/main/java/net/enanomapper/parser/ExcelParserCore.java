package net.enanomapper.parser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


import net.enanomapper.parser.ParserConstants.DataInterpretation;
import net.enanomapper.parser.excel.ExcelUtils;
import net.enanomapper.parser.exceptions.CellException;
import net.enanomapper.parser.exceptions.ExceptionAtLocation;
import net.enanomapper.parser.json.JsonUtilities;

/**
 * Core functionality for extracting data
 * from excel file
 * 
 * @author nick
 *
 */

public class ExcelParserCore 
{
	public final static Logger logger = Logger.getLogger(ExcelParserCore.class.getName());
	
	//Helper variables for excel file iteration
	protected ParallelSheetState parallelSheetStates[] = null;
	protected ArrayList<String> parallelSheetsErrors = new ArrayList<String>();
	protected ExcelParserConfigurator config = null;
	protected Workbook workbook;
	
	protected int primarySheetNum = 0;
	
	protected Row curRow = null;
	protected ArrayList<Row> curRows = null;
	
	//All variables read from the primary sheet and all parallel sheets
	protected HashMap<String, Object> curVariables = new HashMap<String, Object>();
	protected HashMap<String, HashMap<Object, Object>> curVariableMappings = new HashMap<String, HashMap<Object, Object>>();
	
	/**
	 * Generic function (regardless of the iteration access) Reads a string
	 * value from a cell which is of type String If cell is not of type 'String'
	 * error is generated
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected String getStringValue(ExcelDataLocation loc) throws Exception {
		switch (loc.iteration) {
		case ROW_SINGLE:
			if (loc.isFromParallelSheet()) {
				Row row = parallelSheetStates[loc.getParallelSheetIndex()].curRow;
				if (row != null)
					return getStringValue(row, loc);
				else
					return null;
			} else
				return getStringValue(curRow, loc); // from basic sheet

		case ROW_MULTI_FIXED:
		case ROW_MULTI_DYNAMIC:
			// Taking the value from the first row
			ArrayList<Row> rows;
			if (loc.isFromParallelSheet())
				rows = parallelSheetStates[loc.getParallelSheetIndex()].curRows;
			else
				rows = curRows;
			if (rows != null)
				if (!rows.isEmpty())
					return getStringValue(rows.get(0), loc);
			return null;

		case ABSOLUTE_LOCATION: {
			Object value = loc.getAbsoluteLocationValue();
			if (value == null) {
				value = getStringValueFromAbsoluteLocation(loc);
				loc.setAbsoluteLocationValue(value);
			}
			if (value != null)
				return value.toString();
			return null;
		}

		case JSON_VALUE: {
			Object value = loc.getJsonValue();
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else {
					throw new ExceptionAtLocation(loc, -1, "JSON_VALUE", " is not of type STRING!");
					//
				}
			return null;
		}

		case JSON_REPOSITORY: {
			String key = loc.getJsonRepositoryKey();
			Object value = config.jsonRepository.get(key);
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else {
					String msg = String.format("[%s] JSON_REPOSITORY value for key %s is not of type STRING!",
							locationStringForErrorMessage(loc));
					throw new Exception(msg);

				}
			return null;
		}

		case VARIABLE: {
			String key = loc.getVariableKey();
			Object value = curVariables.get(key);
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else {
					String msg = String.format("[%s] JSON_REPOSITORY value for key %s is not of type STRING!",
							locationStringForErrorMessage(loc));
					throw new Exception(msg);
				}
			return null;
		}

		default:
			return null;
		}
	}

	/**
	 * Generic function (regardless of the iteration access) string extraction
	 * and post processing of the string: extract --> SOURCE_COMBINATION ->
	 * MAPPING
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected String getString(ExcelDataLocation loc) throws Exception {
		String value = getString0(loc);
		if (value == null)
			return null;

		// Post processing of the string value
		if (loc.mapping != null) {
			Object obj = getMappingValue(value, loc.mapping);
			if (obj == null)
				return null;
			else
				value = obj.toString();
		}
		return value;
	}

	/**
	 * Generic function (regardless of the iteration access) for extracting
	 * string
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected String getString0(ExcelDataLocation loc) throws Exception {
		// (1)
		// SOURCE_COMBINATION for iteration modes:
		// ROW_SINGLE, ROW_MULTI_FIXED, ROW_MULTI_DYNAMIC
		// is handled within function getString(row, loc)

		// (2)
		// SOURCE_COMBINATION for iteration mode ABSOLUTE_LOCATION
		// is handled within getStringFromAbsoluteLocation(loc)

		// (3)
		// SOURCE_COMBINATION for iteration modes:
		// JSON_VALUE, JSON_REPOSITORY and VARIABLE
		// is handled by function getJsonSourceCombination(loc)
		// This function is called also in the iteration modes from (1) and (2)

		switch (loc.iteration) {
		case ROW_SINGLE:
			if (loc.isFromParallelSheet()) {
				Row row = parallelSheetStates[loc.getParallelSheetIndex()].curRow;
				if (row != null)
					return getString(row, loc);
				else
					return null;
			} else
				return getString(curRow, loc); // from basic sheet

		case ROW_MULTI_FIXED:
		case ROW_MULTI_DYNAMIC:
			// Taking the value from the first row
			ArrayList<Row> rows;
			if (loc.isFromParallelSheet())
				rows = parallelSheetStates[loc.getParallelSheetIndex()].curRows;
			else
				rows = curRows;
			if (rows != null)
				if (!rows.isEmpty())
					return getString(rows.get(0), loc);
			return null;

		case ABSOLUTE_LOCATION: {
			Object value = loc.getAbsoluteLocationValue();
			if (value == null) {
				value = getStringFromAbsoluteLocation(loc);
				loc.setAbsoluteLocationValue(value);
			}
			if (value != null)
				return value.toString();
			return null;
		}

		case JSON_VALUE: {
			if (loc.sourceCombination)
				return getJsonSourceCombination(loc);

			Object value = loc.getJsonValue();
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else {
					String msg = String.format("[%s] JSON_VALUE is not of type STRING!",
							locationStringForErrorMessage(loc));
					throw new Exception(msg);
				}
			return null;
		}

		case JSON_REPOSITORY: {
			if (loc.sourceCombination)
				return getJsonSourceCombination(loc);

			String key = loc.getJsonRepositoryKey();
			Object value = config.jsonRepository.get(key);
			if (value != null)
				if (value instanceof String)
					return (String) value;
				else {
					String msg = String.format("[%s] JSON_REPOSITORY value for key %s is not of type STRING!",
							locationStringForErrorMessage(loc));
					throw new Exception(msg);
				}
			return null;
		}

		case VARIABLE: {
			if (loc.sourceCombination)
				return getJsonSourceCombination(loc);

			String key = loc.getVariableKey();
			Object value = curVariables.get(key);
			if (value != null)
				return value.toString();
			return null;
		}

		default:
			return null;
		}
	}
	
	/**
	 * Generic function (regardless of the iteration access)
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected Date getDate(ExcelDataLocation loc) throws Exception {
		switch (loc.iteration) {
		case ROW_SINGLE:
			if (loc.isFromParallelSheet()) {
				Row row = parallelSheetStates[loc.getParallelSheetIndex()].curRow;
				if (row != null)
					return getDate(row, loc);
				else
					return null;
			} else
				return getDate(curRow, loc); // from basic sheet

		case ROW_MULTI_FIXED:
		case ROW_MULTI_DYNAMIC:
			// Taking the value from the first row
			ArrayList<Row> rows;
			if (loc.isFromParallelSheet())
				rows = parallelSheetStates[loc.getParallelSheetIndex()].curRows;
			else
				rows = curRows;
			if (rows != null)
				if (!rows.isEmpty())
					return getDate(rows.get(0), loc);
			return null;

		case ABSOLUTE_LOCATION: {
			Object value = loc.getAbsoluteLocationValue();
			if (value == null) {
				value = getDateFromAbsoluteLocation(loc);
				loc.setAbsoluteLocationValue(value);
			}
			if (value != null && (value instanceof Date))
				return (Date) value;
			return null;
		}

		case JSON_VALUE: {
			Object value = loc.getJsonValue();
			if (value != null)
				if (value instanceof String) {
					try {
						SimpleDateFormat formatter = new SimpleDateFormat(loc.dateFormat);
						Date d = formatter.parse((String) value);
						if (d != null)
							return d;
					} catch (Exception e) {
						String msg = "JSON_VALUE value \"" + value + "\" is not correclty formatted date! ";
						throw new Exception(msg);
					}

				}
			return null;
		}

		case JSON_REPOSITORY: {
			String key = loc.getJsonRepositoryKey();
			Object value = config.jsonRepository.get(key);
			// TODO
			return null;
		}

		case VARIABLE: {
			// TODO
			return null;
		}

		default:
			return null;
		}
	}

	/**
	 * Generic function (regardless of the iteration access)
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected Number getNumericValue(ExcelDataLocation loc) throws Exception {
		switch (loc.iteration) {
		case ROW_SINGLE:
			if (loc.isFromParallelSheet()) {
				Row row = parallelSheetStates[loc.getParallelSheetIndex()].curRow;
				if (row != null)
					return getNumericValue(row, loc);
				else
					return null;
			} else
				return getNumericValue(curRow, loc);

		case ROW_MULTI_FIXED: // Both treated the same way
		case ROW_MULTI_DYNAMIC:
			// Taking the value from the first row
			ArrayList<Row> rows;
			if (loc.isFromParallelSheet())
				rows = parallelSheetStates[loc.getParallelSheetIndex()].curRows;
			else
				rows = curRows;
			if (rows != null)
				if (!rows.isEmpty())
					return getNumericValue(rows.get(0), loc);
			return null;

		case ABSOLUTE_LOCATION: {
			Object value = loc.getAbsoluteLocationValue();
			if (value == null) {
				value = getNumericFromAbsoluteLocation(loc);
				loc.setAbsoluteLocationValue(value);
			}
			if (value != null && (value instanceof Number))
				return (Number) value;
			return null;
		}

		case JSON_VALUE: {
			Object value = loc.getJsonValue();
			if (value != null) {
				if (value instanceof Number)
					return (Number) value;
				else {
					String msg = String.format("[%s] JSON_VALUE is not of type NUMERIC!",
							locationStringForErrorMessage(loc));
					logger.log(Level.WARNING, msg);
					throw new Exception(msg);
				}
			}
			return null;
		}

		case JSON_REPOSITORY: {
			String key = loc.getJsonRepositoryKey();
			Object value = config.jsonRepository.get(key);
			if (value != null)
				if (value instanceof Number)
					return (Number) value;
				else {
					String msg = String.format("[%s] JSON_REPOSITORY value for key '%s' is not of type NUMERIC!",
							locationStringForErrorMessage(loc), key);
					logger.log(Level.WARNING, msg);
					throw new Exception(msg);
				}
			return null;
		}

		case VARIABLE: {
			String key = loc.getVariableKey();
			Object value = curVariables.get(key);
			if (value != null)
				if (value instanceof Number)
					return (Number) value;
				else {
					String msg = String.format("[%s] JSON_REPOSITORY value for key '%s' is not of type NUMERIC!",
							locationStringForErrorMessage(loc), key);
					logger.log(Level.WARNING, msg);
					throw new Exception(msg);
				}

			return null;
		}

		default:
			return null;
		}
	}

	/**
	 * 
	 * @param loc
	 * @return
	 */
	protected Object[] getArray(ExcelDataLocation loc) throws Exception {
		switch (loc.iteration) {
		case ROW_SINGLE:
			Row row = null;
			if (loc.isFromParallelSheet()) 
				row = parallelSheetStates[loc.getParallelSheetIndex()].curRow;
			 else
				row = curRow;
			if (row == null)
				return null;
			else {
				if (loc.columnIndices == null) {
					Cell c = row.getCell(loc.columnIndex);
					Object o = ExcelUtils.getObjectFromCell(c);
					return new Object[] {o};
				}
				else
					return getArrayFromColumnIndices(row, loc);
			}	

		case ROW_MULTI_FIXED: // Both treated the same way
		case ROW_MULTI_DYNAMIC:
			// TODO
			return null;

		case ABSOLUTE_LOCATION: {
			Object value = loc.getAbsoluteLocationValue();
			if (value == null) {
				value = getArrayFromAbsoluteLocation(loc);
				loc.setAbsoluteLocationValue(value);
			}
			if (value != null)
				return (Object[]) value;
			return null;
		}

		case JSON_VALUE: {
			Object value = loc.getJsonValue();
			if (value != null) {
				if (value instanceof Object[])
					return (Object[]) value;
				else
					throw new ExceptionAtLocation(loc, -1, "JSON_VALUE", " is not an array!");
			}
			return null;
		}

		case JSON_REPOSITORY: {
			// TODO
			return null;
		}

		case VARIABLE: {
			/*
			 * String key = loc.getVariableKey(); Object value =
			 * curVariables.get(key); if (value != null) if (value instanceof
			 * Object[]) return (Object[]) value; else parseErrors.add("[" +
			 * locationStringForErrorMessage(loc) +
			 * "] variable value for key \"" + key + "\" is not an array!");
			 */
			return null;
		}

		default:
			return null;
		}
	}

	/**
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected String getStringValueFromAbsoluteLocation(ExcelDataLocation loc) throws Exception {
		int sheetIndex = loc.getSheetIndex(workbook, primarySheetNum);
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		if (sheet != null) {
			Row r = sheet.getRow(loc.rowIndex);
			if (r == null)
				return null;

			Cell c = r.getCell(loc.columnIndex);
			if (c != null) {
				if (c.getCellType() != CellType.STRING) {
					String msg = String.format("[ %s ]: Cell is not of type STRING!",
							locationStringForErrorMessage(loc));
					throw new Exception(msg);
				}
				return c.getStringCellValue();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param loc
	 * @return
	 */
	protected String getStringFromAbsoluteLocation(ExcelDataLocation loc) throws Exception {
		if (loc.sourceCombination) {
			return getStringFromAbsoluteLocationAsSourceCombination(loc);
		}

		int sheetIndex = loc.getSheetIndex(workbook, primarySheetNum);
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		if (sheet != null) {
			Row r = sheet.getRow(loc.rowIndex);
			if (r == null)
				return null;

			Cell c = r.getCell(loc.columnIndex);
			return ExcelUtils.getStringFromCell(c);
		}
		return null;
	}

	/**
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected Number getNumericFromAbsoluteLocation(ExcelDataLocation loc) throws Exception {
		int sheetIndex = loc.getSheetIndex(workbook, primarySheetNum);
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		if (sheet != null) {
			Row r = sheet.getRow(loc.rowIndex);
			if (r == null)
				return null;

			Cell c = r.getCell(loc.columnIndex);

			Number d = ExcelUtils.getNumericValue(c);
			if (d != null)
				return d;
			else {
				String msg = String.format("[%s]: Cell is not of type NUMERIC!", locationStringForErrorMessage(loc));
				logger.log(Level.WARNING, msg);
				throw new Exception(msg);
				// return null;
			}

			/*
			 * if (c != null) { if (c.getCellType() != Cell.CELL_TYPE_NUMERIC) {
			 * parseErrors.add("[" + locationStringForErrorMessage(loc) +
			 * "]: Cell is not of type NUMERIC!"); return null; } return
			 * c.getNumericCellValue(); }
			 */
		}
		return null;
	}

	/**
	 * 
	 * @param loc
	 * @return
	 * @throws Exception
	 */
	protected Date getDateFromAbsoluteLocation(ExcelDataLocation loc) throws Exception {
		int sheetIndex = loc.getSheetIndex(workbook, primarySheetNum);
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		if (sheet != null) {
			Row r = sheet.getRow(loc.rowIndex);
			if (r == null)
				return null;

			Cell c = r.getCell(loc.columnIndex);

			Date d = ExcelUtils.getDateFromCell(c);
			if (d != null)
				return d;
			else {
				String msg = String.format("[%s]: Cell is not of formatted as date!",
						locationStringForErrorMessage(loc));
				logger.log(Level.WARNING, msg);
				throw new Exception(msg);
				// return null;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param loc
	 * @return
	 */
	protected Object[] getArrayFromAbsoluteLocation(ExcelDataLocation loc) throws Exception {
		int sheetIndex = loc.getSheetIndex(workbook, primarySheetNum);
		Sheet sheet = workbook.getSheetAt(sheetIndex);

		// The array is formed from a matrix defined by rows (and columns)
		// The rows are stored sequentially

		// Columns and row numbers are 0-based indexed
		int rows[];
		int columns[];

		if (loc.rowIndices != null) {
			// rowIndices take precedence over rowIndex
			rows = loc.rowIndices;
		} else {
			rows = new int[1];
			rows[0] = loc.rowIndex;
		}

		if (loc.columnIndices != null) {
			// columnIndices takes precedence over columnsIndex
			columns = loc.columnIndices;
		} else {
			columns = new int[1];
			columns[0] = loc.columnIndex;
		}

		// Array size = the number of 'matrix' elements
		int n = rows.length * columns.length;
		Object objects[] = new Object[n];

		for (int i = 0; i < rows.length; i++) {
			Row r = sheet.getRow(rows[i]);
			if (r == null) {
				for (int k = 0; k < columns.length; k++)
					objects[i * columns.length + k] = null;
				continue;
			}

			for (int k = 0; k < columns.length; k++) {
				Cell c = r.getCell(columns[k]);
				Object o = ExcelUtils.getObjectFromCell(c);
				objects[i * columns.length + k] = o;
			}
		}

	
		if (loc.dataInterpretation == DataInterpretation.AS_TEXT)
		{
			//Convert all objects to String
			Object textObjects[] = new Object[objects.length];
			for (int i = 0; i < objects.length; i++)
			{
				if (objects[i] == null)
					textObjects[i] = null;
				else
					textObjects[i] = objects[i].toString();
			}
			objects = textObjects;
		}
		
		if (loc.trimArray)
			return trimObjects(objects, rows.length, columns.length);

		return objects;
	}

	/**
	 * 
	 * @param objects
	 * @param nRows
	 * @param nColumns
	 * @return
	 */
	protected Object[] trimObjects(Object objects[], int nRows, int nColumns) {
		// The array is treated as a virtual matrix
		// Each dimension is trimmed to the maximal index that represents a
		// non-null object
		// correct values for nRows and nColumns are expected

		int maxRow = -1;
		int maxColumn = -1;
		for (int i = 0; i < nRows; i++) {
			for (int k = 0; k < nColumns; k++) {
				if (objects[i * nColumns + k] != null) {
					maxRow = i;
					if (maxColumn < k)
						maxColumn = k;
				}
			}
		}

		// constructing new 'virtual' matrix
		int nRows1 = maxRow + 1;
		int nColumns1 = maxColumn + 1;

		Object objects1[] = new Object[nRows1 * nColumns1];
		for (int i = 0; i < nRows1; i++)
			for (int k = 0; k < nColumns1; k++)
				objects1[i * nColumns1 + k] = objects[i * nColumns + k];

		return objects1;
	}
	
	protected Object[] getArrayFromColumnIndices(Row row, ExcelDataLocation loc) throws Exception {
		int columns[] = loc.columnIndices;
		Object objects[] = new Object[columns.length];

		for (int k = 0; k < columns.length; k++) {
			Cell c = row.getCell(columns[k]);
			Object o = ExcelUtils.getObjectFromCell(c);
			objects[k] = o;
		}

		if (loc.trimArray)
			return trimObjects(objects, 1, columns.length);

		return objects;
	}

	/**
	 * 
	 * @param row
	 * @param loc
	 * @return Returns null if cell is not of string type (i.e. numerics are
	 *         treated as errors)
	 * @throws Exception
	 */
	protected String getStringValue(Row row, ExcelDataLocation loc) throws Exception {
		Cell c = row.getCell(loc.columnIndex);

		if (c == null) {
			if (loc.allowEmpty) {
				return "";
			} else {
				// if (FlagAddParserStringError)
				throw new CellException(loc.sectionName, (primarySheetNum + 1), (row.getRowNum() + 1),
						(loc.columnIndex + 1), "  is empty!");
			}

		}

		if (c.getCellType() != CellType.STRING) {

			throw new CellException(loc.sectionName, (primarySheetNum + 1), (row.getRowNum() + 1),
					(loc.columnIndex + 1), String.format("Found cell type %s, expected %s (CELL_TYPE_STRING)",
							c.getCellType(), CellType.STRING));

		}

		return c.getStringCellValue();
	}

	/**
	 * 
	 * @param row
	 * @param loc
	 * @return
	 */
	protected String getString(Row row, ExcelDataLocation loc) throws Exception {
		if (loc.sourceCombination) {
			return getStringAsSourceCombination(row, loc);
		} else {
			Cell c = row.getCell(loc.columnIndex);
			return ExcelUtils.getStringFromCell(c);
		}
	}

	protected String getStringAsSourceCombination(Row row, ExcelDataLocation loc) throws Exception {
		StringBuffer sb = new StringBuffer();

		if (loc.columnIndices == null) {
			Cell c = row.getCell(loc.columnIndex);
			sb.append(ExcelUtils.getStringFromCell(c));
		} else {
			Object obj[] = getArrayFromColumnIndices(row, loc);
			if (obj != null)
				for (int i = 0; i < obj.length; i++) {
					if (sb.length() > 0)
						sb.append(loc.combinationSeparator);
					sb.append(obj[i] == null ? "" : obj[i].toString());
				}
		}

		// Handle non-excel info directly taken from json config file
		String jsonSource = getJsonSourceCombination(loc);
		if (!jsonSource.isEmpty()) {
			if (sb.length() > 0)
				sb.append(loc.combinationSeparator);
			sb.append(jsonSource);
		}

		return sb.toString();
	}

	protected String getStringFromAbsoluteLocationAsSourceCombination(ExcelDataLocation loc) throws Exception {
		StringBuffer sb = new StringBuffer();

		// Handle array defined by absolute location
		Object obj[] = getArrayFromAbsoluteLocation(loc);
		if (obj != null)
			JsonUtilities.addObjectToStringBuffer(obj, sb, loc.combinationSeparator);

		// Handle non-excel info directly taken from json config file
		String jsonSource = getJsonSourceCombination(loc);
		if (!jsonSource.isEmpty()) {
			if (sb.length() > 0)
				sb.append(loc.combinationSeparator);
			sb.append(jsonSource);
		}
		return sb.toString();
	}

	protected String getJsonSourceCombination(ExcelDataLocation loc) throws Exception {
		// Handle information from JSON_VALUE, JSON_REPOSITORY,
		// VARIABLE_KEY and VARIABLE_KEYS

		StringBuffer sb = new StringBuffer();

		if (loc.getJsonValue() != null) {
			Object value = loc.getJsonValue();
			JsonUtilities.addObjectToStringBuffer(value, sb, loc.combinationSeparator);
		}

		if (loc.getJsonRepositoryKey() != null) {
			String key = loc.getJsonRepositoryKey();
			Object value = config.jsonRepository.get(key);
			if (value != null)
				JsonUtilities.addObjectToStringBuffer(value, sb, loc.combinationSeparator);
		}

		if (loc.variableKeys != null) {
			// VARIABLE_KEYS takes precedence over VARIABLE_KEY
			for (int i = 0; i < loc.variableKeys.length; i++) {
				String key = loc.variableKeys[i];
				Object value = curVariables.get(key);
				if (value != null)
					JsonUtilities.addObjectToStringBuffer(value, sb, loc.combinationSeparator);
			}
		} else {
			// handle VARIABLE_KEY
			String key = loc.getVariableKey();
			Object value = curVariables.get(key);
			if (value != null)
				JsonUtilities.addObjectToStringBuffer(value, sb, loc.combinationSeparator);
		}

		return sb.toString();
	}

	/**
	 * 
	 * @param row
	 * @param loc
	 * @return
	 */
	protected Date getDate(Row row, ExcelDataLocation loc) {
		Cell c = row.getCell(loc.columnIndex);
		return ExcelUtils.getDateFromCell(c);
	}

	/**
	 * 
	 * @param row
	 * @param loc
	 * @return
	 * @throws CellException
	 */
	protected Number getNumericValue(Row row, ExcelDataLocation loc) throws CellException {
		Cell c = row.getCell(loc.columnIndex);

		if (c == null) {
			if (loc.allowEmpty) { // why empty should be zero ?
				return null;
			} else {
				String msg = String.format(CellException.msg_format, loc.sectionName, (primarySheetNum + 1),
						(row.getRowNum() + 1), (loc.columnIndex + 1), " is empty!");
				logger.log(Level.WARNING, msg);

				return null;
			}
		}

		Number d = ExcelUtils.getNumericValue(c);

		if (d != null)
			return d;
		if (loc.allowEmpty && (CellType.BLANK == c.getCellType()))
			return null;
		else {
			throw new CellException(loc.sectionName, (primarySheetNum + 1), (row.getRowNum() + 1),
					(loc.columnIndex + 1), String.format("Found cell type %s, expected %s (CELL_TYPE_NUMERIC)",
							c.getCellType(), CellType.NUMERIC));
		}


		/*
		 * if (c.getCellType() != Cell.CELL_TYPE_NUMERIC) {
		 * parseErrors.add("JSON Section " + loc.sectionName + ", sheet " +
		 * (primarySheetNum + 1) + ", row " + (row.getRowNum() + 1) + " cell " +
		 * (loc.columnIndex + 1) + " is not of type NUMERIC!"); return null; }
		 * 
		 * return c.getNumericCellValue();
		 */
	}

	protected String locationStringForErrorMessage(ExcelDataLocation loc) {
		StringBuffer sb = new StringBuffer();
		if (loc.columnIndex > 0)
			sb.append(String.format(" Col %d (%s)", loc.columnIndex + 1,
					CellReference.convertNumToColString(loc.columnIndex)));
		if (loc.rowIndex > 0)
			sb.append(String.format(" Row %d", loc.rowIndex + 1));

		return sb.toString();
	}

	protected String locationStringForErrorMessage(ExcelDataLocation loc, int sheet) {
		StringBuffer sb = new StringBuffer();
		if (sheet > 0)
			sb.append(String.format(" Sheet %d", sheet));

		if (loc != null) {
			if (loc.columnIndex > 0)
				sb.append(String.format(" Col %d (%s)", loc.columnIndex + 1,
						CellReference.convertNumToColString(loc.columnIndex)));
			if (loc.rowIndex > 0)
				sb.append(String.format(" Row %d", loc.rowIndex + 1));
		}
		return sb.toString();
	}
	
	protected Object getMappingValue(Object originalValue, String mapping) {
		HashMap<Object, Object> map = curVariableMappings.get(mapping);
		if (map == null)
			return null;
		// Original read value is used as a key to obtain the result value;		
		return map.get(originalValue);
	}

	protected boolean isCellPercentageFormatted(ExcelDataLocation loc) throws Exception 
	{
		switch (loc.iteration) {
		case ROW_SINGLE:
			if (loc.isFromParallelSheet()) {
				Row row = parallelSheetStates[loc.getParallelSheetIndex()].curRow;
				if (row != null)
					return isCellPercentageFormatted(row, loc);
				else
					return false;
			} else
				return isCellPercentageFormatted(curRow, loc);

		case ROW_MULTI_FIXED: // Both treated the same way
		case ROW_MULTI_DYNAMIC:
			// Taking the value from the first row
			ArrayList<Row> rows;
			if (loc.isFromParallelSheet())
				rows = parallelSheetStates[loc.getParallelSheetIndex()].curRows;
			else
				rows = curRows;
			if (rows != null)
				if (!rows.isEmpty())
					return isCellPercentageFormatted(rows.get(0), loc);
			return false;

		case ABSOLUTE_LOCATION: {
			return isCellPercentageFormattedAbsoluteLocation(loc);
		}

		case JSON_VALUE: 
		case JSON_REPOSITORY: 
		case VARIABLE: 
			return false;
		
		default:
			return false;
		}

	}
	
	protected boolean isCellPercentageFormatted(Row row, ExcelDataLocation loc) {
		Cell c = row.getCell(loc.columnIndex);
		if (c == null) 
			return false;
		else
			return (c.getCellStyle().getDataFormatString().contains("%"));		
	}
	
	protected boolean isCellPercentageFormattedAbsoluteLocation(ExcelDataLocation loc) throws Exception {
		int sheetIndex = loc.getSheetIndex(workbook, primarySheetNum);
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		if (sheet != null) {
			Row r = sheet.getRow(loc.rowIndex);
			if (r == null)
				return false;

			Cell c = r.getCell(loc.columnIndex);
			if (c == null) 
				return false;
			else
				return (c.getCellStyle().getDataFormatString().contains("%"));			
		}
		return false;
	}
}
