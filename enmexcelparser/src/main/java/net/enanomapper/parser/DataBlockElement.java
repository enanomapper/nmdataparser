package net.enanomapper.parser;

import java.util.Map;

import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Params;
import net.enanomapper.parser.recognition.RichValue;
import net.enanomapper.parser.recognition.RichValueParser;


/**
 * This class is used to store the result from reading ExcelDataBlocks
 * This is a data associated to a single effect records
 * @author nick
 *
 */
public class DataBlockElement 
{	
	public Double loValue = null;
	public Double upValue = null;
	public String textValue = null;
	public Double error = null;
	public String loQualifier = null;
	public String upQualifier = null;
	public String errQualifier = null;
	public String unit = null;
	
	public IParams params = null;
	public String blockValueGroup = null;
	public String endpointType = null;
	public String substanceRecordMap = null;
	
	public void setValue(Object obj, RichValueParser rvParser)
	{
		if (obj == null)
			return;
		
		if (obj instanceof Double)
		{	
			loValue = (Double)obj;
			return;
		}
		
		if (obj instanceof String)
		{	
			RichValue rv = rvParser.parse((String) obj);
			String rv_error = rvParser.getAllErrorsAsString();

			if (rv_error == null) {
				if (rv.unit != null)
					unit = rv.unit;
				if (rv.loValue != null)
					loValue = rv.loValue;
				if (rv.loQualifier != null)
					loQualifier = rv.loQualifier;
				if (rv.upValue != null)
					upValue = rv.upValue;
				if (rv.upQualifier != null)
					upQualifier = rv.upQualifier;
			} 
			else
				textValue = (String) obj;
			
			return;
		}
	}
	
	public EffectRecord generateEffectRecord()
	{
		EffectRecord effect = new EffectRecord();
		
		effect.setEndpoint(blockValueGroup);   //blockValueGroup is used as an endpoint
		
		if (unit != null)
			effect.setUnit(unit);
		
		if (loValue != null)
			effect.setLoValue(loValue);
		
		if (loQualifier != null)
			effect.setLoQualifier(loQualifier);
		
		if (upValue != null)
			effect.setUpValue(upValue);
		
		if (upQualifier != null)
			effect.setUpQualifier(upQualifier);
		
		if (error != null)
			effect.setErrorValue(error);
		
		if (errQualifier != null)
			effect.setErrQualifier(errQualifier);
		
		if (textValue != null)
			effect.setTextValue(textValue);
		
		//Setting the effect conditions from parameters
		if (params != null)
			effect.setConditions(params);
		
		if (endpointType != null)
			effect.setEndpointType(endpointType);
		
		return effect;
	}
	
	
}
