package net.enanomapper.maker;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Header;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class TemplateMaker {
	protected Logger logger_cli;
	protected final static String header_results = "results";
	protected final static String header_method = "method and instrument information";
	protected final static String header_initialexposure = "analytical parameters - initial exposure media";
	protected final static String header_finalexposure = "analytical parameters - final exposure media";

	protected final static String header_imageanalysis = "image analysis and results";
	protected final static String header_sample = "sample information";
	protected final static String header_size = "size distribution";
	protected final static String header_cell = "cell";
	protected final static String header_endpoint = "endpoint_assay";
	protected final static String header_sop = "sop";
	protected final static String header_result_endpoint = "End-Point Outcome metric";

	public TemplateMaker() {
		this(Logger.getAnonymousLogger());
	}

	public TemplateMaker(Logger logger) {
		this.logger_cli = logger;
	}

	protected void generateIOMTemplates(Iterable<TR> records, TemplateMakerSettings settings, String sheetname)
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
			anchor.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
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

	public Workbook generateJRCTemplates(TemplateMakerSettings settings) throws Exception {
		logger_cli.log(Level.INFO, String.format("%s\t%s", settings.getTemplatesType(), settings.getAssayname()));
		Iterable<TR> records = getJSONConfig();
		String sheetname = settings.getAssayname();
		String endpoint = settings.getEndpointname();
		if (endpoint == null)
			throw new Exception("Endpoint not defined");
		if (sheetname == null)
			throw new Exception("Assay not defined");

		Workbook workbook = new XSSFWorkbook();
		CreationHelper factory = workbook.getCreationHelper();
		Sheet sheet = workbook.createSheet("instruction for data logging");
		insertLogo(workbook,sheet);
		sheet = workbook.createSheet(sheetname);
		workbook.setActiveSheet(1);
		Header header = sheet.getHeader();
		header.setCenter("Center Header");
		header.setLeft("Left Header");

		Map<String, CellStyle> style = new HashMap<String, CellStyle>();

		Map<String, Integer> mincol = new HashMap<String, Integer>();
		Map<String, Integer> maxcol = new HashMap<String, Integer>();

		for (TR record : records)
			try {
				if (sheetname.equals(record.get("Sheet")) && endpoint.equals(record.get("File"))) {

					int row = Integer.parseInt(record.get("Row").toString());
					int col = Integer.parseInt(record.get("Column").toString());

					Row xrow = sheet.getRow(row);
					if (xrow == null)
						xrow = sheet.createRow(row);
					Cell cell = xrow.getCell(col);
					if (cell == null) {
						cell = xrow.createCell(col);
						CellUtil.setAlignment(cell, workbook, CellStyle.ALIGN_CENTER);
					}

					Object v = record.get("cleanedvalue");
					String value = v == null ? "?????" : v.toString();

					if ("material state".equals(value)) {
						validation_materialstate(workbook, sheet, col);
					}

					Object annotation = record.get("Annotation");
					Object header1 = record.get("header1");
					Object hint = record.get("hint");

					logger_cli.log(Level.FINE, String.format("%s\t%s\t%s\t%s", row, col, value, annotation));

					if (hint != null && hint.toString().indexOf("yes/no") >= 0) {
						validation_common(workbook, sheet, col, new String[] { "yes", "no" });
					}

					Object unit = record.get("unit");
					;

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
						cstyle.setBorderTop(CellStyle.BORDER_NONE);
						cstyle.setBorderLeft(CellStyle.BORDER_NONE);
						cstyle.setBorderRight(CellStyle.BORDER_NONE);
						cstyle.setBorderBottom(CellStyle.BORDER_NONE);
						if (header_results.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
						} else if (header_sop.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
						} else if (header_method.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else if (header_sample.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.TAN.getIndex());
						} else if (header_size.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
						} else if (header_cell.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else if (header_endpoint.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
						} else if (header_imageanalysis.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
						} else if (header_initialexposure.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else if (header_finalexposure.equals(annotation)) {
							cstyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
						} else
							cstyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
						cstyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
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
								comment.setAuthor(sheetname);

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
				}

			} catch (Exception x) {
				x.printStackTrace();
			}

		setStyle(workbook, sheet, header_results, mincol, maxcol, style);
		setStyle(workbook, sheet, header_method, mincol, maxcol, style);
		setStyle(workbook, sheet, header_endpoint, mincol, maxcol, style);
		setStyle(workbook, sheet, header_size, mincol, maxcol, style);
		setStyle(workbook, sheet, header_sample, mincol, maxcol, style);
		setStyle(workbook, sheet, header_sop, mincol, maxcol, style);
		setStyle(workbook, sheet, header_imageanalysis, mincol, maxcol, style);

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

	public void write(Workbook workbook, TemplateMakerSettings settings) throws IOException {
		String endpoint = settings.getEndpointname();
		try (FileOutputStream out = new FileOutputStream(
				new File(settings.getOutputfolder(), String.format("%s_%s_COLUMNS.xlsx",
						endpoint == null ? "" : endpoint.replaceAll(".xlsx", ""), settings.getAssayname())))) {
			workbook.write(out);
		} finally {
			workbook.close();
		}
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

				cstyle.setBorderLeft(CellStyle.BORDER_THIN);
				cstyle.setBorderRight(CellStyle.BORDER_THIN);
				if (i == 0) {
					cstyle.setBorderTop(CellStyle.BORDER_MEDIUM);
					cstyle.setBorderLeft(CellStyle.BORDER_NONE);
					cstyle.setBorderRight(CellStyle.BORDER_NONE);

				} else if (i == 3)
					cstyle.setBorderTop(CellStyle.BORDER_HAIR);
				else
					cstyle.setBorderTop(CellStyle.BORDER_NONE);

				if (i == 4 || i == 0)
					cstyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
				else
					cstyle.setBorderBottom(CellStyle.BORDER_NONE);

				if (c == col1) {
					cstyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
				}
				if (c == col2)
					cstyle.setBorderRight(CellStyle.BORDER_MEDIUM);

				if (i > 4) {
					cstyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
					cstyle.setBorderBottom(CellStyle.BORDER_HAIR);
				}

				Cell cell = row.getCell(c);
				if (cell == null)
					cell = row.createCell(c);

				if (i > 4) {
					CellUtil.setAlignment(cell, workbook, CellStyle.ALIGN_LEFT);
					String unit = sheet.getRow(4).getCell(c).getStringCellValue();
					if (unit != null && !"".equals(unit)) {

						cstyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						CellUtil.setAlignment(cell, workbook, CellStyle.ALIGN_RIGHT);
					}
				}

				cell.setCellStyle(cstyle);
			}
			if (i == 0) {
				sheet.addMergedRegion(new CellRangeAddress(i, i, col1, col2));
				CellUtil.setAlignment(row.getCell(col1), workbook, CellStyle.ALIGN_CENTER);
			}
			if ("size distribution".equals(annotation) && i == 1) {
				sheet.addMergedRegion(new CellRangeAddress(i, i, col1, col2));
				CellUtil.setAlignment(row.getCell(col1), workbook, CellStyle.ALIGN_CENTER);
			} else if ("cell".equals(annotation) && i == 1) {
				sheet.addMergedRegion(new CellRangeAddress(i, i, col1, col2));
				CellUtil.setAlignment(row.getCell(col1), workbook, CellStyle.ALIGN_CENTER);
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
		validation_common(workbook, sheet, col, new String[] { "phys-chem", "in vitro tox", "in vivo tox", "eco tox" });
	}

	protected void validation_materialstate(Workbook workbook, Sheet sheet, Integer col) {
		validation_common(workbook, sheet, col, new String[] { "liquid", "fluid", "fluid dispersion", "powder" });
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

	protected Iterable<TR> getJSONConfig() throws Exception {
		return getJSONConfig("net/enanomapper/templates/JRCTEMPLATES_102016.json");
	}

	protected Iterable<TR> getJSONConfig(String config) throws Exception {

		InputStream in = TemplateMaker.class.getClassLoader().getResourceAsStream(config);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = null;
		List<TR> records = new ArrayList<TR>();
		try {
			root = mapper.readTree(in);
			if (root instanceof ArrayNode) {
				ArrayNode aNode = (ArrayNode) root;
				for (int i = 0; i < aNode.size(); i++) {
					TR record = new TR();
					records.add(record);
					JsonNode node = aNode.get(i);
					Iterator<String> fields = node.fieldNames();
					while (fields.hasNext()) {
						String field = fields.next();
						record.put(field, node.get(field).asText());
					}
				}
			}
		} catch (Exception x) {
			throw x;
		} finally {
			try {
				in.close();
			} catch (Exception x) {
			}
		}
		return records;
	}

	public void generate(TemplateMakerSettings settings) throws Exception {
		TemplateMakerSettings._TEMPLATES_TYPE[] ttypes = null;
		// settings
		switch (settings.getTemplatesType()) {
		case jrc: {
			ttypes = new TemplateMakerSettings._TEMPLATES_TYPE[] { settings.getTemplatesType() };
			break;
		}
		case iom: {
			ttypes = new TemplateMakerSettings._TEMPLATES_TYPE[] { settings.getTemplatesType() };
			break;
		}
		case all: {
			ttypes = new TemplateMakerSettings._TEMPLATES_TYPE[] { TemplateMakerSettings._TEMPLATES_TYPE.jrc,
					TemplateMakerSettings._TEMPLATES_TYPE.iom };
			break;
		}
		default:
			break;
		}
		if (settings.getEndpointname() != null) {
			Iterable<TR> records = getJSONConfig();
			HashSet<String> assays = new HashSet<String>();
			for (TR record : records) {
				// System.out.println(record.get("File"));
				if (settings.getEndpointname().equals(record.get("File").toString().trim())) {
					// System.out.println(record);
					if (settings.getAssayname() == null || record.get("Sheet").equals(settings.getAssayname()))
						assays.add(record.get("Sheet").toString());
				}
			}
			// generate
			Iterator<String> i = assays.iterator();
			while (i.hasNext())
				try {
					String assayname = i.next();
					for (TemplateMakerSettings._TEMPLATES_TYPE ttype : ttypes) {
						settings.setTemplatesType(ttype);
						records = getJSONConfig();
						settings.setAssayname(assayname);
						switch (ttype) {
						case jrc: {
							Workbook workbook = generateJRCTemplates(settings);
							write(workbook, settings);
							break;
						}
						case iom: {
							generateIOMTemplates(records, settings, assayname);
							break;
						}
						default:
							break;
						}

					}
				} catch (Exception x) {
					x.printStackTrace();
				}
		} else if (settings.getAssayname() != null) {

			for (TemplateMakerSettings._TEMPLATES_TYPE ttype : ttypes) {
				Iterable<TR> records = getJSONConfig();
				settings.setTemplatesType(ttype);
				switch (ttype) {
				case jrc: {
					generateJRCTemplates(settings);
					break;
				}
				case iom: {
					generateIOMTemplates(records, settings, settings.getAssayname());
					break;
				}
				default:
					break;
				}

			}
		} else {
			throw new Exception(String.format("Assay and endpoint not defined, use option -a and -e"));
		}
	}

}
