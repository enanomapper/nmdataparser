package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Sheet;

import net.enanomapper.parser.ParserConstants.DataElementType;



/**
 * 
 * @author nick
 * This class defines information for dynamic extraction of information during iteration
 * This info is typically used by the iteration modes: ROW_MULTI_*, COLUMN_MULTI_*
 * 
 * 
 */
public class DynamicIterationSpan 
{	
	public static class Element
	{
		public DataElementType type = null;
		int index = -1;
		String jsonInfo = null;
		boolean infoFromHeader = true;
	}
	
	
	public boolean handleByRows = true;    //The flag is related to the iteration mode and it determines whether basic data elements are rows or columns
	public boolean FlagHandleByRows = false;
	
	public DataElementType cumulativeObjectType = null; //This is what type of object is formed by the cumulative effect of all of rows/columns
	public DataElementType rowType = null;  //This is the default row level grouping 
	//public DataElementType columnType = null;  //This is the default column level grouping
	public ArrayList<Element> elements = null;  
	public ArrayList<DynamicGrouping> groupLevels = null;
		
	//data synchronization
	
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"DYNAMIC_ITERATION_SPAN\":\n");
		sb.append(offset + "{\n");
		
		
		if (FlagHandleByRows)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"HANDLE_BY_ROWS\" : \"" + handleByRows + "\"");
			nFields++;
		}
		
		if (cumulativeObjectType != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"CUMULATIVE_OBJECT_TYPE\" : \"" + cumulativeObjectType.toString() + "\"");
			nFields++;
		}
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
	}
	
	
	public ArrayList<Object> createDataObjectsFromRows(Sheet sheet, int startRowIndex, int endRowIndex)
	{
		if (!handleByRows)
			return null;  //This cannot be done if the basic data element is not a row
		
		//TODO
		return null;
	}
	
}
