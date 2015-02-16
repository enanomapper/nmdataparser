package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import ambit2.base.data.SubstanceRecord;

public class DIOSynchronization 
{
	private SubstanceRecord basicRecord = null;
	private DynamicSpanInfo dsInfo = null;
	private HashMap<DynamicIterationSpan,DynamicIterationObject> dios = new HashMap<DynamicIterationSpan,DynamicIterationObject>();
	
	//Work variables
	private SubstanceRecord curRecord = null;
	private ArrayList<SubstanceRecord> curRecords = null;
	
	private DynamicIterationObject primaryDIO = null;  //It is used for the definition of SubstanceRecord or an array of SubstanceRecords
	

	public DIOSynchronization()
	{	
	}
	
	public DIOSynchronization(DynamicSpanInfo dsInfo)
	{	
		this.dsInfo = dsInfo;
	}
	
	public DIOSynchronization(SubstanceRecord basicRecord, DynamicSpanInfo dsInfo)
	{	
		this.dsInfo = dsInfo;
		this.basicRecord = basicRecord;
	}
	
	public SubstanceRecord getBasicRecord() {
		return basicRecord;
	}


	public void setBasicRecord(SubstanceRecord basicRecord) {
		this.basicRecord = basicRecord;
	}


	public DynamicSpanInfo getDsInfo() {
		return dsInfo;
	}


	public void setDsInfo(DynamicSpanInfo dsInfo) {
		this.dsInfo = dsInfo;
	}
	
	public HashMap<DynamicIterationSpan,DynamicIterationObject> getDios() {
		return dios;
	}

	public void setDios(HashMap<DynamicIterationSpan,DynamicIterationObject> dios) {
		this.dios = dios;
	}
	
	public void addDIO(DynamicIterationSpan dis, DynamicIterationObject dio)
	{
		dios.put(dis, dio);
	}
	
	public ArrayList<SubstanceRecord> synchronize()
	{	
		if (basicRecord != null)  //Dynamic span in this case supplies additional info to the primary (static) iteration method 
		{
			curRecord = basicRecord;
			curRecords = null;
			primaryDIO = null;
			
			handleDIOs();
			
			ArrayList<SubstanceRecord> records = new ArrayList<SubstanceRecord>();
			records.add(curRecord);
			return records;
		}
		
		
		if (dsInfo.substanceArray_DS == null)
		{
			//Currently only one substance DIS is used
			DynamicIterationSpan substanceDIS = dsInfo.substance_DS[0];
			primaryDIO = dios.get(substanceDIS);
			curRecord = new SubstanceRecord();
			curRecords = null;
			primaryDIO.substanceRecord = curRecord;
			
			handleDIOs();
			
			ArrayList<SubstanceRecord> records = new ArrayList<SubstanceRecord>();
			records.add(curRecord);
			return records;
		}
		else
		{
			//Handle substance array
			ArrayList<SubstanceRecord> records = assembleRecordArray();
			handleDIOs();
			return records;
		}
	}
	
	
	protected ArrayList<SubstanceRecord> assembleRecordArray()
	{
		ArrayList<SubstanceRecord> records = new ArrayList<SubstanceRecord>();
		
		DynamicIterationSpan saDIS = dsInfo.substanceArray_DS;
		primaryDIO = dios.get(saDIS);
		curRecords = records;
		curRecord = null;
		
		if (primaryDIO.groupObjects.isEmpty())
		{
			//Create SubstanceRecord for each row
			for (int i = 0; i < primaryDIO.rowObjects.size(); i++)
			{
				SubstanceRecord r = new SubstanceRecord(); 
				primaryDIO.rowObjects.get(i).substanceRecord = r;  
				r.setCompanyName(GenericExcelParser.key00 + " #" + (i+1)); //temporary code
				records.add(r);
			}
		}
		else
		{
			//Create SubstanceRecord for each group
			for (int i = 0; i < primaryDIO.groupObjects.size(); i++)
			{	
				SubstanceRecord r = new SubstanceRecord(); 
				primaryDIO.groupObjects.get(i).substanceRecord = r;
				r.setCompanyName(GenericExcelParser.key00 + " #" + (i+1)); //temporary code
				records.add(r);
			}
		}
		
		primaryDIO.setSubstanceRecords(records);
		
		return records;
	}
	
	
	protected void handleDIOs()
	{
		//(1) Synchronize the elements 
		
		//First round (phase = 0)
		for (Entry<DynamicIterationSpan,DynamicIterationObject> entry :  dios.entrySet())
		{
			handleDIO(entry.getValue(), 0);
		}
		
		//Second round (phase = 1) 
		for (Entry<DynamicIterationSpan,DynamicIterationObject> entry :  dios.entrySet())
		{
			handleDIO(entry.getValue(), 1);
		}
		
		//(2) Synchronize larger components: rowObjects, groupObjects and DIOs
		//TODO
		
	}
	
	
	
	protected void handleDIO(DynamicIterationObject dio, int phase)
	{
		if (primaryDIO.groupObjects.isEmpty())
		{
			//Handle the row objects
			for (int i = 0; i < dio.rowObjects.size(); i++)
				processRowObject(dio, dio.rowObjects.get(i), phase);
		}
		else
		{
			//Handle the group objects
			for (int i = 0; i < dio.groupObjects.size(); i++)
				processGroupObject(dio, dio.groupObjects.get(i), phase);
		}
	}
	
	protected void processRowObject(DynamicIterationObject dio, RowObject ro, int phase)
	{
		for (int i = 0; i < ro.elementObjects.length; i++)
			processElementObject(dio, null, ro, i, phase);
	}
	
	protected void processGroupObject(DynamicIterationObject dio, GroupObject go, int phase)
	{
		for (int i = 0; i < go.rowObjects.length; i++)
		{	
			RowObject ro = go.rowObjects[i];
			if (ro != null)
				for (int k = 0; k < ro.elementObjects.length; k++)
					processElementObject(dio, go, ro, k, phase); //??? call another special function
		}	
	}
	
	protected void processElementObject(DynamicIterationObject dio, GroupObject go, RowObject ro, int nElement, int phase)
	{
		DynamicElement de = dio.dynamicIterationSpan.elements.get(nElement);
		Object elObj = ro.elementObjects[nElement];
		
		if (phase == 0)
		{	
			if (de.synchTarget != null)
			{
				//TODO - eventually check synchTarget info
				//This element will be processed in phase 1
				return;
			}
			
			switch (de.synchType)
			{
			case PUT_IN_CUMULATIVE_OBJECT:
				de.putElementInUniversalObject(elObj, dio);
				break;

			case PUT_IN_GROUP:
				if (go != null)
					de.putElementInUniversalObject(elObj, go);
				break;

			case PUT_IN_ROW:
				de.putElementInUniversalObject(elObj, ro);
				break;

			default: //ceses: NONE, UNDEFINED	
				//does nothing
			}
		}
		else
		{ 
			//phase = 1
			if (de.synchTarget == null)
			{
				//This element is already processed in phase 0
				return;
			}
			
			
			//TODO
		}
		
	}
	
	
	
	
	
	
	boolean isNull (RowObject ro)
	{
		//TODO
		return true;
	}
	
	
}
