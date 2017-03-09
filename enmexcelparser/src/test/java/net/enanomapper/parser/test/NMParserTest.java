package net.enanomapper.parser.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import net.enanomapper.parser.GenericExcelParser;

import org.openscience.cdk.tools.LoggingTool;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.relation.composition.CompositionRelation;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class NMParserTest extends TestCase
{
	public LoggingTool logger;
	
	public NMParserTest() throws Exception
	{   
		logger = new LoggingTool(this);
		//initFileData();
	}
	
	void initFileData() throws Exception
	{
		InputStream fin = null;
		try {
			boolean isXLSX = true;
			System.out.println("isXLSX = " + isXLSX + "\n");

			fin = getClass().getClassLoader()
					.getResourceAsStream("net/enanomapper/parser/testExcelParser/testfile1.xlsx");
			
			
			URL url = getClass().getClassLoader()
					.getResource("net/enanomapper/parser/testExcelParser/testfile1-config.json");
			GenericExcelParser parser = new GenericExcelParser(fin, new File(url.getFile()), isXLSX);
			
			

			//System.out.println(parser.getExcelParserConfigurator().toJSONString() + "\n");

			int n = 0;
			while (parser.hasNext()) 
			{
				SubstanceRecord r = parser.nextRecord();
				n++;
				System.out.println("Record #" + n);
				/*
				System.out.println(r.toJSON(null));
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
										+ NMParserTestUtils.compositionRelationStructureToString(relation)); // both give
																							// the same result
						System.out.println(
								" ### Properties: "
										+ NMParserTestUtils.structureRecordProperties(relation
												.getSecondStructure()));
					}

				 */	
			}
		}	
		finally {

			if (fin != null)
				fin.close();
		}
	}
	
	public static Test suite() {
		return new TestSuite(NMParserTest.class);
	}
	
	public void testTest1() throws Exception 
	{
		//TODO
	}
}
