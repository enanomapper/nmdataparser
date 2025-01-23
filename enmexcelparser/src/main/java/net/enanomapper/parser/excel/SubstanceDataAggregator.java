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
import net.enanomapper.parser.excel.AggregatorParameter.AggregationTarget;
import net.enanomapper.parser.excel.AggregatorParameter.SubstanceElement;

public class SubstanceDataAggregator 
{
	public enum AggrationMode {
		DATA_BLOCKS, ROWS, DOSE_RESPONSE_TABLE, UNDEFINED;

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
	
	public List<AggregationBlock> blocks = new ArrayList<AggregationBlock>();
	public List<AggregationValueGroup> valueGroups = new  ArrayList<AggregationValueGroup>();
	public List<AggregatorParameter> aggregatorParameters = new  ArrayList<AggregatorParameter>();
	public List<AggregatorParameter> unregisteredParameters = new  ArrayList<AggregatorParameter>();
	public Map<String, String> expressions = new HashMap<String, String>();
	public List<String> errors = new ArrayList<String>();
			
	//work variable
	public double dataMatrix[][] = null;
	public SubstanceRecord curSubstance;
	public ProtocolApplication curPA;
	
	public SubstanceDataAggregator() {		
	}
	
	void addDefaultIOMBlock() {
		blocks.add(new AggregationBlock());
	}
	
	public void quickIOMConfiguration(int blockIndex, 
			String verticalSubblocksCondition, String horizontalSubblocksCondition,  
			String verticalConditions[], String horizontalConditions[],
			Object valueGroupsDefinitions[][]) 
	{
		//Add needed blocks up to blockIndexs, each block is on a separate sheet
		if (blockIndex > (blocks.size() - 1) ) {
			for (int i = blocks.size(); i <= blockIndex; i++) {
				AggregationBlock block = new AggregationBlock();
				block.sheetIndex = i;
				blocks.add(block);
			}
		}
		
		if (verticalSubblocksCondition != null) {
			AggregatorParameter aggPar = new AggregatorParameter(
					verticalSubblocksCondition, SubstanceElement.CONDITION, 
					AggregationTarget.SUBBLOCK, false);
			aggregatorParameters.add(aggPar);
		}
		
		if (horizontalSubblocksCondition != null) {
			AggregatorParameter aggPar = new AggregatorParameter(
					horizontalSubblocksCondition, SubstanceElement.CONDITION, 
					AggregationTarget.SUBBLOCK, true);
			aggregatorParameters.add(aggPar);
		}
		
		if (verticalConditions != null)	
			for (String s : verticalConditions) {
				if (s == null)
					continue;
				AggregatorParameter aggPar = new AggregatorParameter(s);
				aggregatorParameters.add(aggPar);
			}
		
		if (horizontalConditions != null)
			for (String s : horizontalConditions) {
				if (s == null)
					continue;
				AggregatorParameter aggPar = new AggregatorParameter(s);
				aggPar.isHorizontalOrientation = true;
				aggregatorParameters.add(aggPar);
			}
		
		if (valueGroupsDefinitions == null) {
			//No value groups are defined
			//Adding a default value group with all aggregation parameters
			AggregationValueGroup aggValGrp = new AggregationValueGroup();
			aggValGrp.endpointName = "endpoint";
			aggValGrp.parameters.addAll(aggregatorParameters);
			valueGroups.add(aggValGrp);
		}
		else {
			for (int i = 0; i < valueGroupsDefinitions.length; i++) 
			{
				//Value group definition is in the format:
				//endpointName, horizontalShift, verticalShift, aggrPar 1, aggrPar 2,...
				Object vgObj[] = valueGroupsDefinitions[i];
				if (vgObj == null || vgObj.length < 4) {
					errors.add("Incorrect quick IOM Configuration, value Groups Definitions #" + (i+1) 
							+ ", insufficient number of array elements");
					continue;
				}
				
				//Check correctness
				boolean FlagOK = true;
				for (int k = 0; k < vgObj.length; k++) 
				{
					if (vgObj[k] == null) {
						FlagOK = false;
						errors.add("Incorrect quick IOM Configuration, value Groups Definitions #" + (i+1) 
								+ ", null array element " + (k+1));
						continue;
					}
					if (k == 1 || k== 2) {
						if (!(vgObj[k] instanceof Integer)) {
							errors.add("Incorrect quick IOM Configuration, value Groups Definitions #" + (i+1) 
									+ ", array elements 2 and 3 must be of type integer!");
							continue;
						}	
					}
					else {
						if (!(vgObj[k] instanceof String)) {
							errors.add("Incorrect quick IOM Configuration, value Groups Definitions #" + (i+1) 
									+ ", array elements, except 2 and 3, must be of type String!");
							continue;
						}	
					}
					
					if (k >= 3) {
						//Check whether the aggregation parameter name is correct
						String parName = (String)vgObj[k];
						if (getAggregatorParameterByName(parName) == null) {
							errors.add("Incorrect quick IOM Configuration, value Groups Definitions #" + (i+1) 
									+ ", aggregation parameter " + parName + " is not defined!");
						}
					}
				}	
				if (!FlagOK ) 
					continue; //definition errors found
												
				AggregationValueGroup aggValGrp = new AggregationValueGroup();
				aggValGrp.endpointName = (String)vgObj[0];
				aggValGrp.horizontalShift = (Integer)vgObj[1];
				aggValGrp.verticalShift = (Integer)vgObj[2];
				
				for (int k = 3;  k < vgObj.length; k++) {
					String parName = (String)vgObj[k];
					AggregatorParameter aggPar = getAggregatorParameterByName(parName);
					aggValGrp.parameters.add(aggPar);
				}		
				valueGroups.add(aggValGrp);
			}
		}
		
	}
	
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
		blocks.clear();
		valueGroups.clear();
		aggregatorParameters.clear();
		expressions.clear();
		errors.clear();
	}
	
	public AggregatorParameter getAggregatorParameterByName(String name) {
		for (AggregatorParameter aggPar : aggregatorParameters)
			if (aggPar.name.equals(name))
				return aggPar;
		return null;
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
		for (AggregatorParameter par : aggregatorParameters)
			if (par.substanceElement == SubstanceElement.SUBSTANCE_NAME)
			{
				String name = rec.getSubstanceName();
				if (name != null)
					par.values.add(name);
			}
	}
	
	public void analyse(ProtocolApplication pa)
	{
		Params paParams = (Params) pa.getParameters();		
		Set keys = paParams.keySet();
		for (Object key: keys) 
		{
			Object val = paParams.get(key);
			boolean FlagRegisteredAggPar = false;
			for (AggregatorParameter aggPar : aggregatorParameters)
				if (aggPar.substanceElement == SubstanceElement.PROTOCOL_PARAMETER)
				{
					if (aggPar.name.equals(key)) {
						aggPar.values.add(val);
						FlagRegisteredAggPar = true;
					}	
				}
			
			if (!FlagRegisteredAggPar && FlagHandleUnregisteredElements)
			{
				//Handle unregistered protocol parameters
				boolean newUnregisteredAggPar = true;
				for (AggregatorParameter aggPar : unregisteredParameters)
					if (aggPar.substanceElement == SubstanceElement.PROTOCOL_PARAMETER)
					{
						if (aggPar.name.equals(key)) {
							aggPar.values.add(val);
							newUnregisteredAggPar = false;
						}	
					}
				
				if (newUnregisteredAggPar) {
					AggregatorParameter aggPar = new AggregatorParameter(key.toString(), SubstanceElement.PROTOCOL_PARAMETER);
					aggPar.values.add(val);
				}	
			}
				
		}
	}
	
	public void analyse(EffectRecord eff)
	{
		Params conditions = (Params) eff.getConditions();
		Set keys = conditions.keySet();
		for (Object key: keys) 
		{
			Object val = conditions.get(key);
			boolean FlagRegisteredAggPar = false;
			for (AggregatorParameter aggPar : aggregatorParameters)
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
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("FlagHandleUnregisteredElements = " + FlagHandleUnregisteredElements + "\n");
		sb.append("AggrationMode = " + aggrationMode + "\n");
		sb.append("startRow = " + startRow + "\n");
		sb.append("startColumn = " + startColumn + "\n");
		sb.append("rowSubblocks = " + rowSubblocks + "\n");
		//sb.append("columnSubblocks = " + columnSubblocks + "\n");
		//sb.append("subblockSizeRows = " + subblockSizeRows + "\n");
		//sb.append("subblockSizeColumns = " + subblockSizeColumns + "\n");
		//sb.append("aggregatorParameters = " + subblockSizeColumns + "\n");
		
		sb.append("aggregatorParameters = \n");
		for (AggregatorParameter aggPar : aggregatorParameters) {
			sb.append(aggPar.toString("    "));
		}
		
		sb.append("valueGroups = \n");
		for (AggregationValueGroup aggVG : valueGroups) {
			sb.append(aggVG.toString("    "));
		}
		
		//TODO blocks
		
		return sb.toString();
	}
}
