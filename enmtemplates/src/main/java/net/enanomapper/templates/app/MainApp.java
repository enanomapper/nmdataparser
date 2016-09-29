package net.enanomapper.templates.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.enanomapper.templates.Term;
import net.enanomapper.templates.Tools;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 * Tools for processing templates
 * 
 * @author nina
 * 
 */
public class MainApp {
	protected static Logger logger_cli = Logger.getLogger(MainApp.class
			.getName());

	public static void main(String[] args) {
		// logger_cli.log(Level.INFO, "MSG_INFO_VERSION");
		long now = System.currentTimeMillis();
		int code = 0;
		try {
			MainApp object = new MainApp();
			MainAppSettings s = object.parse(args);
			object.process(s);

		} catch (ConnectException x) {
			logger_cli.log(Level.SEVERE, "MSG_CONNECTION_REFUSED",
					new Object[] { x.getMessage() });
			Runtime.getRuntime().runFinalization();
			code = -1;

		} catch (SQLException x) {
			logger_cli.log(Level.SEVERE, "MSG_ERR_SQL",
					new Object[] { x.getMessage() });
			code = -1;
		} catch (Exception x) {
			logger_cli.log(Level.SEVERE, "MSG_ERR", new Object[] { x });
			code = -1;
		} finally {
			if (code >= 0)
				logger_cli.log(Level.INFO, "MSG_INFO_COMPLETED",
						(System.currentTimeMillis() - now));
		}
	}

	public MainAppSettings parse(String[] args) throws Exception {
		final Options options = createOptions();
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine line = parser.parse(options, args, false);

			MainAppSettings s = new MainAppSettings();
			s.setInputfolder(new File(getOption(line, 'i')));
			s.setOutputfolder(new File(getOption(line, 'o')));

			return s;
		} catch (Exception x) {
			printHelp(options, x.getMessage());
			throw x;
		} finally {

		}
	}

	protected String getOption(CommandLine line, char option)
			throws FileNotFoundException {
		return line.hasOption(option) ? line.getOptionValue(option) : null;
	}

	protected Options createOptions() {
		Options options = new Options();
		Option input = OptionBuilder.hasArg().withLongOpt("input")
				.withArgName("folder").withDescription("Input folder")
				.create("i");

		Option output = OptionBuilder.hasArg().withLongOpt("output")
				.withArgName("folder").withDescription("Output folder")
				.create("o");

		Option help = OptionBuilder.withLongOpt("help")
				.withDescription("This help").create("h");

		options.addOption(input);
		options.addOption(output);

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
		System.out.println(settings);
		File[] files = settings.getInputfolder().listFiles();
		final Map<String, Term> histogram = new HashMap<String, Term>();
		BufferedWriter stats = new BufferedWriter(new FileWriter(new File(
				settings.getOutputfolder(), settings.getInputfolder().getName()
						+ ".txt")));
		stats.write("Folder\tFile\tSheet\tRow\tColumn\tValue\n");
		try {
			for (File file : files)
				try {
					Tools.readJRCExcelTemplate(file, settings.getInputfolder()
							.getName(), file.getName(), histogram, stats);
					stats.flush();
				} catch (Exception x) {
					x.printStackTrace();
				}
		} catch (Exception x) {

		} finally {
			stats.close();
		}

	}
}
