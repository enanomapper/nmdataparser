package net.enanomapper.parser.recognition;

public class EffectSetPattern 
{
	private static String sectionName = "PATTERN";
	
	public boolean skipSpaces = true;
	public boolean FlagSkipSpaces = false;
	
	public String effectSeparator = ";";
	public boolean FlagEffectSeparator = false;
	
	public String internalSeparator = "=";
	public boolean FlagInternalSeparator = false;
	
	public boolean endPointAtFirstPosition = true;
	public boolean FlagEndPointAtFirstPosition = false;
	
	public String endpointPrefix = null;
	public boolean FlagEndpointPrefix = false;
	
	public String endpointSuffix = null;
	public boolean FlagEndpointSuffix = false;
	
	public String valuePrefix = null;
	public boolean FlagValuePrefix = false;
	
	public String valueSuffix = null;
	public boolean FlagValueSuffix = false;
	
	public String unit = null;
	public boolean FlagUnit = false;
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"" + sectionName + "\":\n");
		sb.append(offset + "{\n");
		
		if (FlagSkipSpaces)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"SKIP_SPACES\" : " + skipSpaces);
			nFields++;
		}
		
		if (FlagEffectSeparator)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"EFFECT_SEPARATOR\" : \"" + effectSeparator + "\"");
			nFields++;
		}
		
		if (FlagInternalSeparator)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"INTERNAL_SEPARATOR\" : \"" + internalSeparator + "\"");
			nFields++;
		}
		
		if (FlagEndPointAtFirstPosition)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ENDPOINT_AT_FIRST_POSITION\" : " + endPointAtFirstPosition);
			nFields++;
		}
		
		if (FlagEndpointPrefix)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ENDPOINT_PREFIX\" : \"" + endpointPrefix + "\"");
			nFields++;
		}
		
		if (FlagEndpointSuffix)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"ENDPOINT_SUFFIX\" : \"" + endpointSuffix + "\"");
			nFields++;
		}
		
		if (FlagValuePrefix)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"VALUE_PREFIX\" : \"" + valuePrefix + "\"");
			nFields++;
		}
		
		if (FlagValueSuffix)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"VALUE_SUFFIX\" : \"" + valueSuffix + "\"");
			nFields++;
		}
		
		if (FlagUnit)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"UNIT\" : \"" + unit + "\"");
			nFields++;
		}
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
	}
}
