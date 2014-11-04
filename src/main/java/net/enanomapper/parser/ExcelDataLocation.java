package net.enanomapper.parser;



public class ExcelDataLocation 
{
	public enum IterationAccess {
		ROW_SINGLE, ROW_MULTI_FIXED, ROW_MULTI_DYNAMIC, UNDEFINED;
		
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
	
	public String error = null;
	
	public DataType dataType = DataType.CELL;
	public IterationAccess dataAccess = IterationAccess.ROW_SINGLE;
	public boolean allowEmpty = true;
	public int sheetIndex = 0;
	public String sheetName = null;
	public int columnIndex = 0;
	public String columnName = null;
	public int rowIndex = 0;
	public String rowName = null;
	
	public String toJSONKeyWord()
	{
		//TODO
		return "";
	}
	
}
