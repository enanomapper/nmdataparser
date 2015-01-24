package net.enanomapper.parser;


import java.util.HashMap;

import net.enanomapper.parser.ParserConstants.DynamicIteration;
import net.enanomapper.parser.ParserConstants.IterationAccess;
import net.enanomapper.parser.ParserConstants.Recognition;

public class ExcelSheetConfiguration 
{
	public int sheetIndex = 1;
	public boolean FlagSheetIndex = false;
	
	public String sheetName = null;
	public boolean FlagSheetName = false;
	
	public IterationAccess iteration =  IterationAccess.ROW_SINGLE;
	public boolean FlagIteration = false;
	
	public int rowMultiFixedSize = 1;
	public boolean FlagRowMultiFixedSize = false;
	
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
	
	public int dynamicIterationColumnIndex = 0;
	public boolean FlagDynamicIterationColumnIndex = false;
	
	//Read data as variables
	public HashMap<String, ExcelDataLocation> variableLocations = null;
	
	//Handling locations dynamically
	public DynamicIterationSpan dynamicIterationSpan = null;
	public ColumnSpan columnSpan = null;
	public RowSpan rowSpan = null;
	
	
	public String toJSONKeyWord(String offset)
	{	
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		
		if (FlagSheetIndex)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"SHEET_INDEX\" : \"" + (sheetIndex + 1) + "\"");
			nFields++;
		}
		
		if (FlagSheetName)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"SHEET_NAME\" : \"" + sheetName + "\"");
			nFields++;
		}
		
		if (FlagIteration)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"SUBSTANCE_ITERATION\" : \"" + iteration.toString() + "\"");
			nFields++;
		}
		
		if (FlagRowMultiFixedSize)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ROW_MULTI_FIXED_SIZE\" : \"" + rowMultiFixedSize + "\"");
			nFields++;
		}
		
		
		if (FlagStartRow)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"START_ROW\" : \"" + (startRow + 1) + "\"");
			nFields++;
		}
		
		
		if (FlagStartHeaderRow)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"START_HEADER_ROW\" : \"" + (startHeaderRow + 1) + "\"");
			nFields++;
		}
		
		if (FlagEndHeaderRow)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"END_HEADER_ROW\" : \"" + (endHeaderRow + 1) + "\"");
			nFields++;
		}
		
		
		if (FlagAllowEmpty)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ALLOW_EMPTY\" : \"" + allowEmpty + "\"");
			nFields++;
		}
		
		
		if (FlagRecognition)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"RECOGNITION\" : \"" + recognition.toString() + "\"");
			nFields++;
		}
		
		
		
		if (FlagDynamicIteration)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"DYNAMIC_ITERATION\" : \"" + dynamicIteration.toString() + "\"");
			nFields++;
		}
		
		if (FlagDynamicIterationColumnIndex)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"DYNAMIC_ITERATION_COLUMN_INDEX\" : \"" + (dynamicIterationColumnIndex + 1) + "\"");
			nFields++;
		}
		
		
		if (variableLocations != null)
		{	
			if (nFields > 0)
				sb.append(",\n");
			
			sb.append(offset + "\t\"VARIABLES\" : \n" );
			sb.append(offset + "\t{\n" );
			
			int nParams = 0;
			for (String var : variableLocations.keySet())
			{	
				ExcelDataLocation loc = variableLocations.get(var);
				sb.append(loc.toJSONKeyWord(offset + "\t\t"));
				
				if (nParams < variableLocations.size())
					sb.append(",\n\n");
				else
					sb.append("\n");
				nParams++;
			}
			sb.append(offset + "\t}" );
		}
		
		
		//Dynamic locations
		if (dynamicIterationSpan != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			dynamicIterationSpan.toJSONKeyWord(offset + "\t");
		}
		
		if (columnSpan != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			columnSpan.toJSONKeyWord(offset + "\t");
		}
		
		if (rowSpan != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			rowSpan.toJSONKeyWord(offset + "\t");
		}
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		return sb.toString();
	}
	
}
