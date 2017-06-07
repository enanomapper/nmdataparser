package net.enanomapper.parser.recognition;

public class EffectSetPattern 
{
	private static String sectionName = "PATTERN";
	
	public boolean skipSpaces = true;
	public boolean FlagSkipSpaces = false;
	
	public String effectSeparator = ";";
	public boolean FlagEffectSeparator = false;
	
	public String internalSeparator = "=";
	public boolean FlaginternalSeparator = false;
	
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
		
		//TODO
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
	
	}
}
