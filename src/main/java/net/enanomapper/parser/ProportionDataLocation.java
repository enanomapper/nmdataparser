package net.enanomapper.parser;

import net.enanomapper.parser.json.JsonUtilities;

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
		//JsonUtilities jsonUtils = new JsonUtilities();
		
		//FUNCTION
		ExcelDataLocation loc = ExcelParserConfigurator.extractDataLocation(node,"FUNCTION", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.function = loc;
		}
		
		//TYPICAL
		loc = ExcelParserConfigurator.extractDataLocation(node,"TYPICAL", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.typical = loc;
		}
		
		//TYPICAL_VALUE
		loc = ExcelParserConfigurator.extractDataLocation(node,"TYPICAL_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.typical_value = loc;
		}
		
		//TYPICAL_UNIT
		loc = ExcelParserConfigurator.extractDataLocation(node,"TYPICAL_UNIT", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.typical_unit = loc;
		}
		
		//REAL_VALUE
		loc = ExcelParserConfigurator.extractDataLocation(node,"REAL_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.real_value = loc;
		}
		
		//REAL_LOWER
		loc = ExcelParserConfigurator.extractDataLocation(node,"REAL_LOWER", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.real_lower = loc;
		}
		
		//REAL_LOWER_VALUE
		loc = ExcelParserConfigurator.extractDataLocation(node,"REAL_LOWER_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.real_lowervalue = loc;
		}
		
		//REAL_UPPER
		loc = ExcelParserConfigurator.extractDataLocation(node,"REAL_UPPER", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.real_upper = loc;
		}

		//REAL_UPPER_VALUE
		loc = ExcelParserConfigurator.extractDataLocation(node,"REAL_UPPER_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.real_uppervalue = loc;
		}
		
		//REAL_UNIT
		loc = ExcelParserConfigurator.extractDataLocation(node,"REAL_UNIT", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.real_unit = loc;
		}


		
		
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
