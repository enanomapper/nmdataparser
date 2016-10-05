package net.enanomapper.templates.app;

import java.io.File;
import java.io.FileNotFoundException;

public class MainAppSettings {
	enum _TEMPLATES_TYPE {jrc,iom,undefuned}
	private _TEMPLATES_TYPE templatesType = _TEMPLATES_TYPE.jrc;
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
			throw new FileNotFoundException(inputfolder == null ? "null"
					: inputfolder.getAbsolutePath());
	}

	public File getOutputfolder() {
		return outputfolder;
	}

	public void setOutputfolder(File outputfolder) throws FileNotFoundException {

		if (outputfolder.exists())
			this.outputfolder = outputfolder;
		else
			throw new FileNotFoundException(outputfolder == null ? "null"
					: outputfolder.getAbsolutePath());
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
