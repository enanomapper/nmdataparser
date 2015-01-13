package net.enanomapper.parser.test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import junit.framework.Assert;
import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.GenericExcelParser;

import org.junit.Test;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.ProtocolApplication;

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
		}

		if (parser.hasErrors())
			System.out
					.println("\n\nParser errors:\n" + parser.errorsToString());

		fin.close();

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
		GenericExcelParser parser = new GenericExcelParser(xlsx,
				json.getFile(), true);
		try {
			while (parser.hasNext()) {
				SubstanceRecord r = parser.nextRecord();
				Assert.assertNotNull(r.getCompanyUUID());
				Assert.assertNotNull(r.getMeasurements());
				Assert.assertTrue(r.getMeasurements().size()>0);
				
				System.out.println(r.getMeasurements());
			}
		} finally {
			parser.close();
		}
	}

}
