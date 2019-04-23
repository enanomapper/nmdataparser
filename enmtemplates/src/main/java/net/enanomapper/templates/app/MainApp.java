package net.enanomapper.templates.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
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
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.enanomapper.maker.TR;
import net.enanomapper.maker.TemplateMaker;
import net.enanomapper.maker.TemplateMakerSettings;
import net.enanomapper.maker.TemplateMakerSettings._TEMPLATES_TYPE;
import net.idea.templates.generation.Term;
import net.idea.templates.generation.Tools;


/**
 * Tools for processing templates
 * 
 * @author nina
 * 
 */
public class MainApp {

	protected TemplateMaker maker;

	protected static Logger logger_cli = Logger.getLogger(MainApp.class.getName());
	protected TemplateMakerSettings settings;

	public TemplateMakerSettings getSettings() {
		return settings;
	}

	public MainApp() {
		super();
		maker = createTemplateMaker(logger_cli);
	}

	protected TemplateMaker createTemplateMaker(Logger logger) {
		return new TemplateMaker(logger);
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
				//x.printStackTrace();
				printHelp(options, x.getMessage());
				logger_cli.log(Level.SEVERE, "MSG_ERR", new Object[] { x });
			}
		} finally {
			if (code >= 0)
				logger_cli.log(Level.INFO, "MSG_INFO_COMPLETED", (System.currentTimeMillis() - now));
		}
		return code;
	}

	public TemplateMakerSettings parse(CommandLineParser parser, CommandLine line) throws Exception {
		try {

			TemplateMakerSettings s = new TemplateMakerSettings();
			try {
				s.setTemplatesType(_TEMPLATES_TYPE.valueOf(getOption(line, 't')));
			} catch (Exception x) {
				s.setTemplatesType(_TEMPLATES_TYPE.all);
			}
			try {
				s.setTemplatesCommand(TemplateMakerSettings._TEMPLATES_CMD.valueOf(getOption(line, 'a')));
			} catch (Exception x) {
			}

			try {
				s.setAssayname(getOption(line, 's'));
			} catch (Exception x) {
			}

			String infile = getOption(line, 'i');
			if (infile != null)
				s.setInputfolder(new File(infile));
			else {
				s.setInputfolder(null);
				if (s.getTemplatesCommand().requiresInputFile())
					throw new Exception("Input file missing, see option -i");

			}

			String outfile = getOption(line, 'o');
			if (outfile != null)
				s.setOutputfolder(new File(outfile));
			else {
				s.setOutputfolder(null);
				if (s.getTemplatesCommand().requiresOutputFileFile())
					throw new Exception("Output file/folder missing, see option -o");
			}

			try {
				s.setEndpointname(getOption(line, 'e'));
				if (!s.getEndpointname().endsWith(".xlsx"))
					s.setEndpointname(s.getEndpointname());
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
				.withDescription("What to do: extract|generate|annotate|help").create("a");

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

	protected void process(TemplateMakerSettings settings) throws Exception {
		switch (settings.getTemplatesCommand()) {
		case extract: {
			extract(settings);
			break;
		}
		case generate: {
			maker.generate(settings);
			break;
		}
		case annotate: {
			annotate(settings);
			break;
		}
		case help: {
			final Options options = createOptions();

			printHelp(options, "");
			break;
		}
		default: {
			System.out.println("Unsupported command " + settings.getTemplatesCommand());
		}
		}
	}

	protected void annotate(TemplateMakerSettings settings) throws Exception {
		throw new Exception("Unsupported command " + settings.getTemplatesCommand());
	}

	protected void extract(TemplateMakerSettings settings) throws Exception {
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

}
