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
		
		//TODO
		
		
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
	
	String[] getStringArray(String variable)
	{
		//TODO
		return null;
	}
	
}
