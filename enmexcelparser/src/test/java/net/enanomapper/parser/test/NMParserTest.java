package net.enanomapper.parser.test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ambit2.base.data.Property;
import ambit2.base.data.SubstanceRecord;
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
					System.out.println("Record #" + n);
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
	
	void checkRecord(SubstanceRecord subRec, int substNum)
	{
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
		
		
		
		//System.out.println(r.toJSON(null));
		//List<ProtocolApplication> paList = r.getMeasurements();
		 
		//if (paList != null) for (ProtocolApplication pa : paList)
		//	System.out.println( "***Protocol application:\n" +
		//  pa.toString());
		  
		
		/*
		 * System.out.println(r.toJSON(null));
		 * List<ProtocolApplication> paList = r.getMeasurements();
		 * 
		 * if (paList != null) for (ProtocolApplication pa : paList)
		 * System.out.println( "***Protocol application:\n" +
		 * pa.toString());
		 * 
		 * List<CompositionRelation> composition = r
		 * .getRelatedStructures(); if (composition != null) for
		 * (CompositionRelation relation : composition) { //
		 * System.out.println(" ### Composition " + //
		 * structureRecordToString(relation.getSecondStructure()));
		 * System.out.println( " ### Composition \n" +
		 * NMParserTestUtils.compositionRelationStructureToString(
		 * relation)); // both give // the same result
		 * System.out.println( " ### Properties: " +
		 * NMParserTestUtils.structureRecordProperties(relation
		 * .getSecondStructure())); }
		 * 
		 */		
	}
	
	void testCompositionRelation(int substNum, int constituentIndex, CompositionRelation compRel)
	{
		String prefix = "Substance " + substNum + ", constituent " + constituentIndex + " : ";
		
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
	
	void checkParserConfiguration01 (GenericExcelParser parser)
	{
		String prefix = "Config 1: ";
		ExcelParserConfigurator conf = parser.getExcelParserConfigurator();
		assertEquals(prefix + "sheetIndex", 1, conf.sheetIndex + 1); //0-based --> 1-based
	}
	
	

}
