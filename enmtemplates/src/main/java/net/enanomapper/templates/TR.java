package net.enanomapper.templates;

import java.io.Writer;
import java.util.HashMap;

public class TR extends HashMap<String, Object> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4974769546259809851L;
	public static final String header_string = "ID,Folder,File,Sheet,Row,Column,Value,Annotation,header1,cleanedvalue,unit,hint,JSON_LEVEL1,JSON_LEVEL2,JSON_LEVEL3,Warning";
	public static final String[] header = header_string.split(",");

	public enum hix {
		ID {
			@Override
			public boolean isAnnotation() {
				return false;
			}

		},
		Folder {
			@Override
			public boolean isAnnotation() {
				return false;
			}

			@Override
			public boolean isSearch() {
				return true;
			}
		},
		File {
			@Override
			public boolean isAnnotation() {
				return false;
			}

			@Override
			public boolean isSearch() {
				return true;
			}
		},
		Sheet {
			@Override
			public boolean isAnnotation() {
				return false;
			}

			@Override
			public boolean isSearch() {
				return true;
			}
		},
		Row {
			@Override
			public boolean isAnnotation() {
				return false;
			}

		},
		Column {
			@Override
			public boolean isAnnotation() {
				return false;
			}
		},
		Value {
			@Override
			public boolean isAnnotation() {
				return false;
			}

			@Override
			public boolean isSearch() {
				return true;
			}
		},
		Annotation, header1, cleanedvalue, unit, hint, JSON_LEVEL1, JSON_LEVEL2, JSON_LEVEL3, Warning {

			public boolean isAnnotation() {
				return false;
			}
		};
		public Object get(TR record) {
			return record.get(name());
		}

		public void set(TR record, Object value) {
			record.put(name(), value);
		}

		public boolean isSearch() {
			return false;
		}

		public boolean isAnnotation() {
			return true;
		}
	}

	public void write(Writer writer) throws Exception {
		for (hix h : hix.values()) {
			Object v = get(h.name());
			writer.write(v == null ?"":( v instanceof String)? ('"' + v.toString() + '"'):v.toString());
			writer.write(",");
		}
		writer.write("\n");
	}

}
