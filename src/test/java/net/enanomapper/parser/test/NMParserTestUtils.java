package net.enanomapper.parser.test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.ProtocolApplication;
import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.GenericExcelParser;


public class NMParserTestUtils {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)  throws Exception
	{
		//testExcelParserConfiguration("/Users/nick/Projects/eNanoMapper/config01.json");
		//testExcelParserConfiguration("D:/Projects/nina/eNanoMapper/config01.json");
		
		//testExcelTemplate("/Users/nick/Projects/eNanoMapper/template01-NR.xlsx","/Users/nick/Projects/eNanoMapper/config01.json");
		testExcelTemplate("D:/Projects/nina/eNanoMapper/template01-NR.xlsx","D:/Projects/nina/eNanoMapper/config01.json");
		
		//System.out.println(IterationAccess.fromString("ROW_SINGLE"));
		
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
	
	
	
	public static void testExcelTemplate(String excelFile, String jsonFile) throws Exception
	{
		FileInputStream fin = new FileInputStream(excelFile);
		boolean isXLSX = excelFile.endsWith("xlsx");
		System.out.println("isXLSX = " + isXLSX + "\n");
		GenericExcelParser parser = new GenericExcelParser(fin, jsonFile, isXLSX);
		System.out.println(parser.getExcelParserConfigurator().toJSONString() + "\n");
		
		int n = 0;
		while (parser.hasNext())
		{	
			SubstanceRecord r = parser.nextRecord();
			n++;
			System.out.println("Record #" + n);
			System.out.println(r.toJSON(null));
			List<ProtocolApplication> paList = r.getMeasurements();
			
			if (paList != null)
				for (ProtocolApplication pa : paList)
					System.out.println("***Protocol application:\n"+pa.toString());
		}
		
		if (parser.hasErrors())
			System.out.println("\n\nParser errors:\n" + parser.errorsToString());
		
		fin.close();
		
	}
	

}
