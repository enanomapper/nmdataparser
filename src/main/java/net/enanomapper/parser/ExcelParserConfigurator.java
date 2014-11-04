package net.enanomapper.parser;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import net.enanomapper.parser.ExcelDataLocation.IterationAccess;
import net.enanomapper.parser.json.JsonUtilities;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * 
 * @author nick
 *	Internally all numbers of rows, columns and sheets are represented as 0-based integers
 *  while in the JSON configuration they are 1-based represented (user-friendly style) 
 *  The conversion from 1-based to 0-based and vice versa is done on "parsing" and toJSON() procedures.
 */
public class ExcelParserConfigurator 
{	
	
	public ArrayList<String> configErrors = new ArrayList<String> ();
	public ArrayList<String> configWarning = new ArrayList<String> ();
	
	//Configuration variables
	public String templateName = null;
	public String templateVersion = null;	
	public int templateType = 1;
	
	//Global configuration for the data access
	public ExcelDataLocation.IterationAccess substanceIteration =  IterationAccess.ROW_SINGLE;	
	public int startRow = 2;
	public int sheetNum = 0;
	public int[] headerRows = {0,1};
	
	//Specific data locations
	public HashMap<String, ExcelDataLocation> locations = new HashMap<String, ExcelDataLocation>();
	
	
	
	public static ExcelParserConfigurator loadFromJSON(String jsonConfig) throws Exception
	{
		FileInputStream fin = new FileInputStream(jsonConfig); 
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = null;
		
		try {
			root = mapper.readTree(fin);
		} catch (Exception x) {
			throw x;
		} finally {
			try {fin.close();} catch (Exception x) {}	
		}
		
		JsonUtilities jsonUtils = new JsonUtilities();
		ExcelParserConfigurator conf = new ExcelParserConfigurator(); 
		
		//Handle template info 
		JsonNode curNode = root.path("TEMPLATE_INFO");
		if (curNode.isMissingNode())
			conf.configWarning.add("JSON Section \"TEMPLATE_INFO\" is missing!");
		else
		{
			//NAME
			String keyword =  jsonUtils.extractStringKeyword(curNode, "NAME", false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.templateName = keyword;
			//VERSION
			keyword =  jsonUtils.extractStringKeyword(curNode, "VERSION", false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.templateVersion = keyword;
			//TYPE
			Integer intValue = jsonUtils.extractIntKeyword(curNode, "TYPE", true);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.templateType = intValue;
		}
		
		//Handle global data access
		curNode = root.path("DATA_ACCESS");
		if (curNode.isMissingNode())
			conf.configErrors.add("JSON Section \"DATA_ACCESS\" is missing!");
		else
		{
			//ITERATION
			String keyword =  jsonUtils.extractStringKeyword(curNode, "ITERATION", true);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
			{	
				conf.substanceIteration = IterationAccess.fromString(keyword);
				if (conf.substanceIteration == IterationAccess.UNDEFINED)
					conf.configErrors.add("In JSON Section \"DATA_ACCESS\"  \"ITERATION\" is incorrect or UNDEFINED!");
			}			
			//SHEET_NUM
			Integer intValue = jsonUtils.extractIntKeyword(curNode, "SHEET_NUM", false);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.sheetNum = intValue - 1; //1-based --> 0-based
			//START_ROW
			intValue = jsonUtils.extractIntKeyword(curNode, "START_ROW", false);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.startRow = intValue - 1; //1-based --> 0-based
			
		}
		
		
		//Handle specific data locations
		//TODO
		
		return conf;
	}
	
	
	
	public String toJSONString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("{\n");
		
		sb.append("\t\"TEMPLATE_INFO\" : \n");
		sb.append("\t{\n");
		if (templateName != null)
			sb.append("\t\t\"NAME\" : \"" + templateName + "\",\n" );
		if (templateVersion != null)
			sb.append("\t\t\"VERSION\" : \"" + templateVersion + "\",\n" );		
		sb.append("\t\t\"TYPE\" : " + templateType + "\n" );
		sb.append("\t},\n\n");
		
		sb.append("\t\"DATA_ACCESS\" : \n");
		sb.append("\t{\n");		
		sb.append("\t\t\"ITERATION\" : \"" + substanceIteration.toString() + "\",\n" );	
		sb.append("\t\t\"SHEET_NUM\" : " + (sheetNum + 1) + ",\n" ); //0-based --> 1-based
		sb.append("\t\t\"START_ROW\" : " + (startRow + 1) + ",\n" ); //0-based --> 1-based
		sb.append("\t},\n");
		
		sb.append("}\n");
		return sb.toString();
	}
	
	public String getAllErrorsAsString()
	{
		if (configErrors.isEmpty())
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < configErrors.size(); i++)
			sb.append(configErrors.get(i) + "\n");
		return sb.toString();
	}
	
	public ExcelDataLocation extractDataLocation(JsonNode node, String keyword)
	{
		//TODO
		return null;
	}
}
