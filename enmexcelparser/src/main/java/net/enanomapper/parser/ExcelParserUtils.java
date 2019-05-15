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
	
	public static int[] extractIndicesFromString(String str, boolean column)
	{
		String tok[] = str.split("-");
		if (tok.length != 2)
			return null;
		
		int ind0 = -1;
		int ind1 = -1;
		
		String s0 = tok[0].trim();
		if (s0.isEmpty())
			return null;
		if (Character.isDigit(s0.charAt(0)))
		{		
			try {
				ind0 = Integer.parseInt(s0);
				ind0--; //1 --> 0
			}
			catch (Exception x) {
				return null;
			}
		}
		else
		{	
			if (column)
				ind0 = CellReference.convertColStringToIndex(s0);
			else
				return null;
		}	
		
		String s1 = tok[1].trim();
		if (s1.isEmpty())
			return null;
		if (Character.isDigit(s1.charAt(0)))
		{		
			try {
				ind1 = Integer.parseInt(s1);
				ind1--; //1 --> 0
			}
			catch (Exception x) {
				return null;
			}
		}
		else
		{	
			if (column)
				ind1 = CellReference.convertColStringToIndex(s1);
			else
				return null;
		}
		
		if (ind0 <= ind1)
		{	
			int n = ind1 - ind0 + 1;
			int indices[] = new int[n];
			for (int i = 0; i < n; i++)
				indices[i] = ind0 + i;
			
			return indices;
		}
		return null;
	}
	
	
}
