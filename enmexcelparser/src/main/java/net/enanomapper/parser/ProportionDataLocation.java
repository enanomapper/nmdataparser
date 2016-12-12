package net.enanomapper.parser;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;



public class ProportionDataLocation 
{
	public ExcelDataLocation function = null;
	
	public ExcelDataLocation typical_precision = null;  //analog to the qualifier
	public ExcelDataLocation typical_value = null;
	public ExcelDataLocation typical_unit = null;
	
	public ExcelDataLocation real_value = null;
	public ExcelDataLocation real_lower_precision = null;
	public ExcelDataLocation real_lower_value = null;
	public ExcelDataLocation real_upper_precision = null;
	public ExcelDataLocation real_upper_value = null;
	public ExcelDataLocation real_unit = null;
	
	
	public static ProportionDataLocation extractProportion(JsonNode node, ExcelParserConfigurator conf)
	{
		ProportionDataLocation pdl = new ProportionDataLocation();
		//JsonUtilities jsonUtils = new JsonUtilities();
		
		//FUNCTION
		ExcelDataLocation loc = ExcelDataLocation.extractDataLocation(node,"FUNCTION", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.function = loc;
		}
		
		//TYPICAL_PRECISION
		loc = ExcelDataLocation.extractDataLocation(node,"TYPICAL_PRECISION", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.typical_precision = loc;
		}
		
		//TYPICAL_VALUE
		loc = ExcelDataLocation.extractDataLocation(node,"TYPICAL_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.typical_value = loc;
		}
		
		//TYPICAL_UNIT
		loc = ExcelDataLocation.extractDataLocation(node,"TYPICAL_UNIT", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.typical_unit = loc;
		}
		
		//REAL_VALUE
		loc = ExcelDataLocation.extractDataLocation(node,"REAL_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.real_value = loc;
		}
		
		//REAL_LOWER_PRECISION
		loc = ExcelDataLocation.extractDataLocation(node,"REAL_LOWER_PRECISION", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.real_lower_precision = loc;
		}
		
		//REAL_LOWER_VALUE
		loc = ExcelDataLocation.extractDataLocation(node,"REAL_LOWER_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.real_lower_value = loc;
		}
		
		//REAL_UPPER_PRECISION
		loc = ExcelDataLocation.extractDataLocation(node,"REAL_UPPER_PRECISION", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.real_upper_precision = loc;
		}

		//REAL_UPPER_VALUE
		loc = ExcelDataLocation.extractDataLocation(node,"REAL_UPPER_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.real_upper_value = loc;
		}
		
		//REAL_UNIT
		loc = ExcelDataLocation.extractDataLocation(node,"REAL_UNIT", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				pdl.real_unit = loc;
		}

		return pdl;
	}
	
	
	public void setParallelSheets(ParallelSheetState parSheets[], int primarySheetNum, ArrayList<String> errors)
	{
		if (function != null)
			ExcelParserUtils.setParallelSheet(function, parSheets, primarySheetNum, errors);
		
		if (typical_precision != null)
			ExcelParserUtils.setParallelSheet(typical_precision, parSheets, primarySheetNum, errors);
		
		if (typical_value != null)
			ExcelParserUtils.setParallelSheet(typical_value, parSheets, primarySheetNum, errors);
		
		if (typical_unit != null)
			ExcelParserUtils.setParallelSheet(typical_unit, parSheets, primarySheetNum, errors);
		
		if (real_value != null)
			ExcelParserUtils.setParallelSheet(real_value, parSheets, primarySheetNum, errors);
		
		if (real_lower_precision != null)
			ExcelParserUtils.setParallelSheet(real_lower_precision, parSheets, primarySheetNum, errors);
		
		if (real_lower_value != null)
			ExcelParserUtils.setParallelSheet(real_lower_value, parSheets, primarySheetNum, errors);
		
		if (real_upper_precision != null)
			ExcelParserUtils.setParallelSheet(real_upper_precision, parSheets, primarySheetNum, errors);
		
		if (real_upper_value != null)
			ExcelParserUtils.setParallelSheet(real_upper_value, parSheets, primarySheetNum, errors);
		
		if (real_unit != null)
			ExcelParserUtils.setParallelSheet(real_unit, parSheets, primarySheetNum, errors);
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
		
		if (typical_precision != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(typical_precision.toJSONKeyWord(offset + "\t"));
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
		
		if (real_lower_precision != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(real_lower_precision.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (real_lower_value != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(real_lower_value.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		
		if (real_upper_precision != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(real_upper_precision.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (real_upper_value != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(real_upper_value.toJSONKeyWord(offset + "\t"));
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
