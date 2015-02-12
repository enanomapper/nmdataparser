package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;

import ambit2.base.data.SubstanceRecord;

public class DIOSynchronization 
{
	private SubstanceRecord basicRecord = null;
	private DynamicSpanInfo dsInfo = null;
	private HashMap<DynamicIterationSpan,DynamicIterationObject> dios = new HashMap<DynamicIterationSpan,DynamicIterationObject>();
	
	//Work variables
	private SubstanceRecord curRecord = null;
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
			assembleCurrentRecord();
			records.add(curRecord);
			return records;
		}
		
		
		if (dsInfo.substanceArray_DS == null)
		{
			//Currently only one substance DIS should is used
			DynamicIterationSpan substDIS = dsInfo.substance_DS[0];
			DynamicIterationObject substDIO = dios.get(substDIS);
			curRecord = (SubstanceRecord) substDIO.getObject();
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
		//TODO
		
		//temporary code
		curRecord.setCompanyName(GenericExcelParser.key00 + " #" + (curSubstArrIndex + 1));
	}
	
	
	protected ArrayList<SubstanceRecord> assembleRecordArray()
	{
		ArrayList<SubstanceRecord> records = new ArrayList<SubstanceRecord>();
		
		DynamicIterationSpan saDIS = dsInfo.substanceArray_DS;
		DynamicIterationObject saDIO = dios.get(saDIS);
		
		if (saDIO.groupDIOs.isEmpty())
		{
			//Handle rows
			//TODO
		}
		else
		{
			//Handle groups
			for (int i = 0; i < saDIO.groupDIOs.size(); i++)
			{
				curRecord = (SubstanceRecord) saDIO.groupDIOs.get(i).getObject();
				curSubstArrIndex = i;
				assembleCurrentRecord();
				records.add(curRecord);
			}
			
		}
		
		return records;
	}
	
	
}
