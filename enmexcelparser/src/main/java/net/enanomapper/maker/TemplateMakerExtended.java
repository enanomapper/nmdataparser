package net.enanomapper.maker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TemplateMakerExtended extends TemplateMaker {
	final String fontfamily = "Arial";

	protected Sheet prepareWorkbook(Workbook workbook) {

		Sheet sheet = workbook.createSheet("Test conditions");
		((XSSFSheet) sheet).setTabColor(new XSSFColor(java.awt.Color.ORANGE, null));
		((XSSFSheet) workbook.createSheet("Raw data")).setTabColor(new XSSFColor(java.awt.Color.GREEN, null));
		((XSSFSheet) workbook.createSheet("Test results")).setTabColor(new XSSFColor(java.awt.Color.MAGENTA, null));
		((XSSFSheet) workbook.createSheet("Test summary")).setTabColor(new XSSFColor(java.awt.Color.CYAN, null));

		workbook.setActiveSheet(0);

		Font font14 = workbook.createFont();
		font14.setFontName(fontfamily);
		font14.setColor(IndexedColors.RED.getIndex());
		font14.setBold(true);
		font14.setFontHeightInPoints((short) 14);

		Font font12 = workbook.createFont();
		font12.setColor(IndexedColors.DARK_BLUE.getIndex());
		font12.setFontName(fontfamily);
		font12.setBold(true);
		font12.setFontHeightInPoints((short) 12);

		Font font = workbook.createFont();
		font.setColor(IndexedColors.DARK_BLUE.getIndex());
		font.setFontName(fontfamily);
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);

		for (int i = 0; i < 3; i++) {
			Row row = sheet.createRow(i);
			for (int c = 0; c < 2; c++) {
				Cell cell = row.createCell(c);
				CellStyle cstyle = workbook.createCellStyle();
				cstyle.setFillForegroundColor(
						c == 0 ? IndexedColors.YELLOW.getIndex() : IndexedColors.WHITE.getIndex());
				cstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

				if (i == 0 && c == 0)
					cstyle.setFont(font14);
				else if (i == 0 && c == 1)
					cstyle.setFont(font12);
				else if (i == 1 && c == 0)
					cstyle.setFont(font12);
				else
					cstyle.setFont(font);
				cell.setCellStyle(cstyle);
			}
		}

		return sheet;
	}

	@Override
	public Workbook generateMultisheetTemplates(Workbook workbook, String templateid, Iterable<TR> records)
			throws Exception {
		if (workbook == null) {
			workbook = new XSSFWorkbook();
		}		
		Sheet sheet = prepareWorkbook(workbook);
		CellStyle kstyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName(fontfamily);
		font.setFontHeightInPoints((short) 10);
		font.setBold(true);
		kstyle.setFont(font);
		kstyle.setLocked(true);

		CellStyle hintstyle = workbook.createCellStyle();
		font = workbook.createFont();
		font.setFontName(fontfamily);
		font.setFontHeightInPoints((short) 10);
		font.setBold(true);
		font.setItalic(true);
		hintstyle.setAlignment(HorizontalAlignment.RIGHT);
		hintstyle.setFont(font);
		hintstyle.setLocked(true);
		int rowindex = hilightrow(workbook, sheet, 4, IndexedColors.SKY_BLUE.getIndex(), "", kstyle);

		
		sheet.getRow(0).getCell(0).setCellValue("TEST CONDITIONS");
		sheet.getRow(0).getCell(1)
				.setCellValue("Please complete the details below as far as possible for each set of assay results");
		sheet.getRow(1).getCell(1).setCellValue("Fields as defined in JRC templates - for discussion!");
		// fields
		String sheetname = null;
		Map<String, List<TR>> items = new HashMap<String, List<TR>>();
		for (TR record : records) {
			if (skip(record, templateid))
				continue;

			if (sheetname == null) {
				sheetname =  getSheetName(record,templateid);
				sheet.getRow(1).getCell(0).setCellValue(sheetname + " template");
			}
			String annotation = TR.hix.Annotation.get(record).toString();
			
			List<TR> item = items.get(annotation);
			if (item == null) {
				item = new ArrayList<TR>();
				items.put(annotation, item);
			}
			item.add(record);
			Object level2 = TR.hix.JSON_LEVEL2.get(record);
			
			if ("results".equals(annotation) && "ENDPOINT".equals(level2)) {
				item = items.get(header_result_endpoint);
				if (item == null) {
					item = new ArrayList<TR>();
					items.put(header_result_endpoint, item);
				}
				item.add(record);
			}

		}

		XSSFCellStyle vstyle = (XSSFCellStyle) workbook.createCellStyle();
		font = workbook.createFont();
		font.setFontName(fontfamily);
		font.setFontHeightInPoints((short) 10);
		font.setBold(false);
		// XSSFColor exc = new XSSFColor(new Color(242,220,219)); this just
		// doesn't work, black color appears!
		vstyle.setFillForegroundColor(IndexedColors.TAN.getIndex());
		vstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		vstyle.setFont(font);

		rowindex = writeSection(workbook, sheet, header_endpoint, rowindex, items.get(header_endpoint), kstyle, vstyle,
				hintstyle);
		rowindex = writeSection(workbook, sheet, header_sop, rowindex - 2, items.get(header_sop), kstyle, vstyle,
				hintstyle, false);
		rowindex = writeSection(workbook, sheet, header_result_endpoint, rowindex, items.get(header_result_endpoint),
				kstyle, vstyle, hintstyle);
		rowindex = writeSection(workbook, sheet, header_sample, rowindex, items.get(header_sample), kstyle, vstyle,
				hintstyle);
		rowindex = writeSection(workbook, sheet, header_size, rowindex, items.get(header_size), kstyle, vstyle,
				hintstyle);
		rowindex = writeSection(workbook, sheet, header_method, rowindex, items.get(header_method), kstyle, vstyle,
				hintstyle);
		rowindex = writeSection(workbook, sheet, header_initialexposure, rowindex, items.get(header_initialexposure),
				kstyle, vstyle, hintstyle);
		rowindex = writeSection(workbook, sheet, header_finalexposure, rowindex, items.get(header_initialexposure),
				kstyle, vstyle, hintstyle);
		// This is a test, not linked to fields [TODO]!
		rowindex = writeRepeatSection(workbook, sheet, "Timeline", rowindex, null, "T", 8, kstyle, vstyle, hintstyle);
		rowindex = writeRepeatSection(workbook, sheet, "TREATMENT CONCENTRATION", rowindex, null, "C", 6, kstyle,
				vstyle, hintstyle);
		rowindex = hilightrow(workbook, sheet, rowindex, IndexedColors.GREY_25_PERCENT.getIndex(), "", kstyle);
		/**
		 * size
		 * 
		 */
		Iterator<Row> rowIterator = sheet.rowIterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			Cell cell = row.getCell(0);
			if (cell != null)
				sheet.autoSizeColumn(cell.getColumnIndex());
		}
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnHidden(26, true);
		sheet.setColumnHidden(27, true);
		return workbook;

	}

	protected int hilightrow(Workbook workbook, Sheet sheet, int row, short color, String value, CellStyle kstyle) {
		Row xrow = sheet.createRow(row);
		CellStyle cstyle = workbook.createCellStyle();
		cstyle.cloneStyleFrom(kstyle);
		cstyle.setFillForegroundColor(color);
		cstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		for (int c = 0; c <= 20; c++) {
			Cell cell = xrow.createCell(c);
			if (c == 0)
				cell.setCellValue(value.toUpperCase());
			cell.setCellStyle(cstyle);
		}
		return row + 1;
	}

	protected int writeSection(Workbook workbook, Sheet sheet, String annotation, int startrow, List<TR> items,
			CellStyle kstyle, CellStyle vstyle, CellStyle hintstyle) {
		return writeSection(workbook, sheet, annotation, startrow, items, kstyle, vstyle, hintstyle, true);
	}

	protected int writeSection(Workbook workbook, Sheet sheet, String annotation, int startrow, List<TR> items,
			CellStyle kstyle, CellStyle vstyle, CellStyle hintstyle, boolean headerline) {
		if (items == null)
			return startrow;
		if (headerline) {
			startrow = hilightrow(workbook, sheet, startrow, IndexedColors.GREY_25_PERCENT.getIndex(), annotation,
					kstyle);
		}

		for (TR record : items) {
			String value = record.get("cleanedvalue").toString();
			if ("".equals(value))
				continue;
			startrow++;
			Row row = sheet.createRow(startrow);
			Cell cell = row.createCell(0);
			String vu = value.toUpperCase();
			/*
			 * if ((vu.indexOf("NM ") < 0) && (vu.indexOf("NPS ") != 0) &&
			 * (vu.indexOf("IC50") < 0))
			 * cell.setCellValue(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat. UPPER_CAMEL,
			 * value)); else
			 */
			cell.setCellValue(value);
			cell.setCellStyle(kstyle);
			CellUtil.setAlignment(cell, HorizontalAlignment.RIGHT);
			cell = row.createCell(1);
			cell.setCellStyle(vstyle);

			/*
			 * if ("assay name".equals(value)) { cell.setCellValue(assayName);
			 * cell.setAsActiveCell(); }
			 */
			CellUtil.setAlignment(cell, HorizontalAlignment.LEFT);
			Object unit = record.get("unit");
			if (unit != null) {
				cell = row.createCell(2);
				cell.setCellValue(unit.toString());
			}

			Object json1 = record.get("JSON_LEVEL1");
			if (json1 != null) {
				cell = row.createCell(26);
				cell.setCellValue(json1.toString());
				if (hintstyle != null)
					cell.setCellStyle(hintstyle);
			}
			Object json2 = record.get("JSON_LEVEL2");
			if (json2 != null) {
				cell = row.createCell(27);
				cell.setCellValue(json2.toString());
				if (hintstyle != null)
					cell.setCellStyle(hintstyle);
			}

			Object hint = record.get("hint");
			if (hint != null && !"".equals(hint.toString().trim())) {
				startrow++;
				row = sheet.createRow(startrow);
				cell = row.createCell(0);
				CellUtil.setAlignment(cell, HorizontalAlignment.RIGHT);
				cell.setCellValue(hint.toString());
				if (hintstyle != null)
					cell.setCellStyle(hintstyle);
			}

		}
		return startrow + 2;
	}

	protected int writeRepeatSection(Workbook workbook, Sheet sheet, String annotation, int startrow, CSVRecord record,
			String prefix, int repeat, CellStyle kstyle, CellStyle vstyle, CellStyle hintstyle) {

		startrow = hilightrow(workbook, sheet, startrow, IndexedColors.GREY_25_PERCENT.getIndex(), annotation, kstyle);
		// String value = record.get("cleanedvalue");
		String value = "[TBD]";

		startrow++;
		Row row1 = sheet.createRow(startrow);
		Cell cell = row1.createCell(0);
		cell.setCellValue(value);
		cell.setCellStyle(kstyle);
		CellUtil.setAlignment(cell, HorizontalAlignment.RIGHT);

		startrow++;
		Row row2 = sheet.createRow(startrow);
		cell = row2.createCell(0);
		cell.setCellValue("[unit]");
		cell.setCellStyle(hintstyle);
		CellUtil.setAlignment(cell, HorizontalAlignment.RIGHT);

		for (int i = 0; i < repeat; i++) {
			CellUtil.setAlignment(cell, HorizontalAlignment.RIGHT);
			cell = row1.createCell(i + 1);
			cell.setCellValue(String.format("%s%d", prefix, i + 1));
			cell.setCellStyle(kstyle);

			cell = row2.createCell(i + 1);
			cell.setCellStyle(vstyle);
		}

		return startrow + 2;
	}

}
