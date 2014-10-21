package net.enanomapper.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.IChemObjectReaderErrorHandler;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.listener.IChemObjectIOListener;
import org.openscience.cdk.io.setting.IOSetting;

import ambit2.base.data.SubstanceRecord;
import ambit2.core.io.IRawReader;


/**
 * 
 * @author nick
 *
 */
public class GenericExcelParser implements IRawReader<SubstanceRecord>
{
	
	private ArrayList<String> parseErrors = new ArrayList<String> ();
	private ExcelParserConfigurator parserConfig = null;
	
	public GenericExcelParser(String excelFileName, String jsonConfig) throws Exception
	{
		parserConfig = ExcelParserConfigurator.loadFromJSON(jsonConfig);
		if (parserConfig.configErrors.size() > 0)		
			throw new Exception("GenericExcelParser configuration errors:\n" + parserConfig.getAllErrorsAsString());
		
		init();
		
	}
	
	public void init() 
	{
		//TODO
		
	}


	@Override
	public void handleError(String arg0) throws CDKException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void handleError(String arg0, Exception arg1) throws CDKException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void handleError(String arg0, int arg1, int arg2, int arg3)
			throws CDKException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void handleError(String arg0, int arg1, int arg2, int arg3,
			Exception arg4) throws CDKException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setErrorHandler(IChemObjectReaderErrorHandler arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setReader(Reader arg0) throws CDKException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setReader(InputStream arg0) throws CDKException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setReaderMode(Mode arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean accepts(Class<? extends IChemObject> arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void addChemObjectIOListener(IChemObjectIOListener arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public IResourceFormat getFormat() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public IOSetting[] getIOSettings() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void removeChemObjectIOListener(IChemObjectIOListener arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Object next() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public SubstanceRecord nextRecord() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
