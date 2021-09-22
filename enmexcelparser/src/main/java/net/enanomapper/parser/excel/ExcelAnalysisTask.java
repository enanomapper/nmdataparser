package net.enanomapper.parser.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import net.enanomapper.parser.excel.MiscUtils.IHandleFile;

public class ExcelAnalysisTask 
{
	public static final String mainSplitter = ";";
	public static final String secondarySplitter = ",";
	
	
	public static enum TaskType {
		COMPARE_FILES, CHECK_VALUE, COUNT, PRINT_VALUE, UNDEFINED;
		
		public static TaskType fromString(String s) {
			try {
				TaskType type = TaskType.valueOf(s);
				return (type);
			} catch (Exception e) {
				return TaskType.UNDEFINED;
			}
		}		
	}
	
	class BasicFileHandler implements IHandleFile 
	{
		@Override
		public void handle(File file) throws Exception 
		{
			//Simple processing for folders
			if (file.isDirectory())
			{
				analysisResult.add("Processing folder: " + file.getAbsolutePath());
				return;
			}
			
			int curWorkbookRes = createCurrentWorkbook(file);
			if (curWorkbookRes != 0){
				closeCurIterationFileStreem();
				return;
			}
			
			analysisResult.add("File: " + file.getAbsolutePath());
			//TODO ... main processing goes here
			
			closeCurIterationFileStreem();				
		}
	}
	
	public TaskType type = TaskType.UNDEFINED;
	public ExcelScope excelScope = null;
	public Object params[] = null;
	public File target1 = null;
	public File target2 = null;
	public boolean flagVerboseResult = false;
	public boolean flagFileRecursion = true;
	
	public List<String> analysisResult = new ArrayList<String>();
	public List<String> analysisErrors = new ArrayList<String>();
		
	//work variables
	public File referenceFile = null;
	public File iterationFile = null;
	public FileInputStream referenceFileInput = null;
	public FileInputStream curIterationFileInput = null;
	public Workbook refWorkbook = null;
	public Workbook curWorkbook = null;	
			
	/*
	 * Parsing an ExcelAnalysisTask from a string in the following format
	 * <task type>; <scope>; <params>; <target1>; <target2>
	 */
	public static ExcelAnalysisTask parseFromString(String taskStr) throws Exception 
	{
		List<String> errors = new ArrayList<String>();
		ExcelAnalysisTask eaTask = new ExcelAnalysisTask();		
		String tokens[] = taskStr.split(mainSplitter);
		
		//Task type
		if (tokens.length < 1)
			errors.add("Missing task type token!");
		else {	
			eaTask.type = TaskType.fromString(tokens[0].trim());
			if (eaTask.type == TaskType.UNDEFINED)
				errors.add("Incorrect excel analysis task type: " + tokens[0]);
		}
		
		//Excel Scope
		if (tokens.length < 2)
			errors.add("Missing excel scope token!");
		else {	
			String scopeStr = tokens[1].trim();
			try {
				eaTask.excelScope = ExcelScope.parseFromString(scopeStr);
			}
			catch (Exception x) {
				errors.add("Incorrect excel scope: " + x.getMessage());
			}
		}
		
		//Excel params
		if (tokens.length < 3)
			errors.add("Missing parameters token!");
		else {	
			String paramsStr = tokens[2].trim();
			
			if (!paramsStr.equals("--") && !paramsStr.equals("no params"))
			{						
				String paramTokens[] = paramsStr.split(secondarySplitter);			

				if (paramTokens.length > 0) 
				{
					eaTask.params = new Object[paramTokens.length];

					for (int i = 0; i < paramTokens.length; i++) {
						String par = paramTokens[i].trim();
						if (par.isEmpty())
							errors.add("Parameter #" + (i+1) + " is empty!");

						Object o = par;
						try {
							Double d = Double.parseDouble(par);
							o = d;
						}
						catch (Exception x) {
						}
						eaTask.params[i] = o;
					}
				}
			}
		}
		
		//Target1 (file path)		
		if (tokens.length < 4)
			errors.add("Missing target 1 token!");
		else {	
			String path = tokens[3].trim();
			eaTask.target1 = new File(path);
			if (!eaTask.target1.exists()) {
				errors.add("Target1 file path: " + path + " does not exists!");
				eaTask.target1 = null;
			}	
		}
		
		//Target2 (file path)		
		if (tokens.length >=5 )
		{	
			String tok4 = tokens[4].trim();
			int specTokRes = checkForSpecialToken(tok4, eaTask,  errors);
			if (specTokRes == -1)
			{	
				//Token 4 is not a special key word token
				eaTask.target2 = new File(tok4);
				if (!eaTask.target2.exists()) {
					errors.add("Target2 file path: " + tok4 + " does not exists!");
					eaTask.target2 = null;
				}	
			}
		}
		
		
		//Set Reference and Iterate List Files 
		if (eaTask.type == TaskType.COMPARE_FILES) 
		{
			if (eaTask.target2 == null) 
			{
				//The reference file is the first file from the target1 list
				if (eaTask.target1 != null) 
				{
					if (eaTask.target1.isFile()) 
						errors.add("Target1 file path " + eaTask.target1.getAbsolutePath() 
						+ " is not a folder and Target2 is not specified for TaskType.COMPARE_FILES!");
					else {
						eaTask.referenceFile = MiscUtils.getFirstFile(eaTask.target1, new String[] {"xls", "xlsx"}, true);
						if (eaTask.referenceFile == null)
							errors.add("Target1 file path " + eaTask.target1.getAbsolutePath() 
							+ " folder does not contain at least one file needed as ref. file for TaskType.COMPARE_FILES!");
						
						eaTask.iterationFile = eaTask.target1;
					}
				}
			}
			else 
			{
				if (eaTask.target1 != null) {
					//The reference file is target1
					if (eaTask.target1.isFile())
						eaTask.referenceFile = eaTask.target1;
					else
						errors.add("Target1 file path " + eaTask.target1.getAbsolutePath() 
						+ " is a folder and cannot be used as a refernce file for TaskType.COMPARE_FILES!");
				}
				//The iteration files folder is target2 (it might be only a single file)
				if (eaTask.target2 != null) 					
					eaTask.iterationFile = eaTask.target2;
			}
		}
		else
		{
			//All other task types use only eaTask.iterationFile 
			if (eaTask.target1 != null)
				eaTask.iterationFile = eaTask.target1;
		}
		
		
		if (!errors.isEmpty()) {
			StringBuffer errBuffer = new StringBuffer();
			for (String err: errors)
				errBuffer.append(err + "\n");
			throw new Exception (errBuffer.toString());
		}		
		
		return eaTask;
	}
		

	public static int checkForSpecialToken(String token, ExcelAnalysisTask eaTask, List<String> errors) 
	{
		if (token.equalsIgnoreCase("verbose"))
		{	
			eaTask.flagVerboseResult = true;
			return 0;
		}
		
		if (token.equalsIgnoreCase("file_recursion"))
		{	
			eaTask.flagFileRecursion = true;
			return 0;
		}
		
		return -1;
	}
	
		
	public int run() throws Exception
	{
		analysisResult.clear();
		analysisErrors.clear();
		
		switch (type) {
		case COMPARE_FILES:
			return compareFiles();			
		case CHECK_VALUE:
			return checkValue();
		case COUNT:
			return count();
		case PRINT_VALUE:
			return printValue();
			
		}
		return -1;
	}
		
	int compareFiles() throws Exception
	{
		//Set up reference input file stream and corresponding excel workbook
		int refFileRes = openRefernceFileStreem(referenceFile);
		if (refFileRes != 0)
		{
			closeRefernceFileStreem();
			return -1;
		}
		
		boolean isRefXLSX = ExcelUtils.isXLSXFormat(referenceFile);
		refWorkbook = null;		
		try {
			refWorkbook = ExcelUtils.getExcelReader(referenceFileInput, isRefXLSX);
		} 
		catch(Exception x) {
			analysisErrors.add("Could not create workbook object for reference excel file: " 
					+ referenceFile.getAbsolutePath());
			closeRefernceFileStreem();
			return -1;
		}
		
		/*
		class FileHandler implements IHandleFile 
		{
			@Override
			public void handle(File file) throws Exception 
			{
				//Simple processing for folders
				if (file.isDirectory())
				{
					analysisResult.add("Processing folder: " + file.getAbsolutePath());
					return;
				}
				
				int curWorkbookRes = createCurrentWorkbook(file);
				if (curWorkbookRes != 0){
					closeCurIterationFileStreem();
					return;
				}
				//TODO ... main processing goes here
				
				closeCurIterationFileStreem();				
			}
		}
		*/
		
		try {
			MiscUtils.iterateFiles_BreadthFirst(iterationFile, new String[] {"xlsx", "xls" }, 
					flagFileRecursion, new BasicFileHandler(), true);
		}
		catch (Exception x) {
		} 
		finally {
			closeRefernceFileStreem();
		}	
		
		return 0;
	}
	
	int checkValue() 
	{
		//TODO
		return 1;
	}
	
	int count() 
	{
		//TODO
		return 2;
	}
	
	int printValue() 
	{
		//TODO
		return 3;
	}
	
	int createCurrentWorkbook(File file) 
	{
		//Set up current iteration input file stream and corresponding excel workbook
		int curFileRes = openCurIterationFileStreem(file);
		if (curFileRes != 0)
		{
			closeCurIterationFileStreem();
			return -1;
		}
		
		boolean isXLSX = ExcelUtils.isXLSXFormat(file);
			
		try {
			curWorkbook = ExcelUtils.getExcelReader(curIterationFileInput, isXLSX);
		} 
		catch(Exception x) {
			analysisErrors.add("Could not create workbook object for current iteration excel file: " 
					+ file.getAbsolutePath());
			closeCurIterationFileStreem();
			return -2;
		}
		return 0;
	}
	
	int openRefernceFileStreem(File file) {
		try {
			referenceFileInput = new FileInputStream(file);
			return 0;
		} catch (Exception x) {
			analysisErrors.add("Error on creating Reference File Input stream: " 
				+ file.getAbsolutePath());
			return -1;
		}
	}
	
	int closeRefernceFileStreem() {
		try {
			if (referenceFileInput != null)
				referenceFileInput.close();
			return 0;
		} catch (Exception x) {
			analysisErrors.add("Error on closing Reference File Input stream!");
			return -1;
		}
	}
	
	int openCurIterationFileStreem(File file) {
		try {
			curIterationFileInput = new FileInputStream(file);
			return 0;
		} catch (Exception x) {
			analysisErrors.add("Error on creating Current Iteration File Input stream: " 
				+ file.getAbsolutePath());
			return -1;
		}
	}
	
	int closeCurIterationFileStreem() {
		try {
			if (curIterationFileInput != null)
				curIterationFileInput.close();
			return 0;
		} catch (Exception x) {
			analysisErrors.add("Error on closing Current Iteration File Input stream!");
			return -1;
		}
	}
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Type: " + type + "\n");
		sb.append("Verbose: " + flagVerboseResult + "\n");
		sb.append("File recursion: " + flagFileRecursion + "\n");
		
		sb.append("Excel Scope: ");
		for (int i = 0; i < excelScope.cellRanges.size(); i++)
		{			
			CellRangeAddress cra = excelScope.cellRanges.get(i); 
			sb.append(cra.formatAsString() + " Sheet ");
			if (excelScope.sheetIndices.get(i) != null)
				sb.append("#" + (excelScope.sheetIndices.get(i)+1));
			else
				sb.append(excelScope.sheetNames.get(i));
		}	
		sb.append("\n");
		
		if (params != null) {
			sb.append("Params: ");
			for (int i = 0; i < params.length; i++)
				sb.append(params[i] + " ");
			sb.append("\n");
		}		
		
		if (target1 != null)
			sb.append("Target1:" + target1.getAbsolutePath() + "\n");
		if (target2 != null)
			sb.append("Target2:" + target2.getAbsolutePath() + "\n");
		
		return sb.toString();
	}
}
