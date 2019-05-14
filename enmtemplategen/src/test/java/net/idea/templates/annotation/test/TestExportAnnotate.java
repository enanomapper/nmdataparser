package net.idea.templates.annotation.test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.enanomapper.maker.TR;
import net.idea.templates.annotation.AnnotatorChain;
import net.idea.templates.annotation.JsonConfigGenerator;
import net.idea.templates.annotation.SimpleAnnotator;
import net.idea.templates.extraction.AssayTemplatesParser;
import net.idea.templates.generation.Term;
import net.idea.templates.generation.Tools;

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
