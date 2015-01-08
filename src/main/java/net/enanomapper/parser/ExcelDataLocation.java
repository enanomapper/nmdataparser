package net.enanomapper.parser;

import org.apache.poi.ss.usermodel.Cell;



public class ExcelDataLocation 
{
	public enum IterationAccess {
		ROW_SINGLE, ROW_MULTI_FIXED, ROW_MULTI_DYNAMIC, COLUMN_SINGLE, COLUMN_MULTI_FIXED, COLUMN_MULTI_DYNAMIC,
		ABSOLUTE_LOCATION, JSON_VALUE, JSON_REPOSITORY, UNDEFINED;
		
		public static IterationAccess fromString(String s)
		{	 
			try
			{
				IterationAccess access = IterationAccess.valueOf(s) ;
				return (access);
			}
			catch (Exception e)
			{
				return IterationAccess.UNDEFINED;
			}
		}
		
		public boolean isColumnInfoRequired()
		{	
			if (this.equals(ROW_SINGLE))
				return true;
			if (this.equals(ROW_MULTI_FIXED))
				return true;
			if (this.equals(ROW_MULTI_DYNAMIC))
				return true;
			if (this.equals(ABSOLUTE_LOCATION))
				return true;
			
			return false;
		}
		
		public boolean isRowInfoRequired()
		{	
			if (this.equals(COLUMN_SINGLE))
				return true;
			if (this.equals(COLUMN_MULTI_FIXED))
				return true;
			if (this.equals(COLUMN_MULTI_DYNAMIC))
				return true;
			if (this.equals(ABSOLUTE_LOCATION))
				return true;
			
			return false;
		}
	}
	
	public enum DataType {
		CELL, ROW, COLUMN, BLOCK, UNDEFINED;
		
		public static DataType fromString(String s)
		{
			try
			{
				DataType type  = DataType.valueOf(s) ;
				return (type);
			}
			catch (Exception e)
			{
				return DataType.UNDEFINED;
			}
		}
	}
	
	public enum CellType {
		BLANK, BOOLEAN, ERROR, FORMULA, NUMERIC, STRING, UNDEFINED;
		
		public static CellType fromString(String s)
		{
			try
			{
				CellType type  = CellType.valueOf(s) ;
				return (type);
			}
			catch (Exception e)
			{
				return CellType.UNDEFINED;
			}
		}
		
		public static CellType fromPOICelType(int poiCellType)
		{
			switch (poiCellType)
			{
			case Cell.CELL_TYPE_BLANK:
				return BLANK;
			case Cell.CELL_TYPE_BOOLEAN:
				return BOOLEAN;
			case Cell.CELL_TYPE_ERROR:
				return ERROR;
			case Cell.CELL_TYPE_FORMULA:
				return FORMULA;
			case Cell.CELL_TYPE_NUMERIC:
				return BLANK;
			case Cell.CELL_TYPE_STRING:
				return BLANK;
			}
			return UNDEFINED;
		}
		
		public static int toPOICellType(CellType type)
		{
			switch (type)
			{
			case BLANK:
				return Cell.CELL_TYPE_BLANK;
			case BOOLEAN:
				return Cell.CELL_TYPE_BOOLEAN;
			case ERROR:
				return Cell.CELL_TYPE_ERROR;
			case FORMULA:
				return Cell.CELL_TYPE_FORMULA;
			case NUMERIC:
				return Cell.CELL_TYPE_NUMERIC;
			case STRING:
				return Cell.CELL_TYPE_STRING;
			case UNDEFINED:
				return Cell.CELL_TYPE_STRING;
			}
			return Cell.CELL_TYPE_BLANK;
		}
		
		
	}
	
	public enum Recognition {
		BY_INDEX, BY_NAME, BY_INDEX_AND_NAME, UNDEFINED;
		
		public static Recognition fromString(String s)
		{
			try
			{
				Recognition rec  = Recognition.valueOf(s) ;
				return (rec);
			}
			catch (Exception e)
			{
				return Recognition.UNDEFINED;
			}
		}
	}
	
	private Object absoluteLocationValue = null;
	private Object jsonValue = null;
	private String jsonRepositoryKey = null;
	
	
	public int nErrors = 0;
	public String sectionName = "";
	
	public DataType dataType = DataType.CELL;
	public boolean FlagDataType = false;
	
	public Recognition recognition = Recognition.BY_INDEX;
	public boolean FlagRecognition = false;
	
	//public CellType cellType = CellType.STRING;
	//public boolean FlagCellType = false;
	
	public IterationAccess iteration = IterationAccess.ROW_SINGLE;	
	public boolean FlagIteration = false;
	
	public boolean allowEmpty = true;
	public boolean FlagAllowEmpty = false;
	
	public int sheetIndex = 0;
	public boolean FlagSheetIndex = false;
	
	public String sheetName = null;
	public boolean FlagSheetName = false;
	
	public int columnIndex = 0;
	public boolean FlagColumnIndex = false;
	
	public String columnName = null;
	public boolean FlagColumnName = false;
	
	public int rowIndex = 0;
	public boolean FlagRowIndex = false;
	
	public String rowName = null;
	public boolean FlagRowName = false;
	
		
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"" + sectionName + "\":\n");
		sb.append(offset + "{\n");
		
		if (FlagDataType)
		{
			sb.append(offset + "\t\"DATA_TYPE\" : \"" + dataType.toString() + "\"");
			nFields++;
		}
		
		if (FlagRecognition)
		{
			sb.append(offset + "\t\"RECOGNITION\" : \"" + recognition.toString() + "\"");
			nFields++;
		}
		
		if (FlagIteration)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ITERATION\" : \"" + iteration.toString() + "\"");
			nFields++;
		}
		
		if (FlagAllowEmpty)
		{
			if (nFields > 0)
				sb.append(",\n");
				
			sb.append(offset + "\t\"ALLOW_EMPTY\" : " + allowEmpty);
			nFields++;
		}
		
		if (FlagSheetIndex)
		{
			if (nFields > 0)
				sb.append(",\n");
				
			sb.append(offset + "\t\"SHEET_INDEX\" : " + (sheetIndex + 1)); //0 --> 1-based
			nFields++;
		}
		
		if (FlagSheetName)
		{
			if (nFields > 0)
				sb.append(",\n");
				
			sb.append(offset + "\t\"SHEET_NAME\" : \"" + sheetName+"\"");
			nFields++;
		}
		
		if (FlagColumnIndex)
		{
			if (nFields > 0)
				sb.append(",\n");
				
			sb.append(offset + "\t\"COLUMN_INDEX\" : " + (columnIndex + 1) ); //0 --> 1-based
			nFields++;
		}
		
		if (FlagColumnName)
		{
			if (nFields > 0)
				sb.append(",\n");
				
			sb.append(offset + "\t\"COLUMN_NAME\" : \"" + columnName + "\"");
			nFields++;
		}
		
		if (FlagRowIndex)
		{
			if (nFields > 0)
				sb.append(",\n");
				
			sb.append(offset + "\t\"ROW_INDEX\" : " + (rowIndex + 1) ); //0 --> 1-based
			nFields++;
		}
		
		if (FlagRowName)
		{
			if (nFields > 0)
				sb.append(",\n");
				
			sb.append(offset + "\t\"ROW_NAME\" : \"" + rowName + "\"");
			nFields++;
		}
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
	}

	public Object getAbsoluteLocationValue() {
		return absoluteLocationValue;
	}

	public void setAbsoluteLocationValue(Object absoluteLocationValue) {
		this.absoluteLocationValue = absoluteLocationValue;
	}

	public Object getJsonValue() {
		return jsonValue;
	}

	public void setJsonValue(Object jsonValue) {
		this.jsonValue = jsonValue;
	}

	public String getJsonRepositoryKey() {
		return jsonRepositoryKey;
	}

	public void setJsonRepositoryKey(String jsonRepositoryKey) {
		this.jsonRepositoryKey = jsonRepositoryKey;
	}
}
