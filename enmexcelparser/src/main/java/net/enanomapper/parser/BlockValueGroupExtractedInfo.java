package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.List;

import net.enanomapper.parser.ParserConstants.BlockParameterAssign;
import net.enanomapper.parser.ParserConstants.DataInterpretation;

public class BlockValueGroupExtractedInfo 
{
	public static class ParamInfo
	{
		public String name = null;
		public BlockParameterAssign assign = BlockParameterAssign.UNDEFINED;
		public Integer columnPos = null;
		public Integer rowPos = null;
		public boolean fixColumnPosToStartValue = false;
		public boolean fixRowPosToStartValue = false;
		public boolean syncExcelSheetColumnWithValuePos = false;
		public boolean syncExcelSheetRowWithValuePos = false;
		public DataInterpretation dataInterpretation = DataInterpretation.DEFAULT;
		public String mapping = null;
		public String unit = null;
		public Object jsonValue = null;
	}
	
	protected List<String> errors = new ArrayList<String>();
	
	public String name = null;
	public BlockParameterAssign endpointAssign = BlockParameterAssign.UNDEFINED;
	public Integer endpointColumnPos = null;
	public Integer endpointRowPos = null;
	public boolean fixEndpointColumnPosToStartValue = false;
	public boolean fixEndpointRowPosToStartValue = false;
	public String endpointMapping = null;
	public boolean addValueGroupToEndpointName = false;
	public boolean addValueGroupAsPrefix = false;
	public String separator = " ";
	public String unit = null;
	
	public boolean FlagValues = false;
	public Integer startColumn = null;
	public Integer endColumn = null;
	public Integer startRow = null;
	public Integer endRow = null;
	public Integer errorColumnShift = null;
	public Integer errorRowShift = null;
	
	public List<ParamInfo> paramInfo = null;
	
	public ParamInfo endpointType = null;
	public String endpointTypeString = null;
	
	public ParamInfo valueQualifier = null;
	public String valueQualifierString = null;
	
	public ParamInfo errorQualifier = null;
	public String errorQualifierString = null;
	
	
	public List<String> getErrors()
	{
		return errors;
	}
	
}
