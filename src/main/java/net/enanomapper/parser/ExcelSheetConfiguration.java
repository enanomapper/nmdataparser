package net.enanomapper.parser;


import net.enanomapper.parser.ParserConstants.DynamicIteration;
import net.enanomapper.parser.ParserConstants.IterationAccess;
import net.enanomapper.parser.ParserConstants.Recognition;

public class ExcelSheetConfiguration 
{
	public IterationAccess substanceIteration =  IterationAccess.ROW_SINGLE;
	public boolean FlagSubstanceIteration = false;
	
	public int rowMultiFixedSize = 1;
	public boolean FlagRowMultiFixedSize = false;
	//public boolean Flag = false;
	
	public int startRow = 2;
	public boolean FlagStartRow = false;
	
	public int startHeaderRow = 0;
	public boolean FlagStartHeaderRow = false;
	
	
	public int endHeaderRow = 0;
	public boolean FlagEndHeaderRow = false;
	
	public boolean allowEmpty = true;
	public boolean FlagAllowEmpty = false;
	
	public Recognition recognition = Recognition.BY_INDEX;
	public boolean FlagRecognition = false;
	
	public DynamicIteration dynamicIteration = DynamicIteration.NEXT_NOT_EMPTY;
	public boolean FlagDynamicIteration = false;
	
	
	public String toJSONKeyWord(String offset)
	{	
		int nSections = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		
		//TODO
		
		sb.append(offset + "}");
		return sb.toString();
	}
	
}
