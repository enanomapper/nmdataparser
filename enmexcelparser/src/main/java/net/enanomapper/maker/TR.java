package net.enanomapper.maker;

import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;

import ambit2.base.json.JSONUtils;

public class TR extends HashMap<String, Object> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4974769546259809851L;
	public static final String header_string = "ID\tFolder\tFile\tSheet\tRow\tColumn\tValue\tAnnotation\theader1\tcleanedvalue\tunit\thint\tJSON_LEVEL1\tJSON_LEVEL2\tJSON_LEVEL3\tWarning\tterm_uri\tterm_label\tterm_score\tendpoint";
	public static final String[] header = header_string.split("\t");

	public enum hix {
		id {
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
		},
		term_uri, term_label, term_score, endpoint;
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

	public int getColumn() throws Exception {
		try {
			return (int) hix.Column.get(this);
		} catch (Exception x) {
			throw new Exception("Unknown column");
		}
	}
	public int getRow() throws Exception {
		try {
			return (int) hix.Row.get(this);
		} catch (Exception x) {
			throw new Exception("Unknown row");
		}
	}
	public String getValue() throws Exception {
		try {
			return hix.Value.get(this).toString();
		} catch (Exception x) {
			return null;
		}
	}	
	public void setAnnotation(String value) {
		hix.Annotation.set(this, value);
	}
	public String getAnnotation() {
		return hix.Annotation.get(this).toString();
	}	
	public void setJsonLevel1(String value) {
		hix.JSON_LEVEL1.set(this, value);
	}
	public void setJsonLevel2(String value) {
		hix.JSON_LEVEL2.set(this, value);
	}
	public void setJsonLevel3(String value) {
		hix.JSON_LEVEL3.set(this, value);
	}	
	public void setUnit(String value) {
		hix.unit.set(this, value);
	}
	public void setValueClean(String value) {
		hix.cleanedvalue.set(this, value);
	}
	public void setEndpoint(String value) {
		hix.endpoint.set(this, value);
	}
	
	public boolean isData() {
		return "data".equals(hix.Annotation.get(this));
	}
	public void write(Writer writer) throws Exception {
		for (hix h : hix.values()) {
			Object v = get(h.name());
			writer.write(v == null ? "" : (v instanceof String) ? ('"' + ((String) v) + '"') : v.toString());
			writer.write("\t");
		}
		writer.write("\n");
	}

	public static void writeHeader(XSSFSheet sheet) throws IllegalArgumentException {
		XSSFRow row = sheet.createRow(0);
		for (hix h : hix.values()) {
			Cell cell = row.createCell(h.ordinal());
			cell.setCellValue(h.name());
		}

	}

	public void write(XSSFSheet sheet, int rownum) throws IllegalArgumentException  {
		XSSFRow row = sheet.createRow(rownum);
		for (hix h : hix.values()) {
			Object v = get(h.name());
			if (v != null) {
				Cell cell = row.createCell(h.ordinal());
				if (v instanceof String)
					cell.setCellValue((String) v);
				else if (v instanceof Number)
					cell.setCellValue(((Number) v).doubleValue());
				else
					cell.setCellValue(v.toString());
			}
		}

	}

	public String serialize(Object value) {
		if (value != null) {
			if (value instanceof List) {
				StringBuilder b = new StringBuilder();
				b.append("[");
				String d = "";
				for (Object v : ((List) value)) {
					b.append(d);
					b.append(serialize(v));
					d = ",";
				}
				b.append("]");
				return b.toString();

			} else if (value instanceof TR) {
				return ((TR) value).toJSON();
			} else if (value instanceof Integer)
				return Integer.toString(((Number) value).intValue());
			else if (value instanceof Number)
				return String.format("%4.2f", ((Number) value).doubleValue());
			else if (!"".equals(value.toString()))
				return JSONUtils.jsonQuote(JSONUtils.jsonEscape(value.toString()));
		}
		return null;

	}

	public String toJSON() {
		StringBuilder b = new StringBuilder();
		String comma = "";
		b.append("\n{");
		for (String key : keySet()) {
			Object value = get(key);
			if (value != null) {
				b.append(String.format("\n\t%s\"%s\":\t%s", comma, key, serialize(value)));
				comma = ",";
			}
		}
		b.append("\n}");
		return b.toString();
	}
	
	public HashCode getHashCode(HashFunction hf) {
		return hf.newHasher().putString(String.format("%s-%s-%s",TR.hix.Folder.get(this).toString().toLowerCase(),TR.hix.File.get(this).toString().toLowerCase(),TR.hix.Sheet.get(this).toString().toLowerCase()), Charsets.UTF_8).hash();
	}
}
