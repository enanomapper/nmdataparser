package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.IRawReader;

public class SubstanceDataAggregator 
{
	public enum AggrationMode {
		DATA_BLOCKS, ROWS, UNDEFINED;

		public static AggrationMode fromString(String s) {
			try {
				AggrationMode aggMod = AggrationMode.valueOf(s);
				return (aggMod);
			} catch (Exception e) {
				return AggrationMode.UNDEFINED;
			}
		}
	}
	
	public enum IterationTask {
		ANALYSE, AGGREGATE, DATA_SIMULATION
	}

	public AggrationMode aggrationMode = AggrationMode.UNDEFINED;
	public int rowSubblocks = 1;
	public int columnSubblocks = 1;
	public int subblockSizeRows = 1;
	public int subblockSizeColumns = 1;	
	
	public List<AggregationValueGroup> valueGroups = new  ArrayList<AggregationValueGroup>();
	public List<AggregatorParameter> agregatorParameters = new  ArrayList<AggregatorParameter>();
	public Map<String, String> expressions = new HashMap<String, String>();
	
	public double dataMatrix[][] = null;
	
	
	public void aggregate(IRawReader<IStructureRecord> substanceIterator)
	{
		//TODO
	}
	
	public void aggregate(List<SubstanceRecord> substanceList)
	{
		//TODO
	}
	
	public void simulateData()
	{
		//TODO
	}
	
	
	public void iterate(IRawReader<IStructureRecord> substanceIterator, IterationTask itTask)
	{
		if (substanceIterator == null)
			return;

		while (substanceIterator.hasNext()) {
			SubstanceRecord r = (SubstanceRecord)substanceIterator.nextRecord();
			iterate (r, itTask);
		}
	}
	
	public void iterate(List<SubstanceRecord> substanceList, IterationTask itTask)
	{		
		for (SubstanceRecord r : substanceList) {
			iterate (r, itTask);
		}
	}

	public void iterate(SubstanceRecord rec, IterationTask itTask) 
	{
		List<ProtocolApplication> paList = rec.getMeasurements();
		for (ProtocolApplication pa : paList) 
		{
			iterate(pa, itTask);
		}	
	}


	public void iterate(ProtocolApplication pa, IterationTask itTask)
	{
		List<EffectRecord> effects = pa.getEffects();
		for (EffectRecord eff : effects)
		{
			iterate(eff, itTask);
		}
	}

	public void iterate(EffectRecord eff, IterationTask itTask)
	{
		switch (itTask) {
		case ANALYSE:
			//TODO
			break;
		case AGGREGATE:
			//TODO
			break;
		
		case DATA_SIMULATION:
			//TODO
			break;
		}
	}
	
	public void exportDataMatrixToCSV(String fileName) throws Exception 
	{
		//TODO
	}
	
	public void handleExpressions()
	{
		//TODO
	}
}
