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
				sb.append(",\n\n");
			sb.append(citationYear.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (citationOwner != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(citationOwner.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (protocolTopCategory != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(protocolTopCategory.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		
		if (protocolCategoryCode != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(protocolCategoryCode.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (protocolCategoryTitle != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(protocolCategoryTitle.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		
		if (protocolEndpoint != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(protocolEndpoint.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		
		if (protocolGuideline != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(protocolGuideline.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		 
		
		//TODO  HashMap<String, ExcelDataLocation> parameters
	
		
		if (reliability_isRobustStudy != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(reliability_isRobustStudy.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (reliability_isUsedforClassification != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(reliability_isUsedforClassification.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (reliability_isUsedforMSDS != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(reliability_isUsedforMSDS.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		
		if (reliability_purposeFlag != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(reliability_purposeFlag.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (reliability_studyResultType != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(reliability_studyResultType.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (reliability_value != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(reliability_value.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (interpretationResult != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(interpretationResult.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (interpretationCriteria != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(interpretationCriteria.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (effectsEndpoint != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(effectsEndpoint.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (effectsResultUnit != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(effectsResultUnit.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (effectsLoValue != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(effectsLoValue.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
	
		
		//TODO  HashMap<String, ExcelDataLocation> effectConditions 

		
		if (nSections > 0)
			sb.append("\n");
		
		sb.append(offset + "}\n");
		return sb.toString();
	}	
	
}
