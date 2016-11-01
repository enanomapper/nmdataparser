package net.enanomapper.templates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

import ambit2.base.io.DownloadTool;

public class Tools {

	public static Properties initJRCTemplateNames() throws IOException {
		Properties templates = new Properties();
		InputStream in = Tools.class.getClassLoader().getResourceAsStream("data/xlsx/nanoreg/nrtemplates.properties");
		try {
			templates.load(in);
		} finally {
			in.close();
		}
		return templates;
	}

	public static void readIOMtemplates(File file, Object key, String templateName, Map<String, Term> histogram,
			BufferedWriter stats) throws InvalidFormatException, IOException {
		Workbook workbook;
		if (templateName.endsWith(".xlsx")) {
			workbook = new XSSFWorkbook(file);
		} else if (templateName.endsWith(".xls")) {
			workbook = new HSSFWorkbook(new FileInputStream(file));
		} else
			throw new InvalidFormatException(file.getName());

		Sheet sheet = workbook.getSheetAt(0);

		Iterator<Row> rowIterator = sheet.rowIterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			Cell cell1 = row.getCell(0);
			String value1 = cell1 == null ? null : getValue(cell1);
			Cell cell2 = row.getCell(1);
			String value2 = cell2 == null ? null : getValue(cell2);

			if (value1 == null || value2 == null)
				continue;
			gatherStats(value1, histogram);
			gatherStats(value2, histogram);
			stats.write(String.format("%s,\"%s\",%s,%d,%d,%d,%s,%s\n", key.toString(), templateName,
					sheet.getSheetName(), row.getRowNum(), cell1 == null ? -1 : cell1.getColumnIndex(),
					cell2 == null ? -1 : cell2.getColumnIndex(), value1, value2));
		}
		workbook.close();
	}

	protected static String getValue(Cell cell) {
		String value = null;
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING: {
			value = new String(cell.getStringCellValue().getBytes(Charset.forName("UTF-8")));
		//	System.out.println(value);
			value = value.toLowerCase().replace("\n", " ").replace("\r", "").trim();
			
			break;
		}
		case Cell.CELL_TYPE_FORMULA: {
			// skip for now, we are only looking at terms!
			break;
		}
		}
		return value;
	}

	protected static void gatherStats(String value, Map<String, Term> histogram) {
		if (value == null || "".equals(value))
			return;
		Term count = histogram.get(value);
		if (count == null) {
			Term term = new Term();
			term.setSecondbest(new Term());
			histogram.put(value, term);
		} else {
			count.setFrequency(count.getFrequency() + 1);
			histogram.put(value, count);
		}
		return;
	}

	public static void readJRCExcelTemplate(File file, Object key, String templateName, Map<String, Term> histogram,
			BufferedWriter stats, IAnnotator annotator) throws InvalidFormatException, IOException {

		Workbook workbook;
		if (templateName.endsWith(".xlsx")) {
			workbook = new XSSFWorkbook(file);
		} else if (templateName.endsWith(".xls")) {
			workbook = new HSSFWorkbook(new FileInputStream(file));
		} else
			throw new InvalidFormatException(file.getName());

		int nsh = workbook.getNumberOfSheets();
		TR record = new TR();
		for (int i = 0; i < nsh; i++) {
			Sheet sheet = workbook.getSheetAt(i);
			if ("instruction for data logging".equals(sheet.getSheetName().toLowerCase()))
				continue;
			int rows = 0;
			int maxcols = 0;
			HashFunction hf = Hashing.murmur3_32();

			Iterator<Row> rowIterator = sheet.rowIterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Iterator<Cell> cellIterator = row.cellIterator();
				int columns = 0;
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					String value = getValue(cell);
					try {
						if (value != null) {
							gatherStats(value, histogram);

							HashCode hc = hf.newHasher().putString(value, Charsets.UTF_8).hash();
							if (!"".equals(value.trim())) {
								if (annotator == null)
									stats.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",,,,,,,,\n", hc,
											key.toString(), templateName, sheet.getSheetName(), row.getRowNum(),
											cell.getColumnIndex(), value));
								else {
									record.clear();
									TR.hix.ID.set(record, hc);
									TR.hix.Folder.set(record, key.toString());
									TR.hix.File.set(record, templateName);
									TR.hix.Sheet.set(record, sheet.getSheetName());
									TR.hix.Row.set(record, row.getRowNum());
									TR.hix.Column.set(record, cell.getColumnIndex());
									TR.hix.Value.set(record, value);
									annotator.process(record);
									record.write(stats);
								}
							}

						}
					} catch (Exception x) {
						x.printStackTrace();
					}
					columns++;
				}
				rows++;
				if (columns > maxcols)
					maxcols = columns;
			}
			System.out.println(String.format("%s\t'%s'\t%s\t%d\t%d", key.toString(), templateName, sheet.getSheetName(),
					rows, maxcols));

		}
		workbook.close();
	}

	public static void smash(String rdfurl, String title, boolean splitfirstlevel, IProcessRDF processor)
			throws Exception {
		smash(rdfurl, title, splitfirstlevel, processor, "http://www.w3.org/2002/07/owl#Thing", "RDF/XML");
	}

	public static void smash(String rdfurl, String title, boolean splitfirstlevel, IProcessRDF processor,
			String rootResource, String format) throws Exception {
		Tools.smash(rdfurl, title, splitfirstlevel, processor, rootResource, format, null);
	}

	public static void smash(String rdfurl, String title, boolean splitfirstlevel, IProcessRDF processor,
			String rootResource, String format, String propertyURI) throws Exception {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		System.out.println("Downloading " + rdfurl);

		String ext = ".rdf";
		if (format.toUpperCase().equals("TURTLE"))
			ext = ".ttl";
		File file = getTestFile(rdfurl, title, ext, baseDir);
		System.out.println("Download completed " + file.getAbsolutePath());
		Model jmodel = ModelFactory.createDefaultModel();

		if (propertyURI != null) {
			processor.setHproperty(jmodel.createProperty(propertyURI));
		}
		InputStreamReader in = null;
		try {
			RDFReader reader = jmodel.getReader(format);

			if (rdfurl.endsWith(".gz"))
				in = new InputStreamReader(new GZIPInputStream(new FileInputStream(file)));
			else
				in = new InputStreamReader(new FileInputStream(file));
			reader.read(jmodel, in, format);
			System.out.println("Reading completed " + file.getAbsolutePath());
			Resource root = jmodel.createResource(rootResource);

			if (!splitfirstlevel) {
				String outname = String.format("%s_tree_%s.%s", title, root.getLocalName(),
						processor.getFileExtension());
				BufferedWriter out = new BufferedWriter(new FileWriter(new File(baseDir, outname)));
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
				ResIterator thingi = jmodel.listSubjectsWithProperty(RDFS.subClassOf, root);
				while (thingi.hasNext()) {
					Resource thing = thingi.next();
					BufferedWriter out = null;

					ResIterator entityi = jmodel.listSubjectsWithProperty(RDFS.subClassOf, thing);

					while (entityi.hasNext())
						try {
							Resource entity = entityi.next();
							String outname = String.format("%s_tree_%s.%s", title, entity.getLocalName(),
									processor.getFileExtension());
							out = new BufferedWriter(new FileWriter(new File(baseDir, outname)));
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

	public static File getTestFile(String remoteurl, String localname, String extension, File baseDir)
			throws Exception {
		URL url = new URL(remoteurl);
		boolean gz = remoteurl.endsWith(".gz");
		File file = new File(baseDir, localname + extension + (gz ? ".gz" : ""));
		if (!file.exists())
			DownloadTool.download(url, file);
		return file;
	}
}
