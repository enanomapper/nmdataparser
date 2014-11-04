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
	
	public enum DataType {CELL, ROW, COLUMN, BLOCK }
	
	public DataType elementType = DataType.CELL;
}
