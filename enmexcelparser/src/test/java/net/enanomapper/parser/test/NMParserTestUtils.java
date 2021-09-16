package net.enanomapper.parser.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
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
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.study.StructureRecordValidator;
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
				
				System.out.println("Excel task result:");
				for (int i = 0; i < task.analysisResult.size(); i++) 
					System.out.println(task.analysisResult.get(i));
				
			}
			catch (Exception e) {
				System.out.println("Excel task exception: " + e.getMessage());
			}
		}
	}
	
	public static void testSubstanceAnalysisTaskParser(String saTaskStr) {
		System.out.println("Testing Substance Analysis task:\n" + saTaskStr + "\n");
		try {
			SubstanceAnalysisTask task = SubstanceAnalysisTask.parseFromString(saTaskStr);
			System.out.println(task.toString());
		} 
		catch (Exception e) {
			System.out.println("Excel Analysis Task errors:");
			System.out.println(e.getMessage());
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

}
