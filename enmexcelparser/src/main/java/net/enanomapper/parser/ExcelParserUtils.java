package net.enanomapper.parser;

import java.util.ArrayList;

import org.apache.poi.hssf.util.CellReference;

import com.fasterxml.jackson.databind.JsonNode;

public class ExcelParserUtils 
{
	
	public static int extractColumnIndex(JsonNode node)
	{
		if (node.isInt())
		{	
			Integer intValue = node.asInt();
			if (intValue == null)
				return -1;
			else
				return intValue - 1;  //1 --> 0
		}
		
		if (node.isTextual())
		{
			String s = node.asText();
			if (s == null)
				return -1;
			
			//TODO better check for the string
			int col = CellReference.convertColStringToIndex(s);
			if (col >= 0)			
				return col;
		}
		
		return -1;
	}
	
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
