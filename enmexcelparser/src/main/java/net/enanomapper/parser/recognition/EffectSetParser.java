package net.enanomapper.parser.recognition;

import java.util.ArrayList;

public class EffectSetParser 
{
	// Work variables
	protected ArrayList<String> errors = new ArrayList<String>();
	
	
	public ArrayList<String> getErrors() {
		return errors;
	}
	
	public String getAllErrorsAsString() {
		if (errors.isEmpty())
			return null;

		StringBuffer sb = new StringBuffer();
		for (String err : errors)
			sb.append(err + "\n");
		return sb.toString();
	}
	
	public EffectSet parse(String esStr)
	{
		errors.clear();

		if (esStr == null) {
			errors.add("Empty string!");
			return null;
		}
		
		EffectSet es = new EffectSet();
		
		
		//TODO
		return es;
	}
}
