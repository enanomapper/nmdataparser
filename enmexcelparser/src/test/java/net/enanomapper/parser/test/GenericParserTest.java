package net.enanomapper.parser.test;

import org.junit.Test;

import junit.framework.Assert;
import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.recognition.RecognitionUtils;

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
		for (String q : RecognitionUtils.qualifiers) {
			Assert.assertTrue(q,ExcelParserConfigurator.isValidQualifier(q.toUpperCase()));
		}
	}
}
