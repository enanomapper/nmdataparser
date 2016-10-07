package net.enanomapper.templates.app;

import java.io.File;
import java.io.FileNotFoundException;

public class MainAppSettings {
	public enum _TEMPLATES_TYPE {
		jrc, iom, undefuned
	}

	enum _TEMPLATES_CMD {
		extract, generate
	}

	private _TEMPLATES_TYPE templatesType = _TEMPLATES_TYPE.jrc;
	private _TEMPLATES_CMD templatesCommand = _TEMPLATES_CMD.extract;
	private String assayname;
	
	public String getAssayname() {
		return assayname;
	}

	public void setAssayname(String assayname) {
		this.assayname = assayname;
	}

	public _TEMPLATES_CMD getTemplatesCommand() {
		return templatesCommand;
	}

	public void setTemplatesCommand(_TEMPLATES_CMD templatesCommand) {
		this.templatesCommand = templatesCommand;
	}

	public _TEMPLATES_TYPE getTemplatesType() {
		return templatesType;
	}

	public void setTemplatesType(_TEMPLATES_TYPE templatesType) {
		this.templatesType = templatesType;
	}

	File inputfolder;

	public File getInputfolder() {
		return inputfolder;
	}

	public void setInputfolder(File inputfolder) throws FileNotFoundException {
		if (inputfolder.exists())
			this.inputfolder = inputfolder;
		else
			throw new FileNotFoundException(inputfolder == null ? "null" : inputfolder.getAbsolutePath());
	}

	public File getOutputfolder() {
		return outputfolder;
	}

	public void setOutputfolder(File outputfolder) throws FileNotFoundException {

		if (outputfolder.exists())
			this.outputfolder = outputfolder;
		else
			throw new FileNotFoundException(outputfolder == null ? "null" : outputfolder.getAbsolutePath());
	}

	File outputfolder;

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(getInputfolder());
		b.append("\n");
		b.append(getOutputfolder());
		return b.toString();
	}
}
