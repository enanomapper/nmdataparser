package net.enanomapper.parser;

import java.util.HashMap;

public class EffectRecordDataLocation 
{
	public ExcelDataLocation sampleID = null;
	public ExcelDataLocation endpoint = null;
	public HashMap<String, ExcelDataLocation> conditions = null;
	public ExcelDataLocation unit = null;
	public ExcelDataLocation loValue = null;
	public ExcelDataLocation upValue = null;
	public ExcelDataLocation textValue = null;
	public ExcelDataLocation errValue = null;
	
	
	
	public String toJSONKeyWord(String offset)
	{	
		int nSections = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		
		if (sampleID != null)
		{	
			sb.append(sampleID.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (endpoint != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(endpoint.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		
		if (conditions != null)
		{
			if (nSections > 0)
				sb.append(",\n\n");

			sb.append(offset + "\t\"CONDITIONS\" : \n" );
			sb.append(offset + "\t{\n" );
			
			int nEffCond = 0;
			for (String effCond : conditions.keySet())
			{	
				ExcelDataLocation loc = conditions.get(effCond);
				sb.append(loc.toJSONKeyWord(offset+"\t\t"));
				
				if (nEffCond < conditions.size() - 1)
					sb.append(",\n\n");
				else
					sb.append("\n");
				nEffCond++;
			}
			sb.append(offset + "\t}" );
			nSections++;
		}
		
		
		if (unit != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(unit.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (loValue != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(loValue.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (upValue != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(upValue.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (textValue != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(textValue.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (errValue != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(errValue.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		
		if (nSections > 0)
			sb.append("\n");
		
		sb.append(offset + "}\n");
		return sb.toString();
	}	
	
}
