package net.enanomapper.parser;

import java.util.ArrayList;


/**
 * 
 * @author nick
 *
 */
public class GenericExcelParser 
{
	
	private ArrayList<String> parseErrors = new ArrayList<String> ();
	private ExcelParserConfigurator parserConfig = null;
	
	public void configParser(String jsonFileName) throws Exception
	{
		parserConfig = ExcelParserConfigurator.loadFromJSON(jsonFileName);
		
	}
	
	
}
