package net.enanomapper.templates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import junit.framework.Assert;
import net.enanomapper.parser.test.NRTemplatesTest;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ambit2.base.io.DownloadTool;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Tools {

	public static Properties initJRCTemplateNames() throws IOException {
		Properties templates = new Properties();
		InputStream in = NRTemplatesTest.class
				.getClassLoader()
				.getResourceAsStream("data/xlsx/nanoreg/nrtemplates.properties");
		try {
			Assert.assertNotNull(in);
			templates.load(in);
		} finally {
			in.close();
		}
		return templates;
	}

	public static void readJRCExcelTemplate(File file, Object key,
			String templateName, Map<String, Term> histogram,
			BufferedWriter stats) throws InvalidFormatException, IOException {
		XSSFWorkbook workbook = new XSSFWorkbook(file);
		int nsh = workbook.getNumberOfSheets();
		for (int i = 0; i < nsh; i++) {
			XSSFSheet sheet = workbook.getSheetAt(i);
			if ("instruction for data logging".equals(sheet.getSheetName()
					.toLowerCase()))
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
						String value = cell.getStringCellValue().toLowerCase()
								.replace("\n", " ").replace("\r", "").trim();
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
						// try to split the term
						/*
						 * String[] splitted = value.split(" "); for (int
						 * ii=0;ii<splitted.length;ii++) { String val =
						 * splitted[ii].trim(); if ("".equals(val)) continue;
						 * Term scount = histogram.get(val); if (scount == null)
						 * { histogram.put(val, new Term()); } else {
						 * scount.setFrequency(scount.getFrequency() + 1);
						 * histogram.put(val, scount); } }
						 */
						if (!"".equals(value.trim()))
							stats.write(String.format(
									"%s\t\"%s\"\t%s\t%d\t%d\t%s\n",
									key.toString(), templateName,
									sheet.getSheetName(), rows, columns, value));
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
					key.toString(), templateName, sheet.getSheetName(), rows,
					maxcols));

		}
		workbook.close();
	}

	public static void smash(String rdfurl, String title,
			boolean splitfirstlevel, IProcessRDF processor) throws Exception {
		smash(rdfurl, title, splitfirstlevel, processor,
				"http://www.w3.org/2002/07/owl#Thing", "RDF/XML");
	}

	public static void smash(String rdfurl, String title,
			boolean splitfirstlevel, IProcessRDF processor,
			String rootResource, String format) throws Exception {
		Tools.smash(rdfurl, title, splitfirstlevel, processor, rootResource,
				format, null);
	}

	public static void smash(String rdfurl, String title,
			boolean splitfirstlevel, IProcessRDF processor,
			String rootResource, String format, String propertyURI)
			throws Exception {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		System.out.println("Downloading " + rdfurl);

		String ext = ".rdf";
		if (format.toUpperCase().equals("TURTLE"))
			ext = ".ttl";
		File file = getTestFile(rdfurl, title, ext, baseDir);
		Assert.assertTrue(file.exists());
		System.out.println("Download completed " + file.getAbsolutePath());
		Model jmodel = ModelFactory.createDefaultModel();

		if (propertyURI != null) {
			processor.setHproperty(jmodel.createProperty(propertyURI));
		}
		InputStreamReader in = null;
		try {
			RDFReader reader = jmodel.getReader(format);

			if (rdfurl.endsWith(".gz"))
				in = new InputStreamReader(new GZIPInputStream(
						new FileInputStream(file)));
			else
				in = new InputStreamReader(new FileInputStream(file));
			reader.read(jmodel, in, format);
			System.out.println("Reading completed " + file.getAbsolutePath());
			Resource root = jmodel.createResource(rootResource);

			if (!splitfirstlevel) {
				String outname = String.format("%s_tree_%s.%s", title,
						root.getLocalName(), processor.getFileExtension());
				BufferedWriter out = new BufferedWriter(new FileWriter(
						new File(baseDir, outname)));
				System.out.println("Writing tree into " + outname);
				try {
					processor.traverse(root, jmodel, 0, out);
				} finally {
					try {
						out.close();
					} catch (Exception x) {
					}
				}

			} else {
				int c = 1;
				ResIterator thingi = jmodel.listSubjectsWithProperty(
						RDFS.subClassOf, root);
				while (thingi.hasNext()) {
					Resource thing = thingi.next();
					BufferedWriter out = null;

					ResIterator entityi = jmodel.listSubjectsWithProperty(
							RDFS.subClassOf, thing);

					while (entityi.hasNext())
						try {
							Resource entity = entityi.next();
							String outname = String.format("%s_tree_%s.%s",
									title, entity.getLocalName(),
									processor.getFileExtension());
							out = new BufferedWriter(new FileWriter(new File(
									baseDir, outname)));
							System.out.println("Writing tree into " + outname);
							processor.traverse(entity, jmodel, 0, out);
						} finally {
							try {
								out.close();
							} catch (Exception x) {
							}
						}

					c++;
				}
			}

		} finally {
			jmodel.close();
			try {
				if (in != null)
					in.close();
			} catch (Exception x) {
			}

		}
	}

	public static File getTestFile(String remoteurl, String localname,
			String extension, File baseDir) throws Exception {
		URL url = new URL(remoteurl);
		boolean gz = remoteurl.endsWith(".gz");
		File file = new File(baseDir, localname + extension + (gz ? ".gz" : ""));
		if (!file.exists())
			DownloadTool.download(url, file);
		return file;
	}
}
