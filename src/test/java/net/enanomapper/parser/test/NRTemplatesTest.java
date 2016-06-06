package net.enanomapper.parser.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.BeforeClass;
import org.junit.Test;

public class NRTemplatesTest extends TestWithExternalFiles {
	protected static Properties templates;

	@BeforeClass
	public static void initTemplateNames() throws IOException {
		templates = new Properties();
		InputStream in = NRTemplatesTest.class
				.getClassLoader()
				.getResourceAsStream("data/xlsx/nanoreg/nrtemplates.properties");
		try {
			Assert.assertNotNull(in);
			templates.load(in);
		} finally {
			in.close();
		}
	}

	@Test
	public void testTemplatesAvailable() throws Exception {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		Assert.assertNotNull(templates);
		Assert.assertEquals(15, templates.size());
		Enumeration<Object> e = templates.keys();
		Map<String, Integer> histogram = new HashMap<String, Integer>();
		BufferedWriter stats = new BufferedWriter(new FileWriter(new File(
				baseDir + "/nrtemplate.txt")));
		while (e.hasMoreElements())
			try {
				Object key = e.nextElement();
				String fileUrl = templates.getProperty(key.toString());
				File file = getTestFile(fileUrl, key.toString(), ".xlsx",
						baseDir);
				Assert.assertTrue(file.exists());
				// verify we can read it and extract some stats
				XSSFWorkbook workbook = new XSSFWorkbook(file);
				int nsh = workbook.getNumberOfSheets();
				for (int i = 0; i < nsh; i++) {
					XSSFSheet sheet = workbook.getSheetAt(i);
					if ("instruction for data logging".equals(sheet
							.getSheetName().toLowerCase()))
						continue;
					int rows = 0;
					int maxcols = 0;
					Iterator<Row> rowIterator = sheet.rowIterator();
					while (rowIterator.hasNext()) {
						Row row = rowIterator.next();
						Iterator<Cell> cellIterator = row.cellIterator();
						int columns = 0;
						while (cellIterator.hasNext()) {
							Cell cell = cellIterator.next();
							try {
								String value = cell.getStringCellValue()
										.toLowerCase().replace("\n", " ")
										.replace("\r", "").trim();
								if ("".equals(value))
									continue;
								Integer count = histogram.get(value);
								if (count == null) {
									histogram.put(value, 1);
								} else {
									count++;
									histogram.put(value, count);
								}
								if (!"".equals(value.trim()))
									stats.write(String.format(
											"%s\t\"%s\"\t%s\t%d\t%d\t%s\n",
											key.toString(), templates.get(key),
											sheet.getSheetName(), rows,
											columns, value));
							} catch (Exception x) {
								x.printStackTrace();
							}
							columns++;
						}
						rows++;
						if (columns > maxcols)
							maxcols = columns;
					}
					System.out.println(String.format("%s\t'%s'\t%s\t%d\t%d",
							key.toString(), templates.get(key),
							sheet.getSheetName(), rows, maxcols));

				}
				workbook.close();
			} catch (Exception x) {

			}
		stats.close();
		// histogram
		BufferedWriter terms = new BufferedWriter(new FileWriter(new File(
				baseDir + "/terms.txt")));
		BufferedWriter similar = new BufferedWriter(new FileWriter(new File(
				baseDir + "/similar.txt")));
		Iterator<String> terms1 = histogram.keySet().iterator();

		LevensteinDistance d = new LevensteinDistance();
		while (terms1.hasNext()) {
			String key1 = terms1.next();
			Iterator<String> terms2 = histogram.keySet().iterator();
			double max = 0;
			String mostSimilar = null;
			while (terms2.hasNext()) {
				String key2 = terms2.next();
				if (key1.equals(key2))
					continue;
				// avoid running twice on the same pair
				// if (key1.compareTo(key2) > 0)
				double sim = d.getDistance(key1, key2);
				if (sim > max) {
					mostSimilar = key2;
					max = sim;
				}
				terms.write(String.format("\"%s\"\t\"%s\"\t%s\n", key1, key2,
						sim));
			}
			similar.write(String.format("\"%s\"\t\"%s\"\t%s\n", key1,
					mostSimilar, max));
		}
		terms.close();
		similar.close();
	}
}
