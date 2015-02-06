package net.enanomapper.parser;

import java.util.ArrayList;

public class ExcelParserUtils 
{
	public static void setParallelSheet(ExcelDataLocation loc, ParallelSheetState parallelSheetStates[], 
					int primarySheetNum, ArrayList<String> errors)
	{
		if (loc.sheetIndex != primarySheetNum)
		{
			for (int i = 0; i < parallelSheetStates.length; i ++)
				if (loc.sheetIndex == parallelSheetStates[i].sheetNum)
				{
					loc.setParallelSheetIndex(i);
					return;
				}
			
			if (loc.iteration != ParserConstants.IterationAccess.ABSOLUTE_LOCATION) //This iteration mode is not treated as error
				errors.add("["+ locationStringForErrorMessage(loc) +  "] Sheet number number not valid parallel sheet!");
		}
	}
	
	public static String locationStringForErrorMessage(ExcelDataLocation loc)
	{
		//TODO
		return "location";
	}
	
	
	
	public static String locationStringForErrorMessage(ExcelDataLocation loc, int sheet)
	{
		//TODO 
		return "";
	}
}
