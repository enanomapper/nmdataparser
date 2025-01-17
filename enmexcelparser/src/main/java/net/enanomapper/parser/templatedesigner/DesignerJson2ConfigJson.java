package net.enanomapper.parser.templatedesigner;

public class DesignerJson2ConfigJson {
	
	private StringBuilder configJson = new StringBuilder();
	private String designJsonFile = null;
	
	public DesignerJson2ConfigJson(String designJsonFile) {
		this.designJsonFile = designJsonFile;
	}
	
	public String getConfigJson() {
		return configJson.toString();
	}

}
