package net.enanomapper.parser.app;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public enum _OPTIONS {
	input {
		@Override
		public String argName() {
			return "file";
		}

		@Override
		public String description() {
			return "Input file or folder";
		}

		@Override
		public String command() {
			return "i";
		}
	},
	inputformat {
		@Override
		public String argName() {
			return "format";
		}

		@Override
		public String description() {
			return IO_FORMAT.describe(true, false) + "\nIf not specified, recognised from input file extension";
		}

		@Override
		public String command() {
			return "I";
		}
	},
	output {
		@Override
		public String argName() {
			return "file";
		}

		@Override
		public String description() {
			return "Output file or folder";
		}

		@Override
		public String command() {
			return "o";
		}
	},
	outputformat {
		@Override
		public String argName() {
			return "format";
		}

		@Override
		public String description() {
			return IO_FORMAT.describe(false, true) + "\nIf not specified, recognised from output file extension";
		}

		@Override
		public String command() {
			return "O";
		}
	},
	xconfig {
		@Override
		public String argName() {
			return "file";
		}

		@Override
		public String description() {
			return "JSON config file for input formats xls,xlsx";
		}

		@Override
		public String command() {
			return "x";
		}
	},
	listformats {
		@Override
		public String argName() {
			return "read|write";
		}

		@Override
		public String description() {
			return "List supported formats";
		}

		@Override
		public String command() {
			return "L";
		}

		@Override
		public boolean hasArg() {
			return false;
		}
		public String getOption(CommandLine line) {
			return line.hasOption(command())?"true":null;

		}
	},
	help {
		@Override
		public String argName() {
			return "";
		}

		@Override
		public String description() {
			return "This help";
		}

		@Override
		public String command() {
			return "h";
		}

		@Override
		public boolean hasArg() {
			return false;
		}
	};
	public boolean hasArg() {
		return true;
	}

	public abstract String argName();

	public abstract String command();

	public abstract String description();

	public Option createOption() {
		return OptionBuilder.hasArg(hasArg()).withLongOpt(name()).withArgName(argName()).withDescription(description())
				.create(command());
	}

	public String getOption(CommandLine line) {
		if (line.hasOption(command()))
			return line.getOptionValue(command());
		else
			return null;
	}
}