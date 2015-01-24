package net.enanomapper.parser;


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
	public boolean handleByRows = true;
	public boolean FlaghandleByRows = false;
	
	
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"DYNAMIC_SPAN\":\n");
		sb.append(offset + "{\n");
		
		
		/*
		if ()
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ITERATION\" : \"" + iteration.toString() + "\"");
			nFields++;
		}
		*/
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
	}
}
