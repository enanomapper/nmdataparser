package net.enanomapper.parser.test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.TreeMap;

import junit.framework.Assert;
import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.GenericExcelParser;
import net.enanomapper.parser.excel.ExcelUtils;
import net.enanomapper.parser.recognition.RichValue;
import net.enanomapper.parser.recognition.RichValueParser;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import ambit2.base.data.Property;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.relation.composition.CompositionRelation;

public class NMParserTestUtils {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// IterationAccess.ROW_MULTI_DYNAMIC.test();

		// testExcelParserConfiguration("/Users/nick/Projects/eNanoMapper/config01.json");
		// testExcelParserConfiguration("D:/Projects/nina/eNanoMapper/config01.json");

		testExcelTemplate(
				"/Users/nick/Projects/eNanoMapper/template01-NR.xlsx",
				"/Users/nick/Projects/eNanoMapper/config01.json");
		// testExcelTemplate("D:/Projects/nina/eNanoMapper/template01-NR.xlsx","D:/Projects/nina/eNanoMapper/config01.json");

		// System.out.println(IterationAccess.fromString("ROW_SINGLE"));

	}

	public static void testExcelParserConfiguration(String jsonFile)
			throws Exception {
		ExcelParserConfigurator parserConfig = ExcelParserConfigurator
				.loadFromJSON(jsonFile);

		if (parserConfig.configErrors.size() > 0) {
			System.out.println("GenericExcelParser configuration errors:\n"
					+ parserConfig.getAllErrorsAsString());
			return;
		}

		System.out.println("ExcelParserConfigurator " + jsonFile);
		System.out.println(parserConfig.toJSONString());
	}

	public static void testExcelTemplate(String excelFile, String jsonFile)
			throws Exception {
		FileInputStream fin = new FileInputStream(excelFile);
		boolean isXLSX = excelFile.endsWith("xlsx");
		System.out.println("isXLSX = " + isXLSX + "\n");
		GenericExcelParser parser = new GenericExcelParser(fin, jsonFile,
				isXLSX);
		System.out.println(parser.getExcelParserConfigurator().toJSONString()
				+ "\n");

		int n = 0;
		while (parser.hasNext()) {
			SubstanceRecord r = parser.nextRecord();
			n++;
			System.out.println("Record #" + n);
			System.out.println(r.toJSON(null));
			List<ProtocolApplication> paList = r.getMeasurements();

			if (paList != null)
				for (ProtocolApplication pa : paList)
					System.out.println("***Protocol application:\n"
							+ pa.toString());
			
			List<CompositionRelation> composition = r.getRelatedStructures();
			if (composition != null)
				for (CompositionRelation relation : composition)
				{	
					//System.out.println(" ### Composition " + structureRecordToString(relation.getSecondStructure()));
					System.out.println(" ### Composition \n" + compositionRelationStructureToString(relation));  //both give the same result
					System.out.println(" ### Properties: " + structureRecordProperties(relation.getSecondStructure()));
				}	
			
			
		}

		if (parser.hasErrors())
			System.out
					.println("\n\nParser errors:\n" + parser.errorsToString());

		fin.close();

	}
	
	public static void testRichValue(String rvString)
	{
		System.out.println("Testing RichValue: " + rvString);
		
		RichValueParser par = new RichValueParser();
		RichValue rv = par.parse(rvString);
		
		String errors = par.getAllErrorsAsString(); 
		if (errors != null)
		{
			System.out.println("RichValueParser errors:\n" + errors);
		}
		else
		{
			System.out.println(rv.toString());
		}
	}
	
	public static void testGetRowGroups(String excelFile, int sheetIndex, int startRowIndex, int endRowIndex, int keyColumnIndex, 
			boolean recognizeGroupByNextNonEmpty) throws Exception
	{
		FileInputStream fin = new FileInputStream(excelFile);
		boolean isXLSX = excelFile.endsWith("xlsx");
		System.out.println("isXLSX = " + isXLSX + "\n");
		
		Workbook workbook;
		if (isXLSX)
			workbook = new XSSFWorkbook(fin);
		else
			workbook = new HSSFWorkbook(fin);
		
		//All data is expected as 1-based indexed
		Sheet sheet = workbook.getSheetAt(sheetIndex-1);
		TreeMap<Integer, String> groups = 
				ExcelUtils.getRowGroups(sheet, startRowIndex-1, (endRowIndex == -1)?sheet.getLastRowNum():(endRowIndex-1), keyColumnIndex-1, recognizeGroupByNextNonEmpty);
		
		for (Integer key : groups.keySet())
			System.out.println("Group starts at row " + (key+1) + "   " + groups.get(key));
		
		
		fin.close();
	}
	
	public static String structureRecordToString(IStructureRecord str)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Content : " + str.getContent());
		sb.append("  Format : " + str.getFormat());
		sb.append("  Smiles : " + str.getSmiles());
		sb.append("  Inchi : " + str.getInchi());
		sb.append("  InchiKey : " + str.getInchiKey());
		return sb.toString();
	}
	
	public static String structureRecordProperties(IStructureRecord str)
	{
		StringBuffer sb = new StringBuffer();
		for (Property p : str.getProperties())
		{
			sb.append("    " + p.getName() + ": " + str.getProperty(p));
		}
		return sb.toString();
	}
	
	public static String compositionRelationStructureToString(CompositionRelation rel)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("  Content : \"" + rel.getContent() + "\"\n");
		sb.append("  Format : \"" + rel.getFormat() + "\"\n");
		sb.append("  Smiles : \"" + rel.getSmiles() + "\"\n");
		sb.append("  Formula : \"" + rel.getFormula() + "\"\n");
		sb.append("  Inchi : \"" + rel.getInchi() + "\"\n");
		sb.append("  InchiKey : \"" + rel.getInchiKey() + "\"\n");
		return sb.toString();
	}
	
	//@Test
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
		GenericExcelParser parser = new GenericExcelParser(xlsx,
				json.getFile(), true);
		try {
			while (parser.hasNext()) {
				SubstanceRecord r = parser.nextRecord();
				Assert.assertNotNull(r.getCompanyUUID());
				Assert.assertNotNull(r.getPublicName());
				Assert.assertNotNull(r.getMeasurements());
				Assert.assertTrue(r.getMeasurements().size()>0);
				System.out.println(r.toJSON("http://localhost/"));
				System.out.println(r.getMeasurements());
				for (ProtocolApplication pa : r.getMeasurements()) {
					Assert.assertEquals(r.getCompanyUUID(),  pa.getSubstanceUUID());
					Assert.assertEquals(r.getOwnerName(),  pa.getCompanyName());
					Assert.assertNotNull(pa.getProtocol());
				}
			}
		} finally {
			parser.close();
		}
	}

	@Test
	public void testProteinCoronaXLSX_1() throws Exception {
		InputStream xlsx = this
				.getClass()
				.getClassLoader()
				.getResourceAsStream(
						"net/enanomapper/parser/csv/ProteinCoronaTest1.xlsx");
		URL json = this
				.getClass()
				.getClassLoader()
				.getResource(
						"net/enanomapper/parser/csv/ProteinCoronaTest1.json");
		GenericExcelParser parser = new GenericExcelParser(xlsx,
				json.getFile(), true);
		try {
			while (parser.hasNext()) {
				SubstanceRecord r = parser.nextRecord();
				Assert.assertNotNull(r.getCompanyUUID());
				Assert.assertNotNull(r.getPublicName());
				Assert.assertNotNull(r.getMeasurements());
				Assert.assertTrue(r.getMeasurements().size()>0);
				//System.out.println(r.toJSON("http://localhost/"));
				Assert.assertNotNull(r.getExternalids());
				Assert.assertTrue(r.getExternalids().size()>0);
				Assert.assertEquals("Classification",r.getExternalids().get(0).getSystemDesignator());
				Assert.assertNotNull(r.getRelatedStructures());
				System.out.println(r.getMeasurements());
				for (ProtocolApplication pa : r.getMeasurements()) {
					Assert.assertEquals(r.getCompanyUUID(),  pa.getSubstanceUUID());
					Assert.assertEquals(r.getOwnerName(),  pa.getCompanyName());
					Assert.assertNotNull(pa.getProtocol());
				}
			}
		} finally {
			parser.close();
		}
	}
}
