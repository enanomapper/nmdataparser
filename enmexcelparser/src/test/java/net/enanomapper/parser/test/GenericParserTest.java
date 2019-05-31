package net.enanomapper.parser.test;

import org.junit.Test;

import junit.framework.Assert;
import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.GenericExcelParser;
import net.enanomapper.parser.recognition.RecognitionUtils;
import net.enanomapper.parser.recognition.RichValue;
import net.enanomapper.parser.recognition.RichValueParser;

public class GenericParserTest {
	@Test
	public void testUUID() throws Exception {
		String prefix = "TEST";
		String result = ExcelParserConfigurator.generateUUID(prefix, "blabla");
		Assert.assertEquals(prefix + "-df5ea299-24d3-3c3b-a878-5734f13169c6", result);
		String result1 = ExcelParserConfigurator.generateUUID(prefix, result);
		Assert.assertEquals(result, result1);
	}

	@Test
	public void testValidQualifier() throws Exception {
		Assert.assertTrue(ExcelParserConfigurator.isValidQualifier(null));
		for (String q : RecognitionUtils.qualifiers) {
			if (" ".equals(q)) continue;
			Assert.assertTrue(q,ExcelParserConfigurator.isValidQualifier(q.toUpperCase()));
		}
	}
	

	@Test
	public void testRichValue_bad() throws Exception {
		RichValueParser rvParser = new RichValueParser();
	
		String paramStringValue = "MSP Model 135 Mini-MOUDI impactor";
		RichValue rv = rvParser.parse(paramStringValue);
		
	}

	
}
