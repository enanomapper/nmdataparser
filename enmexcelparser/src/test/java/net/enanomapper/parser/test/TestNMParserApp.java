package net.enanomapper.parser.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.relation.composition.CompositionRelation;
import net.enanomapper.parser.GenericExcelParser;

public class TestNMParserApp {

	public static void main(String[] args) throws Exception
	{
		if (args.length < 2)
			System.out.println("CLI Arguments: <excel file>  <json file>");
		else
			testExcelTemplate(args[0], new File(args[1]), false);
	}
	
	public static void testExcelTemplate(String excelFile, File jsonFile, boolean printJSONConfig)
			throws Exception {
		FileInputStream fin = new FileInputStream(excelFile);
		try {
			boolean isXLSX = excelFile.endsWith("xlsx");
			System.out.println("isXLSX = " + isXLSX + "\n");
			GenericExcelParser parser = new GenericExcelParser(fin, jsonFile,
					isXLSX);
			if (printJSONConfig)
				System.out.println(parser.getExcelParserConfigurator()
						.toJSONString() + "\n");

			int n = 0;
			while (parser.hasNext()) {
				SubstanceRecord r = parser.nextRecord();
				n++;
				System.out.println( "Record #" + n);
				System.out.println( r.toJSON(null));
				List<ProtocolApplication> paList = r.getMeasurements();

				if (paList != null)
					for (ProtocolApplication pa : paList)
						System.out.println( "***Protocol application:\n"
								+ pa.toString());

				List<CompositionRelation> composition = r
						.getRelatedStructures();
				if (composition != null)
					for (CompositionRelation relation : composition) {
						// System.out.println(" ### Composition " +
						// structureRecordToString(relation.getSecondStructure()));
						System.out.println(								
								" ### Composition \n"
										+ NMParserTestUtils.compositionRelationStructureToString(relation)); 
												// both give the same result
						System.out.println(
								" ### Properties: "
										+ NMParserTestUtils.structureRecordProperties(relation
												.getSecondStructure()));
					}

			}

			/*
			 * we'll get the parser errors logged or exceptions thrown if
			 * (parser.hasErrors()) System.out.println("\n\nParser errors:\n" +
			 * parser.errorsToString());
			 */
		} finally {

			fin.close();
		}

	}

}
