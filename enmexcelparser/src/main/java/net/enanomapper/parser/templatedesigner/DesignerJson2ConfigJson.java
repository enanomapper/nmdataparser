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

import net.enanomapper.parser.json.JsonUtilities;

public class DesignerJson2ConfigJson {
	
	private StringBuilder configJson = null;
	private List<String> configErrors = new ArrayList<String>();
	private List<String> configWarnings = new ArrayList<String>();
	
	
	public DesignerJson2ConfigJson() {
		init();
	}
	
	private void init() {
		
	}
	
	public String getConfigJson() {
		return configJson.toString();
	}
	
	
	public void convertDesignJson2ConfigJson(String designJsonFile) throws FileNotFoundException, IOException, JsonProcessingException
	{
		convertDesignJson2ConfigJson(new FileInputStream(designJsonFile));
	}
	
	public void convertDesignJson2ConfigJson(InputStream designJsonStream) throws FileNotFoundException, IOException, JsonProcessingException
	{
		configJson = new StringBuilder();
		configErrors.clear();
		configWarnings.clear();
		
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

		JsonUtilities jsonUtils = new JsonUtilities();
		
		//TODO
		
	}

}
