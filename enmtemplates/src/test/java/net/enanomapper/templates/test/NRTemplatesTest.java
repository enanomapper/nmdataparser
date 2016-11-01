package net.enanomapper.templates.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;
import net.enanomapper.templates.ExtractSynonymsList;
import net.enanomapper.templates.Term;
import net.enanomapper.templates.Tools;

public class NRTemplatesTest extends TestWithExternalFiles {
	protected static Properties templates;

	@BeforeClass
	public static void initTemplateNames() throws IOException {
		templates = Tools.initJRCTemplateNames();
	}

	@Test
	public void testTemplatesAvailable() throws Exception {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		Assert.assertNotNull(templates);
		Assert.assertEquals(33, templates.size());
		Enumeration<Object> e = templates.keys();
		final Map<String, Term> histogram = new HashMap<String, Term>();
		XSSFWorkbook workbook = new XSSFWorkbook();
		FileOutputStream out = new FileOutputStream(new File(baseDir + "/nrtemplate.xlsx"));
		
		XSSFSheet stats = workbook.createSheet();
		int rownum=0;
		while (e.hasMoreElements())
			try {
				Object key = e.nextElement();
				String fileUrl = templates.getProperty(key.toString());
				File file = Tools.getTestFile(fileUrl, key.toString(), ".xlsx", baseDir);
				Assert.assertTrue(file.exists());
				// verify we can read it and extract some stats
				rownum = Tools.readJRCExcelTemplate(file, key, templates.get(key).toString(), histogram, stats, null, rownum+1);
			} catch (Exception x) {

			}
		workbook.write(out);
		out.close();
		workbook.close();

		similarity(histogram, baseDir);
		System.out.println("Estimating annotations");
		Tools.smash(
				"http://data.bioontology.org/ontologies/ENM/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
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
								if (term.getSecondbest() == null)
									term.setSecondbest(new Term());
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
		BufferedWriter annotated = new BufferedWriter(new FileWriter(new File(baseDir + "/annotated.txt")));
		Iterator<String> terms1 = histogram.keySet().iterator();
		while (terms1.hasNext()) {
			String key1 = terms1.next();
			Term term = histogram.get(key1);
			annotated.write(String.format("\"%s\"\t%s\n", key1, term.toString()));
		}
		annotated.close();

	}

	protected void similarity(Map<String, Term> histogram, File baseDir) throws IOException {
		// histogram
		BufferedWriter terms = new BufferedWriter(new FileWriter(new File(baseDir + "/terms.txt")));
		BufferedWriter similar = new BufferedWriter(new FileWriter(new File(baseDir + "/similar.txt")));
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
				terms.write(String.format("\"%s\"\t\"%s\"\t%s\n", key1, key2, sim));
			}
			if (max >= 0.5)
				similar.write(String.format("\"%s\"\t\"%s\"\t%s\n", key1, mostSimilar, max));
		}
		terms.close();
		similar.close();
	}
}
