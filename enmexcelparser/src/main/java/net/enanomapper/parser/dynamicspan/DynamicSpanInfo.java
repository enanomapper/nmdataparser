package net.enanomapper.parser.dynamicspan;

import java.util.ArrayList;

public class DynamicSpanInfo 
{
	public static final int INDEX_NONE = -100000;
	public static final int INDEX_PRIMARY_SHEET = -1;
	
	public int substanceArray_Index = INDEX_NONE;
	public DynamicIterationSpan substanceArray_DS = null;
	
	public int substance_Indices[] = null;
	public DynamicIterationSpan substance_DS[] = null;
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("substanceArrayIndex = " + indexToMsgString(substanceArray_Index)+"\n");
		
		if (substance_Indices == null) 
			sb.append("substanceIndices = null");
		else
		{	
			sb.append("substanceIndices =");
			for (int i = 0; i < substance_Indices.length; i++)
				sb.append(" " + indexToMsgString(substance_Indices[i]));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public static String indexToMsgString(int index)
	{
		if (index == INDEX_NONE)
			return "INDEX_NONE";
		
		if (index == INDEX_PRIMARY_SHEET)
			return "INDEX_PRIMARY_SHEET";
		
		if (index >= 0)
			return (""+(index + 1));
		
		return "INDEX_NONE";
	}
	
	public static String  indexToSheetMessageString (int index)
	{
		if (index == INDEX_PRIMARY_SHEET)
			return ("Primary sheet");
		
		if (index >= 0)
			return ("Parallel sheet[" + (index +1) + "]");
			
		return "NONE";
	}
	
	public static String  indicesToSheetMessageString (ArrayList<Integer> indices)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i< indices.size(); i++)
			sb.append(indexToSheetMessageString(indices.get(i)) +  " ");
		return sb.toString();
	}
	
}
