package net.idea.templates.annotation.test;

import java.io.File;

import net.idea.templates.annotation.AnnotatorChain;
import net.idea.templates.extraction.AssayTemplatesParser;

public class TestExportAnnotate {

	public static void main(String[] args) throws Exception {
		if (args[0] == null)
			throw new Exception("No input file specified");

		File file = new File(args[0]);
		File templatejson = new File("templates.json");
		AnnotatorChain a = new AnnotatorChain();

		AssayTemplatesParser.xls2json(file, templatejson, a);
	}

}
