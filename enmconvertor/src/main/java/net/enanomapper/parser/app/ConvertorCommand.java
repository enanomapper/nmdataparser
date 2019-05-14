package net.enanomapper.parser.app;

public enum ConvertorCommand {
	data {
		@Override
		public String input() {
			return  "Input file or folder";
		}
		@Override
		public String config() {
			return "JSON config file for input formats xls,xlsx";
		}
	},
	extracttemplatefields {
		@Override
		public String input() {
			return  "Root spreadsheet folder as used in .properties";
		}
		@Override
		public String config() {
			return ".properties file assigning JSON to worksheets";
		}
	},
	generatetemplate {
		@Override
		public String input() {
			return  "Input file or folder";
		}
		@Override
		public String config() {
			return null;
		}
	};
	public abstract String input();
	public abstract String config();
	
}
