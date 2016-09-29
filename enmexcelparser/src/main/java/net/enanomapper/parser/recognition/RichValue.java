package net.enanomapper.parser.recognition;

import java.util.List;

import ambit2.base.data.study.IValue;
import ambit2.base.data.study.Value;

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
	
	public IValue toValue()
	{
		IValue value = new Value();
		if (loValue != null)
			value.setLoValue(loValue);
		
		if (loQualifier != null)
			value.setLoQualifier(loQualifier);
		
		if (upValue != null)
			value.setUpValue(upValue);
		
		if (upQualifier != null)
			value.setUpQualifier(upQualifier);
		
		if (unit != null)
			value.setUnits(unit);
		
		return value;
	}
	
	
	public static Object recognizeRichValueFromObject(Object obj, RichValueParser rvParser)
	{
		return recognizeRichValueFromObject(obj, null, rvParser);
	}
	
	public static Object recognizeRichValueFromObject(Object obj, String defaultUnit, RichValueParser rvParser)
	{	
		if (obj == null)
			return null;
		
		
		if (obj instanceof Number)
		{	
			if (defaultUnit == null)
				return obj;
			else
			{
				IValue value = new Value();
				value.setLoQualifier("=");
				value.setLoValue(obj);
				value.setUnits(defaultUnit);
				return value;
			}
		}
		
		if (obj instanceof String)
		{
			RichValue rv = rvParser.parse(obj.toString());
			String rv_error = rvParser.getAllErrorsAsString();
			if (rv_error == null)
			{	
				IValue value = rv.toValue();
				if (value.getUnits() == null)
					if (defaultUnit != null)
						value.setUnits(defaultUnit);
				return value;
			}
			else
			{	
				return obj; //return the result as String
			}	
		}
		
		return null;	
	}
}
