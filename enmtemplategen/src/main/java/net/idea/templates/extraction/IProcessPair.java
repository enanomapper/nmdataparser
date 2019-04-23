package net.idea.templates.extraction;

import java.io.File;

public interface IProcessPair {
	public void process(String prefix, String root, File spreadsheet, File json);
}
