package net.enanomapper.parser.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;
import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.GenericExcelParser;
import net.enanomapper.parser.excel.ExcelAnalysisTask;
import net.enanomapper.parser.excel.ExcelUtils;
import net.enanomapper.parser.excel.SubstanceAnalysisTask;
import net.enanomapper.parser.recognition.RichValue;
import net.enanomapper.parser.recognition.RichValueParser;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import ambit2.base.data.Property;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.study.StructureRecordValidator;
import ambit2.base.data.study.Value;
import ambit2.base.data.substance.ExternalIdentifier;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.relation.composition.CompositionRelation;

public class NMParserTestUtils {
	static Logger logger = Logger.getLogger(NMParserTestUtils.class.getName());

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// IterationAccess.ROW_MULTI_DYNAMIC.test();

		// testExcelParserConfiguration("/Users/nick/Projects/eNanoMapper/config01.json");
		// testExcelParserConfiguration("D:/Projects/nina/eNanoMapper/config01.json");

		//testExcelTemplate(
		//		"/Users/nick/Projects/eNanoMapper/template01-NR.xlsx",
		//		new File("/Users/nick/Projects/eNanoMapper/config01.json"));
		
		// testExcelTemplate("D:/Projects/nina/eNanoMapper/template01-NR.xlsx","D:/Projects/nina/eNanoMapper/config01.json");

		// System.out.println(IterationAccess.fromString("ROW_SINGLE"));
		
		//testCellRangeAddress("B1:C4");
		
		//testExcelAnalysisTaskParser("COMPARE_FILES; B1:C4; no params; /work/test-train-set.csv; VERBOSE", false);
		
	}

	public static void testExcelParserConfiguration(File jsonFile)
			throws Exception {
		ExcelParserConfigurator parserConfig = ExcelParserConfigurator
				.loadFromJSON(jsonFile);

		if (parserConfig.hasErrors()) {
			logger.log(Level.INFO, "GenericExcelParser configuration errors:\n"
					+ parserConfig.getAllErrorsAsString());
			return;
		}

		logger.log(Level.INFO, "ExcelParserConfigurator " + jsonFile);
		logger.log(Level.INFO, parserConfig.toJSONString());
	}

	public static void testExcelTemplate(String excelFile, File jsonFile)
			throws Exception {
		FileInputStream fin = new FileInputStream(excelFile);
		try {
			boolean isXLSX = excelFile.endsWith("xlsx");
			logger.log(Level.FINE, "isXLSX = " + isXLSX + "\n");
			GenericExcelParser parser = new GenericExcelParser(fin, jsonFile,
					isXLSX);
			logger.log(Level.FINE, parser.getExcelParserConfigurator()
					.toJSONString() + "\n");

			int n = 0;
			while (parser.hasNext()) {
				SubstanceRecord r = parser.nextRecord();
				n++;
				logger.log(Level.FINE, "Record #" + n);
				logger.log(Level.FINE, r.toJSON(null));
				List<ProtocolApplication> paList = r.getMeasurements();

				if (paList != null)
					for (ProtocolApplication pa : paList)
						logger.log(Level.FINE, "***Protocol application:\n"
								+ pa.toString());

				List<CompositionRelation> composition = r
						.getRelatedStructures();
				if (composition != null)
					for (CompositionRelation relation : composition) {
						// System.out.println(" ### Composition " +
						// structureRecordToString(relation.getSecondStructure()));
						logger.log(
								Level.FINE,
								" ### Composition \n"
										+ compositionRelationStructureToString(relation)); // both
																							// give
																							// the
																							// same
																							// result
						logger.log(
								Level.FINE,
								" ### Properties: "
										+ structureRecordProperties(relation
												.getSecondStructure()));
					}

			}

			/*
			 * we'll get the parser errors logged or exceptions thrown if
			 * (parser.hasErrors()) System.out.println("\n\nParser errors:\n" +
			 * parser.errorsToString());
			 */
		} finally {

			fin.close();
		}

	}

	public static void testRichValue(String rvString) {
		logger.log(Level.FINE,"Testing RichValue: " + rvString);

		RichValueParser par = new RichValueParser();
		RichValue rv = par.parse(rvString);

		String errors = par.getAllErrorsAsString();
		if (errors != null) {
			logger.log(Level.FINE,"RichValueParser errors:\n" + errors);
		} else {
			logger.log(Level.FINE,rv.toString());
		}
	}

	public static void testGetRowGroups(String excelFile, int sheetIndex,
			int startRowIndex, int endRowIndex, int keyColumnIndex,
			boolean recognizeGroupByNextNonEmpty) throws Exception {
		FileInputStream fin = new FileInputStream(excelFile);
		boolean isXLSX = excelFile.endsWith("xlsx");
		logger.log(Level.FINE,"isXLSX = " + isXLSX + "\n");

		Workbook workbook;
		if (isXLSX)
			workbook = new XSSFWorkbook(fin);
		else
			workbook = new HSSFWorkbook(fin);

		// All data is expected as 1-based indexed
		Sheet sheet = workbook.getSheetAt(sheetIndex - 1);
		TreeMap<Integer, String> groups = ExcelUtils.getRowGroups(sheet,
				startRowIndex - 1, (endRowIndex == -1) ? sheet.getLastRowNum()
						: (endRowIndex - 1), keyColumnIndex - 1,
				recognizeGroupByNextNonEmpty);

		for (Integer key : groups.keySet())
			logger.log(Level.FINE,"Group starts at row " + (key + 1) + "   "
					+ groups.get(key));

		fin.close();
	}
	
	public static void testCellRangeAddress(String ref) {
		CellRangeAddress cra = CellRangeAddress.valueOf(ref);
		System.out.println("Testing: " + ref);
		System.out.println("Format as string -->" + cra.formatAsString());
		System.out.println("getFirstColumn() = " + cra.getFirstColumn());
		System.out.println("getFirstRow() = " + cra.getFirstRow());
		System.out.println("getFirstColumn() = " + cra.getLastColumn());
		System.out.println("getFirstRow() = " + cra.getLastRow());
	}
	
	
	public static void testExcelAnalysisTaskParser(String eaTaskStr, boolean run) {
		System.out.println("Testing excel task:\n" + eaTaskStr + "\n");
		ExcelAnalysisTask task = null;
		try {
			task = ExcelAnalysisTask.parseFromString(eaTaskStr);
			System.out.println(task.toString());
		} 
		catch (Exception e) {
			System.out.println("Excel Analysis Task errors:");
			System.out.println(e.getMessage());
		}

		if (run && (task != null)) {
			try {
				task.run();
				if (!task.analysisErrors.isEmpty()) {
					System.out.println("Excel task errors:");
					for (int i = 0; i < task.analysisErrors.size(); i++) 
						System.out.println(task.analysisErrors.get(i));
				}
				
				if (!task.flagConsoleOutOnly) {
					System.out.println("Excel task result:");
					for (int i = 0; i < task.analysisResult.size(); i++) 
						System.out.println(task.analysisResult.get(i));
				}
			}
			catch (Exception e) {
				System.out.println("Excel task exception: " + e.getMessage());
				
				if (!task.analysisErrors.isEmpty()) {
					System.out.println("Excel task errors:");
					for (int i = 0; i < task.analysisErrors.size(); i++) 
						System.out.println(task.analysisErrors.get(i));
				}
			}
		}
	}
	
	public static void testSubstanceAnalysisTaskParser(String saTaskStr, List<SubstanceRecord> records) {
		System.out.println("Testing Substance Analysis task:\n" + saTaskStr + "\n");
		SubstanceAnalysisTask task = null;
		
		try {
			task = SubstanceAnalysisTask.parseFromString(saTaskStr);
			System.out.println(task.toString());
		} 
		catch (Exception e) {
			System.out.println("Substance Analysis Task errors:");
			System.out.println(e.getMessage());
			
			System.out.println("SA Task Format: " + SubstanceAnalysisTask.SATaskSyntaxFormat);
		}
		
		if ((records != null) && (task != null)) 
		{
			try {
				task.run(records);
				
				if (!task.analysisErrors.isEmpty()) {
					System.out.println("Substance Analysis task errors:");
					for (int i = 0; i < task.analysisErrors.size(); i++) 
						System.out.println(task.analysisErrors.get(i));
				}
				
				if (!task.flagConsoleOutOnly) {
					System.out.println("Substance Analysis task result:");
					for (int i = 0; i < task.analysisResult.size(); i++) 
						System.out.println(task.analysisResult.get(i));
				}
			}
			catch (Exception e) {
				System.out.println("Substance Analysis task exception: " + e.getMessage());				
				if (!task.analysisErrors.isEmpty()) {
					System.out.println("Excel task errors:");
					for (int i = 0; i < task.analysisErrors.size(); i++) 
						System.out.println(task.analysisErrors.get(i));
				}
			}
		}	
	}
	

	public static String structureRecordToString(IStructureRecord str) {
		StringBuffer sb = new StringBuffer();
		sb.append("Content : " + str.getContent());
		sb.append("  Format : " + str.getFormat());
		sb.append("  Smiles : " + str.getSmiles());
		sb.append("  Inchi : " + str.getInchi());
		sb.append("  InchiKey : " + str.getInchiKey());
		return sb.toString();
	}

	public static String structureRecordProperties(IStructureRecord str) {
		StringBuffer sb = new StringBuffer();
		for (Property p : str.getRecordProperties()) {
			sb.append("    " + p.getName() + ": " + str.getRecordProperty(p));
		}
		return sb.toString();
	}
	public static String structureRecordPropertiesToJsonString(IStructureRecord str) {
		StringBuffer sb = new StringBuffer();
		sb.append("{\n");
		int n = 0;
		for (Property p : str.getRecordProperties()) {
			if (n > 0)
				sb.append(",\n");
			n++;
			sb.append("\"" + p.getName() + "\" : \"" + str.getRecordProperty(p) + "\"");
		}
		sb.append("}\n");
		return sb.toString();
	}

	public static String compositionRelationStructureToString(
			CompositionRelation rel) {
		StringBuffer sb = new StringBuffer();
		sb.append("  Content : \"" + rel.getContent() + "\"\n");
		sb.append("  Format : \"" + rel.getFormat() + "\"\n");
		sb.append("  Smiles : \"" + rel.getSmiles() + "\"\n");
		sb.append("  Formula : \"" + rel.getFormula() + "\"\n");
		sb.append("  Inchi : \"" + rel.getInchi() + "\"\n");
		sb.append("  InchiKey : \"" + rel.getInchiKey() + "\"\n");
		return sb.toString();
	}
	
	public static String compositionRelationStructureToJsonString(
			CompositionRelation rel) {
		StringBuffer sb = new StringBuffer();
		sb.append("{\n");
		sb.append("\"Content\" : \"" + rel.getContent() + "\",\n");
		sb.append(" \"Format\" : \"" + rel.getFormat() + "\",\n");
		sb.append("\"Smiles\" : \"" + rel.getSmiles() + "\",\n");
		sb.append("\"Formula\" : \"" + rel.getFormula() + "\",\n");
		sb.append("\"Inchi\" : \"" + rel.getInchi() + "\",\n");
		sb.append("\"InchiKey\" : \"" + rel.getInchiKey() + "\"\n");
		sb.append("}\n");
		return sb.toString();
	}

	@Test
	public void testProteinCoronaXLSX() throws Exception {
		InputStream xlsx = this
				.getClass()
				.getClassLoader()
				.getResourceAsStream(
						"net/enanomapper/parser/csv/ProteinCoronaTest.xlsx");
		URL json = this
				.getClass()
				.getClassLoader()
				.getResource(
						"net/enanomapper/parser/csv/ProteinCoronaTest.json");
		GenericExcelParser parser = new GenericExcelParser(xlsx, new File(
				json.getFile()), true);
		try {
			while (parser.hasNext()) {
				SubstanceRecord r = parser.nextRecord();
				Assert.assertNotNull(r.getSubstanceUUID());
				Assert.assertNotNull(r.getPublicName());
				Assert.assertNotNull(r.getMeasurements());
				Assert.assertTrue(r.getMeasurements().size() > 0);
				logger.log(Level.FINE,r.toJSON("http://localhost/"));
				logger.log(Level.FINE,r.getMeasurements().toString());
				for (ProtocolApplication pa : r.getMeasurements()) {
					Assert.assertEquals(r.getSubstanceUUID(),
							pa.getSubstanceUUID());
					Assert.assertEquals(r.getOwnerName(), pa.getCompanyName());
					Assert.assertNotNull(pa.getProtocol());
				}
			}
		} finally {
			parser.close();
		}
	}
	
	public static Object findEffectValue(ProtocolApplication pa, String endpoint, 
			String conditions[], Object conditionValues[]) 
	{
		List<EffectRecord> effRecList = pa.getEffects();
		for (EffectRecord eff: effRecList) {
			if (eff.getEndpoint().toString().equals(endpoint)) {
				Object obj = matchConditions(eff, conditions, conditionValues);
				if (obj != null)
					return obj;
			}
		}
		return null;
	}
	
	public static Object matchConditions(EffectRecord eff, String conditions[], Object conditionValues[]) {
		for (int i = 0; i < conditions.length; i++) {
			//TODO
		}		
		return false;
	}


	@Test
	public void testDescriptorsTest1() throws Exception {
		InputStream xlsx = this
				.getClass()
				.getClassLoader()
				.getResourceAsStream(
						"net/enanomapper/parser/test1/CalculatedDescriptorsWorkFlow.xlsx");
		URL json = this
				.getClass()
				.getClassLoader()
				.getResource(
						"net/enanomapper/parser/test1/CalculatedDescriptorsWorkFlow.json");
		GenericExcelParser parser = new GenericExcelParser(xlsx, new File(
				json.getFile()), true);
		StructureRecordValidator validator = new StructureRecordValidator();
		validator.setFixErrors(true);
		try {
			while (parser.hasNext()) {
				SubstanceRecord r = parser.nextRecord();
				validator.process(r);
				Assert.assertNotNull(r.getSubstanceUUID());
				Assert.assertNotNull(r.getPublicName());
				Assert.assertNotNull(r.getMeasurements());
				Assert.assertTrue(r.getMeasurements().size() > 0);
				logger.log(Level.FINE, r.toJSON("http://localhost/"));
				logger.log(Level.FINE, r.getMeasurements().toString());
				for (ProtocolApplication pa : r.getMeasurements()) {
					Assert.assertEquals(r.getSubstanceUUID(),
							pa.getSubstanceUUID());
					Assert.assertEquals(r.getOwnerName(), pa.getCompanyName());
					Assert.assertNotNull(pa.getProtocol());
				}
			}
		} finally {
			parser.close();
		}
	}
	

	public static class SubstanceRecordGenerator
	{
		public List<String> errors = new ArrayList<String>();
		RichValueParser rvParser = new RichValueParser(); 
		SubstanceRecord record = null;
		ProtocolApplication curPA = null;
		EffectRecord curEff = null;
		String curEffBlockEndpoint = null;
		List<EffectRecord> curEffBlockList = new ArrayList<EffectRecord>();
		

		//Info format  <token 1>; <token 2>; ...;<token n>
		//<token i> is defined as: <key> = <value>
		public SubstanceRecord generateSubstanceRecord(String info)
		{	
			errors.clear();
			record = new SubstanceRecord();

			List<ProtocolApplication> measurements = new ArrayList<ProtocolApplication>();
			record.setMeasurements(measurements);
			curPA = null;
			curEff = null;

			String tokens[] = info.split(";");
			for (int i = 0; i < tokens.length; i++)
				handleSubRecToken(tokens[i].trim());
			
			if (curEffBlockEndpoint != null)
				finalizeEffectBlock();
			
			return record;
		}

		public void handleSubRecToken(String token)
		{
			if (token.isEmpty())
				return;
			
			//symbol '#' is used for commenting token
			if (token.startsWith("#"))
				return;
			
			int pos = token.indexOf("=");
			if (pos == -1)
			{   
				errors.add("Incorrect token: " + token);
				return;
			}

			String key = token.substring(0, pos).trim();
			String value = token.substring(pos+1).trim();
			
			if (key.equalsIgnoreCase("substanceUUID"))
			{
				record.setSubstanceUUID(value);
				return;
			}            
			if (key.equalsIgnoreCase("substanceName"))
			{
				record.setSubstanceName(value);
				return;
			}            
			if (key.equalsIgnoreCase("ownerUUID"))
			{
				record.setOwnerUUID(value);
				return;
			}            
			if (key.equalsIgnoreCase("ownerName"))
			{
				record.setOwnerName(value);
				return;
			}            
			if (key.equalsIgnoreCase("substanceType"))
			{
				record.setSubstancetype(value);
				return;
			}
			if (key.equalsIgnoreCase("publicName"))
			{
				record.setPublicName(value);
				return;
			}
			if (key.equalsIgnoreCase("idSubstance"))
			{
				Integer i = getInt(value);
				if (i == null)
					errors.add("Incorrect idSubstance:" + value);
				else
					record.setIdsubstance(i);
				return;
			}
			if (key.equalsIgnoreCase("composition"))
			{
				CompositionRelation relation = getComposition(value);
				if (relation != null)
					record.addStructureRelation(relation);
				return;
			}
			if (key.equalsIgnoreCase("extIds") || key.equalsIgnoreCase("ExternalIdentifiers") )
			{
				List<ExternalIdentifier> ids = getExternalIdentifiers(value);
				if (ids != null)
					record.setExternalids(ids);
				return;
			}
			
			//Handle Protocol Application info
			if (key.equalsIgnoreCase("PA") || key.equalsIgnoreCase("ProtocolApplication") )
			{
				//If effect block is started then it must be finalized 
				//and put in previous protocol application
				if (curEffBlockEndpoint != null)
				{	
					finalizeEffectBlock();
					curEffBlockEndpoint = null;
				}	
				
				//Start parsing of a new ProtocolApplication
				//value is used as protocol name
				curPA = new ProtocolApplication(value);
				record.getMeasurements().add(curPA);
				//Reseting curEff and effect block
				curEff = null;
				curEffBlockEndpoint = null;
				return;
			}
			
			//Handle effect info
			if (key.equalsIgnoreCase("Eff") || key.equalsIgnoreCase("Effect") )
			{
				if (curPA == null)
				{
					errors.add("Adding an Effect but no Protocol Appliaction is set: " + token);
					return;
				}
				
				if (curEffBlockEndpoint != null)
				{	
					finalizeEffectBlock();
					curEffBlockEndpoint = null;
				}	
				
				//Adding a new effect object to the currenr protocol application
				EffectRecord effect = new EffectRecord();
				effect.setEndpoint(value);
				curPA.addEffect(effect);
				curEff = effect;
				return;
			}
			
			//Handle simple effect block
			if (key.equalsIgnoreCase("EffBlock") || key.equalsIgnoreCase("EffectBlock") )
			{
				if (curPA == null)
				{
					errors.add("Adding an EffectBlock but no Protocol Appliaction is set: " + token);
					return;
				}
				
				//Starting a new effect block
				curEff = null;
				curEffBlockEndpoint = value;
				curEffBlockList.clear();
				return;
			}
			
			if (key.equalsIgnoreCase("Val") || key.equalsIgnoreCase("Value") )
			{
				if (curEff == null && curEffBlockEndpoint == null)
				{
					errors.add("Adding an value but no Effect is set: " + token);
					return;
				}
				setEffectValue(value);
				return;
			}
			
			if (key.equalsIgnoreCase("Cond") || key.equalsIgnoreCase("Condition") )
			{
				if (curEff == null && curEffBlockEndpoint == null)
				{
					errors.add("Adding an value but no Effect is set: " + token);
					return;
				}
				setParameter(value, false);
				return;
			}
			
			if (key.equalsIgnoreCase("Par") || key.equalsIgnoreCase("Parameter") )
			{
				if (curPA == null)
				{
					errors.add("Adding protocol parameter but no protocol applicatiopn is set: " + token);
					return;
				}
				setParameter(value, true);
				return;
			}

			errors.add("Unknow key in token: " + token);
		}
		
		void finalizeEffectBlock() 
		{
			for (int i = 0; i < curEffBlockList.size(); i++) 
				curPA.addEffect(curEffBlockList.get(i));
		}
		
		void setEffectValue(String value)
		{
			if (curEff != null)
				setEffectValue(value, curEff);
			else
			{
				//Handle EffBlockList
				String toks[] = value.split(",");
				for (int i = 0; i < toks.length; i++) {
					EffectRecord eff = new EffectRecord();
					eff.setEndpoint(curEffBlockEndpoint);
					setEffectValue(toks[i].trim(), eff);
					curEffBlockList.add(eff);
				}
				
				//Set the unit of the last effects to all 
				//other effect records which does not have unit set
				if (!curEffBlockList.isEmpty())
				{
					EffectRecord lastEff = curEffBlockList.get(curEffBlockList.size()-1);
					Object unit = lastEff.getUnit();
					if (unit != null) 
					{
						for (int i = 0; i < curEffBlockList.size()-1; i++) 
						{
							if (curEffBlockList.get(i).getUnit() == null)
								curEffBlockList.get(i).setUnit(unit);
						}	
					}		
				}				
			}
		}
		
		void setEffectValue(String value, EffectRecord eff)
		{
			RichValue rv = rvParser.parse(value, false);
			String rv_error = rvParser.getAllErrorsAsString();
			if (rv_error == null) {
				if (rv.unit != null)
					eff.setUnit(rv.unit);
				if (rv.loValue != null)
					eff.setLoValue(rv.loValue);
				if (rv.loQualifier != null)
					eff.setLoQualifier(rv.loQualifier);
				if (rv.upValue != null)
					eff.setUpValue(rv.upValue);
				if (rv.upQualifier != null)
					eff.setUpQualifier(rv.upQualifier);
				if (rv.errorValue != null)
					eff.setErrorValue(rv.errorValue);
				if (rv.errorValueQualifier != null)
					eff.setErrQualifier(rv.errorValueQualifier);
			} 
			else {
				eff.setTextValue(value);
			}
		}
		
		CompositionRelation getComposition(String value)
		{
			CompositionRelation relation = null;
			//TODO
			return relation;
		}

		List<ExternalIdentifier> getExternalIdentifiers(String value)
		{
			List<ExternalIdentifier> ids = new ArrayList<ExternalIdentifier>();
			String toks[] = value.split(",");
			for (int i = 0; i < (toks.length-1); i+=2)
			{
				String type = toks[i].trim();
				String  id = toks[i+1].trim();

				if ((id.isEmpty()) || (type.isEmpty()))
					errors.add("Incorrect id/type at position " + (i+1));
				else
					ids.add(new ExternalIdentifier(type, id));
			}
			return ids;
		}
		
		void setParameter(String tokValue, boolean isProtocolParameter) 
		{
			int pos = tokValue.indexOf(",");
			if (pos == -1)
			{   
				errors.add("Incorrect " + (isProtocolParameter?"parameter":"condition") + 
						" token " + tokValue);
				return;
			}

			String parName = tokValue.substring(0, pos).trim();
			String parStrValue = tokValue.substring(pos+1).trim();
			
			if (parName.isEmpty() || parStrValue.isEmpty())
			{   
				errors.add("Incorrect " + (isProtocolParameter?"parameter":"condition") + 
						" token: " + tokValue);
				return;
			}
			
			Object obj = getParameterObject(parStrValue);
			if (obj != null) 
			{
				if (isProtocolParameter)
				{
					IParams params = (IParams) curPA.getParameters();
					if (params == null) {
						params = new Params();
						curPA.setParameters(params);
					}
					params.put(parName, obj);
				}
				else
				{	
					if (curEff != null) {
						IParams conditions = (IParams) curEff.getConditions();
						if (conditions == null) {
							conditions = new Params();
							curEff.setConditions(conditions);
						}
						conditions.put(parName, obj);
					}
					else {
						//Handle effect block
						String subToks[] = tokValue.split(",");
						for (int i = 0; i < curEffBlockList.size(); i++) 
						{
							int k = i+1; //first token is codition name (parName)
							if (k >= subToks.length)
								k = subToks.length-1;
							
							Object obj_k = getParameterObject(subToks[k].trim());
							if (obj_k != null) {							
								IParams conditions = (IParams) curEffBlockList.get(i).getConditions();
								if (conditions == null) {
									conditions = new Params();
									curEffBlockList.get(i).setConditions(conditions);
								}
								conditions.put(parName, obj_k);
							}
							else
								errors.add("Cannot create " + (isProtocolParameter?"parameter":"condition") + 
										" object["+ (k+1) + " for token: " + tokValue);
						}
					}						
				}
			}
			else
				errors.add("Cannot create " + (isProtocolParameter?"parameter":"condition") + 
						" object for token: " + tokValue);

		}
		
		Object getParameterObject(String parStr) 
		{
			RichValue rv = rvParser.parse(parStr);
			String rv_error = rvParser.getAllErrorsAsString();

			if (rv_error == null) {
				Value pVal = new Value();
				if (rv.unit != null)
					pVal.setUnits(rv.unit);
				if (rv.loValue != null)
					pVal.setLoValue(rv.loValue);
				if (rv.loQualifier != null)
					pVal.setLoQualifier(rv.loQualifier);
				if (rv.upValue != null)
					pVal.setUpValue(rv.upValue);
				if (rv.upQualifier != null)
					pVal.setUpQualifier(rv.upQualifier);
				return pVal;
			}
			else {
				return parStr;
			}
		}

		//Helpers
		Integer getInt(String value)
		{
			try {
				Integer i = Integer.parseInt(value);
				return i;
			}
			catch (Exception e) {                
			}
			return null;
		}

		Double getDouble(String value)
		{
			try {
				Double d = Double.parseDouble(value);
				return d;
			}
			catch (Exception e) {                
			}
			return null;
		}

		public static void print(SubstanceRecord r)
		{
			System.out.println( r.toJSON(null));
			List<ProtocolApplication> paList = r.getMeasurements();
			if (paList != null)
			{    
				int nPA = 0;
				System.out.println(",");
				for (ProtocolApplication pa : paList)
				{    
					nPA++;
					if (nPA > 1)
						System.out.println(",");

					System.out.print( "\"Protocol application " + nPA + "\" :\n"
							+ pa.toString() );
				}
			}    
		}
	}
	//end of inner class

}
