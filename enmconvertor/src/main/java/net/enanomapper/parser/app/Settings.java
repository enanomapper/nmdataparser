package net.enanomapper.parser.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class Settings {
	protected Logger logger_cli;
	protected boolean gzipped = false;
	protected File jsonConfig;
	protected File inputFile;
	protected File outputFile;
	protected IO_FORMAT outformat = IO_FORMAT.json;
	protected ConvertorCommand command = ConvertorCommand.data;
	
	public Settings(Logger logger) {
		this.logger_cli = logger;
	}

	public boolean isGzipped() {
		return gzipped;
	}

	public void setGzipped(boolean gzipped) {
		this.gzipped = gzipped;
	}

	public File getJsonConfig() {
		return jsonConfig;
	}

	public void setJsonConfig(File jsonConfig) {
		this.jsonConfig = jsonConfig;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public IO_FORMAT getOutformat() {
		return outformat;
	}

	public void setOutformat(IO_FORMAT outformat) {
		this.outformat = outformat;
	}

	public IO_FORMAT getInformat() {
		return informat;
	}

	public void setInformat(IO_FORMAT informat) {
		this.informat = informat;
	}

	protected IO_FORMAT informat;

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

	public boolean parse(String[] args) throws Exception {
		final Options options = createOptions();
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine line = parser.parse(options, args, false);

			if (_OPTIONS.command.getOption(line) != null)
				try {
					command = ConvertorCommand.valueOf(_OPTIONS.command.getOption(line));
				} catch (Exception x) {
					command = ConvertorCommand.data;
				}
			else
				command = ConvertorCommand.data;

			switch (command) {
			case extracttemplatefields: {
				setInputFile(getInput(line));
				if (inputFile == null && !inputFile.exists())
					throw new FileNotFoundException("Missing input folder");
				outputFile = getOutput(line);
				if (outputFile == null && !outputFile.exists())
					throw new FileNotFoundException("Missing output folder");
				jsonConfig = getJSONConfig(line);
				
				
				return true;
			}
			case data: {

				if (_OPTIONS.listformats.getOption(line) != null) {
					System.out.println(IO_FORMAT.list());
					return false;

				} else {

					setInputFile(getInput(line));

					if (inputFile == null && !inputFile.exists())
						throw new FileNotFoundException("Missing input file");
					jsonConfig = getJSONConfig(line);

					String extension = inputFile.getName().toLowerCase();
					IO_FORMAT informat = extension == null ? null
							: extension.endsWith("xlsx") ? IO_FORMAT.xlsx
									: (extension.endsWith("xls") ? IO_FORMAT.xls
											: (extension.endsWith("rdf") ? IO_FORMAT.rdf : null));

					if (informat != null)
						switch (informat) {
						case xlsx:
						case xls: {
							if (jsonConfig == null)
								throw new Exception(
										"Missing JSON config file, mandatory for importing XLSX! Use option -"
												+ _OPTIONS.xconfig.command());
							break;
						}
						default:
						}

					outputFile = getOutput(line);
					setInformat(getInputFormat(line, informat));
					setOutformat(getOutputFormat(line));

					return true;
				}
			}
			default: {
				throw new Exception(String.format("Command not implemented [%s]", command));
			}
			}
		} catch (FileNotFoundException x) {
			throw x;
		} catch (Exception x) {
			printHelp(options, x.getMessage());
			throw x;
		} finally {

		}
	}

	protected Options createOptions() {
		Options options = new Options();
		for (_OPTIONS o : _OPTIONS.values())
			options.addOption(o.createOption());

		return options;
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
		if (f == null || !f.isRead())
			throw new Exception("Not an input format!");
		return f;
	}

	protected static File getJSONConfig(CommandLine line) throws FileNotFoundException {
		String fname = _OPTIONS.xconfig.getOption(line);
		if (fname != null) {
			File file = new File(fname);
			if (!file.exists())
				throw new FileNotFoundException(file.getAbsolutePath());
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

	protected void printHelp(Options options, String message) {
		if (message != null)
			logger_cli.log(Level.WARNING, message);

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("enmconvertor-{version}", options);
		Runtime.getRuntime().runFinalization();
		Runtime.getRuntime().exit(0);
	}

}
