package net.enanomapper.parser.excel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.IRawReader;
import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.excel.AggregatorParameter.SubstanceElement;

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

	public boolean FlagHandleUnregisteredElements = false;
	
	public AggrationMode aggrationMode = AggrationMode.UNDEFINED;
	public int startRow = 1;
	public int startColumn = 1;
	public int rowSubblocks = 1;
	public int columnSubblocks = 1;
	public int subblockSizeRows = 1;
	public int subblockSizeColumns = 1;	
	
	public List<AggregationValueGroup> valueGroups = new  ArrayList<AggregationValueGroup>();
	public List<AggregatorParameter> agregatorParameters = new  ArrayList<AggregatorParameter>();
	public List<AggregatorParameter> unregisteredParameters = new  ArrayList<AggregatorParameter>();
	public Map<String, String> expressions = new HashMap<String, String>();
		
	//work variable
	public double dataMatrix[][] = null;
	public SubstanceRecord curSubstance;
	public ProtocolApplication curPA;
	
		
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
	
	
	public void reset() {
		valueGroups.clear();
		agregatorParameters.clear();
		expressions.clear();
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
		curSubstance = rec;
		
		switch (itTask) {
		case ANALYSE:
			analyse(rec);
			break;
		case AGGREGATE:
			aggregate(rec);
			break;
		case DATA_SIMULATION:
			//TODO
			break;
		}
		
		List<ProtocolApplication> paList = rec.getMeasurements();
		for (ProtocolApplication pa : paList) 
		{
			iterate(pa, itTask);
		}	
	}


	public void iterate(ProtocolApplication pa, IterationTask itTask)
	{
		curPA = pa;
		
		switch (itTask) {
		case ANALYSE:
			analyse(pa);
			break;
		case AGGREGATE:
			aggregate(pa);
			break;
		case DATA_SIMULATION:
			//TODO
			break;
		}
		
		List<EffectRecord> effects = pa.getEffects();
		for (EffectRecord eff : effects)
			iterate(eff, itTask);
	}

	public void iterate(EffectRecord eff, IterationTask itTask)
	{
		switch (itTask) {
		case ANALYSE:
			analyse(eff);
			break;
		case AGGREGATE:
			aggregate(eff);
			break;
		case DATA_SIMULATION:
			//TODO
			break;
		}
	}
		
	public void analyse(SubstanceRecord rec)
	{
		for (AggregatorParameter par : agregatorParameters)
			if (par.substanceElement == SubstanceElement.SUBSTANCE_NAME)
			{
				String name = rec.getSubstanceName();
				if (name != null)
					par.values.add(name);
			}
	}
	
	public void analyse(ProtocolApplication pa)
	{
		/*
		for (AggregatorParameter par : agregatorParameters)
			if (par.substanceElement == SubstanceElement.PROTOCOL_PARAMETER)
			{
				//TODO
			}
		*/	
	}
	
	public void analyse(EffectRecord eff)
	{
		Params conditions = (Params) eff.getConditions();
		Set keys = conditions.keySet();
		for (Object key: keys) 
		{
			Object val = conditions.get(key);
			boolean FlagRegisteredAggPar = false;
			for (AggregatorParameter aggPar : agregatorParameters)
				if (aggPar.substanceElement == SubstanceElement.CONDITION)
				{
					if (aggPar.name.equals(key)) {
						aggPar.values.add(val);
						FlagRegisteredAggPar = true;
					}	
				}
			
			if (!FlagRegisteredAggPar && FlagHandleUnregisteredElements)
			{
				//Handle unregistered conditions
				boolean newUnregisteredAggPar = true;
				for (AggregatorParameter aggPar : unregisteredParameters)
					if (aggPar.substanceElement == SubstanceElement.CONDITION)
					{
						if (aggPar.name.equals(key)) {
							aggPar.values.add(val);
							newUnregisteredAggPar = false;
						}	
					}
				
				if (newUnregisteredAggPar) {
					AggregatorParameter aggPar = new AggregatorParameter(key.toString(), SubstanceElement.CONDITION);
					aggPar.values.add(val);
				}	
			}
				
		}
	}
	
	public void aggregate(SubstanceRecord rec)
	{
		//TODO
	}
	
	public void aggregate(ProtocolApplication pa)
	{
		//TODO
	}
	
	public void aggregate(EffectRecord eff)
	{
		//TODO
	}
	
	
	public void exportDataMatrixToCSV(String fileName) throws Exception 
	{
		//TODO
	}
	
	public void handleExpressions()
	{
		//TODO
	}
	
	public static SubstanceDataAggregator generateAggregator(ExcelParserConfigurator config)
	{
		//TIODO
		return null;
	}
	
	public static SubstanceDataAggregator generateAggregator(File jsonConfig)
	{
		//TIODO
		return null;
	}
}
