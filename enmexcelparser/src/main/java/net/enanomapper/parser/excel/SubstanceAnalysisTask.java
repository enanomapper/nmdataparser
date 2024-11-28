package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import net.enanomapper.parser.excel.SALogicalCondition.ComparisonOperation;
import net.enanomapper.parser.excel.SALogicalCondition.SAConditionResult;
import net.enanomapper.parser.excel.SALogicalCondition.TargetType;




public class SubstanceAnalysisTask 
{
	public static enum SATaskType {
		CHECK_EFFECT_VALUE, CHECK_PROTOCOL_PARAMETER_VALUE, CHECK_CONDITION_VALUE, 
		COUNT_SUBSTANCES, COUNT_EFFECTS, COUNT_PROTOCOLS, COUNT_PROTOCOL_PARAMETERS, COUNT_CONDITIONS, 
		BASIC_COUNT, UNDEFINED;
		
		public static SATaskType fromString(String s) {
			try {
				SATaskType type = SATaskType.valueOf(s);
				return (type);
			} catch (Exception e) {
				return SATaskType.UNDEFINED;
			}
		}		
	}
	
	public static final String SATaskSyntaxFormat = 
				"<task type>; <qualifier>; <params>; <Logical condition 1>; ...; <special keyword 1>...";
	
	public SATaskType type = SATaskType.UNDEFINED;
	List<SALogicalCondition> logicalConditions = new ArrayList<SALogicalCondition>();
	public String qualifier = null;
	public ComparisonOperation comparison = ComparisonOperation.UNDEFINED; //determined from the qualifier
	public Object params[] = null;
			
	public boolean flagVerboseResult = false;	
	public boolean flagConsoleOut = false;
	public boolean flagConsoleOutOnly = false;
	public boolean flagIgnoreCase = true;
	public List<String> analysisResult = new ArrayList<String>();
	public List<String> analysisErrors = new ArrayList<String>();
	public List<String> analysisWarnings = new ArrayList<String>();	
	public int analysisStatTotalOKNum = 0;
	public int analysisStatTotalProblemNum = 0;
	public TargetType groupBy = TargetType.UNDEFINED;
	
	public List<SALogicalCondition> curFailedLogConditions = new ArrayList<SALogicalCondition>();
	
	/*
	 * Parsing a SubstanceAnalysisTask from a string in the following format
	 * <task type>; <qualifier>; <params>; <Logical conditions>; <special keywords> ...
	 * 
	 * Qualifier and params checks are applied on the task level object.
	 * If applicable, Logical conditions checks are applied on lower level objects i.e. 
	 * elements (sub-objects) of the main task object
	 */
	public static SubstanceAnalysisTask parseFromString(String taskStr) throws Exception 
	{
		List<String> errors = new ArrayList<String>();
		SubstanceAnalysisTask saTask = new SubstanceAnalysisTask();		
		String tokens[] = taskStr.split(";");
				
		//SA Task type
		if (tokens.length < 1)
			errors.add("Missing task type token!");
		else {	
			saTask.type = SATaskType.fromString(tokens[0].trim());
			if (saTask.type == SATaskType.UNDEFINED)
				errors.add("Incorrect substance analysis task type: " + tokens[0]);
		}
		
		//Quilifier
		if (tokens.length < 2) {
			if (saTask.type == SATaskType.BASIC_COUNT) {
				//Only one token is allowed for BASIC_COUNT task
				//therefore ending the parsing procedure
				if (errors.isEmpty())
					return saTask;
			}
			else	
				errors.add("Missing qualifier token!");
		}	
		else {	
			saTask.qualifier = tokens[1].trim();
			if (!saTask.qualifier.isEmpty()) 
			{	
				saTask.comparison = SALogicalCondition.qualifierToComparisonOperation(saTask.qualifier);
				if (saTask.comparison == ComparisonOperation.UNDEFINED)
					errors.add("Incorrect qualifier: " + saTask.qualifier);
			}	
		}
		
		//Params
		if (tokens.length < 3)
			errors.add("Missing parameters token!");
		else {	
			String paramsStr = tokens[2].trim();

			if (!paramsStr.isEmpty() && !paramsStr.equals("--") && !paramsStr.equals("no params"))
			{						
				String paramTokens[] = paramsStr.split(",");			

				if (paramTokens.length > 0) 
				{
					saTask.params = new Object[paramTokens.length];

					for (int i = 0; i < paramTokens.length; i++) {
						String par = paramTokens[i].trim();
						if (par.isEmpty())
							errors.add("Parameter #" + (i+1) + " is empty!");

						Object o = par;
						try {
							Double d = Double.parseDouble(par);
							o = d;
						}
						catch (Exception x) {
						}
						saTask.params[i] = o;
					}
				}
			}
		}		
		
		//Logical conditions + special keywords parsing
		if (tokens.length >= 4)
			for (int i = 3; i < tokens.length; i++)	
			{	
				String lcStr = tokens[i].trim();				
				if (lcStr.isEmpty())
					continue;
				int specTokRes = checkForSpecialToken(lcStr, saTask,  errors);
				if (specTokRes == 0)
					continue; //The token is either a special keyword or a comment 

				try {
					SALogicalCondition logCond = SALogicalCondition.parseFromString(lcStr);
					saTask.logicalConditions.add(logCond);
				}
				catch (Exception x) {
					errors.add("Errors on logical conditions #" + (i-3) + " : " + x.getMessage());
				}
						
			}
		
		//Check parsed info consistency
		//Check groupBy
		if (saTask.groupBy != TargetType.UNDEFINED)
		{
			boolean flagErr = true; //Majority of the combinations are not supported
			switch (saTask.type) {
			case COUNT_PROTOCOLS:
				if (saTask.groupBy == TargetType.SUBSTANCE)
					flagErr = false;
				break;
			case COUNT_EFFECTS:
				if (saTask.groupBy == TargetType.PROTOCOL || 
				saTask.groupBy == TargetType.SUBSTANCE)
					flagErr = false;
				break;
			case COUNT_CONDITIONS:
				if (saTask.groupBy == TargetType.EFFECT ||
				saTask.groupBy == TargetType.PROTOCOL || 
				saTask.groupBy == TargetType.SUBSTANCE)
					flagErr = false;
				break;				
			}

			if (flagErr)
				errors.add("Group by " + saTask.groupBy + " is not supported for task type " + saTask.type);
		}
						
		if (!errors.isEmpty()) {
			StringBuffer errBuffer = new StringBuffer();
			for (String err: errors)
				errBuffer.append(err + "\n");
			throw new Exception (errBuffer.toString());
		}		
		
		return saTask;
	}
	
	public static int checkForSpecialToken(String token, SubstanceAnalysisTask saTask, List<String> errors) 
	{
		if (token.startsWith("#"))
		{
			//This token is omitted ('#' symbol makes it a comment token)
			return 0;
		}
		
		if (token.equalsIgnoreCase("verbose"))
		{	
			saTask.flagVerboseResult = true;
			return 0;
		}		
		
		if (token.equalsIgnoreCase("console_out"))
		{	
			saTask.flagConsoleOut = true;
			return 0;
		}
		
		if (token.equalsIgnoreCase("console_out_only"))
		{	
			saTask.flagConsoleOutOnly = true;
			return 0;
		}
		
		if (token.equalsIgnoreCase("GROUP_BY_SUBSTANCE"))
		{	
			saTask.groupBy = SALogicalCondition.TargetType.SUBSTANCE;
			return 0;
		}
		
		if (token.equalsIgnoreCase("GROUP_BY_PROTOCOL"))
		{	
			saTask.groupBy = SALogicalCondition.TargetType.PROTOCOL;
			return 0;
		}
		
		if (token.equalsIgnoreCase("GROUP_BY_PROTOCOL_PARAMETER"))
		{	
			saTask.groupBy = SALogicalCondition.TargetType.PROTOCOL_PARAMETER;
			return 0;
		}
		
		if (token.equalsIgnoreCase("GROUP_BY_EFFECT"))
		{	
			saTask.groupBy = SALogicalCondition.TargetType.EFFECT;
			return 0;
		}
		
		if (token.equalsIgnoreCase("GROUP_BY_CONDITION"))
		{	
			saTask.groupBy = SALogicalCondition.TargetType.CONDITION;
			return 0;
		}
		
		return -1;
	}
	
	void outputLine(String line) {
		if (flagConsoleOutOnly)
			System.out.println(line);
		else
		{
			analysisResult.add(line);
			if (flagConsoleOut)
				System.out.println(line);
		}
	}
	
	public int run(List<SubstanceRecord> records) 
	{
		analysisResult.clear();
		analysisErrors.clear();
		analysisWarnings.clear();
		analysisStatTotalOKNum = 0;
		analysisStatTotalProblemNum = 0;
		
		if (records == null)
			return -1;
		
		for (int i = 0; i<records.size(); i++)
		{	
			outputLine("Record " + (i+1) + "  " + records.get(i).getPublicName());
			
			switch (type) {
			case COUNT_SUBSTANCES:
				taskCountSubstanceRecords(records.get(i), i);
				break;
			case COUNT_EFFECTS:
				taskCountEffects(records.get(i), i);
				break;
			case BASIC_COUNT:
				taskBasicCount(records.get(i), i);
				break;	
			}
		}
		
		if (type != SATaskType.BASIC_COUNT)
			makeResultSummary();
		return 0;
	}
	
	public void makeResultSummary() 
	{
		String okLabel = "OK ";
		String problemLabel = "Problems ";
				
		switch (type) {
		case COUNT_SUBSTANCES:
			okLabel = "Subst. records OK: ";
			problemLabel = "Problematic subst. records: ";
			break;
		case COUNT_PROTOCOLS:
			okLabel = "Protocols OK: ";
			problemLabel = "Problematic protocols: ";
			break;
		case COUNT_PROTOCOL_PARAMETERS:
			okLabel = "Protocol parameterss OK: ";
			problemLabel = "Problematic protocol parameters: ";
			break;	
		case COUNT_EFFECTS:
			okLabel = "Effects OK: ";
			problemLabel = "Problematic effects: ";
			break;
		case COUNT_CONDITIONS:
			okLabel = "Conditions OK: ";
			problemLabel = "Problematic conditions: ";
			break;	
		}
		
		outputLine(okLabel + analysisStatTotalOKNum);
		outputLine(problemLabel + analysisStatTotalProblemNum);
		
		checkAnalysisStat();
	}
	
	void checkAnalysisStat() {
		//Check analysisStatTotalOKNum with qualifier and params info
		if (comparison != ComparisonOperation.UNDEFINED)
		{
			double d_params[] = SALogicalCondition.extractDoubleArray(params);
			boolean comparRes = SALogicalCondition.checkConditionForDoubleTarget(
					new Double(analysisStatTotalOKNum), comparison, d_params);
			if (comparRes)
				outputLine("analysisStatTotalOKNum is OK");
			else
				outputLine("Probm: with analysisStatTotalOKNum = " + analysisStatTotalOKNum);
		}
	}		
	
	public void taskCountSubstanceRecords(SubstanceRecord record, int recIndex)
	{
		boolean checkRes = checkLogConditions(record);
		if (checkRes) 
		{
			analysisStatTotalOKNum++;
			if (flagVerboseResult)
				outputLine("Record " + (recIndex + 1) + ": OK");
		}	
		else {
			analysisStatTotalProblemNum++;
			outputLine("Problem: record " + (recIndex + 1) + ": " + curFailedLogConditionToMessageString());
		}
	}
		
		
	public void taskCountEffects(SubstanceRecord record, int recIndex)
	{
		//TODO
	}
	
	public void taskBasicCount(SubstanceRecord record, int recIndex)
	{
		List<ProtocolApplication> paList = record.getMeasurements();
		outputLine("  Num of Protocol Applications = " + paList.size());
				
		for (ProtocolApplication pa: paList) {
			Protocol prot = (Protocol)pa.getProtocol();
			outputLine("  " + prot.getTopCategory() + "  " + prot.getCategory());
			List effects = pa.getEffects();
			outputLine("   num of effecs = " + ((effects!=null)?effects.size():0));	
			if (effects != null) {
				StringBuffer sb = new StringBuffer();
				List<String> conditionList = extractConditions(effects);
				for (String cond : conditionList)
					sb.append(cond + ", ");
				outputLine("   All conditions(" +  conditionList.size() +"): " + sb.toString());
				List<ConditionGroup> condGrps = extractConditionGroups(effects);
				outputLine("   Condition groups(" +  condGrps.size() +"):");
				for (ConditionGroup cg : condGrps) {
					outputLine("     " + cg.toSingleLineString());
				}
			}
		}		
	}
	
	List<String> extractConditions(List<EffectRecord> effects)
	{
		List<String> condList = new ArrayList<String>();
		for (EffectRecord eff: effects){
			IParams conditions = (IParams) eff.getConditions();
			Set<String> keys = conditions.keySet();
			for (String key : keys)
				if (!condList.contains(key))
					condList.add(key);
		}
		return condList;
	}
	
	List<String> extractConditions(EffectRecord eff)
	{		
		List<String> condList = new ArrayList<String>();
		IParams conditions = (IParams) eff.getConditions();
		Set<String> keys = conditions.keySet();
		for (String key : keys)
			condList.add(key);
		return condList;
	}
	
	class ConditionGroup {
		public String endpoint = null;
		public String endpointType = null;
		public Set<String> conditions = null;
		public int effectCount = 1;

		public ConditionGroup(String endpoint, String endpointType, Set<String> conditions) {
			this.endpoint = endpoint;
			this.endpointType = endpointType;
			this.conditions = conditions;
		}

		public boolean equals (ConditionGroup cg) {
			if (!cg.endpoint.equalsIgnoreCase(endpoint))
				return false;
			if (cg.endpointType == null) {
				if (endpointType != null)
					return false;
			}
			else if (!cg.endpointType.equalsIgnoreCase(endpointType))
				return false;

			if (!cg.conditions.equals(conditions))
				return false;
			return true;
		}

		public ConditionGroup findInList(List<ConditionGroup> condGrps) {
			for (ConditionGroup cg : condGrps)
				if (this.equals(cg))
					return cg;
			return null;
		}

		public String toSingleLineString() {
			StringBuffer sb = new StringBuffer();
			sb.append(endpoint + "[" + endpointType + "] (" + effectCount + ")");
			sb.append(" --> ");
			for (String cond: conditions)
				sb.append(cond + ", ");
			return sb.toString();
		}
	}
	
	class ConditionValues {
		public String endpoint = null;
		public String endpointType = null;
		public String condition = null;
		public Set<Object> values = null;
		public int effectCount = 1;
		
		public ConditionValues(String endpoint, String endpointType, String condition, Set<Object> values) {
			this.endpoint = endpoint;
			this.endpointType = endpointType;
			this.condition = condition;
			this.values = values;
		}
		
		public boolean equals(ConditionValues cv) {
			if (!cv.endpoint.equalsIgnoreCase(endpoint))
				return false;
			if (cv.endpointType == null) {
				if (endpointType != null)
					return false;
			}
			else if (!cv.endpointType.equalsIgnoreCase(endpointType))
				return false;

			if (!cv.condition.equals(condition))
				return false;
			
			if (cv.values.size() != values.size())
				return false;
			
			/* This check is excluded from ConditionValues identification
			for (Object o: cv.values) {
				boolean match_o = false;
				for (Object v: values) 
					if (o.toString().equals(v.toString())) {
						match_o = true;
						break;
					}
				if (!match_o)
					return false;
			}
			*/			
			return true;
		}
		
		public ConditionValues findInList(List<ConditionValues> condVals) {
			for (ConditionValues cv : condVals)
				if (this.equals(cv))
					return cv;
			return null;
		}
		
		public String toSingleLineString() {
			StringBuffer sb = new StringBuffer();
			sb.append(endpoint + "[" + endpointType + "]" +
					"[cond=" + condition+ "]" +"(" + effectCount + ")");
			sb.append(" --> ");
			for (Object o: values)
				sb.append(o.toString() + ", ");
			return sb.toString();
		}
	}	

	List<ConditionGroup> extractConditionGroups(List<EffectRecord> effects)
	{
		List<ConditionGroup> condGrps = new ArrayList<ConditionGroup>();
		for (EffectRecord eff: effects){
			IParams conditions = (IParams) eff.getConditions();
			Set<String> keys = conditions.keySet();
			ConditionGroup cg = new ConditionGroup(eff.getEndpoint().toString(), eff.getEndpointType(), keys);
			ConditionGroup cgListInst = cg.findInList(condGrps);
			if (cgListInst == null)
				condGrps.add(cg);
			else
				cgListInst.effectCount++;
		}
		return condGrps;
	}
	
	
	boolean checkLogConditions(SubstanceRecord record)
	{
		curFailedLogConditions.clear();
		for (SALogicalCondition lc:  logicalConditions)
		{
			if (lc.targetType == TargetType.SUBSTANCE)
			{
				if (!lc.applyForSubstance(record)) {
					curFailedLogConditions.add(lc);
					return false;	
				}	
			}
		}
		return true;
	}
	
	boolean checkLogConditions(ProtocolApplication pa)
	{
		curFailedLogConditions.clear();
		for (SALogicalCondition lc:  logicalConditions)
		{
			if (lc.targetType == TargetType.PROTOCOL)
			{
				if (!lc.applyForProtocolApplication(pa)) {
					curFailedLogConditions.add(lc);
					return false;	
				}	
			}
		}
		return true;
	}
			
	boolean checkLogConditions(EffectRecord effect)
	{
		curFailedLogConditions.clear();
		for (SALogicalCondition lc:  logicalConditions)
		{
			if (lc.targetType == TargetType.EFFECT)
			{
				if (!lc.applyForEffect(effect)) {
					curFailedLogConditions.add(lc);
					return false;	
				}	
			}
		}
		return true;
	}
	
	boolean checkLogConditionsForProtocolParameter(String key, Object value)
	{
		curFailedLogConditions.clear();
		for (SALogicalCondition lc:  logicalConditions)
		{
			if (lc.targetType == TargetType.PROTOCOL_PARAMETER)
			{
				if (!lc.applyForKeyValue(key, value)) {
					curFailedLogConditions.add(lc);
					return false;	
				}	
			}
		}
		return true;
	}
	
	boolean checkLogConditionsForCondition(String key, Object value)
	{
		curFailedLogConditions.clear();
		for (SALogicalCondition lc:  logicalConditions)
		{
			if (lc.targetType == TargetType.CONDITION)
			{
				if (!lc.applyForKeyValue(key, value)) {
					curFailedLogConditions.add(lc);
					return false;	
				}	
			}
		}
		return true;
	}
	
	String curFailedLogConditionToMessageString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < curFailedLogConditions.size(); i++)
		{
			if (i > 0)
				sb.append("\n  ");
			SALogicalCondition lc = curFailedLogConditions.get(i);
			sb.append(lc.toString());
		}
		return sb.toString();
	}	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SA Type: " + type + "\n");
				
		if (qualifier != null) 
			sb.append("Qualifier: " + qualifier + "\n");
				
		if (params != null) {
			sb.append("Params: ");
			for (int i = 0; i < params.length; i++)
				sb.append(params[i] + " ");
			sb.append("\n");
		}
		
		if (!logicalConditions.isEmpty()) {
			sb.append("Logical Conditions: \n");
			for (int i = 0; i < logicalConditions.size(); i++) {
				SALogicalCondition lc = logicalConditions.get(i);
				sb.append("  " + lc.toString());
				sb.append("\n");
			}			
		}
		
		if (groupBy != TargetType.UNDEFINED)
			sb.append("groupBy: " + groupBy + "\n");
		
		sb.append("Verbose: " + flagVerboseResult + "\n");
				
		return sb.toString();
	}
}
