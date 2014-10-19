package net.enanomapper.parser;

import java.util.ArrayList;


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
	
	public static ExcelParserConfigurator loadFromJSON(String jsonFileName) throws Exception
	{
		ExcelParserConfigurator epConfig = new ExcelParserConfigurator(); 
		
		//TODO
		
		return epConfig;
	}
}
