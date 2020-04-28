package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ambit2.base.data.SubstanceRecord;

public class SubstanceRecordMap 
{	
	protected Map<String,SubstanceRecord> substances = new HashMap<String,SubstanceRecord>();	
	protected List<String> errors = new ArrayList<String>(); 
	
	protected ExcelParserConfigurator config = null;
	protected HashMap<String, Object> curVariables = null;
	protected HashMap<String, HashMap<Object, Object>> curVariableMappings = null;
	protected String mapKeys[] = null;
	protected SubstanceRecordMapLocation subRecMapLoc = null;
	protected HashMap<String, String[]> strArrays = null;
	
	public Map<String, SubstanceRecord> getSubstances() {
		return substances;
	}

	public List<String> getErrors() {
		return errors;
	}
	
	public void init() {
		errors.clear();
	}

	public void setup(ExcelParserConfigurator config, 
				HashMap<String, Object> curVariables, HashMap<String, HashMap<Object, Object>> curVariableMappings)
	{
		this.config = config;
		this.curVariables = curVariables;
		this.curVariableMappings = curVariableMappings;
		subRecMapLoc = config.substanceRecordMap; 
		
		setMapKeys();
		fillStringArrays();
		
		if (mapKeys == null) {
			errors.add("Unable to create set of mapping keys");
			return;
		}				
		
		int n = mapKeys.length;
		
		if (n == 0){
			errors.add("Empty set of mapping keys");
			return;
		}
		
		for (int i = 0; i < n; i++)
		{
			SubstanceRecord rec =  setupSubstanceRecord(i);
			if (rec != null)
				substances.put(mapKeys[i], rec);
		}
		
	}
	
	void setMapKeys()
	{	
		if (subRecMapLoc.mapElement.equals(KEYWORD.SUBSTANCE_NAME.name()))
		{	
			if (subRecMapLoc.substanceNameVariable != null)
				mapKeys =  getStringArray(subRecMapLoc.substanceNameVariable);
			else if (subRecMapLoc.substanceNameArray != null)
				mapKeys = subRecMapLoc.substanceNameArray;
		}
		
		if (subRecMapLoc.mapElement.equals(KEYWORD.PUBLIC_NAME.name()))
		{	
			if (subRecMapLoc.publicNameVariable != null)
				mapKeys =  getStringArray(subRecMapLoc.publicNameVariable);
			else if (subRecMapLoc.publicNameArray != null)
				mapKeys = subRecMapLoc.publicNameArray;
		} 
		
	}
	
	SubstanceRecord setupSubstanceRecord(int subIndex) 
	{
		//Filling data from strArray into substance
		SubstanceRecord rec = new SubstanceRecord(); 
		String s[];
		
		s = strArrays.get(KEYWORD.SUBSTANCE_NAME.name());
		if (s != null)
			if (subIndex < s.length)
				if (s[subIndex] != null)
					rec.setSubstanceName(s[subIndex]);
		
		s = strArrays.get(KEYWORD.PUBLIC_NAME.name());
		if (s != null)
			if (subIndex < s.length)
				if (s[subIndex] != null)
					rec.setPublicName(s[subIndex]);
		
		
		return rec;
	}
	
	void fillStringArrays() 
	{
		HashMap<String, Object> strArrays = new HashMap<String, Object>();
		
		String s[] = getStringArray(subRecMapLoc.substanceNameVariable, subRecMapLoc.substanceNameArray);
		if (s != null)
			strArrays.put(KEYWORD.SUBSTANCE_NAME.name(), s);
		
		s = getStringArray(subRecMapLoc.publicNameVariable, subRecMapLoc.publicNameArray);
		if (s != null)
			strArrays.put(KEYWORD.PUBLIC_NAME.name(), s);
		
	}
	
	
	String[] getStringArray(String variable, String strArray[])
	{
		if (variable != null)
			return getStringArray(variable);
		else
			return strArray;
	}
	
	
	String[] getStringArray(String variable)
	{
		Object o = curVariables.get(variable);
		if (o instanceof Object[])
			return SubstanceRecordMapLocation.objectArrayToStringArray ((Object[]) o);
		
		return null;
	}
	
}
