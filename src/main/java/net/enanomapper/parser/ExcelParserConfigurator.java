package net.enanomapper.parser;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import net.enanomapper.parser.ExcelDataLocation.IterationAccess;
import net.enanomapper.parser.ExcelDataLocation.Recognition;
import net.enanomapper.parser.json.JsonUtilities;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * 
 * @author nick
 *	Internally all indices/numbers/ of rows, columns and sheets are represented as 0-based integers
 *  while in the JSON configuration they are 1-based represented (user-friendly style) 
 *  The conversion from 1-based to 0-based and vice versa is done on "parsing" and toJSON() procedures respectively.
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
	public int rowMultiFixedSize = 1;
	public int startRow = 2;
	public int sheetIndex = 0;
	public int startHeaderRow = 0;
	public int endHeaderRow = 0;
	public boolean allowEmpty = true;
	public Recognition columnRecognition = Recognition.BY_INDEX;
	
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
					conf.configErrors.add("In JSON Section \"DATA_ACCESS\", keyword \"ITERATION\" is incorrect or UNDEFINED!");
			}			
			//SHEET_INDEX
			Integer intValue = jsonUtils.extractIntKeyword(curNode, "SHEET_INDEX", false);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.sheetIndex = intValue - 1; //1-based --> 0-based
			//START_ROW
			intValue = jsonUtils.extractIntKeyword(curNode, "START_ROW", false);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.startRow = intValue - 1; //1-based --> 0-based			
			//START_HEADER_ROW
			intValue = jsonUtils.extractIntKeyword(curNode, "START_HEADER_ROW", false);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.startHeaderRow = intValue - 1; //1-based --> 0-based
			//END_HEADER_ROW
			intValue = jsonUtils.extractIntKeyword(curNode, "END_HEADER_ROW", false);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.endHeaderRow = intValue - 1; //1-based --> 0-based
			//ALLOW_EMPTY
			Boolean boolValue = jsonUtils.extractBooleanKeyword(curNode, "ALLOW_EMPTY", false);
			if (boolValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.allowEmpty = boolValue;
			//COLUMN_RECOGNITION
			keyword =  jsonUtils.extractStringKeyword(curNode, "COLUMN_RECOGNITION", true);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
			{	
				conf.columnRecognition = Recognition.fromString(keyword);
				if (conf.columnRecognition == Recognition.UNDEFINED)
					conf.configErrors.add("In JSON Section \"DATA_ACCESS\", keyword \"COLUMN_RECOGNITION\" is incorrect or UNDEFINED!");
			}	
			
		}
		
		//Handle SubstanceRecord data locations
		curNode = root.path("SUBSTANCE_RECORD");
		if (curNode.isMissingNode())
			conf.configErrors.add("JSON Section \"SUBSTANCE_RECORD\" is missing!");
		else
		{
			//COMPANY_NAME
			ExcelDataLocation loc = extractDataLocation(curNode,"COMPANY_NAME", conf);
			if (loc == null)
				conf.configErrors.add("JSON Section \"SUBSTANCE_RECORD\", keyword  \"COMPANY_NAME\" is missing!");
			else
			{	
				if (loc.nErrors == 0)							
					conf.locations.put("SubstanceRecord.companyName", loc);
				//else
				//	conf.configErrors.add("JSON Section \"SUBSTANCE_RECORD\", keyword  \"COMPANY_NAME\" error: " + loc.error);
			}
			
		}
		
		return conf;
	}
	
	
	
	public String toJSONString()
	{
		ExcelDataLocation loc;
		int n;
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
		sb.append("\t\t\"SHEET_INDEX\" : " + (sheetIndex + 1) + ",\n" ); //0-based --> 1-based
		sb.append("\t\t\"START_ROW\" : " + (startRow + 1) + ",\n" ); //0-based --> 1-based
		sb.append("\t\t\"START_HEADER_ROW\" : " + (startHeaderRow + 1) + ",\n" ); //0-based --> 1-based
		sb.append("\t\t\"END_HEADER_ROW\" : " + (endHeaderRow + 1) + ",\n" ); //0-based --> 1-based
		sb.append("\t\t\"ALLOW_EMPTY\" : \"" + allowEmpty + "\",\n" );	
		sb.append("\t\t\"COLUMN_RECOGNITION\" : \"" + columnRecognition.toString() + "\",\n" );	
		sb.append("\t},\n\n");
		
		sb.append("\t\"SUBSTANCE_RECORD\" : \n");
		sb.append("\t{\n");
		n = 0;
		
		loc = locations.get("SubstanceRecord.companyName");
		if (loc != null)
		{
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}
		
		if (n > 0)
			sb.append("\n");
		
		sb.append("\t},\n\n"); //end of SUBSTANCE_RECORD
		
		
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
	
	public static ExcelDataLocation extractDataLocation(JsonNode node, String jsonSection, ExcelParserConfigurator conf)
	{
		JsonNode sectionNode = node.path(jsonSection);
		if (sectionNode.isMissingNode())
			return null;
		
		JsonUtilities jsonUtils = new JsonUtilities();
		
		ExcelDataLocation loc = new ExcelDataLocation();
		loc.sectionName = jsonSection;
		
		if (!sectionNode.path("ITERATION").isMissingNode())
		{
			String keyword =  jsonUtils.extractStringKeyword(sectionNode, "ITERATION", false);
			if (keyword == null)
			{	
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"ITERATION\" : " + jsonUtils.getError());
			}	
			else
			{	
				loc.FlagIteration = true;
				loc.iteration = IterationAccess.fromString(keyword);
				if (loc.iteration == IterationAccess.UNDEFINED)
					conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"ITERATION\" is incorrect or UNDEFINED!");
			}
		}
		
		Integer intValue = jsonUtils.extractIntKeyword(sectionNode, "COLUMN_INDEX", false);
		if (intValue == null)
			conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"COLUMN_INDEX\" : " + jsonUtils.getError());
		else
		{	
			loc.FlagColumnIndex = true;
			loc.columnIndex = intValue - 1; //1-based --> 0-based
		}
		
		String stringValue = jsonUtils.extractStringKeyword(sectionNode, "COLUMN_NAME", false);
		if (stringValue == null)
			conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"COLUMN_NAME\" : " + jsonUtils.getError());
		else
		{	
			loc.FlagColumnName = true;
			loc.columnName = stringValue;
		}
		
		return loc;
	}
}
