package net.enanomapper.parser.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;

import org.junit.Assert;

import ambit2.base.io.DownloadTool;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestWithExternalFiles implements IProcessRDF {
	protected ExtractSynonymsList syn;

	protected File getTestFile(String remoteurl, String localname,
			String extension, File baseDir) throws Exception {
		URL url = new URL(remoteurl);
		File file = new File(baseDir, localname + extension);
		if (!file.exists())
			DownloadTool.download(url, file);
		return file;
	}

	public void smash(String rdfurl, String title) throws Exception {
		smash(rdfurl, title, true, this, "http://www.w3.org/2002/07/owl#Thing");
	}

	public void smash(String rdfurl, String title, boolean splitfirstlevel,
			IProcessRDF processor) throws Exception {
		smash(rdfurl, title, splitfirstlevel, processor,
				"http://www.w3.org/2002/07/owl#Thing");
	}

	public void smash(String rdfurl, String title, boolean splitfirstlevel,
			IProcessRDF processor, String rootResource) throws Exception {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		File file = getTestFile(rdfurl, title, ".rdf", baseDir);
		Assert.assertTrue(file.exists());
		Model jmodel = ModelFactory.createDefaultModel();
		FileInputStream in = null;

		try {
			RDFReader reader = jmodel.getReader();
			in = new FileInputStream(file);
			reader.read(jmodel, in, "RDF/XML");
			System.out.println("Reading completed " + file.getAbsolutePath());
			Resource root = jmodel.createResource(rootResource);

			if (!splitfirstlevel) {
				String outname = String.format("%s_tree_%s.%s", title,
						root.getLocalName(), processor.getFileExtension());
				BufferedWriter out = new BufferedWriter(new FileWriter(
						new File(baseDir, outname)));
				System.out.println("Writing tree into " + outname);
				try {
					processor.traverse(root, jmodel, 0, out);
				} finally {
					try {
						out.close();
					} catch (Exception x) {
					}
				}

			} else {
				int c = 1;
				ResIterator thingi = jmodel.listSubjectsWithProperty(
						RDFS.subClassOf, root);
				while (thingi.hasNext()) {
					Resource thing = thingi.next();
					BufferedWriter out = null;

					ResIterator entityi = jmodel.listSubjectsWithProperty(
							RDFS.subClassOf, thing);

					while (entityi.hasNext())
						try {
							Resource entity = entityi.next();
							String outname = String.format("%s_tree_%s.%s",
									title, entity.getLocalName(),
									processor.getFileExtension());
							out = new BufferedWriter(new FileWriter(new File(
									baseDir, outname)));
							System.out.println("Writing tree into " + outname);
							processor.traverse(entity, jmodel, 0, out);
						} finally {
							try {
								out.close();
							} catch (Exception x) {
							}
						}

					c++;
				}
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

	@Override
	public String getFileExtension() {
		if (syn == null)
			syn = new ExtractSynonymsList();
		return syn.getFileExtension();
	}

	@Override
	public int traverse(Resource root, Model jmodel, int level, Writer out)
			throws IOException {
		if (syn == null)
			syn = new ExtractSynonymsList();
		return syn.traverse(root, jmodel, level, out);

	}

	@Override
	public void processEntry(String entity, String label) {

	}

}
