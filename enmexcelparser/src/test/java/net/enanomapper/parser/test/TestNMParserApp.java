package net.enanomapper.parser.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.relation.composition.CompositionRelation;
import net.enanomapper.parser.GenericExcelParser;
import net.enanomapper.parser.excel.SubstanceAnalysisTask;
import net.enanomapper.parser.excel.SubstanceDataAggregator;

public class TestNMParserApp {

	public static SubstanceDataAggregator substanbceAggregator = null;
	
	public static void main(String[] args) throws Exception
	{
		if (args.length < 2)
			System.out.println("CLI Arguments: <excel file>  <json file>");
		else
			testExcelTemplate(args[0], new File(args[1]), false);
	}
	
	public static void testExcelTemplate(String excelFile, File jsonFile, boolean printJSONConfig)
			throws Exception {
		testExcelTemplate(excelFile, jsonFile, printJSONConfig, true, true);
	}
	
	public static void testExcelTemplate(String excelFile, File jsonFile, 
			boolean printJSONConfig, boolean flagLogVariableMapping) throws Exception 
	{
		testExcelTemplate(excelFile, jsonFile, printJSONConfig, flagLogVariableMapping, true);
	}
	
	public static void testExcelTemplate(String excelFile, File jsonFile, 
			boolean printJSONConfig, boolean flagLogVariableMapping, boolean flagBasicCount) throws Exception 
	{
		FileInputStream fin = new FileInputStream(excelFile);
		try {
			boolean isXLSX = excelFile.endsWith("xlsx");
			System.out.println("{"); //opening json bracket
			
			System.out.println("\"isXLSX\" : " + isXLSX + ",\n");
			GenericExcelParser parser = new GenericExcelParser(fin, jsonFile,
					isXLSX);
			
			parser.setFlagVariableMappingLogging(flagLogVariableMapping);
			
			if (printJSONConfig)
				System.out.println(parser.getExcelParserConfigurator()
						.toJSONString() + "\n");
			
			int n = 0;
			List<SubstanceRecord> recordList = new ArrayList<SubstanceRecord>();
			
			while (parser.hasNext()) 
			{
				SubstanceRecord r = parser.nextRecord();
				if (flagBasicCount)
					recordList.add(r);
				n++;
				if (n > 1)
					System.out.println(",");
				
				System.out.println( "\"Record #" + n + "\" : {");
				System.out.print( "\"record data\" : ");
				System.out.println( r.toJSON(null));
				
				List<ProtocolApplication> paList = r.getMeasurements();
				if (paList != null)
				{	
					int nPA = 0;
					System.out.println(",");
					for (ProtocolApplication pa : paList)
					{	
						nPA++;
						if (nPA > 1)
							System.out.println(",");
						
						System.out.print( "\"Protocol application " + nPA + "\" :\n"
								+ pa.toString() );
					}
				}	

				
				List<CompositionRelation> composition = r
						.getRelatedStructures();
				
				if (composition != null)
				{	
					for (CompositionRelation relation : composition) {
						// System.out.println(" ### Composition " +
						// structureRecordToString(relation.getSecondStructure()));
						System.out.println(",");
						System.out.println(								
								"\"### Composition\" : "
										+ NMParserTestUtils.compositionRelationStructureToJsonString(relation)); 
												// both give the same result
						
						System.out.println(",");
						System.out.println(
								"\"### Properties\" : "
										+ NMParserTestUtils.structureRecordPropertiesToJsonString(relation										
												.getSecondStructure()));
					}
				}
				
				System.out.println("}"); //closing record bracket

			}
			
			System.out.println("}"); //closing entire json bracket

			/*
			 * we'll get the parser errors logged or exceptions thrown if
			 * (parser.hasErrors()) System.out.println("\n\nParser errors:\n" +
			 * parser.errorsToString());
			 */
			
			if (flagBasicCount) {
				System.out.println("\nBasic count:");
				System.out.println("--------------");	
				System.out.println("Number of records = " + recordList.size());
				
				SubstanceAnalysisTask sat = SubstanceAnalysisTask.parseFromString("BASIC_COUNT");
				sat.flagConsoleOutOnly = true;
				sat.run(recordList);
			}
			
			if (substanbceAggregator != null) {
				System.out.println("\nRunning Substance Aggregation ...");
				//TODO
			}
				

			
		} finally {

			fin.close();
		}
	}

}
