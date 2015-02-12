package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;

import net.enanomapper.parser.DynamicIterationSpan.RowObject;
import net.enanomapper.parser.ParserConstants.ElementDataType;
import ambit2.base.data.SubstanceRecord;

public class DynamicIterationObject 
{	
	public DynamicIterationSpan dynamicIterationSpan = null;  //pointer to the DynamicIterationSpan corresponding to this object
	public int groupIndex = -1; //if groupIndex >= 0 then this DIO is a group within dynamicIterationSpan
	
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
	
	
	public static ArrayList<SubstanceRecord> synchronize(HashMap<DynamicIterationSpan,DynamicIterationObject> diObjects, SubstanceRecord basicRecord, DynamicSpanInfo dsInfo)
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
		ElementDataType returnType = dynamicIterationSpan.cumulativeObjectType;
		
		if (groupIndex >= 0)
		{	
			//Current DIO is from a group defined in dynamicIterationSpan
			returnType = dynamicIterationSpan.groupLevels.get(0).groupCumulativeType;
		}	
			
		switch (returnType)
		{
		case SUBSTANCE_ARRAY:
			ArrayList<SubstanceRecord> list = new ArrayList<SubstanceRecord>();
			return list;
			
		case SUBSTANCE:
			SubstanceRecord r = new SubstanceRecord();
			return r;
		}
		
		return null;
	}
	
	
	
	
}
