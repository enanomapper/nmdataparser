package net.enanomapper.parser;

import org.codehaus.jackson.JsonNode;

public class ProportionDataLocation 
{
	public ExcelDataLocation function = null;
	public ExcelDataLocation typical = null;
	public ExcelDataLocation typical_value = null;
	public ExcelDataLocation typical_unit = null;
	public ExcelDataLocation real_value = null;
	public ExcelDataLocation real_lower = null;
	public ExcelDataLocation real_lowervalue = null;
	public ExcelDataLocation real_upper = null;
	public ExcelDataLocation real_uppervalue = null;
	public ExcelDataLocation real_unit = null;
	
	
	public static ProportionDataLocation extractProportion(JsonNode node, ExcelParserConfigurator conf)
	{
		ProportionDataLocation pdl = new ProportionDataLocation();
		
		
		//TODO
		return pdl;
	}
	
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"PROPORTION\" : \n" );
		sb.append(offset + "{\n");
		
		if (function != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(function.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (typical != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(typical.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (typical_value != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(typical_value.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (typical_unit != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(typical_unit.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (real_value != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(real_value.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (real_lower != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(real_lower.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (real_lowervalue != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(real_lowervalue.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		
		if (real_upper != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(real_upper.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (real_uppervalue != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(real_uppervalue.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (real_unit != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(real_unit.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");

		return sb.toString();
	}
	
}
