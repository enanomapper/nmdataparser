package net.enanomapper.parser;

import net.enanomapper.parser.ExcelDataLocation.IterationAccess;
import net.enanomapper.parser.ExcelDataLocation.Recognition;

public class ExcelSheetConfiguration 
{
	public ExcelDataLocation.IterationAccess substanceIteration =  IterationAccess.ROW_SINGLE;
	public int rowMultiFixedSize = 1;
	public int startRow = 2;
	public int startHeaderRow = 0;
	public int endHeaderRow = 0;
	public boolean allowEmpty = true;
	public Recognition recognition = Recognition.BY_INDEX;
}
