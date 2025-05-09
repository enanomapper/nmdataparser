package net.enanomapper.parser.templatedesigner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.KEYWORD;
import net.enanomapper.parser.ProtocolApplicationDataLocation;
import net.enanomapper.parser.json.JsonUtilities;

public class DesignerJson2ConfigJson {
	
	//private StringBuilder configJson = null;
	//private List<String> configErrors = new ArrayList<String>();
	//private List<String> configWarnings = new ArrayList<String>();
	JsonUtilities jsonUtils = new JsonUtilities();
	ExcelParserConfigurator curExParConf = null;
	
	
	public DesignerJson2ConfigJson() {
		init();
	}
	
	private void init() {
		
	}
	
	/*
	public String getConfigJson() {
		return configJson.toString();
	}
	*/
	
	
	public ExcelParserConfigurator convertDesignJson2ExcelParserConfigurator(String designJsonFile) throws FileNotFoundException, IOException, JsonProcessingException
	{
		return convertDesignJson2ExcelParserConfigurator(new FileInputStream(designJsonFile));
	}
	
	public ExcelParserConfigurator convertDesignJson2ExcelParserConfigurator(InputStream designJsonStream) throws FileNotFoundException, IOException, JsonProcessingException
	{
		//configJson = new StringBuilder();
		//configErrors.clear();
		//configWarnings.clear();
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = null;

		try {
			root = mapper.readTree(designJsonStream);
		} catch (JsonProcessingException x) {
			throw x;			
		} catch (IOException x) {
			throw x;
		} finally {
			try {
				designJsonStream.close();
			} catch (Exception x) {
			}
		}

		
		ExcelParserConfigurator conf = new ExcelParserConfigurator();
		curExParConf = conf;
		
		//Handle basic template info
		String template_date = jsonUtils.extractStringKeyword(root, "template_date", false);
		String template_status = jsonUtils.extractStringKeyword(root, "template_status", false);
		String template_name = jsonUtils.extractStringKeyword(root, "template_name", false);
		String template_author = jsonUtils.extractStringKeyword(root, "template_author", false);
		conf.templateName = template_name;
		conf.templateVersion = template_status + "-" + template_date + "-" + template_author;
				
		//Adding the basic ProtocolApplicationDataLocation
		ProtocolApplicationDataLocation padl = new ProtocolApplicationDataLocation();
		conf.protocolAppLocations.add(padl);
		
		// Handle template_layout
		String template_layout = jsonUtils.extractStringKeyword(root, "template_layout", false);
		//System.out.println("template_layout = " + template_layout);
				
		if (template_layout.equals("pchem"))
			convertPChemLayoutDesign(root);
		
		if (template_layout.equals("dose_response"))
			convertDoseResponseLayoutDesign(root);
		
		return conf;
		
	}
	
	void convertPChemLayoutDesign(JsonNode root) {
		//TODO
	}
	
	void convertDoseResponseLayoutDesign(JsonNode root) {
		//TODO
	}

}
