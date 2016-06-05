package net.enanomapper.parser.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import net.idea.modbcum.i.json.JSONUtils;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class RDFsmasher extends TestWithExternalFiles {
	int maxlevel = Integer.MAX_VALUE;

	@Test
	public void testGO() throws Exception {
		smash("http://data.bioontology.org/ontologies/NCIT/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"GO");
	}

	@Test
	public void testBAO() throws Exception {
		smash("http://data.bioontology.org/ontologies/BAO/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"BAO");
	}

	@Test
	public void testENM() throws Exception {
		smash("http://data.bioontology.org/ontologies/ENM/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"ENM");
	}

	@Test
	public void testCLO() throws Exception {
		smash("http://data.bioontology.org/ontologies/CLO/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"CLO");
	}

	public void smash(String rdfurl, String title) throws Exception {
		smash(rdfurl, title, true);
	}

	public void smash(String rdfurl, String title, boolean splitfirstlevel)
			throws Exception {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		File file = getTestFile(rdfurl, title ,".rdf", baseDir);
		Assert.assertTrue(file.exists());
		Model jmodel = ModelFactory.createDefaultModel();
		FileInputStream in = null;

		try {
			RDFReader reader = jmodel.getReader();
			in = new FileInputStream(file);
			reader.read(jmodel, in, "RDF/XML");
			System.out.println("Reading completed " + file.getAbsolutePath());
			Resource root = jmodel
					.createResource("http://www.w3.org/2002/07/owl#Thing");
			// final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

			int c = 1;
			ResIterator thingi = jmodel.listSubjectsWithProperty(
					RDFS.subClassOf, root);
			while (thingi.hasNext()) {
				Resource thing = thingi.next();
				ResIterator entityi = jmodel.listSubjectsWithProperty(
						RDFS.subClassOf, thing);
				BufferedWriter out = null;
				while (entityi.hasNext())
					try {
						Resource entity = entityi.next();
						String outname = String.format("%s_tree_%s.json",
								title, entity.getLocalName());
						out = new BufferedWriter(new FileWriter(new File(
								baseDir, outname)));
						System.out.println("Writing tree into " + outname);
						traverse(entity, jmodel, 0, out);
					} finally {
						try {
							out.close();
						} catch (Exception x) {
						}
					}
				c++;
			}

		} finally {
			jmodel.close();
			try {
				if (in != null)
					in.close();
			} catch (Exception x) {
			}

		}
	}

	/**
	 * generate tree json
	 * https://github.com/ideaconsult/Toxtree.js/blob/facet_kit/flare.json
	 * 
	 * @param root
	 * @param jmodel
	 * @param level
	 */
	protected void traverse(Resource root, Model jmodel, int level, Writer out)
			throws IOException {
		if (level > maxlevel)
			return;
		NodeIterator n = jmodel.listObjectsOfProperty(root, RDFS.label);
		StringBuilder label = new StringBuilder();
		while (n.hasNext()) {
			RDFNode node = n.next();
			label.append(node.asLiteral().getString());
		}
		ResIterator i = jmodel.listSubjectsWithProperty(RDFS.subClassOf, root);
		out.write("{");
		out.write("\n\"name\":");
		out.write(JSONUtils.jsonQuote(JSONUtils.jsonEscape(label.toString())));
		out.write(",\n\"id\":");
		out.write(JSONUtils.jsonQuote(JSONUtils.jsonEscape(root.getLocalName())));
		out.write(",\n\"size\":1");

		int count = 0;
		while (i.hasNext()) {
			if (count == 0)
				out.write(",\n\"children\": [\n");
			else
				out.write(",");
			Resource res = i.next();
			traverse(res, jmodel, (level + 1), out);
			count++;
		}
		if (count > 0)
			out.write("\n]");
		out.write("\n}");
		out.flush();
	}
}
