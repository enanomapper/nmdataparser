package net.enanomapper.parser.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.idea.modbcum.i.json.JSONUtils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

public class CSV2TreeTest {
	static Logger logger = Logger.getLogger(CSV2TreeTest.class.getName());
	int maxlevel = Integer.MAX_VALUE;

	@Test
	public void test() throws Exception {
		BufferedWriter out = null;
		Reader in = new InputStreamReader(getClass().getClassLoader()
				.getResourceAsStream(
						"net/enanomapper/tree/protein_classification.txt"));
		CSVFormat f = CSVFormat.TDF.withHeader("protein_class_id", "parent_id",
				"pref_name", "short_name", "protein_class_desc",
				"definition	class_level");
		List<CSVRecord> lookup = new ArrayList<CSVRecord>();
		CSVRecord root = null;
		try {
			CSVParser p = f.parse(in);
			Iterator<CSVRecord> i = p.iterator();
			while (i.hasNext()) {
				CSVRecord record = i.next();
				lookup.add(record);
				if ("".equals(record.get("parent_id").trim()))
					root = record;
			}
			if (root != null) {
				String outname = "protein_classification.json";
				File baseDir = new File(System.getProperty("java.io.tmpdir"));
				logger.log(Level.FINE, baseDir.getAbsolutePath());
				out = new BufferedWriter(new FileWriter(new File(baseDir,
						outname)));
				traverse(root, lookup, 0, out);
			}
		} finally {
			in.close();
			if (out != null)
				out.close();
		}
	}

	public int traverse(CSVRecord node, List<CSVRecord> jmodel, int level,
			Writer out) throws IOException {
		if (level > maxlevel)
			return 0;

		String id = node.get("protein_class_id");
		String name = node.get("pref_name");
		String shortname = node.get("short_name").replace("_", "");

		out.write("{");
		out.write("\n\"name\":");
		out.write(JSONUtils.jsonQuote(JSONUtils
				.jsonEscape(level > 2 ? shortname : name)));
		out.write(",\n\"id\":");
		out.write(JSONUtils.jsonQuote(JSONUtils.jsonEscape(shortname)));
		out.write(",\n\"description\":");
		out.write(JSONUtils.jsonQuote(JSONUtils.jsonEscape(node
				.get("protein_class_desc"))));

		int count = 0;
		int size = 0;
		for (CSVRecord record : jmodel)
			if (record.get("parent_id").equals(id)) {
				if (count == 0)
					out.write(",\n\"children\": [\n");
				else
					out.write(",");
				size += traverse(record, jmodel, (level + 1), out);
				count++;
			}
		if (count > 0)
			out.write("\n]");
		out.write(",");
		out.write(String.format("\n\"size\":%d", size + 1));
		out.write("\n}");
		out.flush();
		return size + 1;
	}
}
