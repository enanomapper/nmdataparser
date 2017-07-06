package net.enanomapper.parser.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.idea.modbcum.i.json.JSONUtils;

public class XMLParserTest {
	int maxlevel = 0;

	@Test
	public void testDOM() throws Exception {
		try (InputStream in = getClass().getClassLoader()
				.getResourceAsStream("net/enanomapper/tree/det_model_morgan_32_24.xml")) {
			parseDOM(in, new File("D:/src-ideaconsult/Toxtree.js-ivan"));
		}
	}

	public void parseDOM(InputStream in, File outDir) throws Exception {
		System.out.println(outDir);

		String outname = "det_model.json";
		try (BufferedWriter out = new BufferedWriter(new FileWriter(new File(outDir, outname)));) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			NodeList det_model = doc.getDocumentElement().getElementsByTagName("det_model");
			traverse(det_model.item(0), 0, "", out);
			System.out.println(maxlevel);
		}
	}

	private int traverse(Node node, int level, String lr, Writer out) throws IOException {
		if (level > maxlevel)
			maxlevel = level;

		String id = Integer.toString(level);
		try {
			id = node.getAttributes().getNamedItem("object_id").getNodeValue();
		} catch (Exception x) {
		}
		StringBuilder name = new StringBuilder();
		// name.append(id);

		NodeList children = node.getChildNodes();
		if (children.getLength() == 0)
			return 0;

		int count = 0;
		int size = 0;
		String splitdim = "";
		String splitValue = "";
		Node left = null;
		Node right = null;
		String start = null;
		String end = null;
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);
			if ("splitDim".equals((c.getNodeName()))) {
				splitdim = c.getFirstChild().getNodeValue();
			} else if ("splitValue".equals((c.getNodeName()))) {
				splitValue = c.getFirstChild().getNodeValue();
			} else if ("start".equals((c.getNodeName()))) {
				start = c.getFirstChild().getNodeValue();
			} else if ("end".equals((c.getNodeName()))) {
				end = c.getFirstChild().getNodeValue();
			} else if ("left".equals(c.getNodeName()))
				left = c;
			else if ("right".equals(c.getNodeName()))
				right = c;
		}

		out.write("{");
		out.write("\n\"id\":");
		out.write(JSONUtils.jsonQuote(JSONUtils.jsonEscape(id)));
		// name.append(id);
		if (!splitdim.equals("18446744073709551615")) {
			// name.append(" X");
			// name.append(splitdim);
			// name.append(lr);

			try {
				// name.append(String.format("%s%2.0e", lr.equals("R") ? ">" :
				// "<", Double.parseDouble(splitValue)));
				name.append(String.format("%s%s", lr, splitdim));
				// name.append(splitValue);
			} catch (Exception x) {
				x.printStackTrace();
			}
		} else {
			name.append(lr);
		}

		// name.append("@");
		// name.append(level);
		out.write(",");
		out.write("\n\"name\":");
		out.write(JSONUtils.jsonQuote(JSONUtils.jsonEscape(name.toString())));

		if (left != null || right != null) {
			out.write(",\n\"children\": [\n");
			size += traverse(left, (level + 1), "L", out);
			if (size > 0)
				out.write(",");
			size += traverse(right, (level + 1), "R", out);
			out.write("\n]");
		}

		out.write(",");

		int s = size + 1;
		try {
			s = Integer.parseInt(end) - Integer.parseInt(start);
		} catch (Exception x) {

		}
		out.write(String.format("\n\"size\":%d", s));
		out.write("\n}");
		out.flush();
		return s;
	}

	private String apply(Node node, int level, double[] point) throws IOException {
		if (level > maxlevel)
			maxlevel = level;

		NodeList children = node.getChildNodes();
		if (children.getLength() == 0)
			return "";

		int count = 0;
		int size = 0;
		String splitdim = "";
		String splitValue = "";
		Node left = null;
		Node right = null;
		String start = null;
		String end = null;
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);
			if ("splitDim".equals((c.getNodeName()))) {
				splitdim = c.getFirstChild().getNodeValue();
			} else if ("splitValue".equals((c.getNodeName()))) {
				splitValue = c.getFirstChild().getNodeValue();
			} else if ("start".equals((c.getNodeName()))) {
				start = c.getFirstChild().getNodeValue();
			} else if ("end".equals((c.getNodeName()))) {
				end = c.getFirstChild().getNodeValue();
			} else if ("left".equals(c.getNodeName()))
				left = c;
			else if ("right".equals(c.getNodeName()))
				right = c;
		}
		if (!splitdim.equals("18446744073709551615")) {
			int idx = Integer.parseInt(splitdim);
			Double t = Double.parseDouble(splitValue);
			// System.out.println(String.format("%s\t%s\t%s", idx, t,
			// point[idx]));
			if (point[idx] > t)
				return String.format("%4s%s%s", splitdim, "R", apply(right, level + 1, point));
			else
				return String.format("%4s%s%s", splitdim, "L", apply(left, level + 1, point));
		} else
			return "";

	}

	public Node parse(InputStream in) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(in);
		return doc.getDocumentElement().getElementsByTagName("det_model").item(0);
	}

	public static void main(String[] args) {
		XMLParserTest t = new XMLParserTest();
		Node root = null;
		try (FileInputStream in = new FileInputStream(new File(args[0]))) {
			root = t.parse(in);
		} catch (Exception x) {
			x.printStackTrace();
		}
		double[] point = new double[280];
		try {
			for (int i = 0; i < 280; i++)
				point[i] = 0;
			System.out.println(t.apply(root, 0, point));
			for (int i = 0; i < 280; i++)
				point[i] = 1;
			System.out.println(t.apply(root, 0, point));
			for (int i = 0; i < 280; i++)
				point[i] = (i > 140) ? 0 : 1;
			System.out.println(t.apply(root, 0, point));
			for (int i = 0; i < 280; i++)
				point[i] = (i < 140) ? 0 : 1;
			System.out.println(t.apply(root, 0, point));
			for (int i = 0; i < 280; i++)
				point[i] = 0.1;
			System.out.println(t.apply(root, 0, point));
			for (int i = 0; i < 280; i++)
				point[i] = (i < 140) ? 0 : Math.random();
			System.out.println(t.apply(root, 0, point));
		} catch (Exception x) {

		}
		CSVFormat format = CSVFormat.DEFAULT.withDelimiter('\t').withSkipHeaderRecord(false);
		File outDir = new File(System.getProperty("java.io.tmpdir"));

		try (FileReader in = new FileReader(new File(args[1]))) {
			try (BufferedWriter w = new BufferedWriter(new FileWriter(new File(outDir, "det.csv")))) {
				CSVParser parser = format.withHeader().parse(in);
				Iterator<CSVRecord> iterator = parser.iterator();
				int r = 0;
				while (iterator.hasNext()) {
					CSVRecord record = iterator.next();
					for (int i = 0; i < point.length; i++)
						point[i] = Double.parseDouble(record.get(i+1));
					String branch = t.apply(root, 0, point);
					/*
					for (int i = 0; i <= point.length; i++) {
						w.write(record.get(i));
						w.write(",");
					}
					*/
					w.write(record.get(0));
					w.write(",");
					w.write(branch);
					w.write("\n");
					r++;
				}
			}
		} catch (Exception x) {
			x.printStackTrace();
		}

		/*
		 * File outDir = // new File(System.getProperty("java.io.tmpdir")); new
		 * File("D:/src-ideaconsult/Toxtree.js-ivan"); try (FileInputStream in =
		 * new FileInputStream(new File(args[0]))) { t.parseDOM(in, outDir); }
		 * catch (Exception x) { x.printStackTrace(); }
		 */
	}

	@Test
	public void testsplit() throws Exception {
		CSVFormat format = CSVFormat.EXCEL.withDelimiter(',').withHeader();
		//String sorted = "F:/Downloads/Chemical data/Ames_HONMA/ames/step-03/train_concatpaths_vecmol40_binary_std_detclass.csv";
		String sorted = "F:/Downloads/Chemical data/Ames_HONMA/ames/step-02/det/l3/detclass.csv";
		File outDir = new File(System.getProperty("java.io.tmpdir"));
		try (FileReader in = new FileReader(new File(sorted))) {
			try (BufferedWriter w = new BufferedWriter(new FileWriter(new File(outDir, "split.csv")))) {
				CSVParser parser = format.parse(in);
				
				String[] header = format.getHeader();

				String tag = null;

				Iterator<CSVRecord> iterator = parser.iterator();
				int r = 0;
				int a = 0;
				int na = 0;
				int c  = 0;
				while (iterator.hasNext()) {
					CSVRecord record = iterator.next();

					if (!record.get("det").equals(tag)) {
						tag = record.get("det");
						a = 0;
						na = 0;
						c++;
					}

					boolean write = "1".equals(record.get("Activity"));

					if (write || (na < a) || (na==0)) {
						
						Iterator<String> i = record.iterator();
						while (i.hasNext()) {
							w.write(i.next());
							w.write(",");
						}
						w.write(Integer.toString(c));
						w.write("\n");
					}
					
					if (write) a++; else na++;
					r++;

				}
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
}
