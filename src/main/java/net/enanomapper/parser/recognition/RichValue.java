package net.enanomapper.parser.recognition;

import java.util.List;

public class RichValue {
	public Double loValue = null;
	public String loQualifier = null;
	public Double upValue = null;
	public String upQualifier = null;
	public String unit = null;
	public String errorMsg = null;

	public List<RichValue> additionalValues = null;
	/**
	 * 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (loValue != null)
			sb.append("loValue : " + loValue + "\n");
		if (loQualifier != null)
			sb.append("loQualifier : " + loQualifier + "\n");
		if (upValue != null)
			sb.append("upValue : " + upValue + "\n");
		if (upQualifier != null)
			sb.append("upQualifier : " + upQualifier + "\n");
		if (unit != null)
			sb.append("unit : " + unit + "\n");

		return sb.toString();
	}
}
