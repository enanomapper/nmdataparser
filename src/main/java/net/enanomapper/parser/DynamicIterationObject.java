package net.enanomapper.parser;

import java.util.ArrayList;

import ambit2.base.data.SubstanceRecord;

public class DynamicIterationObject 
{
	public int id = -1; //default which corresponds to the primary sheet 
	
	public ArrayList<SubstanceRecord> substanceRecords = null;
	public SubstanceRecord substanceRecord = null;
	
	public static ArrayList<SubstanceRecord> synchronize(ArrayList<DynamicIterationObject> diObjects, SubstanceRecord basicRecord)
	{
		ArrayList<SubstanceRecord> records = new ArrayList<SubstanceRecord>();
		
		//TODO - temporary code
		records.add(new SubstanceRecord());
		
		return records;
	}
	
}
