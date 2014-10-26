package net.enanomapper.parser.test;

import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.ExcelParserConfigurator.IterationAccess;

public class NMParserTestUtils {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)  throws Exception
	{
		//testExcelParserConfiguration("/Users/nick/Projects/eNanoMapper/config01.json");
		
		System.out.println(IterationAccess.fromString("ROW_SINGLE"));
		

	}
	
	public static void testExcelParserConfiguration(String jsonFile) throws Exception
	{
		ExcelParserConfigurator parserConfig = ExcelParserConfigurator.loadFromJSON(jsonFile);
		
		if (parserConfig.configErrors.size() > 0)
		{	
			System.out.println("GenericExcelParser configuration errors:\n" + parserConfig.getAllErrorsAsString());
			return;
		}	
		
		System.out.println("ExcelParserConfigurator " + jsonFile);
		System.out.println(parserConfig.toJSONString());
	}

}
