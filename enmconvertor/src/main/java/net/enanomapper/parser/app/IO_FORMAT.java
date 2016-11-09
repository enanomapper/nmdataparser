package net.enanomapper.parser.app;

public enum IO_FORMAT {
	xls {
		@Override
		public String toString() {
			return "Excel (.xls) spreadsheet, requires JSON configuration file (option -x) on input";
		}
		@Override
		public boolean isWrite() {
			return false;
		}
	},
	xlsx {
		@Override
		public String toString() {
			return "Excel (.xlsx) spreadsheet, requires JSON configuration file (option -x) on input";
		}
		@Override
		public boolean isWrite() {
			return false;
		}		
	},
	json {
		@Override
		public String toString() {
			return "AMBIT JSON";
		}
		@Override
		public boolean isWrite() {
			return false;
		}
	},
	NWrdf {
		@Override
		public String toString() {
			return "NanoWiki RDF (Semantic Media Wiki RDF export)";
		}
		public boolean isWrite() {
			return false;
		}		
	},	
	rdf {
		@Override
		public String toString() {
			return "eNanoMapper RDF (based on BioAssayOntology RDF)";
		}
	}, 
	isa {
		@Override
		public boolean isRead() {
			return false;
		}
		@Override
		public String toString() {
			return "ISA-JSON v1 (see https://github.com/ISA-tools/isa-api)";
		}
	},

	report {
		@Override
		public String toString() {
			return "Human readable Excel export";
		}
		@Override
		public boolean isRead() {
			return false;
		}
	};
	// xlsx
	public boolean isRead() {
		return true;
	}
	public boolean isWrite() {
		return true;
	}
	public static String describe(boolean includeR,boolean includeW) {
		StringBuilder b = new StringBuilder();
		String d = "";
		for (IO_FORMAT f : IO_FORMAT.values()) {
			if (includeR&&f.isRead() || includeW&&f.isWrite())
				b.append(String.format("%s%s",d,f.name()));
			d="|";
			//b.append(String.format("%s(%s%s)",f.name(),f.isRead()?"R":"",f.isWrite()?"W":""));
		}	
		return b.toString();
	}
	public static String list() {
		StringBuilder b = new StringBuilder();
		String d = "";
		for (IO_FORMAT f : IO_FORMAT.values()) {
			b.append(String.format("(%s%s)\t%s\t%s\n",f.isRead()?"R":"",f.isWrite()?"W":"",f.name(),f.toString()));
		}	
		return b.toString();
	}
}