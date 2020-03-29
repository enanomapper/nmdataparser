package net.enanomapper.maker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFRow.CellIterator;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFTableStyleInfo;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TemplateMakerExtended extends TemplateMaker {
	final String fontfamily = "Arial";

	protected Sheet prepareSheet(Workbook workbook, String title, XSSFColor color) {

		Sheet sheet = workbook.createSheet(title);
		((XSSFSheet) sheet).setTabColor(color);
		workbook.setActiveSheet(workbook.getSheetIndex(title));
		return sheet;
	}

	protected int get_number_x_axis(TemplateMakerSettings settings) {
		return settings.getNumber_of_experiments();
	}

	protected int get_number_y_axis(TemplateMakerSettings settings) {
		return settings.getNumber_of_replicates();
	}

	protected int prepareSheet_RawData_header(Workbook workbook, Sheet sheet, TemplateMakerSettings settings,
			CellStyle kstyle, int startrow, int startcol, int y_number, int t_number) {
		int row = startrow;
		Row row1 = sheet.getRow(row);
		if (row1 == null)
			row1 = sheet.createRow(row);
		Cell cellm = row1.createCell(0 + startcol);
		cellm.setCellValue("Material");
		cellm.setCellStyle(kstyle);
		Cell cellr = row1.createCell(1 + startcol);
		cellr.setCellValue(String.format("%s %d", settings.getLayout_raw_data().get_label_y_axis(), y_number + 1));
		// cellr.setCellValue( settings.getLayout_raw_data().get_label_y_axis());
		cellr.setCellStyle(kstyle);
		Cell cellt = row1.createCell(2 + startcol);
		cellt.setCellValue(String.format("T%d", t_number + 1));
		// cellt.setCellValue("Time");
		cellt.setCellStyle(kstyle);
		Cell cellc = row1.createCell(3 + startcol);
		cellc.setCellValue("Concentration");
		cellc.setCellStyle(kstyle);

		for (int i = 0; i < settings.get_number_x_axis(); i++) {
			Cell cell = row1.createCell(4 + i + startcol);
			cell.setCellValue(String.format("%s %d", settings.getLayout_raw_data().get_label_x_axis(), i + 1));
			cell.setCellStyle(kstyle);
		}
		Cell cella = row1.createCell(3 + settings.get_number_x_axis() + 1 + startcol);
		cella.setCellValue("Average");
		cella.setCellStyle(kstyle);
		row++;
		return row;
	}

	protected Sheet prepareSheet_RawData(Workbook workbook, TemplateMakerSettings settings) {
		Sheet sheet = prepareSheet(workbook, "Raw data", new XSSFColor(java.awt.Color.GREEN, null));
		CellStyle kstyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName(fontfamily);
		font.setFontHeightInPoints((short) 10);
		font.setBold(true);
		kstyle.setFont(font);
		kstyle.setLocked(true);

		int startcol = 1;

		for (int e = 0; e < settings.getNumber_of_endpoints(); e++) {
			int startrow = 3;
			int row = startrow;

			Row row_ep = sheet.getRow(row - 1);
			if (row_ep == null)
				row_ep = sheet.createRow(row - 1);
			Cell cell_ep = row_ep.createCell(1 + startcol);
			cell_ep.setCellValue(String.format("EP %d", e + 1));
			cell_ep.setCellStyle(kstyle);

			for (int r = 0; r < settings.get_number_y_axis(); r++) {

				for (int t = 0; t < settings.getNumber_of_timepoints(); t++) {
					startrow = row;
					row = prepareSheet_RawData_header(workbook, sheet, settings, kstyle, startrow, startcol, r, t);
					for (int c = 0; c < settings.getNumber_of_concentration(); c++) {
						Row row1 = sheet.getRow(row);
						if (row1 == null)
							row1 = sheet.createRow(row);

						Cell cellm = row1.createCell(0 + startcol);
						Cell cellr = row1.createCell(1 + startcol);
						// cellr.setCellValue(String.format("%s %d",
						// settings.getLayout_raw_data().get_label_y_axis(),r + 1));
						Cell cellt = row1.createCell(2 + startcol);
						// cellt.setCellValue(String.format("T%d", t + 1));
						Cell cellc = row1.createCell(3 + startcol);
						cellc.setCellValue(String.format("C%d", c + 1));

						CellAddress c1 = null;
						CellAddress c2 = null;
						for (int i = 0; i < settings.get_number_x_axis(); i++) {
							Cell cell = row1.createCell(4 + i + startcol);
							cell.setCellValue(0);
							if (i == 0)
								c1 = cell.getAddress();
							else
								c2 = cell.getAddress();

						}

						Cell cell = row1.createCell(4 + settings.get_number_x_axis() + startcol);
						cell.setCellType(CellType.FORMULA);
						cell.setCellFormula(String.format("AVERAGE(%s:%s)", c1.formatAsString(), c2.formatAsString()));

						row++;
					}

					try {
						AreaReference reference = workbook.getCreationHelper().createAreaReference(
								new CellReference(startrow, startcol),
								new CellReference(row - 1, 3 + settings.get_number_x_axis() + 1 + startcol));
						// System.out.println(String.format("%d %d %d %d", startrow, startcol, row - 1,
						// 3 + settings.get_number_x_axis() + 1 + startcol));
						// Create
						XSSFTable table = ((XSSFSheet) sheet).createTable(reference);

						for (int i = 0; i < table.getCTTable().getTableColumns().getCount(); i++) {
							table.getCTTable().getTableColumns().getTableColumnArray(i).setId(i + 1);
						}

						table.setName(String.format("%s_%d_T%d_E%d", settings.getLayout_raw_data().get_label_y_axis(),
								r + 1, t + 1, e + 1));
						table.setDisplayName(String.format("Table_%s_%d_T%d_E%d",
								settings.getLayout_raw_data().get_label_y_axis(), r + 1, t + 1, e + 1));
						// For now, create the initial style in a low-level way
						table.getCTTable().addNewTableStyleInfo();
						table.getCTTable().getTableStyleInfo().setName("TableStyleMedium2");

						XSSFTableStyleInfo style = (XSSFTableStyleInfo) table.getStyle();
						style.setName("TableStyleMedium2");
						style.setShowColumnStripes(true);
						// style.setShowRowStripes(true);
						// style.setFirstColumn(false);
						// style.setLastColumn(false);
						//

					} catch (Exception x) {
						x.printStackTrace();
					}
					row++;
				}

				row += settings.getLayout_raw_data().get_y_space();
			}

			startcol = 3 + settings.get_number_x_axis() + 1 + startcol + settings.getLayout_raw_data().get_x_space();
		}

		return sheet;

	}

	protected Sheet prepareSheet_TestConditions(Workbook workbook) {

		Sheet sheet = prepareSheet(workbook, "Test conditions", new XSSFColor(java.awt.Color.ORANGE, null));

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
	public Workbook generateMultisheetTemplates(Workbook workbook, TemplateMakerSettings settings, Iterable<TR> records)
			throws Exception {
		String templateid = settings.getQueryTemplateId();
		if (workbook == null) {
			workbook = new XSSFWorkbook();
		}
		Sheet sheet = prepareSheet_TestConditions(workbook);
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
				sheetname = getSheetName(record, templateid);
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
		rowindex = writeRepeatSection(workbook, sheet, "Endpoints", rowindex, null, "EP",
				settings.getNumber_of_endpoints(), kstyle, vstyle, hintstyle);

		rowindex = writeRepeatSection(workbook, sheet, "Timeline", rowindex, null, "T",
				settings.getNumber_of_timepoints(), kstyle, vstyle, hintstyle);
		rowindex = writeRepeatSection(workbook, sheet, "TREATMENT CONCENTRATION", rowindex, null, "C",
				settings.getNumber_of_concentration(), kstyle, vstyle, hintstyle);
		rowindex = hilightrow(workbook, sheet, rowindex, IndexedColors.GREY_25_PERCENT.getIndex(), "", kstyle);
		/**
		 * size
		 * 
		 */

		autosize(sheet, 0);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnHidden(26, true);
		sheet.setColumnHidden(27, true);

		sheet = prepareSheet_RawData(workbook, settings);
		autosize(sheet, -1);
		sheet = prepareSheet(workbook, "Test result", new XSSFColor(java.awt.Color.MAGENTA, null));
		sheet = prepareSheet(workbook, "Test summary", new XSSFColor(java.awt.Color.CYAN, null));
		workbook.setActiveSheet(0);
		return workbook;

	}

	protected void autosize(Sheet sheet, int col) {
		Iterator<Row> rowIterator = sheet.rowIterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (col >= 0) {
				Cell cell = row.getCell(col);
				if (cell != null)
					sheet.autoSizeColumn(cell.getColumnIndex());
			} else {
				Iterator<Cell> colIterator = row.cellIterator();
				while (colIterator.hasNext()) {
					Cell cell = colIterator.next();
					if (cell != null)
						sheet.autoSizeColumn(cell.getColumnIndex());
				}
			}
		}
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
