package net.idea.templates.extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ambit2.core.io.FileState;
import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;
import net.idea.templates.annotation.AbstractAnnotationProcessor;

public abstract class AssayTemplatesParser {

	private static final String _root = "root";

	public static String getRoot() {
		return _root;
	}

	private static final String _singledb = "singledb";
	private static final String _release = "release";

	protected static Logger logger = Logger.getLogger(AssayTemplatesParser.class.getName());

	public int parseResources(ResourceBundle nanodataResources, String prefix) throws Exception {
		return parseResources(nanodataResources, prefix, false, null);
	}

	public String getRootValue(ResourceBundle nanodataResources) {
		return nanodataResources.getString(_root);
	}

	public int parseResources(ResourceBundle nanodataResources, String prefix, boolean dryRun, IProcessPair processpair)
			throws Exception {
		final String root = getRootValue(nanodataResources);
		boolean singledb = Boolean.parseBoolean(nanodataResources.getString(_singledb));
		String release = nanodataResources.getString(_release);
		Enumeration<String> enumeration = nanodataResources.getKeys();

		int count = 0;
		int substance = 0;
		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();

			if (_root.equals(key))
				continue;
			if (_singledb.equals(key))
				continue;
			if (_release.equals(key))
				continue;
			// hack to have one file with multiple json configs - the keys
			// should be unique, but files the same
			int ix = key.indexOf("#");
			File file = new File(root, (ix > 0) ? key.substring(0, ix) : key);
			if (file.exists()) {
				logger.log(Level.INFO, String.format("Directory:%s\t%s%s\t%s", file.isDirectory(), root, key,
						nanodataResources.getString(key)));
				if (file.isDirectory()) {
					if (new File(file, "donotimport.properties").exists())
						continue;
					File json = new File(file, nanodataResources.getString(key));
					if (!json.exists()) {
						logger.log(Level.WARNING, String.format("%s not found", json.getAbsolutePath()));
						json = null;
					}
					for (File data : file.listFiles())
						if (acceptDataFile(data))
							if (dryRun && processpair != null)
								processpair.process(prefix, root, data,
										json == null ? getJsonDataName(data, nanodataResources.getString(key)) : json);
							else
								try {
									substance += processFile(data, json == null
											? getJsonDataName(data, nanodataResources.getString(key)) : json, prefix,
											!singledb, release);
								} catch (Exception x) {
									logger.log(Level.WARNING, x.getMessage(), x);
								}
				} else {
					if (acceptDataFile(file)) {
						File json = getJsonDataName(file.getParentFile(), nanodataResources.getString(key));
						if (json == null)
							continue;
						if (!json.exists()) {
							logger.log(Level.WARNING, String.format("%s not found", json.getAbsolutePath()));
						} else {
							if (dryRun)
								processpair.process(prefix, root, file, json);
							else
								substance += processFile(file, json, prefix, !singledb, release);
						}
					}
				}
				count++;
			} else
				logger.log(Level.WARNING, String.format("%s\t%s", file.getAbsolutePath(), "not found"));
		}
		if (!dryRun)
			return verifyFiles(count);
		else
			return count;
	}

	protected File getJsonDataName(File file, String jsonname) {
		File json = new File(file, jsonname);
		if (!json.exists()) {
			json = changeFileExtension(file, ".json");
		}
		return json;
	}

	protected boolean acceptDataFile(File data) {
		return (FileState._FILE_TYPE.XLS_INDEX.hasExtension(data)
				|| FileState._FILE_TYPE.XLSX_INDEX.hasExtension(data));
	}

	protected File changeFileExtension(File data, String newextension) {
		if (FileState._FILE_TYPE.XLS_INDEX.hasExtension(data))
			return new File(data.getAbsolutePath().replaceAll(".xls", newextension));
		else if (FileState._FILE_TYPE.XLS_INDEX.hasExtension(data))
			return new File(data.getAbsolutePath().replaceAll(".xlsx", newextension));
		return null;
	}

	protected abstract int processFile(File spreadsheet, File json, String prefix, boolean resetdb, String release)
			throws Exception;

	protected int verifyFiles(int count) throws Exception {
		return count;
	}

	public static void xls2json(File file, File templatejson, IAnnotator a) throws Exception {

		try (Workbook workbook = new XSSFWorkbook(file)) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(templatejson))) {
				xls2json(workbook,writer, a);
			}
		}
	}

	public static void xls2json(File file, File templatejson) throws Exception {
		xls2json(file, templatejson, null);
	}

	public static void xls2json(Workbook workbook, Writer writer, IAnnotator a) throws Exception {

		Sheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.rowIterator();

		Row row0 = sheet.getRow(0);

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
						if (a != null)
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
						if (a != null)
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
						if (a != null)
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