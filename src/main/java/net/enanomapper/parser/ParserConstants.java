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
		VARIABLE,
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
	
	public enum SheetSynchronization {
		NONE, MATCH_KEY, UNDEFINED;
		
		public static SheetSynchronization fromString(String s)
		{	 
			try
			{
				SheetSynchronization synch = SheetSynchronization.valueOf(s) ;
				return (synch);
			}
			catch (Exception e)
			{
				return SheetSynchronization.UNDEFINED;
			}
		}
	}
	
	
	/*
	public enum StructureInfoType {
		INCHI, INCHI_KEY, FORMULA, SMILES
	}
	*/
	
	public enum ElementPosition {
		ANY_ROW, FIRST_ROW, NON_FIRST_ROW, ANY_GROUP_ROW, FIRST_GROUP_ROW, NON_FIRST_GROUP_ROW, UNDEFINED;
		
		public static ElementPosition fromString(String s)
		{	 
			try
			{
				ElementPosition pos = ElementPosition.valueOf(s) ;
				return (pos);
			}
			catch (Exception e)
			{
				return ElementPosition.UNDEFINED;
			}
		}
	}
	
	public enum ElementDataType {
		
		EFFECT, EFFECT_ARRAY, 
		PROTOCOL, PROTOCOL_ARRAY, 
		PROTOCOL_APPLICATION, PROTOCOL_APPLICATION_ARRAY, 
		SUBSTANCE, SUBSTANCE_ARRAY,
		VARIABLE, VARIABLE_CONTAINER,
		UNDEFINED;
		
		public static ElementDataType fromString(String s)
		{	 
			try
			{
				ElementDataType type = ElementDataType.valueOf(s) ;
				return (type);
			}
			catch (Exception e)
			{
				return ElementDataType.UNDEFINED;
			}
		}
		
		public boolean isElementOf(ElementDataType type)
		{
			//TODO improve it
			return (this.ordinal() < type.ordinal());
		}
	};
	
	
	public enum ElementField {
		NONE,
		
		//Substance record fields
		COMPANY_NAME {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.SUBSTANCE)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		COMPANY_UUID {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.SUBSTANCE)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		OWNER_NAME {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.SUBSTANCE)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		OWNER_UUID {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.SUBSTANCE)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		SUBSTANCE_TYPE {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.SUBSTANCE)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		PUBLIC_NAME {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.SUBSTANCE)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		ID_SUBSTANCE {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.SUBSTANCE)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		COMPOSITION_STR_TYPE {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.SUBSTANCE)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		COMPOSITION_STR_INFO {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.SUBSTANCE)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		COMPOSITION_RELATION {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.SUBSTANCE)
		    		return true;
		    	else
		    		return false;
			}
		},  
		
		//Protocol fields
		PROTOCOL_TOP_CATEGORY {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		PROTOCOL_CATEGORY_CODE {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		PROTOCOL_CATEGORY_TITLE {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		PROTOCOL_GUIDELINE {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		//Protocol application fields
		CITATION_TITLE {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL_APPLICATION)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		CITATION_YEAR {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL_APPLICATION)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		CITATION_OWNER {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL_APPLICATION)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		PARAMETERS {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL_APPLICATION)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		RELIABILITY_IS_ROBUST_STUDY {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL_APPLICATION)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		RELIABILITY_IS_USED_FOR_CLASSIFICATION {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL_APPLICATION)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		RELIABILITY_IS_USED_FOR_MSDS {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL_APPLICATION)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		RELIABILITY_PURPOSE_FLAG {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL_APPLICATION)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		RELIABILITY_STUDY_RESULT_TYPE {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL_APPLICATION)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		RELIABILITY_VALUE  {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL_APPLICATION)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		INTERPRETATION_RESULT {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL_APPLICATION)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		INTERPRETATION_CRITERIA {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.PROTOCOL_APPLICATION)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		//Effect fields
		ENDPOINT {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.EFFECT)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		SAMPLE_ID {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.EFFECT)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		UNIT {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.EFFECT)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		LO_VALUE {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.EFFECT)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		LO_QUALIFIER {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.EFFECT)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		UP_VALUE {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.EFFECT)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		UP_QUALIFIER {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.EFFECT)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		TEXT_VALUE {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.EFFECT)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		ERR_VALUE {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.EFFECT)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		ERR_QUALIFIER {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.EFFECT)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		VALUE {      //Used for reach value approach (setting lo/up/err + qualifiers + unit at once)
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.EFFECT)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		
		CONDITIONS {
			@Override
		    public boolean isFieldOf(ElementDataType type) {
		    	if (type == ElementDataType.EFFECT)
		    		return true;
		    	else
		    		return false;
			}
		},
		
		UNDEFINED;
		
		public boolean isFieldOf(ElementDataType type)
		{
			return false;
		}
		
		public ElementDataType getElement()
		{
			if (this.isFieldOf(ElementDataType.SUBSTANCE))
				return ElementDataType.SUBSTANCE;
			
			if (this.isFieldOf(ElementDataType.PROTOCOL_APPLICATION))
				return ElementDataType.PROTOCOL_APPLICATION;
			
			if (this.isFieldOf(ElementDataType.PROTOCOL))
				return ElementDataType.PROTOCOL;
			
			if (this.isFieldOf(ElementDataType.EFFECT))
				return ElementDataType.EFFECT;
			
			return ElementDataType.UNDEFINED;
		}
		
		public static ElementField fromString(String s)
		{	 
			try
			{
				ElementField type = ElementField.valueOf(s) ;
				return (type);
			}
			catch (Exception e)
			{
				return ElementField.UNDEFINED;
			}
		}
	};
	
	
	
}
