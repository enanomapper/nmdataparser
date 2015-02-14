package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.enanomapper.parser.DynamicIterationSpan.RowObject;
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
		ArrayList<SubstanceRecord> records = new ArrayList<SubstanceRecord>();
		
		if (basicRecord != null)  //Dynamic span in this case supplies additional info to the primary (static) iteration method 
		{
			curRecord = basicRecord;
			curRecords = null;
			primaryDIO = null;
			
			handleDIOs();
			
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
			
			handleDIOs();
			
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
				r.setCompanyName(GenericExcelParser.key00 + " #" + (i+1)); //temporary code
				records.add(r);
			}
		}
		
		return records;
	}
	
	
	protected void handleDIOs()
	{
		for (Entry<DynamicIterationSpan,DynamicIterationObject> entry :  dios.entrySet())
		{
			handleDIO(entry.getValue());
		}
		
		
		
		//TODO 
		
		//Second round ...
	}
	
	
	
	protected void handleDIO(DynamicIterationObject dio)
	{
		//TODO
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
	
	/*
	protected void assembleCurrentRecord()
	{	
		if (curDIO != null)
			for (int i = 0; i < curDIO.rowObjects.size(); i++)
			{
				//Handle row objects
			}
		
		//TODO
		
		//temporary code
		curRecord.setCompanyName(GenericExcelParser.key00 + " #" + (curSubstArrIndex + 1));
	}
	*/
	
	
}
