package net.enanomapper.parser.test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import ambit2.base.data.SubstanceRecord;
import net.enanomapper.parser.GenericExcelParser;

public class NMParserTest {
	@Test
	public void test_empty() throws Exception {
		
	}
	// @Test
	//Disabled - the xlsx file does not have sheet 3, which is refered by the json. 
	
	public void test() throws Exception {
		// this will close the inputstream automatically
		try (InputStream fin = getClass().getClassLoader()
				.getResourceAsStream("net/enanomapper/parser/testExcelParser/testfile1.xlsx")) {
			boolean isXLSX = true;
			URL url = getClass().getClassLoader()
					.getResource("net/enanomapper/parser/testExcelParser/testfile1-config.json");
			try (GenericExcelParser parser = new GenericExcelParser(fin, new File(url.getFile()), isXLSX)) {

				// System.out.println(parser.getExcelParserConfigurator().toJSONString()
				// + "\n");
				
				int n = 0;
				while (parser.hasNext()) {
					SubstanceRecord r = parser.nextRecord();
					n++;
					System.out.println("Record #" + n);
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
			} catch (Exception x) {
				Logger.getAnonymousLogger().log(Level.SEVERE,x.getMessage());
				throw x;
			}
		} catch (Exception x1) {
			Logger.getAnonymousLogger().log(Level.SEVERE,x1.getMessage());
			throw x1;
		}
	}

}
