package net.idea.templates.annotation.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.enanomapper.maker.JsonConfigAnnotator;
import net.enanomapper.maker.TR;
import net.idea.templates.annotation.AbstractAnnotationProcessor;
import net.idea.templates.annotation.AnnotatorChain;

public class TestExportAnnotate {

	public static void main(String[] args) throws Exception {
		if (args[0] == null)
			throw new Exception("No input file specified");
		JsonConfigAnnotator jc=null;
		if (args.length>1)
			jc = new JsonConfigAnnotator(new File(args[1]));
		
		File file = new File(args[0]);
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("templates.json")));
		Workbook workbook = new XSSFWorkbook(file);
		Sheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.rowIterator();

		Row row0 = sheet.getRow(0);

		AnnotatorChain a = new AnnotatorChain();
		if (jc!=null)
			a.add(jc);
		/*
		AnnotationToolsOntology[] oo = new AnnotationToolsOntology[] { 
				new AnnotationToolsOntology("net/idea/templates/ontology/merged_enm.rdf.gz", "merged_enm.rdf.gz", "enm",
						"definition ", 3),
				new AnnotationToolsOntology("net/idea/templates/ontology/merged_bao.rdf.gz", "merged_bao.rdf.gz", "bao",
						"definition ", 3),
				new AnnotationToolsOntology("net/idea/templates/ontology/cseo.rdf.gz", "cseo.rdf.gz", "cseo", "label", 3),
				new AnnotationToolsOntology("net/idea/templates/ontology/ncit.rdf.gz", "ncit.rdf.gz", "ncit", "Preferred_Name",
						3),
				new AnnotationToolsOntology("net/idea/templates/ontology/chmo.rdf.gz", "chmo.rdf.gz", "chmo", "label", 3) };
		for (AnnotationToolsOntology o : oo)
			try {
				o.open();
				a.add(o);
			} catch (Exception x) {
				System.err.println(o.getTerm_tag());
				x.printStackTrace();
				// System.exit(-1); }
		 */
		HashMap<String, TR> assay_terms = new HashMap<String, TR>();
		HashMap<String, TR> endpoint_terms = new HashMap<String, TR>();
		HashMap<String, TR> field_terms = new HashMap<String, TR>();

		writer.write("[\n");
		String d = "";
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Iterator<Cell> cellIterator = row.cellIterator();
			writer.write(d);

			String comma = "\t";
			String cleanedvalue = null;
			String Sheet = null;
			String module = null;
			String endpoint = null;
			String annotation = null;
			String fileName = null;

			TR tr = new TR();
			while (cellIterator.hasNext())
				try {
					Cell cell = cellIterator.next();
					String name = row0.getCell(cell.getColumnIndex()).getStringCellValue();
					String jsonname = name;

					if ("Sheet".equals(jsonname))
						jsonname = "s_uuid";
					else if ("ID".equals(jsonname))
						jsonname = "id";
					if ("Warning".equals(jsonname) || "term_score".equals(jsonname) || "term_label".equals(jsonname)
							|| "term_uri".equals(jsonname))
						continue;

					switch (cell.getCellType()) {
					case STRING: {

						String value = new String(cell.getStringCellValue().getBytes(Charset.forName("UTF-8")));
						if ("".equals(value))
							continue;
						if ("cleanedvalue".equals(jsonname)) {
							cleanedvalue = value;
							if (cleanedvalue == "" || cleanedvalue == null)
								cleanedvalue = "WARNING_EMPTY_VALUE";
						}
						if ("endpoint".equals(jsonname))
							endpoint = value;
						else if ("JSON_LEVEL1".equals(jsonname))
							tr.put(jsonname, value);
						else if ("JSON_LEVEL2".equals(jsonname))
							tr.put(jsonname, value);
						else if ("JSON_LEVEL3".equals(jsonname))
							tr.put(jsonname, value);
						else if ("Annotation".equals(jsonname))
							annotation = value;
						else if ("s_uuid".equals(jsonname))
							Sheet = value;
						else if ("File".equals(jsonname))
							try {
								String[] v = value.split("_");
								module = v[0];
								fileName = value;
							} catch (Exception x) {
								module = "";
							}
						tr.put(jsonname, value);

						break;
					}
					case NUMERIC: {
						Double value = cell.getNumericCellValue();
						if ("Row".equals(jsonname) || "Column".equals(jsonname))
							tr.put(jsonname, value.intValue());
						else
							tr.put(jsonname, value);
						break;
					}
					case FORMULA: {
						break;
					}
					}
					comma = "\t,";
				} catch (Exception x) {
					x.printStackTrace();
				}

			if (module != null)
				tr.put("module", module);
			if (endpoint != null)
				tr.put("endpoint", endpoint);

			if (cleanedvalue != null)
				try {
					TR cv = field_terms.get(cleanedvalue);
					if (cv == null) {
						cv = new TR();
						cv.put("value", cleanedvalue);
						String suffix = ""; // (annotation.startsWith("header")?"":("
						// " + annotation));
						a.process(cv, "_text", cleanedvalue + suffix, 5, "label");
						field_terms.put(cleanedvalue, cv);
						List<String> f = new ArrayList<String>();
						f.add(fileName);
						cv.put("file", f);
					} else {
						List<String> f = (List<String>) cv.get("file");
						if (f.indexOf(fileName) < 0)
							f.add(fileName);
					}
					tr.put("cleanedvalue", cleanedvalue);
				} catch (Exception x) {
				}

			if (Sheet != null)
				try {
					String q = Sheet + " assay";
					TR cv = assay_terms.get(Sheet);
					if (cv == null) {
						cv = new TR();
						cv.put("value", Sheet);
						a.process(cv, "_text", q, 5, "label");
						assay_terms.put(Sheet, cv);
						List<String> f = new ArrayList<String>();
						f.add(fileName);
						cv.put("file", f);
					} else {
						List<String> f = (List<String>) cv.get("file");
						if (f.indexOf(fileName) < 0)
							f.add(fileName);
					}
					tr.put("Sheet", Sheet);
				} catch (Exception x) {
				}

			if (endpoint != null)
				try {
					String q = endpoint;
					TR cv = endpoint_terms.get(endpoint);
					if (cv == null) {
						cv = new TR();
						cv.put("value", endpoint);

						String[] v = fileName.split("_");
						cv.put("abbr", v[1].replace(".xlsx", "").toUpperCase());
						a.process(cv, "_text", q, 5, "label");
						endpoint_terms.put(endpoint.toLowerCase(), cv);
						List<String> f = new ArrayList<String>();
						f.add(fileName);
						cv.put("file", f);
					} else {
						List<String> f = (List<String>) cv.get("file");
						if (f.indexOf(fileName) < 0)
							f.add(fileName);
					}
					tr.put("endpoint", endpoint.toLowerCase());

				} catch (Exception x) {
				}
			
			if (!tr.isData()) {
				writer.write(tr.toJSON());
				d = ",";
			}
		}
		writer.write("\n]");
		workbook.close();
		writer.close();

		try (BufferedWriter w = new BufferedWriter(new FileWriter(new File("assay_terms.json")))) {
			AbstractAnnotationProcessor.writeTerms(assay_terms, w);
		} catch (Exception x) {
		}
		try (BufferedWriter w = new BufferedWriter(new FileWriter(new File("endpoint_terms.json")))) {
			AbstractAnnotationProcessor.writeTerms(endpoint_terms, w);
		} catch (Exception x) {
		}
		try (BufferedWriter w = new BufferedWriter(new FileWriter(new File("field_terms.json")))) {
			AbstractAnnotationProcessor.writeTerms(field_terms, w);
		} catch (Exception x) {
		}

		try (BufferedWriter w = new BufferedWriter(new FileWriter(new File("assay_terms.txt")))) {
			AbstractAnnotationProcessor.writeTermsTSV(assay_terms, w);
		} catch (Exception x) {
			x.printStackTrace();
		}

		try (BufferedWriter w = new BufferedWriter(new FileWriter(new File("endpoint_terms.txt")))) {
			AbstractAnnotationProcessor.writeTermsTSV(endpoint_terms, w);
		} catch (Exception x) {
			x.printStackTrace();
		}
		try (BufferedWriter w = new BufferedWriter(new FileWriter(new File("field_terms.txt")))) {
			AbstractAnnotationProcessor.writeTermsTSV(field_terms, w);
		} catch (Exception x) {
			x.printStackTrace();
		}

	}

}
