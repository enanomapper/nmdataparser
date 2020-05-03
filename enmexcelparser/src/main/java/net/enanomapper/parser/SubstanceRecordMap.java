package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.ProtocolApplication;

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
	
	public void duplicateProtocolApplication(ProtocolApplication pa) 
	{
		for (String mapkey : mapKeys) {
			SubstanceRecord r = substances.get(mapkey);
			ProtocolApplication dup_pa = duplicate(pa);
			
			//check needed when adding the first protocol application
			if (r.getMeasurements() == null)
				r.setMeasurements(new ArrayList<ProtocolApplication>());
			
			r.getMeasurements().add(dup_pa);
		}	
		
	}
	
	ProtocolApplication duplicate(ProtocolApplication pa)
	{
		//The ProtocolApplication fields are not cloned. 
		//The same references are used in all duplicated copies
		ProtocolApplication dup_pa = new ProtocolApplication(pa.getProtocol());
		
		dup_pa.setDocumentUUID(pa.getDocumentUUID());
		dup_pa.setInvestigationUUID(pa.getInvestigationUUID());
		dup_pa.setAssayUUID(pa.getAssayUUID());	
		
		dup_pa.setReliability(pa.getReliability());
		dup_pa.setInterpretationResult(pa.getInterpretationResult());
		dup_pa.setInterpretationCriteria(pa.getInterpretationCriteria());
		dup_pa.setParameters(pa.getParameters());
		
		//Duplicate reference info
		if (pa.getReference() != null)
		{
			dup_pa.setReference(pa.getReference());
			
			String s = pa.getReferenceYear();
			if (s != null)
				dup_pa.setReferenceYear(s);
			
			s = pa.getReferenceOwner();
			if (s != null)
				dup_pa.setReferenceOwner(s);
		}
		
		
		
		return dup_pa;
	}
	
	public void dispatch(List<DataBlockElement> effDataBlock)
	{	
		for (DataBlockElement dbe : effDataBlock)
		{
			EffectRecord effect = dbe.generateEffectRecord();
			String substKey = dbe.substanceRecordMap;
			
			if (substKey == null || substKey.equals("ALL_SUBSTANCES") )
			{
				//Dispatch to all substances:
				for (String mapkey : mapKeys) {
					SubstanceRecord r = substances.get(mapkey);
					
					//Adding to the last protocol application
					ProtocolApplication pa = r.getMeasurements().get(r.getMeasurements().size()-1);
					pa.addEffect(effect);
				}	
			}
			else
			{	
				//Dispatch to a specific substance
				SubstanceRecord r = substances.get(substKey);
				if (r != null)
				{
					//Adding to the last protocol application
					ProtocolApplication pa = r.getMeasurements().get(r.getMeasurements().size()-1);
					pa.addEffect(effect);
				}
				else
				{
					//Error on SUBSTANCE_RECORD_MAP
					System.out.println("---------------> Error on SUBSTANCE_RECORD_MAP: " + substKey);
				}
			}
		}
		
		
	}
	
	/*
	public void dispatchToProtAppl(List<DataBlockElement> effDataBlock, ProtocolApplication pa)
	{
		for (DataBlockElement dbe : effDataBlock)
		{
			EffectRecord effect = dbe.generateEffectRecord();
			pa.addEffect(effect);
		}
	}
	*/
	
	
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
		strArrays = new HashMap<String, String[]>();
		
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
	
	public boolean hasErrors() {
		return (!errors.isEmpty());
	}
	
	public String getAllErrorsAsString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < errors.size(); i++)
			sb.append(errors.get(i) + "\n");
		return sb.toString();
	}
	
}
