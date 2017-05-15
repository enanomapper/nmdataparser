package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;


public class ProtocolApplicationDataLocation 
{
	public ExcelDataLocation protocolApplicationUUID = null;
	public ExcelDataLocation investigationUUID = null;
	
	public ExcelDataLocation citationTitle = null;
	public ExcelDataLocation citationYear = null;
	public ExcelDataLocation citationOwner = null;
	
	public ExcelDataLocation protocolTopCategory = null;
	public ExcelDataLocation protocolCategoryCode = null;
	public ExcelDataLocation protocolCategoryTitle = null;
	public ExcelDataLocation protocolEndpoint = null;
	public ArrayList<ExcelDataLocation> protocolGuideline = null;  
	
	public HashMap<String, ExcelDataLocation> parameters = null;
	
	public ExcelDataLocation reliability_isRobustStudy = null;
	public ExcelDataLocation reliability_isUsedforClassification = null;
	public ExcelDataLocation reliability_isUsedforMSDS = null;
	public ExcelDataLocation reliability_purposeFlag = null;
	public ExcelDataLocation reliability_studyResultType = null;
	public ExcelDataLocation reliability_value = null;
	
	public ExcelDataLocation interpretationResult = null;
	public ExcelDataLocation interpretationCriteria = null;
	
	public ArrayList<EffectRecordDataLocation> effects = null;  //The other effect related fields to be removed
	
	public ArrayList<ExcelDataBlockLocation> effectsBlock = null; //Blocks of data used to define sets of effects measurements
														
	
	public String toJSONKeyWord(String offset)
	{	
		int nSections = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		if (protocolApplicationUUID != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(protocolApplicationUUID.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (investigationUUID != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			sb.append(investigationUUID.toJSONKeyWord(offset+"\t"));
			nSections++;
		}
		
		if (citationTitle != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
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
			
			sb.append(offset + "\t\"PROTOCOL_GUIDELINE\" : \n" );
			sb.append(offset + "\t{\n" );
			
			for (int i = 0; i < protocolGuideline.size(); i++)
			{
				ExcelDataLocation loc = protocolGuideline.get(i);
				sb.append(loc.toJSONKeyWord(offset+"\t\t"));
				
				if (i < protocolGuideline.size()-1)
					sb.append(",\n\n");
				else
					sb.append("\n");
			}
			
			sb.append(offset + "\t}" );
			nSections++;
		}
		
		
		if (parameters != null)
		{
			if (nSections > 0)
				sb.append(",\n\n");

			sb.append(offset + "\t\"PARAMETERS\" : \n" );
			sb.append(offset + "\t{\n" );
			
			int nParams = 0;
			for (String param : parameters.keySet())
			{	
				ExcelDataLocation loc = parameters.get(param);
				sb.append(loc.toJSONKeyWord(offset+"\t\t"));
				
				if (nParams < parameters.size())
					sb.append(",\n\n");
				else
					sb.append("\n");
				nParams++;
			}
			sb.append(offset + "\t}" );
		}
		
		
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
		
		
		if (effects != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");

			sb.append(offset + "\t\"EFFECTS\":\n");
			sb.append(offset + "\t[\n");

			for (int i = 0; i < effects.size(); i++)
			{	
				sb.append(effects.get(i).toJSONKeyWord(offset + "\t\t"));			
				if (i < effects.size()-1) 
					sb.append(",\n");
				sb.append("\n");
			}
			
			sb.append(offset + "\t]\n\n"); 
		}
		
		
		if (effectsBlock != null)
		{	
			if (nSections > 0)
				sb.append(",\n\n");
			 
			sb.append(offset+"\t" + "\"EFFECTS_BLOCK\":\n");
			sb.append(offset+"\t[\n");
			
			for (ExcelDataBlockLocation exEfBl: effectsBlock) 
				sb.append(exEfBl.toJSONKeyWord(offset+"\t\t", null, true) + "\n");
			
			sb.append(offset+"\t]\n");
			nSections++;
		}
		
		
		if (nSections > 0)
			sb.append("\n");
		
		sb.append(offset + "}\n");
		return sb.toString();
	}	
	
}
