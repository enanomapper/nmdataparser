package net.enanomapper.parser.recognition;

import java.util.List;

public class RichValue 
{	
	public Double loValue = null;
	public String loQualifier = null;
	public Double upValue = null;
	public String upQualifier = null;
	public String unit = null;
	public String errorMsg = null;
	
	public List<RichValue> additionalValues = null;
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		if (loValue != null)
			sb.append("loValue : " + loValue);
		if (loQualifier != null)
			sb.append("loQualifier : " + loQualifier);
		if (upValue != null)
			sb.append("upValue : " + upValue);
		if (upQualifier != null)
			sb.append("upQualifier : " + upQualifier);
		if (unit != null)
			sb.append("unit : " + unit);
		
		
		return sb.toString();
	}
}
