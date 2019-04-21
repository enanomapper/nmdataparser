package net.idea.templates.extraction;

import java.io.File;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


import ambit2.core.io.FileState;

public abstract class AssayTemplatesParser {

	private static final String _root = "root";

	public static String getRoot() {
		return _root;
	}

	private static final String _singledb = "singledb";
	private static final String _release = "release";

	protected static Logger logger = Logger.getLogger(AssayTemplatesParser.class.getName());

	public int parseResources(ResourceBundle nanodataResources, String prefix) throws Exception {
		return parseResources(nanodataResources, prefix, false, null);
	}

	public String getRootValue(ResourceBundle nanodataResources) {
		return nanodataResources.getString(_root);
	}
	public int parseResources(ResourceBundle nanodataResources, String prefix, boolean dryRun, IProcessPair processpair)
			throws Exception {
		final String root = getRootValue(nanodataResources);
		boolean singledb = Boolean.parseBoolean(nanodataResources.getString(_singledb));
		String release = nanodataResources.getString(_release);
		Enumeration<String> enumeration = nanodataResources.getKeys();

		int count = 0;
		int substance = 0;
		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();

			if (_root.equals(key))
				continue;
			if (_singledb.equals(key))
				continue;
			if (_release.equals(key))
				continue;
			// hack to have one file with multiple json configs - the keys
			// should be unique, but files the same
			int ix = key.indexOf("#");
			File file = new File(root, (ix > 0) ? key.substring(0, ix) : key);
			if (file.exists()) {
				logger.log(Level.INFO, String.format("Directory:%s\t%s%s\t%s", file.isDirectory(), root, key,
						nanodataResources.getString(key)));
				if (file.isDirectory()) {
					if (new File(file, "donotimport.properties").exists())
						continue;
					File json = new File(file, nanodataResources.getString(key));
					if (!json.exists()) {
						logger.log(Level.WARNING, String.format("%s not found", json.getAbsolutePath()));
						json = null;
					}
					for (File data : file.listFiles())
						if (acceptDataFile(data))
							if (dryRun && processpair != null)
								processpair.process(prefix, root, data,
										json == null ? getJsonDataName(data, nanodataResources.getString(key)) : json);
							else
								try {
									substance += processFile(data, json == null
											? getJsonDataName(data, nanodataResources.getString(key)) : json, prefix,
											!singledb, release);
								} catch (Exception x) {
									logger.log(Level.WARNING, x.getMessage(), x);
								}
				} else {
					if (acceptDataFile(file)) {
						File json = getJsonDataName(file.getParentFile(), nanodataResources.getString(key));
						if (json == null)
							continue;
						if (!json.exists()) {
							logger.log(Level.WARNING, String.format("%s not found", json.getAbsolutePath()));
						} else {
							if (dryRun)
								processpair.process(prefix, root, file, json);
							else
								substance += processFile(file, json, prefix, !singledb, release);
						}
					}
				}
				count++;
			} else
				logger.log(Level.WARNING, String.format("%s\t%s", file.getAbsolutePath(), "not found"));
		}
		if (!dryRun)
			return verifyFiles(count);
		else
			return count;
	}

	protected File getJsonDataName(File file, String jsonname) {
		File json = new File(file, jsonname);
		if (!json.exists()) {
			json = changeFileExtension(file, ".json");
		}
		return json;
	}

	protected boolean acceptDataFile(File data) {
		return (FileState._FILE_TYPE.XLS_INDEX.hasExtension(data)
				|| FileState._FILE_TYPE.XLSX_INDEX.hasExtension(data));
	}

	protected File changeFileExtension(File data, String newextension) {
		if (FileState._FILE_TYPE.XLS_INDEX.hasExtension(data))
			return new File(data.getAbsolutePath().replaceAll(".xls", newextension));
		else if (FileState._FILE_TYPE.XLS_INDEX.hasExtension(data))
			return new File(data.getAbsolutePath().replaceAll(".xlsx", newextension));
		return null;
	}

	protected abstract int processFile(File spreadsheet, File json, String prefix, boolean resetdb, String release)
			throws Exception;

	protected int verifyFiles(int count) throws Exception {
		return count;
	}

}
