package net.enanomapper.parser.dynamicspan;


/**
 * 
 * @author nick
 * This class is typically used for dynamic recognition (assignment) of the row data 
 * when the Excel sheet is read by columns
 *
 */

public class RowSpan 
{

	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"ROW_SPAN\":\n");
		sb.append(offset + "{\n");
		
		//TODO
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
	}
	
}
