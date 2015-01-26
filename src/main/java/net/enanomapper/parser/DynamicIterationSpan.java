package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;

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
	public boolean handleByRows = true;    //The flag is related to the iteration mode and it determines the whether basic data elements are rows or columns
	public boolean FlagHandleByRows = false;
	
	public HashMap <Integer, DataElementType> fields = new HashMap <Integer, DataElementType> ();   //may this could be member variable of the grouping class 
	public ArrayList<DynamicGrouping> groupings = new ArrayList<DynamicGrouping>();
	
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"DYNAMIC_SPAN\":\n");
		sb.append(offset + "{\n");
		
		
		if (FlagHandleByRows)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"HANDLE_BY_ROWS\" : \"" + handleByRows + "\"");
			nFields++;
		}
		
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
	}
}
