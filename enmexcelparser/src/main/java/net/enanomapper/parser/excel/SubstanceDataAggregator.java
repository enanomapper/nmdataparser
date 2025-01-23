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
	
	public enum DRTColumnType {
		CONDITION, ENDPOINT, MATERIAL, UNDEFINED;
		public static DRTColumnType fromString(String s) {
			try {
				DRTColumnType t = DRTColumnType.valueOf(s);
				return (t);
			} catch (Exception e) {
				return DRTColumnType.UNDEFINED;
			}
		}
	}
	
	//DOSE_RESPONSE_TEMLATE
	public static class DRTColumnInfo {
		public String name = null;
		public int index = 1;
		public DRTColumnType type = DRTColumnType.CONDITION;
		public DRTColumnInfo(String name, int index, DRTColumnType type) {
			this.name = name;
			this.index = index;
			this.type = type;
		}
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
	public List<DRTColumnInfo> resultDrtColumns = null;
	public List<DRTColumnInfo> rawDrtColumns = null;
	public List<String> errors = new ArrayList<String>();
			
	//work variable
	public List<double[]> rawDataMatrix = null;
	public List<double[]> resultDataMatrix = null;
	public SubstanceRecord curSubstance;
	public ProtocolApplication curPA;
	
	public SubstanceDataAggregator() {		
	}
	
	public SubstanceDataAggregator(List<DRTColumnInfo> rawDrtColumns, List<DRTColumnInfo> resultDrtColumns) {
		this.rawDrtColumns = rawDrtColumns;
		this.resultDrtColumns = resultDrtColumns;
		aggrationMode = AggrationMode.DOSE_RESPONSE_TABLE;
	}
	
	public static SubstanceDataAggregator parseDRTAggratorSetupFromString(String str) throws Exception {
		List<DRTColumnInfo> rawDrtColumns = new ArrayList<DRTColumnInfo>();
		List<DRTColumnInfo> resultDrtColumns = null;
		int rawPos = str.indexOf("RAW:");
		int resPos = str.indexOf("RESULT:");
		//System.out.println("rawPos = " + rawPos + "   resPos = " + resPos);
		if (rawPos < 0 && resPos < 0)
			throw new Exception("No RAW or RESULT section is present!");
		
		if (resPos >= 0 && rawPos >= resPos )
			throw new Exception("RAW: section is expected before RESULT: section!");
		
		String rawStr = null;
		String resStr = null;
		
		if (resPos < 0)
			rawStr = str.substring(rawPos + 4).trim();
		else {
			rawStr = str.substring(rawPos + 4, resPos).trim();
			resStr = str.substring(resPos + 7).trim();
		}
		
		if (rawStr != null) {
			System.out.println("RAW:" + rawStr);
			if (rawStr.isEmpty())
				throw new Exception("Incorrect empty RAW: section!");
			String tokens[] = rawStr.split(";");
			int nTok = tokens.length;
			if (tokens.length % 3 != 0)
				throw new Exception("Incorrect RAW: tokens number. It must be 3xn!");
			int k = 0;
			while (k < nTok) {
				DRTColumnInfo drtci = parseDRTColumnInfo(tokens[k].trim(), tokens[k+1].trim(), tokens[k+2].trim());
				if (drtci == null)
					throw new Exception("Incorrect triple for DRTColumnInfo: " + 
							tokens[k].trim() + " " + tokens[k+1].trim() + " " + tokens[k+2].trim());
				else
					rawDrtColumns.add(drtci);
				k += 3;
			}
		}
		
		if (resStr != null) {
			System.out.println("RESULT:" + resStr);
			if (resStr.isEmpty())
				throw new Exception("Incorrect empty RESULT: section!");
			String tokens[] = resStr.split(";");
			int nTok = tokens.length;
			if (tokens.length % 3 != 0)
				throw new Exception("Incorrect RESULT: tokens number. It must be 3xn!");
			
			resultDrtColumns = new ArrayList<DRTColumnInfo>();
			//TODO
		}	
		
		return new SubstanceDataAggregator(rawDrtColumns, resultDrtColumns);
		
	}
	
	static DRTColumnInfo parseDRTColumnInfo(String nameStr, String colStr, String typeStr) {
		int col = -1;
		try {
			col = Integer.parseInt(colStr);
		} catch (Exception e) {
			return null;
		}
		if (col < 0)
			return null;
		
		DRTColumnType type = DRTColumnType.fromString(typeStr);
		if (type == DRTColumnType.UNDEFINED)
			return null;
		
		return new DRTColumnInfo(nameStr, col, type);
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
		
		if (rawDrtColumns != null) {
			sb.append("rawDrtColumns = \n");
			for (DRTColumnInfo dstci: rawDrtColumns)
				sb.append("   " + dstci.name + ", " + dstci.index + ", " + dstci.type + "\n");
		}
		
		if (resultDrtColumns != null) {
			sb.append("resultDrtColumns = \n");
			for (DRTColumnInfo dstci: resultDrtColumns)
				sb.append("   " + dstci.name + ", " + dstci.index + ", " + dstci.type + "\n");
		}
		
		
		//TODO blocks
		
		return sb.toString();
	}
}
