package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.List;

import net.enanomapper.parser.ParserConstants.BlockParameterAssign;

public class BlockValueGroupExtractedInfo 
{
	public static class ParamInfo
	{
		public String name = null;
		public BlockParameterAssign assign = BlockParameterAssign.UNDEFINED;
		public Integer columnPos = null;
		public Integer rowPos = null;
		public String mapping = null;
		public String unit = null;
	}
	
	protected List<String> errors = new ArrayList<String>();
	
	public String name = null;
	public String unit = null;
	
	public boolean FlagValues = false;
	public Integer startColumn = null;
	public Integer endColumn = null;
	public Integer startRow = null;
	public Integer endRow = null;
	public Integer errorColumnShift = null;
	public Integer errorRowShift = null;
	
	public List<ParamInfo> paramInfo = null;
	
	public List<String> getErrors()
	{
		return errors;
	}
	
	
	
}
