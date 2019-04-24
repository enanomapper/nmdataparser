package net.idea.templates.generation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import ambit2.base.io.DownloadTool;
import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;

public class Tools {

	public static void readIOMtemplates(File file, Object key, String templateName, Map<String, Term> histogram,
			XSSFSheet stats) throws InvalidFormatException, IOException {
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
			/*
			 * stats.write(String.format("%s,\"%s\",%s,%d,%d,%d,%s,%s\n",
			 * key.toString(), templateName, sheet.getSheetName(),
			 * row.getRowNum(), cell1 == null ? -1 : cell1.getColumnIndex(),
			 * cell2 == null ? -1 : cell2.getColumnIndex(), value1, value2));
			 */
		}
		workbook.close();
	}

	protected static String getValue(Cell cell) {
		return getValue(cell, true);
	}

	protected static String getValue(Cell cell, boolean clean) {
		String value = null;
		switch (cell.getCellType()) {
		case STRING: {
			value = new String(cell.getStringCellValue().getBytes(Charset.forName("UTF-8")));
			// System.out.println(value);
			value = value.toLowerCase();
			if (clean)
				value = value.replace("\n", " ").replace("\r", "").trim();
			break;
		}
		case FORMULA: {
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
	public static int readJRCExcelTemplate(File file, Object key, String templateName, Map<String, Term> histogram,
			XSSFSheet stats, IAnnotator annotator, int rownum) throws InvalidFormatException, IOException {
		return readJRCExcelTemplate( file,  key,  templateName, histogram,
				 stats,  annotator,  rownum, null);
	}
	public static int readJRCExcelTemplate(File file, Object key, String templateName, Map<String, Term> histogram,
			XSSFSheet stats, IAnnotator annotator, int rownum, Integer nsh) throws InvalidFormatException, IOException {

		Workbook workbook;
		if (templateName.endsWith(".xlsx")) {
			workbook = new XSSFWorkbook(file);
		} else if (templateName.endsWith(".xls")) {
			workbook = new HSSFWorkbook(new FileInputStream(file));
		} else
			throw new InvalidFormatException(file.getName());

		int _startsheet=0;
		int _endsheet=workbook.getNumberOfSheets();
		if (nsh!=null) {
			_startsheet = nsh;
			_endsheet = nsh+1;
		}
		
		TR record = new TR();

		for (int i = _startsheet; i < _endsheet; i++) {
			Sheet sheet = workbook.getSheetAt(i);
			if ("instruction for data logging".equals(sheet.getSheetName().toLowerCase()))
				continue;
			int rows = 0;
			int maxcols = 0;
			HashFunction hf = Hashing.murmur3_32();

			Iterator<Row> rowIterator = sheet.rowIterator();
			boolean _break=false;
			while (rowIterator.hasNext() && !_break) {
				Row row = rowIterator.next();
				Iterator<Cell> cellIterator = row.cellIterator();
				int columns = 0;
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					String value = getValue(cell, false);
					String values[] = value == null ? null : value.split("\n");
					value = value == null ? null : value.replace("\n", " ").replace("\r", "").trim();

					try {
						if (value != null) {
							gatherStats(value, histogram);

							
							if (!"".equals(value.trim())) {
								rownum++;
								record.clear();
								try {
									TR.hix.cleanedvalue.set(record, values[0]);
								} catch (Exception x) {
								}
								try {
									TR.hix.hint.set(record, values[1]);
								} catch (Exception x) {
								}

								
								TR.hix.Folder.set(record, key.toString());
								TR.hix.File.set(record, templateName);
								TR.hix.Sheet.set(record, sheet.getSheetName());
								TR.hix.Row.set(record, row.getRowNum());
								TR.hix.Column.set(record, cell.getColumnIndex());
								TR.hix.Value.set(record, value);
							
								HashCode hc = hf.newHasher().putString(String.format("%s-%s-%s",key.toString().toLowerCase(),templateName.toLowerCase(),sheet.getSheetName().toLowerCase()), Charsets.UTF_8).hash();
								TR.hix.ID.set(record, hc);
								
								if (annotator != null)
									annotator.process(record);
								
								if (!"data".equals(TR.hix.Annotation.get(record)))
										record.write(stats, rownum);
								else {
									rownum--;
									_break = true;
									break;
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
		return rownum;
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
