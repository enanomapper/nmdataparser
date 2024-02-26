package net.enanomapper.parser.test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import ambit2.base.data.Property;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.IValue;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.relation.composition.CompositionRelation;
import ambit2.base.relation.composition.Proportion;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.GenericExcelParser;

public class NMParserTest extends TestCase 
{

	public static Test suite() {
		return new TestSuite(NMParserTest.class);
	}
	
	public static double concentrations[] = {0,	1,	5,	10,	25};
	public static double timePoints[] = {6,	24};
	public static String errQualifiers[] = {"<", "<", "<", "<=", "<="};
	public static String effQualifiers[] = {"<", "<=", "~"};
	public static String htsParams[] = {"A1 6wo", "A2 6wo", "A3 6wo", "A4 6wo"};
	
	
		
	public void test01() throws Exception {
		// this will close the inputstream automatically
		try (InputStream fin = getClass().getClassLoader()
				.getResourceAsStream("net/enanomapper/parser/testExcelParser/testfile1.xlsx")) {
			boolean isXLSX = true;
			URL url = getClass().getClassLoader()
					.getResource("net/enanomapper/parser/testExcelParser/testfile1-config.json");
			try (GenericExcelParser parser = new GenericExcelParser(fin, new File(url.getFile()), isXLSX)) {

				// System.out.println(parser.getExcelParserConfigurator().toJSONString()
				// + "\n");
				
				checkParserConfiguration01(parser);
				
				int n = 0;
				while (parser.hasNext()) {
					SubstanceRecord r = parser.nextRecord();
					n++;
					System.out.println("Test01/Record #" + n);
					checkRecord(r, n);
				}
			} catch (Exception x) {
				Logger.getAnonymousLogger().log(Level.SEVERE,x.getMessage());
				throw x;
			}
		} catch (Exception x1) {
			Logger.getAnonymousLogger().log(Level.SEVERE,x1.getMessage());
			throw x1;
		}
	}
	
	public void test02() throws Exception {
		// this will close the inputstream automatically
		try (InputStream fin = getClass().getClassLoader()
				.getResourceAsStream("net/enanomapper/parser/testExcelParser/testfile2.xlsx")) {
			boolean isXLSX = true;
			URL url = getClass().getClassLoader()
					.getResource("net/enanomapper/parser/testExcelParser/testfile2-config.json");
			try (GenericExcelParser parser = new GenericExcelParser(fin, new File(url.getFile()), isXLSX)) {

				// System.out.println(parser.getExcelParserConfigurator().toJSONString()
				// + "\n");
				SubstanceRecord r = parser.nextRecord();
				testEffectBlocks02(r);
				
			} catch (Exception x) {
				Logger.getAnonymousLogger().log(Level.SEVERE,x.getMessage());
				throw x;
			}
		} catch (Exception x1) {
			Logger.getAnonymousLogger().log(Level.SEVERE,x1.getMessage());
			throw x1;
		}
	}
	
	public void test03() throws Exception {
		// this will close the inputstream automatically
		try (InputStream fin = getClass().getClassLoader()
				.getResourceAsStream("net/enanomapper/parser/testExcelParser/testfile3.xlsx")) {
			boolean isXLSX = true;
			URL url = getClass().getClassLoader()
					.getResource("net/enanomapper/parser/testExcelParser/testfile3-config.json");
			try (GenericExcelParser parser = new GenericExcelParser(fin, new File(url.getFile()), isXLSX)) {

				// System.out.println(parser.getExcelParserConfigurator().toJSONString()
				// + "\n");
				
				int n = 0;
				while (parser.hasNext()) {
					SubstanceRecord r = parser.nextRecord();
					n++;
					System.out.println("Test03/Record #" + n);
					testEffectBlocks03(r,n);
				}
				
			} catch (Exception x) {
				Logger.getAnonymousLogger().log(Level.SEVERE,x.getMessage());
				throw x;
			}
		} catch (Exception x1) {
			Logger.getAnonymousLogger().log(Level.SEVERE,x1.getMessage());
			throw x1;
		}
	}
	
	public void test04() throws Exception {
		// this will close the inputstream automatically
		try (InputStream fin = getClass().getClassLoader()
				.getResourceAsStream("net/enanomapper/parser/testExcelParser/testfile4.xlsx")) {
			boolean isXLSX = true;
			URL url = getClass().getClassLoader()
					.getResource("net/enanomapper/parser/testExcelParser/testfile4-config.json");
			try (GenericExcelParser parser = new GenericExcelParser(fin, new File(url.getFile()), isXLSX)) {

				// System.out.println(parser.getExcelParserConfigurator().toJSONString()
				// + "\n");
				
				int n = 0;
				while (parser.hasNext()) {
					SubstanceRecord r = parser.nextRecord();
					n++;
					System.out.println("Test04/Record #" + n);
					testEffectBlocks04(r,n);
				}
				
			} catch (Exception x) {
				Logger.getAnonymousLogger().log(Level.SEVERE,x.getMessage());
				throw x;
			}
		} catch (Exception x1) {
			Logger.getAnonymousLogger().log(Level.SEVERE,x1.getMessage());
			throw x1;
		}
	}
	
	void checkRecord(SubstanceRecord subRec, int substNum)
	{
		//substNum is 1-based index
		String prefix = "Substance " + substNum + ": ";
		assertEquals(prefix + "getPublicName()", "NM-00" + substNum, subRec.getPublicName());
		assertEquals(prefix + "getSubstanceName()", "name-" + substNum, subRec.getSubstanceName());
		
		//getSubstanceUUID() works both from JSON keyword SUBSTANCE_UUID or by default taken from SUBSTANCE_NAME 
		String substUUID = ExcelParserConfigurator.generateUUID("XLSX", "name-" + substNum);
		assertEquals(prefix + "getSubstanceUUID()", substUUID, subRec.getSubstanceUUID());
		
		String rsUUID = ExcelParserConfigurator.generateUUID("XLSX", "my-ref-subst");
		assertEquals(prefix + "getReferenceSubstanceUUID()", rsUUID, subRec.getReferenceSubstanceUUID());
		
		assertEquals(prefix + "getOwnerUUID()", "owner-" + substNum, subRec.getOwnerUUID());
		assertEquals(prefix + "getOwnerName()", "test-owner-name", subRec.getOwnerName());
		assertEquals(prefix + "getSubstancetype()", "NPO_1317", subRec.getSubstancetype());
		assertEquals(prefix + "getIdsubstance()", 123456, subRec.getIdsubstance());
		
		
		assertEquals(prefix + "getExternalids().get(0).getSystemDesignator()", "ID1", 
				subRec.getExternalids().get(0).getSystemDesignator());
		assertEquals(prefix + "getExternalids().get(0).getSystemDesignator()", "id1-" + substNum, 
				subRec.getExternalids().get(0).getSystemIdentifier());
		assertEquals(prefix + "getExternalids().get(1).getSystemDesignator()", "ID2", 
				subRec.getExternalids().get(1).getSystemDesignator());
		assertEquals(prefix + "getExternalids().get(1).getSystemDesignator()", "id2-" + substNum, 
				subRec.getExternalids().get(1).getSystemIdentifier());
		
		List<CompositionRelation> composition = subRec.getRelatedStructures();
		testCompositionRelation(substNum, 1, composition.get(0));
		testCompositionRelation(substNum, 2, composition.get(1));
		
		List<ProtocolApplication> paList = subRec.getMeasurements();
		for (int i = 0; i < paList.size(); i++)
		{	
			ProtocolApplication pa = paList.get(i);
			testProtocolApplication(substNum, i, pa );
			//System.out.println( "***Protocol application:\n" + pa.toString());
		}
		
		//System.out.println(r.toJSON(null));
				
	}
	
	void testCompositionRelation(int substNum, int constituentIndex, CompositionRelation compRel)
	{
		String prefix = "Substance " + substNum + ", constituent " + (constituentIndex + 1) + " : ";
		
		switch (constituentIndex)
		{
		case 1:
			assertEquals(prefix + "getFormat()", "test-format", compRel.getFormat());
			assertEquals(prefix + "getContent()", "test-content", compRel.getContent());
			assertEquals(prefix + "getFormula()", "test-formula", compRel.getFormula());
			
			Proportion prop = compRel.getRelation();
			assertEquals(prefix + " proporion getFunction()", "test-function", prop.getFunction());
			assertEquals(prefix + " proporion getTypical()", "test-typical-precision", prop.getTypical());
			assertEquals(prefix + " proporion getTypical_value()", 0.33, prop.getTypical_value());
			assertEquals(prefix + " proporion getTypical_unit()", "test-typical-unit", prop.getTypical_unit());			
			assertEquals(prefix + " proporion getReal_lower()", "test-real-lower-precision", prop.getReal_lower());
			assertEquals(prefix + " proporion getReal_lowervalue()", 0.20, prop.getReal_lowervalue());
			assertEquals(prefix + " proporion getReal_upper()", "test-real-upper-precision", prop.getReal_upper());
			assertEquals(prefix + " proporion getReal_uppervalue()", 0.50, prop.getReal_uppervalue());
			assertEquals(prefix + " proporion getReal_unit()", "test-real-unit", prop.getReal_unit());
			
			break;
		case 2:
			assertEquals(prefix + "getFormat()", "format-" + substNum, compRel.getFormat());
			assertEquals(prefix + "getContent()", "content-" + substNum, compRel.getContent());
			assertEquals(prefix + "getFormula()", "formula-" + substNum, compRel.getFormula());
			assertEquals(prefix + "getSmiles()", "CCC-" + substNum, compRel.getSmiles());
			assertEquals(prefix + "getInchi()", "inchi-" + substNum, compRel.getInchi());
			assertEquals(prefix + "getInchiKey()", "inchi-key-" + substNum, compRel.getInchiKey());
			
			IStructureRecord str = compRel.getSecondStructure();
			for (Property p : str.getRecordProperties()) 
			{
				if (p.getName().equals("PROP1"))
					assertEquals(prefix + "property PROP1", "prop1-" + substNum, str.getRecordProperty(p));
				
				if (p.getName().equals("PROP2"))
					assertEquals(prefix + "property PROP2", 5.0 + 0.1*substNum, str.getRecordProperty(p));
				
				if (p.getName().equals("PROP3"))
				{	
					//Ptroperty PROP3 is set with: "DATA_INTERPRETATION" : "AS_TEXT"
					//assertEquals(prefix + "property PROP3", (new Double(200 + 0.1 * substNum)).toString(), str.getRecordProperty(p));
					assertEquals(prefix + "property PROP3", "200." + substNum, str.getRecordProperty(p));
				}
			}
			
		}
	}
	
	void testProtocolApplication(int substNum, int paIndex, ProtocolApplication pa)
	{
		//substNum is 1-based index, paIndex is 0-based index
		String prefix = "Substance " + substNum + ", ProtocolApplication " + (paIndex+1) + " : ";
		Protocol p = (Protocol) pa.getProtocol();
		assertEquals(prefix + "getProtocol()", "test-protocol-endpoint", p.getEndpoint());
		assertEquals(prefix + "getTopCategory()", "test-top-cat", p.getTopCategory());
		assertEquals(prefix + "getCategory()", "test-category-code", p.getCategory());		
		List<String> guidesLines = p.getGuideline();
		assertEquals(prefix + "guidesLines 1", "guide1-0" + substNum, guidesLines.get(0));
		assertEquals(prefix + "guidesLines 2", "guide2-0" + substNum, guidesLines.get(1));
		
		String paUUID = ExcelParserConfigurator.generateUUID("XLSX", "pr-endpoint-" + substNum);
		assertEquals(prefix + "getDocumentUUID()", paUUID, pa.getDocumentUUID());
		
		String invUUID = ExcelParserConfigurator.generateUUID("XLSX", "test-investigation-uuid");		
		assertEquals(prefix + "getInvestigationUUID()", UUID.nameUUIDFromBytes(invUUID.getBytes()), 
						pa.getInvestigationUUID());		
		//problem with ordinary text. invUUID should be the exoected value but 
		//function setInvestigationUUID() generates redundantly UUID from invUUID
		//
		//The commented test below currently would work
		//assertEquals(prefix + "getInvestigationUUID()", UUID.fromString("8a9becb6-c841-3717-953c-9f05cda7b54a"), 
		//		pa.getInvestigationUUID());
		
		assertEquals(prefix + "getAssayUUID()", UUID.nameUUIDFromBytes("test-assay-uuid".getBytes()), 
				pa.getAssayUUID());
		
		//JSON CITATION_* sections defiens reference info
		assertEquals(prefix + "getReference()", "title-" + substNum, pa.getReference());
		assertEquals(prefix + "getReferenceOwner()", "cit-own-" + substNum, pa.getReferenceOwner());
		assertEquals(prefix + "getReferenceYear()", "year" + (2010 + substNum), pa.getReferenceYear());
		
		assertEquals(prefix + "getInterpretationCriteria()", "interp-crit-" + substNum, pa.getInterpretationCriteria());
		assertEquals(prefix + "getInterpretationResult()", "interp-res-" + substNum, pa.getInterpretationResult());
		
		//Reliability
		assertEquals(prefix + "getReliability().getValue()", "reliability-value", pa.getReliability().getValue());
		assertEquals(prefix + "getReliability().getIsRobustStudy()", "robust", pa.getReliability().getIsRobustStudy());
		assertEquals(prefix + "getReliability().getIsUsedforClassification()", "classification", pa.getReliability().getIsUsedforClassification());
		assertEquals(prefix + "getReliability().getIsUsedforMSDS()", "msds", pa.getReliability().getIsUsedforMSDS());
		assertEquals(prefix + "getReliability().getPurposeFlag())", "purpose-flag", pa.getReliability().getPurposeFlag());
		assertEquals(prefix + "getReliability().getStudyResultType())", "result-type", pa.getReliability().getStudyResultType());
		
		//Parameters
		IParams params = (IParams) pa.getParameters();
		IValue v = (IValue) params.get("par1");
		assertEquals(prefix + "parameters par1 value", 1.0 * substNum, v.getLoValue());
		assertEquals(prefix + "parameters par1 unit", "K", v.getUnits());
		String par2_str = (String) params.get("par2");
		assertEquals(prefix + "parameters par1 value", "par2-0" + substNum, par2_str);
		
		List<EffectRecord> effRecList = pa.getEffects();
		for (int i = 0; i < effRecList.size(); i++)
			testEffectRecord(substNum, paIndex, i, effRecList.get(i));
	}
	
	void testEffectRecord(int substNum, int paIndex, int effRecIndex, EffectRecord effRec)
	{
		//substNum is 1-based index, paIndex is 0-based index
		String prefix = "Substance " + substNum + ", ProtocolApplication " + (paIndex+1) + 
				" Effect Rec. " + (effRecIndex + 1) + " : ";
		
		IParams conds;
		IValue v;
		String s;
		
		switch (effRecIndex)
		{
		case 0:
			assertEquals(prefix + "getEndpoint()", "Size".toUpperCase(), effRec.getEndpoint().toString());
			assertEquals(prefix + "getEndpointType()", "Average".toUpperCase(), effRec.getEndpointType());
			assertEquals(prefix + "getLoValue()", 300.0 + substNum, effRec.getLoValue());
			assertEquals(prefix + "getUnit()", "nm", effRec.getUnit());
			//Conditions
			conds = (IParams) effRec.getConditions();
			s = (String) conds.get("cond1");
			assertEquals(prefix + "conditipons cond1 value", "cond1-val", s);			
			break;
			
		case 1:
			assertEquals(prefix + "getEndpoint()", "Eff1".toUpperCase(), effRec.getEndpoint().toString());
			assertEquals(prefix + "getLoValue()", 100.0 * (substNum+1), effRec.getLoValue());
			assertEquals(prefix + "getErrorValue()", 80.0 + substNum, effRec.getErrorValue());
			assertEquals(prefix + "getUnit()", "K", effRec.getUnit());
			switch (substNum) {
			case 1:
				assertEquals(prefix + "getLoQualifier())", ">", effRec.getLoQualifier());
				break;
			case 2:
				assertEquals(prefix + "getLoQualifier())", "ca.", effRec.getLoQualifier());
				break;
			case 3:
				assertEquals(prefix + "getLoQualifier())", "~", effRec.getLoQualifier());
				break;	
			}
			//Conditions
			conds = (IParams) effRec.getConditions();
			s = (String) conds.get("cond11");
			assertEquals(prefix + "conditipons cond11 value", "cond1-" + substNum, s);			
			v = (IValue) conds.get("cond12");
			assertEquals(prefix + "conditipons cond12 value", 100.0 + substNum, v.getLoValue());
			assertEquals(prefix + "conditipons cond12 unit", "m", v.getUnits());
			break;
		case 2:
			assertEquals(prefix + "getEndpoint()", "Eff2".toUpperCase(), effRec.getEndpoint().toString());
			assertEquals(prefix + "getUpValue()", 50.0 + substNum, effRec.getUpValue());
			assertEquals(prefix + "getUnit()", "A", effRec.getUnit());
			//Conditions
			conds = (IParams) effRec.getConditions();
			v = (IValue) conds.get("cond21");
			assertEquals(prefix + "conditipons cond21 value", 0.0 + substNum, v.getLoValue());
			assertEquals(prefix + "conditipons cond21 LoQualifier", ">", v.getLoQualifier());
			break;
		case 3:
			assertEquals(prefix + "getEndpoint()", "Eff3".toUpperCase(), effRec.getEndpoint().toString());
			assertEquals(prefix + "getLoValue()", 0.0 + substNum, effRec.getLoValue());
			assertEquals(prefix + "getUpValue()", 56.0, effRec.getUpValue());
			assertEquals(prefix + "getLoQualifier())", ">=", effRec.getLoQualifier());
			assertEquals(prefix + "getUpQualifier())", "<=", effRec.getUpQualifier());
			assertEquals(prefix + "getUnit()", "Pa", effRec.getUnit());
			break;
		case 4:
			assertEquals(prefix + "getEndpoint()", "Eff4".toUpperCase(), effRec.getEndpoint().toString());
			assertEquals(prefix + "getLoValue()", 10.0 + substNum, effRec.getLoValue());
			assertEquals(prefix + "getUpValue()", 20.0 + substNum, effRec.getUpValue());
			assertEquals(prefix + "getLoQualifier())", ">=", effRec.getLoQualifier());
			assertEquals(prefix + "getUpQualifier())", "~", effRec.getUpQualifier());
			assertEquals(prefix + "getUnit()", "nm", effRec.getUnit());
			break;
		case 5:
			assertEquals(prefix + "getEndpoint()", "Eff5".toUpperCase(), effRec.getEndpoint().toString());
			assertEquals(prefix + "getTextValue()", "text-" + substNum, effRec.getTextValue());
			assertEquals(prefix + "getSampleID()", "sample-id-value", effRec.getSampleID());			
			break;	
		}
	}
	
	
	void testEffectBlocks02(SubstanceRecord r)
	{
		String prefix = "testing ";
		assertEquals(prefix + "getPublicName()", "TiO2", r.getPublicName());
		
		for (ProtocolApplication pa : r.getMeasurements())
		{	
			List<EffectRecord> effRecList = pa.getEffects();
			for (int i = 0; i < effRecList.size(); i++)
				testEffectRecord02(effRecList.get(i));
		}
		
		//System.out.println(r.toJSON(null));
		//System.out.println(r.getMeasurements().get(1).toString());
	}
	
	void testEffectRecord02(EffectRecord effRec)
	{
		String effEndpoint = effRec.getEndpoint().toString();
		String effEndPointType =  effRec.getEndpointType();
		IParams conds;
		IValue v;
		String s;
		
		if (effEndpoint.equalsIgnoreCase("Cell viability"))
		{		
			conds = (IParams)effRec.getConditions();			
			v = (IValue) conds.get("Time point");			
			int timeIndex = timePointIndex((Double)v.getLoValue());
			s = (String) conds.get("Replicate");			
			int rep = replicateToNum(s);
			v = (IValue) conds.get("Concentration");
			int concIndex = concentrationIndex((Double)v.getLoValue());

			double expEffValue;
			if (effEndPointType.equals("average raw data"))
				expEffValue = 0.3 + (double)concIndex*0.1 + rep*0.01 + (double)timeIndex * 0.002;
			else
				expEffValue = 100.0 - concIndex * (rep + timeIndex*3);

			String prefix = "Cell viability " + s + " concIndex " + concIndex + " timeIndex " + timeIndex;
			assertEquals(prefix + " getLoValue()", expEffValue, effRec.getLoValue(), 0.00000001);
			assertEquals(prefix + " getUnit()", "ng/ml", effRec.getUnit());
		}
		
		if (effEndpoint.equalsIgnoreCase("Intracellular LDH Control"))
		{			
			conds = (IParams)effRec.getConditions();			
			v = (IValue) conds.get("Time point");			
			int timeIndex = timePointIndex((Double)v.getLoValue());
			s = (String) conds.get("Replicate");			
			int rep = replicateToNum(s);
			double expEffValue = 0.9 + rep*0.01 + (double)timeIndex * 0.002;
			assertEquals("Intracellular LDH Control " + s + " timeIndex " + timeIndex +  
					" getLoValue()", expEffValue, effRec.getLoValue(), 0.00000001);
		}
		
		if (effEndpoint.equalsIgnoreCase("Eff1") ||
				effEndpoint.equalsIgnoreCase("Eff2") ||
				effEndpoint.equalsIgnoreCase("Eff3"))
		{
			conds = (IParams)effRec.getConditions();
			IValue conc = (IValue) conds.get("Concentration");
			int concIndex = concentrationIndex((Double)conc.getLoValue());
			Double indexVal = (Double) conds.get("Index");
			int index = indexVal.intValue(); 
			String replicate = (String) conds.get("Replicate");
			IValue p1 = (IValue) conds.get("p1");
			
			double expEffValue = 500 + 0.1*concIndex + 0.01*index;
			double expErrValue = expEffValue / 20.0;			
			String prefix = "Eff" + index + "  concIndex " + concIndex;
			
			//Check endpoint
			assertEquals(prefix + " getEndpoint() ", "Eff" + index, effRec.getEndpoint().toString() );
			assertEquals(prefix + " getLoValue() ", expEffValue, effRec.getLoValue() );
			assertEquals(prefix + " getLoQualifier() ", effQualifiers[index-1], effRec.getLoQualifier());
			assertEquals(prefix + " getErrorValue() ", expErrValue, effRec.getErrorValue() );
			assertEquals(prefix + " getErrQualifier() ", errQualifiers[concIndex], effRec.getErrQualifier());
			assertEquals(prefix + " getUnit ", "nm", effRec.getUnit());
				
			//Check conditions
			assertEquals(prefix + " condition Concentration ", "ug/ml" , conc.getUnits());
			
			assertEquals(prefix + " condition Replicate ", "Replicate 1", replicate);
			assertEquals(prefix + " condition p1.getUpValue() ", 3.0, p1.getUpValue());
			assertEquals(prefix + " condition p1.getUnits() ", "Pa", p1.getUnits());
			assertEquals(prefix + " condition p1.getUpQualifier() ", "<", p1.getUpQualifier());
		}
		
	}
	
	void testEffectBlocks03(SubstanceRecord r, int substNum)
	{
		//substNum is 1-based index
		String prefix = "Test03/substance #" + substNum;
		assertEquals(prefix + "getPublicName()", "Material" + substNum, r.getPublicName());
		assertEquals(prefix + "getExternalids().get(0).getSystemDesignator()", "Sample name", 
				r.getExternalids().get(0).getSystemDesignator());
		assertEquals(prefix + "getExternalids().get(0).getSystemDesignator()", "C" + substNum, 
				r.getExternalids().get(0).getSystemIdentifier());
		
		
		for (ProtocolApplication pa : r.getMeasurements())
		{	
			//TODO check protocol
			
			List<EffectRecord> effRecList = pa.getEffects();
			for (int i = 0; i < effRecList.size(); i++)
				testEffectRecord03(effRecList.get(i), substNum);
		}
		
		//System.out.println(r.toJSON(null));
		//System.out.println(r.getMeasurements().get(0).toString());
	}
	
	void testEffectRecord03(EffectRecord effRec, int substNum)
	{
		String effEndpoint = effRec.getEndpoint().toString();
		String effEndPointType =  effRec.getEndpointType();
		
		IParams conds = (IParams)effRec.getConditions();			
		String well = (String) conds.get("Well");
		String s = (String) conds.get("CONDITIONS_HTS");			
		int htsParNum = htsParamToNum(s);
		IValue conc = (IValue) conds.get("Concentration");
		int concIndex = concentrationIndex((Double)conc.getLoValue());
		
		String prefix = " HTS " + s + " concIndex " + concIndex;
		assertEquals(prefix + " condition Well", getWellLetter(substNum) + (concIndex+1), well);
		
		double expEffValue = 100000*substNum + 1000*htsParNum + (Double)conc.getLoValue();
		assertEquals(prefix + " getLoValue()", expEffValue, effRec.getLoValue());
		//assertEquals(prefix + " getUnit()", "ng/ml", effRec.getUnit());

	}
	
	void testEffectBlocks04(SubstanceRecord r, int substNum)
	{
		//substNum is 1-based index
		String prefix = "Test04/substance #" + substNum;
		assertEquals(prefix + "getPublicName()", "NM-10" + substNum, r.getPublicName());
		
				
		for (ProtocolApplication pa : r.getMeasurements())
		{						
			List<EffectRecord> effRecList = pa.getEffects();
			for (int i = 0; i < effRecList.size(); i++)
				testEffectRecord04(effRecList.get(i), substNum);
		}
		
		//System.out.println(r.toJSON(null));
		//System.out.println(r.getMeasurements().get(0).toString());
	}
	
	void testEffectRecord04(EffectRecord effRec, int substNum)
	{
		//TODO
	}
	

	
	int replicateToNum (String rep)
	{
		if (rep.equals("Replicate 1"))
			return 1;
		if (rep.equals("Replicate 2"))
			return 2;
		if (rep.equals("Replicate 3"))
			return 3;
		return 0;
	}
	
	int htsParamToNum (String par)
	{
		for (int i = 0; i < htsParams.length; i++)
			if (par.equals(htsParams[i]))
				return (i+1);
		return 0;
	}
	
	int concentrationIndex (double c)
	{
		for (int i = 0; i < concentrations.length; i++)
			if (c == concentrations[i])
				return i;
		return -1;
	}
	
	int timePointIndex (double t)
	{
		for (int i = 0; i < timePoints.length; i++)
			if (t == timePoints[i])
				return i;
		return -1;
	}
	
	String getWellLetter(int substNum)
	{
		switch (substNum)
		{
		case 1:
			return "L";
		case 2:
			return "M";
		case 3:
			return "N";
		}
		return null;
	}
		
	
	
	void checkParserConfiguration01 (GenericExcelParser parser)
	{
		String prefix = "Config 1: ";
		ExcelParserConfigurator conf = parser.getExcelParserConfigurator();
		assertEquals(prefix + "sheetIndex", 1, conf.sheetIndex + 1); //0-based --> 1-based
	}
	
	

}
