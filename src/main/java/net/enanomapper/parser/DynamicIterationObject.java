package net.enanomapper.parser;

import java.util.ArrayList;

import net.enanomapper.parser.ParserConstants.ObjectType;
import ambit2.base.data.SubstanceRecord;

public class DynamicIterationObject extends UniversalObject
{	
	public DynamicIterationSpan dynamicIterationSpan = null;  //pointer to the DynamicIterationSpan corresponding to this object
	public int groupIndex = -1; //if groupIndex >= 0 then this DIO is a group within dynamicIterationSpan
	
	public ArrayList<RowObject> rowObjects = new ArrayList<RowObject>(); 
	public ArrayList<GroupObject> groupObjects = new ArrayList<GroupObject>(); 
	
	public ArrayList<SubstanceRecord> substanceRecords = null;
	//public SubstanceRecord substanceRecord = null;
	
	public ArrayList<String> errors = new ArrayList<String>();
	
	
	public DynamicIterationObject(){
	}
	
	public DynamicIterationObject(DynamicIterationSpan dynamicIterationSpan){
		this.dynamicIterationSpan = dynamicIterationSpan;
	}
	
	
	
	/*
	 
	public Object getObject()
	{
		ElementDataType returnType = getResultObjectType();
			
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
	*/
	
	public ObjectType getResultObjectType()
	{
		ObjectType resType = dynamicIterationSpan.cumulativeObjectType;
		
		if (groupIndex >= 0)
		{	
			//Current DIO is from a group defined in dynamicIterationSpan
			resType = dynamicIterationSpan.groupLevels.get(0).groupCumulativeType;
		}
		return resType;
	}
	
	
	
	
}
