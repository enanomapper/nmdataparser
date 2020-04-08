package net.enanomapper.parser.test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.ProtocolApplication;
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
		assertEquals(prefix + "getOwnerUUID()", "owner-" + substNum, subRec.getOwnerUUID());
		assertEquals(prefix + "getSubstancetype()", "NPO_1317", subRec.getSubstancetype());
		
		
		
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
	
	void checkParserConfiguration01 (GenericExcelParser parser)
	{
		String prefix = "Config 1: ";
		ExcelParserConfigurator conf = parser.getExcelParserConfigurator();
		assertEquals(prefix + "sheetIndex", 1, conf.sheetIndex + 1); //0-based --> 1-based
	}

}
