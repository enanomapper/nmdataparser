package net.idea.templates.annotation;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;

public class AnnotationProcessor extends AbstractAnnotationProcessor<Workbook, HashMap<String, TR>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1596097357620029473L;

	protected HashMap<String, TR> terms;
	protected String source = "";
	protected int[] column_indices;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public HashMap<String, TR> getTerms() {
		return terms;
	}

	public void setTerms(HashMap<String, TR> terms) {
		this.terms = terms;
	}

	public AnnotationProcessor() {
		super();
	}

	public AnnotationProcessor(String source, IAnnotator annotator, int[] column_indices) {
		super();
		this.annotator = annotator;
		this.source = source;
		this.column_indices = column_indices;
	}

	public static StringBuilder hack_clean(String value) {
		String[] vv = value.split(" ");
		StringBuilder b = new StringBuilder();
		//get rid of numeric parts!
		for (String v : vv) {
			if ("".equals(v))
				continue;
			try {
				Double.parseDouble(v.substring(0, 1));
				// don't attempt to annotate numbers, or
				// anything starting with a digit
				continue;
			} catch (Exception x) {
			}
			try {
				Date.parse(v);
				continue;
			} catch (Exception x) {

			}
			b.append(v);
			b.append(" ");
		}
		return b;
	}
	@Override
	public HashMap<String, TR> process(Workbook workbook) throws Exception {
		HashMap<String, TR> field_terms = terms == null ? new HashMap<String, TR>() : terms;
		Iterator<Sheet> sheets = workbook.sheetIterator();
		while (sheets.hasNext()) {
			Sheet sheet = sheets.next();
			Iterator<Row> rowIterator = sheet.rowIterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				// if (row.getRowNum() == 0) continue;
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();

					if (column_indices == null || column_indices.length == 0) {
						// all
					} else if (Arrays.binarySearch(column_indices, cell.getColumnIndex()) < 0)
						continue;

					switch (cell.getCellType()) {

					case STRING: {
						try {
							String value = new String(
									cell.getStringCellValue().getBytes(Charset.forName("UTF-8"))).replaceAll("\n", " ")
											.replaceAll("\r", " ").trim();

							StringBuilder b = hack_clean(hack_jrcnm(value));

							String cleanedvalue = b.toString().trim();
							if ("".equals(cleanedvalue)) continue;
							TR cv = field_terms.get(cleanedvalue);
							if (cv == null) {
								cv = new TR();
								cv.put("value", cleanedvalue);
								String suffix = "";
								annotator.process(cv, "_text", cleanedvalue + suffix, 5, "label");
								field_terms.put(cleanedvalue, cv);

								List<String> f = new ArrayList<String>();
								f.add(source);
								cv.put("file", f);

								f = new ArrayList<String>();
								f.add(sheet.getSheetName());
								cv.put("sheet", f);
							} else {
								// already annotated
								List<String> f = (List<String>) cv.get("sheet");
								if (f.indexOf(sheet.getSheetName()) < 0)
									f.add(sheet.getSheetName());

								f = (List<String>) cv.get("file");
								if (f.indexOf(source) < 0)
									f.add(source);
							}

						} catch (Exception x) {
						}
						break;
					}
					default: {
						// ignore other types for now
					}
					}
				}
			}
		}

		return field_terms;
	}

	/**
	 * http://bioportal.bioontology.org/ontologies/ENM/?p=classes&conceptid=http%3A%2F%2Fpurl.enanomapper.org%2Fonto%2FENM_9000074
	 * 
	 * @param v
	 * @return
	 */
	public static String hack_jrcnm(String v) {
		if (v.startsWith("NM1"))
			return v.replaceAll("NM1", "NM-1");
		else if (v.startsWith("NM2"))
			return v.replaceAll("NM2", "NM-2");
		else if (v.startsWith("NM3"))
			return v.replaceAll("NM3", "NM-3");
		else if (v.startsWith("NM4"))
			return v.replaceAll("NM4", "NM-4");
		else if (v.startsWith("NM"))
			return v.replaceAll(" ", "-");		
		else
			return v;
	}
}
