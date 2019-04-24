package net.enanomapper.maker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Workbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class TemplateMakerSettings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7761828667318892718L;
	transient protected IAnnotator annotator;

	public IAnnotator getAnnotator() {
		return annotator;
	}

	public void setAnnotator(IAnnotator annotator) {
		this.annotator = annotator;
	}

	public enum _TEMPLATES_TYPE {
		jrc, iom, all, undefuned
	}

	public enum _TEMPLATES_CMD {
		extract, generate {
			@Override
			public boolean requiresInputFile() {
				return false;
			}
		},
		annotate, help {
			@Override
			public boolean requiresInputFile() {
				return false;
			}

			@Override
			public boolean requiresOutputFileFile() {
				return false;
			}
		};
		public boolean requiresInputFile() {
			return true;
		}

		public boolean requiresOutputFileFile() {
			return true;
		}
	}

	private _TEMPLATES_TYPE templatesType = _TEMPLATES_TYPE.jrc;
	private _TEMPLATES_CMD templatesCommand = _TEMPLATES_CMD.help;
	protected boolean singlefile = false;
	public boolean isSinglefile() {
		return singlefile;
	}

	public void setSinglefile(boolean singlefile) {
		this.singlefile = singlefile;
	}

	protected Map<String, String> query = new TreeMap<String, String>();

	public String getEndpointname() {
		return getQueryEndpoint();
	}

	public void setEndpointname(String value) {
		setQueryEndpoint(value);
	}

	public String getAssayname() {
		return getQueryAssay();
	}

	public void setAssayname(String value) {
		setQueryAssay(value);
	}

	public String getQueryEndpoint() {
		return query.get("endpoint");
	}

	public void setQueryEndpoint(String value) {
		query.put("endpoint", value);
	}

	public void setQueryAssay(String value) {
		query.put("s_uuid", value);
	}

	public String getQueryAssay() {
		return query.get("s_uuid");
	}

	public void setQueryTemplateid(String value) {
		query.put("id", value);
	}

	public String getQueryTemplateId() {
		return query.get("id");
	}

	public String getQueryFile() {
		return query.get("File");
	}

	public String getQuerySheet() {
		return query.get("Sheet");
	}

	public Map<String, String> getQuery() {
		return query;
	}

	public void setQuery(Map<String, String> query) {
		this.query = query;
	}

	public _TEMPLATES_CMD getTemplatesCommand() {
		return templatesCommand;
	}

	public void setTemplatesCommand(_TEMPLATES_CMD templatesCommand) {
		this.templatesCommand = templatesCommand;
	}

	public _TEMPLATES_TYPE getTemplatesType() {
		return templatesType;
	}

	public void setTemplatesType(_TEMPLATES_TYPE templatesType) {
		this.templatesType = templatesType;
	}

	File inputfolder;

	public File getInputfolder() {
		return inputfolder;
	}

	public void setInputfolder(File inputfolder) throws FileNotFoundException {
		if (inputfolder != null && inputfolder.exists())
			this.inputfolder = inputfolder;
		else if (getTemplatesCommand().requiresInputFile())
			throw new FileNotFoundException(
					inputfolder == null ? "Input file not specified, see option -i" : inputfolder.getAbsolutePath());
	}

	public File getOutputfolder() {
		return outputfolder;
	}

	public void setOutputfolder(File outputfolder) throws FileNotFoundException {

		if (outputfolder != null && outputfolder.exists())
			this.outputfolder = outputfolder;
		else if (getTemplatesCommand().requiresOutputFileFile())
			throw new FileNotFoundException(
					outputfolder == null ? "Output file not specified, see option -o" : outputfolder.getAbsolutePath());
	}

	File outputfolder;

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(getInputfolder());
		b.append("\n");
		b.append(getOutputfolder());
		return b.toString();
	}

	public Iterable<TR> getTemplateRecords() throws Exception {
		return getTemplateRecords("net/enanomapper/templates/JRCTEMPLATES_102016.json");
	}

	public Iterable<TR> getTemplateRecords(String config) throws Exception {
		try (InputStream in = TemplateMaker.class.getClassLoader().getResourceAsStream(config)) {
			return getTemplateRecords(in);
		} catch (Exception x) {
			throw x;
		}
	}


	public HashSet<String> getUniqueTemplateID(Iterable<TR> records) throws Exception {
		HashSet<String> templateid = new HashSet<String>();
		for (TR record : records)
			try {
				Object id = record.get("id");
				if (id == null)
					continue;
				else if (!templateid.contains(id.toString()))
					templateid.add(id.toString());
			} catch (Exception x) {
				throw x;
			}
		return templateid;
	}
	
	public boolean filterRecord(TR record) {
		if (query == null || query.size() == 0)
			return true;
		Iterator<Entry<String, String>> q = query.entrySet().iterator();
		boolean ok = true;
		while (q.hasNext()) {
			Entry<String, String> e = q.next();
			ok = ok && (e.getValue().equals(record.get(e.getKey())));
			if (!ok)
				break;
		}
		return ok;
	}

	public Iterable<TR> getTemplateRecords(InputStream in) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = null;
		List<TR> records = new ArrayList<TR>();
		try {
			root = mapper.readTree(in);
			if (root instanceof ArrayNode) {
				ArrayNode aNode = (ArrayNode) root;
				for (int i = 0; i < aNode.size(); i++) {
					TR record = new TR();
					JsonNode node = aNode.get(i);
					Iterator<String> fields = node.fieldNames();
					while (fields.hasNext()) {
						String field = fields.next();
						record.put(field, node.get(field).asText());
					}
					if (filterRecord(record))
						records.add(record);

				}
			}
		} catch (Exception x) {
			throw x;
		} finally {

		}
		return records;
	}

	public File getOutputFile(String templateid,_TEMPLATES_TYPE ttype) throws IOException {
		return new File(getOutputfolder(),String.format("%s_%s.xlsx", templateid, _TEMPLATES_TYPE.jrc.equals(ttype)?"COLUMNS":"BLOCKS"));
	}

	public File write(String templateid, _TEMPLATES_TYPE ttype, Workbook workbook) throws IOException {
		File outfile = getOutputFile(templateid,ttype);
		try (FileOutputStream out = new FileOutputStream(outfile)) {
			workbook.write(out);
			return outfile;
		} finally {
			workbook.close();
		}
	}
}
