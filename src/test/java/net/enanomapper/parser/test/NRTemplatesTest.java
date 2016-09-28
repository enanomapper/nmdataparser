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
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
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

	protected void readExcelTemplate(File file, Object key, Map<String, Term> histogram, BufferedWriter stats) throws InvalidFormatException, IOException {
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
						Term count = histogram.get(value);
						if (count == null) {
							Term term = new Term();
							term.setSecondbest(new Term());
							histogram.put(value, term);
						} else {
							count.setFrequency(count.getFrequency() + 1);
							histogram.put(value, count);
						}
						//try to split the term 
						/*
						String[] splitted = value.split(" ");
						for (int ii=0;ii<splitted.length;ii++) {
							String val = splitted[ii].trim();
							if ("".equals(val)) continue;
							Term scount = histogram.get(val);
							if (scount == null) {
								histogram.put(val, new Term());
							} else {
								scount.setFrequency(scount.getFrequency() + 1);
								histogram.put(val, scount);
							}
						}
						*/	
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
	}
	@Test
	public void testTemplatesAvailable() throws Exception {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		Assert.assertNotNull(templates);
		Assert.assertEquals(15, templates.size());
		Enumeration<Object> e = templates.keys();
		final Map<String, Term> histogram = new HashMap<String, Term>();
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
				readExcelTemplate(file , key, histogram, stats);
			} catch (Exception x) {

			}
		stats.close();

		similarity(histogram, baseDir);
		System.out.println("Estimating annotations");
		smash("http://data.bioontology.org/ontologies/ENM/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"ENM", true, new ExtractSynonymsList() {
					LevensteinDistance d = new LevensteinDistance();

					@Override
					public void processEntry(String entity, String label) {
						Iterator<String> terms1 = histogram.keySet().iterator();
						while (terms1.hasNext()) {
							String key1 = terms1.next();
							double sim = d.getDistance(key1, label);
							Term term = histogram.get(key1);
							if (sim > term.getDistance()) {
								if (term.getSecondbest()==null) term.setSecondbest(new Term());
								term.getSecondbest().setDistance(term.getDistance());
								term.getSecondbest().setAnnotation(term.getAnnotation());
								term.getSecondbest().setLabel(term.getLabel());
								term.getSecondbest().setFrequency(term.getFrequency());
								term.setDistance(sim);
								term.setAnnotation(entity);
								term.setLabel(label);
							}
						}
					}
				});
		System.out.println("Writing annotated terms");
		BufferedWriter annotated = new BufferedWriter(new FileWriter(new File(
				baseDir + "/annotated.txt")));
		Iterator<String> terms1 = histogram.keySet().iterator();
		while (terms1.hasNext()) {
			String key1 = terms1.next();
			Term term = histogram.get(key1);
			annotated.write(String.format("\"%s\"\t%s\n", key1, term.toString()));
		}
		annotated.close();

	}

	protected void similarity(Map<String, Term> histogram, File baseDir)
			throws IOException {
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
			if (max >= 0.5)
				similar.write(String.format("\"%s\"\t\"%s\"\t%s\n", key1,
						mostSimilar, max));
		}
		terms.close();
		similar.close();
	}
}

class Term {
	protected Term secondbest;
	public Term getSecondbest() {
		return secondbest;
	}

	public void setSecondbest(Term secondbest) {
		this.secondbest = secondbest;
	}

	public Term() {
		setFrequency(1);
		setDistance(0);
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	int frequency;

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	String annotation;
	String label;
	double distance;

	@Override
	public String toString() {
		return String.format("%d\t\"%s\"\t\"%s\"\t%s\t%s", frequency, annotation,
				label, distance,secondbest);
	}
}
