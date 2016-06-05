package net.enanomapper.parser.test;

import java.io.File;
import java.net.URL;

import ambit2.base.io.DownloadTool;

public class TestWithExternalFiles {

	protected File getTestFile(String remoteurl, String localname, File baseDir)
			throws Exception {
		URL url = new URL(remoteurl);
		File file = new File(baseDir, localname + ".rdf");
		if (!file.exists())
			DownloadTool.download(url, file);
		return file;
	}
}
