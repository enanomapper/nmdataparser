package net.enanomapper.parser;

import java.util.HashMap;

public class EffectRecordDataLocation 
{
	public ExcelDataLocation sampleID = null;
	public ExcelDataLocation endpoint = null;
	public HashMap<String, ExcelDataLocation> conditions = null;
	public ExcelDataLocation unit = null;
	public ExcelDataLocation loValue = null;
	public ExcelDataLocation loQualifier = null;
	public ExcelDataLocation upValue = null;
	public ExcelDataLocation upQualifier = null;
	public ExcelDataLocation textValue = null;
	public ExcelDataLocation errValue = null;
	public ExcelDataLocation errQualifier = null;
	
	public ExcelDataLocation value = null; //It is read as a RichValue object and takes precedent over lo/up values and qualifiers
	
	
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
		
		if (loQualifier != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(loQualifier.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (upValue != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(upValue.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (upQualifier != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(upQualifier.toJSONKeyWord(offset+"\t"));
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
		
		if (errQualifier != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(errQualifier.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (value != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(value.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		
		if (nSections > 0)
			sb.append("\n");
		
		sb.append(offset + "}\n");
		return sb.toString();
	}	
	
}
