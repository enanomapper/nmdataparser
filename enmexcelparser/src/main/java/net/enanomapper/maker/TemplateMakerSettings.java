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
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class TemplateMakerSettings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7761828667318892718L;

	public enum _LAYOUT_RAW_DATA {
		x_replicate_y_experiment {
			public int get_number_y_axis(TemplateMakerSettings settings) {
				return settings.getNumber_of_experiments();
			}

			public int get_number_x_axis(TemplateMakerSettings settings) {
				return settings.getNumber_of_replicates();
			}
			public String get_label_x_axis() {
				return "Test";
			}
			public String get_label_y_axis() {
				return "Replicate";
			}			

		},
		x_experiment_y_replicate;

		public int get_number_x_axis(TemplateMakerSettings settings) {
			return settings.getNumber_of_experiments();
		}

		public int get_number_y_axis(TemplateMakerSettings settings) {
			return settings.getNumber_of_replicates();
		}
		
		public String get_label_x_axis() {
			return "Replicate";
		}
		public String get_label_y_axis() {
			return "Experiment";
		}
		public int get_x_space() {
			return 2;
		}
		public int get_y_space() {
			return 2;
		}		
	}

	//protected _LAYOUT_RAW_DATA layout_raw_data = _LAYOUT_RAW_DATA.x_replicate_y_experiment;
	protected _LAYOUT_RAW_DATA layout_raw_data = _LAYOUT_RAW_DATA.x_experiment_y_replicate;
	public _LAYOUT_RAW_DATA getLayout_raw_data() {
		return layout_raw_data;
	}

	public void setLayout_raw_data(_LAYOUT_RAW_DATA layout_raw_data) {
		this.layout_raw_data = layout_raw_data;
	}

	transient protected IAnnotator annotator;
	protected int number_of_experiments = 4;
	protected int number_of_endpoints = 1;

	public int getNumber_of_endpoints() {
		return number_of_endpoints;
	}

	public void setNumber_of_endpoints(int number_of_endpoints) {
		this.number_of_endpoints = number_of_endpoints;
	}

	public int getNumber_of_experiments() {
		return number_of_experiments;
	}

	public void setNumber_of_experiments(int number_of_experiments) {
		this.number_of_experiments = number_of_experiments;
	}

	protected int number_of_replicates = 6;
	protected int number_of_timepoints = 1;
	protected int number_of_concentration = 11;

	public IAnnotator getAnnotator() {
		return annotator;
	}

	public void setAnnotator(IAnnotator annotator) {
		this.annotator = annotator;
	}

	public enum _TEMPLATES_TYPE {
		jrc, multisheet, all, undefuned
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
	protected boolean generatehash = false;

	public boolean isGeneratehash() {
		return generatehash;
	}

	public void setGeneratehash(boolean generatehash) {
		this.generatehash = generatehash;
	}

	public boolean isSinglefile() {
		return singlefile;
	}

	public void setSinglefile(boolean singlefile) {
		this.singlefile = singlefile;
	}

	protected Map<String, String> query = new TreeMap<String, String>();

	public int getNumber_of_replicates() {
		return number_of_replicates;
	}

	public void setNumber_of_replicates(int number_of_replicates) {
		this.number_of_replicates = number_of_replicates;
	}

	public int getNumber_of_timepoints() {
		return number_of_timepoints;
	}

	public void setNumber_of_timepoints(int number_of_timepoints) {
		this.number_of_timepoints = number_of_timepoints;
	}

	public int getNumber_of_concentration() {
		return number_of_concentration;
	}

	public void setNumber_of_concentration(int number_of_concentration) {
		this.number_of_concentration = number_of_concentration;
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

	public Iterable<TR> getTemplateRecords(Iterator<TR> iterator) {
		return new Iterable<TR>() {
			@Override
			public Iterator<TR> iterator() {
				return iterator;
			}
		};
	}

	public Iterable<TR> getTemplateRecords(InputStream in) throws Exception {
		HashFunction hf = null;
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
						if (node.get(field).isInt())
							record.put(field, node.get(field).intValue());
						else
							record.put(field, node.get(field).asText());
					}
					if (generatehash) {
						if (hf == null)
							hf = Hashing.murmur3_32();
						TR.hix.id.set(record, record.getHashCode(hf));
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

	public String getOutputFileName() {
		if (getQueryTemplateId() != null)
			return String.format("%s_%s.xlsx", getQueryTemplateId(),
					_TEMPLATES_TYPE.jrc.equals(getTemplatesType()) ? "COLUMNS" : "BLOCKS");
		else
			return String.format("%s_%s.xlsx", "datatemplate",
					_TEMPLATES_TYPE.jrc.equals(getTemplatesType()) ? "COLUMNS" : "BLOCKS");
	}

	public File getOutputFile(String templateid, _TEMPLATES_TYPE ttype) throws IOException {
		return new File(getOutputfolder(),
				String.format("%s_%s.xlsx", templateid, _TEMPLATES_TYPE.jrc.equals(ttype) ? "COLUMNS" : "BLOCKS"));
	}

	public File write(String templateid, _TEMPLATES_TYPE ttype, Workbook workbook) throws IOException {
		File outfile = getOutputFile(templateid, ttype);
		try (FileOutputStream out = new FileOutputStream(outfile)) {
			workbook.write(out);
			return outfile;
		} finally {
			workbook.close();
		}
	}
	public int get_number_y_axis() {
		return getLayout_raw_data().get_number_x_axis(this);
	}

	public int get_number_x_axis() {
		return getLayout_raw_data().get_number_y_axis(this);
	}
}
