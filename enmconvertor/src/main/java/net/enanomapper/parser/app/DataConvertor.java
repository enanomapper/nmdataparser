package net.enanomapper.parser.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import ambit2.base.data.Property;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.StructureRecordValidator;
import ambit2.base.data.substance.SubstanceEndpointsBundle;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.relation.composition.CompositionRelation;
import ambit2.core.io.IRawReader;
import ambit2.core.io.json.SubstanceStudyParser;
import ambit2.export.isa.v1_0.ISAJsonExporter1_0;
import ambit2.rest.substance.SubstanceRDFReporter;
import net.enanomapper.parser.GenericExcelParser;
import net.enanomapper.parser.InvalidCommand;
import net.idea.loom.nm.nanowiki.ENanoMapperRDFReader;
import net.idea.loom.nm.nanowiki.NanoWikiRDFReader;

public class DataConvertor {

	protected static Logger logger_cli = Logger.getLogger(DataConvertor.class.getName());
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
	protected boolean gzipped = false;
	protected File jsonConfig;
	protected File inputFile;
	protected File outputFile;
	protected IO_FORMAT outformat = IO_FORMAT.json;
	protected IO_FORMAT informat = IO_FORMAT.xlsx;

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
		if (inputFile.toString().toLowerCase().endsWith(".gz"))
			gzipped = true;
		else
			gzipped = false;
	}

	protected IRawReader<IStructureRecord> createParser(InputStream stream, String extension) throws Exception {

		InputStream in = null;
		if (gzipped)
			in = new GZIPInputStream(stream);
		else
			in = stream;

		switch (informat) {
		case xls:
			return new GenericExcelParser(in, jsonConfig, false);
		case xlsx:
			return new GenericExcelParser(in, jsonConfig, true);
		case NWrdf:
			return new NanoWikiRDFReader(new InputStreamReader(in));
		case rdf:
			return new ENanoMapperRDFReader(new InputStreamReader(in), "ENM");
		case json:
			return new SubstanceStudyParser(new InputStreamReader(in, "UTF-8"));
		default:
			throw new Exception("Unsupported format " + informat);
		}

	}

	protected static IO_FORMAT getOutputFormat(CommandLine line) throws Exception {
		String format = _OPTIONS.outputformat.getOption(line);
		IO_FORMAT f = IO_FORMAT.json;
		try {
			f = IO_FORMAT.valueOf(format);
		} catch (Exception x) {
		}
		if (!f.isWrite())
			throw new Exception("Not an output format!");
		return f;
	}

	protected static IO_FORMAT getInputFormat(CommandLine line, IO_FORMAT defautlformat) throws Exception {
		String format = _OPTIONS.inputformat.getOption(line);
		IO_FORMAT f = defautlformat;
		try {
			f = IO_FORMAT.valueOf(format);
		} catch (Exception x) {
		}
		if (f==null || !f.isRead())
			throw new Exception("Not an input format!");
		return f;
	}

	protected static File getJSONConfig(CommandLine line) throws FileNotFoundException {
		String fname = _OPTIONS.xconfig.getOption(line);
		if (fname != null) {
			File file = new File(fname);
			if (!file.exists())
				throw new FileNotFoundException(file.getName());
			else
				return file;
		} else
			return null;
	}

	protected static File getInput(CommandLine line) throws FileNotFoundException {
		String fname = _OPTIONS.input.getOption(line);
		if (fname != null) {
			File file = new File(fname);
			if (!file.exists())
				throw new FileNotFoundException(file.getName());
			else
				return file;
		} else
			return null;
	}

	protected static File getOutput(CommandLine line) {
		String fname = _OPTIONS.output.getOption(line);
		return fname == null ? null : new File(fname);
	}

	public boolean parse(String[] args) throws Exception {
		final Options options = createOptions();
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine line = parser.parse(options, args, false);

			if (_OPTIONS.listformats.getOption(line) != null) {
				System.out.println(IO_FORMAT.list());
				return false;

			} else {

				setInputFile(getInput(line));

				if (inputFile == null && !inputFile.exists())
					throw new Exception("Missing input file");
				jsonConfig = getJSONConfig(line);

				String extension = inputFile.getName().toLowerCase();
				IO_FORMAT informat = extension == null ? null
						: extension.endsWith("xlsx") ? IO_FORMAT.xlsx
								: (extension.endsWith("xls") ? IO_FORMAT.xls :  (extension.endsWith("rdf") ? IO_FORMAT.rdf:null));

				switch (informat) {
				case xlsx:
				case xls: {
					if (jsonConfig == null)
						throw new Exception("Missing JSON config file, mandatory for importing XLSX! Use option -"
								+ _OPTIONS.xconfig.command());
					break;
				}
				default:
				}

				outputFile = getOutput(line);
				informat = getInputFormat(line,informat);
				outformat = getOutputFormat(line);

				return true;
			}
		} catch (Exception x) {
			printHelp(options, x.getMessage());
			throw x;
		} finally {

		}
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	protected int convertFile() throws Exception {
		if (inputFile.isDirectory()) {
			logger_cli.log(Level.INFO, "MSG_IMPORT", new Object[] { "folder", inputFile.getAbsolutePath() });
			File[] allFiles = inputFile.listFiles();
			long started = System.currentTimeMillis();
			int allrecords = 0;
			for (int i = 0; i < allFiles.length; i++) {
				File file = allFiles[i];
				setInputFile(file);
				try {
					allrecords += convertFile();
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
			String ext = inputFile.toString().toLowerCase();
			return convertFile(ext.endsWith(".xlsx"));

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
		default: {

		}
		}
		return 0;
	}

	public int writeAsJSON(IRawReader<IStructureRecord> reader, StructureRecordValidator validator, File outputFile)
			throws Exception {
		int records = 0;
		try {
			while (reader.hasNext()) {
				Object record = reader.next();
				if (record == null)
					continue;
				try {
					validator.process((IStructureRecord) record);
					Writer writer = new FileWriter(outputFile);
					writer.write(((SubstanceRecord) record).toJSON(null));
					writer.close();
				} catch (Exception x) {
					logger_cli.log(Level.FINE, x.getMessage());
				}
				records++;
			}

		} catch (Exception x) {
			logger_cli.log(Level.WARNING, x.getMessage(), x);
		} finally {

			logger_cli.log(Level.INFO, "MSG_IMPORTED", new Object[] { records });
		}
		return records;
	}

	public int writeAsRDF(IRawReader<IStructureRecord> reader, StructureRecordValidator validator, File outputFile)
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
		endpointBundle.setDescription(inputFile.getName());
		endpointBundle.setTitle(inputFile.getName());

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
				mapper.setSerializationInclusion(Inclusion.NON_EMPTY);
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

	protected int convertFile(boolean xlsx) throws Exception {
		IRawReader<IStructureRecord> parser = null;
		Connection c = null;
		try {
			FileInputStream fin = new FileInputStream(inputFile);

			parser = createParser(fin, inputFile.getName());
			logger_cli.log(Level.INFO, "MSG_IMPORT",
					new Object[] { parser.getClass().getName(), inputFile.getAbsolutePath() });

			StructureRecordValidator validator = new StructureRecordValidator(inputFile.getName(), true) {
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

			return write(parser, validator, outformat, outputFile);
		} catch (Exception x) {
			throw x;
		} finally {
			if (parser != null)
				parser.close();
			try {
				c.close();
			} catch (Exception x) {
			}
		}
	}

	protected Options createOptions() {
		Options options = new Options();
		for (_OPTIONS o : _OPTIONS.values())
			options.addOption(o.createOption());

		return options;
	}

	public static void main(String[] args) {
		logger_cli.log(Level.INFO, "MSG_INFO_VERSION");
		long now = System.currentTimeMillis();
		int code = 0;
		try {
			DataConvertor object = new DataConvertor();
			if (object.parse(args)) {
				object.convertFile();
			} else
				code = -1;

		} catch (ConnectException x) {
			logger_cli.log(Level.SEVERE, "MSG_CONNECTION_REFUSED", new Object[] { x.getMessage() });
			Runtime.getRuntime().runFinalization();
			code = -1;

		} catch (SQLException x) {
			logger_cli.log(Level.SEVERE, "MSG_ERR_SQL", new Object[] { x.getMessage() });
			code = -1;
		} catch (InvalidCommand x) {
			logger_cli.log(Level.SEVERE, "MSG_INVALIDCOMMAND", new Object[] { x.getMessage() });
			code = -1;
		} catch (Exception x) {
			logger_cli.log(Level.SEVERE, "MSG_ERR", new Object[] { x });
			code = -1;
		} finally {
			if (code >= 0)
				logger_cli.log(Level.INFO, "MSG_INFO_COMPLETED", (System.currentTimeMillis() - now));
		}
	}

	protected static void printHelp(Options options, String message) {
		if (message != null)
			logger_cli.log(Level.WARNING, message);

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("enmconvertor-{version}", options);
		Runtime.getRuntime().runFinalization();
		Runtime.getRuntime().exit(0);
	}

}
