package net.enanomapper.templates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

import ambit2.base.io.DownloadTool;
import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;

public class ToolsRDF {
	public static Properties initJRCTemplateNames() throws IOException {
		Properties templates = new Properties();
		InputStream in = ToolsRDF.class.getClassLoader().getResourceAsStream("data/xlsx/nanoreg/nrtemplates.properties");
		try {
			templates.load(in);
		} finally {
			in.close();
		}
		return templates;
	}

	public static void smash(String rdfurl, String title, boolean splitfirstlevel, IProcessRDF processor)
			throws Exception {
		smash(rdfurl, title, splitfirstlevel, processor, "http://www.w3.org/2002/07/owl#Thing", "RDF/XML");
	}

	public static void smash(String rdfurl, String title, boolean splitfirstlevel, IProcessRDF processor,
			String rootResource, String format) throws Exception {
		ToolsRDF.smash(rdfurl, title, splitfirstlevel, processor, rootResource, format, null);
	}

	public static void smash(String rdfurl, String title, boolean splitfirstlevel, IProcessRDF processor,
			String rootResource, String format, String propertyURI) throws Exception {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		System.out.println("Downloading " + rdfurl);

		String ext = ".rdf";
		if (format.toUpperCase().equals("TURTLE"))
			ext = ".ttl";
		File file = getTestFile(rdfurl, title, ext, baseDir);
		System.out.println("Download completed " + file.getAbsolutePath());
		Model jmodel = ModelFactory.createDefaultModel();

		if (propertyURI != null) {
			processor.setHproperty(jmodel.createProperty(propertyURI));
		}
		InputStreamReader in = null;
		try {
			RDFReader reader = jmodel.getReader(format);

			if (rdfurl.endsWith(".gz"))
				in = new InputStreamReader(new GZIPInputStream(new FileInputStream(file)));
			else
				in = new InputStreamReader(new FileInputStream(file));
			reader.read(jmodel, in, format);
			System.out.println("Reading completed " + file.getAbsolutePath());
			Resource root = jmodel.createResource(rootResource);

			if (!splitfirstlevel) {
				String outname = String.format("%s_tree_%s.%s", title, root.getLocalName(),
						processor.getFileExtension());
				BufferedWriter out = new BufferedWriter(new FileWriter(new File(baseDir, outname)));
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
				ResIterator thingi = jmodel.listSubjectsWithProperty(RDFS.subClassOf, root);
				while (thingi.hasNext()) {
					Resource thing = thingi.next();
					BufferedWriter out = null;

					ResIterator entityi = jmodel.listSubjectsWithProperty(RDFS.subClassOf, thing);

					while (entityi.hasNext())
						try {
							Resource entity = entityi.next();
							String outname = String.format("%s_tree_%s.%s", title, entity.getLocalName(),
									processor.getFileExtension());
							out = new BufferedWriter(new FileWriter(new File(baseDir, outname)));
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

	public static File getTestFile(String remoteurl, String localname, String extension, File baseDir)
			throws Exception {
		URL url = new URL(remoteurl);
		boolean gz = remoteurl.endsWith(".gz");
		File file = new File(baseDir, localname + extension + (gz ? ".gz" : ""));
		if (!file.exists())
			DownloadTool.download(url, file);
		return file;
	}
}
