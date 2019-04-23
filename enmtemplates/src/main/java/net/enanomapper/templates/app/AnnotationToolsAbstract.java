package net.enanomapper.templates.app;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import ambit2.base.io.DownloadTool;
import net.enanomapper.maker.IAnnotator;

public abstract class AnnotationToolsAbstract implements IAnnotator {
	protected int numberofhits  =1;
	public int getNumberofhits() {
		return numberofhits;
	}

	public void setNumberofhits(int numberofhits) {
		this.numberofhits = numberofhits;
	}

	protected Path pathIndex;
	protected IndexReader reader = null;
	protected IndexSearcher searcher = null;

	public IndexSearcher getSearcher() {
		return searcher;
	}

	protected Directory index = null;
	protected Analyzer analyzer = new StandardAnalyzer();

	public AnnotationToolsAbstract() {
		this("/enmindez");
	}

	public AnnotationToolsAbstract(String indexname) {
		pathIndex = createPath(indexname);
	}

	protected Path createPath(String indexname) {
		return FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir") + indexname);
	}

	public Path getPathIndex() {
		return pathIndex;
	}

	public void setPathIndex(Path pathIndex) {
		this.pathIndex = pathIndex;
	}

	protected IndexWriter getIndexedWriter() throws Exception {
		Directory index = FSDirectory.open(pathIndex);
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		return new IndexWriter(index, config);
	}

	protected void index(File tmpfile) throws Exception {
		IndexWriter writer = getIndexedWriter();
		try {
			writer = process(writer, tmpfile);
		} catch (Exception x) {
			if (writer != null)
				writer.close();
		}
	}

	protected abstract IndexWriter process(IndexWriter writer, File tmpfile) throws Exception;

	public void close() throws Exception {
		if (reader != null) {
			reader.close();
			reader = null;
		}
		if (index != null) {
			index.close();
			index = null;
		}
		searcher = null;

	}

	protected boolean isClosed() {
		return (index == null || reader == null || searcher == null);
	}

	public TopScoreDocCollector search(String query, int maxhits) throws Exception {
		return search("_text", query, maxhits);
	}

	public TopScoreDocCollector search(String field, String query, int maxhits) throws Exception {
		if (isClosed())
			open();
		String queryclean = query.replaceAll(":", " ").replace("/", " ").replace("*", " ").replace("["," ").replace("]"," ").replace("_"," ").trim();
		org.apache.lucene.search.Query q = new QueryParser(field, analyzer).parse(queryclean);
		TopScoreDocCollector collector1 = TopScoreDocCollector.create(maxhits);
		searcher.search(q, collector1);
		return collector1;
		// "http://www.w3.org/2002/07/owl#Thing");
	}

	protected abstract File getFile() throws Exception;

	public void open() throws Exception {
		if (!pathIndex.toFile().exists()) {
			index(getFile());
		}
		if (index == null || reader == null || searcher == null) {
			try {
				close();
			} catch (Exception x) {
			}
			index = FSDirectory.open(pathIndex);
			reader = DirectoryReader.open(index);
			searcher = new IndexSearcher(reader);
		}
	}

	public void printHits(String prefix, ScoreDoc[] hits) throws IOException {
		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			System.out.println();
			for (IndexableField f : d.getFields()) {
				System.out.println(
						String.format("%s\t%s\t%s\t%s\t%s", hits[i].score, i, docId, f.name(), d.get(f.name())));
			}
			System.out.println();
		}
	}

	public static void query(AnnotationToolsAbstract tools, String field, String query, int maxhits) {
		try {
			TopScoreDocCollector collector = tools.search(field, query, maxhits);
			ScoreDoc[] hits1 = collector.topDocs().scoreDocs;
			tools.printHits("", hits1);
			System.out.println(String.format("Query completed %s\t%s", tools.getPathIndex().toString(), hits1.length));
		} catch (Exception x) {
			x.printStackTrace();
		} finally {
			try {
				tools.close();
			} catch (Exception x) {
			}
		}
	}

	public static File getCachedFile(URL remoteurl, String localname, File baseDir) throws Exception {
		boolean gz = remoteurl.getFile().endsWith(".gz");
		File file = new File(baseDir, localname + (gz ? ".gz" : ""));
		if (!file.exists())
			DownloadTool.download(remoteurl, file);
		return file;
	}

	private String term_tag;
	private String queryField = "label";
	public String getTerm_tag() {
		return term_tag;
	}

	public void setTerm_tag(String term_tag) {
		this.term_tag = term_tag;
	}
	@Override
	public void setQueryField(String queryField) {
		this.queryField = queryField;
	}
	@Override
	public String getQueryField() {
		return queryField;
	}
	@Override
	public String getLabel_tag() {
		return "label";
	}
}
