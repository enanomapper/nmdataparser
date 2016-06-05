package net.enanomapper.parser.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

public class NRTemplatesTest extends TestWithExternalFiles {
	protected static Properties templates;

	@BeforeClass
	public static void initTemplateNames() throws IOException {
		templates = new Properties();
		InputStream in = NRTemplatesTest.class.getClassLoader()
				.getResourceAsStream(
						"data/xlsx/nanoreg/nrtemplates.properties");
		try {
			Assert.assertNotNull(in);
			templates.load(in);
		} finally {
			in.close();
		}
	}

	@Test
	public void testTemplatesAvailable() throws Exception {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		Assert.assertNotNull(templates);
		Assert.assertEquals(15, templates.size());
		Enumeration<Object> e = templates.keys();
		while (e.hasMoreElements()) {
			Object key = e.nextElement();
			String fileUrl = templates.getProperty(key.toString());
			File file = getTestFile(fileUrl, key.toString(), ".xlsx",baseDir);
			Assert.assertTrue(file.exists());
			System.out.println(file.getAbsolutePath());
		}
	}
}
