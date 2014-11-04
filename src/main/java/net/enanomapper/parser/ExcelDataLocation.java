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
	
	public DataType dataType = DataType.CELL;
	public IterationAccess dataAccess = IterationAccess.ROW_SINGLE;
	public boolean allowEmpty = true;
	public int sheetNumber = 0;
	public String sheetName = null;
	public int columnNum = 0;
	public int rowNum = 0;
	
}
