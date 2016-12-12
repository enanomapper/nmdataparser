package net.enanomapper.templates.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.IndexedColors;
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

import net.enanomapper.templates.TR;
import net.enanomapper.templates.Term;
import net.enanomapper.templates.Tools;
import net.enanomapper.templates.app.MainAppSettings._TEMPLATES_CMD;
import net.enanomapper.templates.app.MainAppSettings._TEMPLATES_TYPE;

/**
 * Tools for processing templates
 * 
 * @author nina
 * 
 */
public class MainApp {
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

	protected static Logger logger_cli = Logger.getLogger(MainApp.class.getName());
	protected MainAppSettings settings;

	public MainAppSettings getSettings() {
		return settings;
	}

	public static void main(String[] args) {
		// logger_cli.log(Level.INFO, "MSG_INFO_VERSION");
		MainApp object = new MainApp();
		object.run(args);
	}

	public int run(String[] args) {
		// logger_cli.log(Level.INFO, "MSG_INFO_VERSION");
		long now = System.currentTimeMillis();
		int code = 0;
		try {
			final Options options = createOptions();
			CommandLineParser parser = new PosixParser();
			try {
				CommandLine line = parser.parse(options, args, false);
				settings = parse(parser, line);
				process(settings);
			} catch (Exception x) {
				x.printStackTrace();
				printHelp(options, x.getMessage());
				logger_cli.log(Level.SEVERE, "MSG_ERR", new Object[] { x });
			}
		} finally {
			if (code >= 0)
				logger_cli.log(Level.INFO, "MSG_INFO_COMPLETED", (System.currentTimeMillis() - now));
		}
		return code;
	}

	public MainAppSettings parse(CommandLineParser parser, CommandLine line) throws Exception {
		try {

			MainAppSettings s = new MainAppSettings();
			try {
				s.setTemplatesType(_TEMPLATES_TYPE.valueOf(getOption(line, 't')));
			} catch (Exception x) {
				s.setTemplatesType(_TEMPLATES_TYPE.all);
			}
			try {
				s.setTemplatesCommand(_TEMPLATES_CMD.valueOf(getOption(line, 'a')));
			} catch (Exception x) {
			}

			s.setInputfolder(new File(getOption(line, 'i')));

			s.setOutputfolder(new File(getOption(line, 'o')));

			try {
				s.setAssayname(getOption(line, 's'));
			} catch (Exception x) {
			}
			try {
				s.setEndpointname(getOption(line, 'e'));
				if (!s.getEndpointname().endsWith(".xlsx"))
					s.setEndpointname(s.getEndpointname() + ".xlsx");
			} catch (Exception x) {
			}

			if (s.getAssayname() == null)
				s.setAssayname("COMET");
			return s;
		} catch (Exception x) {

			throw x;
		} finally {

		}

	}

	protected String getOption(CommandLine line, char option) throws FileNotFoundException {
		return line.hasOption(option) ? line.getOptionValue(option) : null;
	}

	protected Options createOptions() {
		Options options = new Options();
		Option input = OptionBuilder.hasArg().withLongOpt("input").withArgName("folder").withDescription("Input folder")
				.create("i");

		Option output = OptionBuilder.hasArg().withLongOpt("output").withArgName("folder")
				.withDescription("Output folder").create("o");

		Option template = OptionBuilder.hasArg().withLongOpt("template").withArgName("type")
				.withDescription("Template type jrc|iom|all|undefined").create("t");

		Option cmd = OptionBuilder.hasArg().withLongOpt("command").withArgName("cmd")
				.withDescription("What to do: extract|generate").create("a");

		Option assay = OptionBuilder.hasArg().withLongOpt("assay").withArgName("assayname")
				.withDescription("Sheet name as defined in JRC templates").create("s");

		Option endpoint = OptionBuilder.hasArg().withLongOpt("endpoint").withArgName("endpointname")
				.withDescription("Endpoint name as defined in JRC templates").create("e");

		Option help = OptionBuilder.withLongOpt("help").withDescription("This help").create("h");

		options.addOption(input);
		options.addOption(output);
		options.addOption(template);
		options.addOption(assay);
		options.addOption(endpoint);
		options.addOption(cmd);

		options.addOption(help);

		return options;
	}

	protected static void printHelp(Options options, String message) {
		if (message != null)
			logger_cli.log(Level.WARNING, message);

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("enmtemplates", options);
		Runtime.getRuntime().runFinalization();
		Runtime.getRuntime().exit(0);
	}

	protected void process(MainAppSettings settings) throws Exception {
		switch (settings.getTemplatesCommand()) {
		case extract: {
			extract(settings);
			break;
		}
		case generate: {
			generate(settings);
			break;
		}
		default: {
			System.out.println("Unsupported command " + settings.getTemplatesCommand());
		}
		}
	}

	protected void generate(MainAppSettings settings) throws Exception {
		MainAppSettings._TEMPLATES_TYPE[] ttypes = null;
		// settings
		switch (settings.getTemplatesType()) {
		case jrc: {
			ttypes = new MainAppSettings._TEMPLATES_TYPE[] { settings.getTemplatesType() };
			break;
		}
		case iom: {
			ttypes = new MainAppSettings._TEMPLATES_TYPE[] { settings.getTemplatesType() };
			break;
		}
		case all: {
			ttypes = new MainAppSettings._TEMPLATES_TYPE[] { MainAppSettings._TEMPLATES_TYPE.jrc,
					MainAppSettings._TEMPLATES_TYPE.iom };
			break;
		}
		default:
			break;
		}
		if (settings.getEndpointname() != null) {
			Iterable<TR> records = getJSONConfig();
			HashSet<String> assays = new HashSet<String>();
			for (TR record : records) {

				if (settings.getEndpointname().equals(record.get("File").toString().trim())) {
					System.out.println(record);
					if (settings.getAssayname() == null || record.get("Sheet").equals(settings.getAssayname()))
						assays.add(record.get("Sheet").toString());
				}
			}
			// generate
			Iterator<String> i = assays.iterator();
			while (i.hasNext())
				try {
					String assayname = i.next();
					for (MainAppSettings._TEMPLATES_TYPE ttype : ttypes) {
						settings.setTemplatesType(ttype);
						records = getJSONConfig();
						settings.setAssayname(assayname);
						switch (ttype) {
						case jrc: {
							generateJRCTemplates(settings);
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

			for (MainAppSettings._TEMPLATES_TYPE ttype : ttypes) {
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

	protected void extract(MainAppSettings settings) throws Exception {
		System.out.println(settings);
		File[] files = settings.getInputfolder().listFiles();
		final Map<String, Term> histogram = new HashMap<String, Term>();
		/*
		 * BufferedWriter stats = new BufferedWriter( new OutputStreamWriter(new
		 * FileOutputStream(new File(settings.getOutputfolder(),
		 * settings.getInputfolder().getName() +
		 * ".txt")),StandardCharsets.UTF_8)); //BOM stats.write('\ufeff');
		 */
		XSSFWorkbook workbook = new XSSFWorkbook();
		FileOutputStream out = new FileOutputStream(
				new File(settings.getOutputfolder(), settings.getInputfolder().getName() + ".xlsx"));
		XSSFSheet stats = workbook.createSheet();
		switch (settings.getTemplatesType()) {
		case iom: {
			// stats.write("Folder,File,Sheet,Row,Column1,Column2,Value1,Value2\n");
			// todo
			break;
		}
		default: {
			TR.writeHeader(stats);
			// todostats.write(TR.header_string+"\n");
		}
		}

		try {
			int rownum = 0;
			for (File file : files) {
				try {
					switch (settings.getTemplatesType()) {
					case iom: {
						Tools.readIOMtemplates(file, settings.getInputfolder().getName(), file.getName(), histogram,
								stats);
						break;
					}
					default: {
						rownum = Tools.readJRCExcelTemplate(file, settings.getInputfolder().getName(), file.getName(),
								histogram, stats, settings.getAnnotator(), rownum);
					}
					}

					// stats.flush();
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		} catch (Exception x) {

		} finally {
			workbook.write(out);
			workbook.close();
			out.close();
		}

	}

	protected Iterable<TR> getJSONConfig() throws Exception {

		InputStream in = MainApp.class.getClassLoader()
				.getResourceAsStream("net/idea/magicmapper/JRCTEMPLATES_102016.json");
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

	protected void generateIOMTemplates(Iterable<TR> records, MainAppSettings settings, String sheetname)
			throws Exception {
		throw new Exception("Unsupported");
	}

	protected void generateJRCTemplates(MainAppSettings settings) throws Exception {
		System.out.println(String.format("%s\t%s", settings.getTemplatesType(), settings.getAssayname()));
		Iterable<TR> records = getJSONConfig();
		String sheetname = settings.getAssayname();
		String endpoint = settings.getEndpointname();
		if (endpoint == null)
			throw new Exception("Endpoint not defined");
		if (sheetname == null)
			throw new Exception("Assay not defined");

		Workbook workbook = new XSSFWorkbook();
		CreationHelper factory = workbook.getCreationHelper();
		workbook.createSheet("instruction for data logging");
		Sheet sheet = workbook.createSheet(sheetname);
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

					String value = record.get("cleanedvalue").toString();

					if ("material state".equals(value)) {
						validation_materialstate(workbook, sheet, col);
					}

					Object annotation = record.get("Annotation");
					Object header1 = record.get("header1");
					Object hint = record.get("hint");

					System.out.println(String.format("%s\t%s\t%s\t%s", row, col, value, annotation));

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
		FileOutputStream out = new FileOutputStream(new File(settings.getOutputfolder(), String
				.format("%s_%s_COLUMNS.xlsx", endpoint == null ? "" : endpoint.replaceAll(".xlsx", ""), sheetname)));
		workbook.write(out);
		workbook.close();
		out.close();

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

}
