package net.enanomapper.maker;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.enanomapper.maker.TemplateMakerSettings._TEMPLATES_TYPE;
import net.enanomapper.parser.ParserConstants.ElementField;

public class TemplateMaker {
	protected Logger logger_cli;

	public enum _header {
		module {
			@Override
			public String toString() {
				return "module";
			}
		},
		results, method {
			@Override
			public String toString() {
				return "method and instrument information";
			}
		},
		experimental_parameters {
			@Override
			public String toString() {
				return "experimental parameters";
			}
		},
		sample_preparation {
			@Override
			public String toString() {
				return "sample preparation";
			}
		},
		parameters {
			@Override
			public String toString() {
				return "experimental parameters";
			}
		},
		initialexposure {
			@Override
			public String toString() {
				return "analytical parameters - initial exposure media";
			}
		},
		finalexposure {
			@Override
			public String toString() {
				return "analytical parameters - final exposure media";
			}
		},
		imageanalysis {
			@Override
			public String toString() {
				return "image analysis and results";
			}
		},
		sample {
			@Override
			public String toString() {
				return "sample information";
			}
		},
		size {
			@Override
			public String toString() {
				return "size distribution";
			}
		},
		cell, endpoint {
			@Override
			public String toString() {
				return "endpoint_assay";
			}
		},
		sop, resultendpoint {
			@Override
			public String toString() {
				return "end-point outcome metric";
			}
		},
		exposure_echa_use_descriptors {
			@Override
			public String toString() {
				return "echa use descriptors";
			}

			@Override
			public String getPrefix() {
				return "ECHA.";
			}
		},
		exposure_contributing_scenario {
			@Override
			public String toString() {
				return "contributing exposure scenario/activity";
			}

			@Override
			public String getPrefix() {
				return "EXPOSURE_CONTRIBUTING_SCENARIO.";
			}
		},
		exposure_nm_physchem {
			@Override
			public String toString() {
				return "nf phys-chem characteristics";
			}
		},
		exposure_matrix {
			@Override
			public String toString() {
				return "matrix characteristics";
			}

			@Override
			public String getPrefix() {
				return "MATRIX_CHARACTERISTICS.";
			}
		},
		exposure_control_measures {
			@Override
			public String toString() {
				return "exposure control measures";
			}

			@Override
			public String getPrefix() {
				return "EXPOSURE_CONTROL_MEASURES.";
			}
		},
		exposure_factors {
			@Override
			public String toString() {
				return "exposure factors";
			}

			@Override
			public String getPrefix() {
				return "EXPOSURE_FACTORS.";
			}
		},
		exposure_premises {
			@Override
			public String toString() {
				return "premises";
			}

			@Override
			public String getPrefix() {
				return "PREMISES.";
			}
		},
		exposure_summary_exposure_results {
			@Override
			public String getPrefix() {
				return "SUMMARY_EXPOSURE_RESULTS.";
			}

			@Override
			public String toString() {
				return "summary exposure results";
			}
		},
		exposure_measurements_instruments {
			@Override
			public String toString() {
				return "measurements & instrument characteristics";
			}
		},
		exposure_measurement_results {
			@Override
			public String toString() {
				return "measurement results";
			}
		},
		exposure_reference {
			@Override
			public String toString() {
				return "reference";
			}
		},
		exposure_quality_scenario {
			@Override
			public String toString() {
				return "quality scenario";
			}
		}

		;

		@Override
		public String toString() {
			return this.name().toString();
		}

		public String getPrefix() {
			return null;
		}
	}

	// for compatibility, to be refactored to use enums
	protected final static String header_results = _header.results.toString();
	protected final static String header_method = _header.method.toString();
	protected final static String header_experimentalparameters = _header.experimental_parameters.toString();
	protected final static String header_sample_preparation = _header.sample_preparation.toString();
	protected final static String header_initialexposure = _header.initialexposure.toString();
	protected final static String header_finalexposure = _header.finalexposure.toString();

	protected final static String header_imageanalysis = _header.imageanalysis.toString();
	protected final static String header_sample = _header.sample.toString();
	protected final static String header_size = _header.size.toString();
	protected final static String header_cell = _header.cell.toString();
	protected final static String header_endpoint = _header.endpoint.toString();
	protected final static String header_sop = _header.sop.toString();
	protected final static String header_result_endpoint = _header.resultendpoint.toString();

	public TemplateMaker() {
		this(Logger.getAnonymousLogger());
	}

	public TemplateMaker(Logger logger) {
		this.logger_cli = logger;
	}

	
	public Workbook generateMultisheetTemplates(Workbook workbook, TemplateMakerSettings settings, Iterable<TR> records)
			throws Exception {

		throw new Exception("Unsupported");
	}

	protected void insertLogo(Workbook workbook, Sheet sheet) {
		sheet.createRow(0).createCell(0).setCellValue("NANoREG templates");
		sheet.createRow(1).createCell(0).setCellValue("http://www.nanoreg.eu/media-and-downloads/templates");
		sheet.createRow(2).createCell(0).setCellValue(
				"The templates are licensed under a Creative Commons Attribution-ShareAlike 4.0 International License.");
		sheet.createRow(3).createCell(0).setCellValue("https://creativecommons.org/licenses/by-sa/4.0/");

		sheet.createRow(5).createCell(0).setCellValue(
				"Within eNanoMapper project the templates and fields are cleaned up. This is an eNanoMapper template derived from NANoREG template");
		sheet.createRow(6).createCell(0).setCellValue("http://ambit.sourceforge.net/enanomapper/templates/");
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		sheet.createRow(7).createCell(0).setCellValue(String.format("Generated on %s", dateFormat.format(date)));

		InputStream in = null;
		try {
			in = TemplateMaker.class.getClassLoader().getResourceAsStream("net/enanomapper/templates/logonr.png");
			BufferedImage img = ImageIO.read(in);

			final CreationHelper helper = workbook.getCreationHelper();
			final Drawing drawing = sheet.createDrawingPatriarch();

			final ClientAnchor anchor = helper.createClientAnchor();
			anchor.setAnchorType(AnchorType.MOVE_AND_RESIZE);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "png", baos);
			baos.flush();
			baos.close();

			final int pictureIndex = workbook.addPicture(baos.toByteArray(), Workbook.PICTURE_TYPE_PNG);

			anchor.setCol1(0);
			anchor.setRow1(9); // same row is okay
			anchor.setRow2(9);
			anchor.setCol2(1);
			final Picture pict = drawing.createPicture(anchor, pictureIndex);
			pict.resize();
		} catch (Exception x) {

		} finally {
			try {
				in.close();
			} catch (Exception x) {
			}
		}

	}

	protected boolean skip(TR record, String templateid) {
		Object _id = record.get(TR.hix.id.name());
		if (_id == null || record.get(TR.hix.Sheet.name()) == null)
			return true;
		if (!templateid.equals(_id))
			return true;
		else
			return false;
	}

	protected String getSheetName(TR record, String templateid) {
		String _sheet = record.get(TR.hix.Sheet.name()).toString();
		return String.format("%s_%s", _sheet.toString(), templateid);

	}

	public Workbook generateJRCTemplates(Workbook workbook, String templateid, Iterable<TR> records) throws Exception {

		if (workbook == null) {
			workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("instruction for data logging");
			insertLogo(workbook, sheet);
		}
		CreationHelper factory = workbook.getCreationHelper();
		Map<String, CellStyle> style = new HashMap<String, CellStyle>();
		Map<String, Integer> mincol = new HashMap<String, Integer>();
		Map<String, Integer> maxcol = new HashMap<String, Integer>();
		Sheet sheet = null;

		for (TR record : records)
			try {
				if (skip(record, templateid))
					continue;
				String _sheet = getSheetName(record, templateid);
				if (_sheet.length() > 30)
					_sheet = _sheet.substring(0, 30);
				if (sheet == null) {
					try {
						sheet = workbook.createSheet(_sheet);
					} catch (Exception x) {
						throw x;
					}
					workbook.setActiveSheet(workbook.getNumberOfSheets() - 1);
					Header header = sheet.getHeader();
					header.setCenter("Center Header");
					header.setLeft("Left Header");

				}

				if (sheet.getSheetName().equals(_sheet)) {
					// System.out.println(String.format("%s\t%s\t%s",record.getRow(),record.getColumn(),record.get("unit")));
					int row = Integer.parseInt(record.get(TR.hix.Row.name()).toString());
					int col = Integer.parseInt(record.get(TR.hix.Column.name()).toString());

					Row xrow = sheet.getRow(row);
					if (xrow == null)
						xrow = sheet.createRow(row);
					Cell cell = xrow.getCell(col);
					if (cell == null) {
						cell = xrow.createCell(col);
						CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
					}

					Object v = record.get("cleanedvalue");
					String value = v == null ? "?????" : v.toString();

					// through hint options
					/*
					 * if ("material state".equals(value) || value.indexOf("physical state") >= 0) {
					 * validation_materialstate(workbook, sheet, col); }
					 */

					Object annotation = TR.hix.Annotation.get(record);
					try {
						for (_header h : _header.values())
							if (annotation.toString().equals(h.toString()))
								if (h.getPrefix() != null) {
									value = value.replaceAll(h.getPrefix(), "");
									break;
								}
					} catch (Exception x) {

					}
					Object header1 = TR.hix.header1.get(record);
					Object hint = TR.hix.hint.get(record);

					Object level1 = TR.hix.JSON_LEVEL1.get(record);
					Object level2 = TR.hix.JSON_LEVEL2.get(record);
					Object level3 = TR.hix.JSON_LEVEL3.get(record);

					if (ElementField.ERR_VALUE.name().equals(level2)) {
						value = String.format("Uncertainty (%s)", value);
					}

					logger_cli.log(Level.FINE, String.format("%s\t%s\t%s\t%s", row, col, value, annotation));
					String hint_enum_prefix = "ENUM:";
					if (hint != null && hint.toString().startsWith(hint_enum_prefix)) {
						String[] options = hint.toString().replaceAll(hint_enum_prefix, "").split(",");
						if (options.length > 1)
							validation_common(workbook, sheet, col, options);
						hint = "See dropdown options";
					}

					Object unit = record.get("unit");

					stats(annotation == null ? null : annotation.toString(), col, mincol, true);
					stats(annotation == null ? null : annotation.toString(), col, maxcol, false);
					stats(header1 == null ? null : header1.toString(), col, maxcol, false);

					CellStyle cstyle = style.get(annotation);
					if (cstyle == null) {
						cstyle = workbook.createCellStyle();
						cstyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
						cstyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
						cstyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
						cstyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
						cstyle.setBorderTop(BorderStyle.NONE);
						cstyle.setBorderLeft(BorderStyle.NONE);
						cstyle.setBorderRight(BorderStyle.NONE);
						cstyle.setBorderBottom(BorderStyle.NONE);
						if (header_results.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
						} else if (_header.exposure_measurement_results.toString().equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
						} else if (_header.exposure_summary_exposure_results.toString().equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
						} else if (header_sop.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
						} else if (_header.module.toString().equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
						} else if (_header.exposure_echa_use_descriptors.toString().equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
						} else if (header_method.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else if (_header.exposure_contributing_scenario.toString().equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
						} else if (header_experimentalparameters.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else if (_header.exposure_measurements_instruments.toString().equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else if (header_sample.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.TAN.getIndex());
						} else if (header_sample_preparation.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
						} else if (header_size.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
						} else if (_header.exposure_nm_physchem.toString().equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
						} else if (header_cell.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else if (_header.exposure_control_measures.toString().equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else if (_header.exposure_factors.toString().equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else if (_header.exposure_matrix.toString().equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else if (_header.exposure_premises.toString().equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else if (header_endpoint.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
						} else if (header_imageanalysis.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
						} else if (header_initialexposure.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else if (header_finalexposure.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else
							cstyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
						cstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						if (annotation != null)
							style.put(annotation.toString(), cstyle);

					}

					if (row == 0) {
						value = value.toUpperCase();
					} else {
						// if (value.indexOf("NM ") < 0)
						// value =
						// CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
						// value);
					}

					cell.setCellValue(value);

					if (hint != null && !"".equals(hint)) {

						Drawing drawing = sheet.createDrawingPatriarch();

						// When the comment box is visible, have it show in a
						// 1x3 space
						ClientAnchor anchor = factory.createClientAnchor();
						anchor.setCol1(cell.getColumnIndex());
						anchor.setCol2(cell.getColumnIndex() + 1);
						anchor.setRow1(row);
						anchor.setRow2(row + 5);

						// Create the comment and set the text+author
						if (hint != null)
							try {
								Comment comment = drawing.createCellComment(anchor);
								RichTextString str = factory.createRichTextString(hint.toString());
								comment.setString(str);
								comment.setAuthor(_sheet.toString());

								// Assign the comment to the cell
								cell.setCellComment(comment);
							} catch (Exception x) {
								logger_cli.log(Level.WARNING, x.getMessage());
							}
					}

					if (unit != null && !"".equals(unit)) {
						xrow = sheet.getRow(4);
						if (xrow == null)
							xrow = sheet.createRow(4);
						cell = xrow.getCell(col);
						if (cell == null)
							cell = xrow.createCell(col);

						cell.setCellValue(unit.toString());
					}
					if (!ElementField.ERR_VALUE.name().equals(level2) && !ElementField.UNIT.name().equals(level2))
						if (level3 != null && !"".equals(level3.toString())) {
							xrow = sheet.getRow(3);
							if (xrow == null)
								xrow = sheet.createRow(3);
							cell = xrow.getCell(col);
							if (cell == null)
								cell = xrow.createCell(col);

							cell.setCellValue(level3.toString());
						}
				} else {
					throw new Exception(
							String.format("Expected sheet name' %s' but found '%s'", sheet.getSheetName(), _sheet));
				}

			} catch (Exception x) {
				x.printStackTrace();
			}

		setStyle(workbook, sheet, header_results, mincol, maxcol, style);
		setStyle(workbook, sheet, header_method, mincol, maxcol, style);
		setStyle(workbook, sheet, header_experimentalparameters, mincol, maxcol, style);
		setStyle(workbook, sheet, header_endpoint, mincol, maxcol, style);
		setStyle(workbook, sheet, header_size, mincol, maxcol, style);
		setStyle(workbook, sheet, header_sample, mincol, maxcol, style);
		setStyle(workbook, sheet, header_sample_preparation, mincol, maxcol, style);
		setStyle(workbook, sheet, header_sop, mincol, maxcol, style);
		setStyle(workbook, sheet, header_imageanalysis, mincol, maxcol, style);

		setStyle(workbook, sheet, _header.module.toString(), mincol, maxcol, style);
		setStyle(workbook, sheet, _header.exposure_echa_use_descriptors.toString(), mincol, maxcol, style);
		setStyle(workbook, sheet, _header.exposure_contributing_scenario.toString(), mincol, maxcol, style);
		setStyle(workbook, sheet, _header.exposure_control_measures.toString(), mincol, maxcol, style);
		setStyle(workbook, sheet, _header.exposure_factors.toString(), mincol, maxcol, style);
		setStyle(workbook, sheet, _header.exposure_matrix.toString(), mincol, maxcol, style);
		setStyle(workbook, sheet, _header.exposure_nm_physchem.toString(), mincol, maxcol, style);
		setStyle(workbook, sheet, _header.exposure_premises.toString(), mincol, maxcol, style);
		setStyle(workbook, sheet, _header.exposure_summary_exposure_results.toString(), mincol, maxcol, style);
		setStyle(workbook, sheet, _header.exposure_measurements_instruments.toString(), mincol, maxcol, style);
		setStyle(workbook, sheet, _header.exposure_measurement_results.toString(), mincol, maxcol, style);
		setStyle(workbook, sheet, _header.exposure_reference.toString(), mincol, maxcol, style);
		setStyle(workbook, sheet, _header.exposure_quality_scenario.toString(), mincol, maxcol, style);

		Iterator<Row> rowIterator = sheet.rowIterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				sheet.autoSizeColumn(cell.getColumnIndex());
			}
		}

		validation_endpoint(workbook, sheet, mincol.get(header_endpoint));
		return workbook;

	}

	protected void setStyle(Workbook workbook, Sheet sheet, String annotation, Map<String, Integer> mincol,
			Map<String, Integer> maxcol, Map<String, CellStyle> cellstyle) {

		Integer col1 = mincol == null ? null : mincol.get(annotation);
		Integer col2 = maxcol == null ? null : maxcol.get(annotation);
		if (col1 == null || col2 == null)
			return;

		CellStyle astyle = cellstyle.get(annotation);
		for (int i = 0; i < 20; i++) {
			Row row = sheet.getRow(i);
			if (i == 0)
				row.setHeight((short) 1000);
			if (row == null)
				row = sheet.createRow(i);

			for (int c = col1; c <= col2; c++) {
				CellStyle cstyle = workbook.createCellStyle();
				cstyle.cloneStyleFrom(astyle);

				cstyle.setBorderLeft(BorderStyle.THIN);
				cstyle.setBorderRight(BorderStyle.THIN);
				if (i == 0) {
					cstyle.setBorderTop(BorderStyle.MEDIUM);
					cstyle.setBorderLeft(BorderStyle.NONE);
					cstyle.setBorderRight(BorderStyle.NONE);

				} else if (i == 3)
					cstyle.setBorderTop(BorderStyle.HAIR);
				else
					cstyle.setBorderTop(BorderStyle.NONE);

				if (i == 4 || i == 0)
					cstyle.setBorderBottom(BorderStyle.MEDIUM);
				else
					cstyle.setBorderBottom(BorderStyle.NONE);

				if (c == col1) {
					cstyle.setBorderLeft(BorderStyle.MEDIUM);
				}
				if (c == col2)
					cstyle.setBorderRight(BorderStyle.MEDIUM);

				if (i > 4) {
					cstyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
					cstyle.setBorderBottom(BorderStyle.HAIR);
				}

				Cell cell = row.getCell(c);
				if (cell == null)
					cell = row.createCell(c);

				if (i > 4) {
					CellUtil.setAlignment(cell, HorizontalAlignment.LEFT);
					String unit = sheet.getRow(4).getCell(c).getStringCellValue();
					if (unit != null && !"".equals(unit)) {

						cstyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
						cell.setCellType(CellType.NUMERIC);
						CellUtil.setAlignment(cell, HorizontalAlignment.RIGHT);
					}
				}

				cell.setCellStyle(cstyle);
			}
			if (i == 0)
				try {
					sheet.addMergedRegion(new CellRangeAddress(i, i, col1, col2));
					CellUtil.setAlignment(row.getCell(col1), HorizontalAlignment.CENTER);
				} catch (Exception x) {
					logger_cli.warning(String.format("%s\t%s", sheet.getSheetName(), x.getMessage()));
				}
			if ("size distribution".equals(annotation) && i == 1) {
				sheet.addMergedRegion(new CellRangeAddress(i, i, col1, col2));
				CellUtil.setAlignment(row.getCell(col1), HorizontalAlignment.CENTER);
			} else if ("cell".equals(annotation) && i == 1) {
				sheet.addMergedRegion(new CellRangeAddress(i, i, col1, col2));
				CellUtil.setAlignment(row.getCell(col1), HorizontalAlignment.CENTER);
			}

		}

	}

	protected void validation_common(Workbook workbook, Sheet sheet, Integer col, String[] constraints) {
		XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
		XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
				.createExplicitListConstraint(constraints);
		CellRangeAddressList addressList = new CellRangeAddressList(5, 5, col, col);
		XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		sheet.addValidationData(validation);
	}

	protected void validation_endpoint(Workbook workbook, Sheet sheet, Integer col) {
		if (col == null)
			return;
		validation_common(workbook, sheet, col,
				new String[] { "phys-chem", "in vitro tox", "in vivo tox", "eco tox", "exposure" });
	}

	protected void validation_materialstate(Workbook workbook, Sheet sheet, Integer col) {
		validation_common(workbook, sheet, col, new String[] { "liquid", "fluid", "fluid dispersion", "powder",
				"liquid suspension", "paste", "embedded matrix" });
	}

	protected void stats(String annotation, int col, Map<String, Integer> bucket, boolean min) {
		Integer val = bucket.get(annotation);
		if (val == null)
			bucket.put(annotation, col);
		else if (min) {
			if (col < val)
				bucket.put(annotation, col);
		} else {
			if (col > val)
				bucket.put(annotation, col);
		}
	}

	public Workbook generate(TemplateMakerSettings settings) throws Exception {
		Iterable<TR> records = settings.getTemplateRecords();
		HashSet<String> templateids = settings.getUniqueTemplateID(records);
		if (templateids.size() == 0)
			throw new Exception(String.format("Not found"));
		return generate(settings, templateids);
	}

	public Workbook generate(TemplateMakerSettings settings, String templateid) throws Exception {
		HashSet<String> templateids = new HashSet<String>();
		templateids.add(templateid);
		return generate(settings, templateids);
	}

	public Workbook generate(TemplateMakerSettings settings, HashSet<String> templateids) throws Exception {
		Workbook workbook = null;
		TemplateMakerSettings._TEMPLATES_TYPE[] ttypes = null;
		// settings
		switch (settings.getTemplatesType()) {
		case jrc: {
			ttypes = new TemplateMakerSettings._TEMPLATES_TYPE[] { settings.getTemplatesType() };
			break;
		}
		case multisheet: {
			ttypes = new TemplateMakerSettings._TEMPLATES_TYPE[] { settings.getTemplatesType() };
			break;
		}
		case all: {
			ttypes = new TemplateMakerSettings._TEMPLATES_TYPE[] { TemplateMakerSettings._TEMPLATES_TYPE.jrc,
					TemplateMakerSettings._TEMPLATES_TYPE.multisheet };
			break;
		}
		default:
			break;
		}

		if (templateids == null || templateids.size() == 0) {
			workbook = createWorkbook(workbook, null, settings, ttypes);
		} else {
			Iterator<String> i = templateids.iterator();
			while (i.hasNext())
				try {
					workbook = createWorkbook(workbook, i.next(), settings, ttypes);
				} catch (Exception x) {
					x.printStackTrace();
				}
		}
		if (settings.isSinglefile()) {
			settings.write("TEMPLATES", settings.getTemplatesType(), workbook);
			return workbook;
		} else
			return null;
	}

	protected Workbook createWorkbook(Workbook workbook, String idtemplate, TemplateMakerSettings settings,
			TemplateMakerSettings._TEMPLATES_TYPE[] ttypes) throws Exception {
		settings.getQuery().clear();
		settings.setQueryTemplateid(idtemplate);
		Iterable<TR> records = settings.getTemplateRecords();
		for (TemplateMakerSettings._TEMPLATES_TYPE ttype : ttypes) {
			settings.setTemplatesType(ttype);
			settings.setQueryTemplateid(idtemplate);
			switch (ttype) {
			case jrc: {
				workbook = generateJRCTemplates(settings.isSinglefile() ? workbook : null,
						settings.getQueryTemplateId(), records);
				if (!settings.isSinglefile())
					settings.write(String.format("%s_%s", workbook.getSheetAt(1).getSheetName(),
							settings.getQueryTemplateId()), _TEMPLATES_TYPE.jrc, workbook);
				break;
			}
			case multisheet:
				try {
					workbook = generateMultisheetTemplates(workbook, settings, records);

					break;
				} catch (Exception x) {
					x.printStackTrace();
				}
			default:
				break;
			}

		}
		return workbook;
	}

}
