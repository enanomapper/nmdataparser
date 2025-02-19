package net.enanomapper.parser.excel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.IValue;
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
				//Handle short synonymous
				if (s.equals("CON"))
					return DRTColumnType.CONDITION;
				if (s.equals("EP"))
					return DRTColumnType.ENDPOINT;
				if (s.equals("MAT"))
					return DRTColumnType.MATERIAL;
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
	public Map<String, List<DRTColumnInfo>> epDrtColumns = null;  //DRTColumnInfo for various endpoints
	public List<String> endpointList = null; //List of endpoints to be aggregated (if null all endpoints are considered)
	public List<String> errors = new ArrayList<String>();
			
	//work variable	
	public Map<String, List<Object[]>> dataMatrices = new HashMap<String, List<Object[]>>();
	public SubstanceRecord curSubstance;
	public ProtocolApplication curPA;
	
	public SubstanceDataAggregator() {		
	}
	
	public SubstanceDataAggregator(Map<String, List<DRTColumnInfo>> epDrtColumns) {
		this.epDrtColumns = epDrtColumns;		
		aggrationMode = AggrationMode.DOSE_RESPONSE_TABLE;
	}
	
	public SubstanceDataAggregator(Map<String, List<DRTColumnInfo>> epDrtColumns, List<String> endpointList) {
		this.epDrtColumns = epDrtColumns;
		this.endpointList = endpointList; 
		aggrationMode = AggrationMode.DOSE_RESPONSE_TABLE;
	}
	
	public static SubstanceDataAggregator parseDRTAggratorSetupFromString(String str) throws Exception 
	{
		Map<String, List<DRTColumnInfo>> mapDrtColumns = new HashMap<String, List<DRTColumnInfo>>();
		List<String> endpointList = null;
		//Find all occurrences of "##"
		int lastIndex = 0;
		List<Integer> positions = new ArrayList<Integer>();
		while(lastIndex != -1) {
			lastIndex = str.indexOf("##",lastIndex);
			if(lastIndex != -1){
				positions.add(lastIndex);
				lastIndex += 2;
			}
		}
		
		if (positions.isEmpty())
			throw new Exception("No endpoint types defined. Use follwoitn syntax: ##ENDPOINT_TYPE: ...");
		
		for (int i = 0; i < positions.size(); i++) 
		{
			//Handle endpoint type
			int pos = positions.get(i);
			String epStr;
			if (i == positions.size() -1)
				epStr = str.substring(pos+2);
			else
				epStr = str.substring(pos+2, positions.get(i+1));
			
			int semiColPos = epStr.indexOf(":");
			if (semiColPos == -1 )
				throw new Exception("Incorrect endpoint section\n" + epStr);
			
			String epType = epStr.substring(0,semiColPos).trim();
			if (epType.isEmpty())
				throw new Exception("Incorrect endpoint section\n" + epStr);
			
			//Check for reserved words:
			if (epType.equals("ENDPOINT_LIST")) {
				String epListStr = epStr.substring(semiColPos+1).trim();
				endpointList = parseEndpointList(epListStr);
				continue;
			}
			
			//Parse column info
			String ciStr = epStr.substring(semiColPos+1).trim();			
			List<DRTColumnInfo> drtColumns = parseDRTColumnInfoFromString(ciStr, epType);
			
			mapDrtColumns.put(epType, drtColumns);
		}
		
		return new SubstanceDataAggregator(mapDrtColumns, endpointList);	
	}
	
	public static List<DRTColumnInfo> parseDRTColumnInfoFromString(String ciStr, String endpointType) throws Exception 
	{
		List<DRTColumnInfo> drtColumns = new ArrayList<DRTColumnInfo>();
				
		String tokens[] = ciStr.split(";");
		int nTok = tokens.length;
		if (tokens.length % 3 != 0)
			throw new Exception("Incorrect " + endpointType + ": tokens number. It must be 3xn!");
		int k = 0;
		while (k < nTok) {
			DRTColumnInfo drtci = parseDRTColumnInfo(tokens[k].trim(), tokens[k+1].trim(), tokens[k+2].trim());
			if (drtci == null)
				throw new Exception("For " + endpointType + ": Incorrect triple for DRTColumnInfo: " + 
						tokens[k].trim() + "; " + tokens[k+1].trim() + "; " + tokens[k+2].trim());
			else
				drtColumns.add(drtci);
			k += 3;
		}		
		return drtColumns;
	}
	
	public static List<String> parseEndpointList(String epListStr) throws Exception 
	{
		List<String> epList = new ArrayList<String>();
		String tokens[] = epListStr.split(";");
		for (String tok : tokens) {
			String ep = tok.trim();
			if (ep.isEmpty())
				throw new Exception("Incorrect ##ENDPOINT_LIST: " + epListStr);
			else
				epList.add(ep);
		}	
		return epList;
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
		
		if (aggrationMode == AggrationMode.DOSE_RESPONSE_TABLE) {
			dataMatrices.clear();
		}
			
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
		System.out.println("Iterating: " + rec.getPublicName());
		
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
		for (ProtocolApplication pa : paList) {
			iterate(pa, itTask);
		}	
	}


	public void iterate(ProtocolApplication pa, IterationTask itTask)
	{
		curPA = pa;
		//System.out.println("  Prot.App: " + pa.getProtocol());
		
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
		if (effects != null)
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
		//currently nothing is done
	}
	
	public void aggregate(ProtocolApplication pa)
	{
		//currently nothing is done
	}
	
	public void aggregate(EffectRecord eff)
	{
		if (aggrationMode == AggrationMode.DOSE_RESPONSE_TABLE) 
		{
			//System.out.println("*****" + eff.asJSON());
			IParams conditions = (IParams)eff.getConditions();			
			String endpointType = eff.getEndpointType();
			String endpoint = eff.getEndpoint().toString();
			
			if (endpointList != null) {
				//check endpoint
				if (!endpointList.contains(endpoint))
					return;
			}
			
			if (endpointType != null) {
				List<DRTColumnInfo> drtColumns = epDrtColumns.get(endpointType);
				if (drtColumns != null) 
				{					
					Object dataArray[] = new Object[drtColumns.size()];
					//Set material
					DRTColumnInfo matCol = getMaterialColumn(drtColumns);
					dataArray[matCol.index-1] = curSubstance.getPublicName();
					//Set effect value					
					DRTColumnInfo epCol = findColumnByName(endpoint, drtColumns);
					dataArray[epCol.index-1] = eff.getLoValue();
					//Set conditions
					for (Object key: conditions.keySet()) {
						DRTColumnInfo condCol = findColumnByName(key.toString(), drtColumns);
						if (condCol == null)
							continue;
						Object cond = conditions.get(key);
						//System.out.println(key.toString() + ":" + extractValueAsString(cond));
						dataArray[condCol.index-1] = extractValueAsString(cond);					
					}
					String data_s = dataArrayToString(dataArray, ", ");
					System.out.println(data_s + "  " + endpoint + "  " + endpointType);
					//TODO calculate "material + condition signature" + Map use to agragate all endpoint value to a single row
				}
			}
			
		}
		
		//TODO other modes
	}
	
	public DRTColumnInfo findColumnByName(String colName, List<DRTColumnInfo> columns) {
		for (DRTColumnInfo ci : columns) {
			if (ci.name.equals(colName))
				return ci;
		}
		return null;
	}
	
	public DRTColumnInfo getMaterialColumn(List<DRTColumnInfo> columns) {
		for (DRTColumnInfo ci : columns) {
			if (ci.type == DRTColumnType.MATERIAL)
				return ci;
		}
		return null;
	}
	
	public String extractValueAsString (Object o) {
		if (o instanceof IValue) {
			//In this case the unit and qualifier information is omitted
			IValue v = (IValue)o;
			return v.getLoValue().toString();
		}
		else
			return o.toString();
	}
	
	public String dataArrayToString(Object dataArray[], String sep) {
		return dataArrayToString(dataArray, dataArray.length, sep);
	}
	
	public String dataArrayToString(Object dataArray[], int lastIndex, String sep) {
		StringBuffer sb = new StringBuffer();
		int n = dataArray.length;
		if (n > lastIndex)
			n = lastIndex;
		for (int i = 0; i < n; i++) {
			sb.append(dataArray[i]);
			if (i < n-1)
				sb.append(sep);
		}	
		return sb.toString();
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
		//TODO
		return null;
	}
	
	public static SubstanceDataAggregator generateAggregator(File jsonConfig)
	{
		//TODO
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
		
		if (epDrtColumns != null) 
		{
			Set<String> epTypes = epDrtColumns.keySet();
			for (String ept :epTypes) {
				sb.append(ept + " DrtColumns = \n");
				List<DRTColumnInfo> drtColumns = epDrtColumns.get(ept);
				for (DRTColumnInfo dstci: drtColumns)
					sb.append("   " + dstci.name + ", " + dstci.index + ", " + dstci.type + "\n");
			}
		}
		
		if (endpointList != null)
		{
			sb.append("ENDPOINT_LIST = \n");
			for (String ep : endpointList)
				sb.append("    " + ep + "\n");
		}
		
		//TODO blocks
		
		return sb.toString();
	}
}
