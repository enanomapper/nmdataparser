package net.enanomapper.parser.test;

import org.openscience.cdk.tools.LoggingTool;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class NMParserTest extends TestCase
{
	public LoggingTool logger;
	
	public NMParserTest() 
	{   
		logger = new LoggingTool(this);
	}
	
	public static Test suite() {
		return new TestSuite(NMParserTest.class);
	}
	
	public void testTest1() throws Exception 
	{
		//TODO
	}
}
