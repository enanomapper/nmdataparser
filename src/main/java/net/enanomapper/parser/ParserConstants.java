package net.enanomapper.parser;


import org.apache.poi.ss.usermodel.Cell;

public class ParserConstants 
{
	public enum IterationAccess {
		ROW_SINGLE {
		    @Override
		    public boolean isColumnInfoRequired() {
			return true;
		    }
		}, 
		
		ROW_MULTI_FIXED {
		    @Override
		    public boolean isColumnInfoRequired() {
		        return true;
		    }
		},
		
		ROW_MULTI_DYNAMIC {
		    @Override
		    public boolean isColumnInfoRequired() {
		        return true;
		    }
		},
		
		COLUMN_SINGLE {
			@Override
		    public boolean isRowInfoRequired() {
		    	return true;
			}
		}, 
		
		COLUMN_MULTI_FIXED {
			@Override
			public boolean isRowInfoRequired() {
		    	return true;
			}
		},
		
		COLUMN_MULTI_DYNAMIC {
			@Override
			public boolean isRowInfoRequired() {
		    	return true;
			}
		},
		
		ABSOLUTE_LOCATION {
		    @Override
		    public boolean isColumnInfoRequired() {
		        return true;
		    }
		    @Override
		    public boolean isRowInfoRequired() {
		    	return true;
			}
		},
		
		JSON_VALUE, 
		JSON_REPOSITORY, 
		UNDEFINED;
		
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
			return false; //default value is overridden for some cases
		}
		
		public boolean isRowInfoRequired()
		{	
			return false; //default value is overridden for some cases
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
	
	public enum DynamicIteration {
		NEXT_NOT_EMPTY, NEXT_DIFFERENT_VALUE, UNDEFINED;
		
		public static DynamicIteration fromString(String s)
		{	 
			try
			{
				DynamicIteration di = DynamicIteration.valueOf(s) ;
				return (di);
			}
			catch (Exception e)
			{
				return DynamicIteration.UNDEFINED;
			}
		}
	}
	
	public enum SheetSynhronization {
		NONE, MATCH_KEY, UNDEFINED
	}
	
}
