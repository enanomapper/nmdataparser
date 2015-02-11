package net.enanomapper.parser;

import java.util.ArrayList;

import net.enanomapper.parser.DynamicIterationSpan.RowObject;
import ambit2.base.data.SubstanceRecord;

public class DynamicIterationObject 
{	
	public DynamicIterationSpan dynamicIterationSpan = null;  //pointer to the DynamicIterationSpan corresponding to this object
	
	public ArrayList<RowObject> rowObjects = new ArrayList<RowObject>(); 
	public ArrayList<DynamicIterationObject> groupDIOs = new ArrayList<DynamicIterationObject>(); 
	
	public ArrayList<SubstanceRecord> substanceRecords = null;
	public SubstanceRecord substanceRecord = null;
	
	public ArrayList<String> errors = new ArrayList<String>();
	
	
	public DynamicIterationObject(){
	}
	
	public DynamicIterationObject(DynamicIterationSpan dynamicIterationSpan){
		this.dynamicIterationSpan = dynamicIterationSpan;
	}
	
	
	public static ArrayList<SubstanceRecord> synchronize(ArrayList<DynamicIterationObject> diObjects, SubstanceRecord basicRecord)
	{
		ArrayList<SubstanceRecord> records = new ArrayList<SubstanceRecord>();
		
		//TODO - temporary code
		SubstanceRecord r = new SubstanceRecord();
		r.setCompanyName(GenericExcelParser.key00);
		records.add(r);
		
		return records;
	}
	
	/*
	 * Creates the basic object from the DynamicIterationObject
	 */
	public Object getObject()
	{
		
		
		return null;
	}
	
	
	
}
