package net.enanomapper.parser.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import ambit2.base.data.Property;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.SubstanceRecord.jsonSubstance;
import ambit2.base.data.study.StructureRecordValidator;
import ambit2.base.data.substance.SubstanceEndpointsBundle;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.relation.composition.CompositionRelation;
import ambit2.base.ro.SubstanceRecordAnnotationProcessor;
import ambit2.core.io.IRawReader;
import ambit2.core.io.json.SubstanceStudyParser;
import ambit2.export.isa.v1_0.ISAJsonExporter1_0;
import ambit2.rest.AmbitFreeMarkerApplication;
import ambit2.rest.substance.SubstanceRDFReporter;
import net.enanomapper.maker.JsonConfigAnnotator;
import net.enanomapper.maker.TR;
import net.enanomapper.maker.TemplateMakerExtended;
import net.enanomapper.maker.TemplateMakerSettings;
import net.enanomapper.maker.TemplateMakerSettings._TEMPLATES_CMD;
import net.enanomapper.maker.TemplateMakerSettings._TEMPLATES_TYPE;
import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.GenericExcelParser;
import net.enanomapper.parser.InvalidCommand;
import net.idea.loom.nm.nanowiki.ENanoMapperRDFReader;
import net.idea.loom.nm.nanowiki.NanoWikiRDFReader;
import net.idea.restnet.c.ChemicalMediaType;
import net.idea.templates.extraction.AssayTemplatesParser;
import net.idea.templates.generation.Term;
import net.idea.templates.generation.Tools;

public class DataConvertor {
	protected transient Settings settings;
	protected static transient Logger logger_cli = Logger.getLogger(DataConvertor.class.getName());
	static final String loggingProperties = "net/enanomapper/logging.properties";
	static final String log4jProperties = "net/enanomapper/log4j.properties";
	static {
		logger_cli = Logger.getLogger("dataconvertor", "net.enanomapper.msg");
		Locale.setDefault(Locale.ENGLISH);
		String dOption = System.getProperty("java.util.logging.config.file");
		if (dOption == null || "".equals(dOption)) {
			InputStream in = null;
			try {
				in = DataConvertor.class.getClassLoader().getResourceAsStream(loggingProperties);
				LogManager.getLogManager().readConfiguration(in);

			} catch (Exception x) {
				logger_cli.log(Level.WARNING, x.getMessage());
			} finally {
				try {
					in.close();
				} catch (Exception x) {
				}
			}
		}
		// now log4j for those who use it
		InputStream in = null;
		try {
			in = DataConvertor.class.getClassLoader().getResourceAsStream(log4jProperties);
			PropertyConfigurator.configure(in);

		} catch (Exception x) {
			logger_cli.log(Level.WARNING, x.getMessage());
		} finally {
			try {
				in.close();
			} catch (Exception x) {
			}
		}

	}

	public DataConvertor(Settings settings) {
		this.settings = settings;
	}

	public DataConvertor() {
		settings = new Settings(logger_cli);
	}

	protected IRawReader<IStructureRecord> createParser(InputStream stream, String extension) throws Exception {

		InputStream in = null;
		if (settings.isGzipped())
			in = new GZIPInputStream(stream);
		else
			in = stream;

		switch (settings.getInformat()) {
		case xls: {
			if (settings.getJsonConfig() == null)
				throw new FileNotFoundException("No JSON config file, use option -x");
			return new GenericExcelParser(in, settings.getJsonConfig(), false);
		}
		case xlsx: {
			if (settings.getJsonConfig() == null)
				throw new FileNotFoundException("No JSON config file, use option -x");
			return new GenericExcelParser(in, settings.getJsonConfig(), true);
		}
		case NWrdf:
			return new NanoWikiRDFReader(new InputStreamReader(in));
		case rdf:
			return new ENanoMapperRDFReader(new InputStreamReader(in), "ENM");
		case json:
			return new SubstanceStudyParser(new InputStreamReader(in, "UTF-8"));
		default:
			throw new Exception("Unsupported format " + settings.getInformat());
		}

	}

	public boolean parse(String[] args) throws Exception {
		return settings.parse(args);
	}

	protected void makeTemplate() throws Exception {
		long now = System.currentTimeMillis();
		if (settings.getJsonConfig() == null)
			throw new Exception("Template definitions not found");
		TemplateMakerSettings tsettings = new TemplateMakerSettings() {

			public java.lang.Iterable<TR> getTemplateRecords() throws Exception {
				try (InputStream in = new FileInputStream(settings.getJsonConfig())) {
					return getTemplateRecords(in);
				}
			};
		};
		tsettings.setSinglefile(true);
		
		switch (settings.getOutformat()) {
		case xlsx_multisheet: {
			tsettings.setTemplatesType(_TEMPLATES_TYPE.multisheet);
			break;
		}
		default: {
			tsettings.setTemplatesType(_TEMPLATES_TYPE.jrc);	
		}
		}
		tsettings.setTemplatesCommand(_TEMPLATES_CMD.generate);

		tsettings.getQuery().clear();
		// tsettings.setQueryEndpoint(endpoint);
		HashSet<String> templateids = new HashSet<String>();
		templateids.add(settings.getTemplateid());
		TemplateMakerExtended maker = new TemplateMakerExtended();
		tsettings.setInputfolder(settings.getJsonConfig());
		tsettings.setOutputfolder(settings.getOutputFile());
		Workbook workbook = maker.generate(tsettings, templateids);

		logger_cli.log(Level.INFO, "MSG_GENERATETEMPLATE_COMPLETE",
				new Object[] { settings.templateid, settings.getOutputFile().getAbsolutePath(),
						settings.getJsonConfig().getAbsolutePath(), (System.currentTimeMillis() - now) });
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	protected int convertFiles() throws Exception {
		if (settings.getInputFile().isDirectory()) {
			logger_cli.log(Level.INFO, "MSG_IMPORT",
					new Object[] { "folder", settings.getInputFile().getAbsolutePath() });
			File[] allFiles = settings.getInputFile().listFiles();
			long started = System.currentTimeMillis();
			int allrecords = 0;
			for (int i = 0; i < allFiles.length; i++) {
				File file = allFiles[i];
				settings.setInputFile(file);
				try {
					allrecords += convertFiles();
				} catch (Exception x) {
					logger_cli.log(Level.INFO, "MSG_ERR", new Object[] { x.getMessage() });
				} finally {
					long now = System.currentTimeMillis();
					logger_cli.log(Level.INFO, "MSG_INFO_RECORDS",
							new Object[] { (i + 1), (double) (now - started) / ((double) (i + 1)), allrecords,
									(double) (now - started) / ((double) allrecords) });
				}
			}
			return allrecords;
		} else {
			return convertSingleFile();

		}
	}

	public int write(IRawReader<IStructureRecord> reader, StructureRecordValidator validator, IO_FORMAT outformat,
			File outputFile) throws Exception {
		switch (outformat) {
		case json: {
			return writeAsJSON(reader, validator, outputFile);
		}
		case isa: {
			return writeAsISA(reader, validator, outputFile);
		}
		case rdf: {
			return writeAsRDF(reader, validator, outputFile);
		}
		case report: {
			return writeAsReport(reader, validator, outputFile);
		}
		default: {

		}
		}
		return 0;
	}
	//tbd use annotator = new SubstanceRecordAnnotationProcessor
	public int writeAsJSON(IRawReader<IStructureRecord> reader, StructureRecordValidator validator, File outputFile)
			throws Exception {
		int records = 0;
		try (Writer writer = new FileWriter(outputFile)) {
			writer.write("{ \"substance\" : [\n");
			String delimiter = "";
			while (reader.hasNext()) {
				Object record = reader.next();
				if (record == null)
					continue;
				try  {
					validator.process((IStructureRecord) record);
					;
					String tmp = ((SubstanceRecord) record).toJSON("http://localhost/ambit2",true);
					writer.write(delimiter);
					writer.write(tmp);
					writer.flush();
					delimiter =",";
				} catch (Exception x) {
					x.printStackTrace();
					logger_cli.log(Level.FINE, x.getMessage());
				}
				records++;
			}
			writer.write("\n]}");
		} catch (Exception x) {
			logger_cli.log(Level.WARNING, x.getMessage(), x);
		} finally {
			
			logger_cli.log(Level.INFO, "MSG_IMPORTED", new Object[] { records });
		}
		return records;
	}

	public int writeAsRDF(IRawReader<IStructureRecord> reader, StructureRecordValidator validator, File outputFile)
			throws Exception {

		SubstanceRecordAnnotationProcessor annotator = null;
		try {
			annotator = new SubstanceRecordAnnotationProcessor(annotationFolder,false);
		} catch (Exception x) {
			Logger.getGlobal().log(Level.WARNING,x.getMessage());
			annotator = null;
		}	
		
		Request hack = new Request();
		hack.setRootRef(new Reference("http://localhost/ambit2"));
		MediaType outmedia = MediaType.TEXT_RDF_N3;
		
		String ext = FilenameUtils.getExtension(settings.getOutputFile().getName().toLowerCase());
		if ("rdf".equals(ext))
			outmedia = MediaType.APPLICATION_RDF_XML;
		else if ("ttl".equals(ext))
			outmedia = MediaType.APPLICATION_RDF_TURTLE;
		else if ("json".equals(ext))
			outmedia = ChemicalMediaType.APPLICATION_JSONLD;
		
				
		SubstanceRDFReporter exporter = new SubstanceRDFReporter(hack, outmedia);
		Model model = ModelFactory.createDefaultModel();
		exporter.header(model, null);
		exporter.setOutput(model);

		int records = 0;
		try {
			while (reader.hasNext()) {
				Object record = reader.next();
				if (record == null)
					continue;
				try {
					validator.process((IStructureRecord) record);
					exporter.processItem((SubstanceRecord) record);
				} catch (Exception x) {
					logger_cli.log(Level.FINE, x.getMessage());
				}
				records++;
			}
			FileOutputStream out = new FileOutputStream(outputFile);
			RDFDataMgr.write(out, model, RDFFormat.TURTLE);
			out.close();
		} catch (Exception x) {
			logger_cli.log(Level.WARNING, x.getMessage(), x);
		} finally {
			if (exporter != null)
				exporter.close();
			logger_cli.log(Level.INFO, "MSG_IMPORTED", new Object[] { records });
		}
		return records;
	}

	public int writeAsReport(IRawReader<IStructureRecord> reader, StructureRecordValidator validator, File outputFile)
			throws Exception {

		Request hack = new Request();
		hack.setRootRef(new Reference("http://localhost/ambit2"));

		SubstanceRDFReporter exporter = new SubstanceRDFReporter(hack, MediaType.TEXT_RDF_N3);
		Model model = ModelFactory.createDefaultModel();
		exporter.header(model, null);
		exporter.setOutput(model);

		int records = 0;
		try {
			while (reader.hasNext()) {
				Object record = reader.next();
				if (record == null)
					continue;
				try {
					validator.process((IStructureRecord) record);
					exporter.processItem((SubstanceRecord) record);
				} catch (Exception x) {
					logger_cli.log(Level.FINE, x.getMessage());
				}
				records++;
			}
			FileOutputStream out = new FileOutputStream(outputFile);
			RDFDataMgr.write(out, model, RDFFormat.TURTLE);
			out.close();
		} catch (Exception x) {
			logger_cli.log(Level.WARNING, x.getMessage(), x);
		} finally {
			if (exporter != null)
				exporter.close();
			logger_cli.log(Level.INFO, "MSG_IMPORTED", new Object[] { records });
		}
		return records;
	}

	public int writeAsISA(IRawReader<IStructureRecord> reader, StructureRecordValidator validator, File outputFile)
			throws Exception {
		SubstanceEndpointsBundle endpointBundle = null;
		endpointBundle = new SubstanceEndpointsBundle();
		endpointBundle.setDescription(settings.getInputFile().getName());
		endpointBundle.setTitle(settings.getInputFile().getName());

		ISAJsonExporter1_0 exporter;
		try {
			exporter = new ISAJsonExporter1_0();
			exporter.init(endpointBundle);
			exporter.setOutputDir(outputFile);
		} catch (Exception x) {
			logger_cli.log(Level.SEVERE, x.getMessage());
			throw x;
		}

		int records = 0;
		try {
			while (reader.hasNext()) {
				Object record = reader.next();
				if (record == null)
					continue;
				try {
					validator.process((IStructureRecord) record);
					exporter.process((SubstanceRecord) record);
				} catch (Exception x) {
					logger_cli.log(Level.FINE, x.getMessage());
				}
				records++;
			}
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(Include.NON_EMPTY);
				mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, exporter.getOutput());
			} catch (Exception x) {
				logger_cli.log(Level.WARNING, x.getMessage());
			} finally {
				try {
					exporter.close();
				} catch (Exception x) {
				}
				exporter = null;
			}
		} catch (Exception x) {
			logger_cli.log(Level.WARNING, x.getMessage(), x);
		} finally {
			if (exporter != null)
				exporter.close();
			logger_cli.log(Level.INFO, "MSG_IMPORTED", new Object[] { records });
		}
		return records;
	}

	protected int convertSingleFile() throws Exception {
		IRawReader<IStructureRecord> parser = null;

		try (FileInputStream fin = new FileInputStream(settings.getInputFile())) {

			parser = createParser(fin, settings.getInputFile().getName());
			logger_cli.log(Level.INFO, "MSG_IMPORT",
					new Object[] { parser.getClass().getName(), settings.getInputFile().getAbsolutePath() });

			StructureRecordValidator validator = new StructureRecordValidator(settings.getInputFile().getName(), true,
					"XLSX") {
				@Override
				public IStructureRecord validate(SubstanceRecord record) throws Exception {
					if (record.getRelatedStructures() != null && !record.getRelatedStructures().isEmpty()) {

						for (int i = record.getRelatedStructures().size() - 1; i >= 0; i--) {
							CompositionRelation rel = record.getRelatedStructures().get(i);
							int props = 0;
							for (Property p : rel.getSecondStructure().getRecordProperties()) {
								Object val = rel.getSecondStructure().getRecordProperty(p);
								if (val != null && !"".equals(val.toString()))
									props++;
							}
							if ((rel.getContent() == null || "".equals(rel.getContent())) && (props == 0))
								record.getRelatedStructures().remove(i);

						}

					}
					return super.validate(record);
				}
			};

			return write(parser, validator, settings.getOutformat(), settings.getOutputFile());
		} catch (Exception x) {
			throw x;
		} finally {
			if (parser != null)
				parser.close();
		}
	}

	public static void main(String[] args) {
		DataConvertor object = new DataConvertor();
		logger_cli.log(Level.INFO, "MSG_INFO_VERSION");
		long now = System.currentTimeMillis();
		int code = -1;
		try {
			if (object.parse(args))
				code = object.go();
			else
				code = -1;
		} catch (ConnectException x) {
			logger_cli.log(Level.SEVERE, "MSG_CONNECTION_REFUSED", new Object[] { x.getMessage() });
			Runtime.getRuntime().runFinalization();
			code = -1;
		} catch (FileNotFoundException x) {
			logger_cli.log(Level.SEVERE, "MSG_FILENOTFOUND", new Object[] { x.getMessage() });
			code = -1;
		} catch (SQLException x) {
			logger_cli.log(Level.SEVERE, "MSG_ERR_SQL", new Object[] { x.getMessage() });
			code = -1;
		} catch (InvalidCommand x) {
			logger_cli.log(Level.SEVERE, "MSG_INVALIDCOMMAND", new Object[] { x.getMessage() });
			code = -1;
		} catch (Exception x) {
			x.printStackTrace();
			logger_cli.log(Level.SEVERE, "MSG_ERR", new Object[] { x });
			code = -1;
		} finally {
			if (code >= 0)
				logger_cli.log(Level.INFO, "MSG_INFO_COMPLETED", (System.currentTimeMillis() - now));
		}
	}

	public int go() throws Exception {

		switch (settings.command) {
		case extracttemplatefields: {
			spreadsheets2template();
			break;
		}
		case generatejsonconfig: {
			generate_jsonconfig();
			break;
		}
		case data: {
			convertFiles();
			break;
		}
		case generatetemplate: {
			makeTemplate();
			break;
		}
		default:
			throw new Exception("Unknown command");
		}
		return 0;

	}

	protected String[] spreadsheets2template() throws Exception {
		long now = System.currentTimeMillis();
		if (settings.getInputFile().isDirectory() && settings.getOutputFile().isDirectory()) {
			logger_cli.log(Level.INFO, "MSG_EXTRACTFIELDS",
					new Object[] { settings.getInputFile(), settings.getJsonConfig(), settings.getOutputFile() });
			try {
				String[] results = spreadsheets2template(settings.getInputFile(), settings.getJsonConfig(),
						settings.getOutputFile());
				logger_cli.log(Level.INFO, "MSG_EXTRACTFIELDS_COMPLETED",
						new Object[] { results[0], results[1], results[2] });
				return results;
			} catch (Exception x) {
				logger_cli.log(Level.INFO, "MSG_ERR", new Object[] { x.getMessage() });
				throw x;
			} finally {
				logger_cli.log(Level.INFO, "MSG_INFO_COMPLETED", (System.currentTimeMillis() - now));
			}

		} else
			throw new FileNotFoundException("Folders expected");
	}

	public static String[] spreadsheets2template(File rootSpreadsheetsFolder, File propertiesFolder, File outputFolder)
			throws FileNotFoundException, IOException, IllegalArgumentException, Exception {
		final Map<String, Term> histogram = new HashMap<String, Term>();
		File templates = new File(outputFolder, "templates.xlsx");
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			try (FileOutputStream out = new FileOutputStream(templates)) {
				XSSFSheet stats = workbook.createSheet();
				TR.writeHeader(stats);
				AssayTemplatesParser parser = new AssayTemplatesParser() {
					int rownum = 0;

					@Override
					protected int processFile(File spreadsheet, File json, String prefix, boolean resetdb,
							String release, String expandconfig) throws Exception {

						try {
							ExcelParserConfigurator config = ExcelParserConfigurator.loadFromJSON(json);
							JsonConfigAnnotator annotator = new JsonConfigAnnotator(config);

							rownum = Tools.readJRCExcelTemplate(spreadsheet, spreadsheet.getParentFile().getName(),
									spreadsheet.getName(), histogram, stats, annotator, rownum, config.sheetIndex);
							return rownum;
						} catch (Exception x) {
							System.out.println(String.format("%s\t%s", spreadsheet.getName(), json.getAbsolutePath()));
							x.printStackTrace();
							return 0;
						}

					}

					@Override
					public String getRootValue(ResourceBundle nanodataResources) {
						return rootSpreadsheetsFolder.getAbsolutePath();
					}
				};
				File[] all;
				if (propertiesFolder.isDirectory())
					all = propertiesFolder.listFiles();
				else
					all = new File[] { propertiesFolder };
				for (File resource : all)
					if (resource.getName().endsWith(".properties")) {
						try (FileReader reader = new FileReader(resource)) {
							PropertyResourceBundle nanodataResources = new PropertyResourceBundle(reader);
							parser.parseResources(nanodataResources, "TEST", false, null);
						}
					}

				workbook.write(out);

			}
		}
		// System.out.println("Generating json file...");
		File templatesjson = new File(outputFolder, "templates.json");
		File templatessql = new File(outputFolder, "templates.sql");
		AssayTemplatesParser.xls2json(templates, templatesjson);
		AssayTemplatesParser.json2sql(templatesjson, templatessql);
		return new String[] { templates.getAbsolutePath(), templatesjson.getAbsolutePath(),
				templatessql.getAbsolutePath() };
	}

	public String[] generate_jsonconfig() throws Exception {

		if (settings.getInputFile().isDirectory())
			throw new FileNotFoundException(settings.getInputFile().toString());
		if (!settings.getOutputFile().isDirectory())
			throw new FileNotFoundException(settings.getOutputFile().toString());
		logger_cli.log(Level.INFO, "MSG_GENERATEJSON",
				new Object[] { settings.getInputFile(), settings.getOutputFile() });

		String[] results = AssayTemplatesParser.generate_jsonconfig(settings.getInputFile(), settings.getOutputFile(),
				settings.getSheetNumber());
		logger_cli.log(Level.INFO, "MSG_EXTRACTFIELDS_COMPLETED",
				new Object[] { settings.getInputFile().toString(), results[0] });
		return results;
	}

}
