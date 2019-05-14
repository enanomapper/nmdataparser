package net.enanomapper.templates.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;

import ambit2.base.io.DownloadTool;
import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;

public class AnnotationToolsOntology extends AnnotationToolsAbstract {
	protected URL remoteOntology;
	protected String ontologyResource;
	protected String localOntologyName;
	protected  String label_tag;
	public String getLabel_tag() {
		return label_tag;
	}

	public void setLabel_tag(String label_tag) {
		this.label_tag = label_tag;
	}

	public AnnotationToolsOntology() {
		this((URL) null, null, "enm","label",1);
	}

	public AnnotationToolsOntology(URL remoteOntology, String localOntologyName, String term_tag, String label_tag, int nhits) {
		super(remoteOntology == null ? ".zenmonto" : ".z" + localOntologyName);
		this.remoteOntology = remoteOntology;
		this.localOntologyName = localOntologyName;
		if (term_tag == null) {
			setTerm_tag("term");
		} else {
			setTerm_tag(term_tag);
		}
		setLabel_tag(label_tag);
		setNumberofhits(nhits);
	}

	public AnnotationToolsOntology(String ontologyResource, String localOntologyName, String term_tag, String label_tag, int nhits) {
		super(".z" + localOntologyName);
		this.ontologyResource = ontologyResource;
		this.localOntologyName = localOntologyName;
		if (term_tag == null) {
			setTerm_tag("term");
		} else {
			setTerm_tag(term_tag);
		}
		setLabel_tag(label_tag);
		setNumberofhits(nhits);
	}

	@Override
	protected File getFile() throws Exception {
		if (remoteOntology!=null) 
			return getCachedFile(remoteOntology, localOntologyName, new File(System.getProperty("java.io.tmpdir")));
		else if (ontologyResource != null) {
			File tmpfile = new File(System.getProperty("java.io.tmpdir"), localOntologyName);
			DownloadTool.download(ontologyResource, tmpfile);
			return tmpfile;
		} else {
			File tmpfile = new File(System.getProperty("java.io.tmpdir"), "merged_enm.rdf.gz");
			DownloadTool.download("net/idea/ontology/merged_enm.rdf.gz", tmpfile);
			return tmpfile;
		}
	}

	@Override
	protected IndexWriter process(IndexWriter writer, File tmpfile) throws Exception {
		smashRDF(writer, tmpfile, true, pathIndex);
		writer.flush();
		writer.commit();
		return writer;
	}

	protected String printNode(RDFNode node) {
		return node == null ? null
				: node.isLiteral() ? (node.asLiteral().getString())
						: (node.isResource() ? (node.asResource().getLocalName()) : node.getClass().getName());
	}

	public void smashRDF(IndexWriter writer, File file, boolean zipped, Path indexed) throws Exception {

		Model jmodel = ModelFactory.createDefaultModel();

		InputStreamReader in = null;
		try {
			RDFReader reader = jmodel.getReader("RDF/XML");

			if (file.getName().endsWith(".gz")) {

				in = new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "UTF-8");
			} else
				in = new InputStreamReader(new FileInputStream(file), "UTF-8");

			reader.read(jmodel, in, "RDF/XML");
			System.out.println("Reading completed " + file.getAbsolutePath());

			String sparql = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX npoext:<http://purl.enanomapper.org/onto/internal/npo-ext.owl#> SELECT distinct ?s ?p (group_concat(distinct ?l;separator=';') as ?pl) (group_concat(distinct ?sn;separator=';') as ?ps) ?o  where {?s ?p ?o. ?s a owl:Class. ?p a owl:AnnotationProperty. OPTIONAL {?p rdfs:label ?l.}. OPTIONAL {?p npoext:synonym ?sn.}. }  group by ?s ?p ?o order by ?s";

			Query qry = QueryFactory.create(sparql);
			QueryExecution qe = QueryExecutionFactory.create(qry, jmodel);
			ResultSet rs = qe.execSelect();

			String s_prev = null;
			Document doc = null;
			BufferedWriter w = new BufferedWriter(new FileWriter(new File("test.txt")));
			StringBuilder _catchall = new StringBuilder();
			while (rs.hasNext()) {
				QuerySolution sol = rs.nextSolution();
				String s = sol.get("s").asResource().getLocalName();
				String s_uri = sol.get("s").asResource().getURI();
				String p = printNode(sol.get("p"));
				String o = printNode(sol.get("o"));
				if (o==null) continue;
				if (o.endsWith(".owl")) continue;
				//labels
				StringBuilder pb = null;
				try {
					String[] pls = sol.get("pl").asLiteral().getString().split(";");
					if (pls.length == 1) {
						pb = new StringBuilder();
						pb.append(pls[0]);
					} else {
						Arrays.sort(pls);
						String l1 = null;
						for (String l : pls) {
							if (!l.equals(l1)) {
								if (pb == null)
									pb = new StringBuilder();
								pb.append(l);
								pb.append(" ");
								l1 = l;
							}
						}
					}
				} catch (Exception x) {
					pb = null;
				}
				//synonyms
				try {
					String[] pls = sol.get("ps").asLiteral().getString().split(";");
					if (pls.length == 1) {
						if (pb==null) pb = new StringBuilder();
						pb.append(pls[0]);
					} else {
						Arrays.sort(pls);
						String l1 = null;
						for (String l : pls) {
							if (!l.equals(l1)) {
								if (pb == null)
									pb = new StringBuilder();
								pb.append(l);
								pb.append(" ");
								l1 = l;
							}
						}
					}
				} catch (Exception x) {
					//pb = null;
				}	
				
				//
				String pl = pb == null ? null : pb.toString();
				w.write(String.format("%s\t%s\t%s\t'%s'\t%s\n", s, s_uri, p, pl, o));
				if (!s.equals(s_prev)) {
					if (doc != null) {
						TextField tf = new TextField("_text", _catchall.toString(), Store.NO);
						doc.add(tf);
						writer.addDocument(doc);
						_catchall = new StringBuilder();

					}
					s_prev = s;
					doc = new Document();
					Field tf = new TextField("subject", s, Store.YES);
					doc.add(tf);
					tf = new TextField("subject_uri", s_uri, Store.YES);
					doc.add(tf);
					_catchall.append(s);
					_catchall.append(" ");
				}
				try {
					Field tf = new TextField(pl == null ? p : (p.equals(pl) ? p : pl), o, Store.YES);
					doc.add(tf);
				} catch (Exception x) {
					x.printStackTrace();
				}

				_catchall.append(o);
				_catchall.append(" ");

			}

			TextField tf = new TextField("_text", _catchall.toString(), Store.NO);
			doc.add(tf);
			writer.addDocument(doc);

			qe.close();

			System.out.println("Indexing completed " + indexed.toString());
			w.close();

			// Resource root = jmodel.createResource(rootResource);

			try {
				// traverse(root, jmodel, 0, null);
			} finally {
				try {
					// out.close();
				} catch (Exception x) {
				}
			}
		} catch (Exception x) {
			x.printStackTrace();
		} finally {
			jmodel.close();
			try {
				if (in != null)
					in.close();
			} catch (Exception x) {
			}

		}
	}

	public static void main(String[] args) {
		if (args.length > 0)
			query(new AnnotationToolsOntology(), "_text", args[0], 10);
	}

	protected String getQuery(TR record) {
		StringBuilder _catchall = new StringBuilder();
		if (TR.hix.cleanedvalue.get(record) != null)
			_catchall.append(TR.hix.cleanedvalue.get(record));
		else
			_catchall.append(TR.hix.Value.get(record));
		_catchall.append(" ");
		return _catchall.toString();
	}

	@Override
	public void process(TR record) {
		process(record, getQueryField(), getQuery(record), numberofhits,getLabel_tag());
	}

	@Override
	public void process(TR record, String queryField, String query, int maxhits , String label) {
		try {
			TopScoreDocCollector collector = search(queryField, query, maxhits);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			List<TR> tags = new ArrayList<TR>();
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);

				TR tag = new TR();
				tag.put(IAnnotator._termtag.uri.name(), d.get("subject_uri"));
				tag.put(IAnnotator._termtag.label.name(), d.get(label));
				tag.put(IAnnotator._termtag.score.name(), hits[i].score);
				tag.put(IAnnotator._termtag.rank.name(),(i+1));
				tags.add(tag);
			}
			TR terms = (TR)record.get("term");
			if (terms ==null) {
				terms = new TR();
				record.put("term",terms);
			}
			terms.put(getTerm_tag(), tags);
		} catch (Exception x) {
			//x.printStackTrace();
		}
	}
	@Override
	public String toString() {
		return getTerm_tag();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void done() {
		// TODO Auto-generated method stub
		
	}
}
