package net.enanomapper.maker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

public class TemplateMakerSettings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7761828667318892718L;
	transient protected IAnnotator annotator;

	public IAnnotator getAnnotator() {
		return annotator;
	}

	public void setAnnotator(IAnnotator annotator) {
		this.annotator = annotator;
	}

	public enum _TEMPLATES_TYPE {
		jrc, iom, all, undefuned
	}

	public enum _TEMPLATES_CMD {
		extract, generate {
			@Override
			public boolean requiresInputFile() {
				return false;
			}
		}, annotate, help {
			@Override
			public boolean requiresInputFile() {
				return false;
			}

			@Override
			public boolean requiresOutputFileFile() {
				return false;
			}
		};
		public boolean requiresInputFile() {
			return true;
		}

		public boolean requiresOutputFileFile() {
			return true;
		}
	}

	private _TEMPLATES_TYPE templatesType = _TEMPLATES_TYPE.jrc;
	private _TEMPLATES_CMD templatesCommand = _TEMPLATES_CMD.help;
	private String assayname;
	private String endpointname;

	public String getEndpointname() {
		return endpointname;
	}

	public void setEndpointname(String endpointname) {
		this.endpointname = endpointname;
	}

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
		if (inputfolder != null && inputfolder.exists())
			this.inputfolder = inputfolder;
		else if (getTemplatesCommand().requiresInputFile())
			throw new FileNotFoundException(inputfolder == null ? "Input file not specified, see option -i" : inputfolder.getAbsolutePath());
	}

	public File getOutputfolder() {
		return outputfolder;
	}

	public void setOutputfolder(File outputfolder) throws FileNotFoundException {

		if (outputfolder != null && outputfolder.exists())
			this.outputfolder = outputfolder;
		else if (getTemplatesCommand().requiresOutputFileFile())
			throw new FileNotFoundException(outputfolder == null ? "Output file not specified, see option -o" : outputfolder.getAbsolutePath());
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
