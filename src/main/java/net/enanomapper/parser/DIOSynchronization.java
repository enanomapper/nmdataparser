package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;

import net.enanomapper.parser.DynamicIterationSpan.RowObject;
import ambit2.base.data.SubstanceRecord;

public class DIOSynchronization 
{
	private SubstanceRecord basicRecord = null;
	private DynamicSpanInfo dsInfo = null;
	private HashMap<DynamicIterationSpan,DynamicIterationObject> dios = new HashMap<DynamicIterationSpan,DynamicIterationObject>();
	
	//Work variables
	private SubstanceRecord curRecord = null;
	private DynamicIterationObject curDIO = null;
	private int curSubstArrIndex = -1;
	

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
		ArrayList<SubstanceRecord> records = new ArrayList<SubstanceRecord>();
		
		if (basicRecord != null)  //Dynamic span in this case supplies additional info to the primary (static) iteration method 
		{
			curRecord = basicRecord;
			curDIO = null;
			assembleCurrentRecord();
			records.add(curRecord);
			return records;
		}
		
		
		if (dsInfo.substanceArray_DS == null)
		{
			//Currently only one substance DIS should is used
			DynamicIterationSpan substanceDIS = dsInfo.substance_DS[0];
			curDIO = dios.get(substanceDIS);
			curRecord = (SubstanceRecord) curDIO.getObject();
			assembleCurrentRecord();
			records.add(curRecord);
			return records;
		}
		else
		{
			//Handle substance array
			records = assembleRecordArray();
			return records;
		}
		
		/*
		//temporary code
		SubstanceRecord r = new SubstanceRecord();
		r.setCompanyName(GenericExcelParser.key00);
		records.add(r);
		return records;
		*/
	}
	
	
	protected void assembleCurrentRecord()
	{	
		
		/*
		for (int i = 0; i < curDIO.rowObjects.size(); i++)
		{
			//Handle row objects
			
			
		}
		*/
		
		
		
		//TODO
		
		//temporary code
		curRecord.setCompanyName(GenericExcelParser.key00 + " #" + (curSubstArrIndex + 1));
	}
	
	protected void processRowObjects(DynamicIterationObject dio)
	{
		//TODO
	}
	
	
	boolean isNull (RowObject ro)
	{
		//TODO
		return true;
	}
	
	protected ArrayList<SubstanceRecord> assembleRecordArray()
	{
		ArrayList<SubstanceRecord> records = new ArrayList<SubstanceRecord>();
		
		DynamicIterationSpan saDIS = dsInfo.substanceArray_DS;
		DynamicIterationObject saDIO = dios.get(saDIS);
		
		if (saDIO.groupObjects.isEmpty())
		{
			//Handle rows
			//TODO
		}
		else
		{
			//Handle groups
			for (int i = 0; i < saDIO.groupObjects.size(); i++)
			{
				//curDIO = saDIO.groupDIOs.get(i);
				curRecord = new SubstanceRecord(); //(SubstanceRecord) curDIO.getObject();
				curSubstArrIndex = i;
				assembleCurrentRecord();
				records.add(curRecord);
			}
			
		}
		
		return records;
	}
	
	
	
	
}
