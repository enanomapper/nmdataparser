package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.enanomapper.parser.ParserConstants.ElementSynchronization;
import ambit2.base.data.StructureRecord;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.relation.composition.CompositionRelation;
import ambit2.base.relation.composition.Proportion;

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
			checkRecords(records);
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
			checkRecords(records);
			return records;
		}
		else
		{
			//Handle substance array
			ArrayList<SubstanceRecord> records = assembleRecordArray();
			handleDIOs();
			checkRecords(records);
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
				r.setSubstanceName(GenericExcelParser.key00 + " #" + (i+1)); //temporary code
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
				r.setSubstanceName(GenericExcelParser.key00 + " #" + (i+1)); //temporary code
				records.add(r);
			}
		}
		
		primaryDIO.setSubstanceRecords(records);
		
		return records;
	}
	
	
	protected void handleDIOs()
	{
		//System.out.println("----numDIOs: " + dios.size());
		
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
		
		//(2) Synchronize result objects obtained from: rowObjects, groupObjects and DIOs
		
		//primary DIO is synchronized first
		synchResultObjects(primaryDIO); 
		
		//All other DIOs are synchronized
		for (Entry<DynamicIterationSpan,DynamicIterationObject> entry :  dios.entrySet())
		{
			DynamicIterationObject dio = entry.getValue();
			if (dio != primaryDIO)
				synchResultObjects(dio);
		}
		
	}
	
	protected void synchResultObjects(DynamicIterationObject dio)
	{	
		if (dio.groupObjects.isEmpty())     
		{	
			//Handle the row objects
			for (int i = 0; i < dio.rowObjects.size(); i++)
			{
				dio.rowObjects.get(i).selfDispatch();
				dispatchRowObject(dio.rowObjects.get(i),  dio, null);	
			}
		}
		else
		{
			//Handle the group objects
			for (int i = 0; i < dio.groupObjects.size(); i++)
			{	
				GroupObject groupObj =  dio.groupObjects.get(i);
				//Dispatch each row in the group
				if (groupObj.rowObjects != null)
					for (int k = 0; k < groupObj.rowObjects.length; k++)
					{
						groupObj.rowObjects[k].selfDispatch();
						dispatchRowObject(groupObj.rowObjects[k],  dio, groupObj);
					}
				
				groupObj.selfDispatch();
				dispatchGroupObject(groupObj,  dio);	
			}
		}
		
		//Handle the cumulative object
		dio.selfDispatch();
		dispatchDIO(dio);
		
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

			default: //cases: NONE, UNDEFINED	
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
	
	
	protected void dispatchElement(Object elObj, ElementSynchronization synchType, SynchronizationTarget synchTarget)
	{
		//TODO
	}
	
	
	protected void dispatchRowObject(RowObject rowObj, DynamicIterationObject dio, GroupObject groupObj)
	{
		DynamicIterationSpan dis = dio.dynamicIterationSpan;
		
		if (dis.rowSynchTarget != null)
			rowObj.dispatchTo(dis.rowSynch, dis.rowSynchTarget, this);
		else
		{
			switch (dis.rowSynch)
			{
			case PUT_IN_PRIMARY_CUMULATIVE_OBJECT:
				rowObj.dispatchTo(primaryDIO);
				break;
			
			case PUT_IN_CUMULATIVE_OBJECT:
				rowObj.dispatchTo(dio);
				break;
				
			case PUT_IN_EACH_CUMULATIVE_OBJECT:
				//TODO ???
				break;	
				
			case PUT_IN_GROUP:
				if (groupObj != null)
					rowObj.dispatchTo(groupObj);
				break;
			
			case PUT_IN_EACH_GROUP:
				if (!dio.groupObjects.isEmpty())
					for (int i = 0; i < dio.groupObjects.size(); i++)
						rowObj.dispatchTo(dio.groupObjects.get(i));
				break;	
				
			default:
				//PUT_IN_ELEMENT, PUT_IN_EACH_ELEMENT are not possible for this case
			}
		}
		
	}
	
	protected void dispatchGroupObject(GroupObject groupObj, DynamicIterationObject dio)
	{
		DynamicIterationSpan dis = dio.dynamicIterationSpan;
		
		if (dis.groupSynchTarget != null)
			groupObj.dispatchTo(dis.groupSynch, dis.groupSynchTarget, this);
		else
		{
			switch (dis.groupSynch)
			{
			case PUT_IN_PRIMARY_CUMULATIVE_OBJECT:
				groupObj.dispatchTo(primaryDIO);
				break;
			
			case PUT_IN_CUMULATIVE_OBJECT:
				groupObj.dispatchTo(dio);
				break;
				
			case PUT_IN_EACH_CUMULATIVE_OBJECT:
				//TODO ???
				break;	
			default:
			}
		}
	}
	
	protected void dispatchDIO(DynamicIterationObject dio)
	{
		DynamicIterationSpan dis = dio.dynamicIterationSpan;

		if (dis.cumulativeObjectSynchTarget != null)
			dio.dispatchTo(dis.cumulativeObjectSynch, dis.cumulativeObjectSynchTarget, this);
		else
		{
			//If cumulativeObjectSynchTarget is not present, cumulativeObjectSynch is not take into account
			
			if (dis.parallelToPrimary)
				parallelDispatch(dio);
			else
				dio.dispatchTo(primaryDIO);
		}
	}
	
	
	protected void parallelDispatch(DynamicIterationObject dio)
	{
		//System.out.println(">>>> parallelDispatch");
		if (dio.groupObjects.isEmpty())     
		{	
			//Handle the row objects
			for (int i = 0; i < dio.rowObjects.size(); i++)
			{
				//TODO
			}
		}
		else
		{
			//Handle the group objects
			if (primaryDIO.groupObjects.size() == dio.groupObjects.size()) //just a protection
				for (int i = 0; i < dio.groupObjects.size(); i++)
				{	
					GroupObject groupObj =  dio.groupObjects.get(i);
					//Dispatch each group to the parallel one from the primary DIO
					//System.out.println(">> par. group: " + (i+1) + "  " + groupObj.debugInfo(0));
					groupObj.dispatchTo(primaryDIO.groupObjects.get(i));
					
				}
		}
	}
	
	
	public void checkRecords(ArrayList<SubstanceRecord> records)
	{
		for (SubstanceRecord record : records )
			checkRecord(record);
	}
	
	public static void checkRecord(SubstanceRecord record)
	{
		if (record.getRelatedStructures() != null)
			for (CompositionRelation composition : record.getRelatedStructures())
			{
				check(composition);
			}
	}
	
	public static void check(CompositionRelation composition)
	{
		if (composition.getRelation() == null)
			composition.setRelation(new Proportion());
		
		if (composition.getSecondStructure() == null)
			composition.setSecondStructure(new StructureRecord());
	}
	
	
	
	
	
}
