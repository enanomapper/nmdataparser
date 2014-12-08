package net.enanomapper.parser;

import java.util.HashMap;


public class ProtocolApplicationDataLocation 
{
	public ExcelDataLocation citationTitle = null;
	public ExcelDataLocation citationYear = null;
	public ExcelDataLocation citationOwner = null;
	
	public ExcelDataLocation protocolTopCategory = null;
	public ExcelDataLocation protocolCategoryCode = null;
	public ExcelDataLocation protocolCategoryTitle = null;
	public ExcelDataLocation protocolEndpoint = null;
	public ExcelDataLocation protocolGuideline = null;  //TODO []?
	
	public HashMap<String, ExcelDataLocation> parameters = new HashMap<String, ExcelDataLocation>();
	
	public ExcelDataLocation reliability_isRobustStudy = null;
	public ExcelDataLocation reliability_isUsedforClassification = null;
	public ExcelDataLocation reliability_isUsedforMSDS = null;
	public ExcelDataLocation reliability_purposeFlag = null;
	public ExcelDataLocation reliability_studyResultType = null;
	public ExcelDataLocation reliability_value = null;
	
	public ExcelDataLocation interpretationResult = null;
	public ExcelDataLocation interpretationCriteria = null;
	
	public ExcelDataLocation effectsEndpoint = null;
	public HashMap<String, ExcelDataLocation> effectConditions = new HashMap<String, ExcelDataLocation>();
	public ExcelDataLocation effectsResultUnit = null;
	public ExcelDataLocation effectsLoValue = null;
	
	
	public String toJSONKeyWord(String offset)
	{	
		int nSections = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		if (citationTitle != null)
		{	
			sb.append(citationTitle.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (citationYear != null)
		{	
			if (nSections > 0)
				sb.append(",\n");
			sb.append(citationYear.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (citationOwner != null)
		{	
			if (nSections > 0)
				sb.append(",\n");
			sb.append(citationOwner.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		sb.append(offset + "}\n");
		return sb.toString();
	}	
	
}
