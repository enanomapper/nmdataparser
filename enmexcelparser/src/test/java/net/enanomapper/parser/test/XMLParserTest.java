package net.enanomapper.parser.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.idea.modbcum.i.json.JSONUtils;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLParserTest {
	int maxlevel = 0;

	@Test
	public void testDOM() throws Exception {
		BufferedWriter out = null;
		InputStream in = getClass().getClassLoader().getResourceAsStream(
				"net/enanomapper/tree/det_model_morgan_32_24.xml");
		try {
			String outname = "det_model.json";
			File baseDir = new File(System.getProperty("java.io.tmpdir"));
			baseDir = new File("D:/src-ideaconsult/Toxtree.js-ivan");
			System.out.println(baseDir);
			out = new BufferedWriter(new FileWriter(new File(baseDir, outname)));

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			NodeList det_model = doc.getDocumentElement().getElementsByTagName(
					"det_model");
			traverse(det_model.item(0), 0, "", out);
			System.out.println(maxlevel);
		} finally {
			in.close();
			if (out != null)
				out.close();
		}
	}

	private int traverse(Node node, int level, String lr, Writer out)
			throws IOException {
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
		name.append(id);
		if (!splitdim.equals("18446744073709551615")) {
			name.append(" X");
			name.append(splitdim);
			// name.append(lr);

			try {
				name.append(String.format("%s%2.0e",
						lr.equals("R")?">":"<",Double.parseDouble(splitValue)));
				// name.append(splitValue);
			} catch (Exception x) {
				x.printStackTrace();
			}
		} else {
			name.append(lr);
		}

		name.append("@");
		name.append(level);
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
}
