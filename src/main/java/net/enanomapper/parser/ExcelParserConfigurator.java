package net.enanomapper.parser;

import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.IOException;

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
		ROW_SINGLE, ROW_MULTI_FIXED, ROWS_MULTI_DYNAMIC, UNDEFINED;
		
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
	
	public int configurationType = 1;
	public int startRow = 1;
	public int headerRow = 1;
	public String templateName = ""; 
	
	
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
		ExcelParserConfigurator epConfig = new ExcelParserConfigurator(); 
		
		//TODO
		
		return epConfig;
	}
	
	public String toJSONString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("{\n");
		sb.append("   \"template_name\" : \"" + templateName + "\",\n" );
		
		
		
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
