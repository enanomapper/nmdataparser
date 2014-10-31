package net.enanomapper.parser;

import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.IOException;

import net.enanomapper.parser.json.JsonUtilities;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * 
 * @author nick
 *
 */
public class ExcelParserConfigurator 
{
	public enum IterationAccess {
		ROW_SINGLE, ROW_MULTI_FIXED, ROW_MULTI_DYNAMIC, UNDEFINED;
		
		public static IterationAccess fromString(String s)
		{	 
			try
			{
				IterationAccess access = IterationAccess.valueOf(s) ;
				return (access);
			}
			catch (Exception e)
			{
				return IterationAccess.UNDEFINED;
			}
		}
	}
	
	
	public ArrayList<String> configErrors = new ArrayList<String> ();
	public ArrayList<String> configWarning = new ArrayList<String> ();
	
	//Configuration variables
	public String templateName = null;
	public String templateVersion = null;	
	public int templateType = 1;
	
	public IterationAccess substanceIteration =  IterationAccess.ROW_SINGLE;	
	public int startRow = 2;
	public int[] headerRows = {1,2};
	 
	
	
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
		JsonNode templateNode = root.path("TEMPLATE_INFO");
		if (templateNode.isMissingNode())
			conf.configWarning.add("JSON Section 'TEMPLATE_INFO' is missing!");
		else
		{
			//NAME
			String keyword =  jsonUtils.extractStringKeyword(templateNode, "NAME", false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.templateName = keyword;
			//VERSION
			keyword =  jsonUtils.extractStringKeyword(templateNode, "VERSION", false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.templateVersion = keyword;
			//TYPE
			Integer intValue = jsonUtils.extractIntKeyword(templateNode, "TYPE", true);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.templateType = intValue;
			
		}	
		
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
}
