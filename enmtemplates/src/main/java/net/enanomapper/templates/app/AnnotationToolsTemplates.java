package net.enanomapper.templates.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ambit2.base.io.DownloadTool;
import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;

public class AnnotationToolsTemplates extends AnnotationToolsAbstract  {

	public AnnotationToolsTemplates() {
		super("jrctemplatez");
	}

	@Override
	protected IndexWriter process(IndexWriter writer, File tmpfile) throws Exception {
		if (tmpfile.getName().toLowerCase().endsWith(".csv"))
			smashCSV(writer, tmpfile, true, pathIndex);
		else if (tmpfile.getName().toLowerCase().endsWith(".xlsx"))
			smashXLSX(writer, tmpfile, true, pathIndex);
		writer.flush();
		writer.commit();
		return writer;
	}

	@Override
	protected File getFile() throws Exception {
		File tmpfile = new File(System.getProperty("java.io.tmpdir"), "JRCTEMPLATES.csv");
		DownloadTool.download("net/idea/magicmapper/JRCTEMPLATES.csv", tmpfile);
		return tmpfile;
	}

	public void smashXLSX(IndexWriter writer, File file, boolean zipped, Path indexed) throws Exception {
		/*
		Workbook workbook = new XSSFWorkbook(file);
		Sheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.rowIterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			Document doc = new Document();
			StringBuilder _catchall = new StringBuilder();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				
				TextField tf = new TextField(header[j], record.get(j), Store.YES);
				doc.add(tf);
				if (j == 1 || j == 2 || j == 3 || j == 6) {
					_catchall.append(record.get(j));
					_catchall.append(" ");
				}
				
			}
		}
		workbook.close();
		*/
	}
	public void smashCSV(IndexWriter writer, File file, boolean zipped, Path indexed) throws Exception {

		try (InputStreamReader in = new InputStreamReader(new BOMInputStream(new FileInputStream(file)),
				StandardCharsets.UTF_8)) {
			CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(in);

			final String[] header = TR.header;
			for (CSVRecord record : parser) {

				// System.out.println(record);
				Document doc = new Document();

				StringBuilder _catchall = new StringBuilder();

				for (int j = 0; j < header.length; j++)
					if (j < record.size()) {
						TextField tf = new TextField(header[j], record.get(j), Store.YES);
						doc.add(tf);
						if (j == 1 || j == 2 || j == 3 || j == 6) {
							_catchall.append(record.get(j));
							_catchall.append(" ");
						}
					}
				TextField tf = new TextField("_text", _catchall.toString(), Store.NO);
				doc.add(tf);
				writer.addDocument(doc);
				// ID,Folder,File,Sheet,Row,Column,Value,Annotation,header1,cleanedvalue,unit,hint,JSON_LEVEL1,JSON_LEVEL2,JSON_LEVEL3
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			AnnotationToolsTemplates tools = new AnnotationToolsTemplates();
			try {
				FileUtils.deleteDirectory(tools.getPathIndex().toFile());
			} catch (Exception x) {
				x.printStackTrace();
			}

			// tools = new AnnotationToolsTemplates();
			try {
				tools.open();
			} catch (Exception x) {
				x.printStackTrace();
			} finally {
				try {
					tools.close();
				} catch (Exception x) {
				}
			}
		} else

		if (args.length > 0)
			query(new AnnotationToolsTemplates(), "_text", args[0], 3);
	}

	@Override
	public void process(TR record) {
		StringBuilder _catchall = new StringBuilder();
		_catchall.append(TR.hix.Folder.get(record));
		_catchall.append(" ");
		_catchall.append(TR.hix.File.get(record));
		_catchall.append(" ");
		_catchall.append(TR.hix.Sheet.get(record));
		_catchall.append(" ");
		_catchall.append(TR.hix.Value.get(record));
		try {
			TopScoreDocCollector collector = search(_catchall.toString(), 1);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			if (hits == null || hits.length == 0)
				record.put(TR.hix.Warning.name(), "No hits");
			else
				for (int i = 0; i < hits.length; ++i) {
					int docId = hits[i].doc;
					Document d = searcher.doc(docId);
					for (TR.hix h : TR.hix.values())
						if (h.isAnnotation()) {
							record.put(h.name(), d.get(h.name()));
						}
					record.put(TR.hix.Warning.name(), hits[i].score);
				}
		} catch (Exception x) {

		}
	}
	
	@Override
	public void process(TR record, String queryField, String query, int maxhits, String label) {

		
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
