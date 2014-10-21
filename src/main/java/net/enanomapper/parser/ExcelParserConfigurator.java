package net.enanomapper.parser;

import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * 
 * @author nick
 *
 */
public class ExcelParserConfigurator 
{
	public int configurationType = 1;
	public int startRow = 1;
	public int headerRow = 1;
	public ArrayList<String> configErrors = new ArrayList<String> ();
	
	public static ExcelParserConfigurator loadFromJSON(String jsonConfig) throws Exception
	{
		FileInputStream fin = new FileInputStream(jsonConfig); 
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = null;
		
		try {
			rootNode = (ObjectNode)mapper.readTree(fin);
		} catch (Exception x) {
			throw x;
		} finally {
			try {fin.close();} catch (Exception x) {}	
		}
		ExcelParserConfigurator epConfig = new ExcelParserConfigurator(); 
		
		//TODO
		
		return epConfig;
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
