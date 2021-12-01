package net.enanomapper.parser.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

import net.enanomapper.parser.excel.ExcelUtils.CellComparisonMethod;
import net.enanomapper.parser.excel.ExcelUtils.IHandleExcelAddress;
import net.enanomapper.parser.excel.MiscUtils.IHandleFile;
import net.enanomapper.parser.excel.SALogicalCondition.ComparisonOperation;

public class ExcelAnalysisTask 
{
	public static final String mainSplitter = ";";
	public static final String secondarySplitter = ",";
	
	
	public static enum TaskType {
		COMPARE_FILES, CHECK_VALUE, COUNT, PRINT_VALUE, GENERATE_JSON_CONFIG, UNDEFINED;
		
		public static TaskType fromString(String s) {
			try {
				TaskType type = TaskType.valueOf(s);
				return (type);
			} catch (Exception e) {
				return TaskType.UNDEFINED;
			}
		}		
	}
	
	public static enum JSONGenerationElement {
		PARAMETER, UNDEFINED;
		
		public static JSONGenerationElement fromString(String s) {
			try {
				JSONGenerationElement jge = JSONGenerationElement.valueOf(s);
				return (jge);
			} catch (Exception e) {
				return JSONGenerationElement.UNDEFINED;
			}
		}		
	}
	
	class JSONGenerationInfo 
	{
		public JSONGenerationElement jsonElement = JSONGenerationElement.PARAMETER;
		String jsonOffset = "\t\t";
		public int sheetNumber = 1;
		public int rowShift = 0;
		public int columnShift = 1;
	}
	
	class BasicFileHandler implements IHandleFile 
	{
		@Override
		public void handle(File file) throws Exception 
		{
			//Reference file is omitted
			if (referenceFile != null)
				if (referenceFile.equals(file))
				{
					outputLine("Reference file is not processed: " + file.getAbsolutePath());
					return;
				}	
			
			//Simple processing for folders
			if (file.isDirectory())
			{
				outputLine("Processing folder: " + file.getAbsolutePath());
				return;
			}
			
			int curWorkbookRes = createCurrentWorkbook(file);
			if (curWorkbookRes != 0){
				closeCurIterationFileStreem();
				return;
			}
			
			outputLine("File: " + file.getAbsolutePath());
			
			//main processing goes here
			ExcelUtils.iterateExcelScope(excelScope, curWorkbook, curExcelHandler);
			
			closeCurIterationFileStreem();				
		}
	}
	
	public TaskType type = TaskType.UNDEFINED;
	public ExcelScope excelScope = null;
	public String qualifier = null;
	public ComparisonOperation comparison = ComparisonOperation.UNDEFINED; //determined from the qualifier
	public Object params[] = null;
	public File target1 = null;
	public File target2 = null;
	public boolean flagVerboseResult = false;
	public boolean flagFileRecursion = true;
	public boolean flagConsoleOut = false;
	public boolean flagConsoleOutOnly = false;
	public boolean flagIgnoreCase = true;
	
	public List<String> analysisResult = new ArrayList<String>();
	public List<String> analysisErrors = new ArrayList<String>();
	public int analysisStatTotalOKNum = 0;
	public int analysisStatTotalProblemNum = 0;
		
	//work variables
	public File referenceFile = null;
	public File iterationFile = null;
	public FileInputStream referenceFileInput = null;
	public FileInputStream curIterationFileInput = null;
	public Workbook refWorkbook = null;
	public Workbook curWorkbook = null;
	public IHandleExcelAddress curExcelHandler = null;
			
	/*
	 * Parsing an ExcelAnalysisTask from a string in the following format
	 * <task type + qualifier>; <params>; <scope>; <target1>; <target2>
	 */
	public static ExcelAnalysisTask parseFromString(String taskStr) throws Exception 
	{
		List<String> errors = new ArrayList<String>();
		ExcelAnalysisTask eaTask = new ExcelAnalysisTask();		
		String tokens[] = taskStr.split(mainSplitter);
		
		//Task type + qualifier 
		if (tokens.length < 1)
			errors.add("Missing task type token!");
		else {	
			String ss0 = tokens[0].trim();
			if (ss0.isEmpty()) {
				errors.add("Empty string for excel analysis task type!");
			}
			else {
				String subTokens[] = ss0.split(",");
				eaTask.type = TaskType.fromString(subTokens[0]);
				if (eaTask.type == TaskType.UNDEFINED)
					errors.add("Incorrect excel analysis task type: " + subTokens[0]);
				//Handle qualifier
				if (subTokens.length > 1) {
					eaTask.qualifier = subTokens[1].trim();
					eaTask.comparison = SALogicalCondition.qualifierToComparisonOperation(eaTask.qualifier);
					if (eaTask.comparison == ComparisonOperation.UNDEFINED)
						errors.add("Incorrect qualifier: " + eaTask.qualifier);
				}
								
				//Check for extra tokens
				if (subTokens.length > 2) {
					for (int i = 2; i < subTokens.length; i++ )
						if (!subTokens[i].isEmpty())
							errors.add("Extra token '" + subTokens[i] + "' in excel type section: " + ss0);
				}	
			}
		}
		
		
		
		//Excel params
		if (tokens.length < 2)
			errors.add("Missing parameters token!");
		else {	
			String paramsStr = tokens[1].trim();
			
			if (!paramsStr.isEmpty() && !paramsStr.equals("--") && !paramsStr.equals("no params"))
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
		
		//Excel Scope
		if (tokens.length < 3)
			errors.add("Missing excel scope token!");
		else {	
			String scopeStr = tokens[2].trim();
			try {
				eaTask.excelScope = ExcelScope.parseFromString(scopeStr);
			}
			catch (Exception x) {
				errors.add("Incorrect excel scope: " + x.getMessage());
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
		
		//Target2 (file path and/or special tokens)		
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
			
			for (int i = 5; i < tokens.length; i++)
			{
				String spec_tok = tokens[i].trim();
				if (spec_tok.isEmpty())
					continue;
				
				int specTokRes2 = checkForSpecialToken(spec_tok, eaTask,  errors);
				if (specTokRes2 == -1)
					errors.add("Incorrect special word/flag: " + spec_tok);
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
		if (token.startsWith("#"))
		{
			//This token is omitted ('#' symbol makes it a comment token)
			return 0;
		}
		
		if (token.equalsIgnoreCase("verbose"))
		{	
			eaTask.flagVerboseResult = true;
			return 0;
		}
		
		if (token.equalsIgnoreCase("file_recursion") || token.equalsIgnoreCase("recursion") )
		{	
			eaTask.flagFileRecursion = true;
			return 0;
		}
		
		if (token.equalsIgnoreCase("no_file_recursion") || token.equalsIgnoreCase("no_recursion") )
		{	
			eaTask.flagFileRecursion = false;
			return 0;
		}
		
		if (token.equalsIgnoreCase("console_out"))
		{	
			eaTask.flagConsoleOut = true;
			return 0;
		}
		
		if (token.equalsIgnoreCase("console_out_only"))
		{	
			eaTask.flagConsoleOutOnly = true;
			return 0;
		}
		
		return -1;
	}
	
	void outputLine(String line) {
		if (flagConsoleOutOnly)
			System.out.println(line);
		else
		{
			analysisResult.add(line);
			if (flagConsoleOut)
				System.out.println(line);
		}
	}
	
	public int run() throws Exception
	{
		analysisResult.clear();
		analysisErrors.clear();
		analysisStatTotalOKNum = 0;
		analysisStatTotalProblemNum = 0;
		
		switch (type) {
		case COMPARE_FILES:
			return compareFiles();			
		case CHECK_VALUE:
			return checkValue();
		case COUNT:
			return count();
		case PRINT_VALUE:
			return printValue();
		case GENERATE_JSON_CONFIG:
			return generateJsonConfig();
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
		
		class ExcelAddressHandler implements IHandleExcelAddress {
			@Override
			public void handle(CellAddress cellAddr, Sheet sheet, int cellRangeIndex) throws Exception 
			{				
				if (sheet == null || cellAddr == null)
					return;
				
				//Get reference sheet and cell
				//cellRangeIndex is needed to get the reference sheet 				
				Integer sheetIndex = excelScope.sheetIndices.get(cellRangeIndex); 
				String sheetName = excelScope.sheetNames.get(cellRangeIndex);
				Sheet refSheet = ExcelUtils.getSheet(refWorkbook, sheetIndex, sheetName);
				Row refRow = refSheet.getRow(cellAddr.getRow());
				Cell refCell = null;
				if (refRow  != null)				
					refCell = refRow.getCell(cellAddr.getColumn());
								
				//Cell comparison is performed only for those reference cells 
				//which comply parameters conditions
				boolean paramCheckRes = ExcelUtils.checkConditionForCell(refCell, comparison, flagIgnoreCase, params);
				if (paramCheckRes) 
				{	
					//Get current cell
					Cell cell = null;
					Row row = sheet.getRow(cellAddr.getRow());
					if (row  != null)					
						cell = row.getCell(cellAddr.getColumn());
					
					int cellCoparisonRes = ExcelUtils.compareCells(refCell, cell, CellComparisonMethod.IDENTICAL);
					
					if (cellCoparisonRes == 0) {
						analysisStatTotalOKNum++;
						if (flagVerboseResult)						
							outputLine(cellAddr.formatAsString() + ": OK");
					}
					else {
						analysisStatTotalProblemNum++;
						String errorMessage = ExcelUtils.getComparisonErrorMessage(cellCoparisonRes, refCell, cell);
						outputLine("Problem: " + cellAddr.formatAsString() + ": " + errorMessage);
					}
				}	
			}
			
			@Override
			public void handle(CellRangeAddress cellRangeAddr, Sheet sheet, int cellRangeIndex) throws Exception {
				if (flagVerboseResult)
					outputLine("Range " + cellRangeAddr.formatAsString() + " cell range index = " + (cellRangeIndex+1));
			}
		}
		
		curExcelHandler = new ExcelAddressHandler(); 
		
		try {
			MiscUtils.iterateFiles_BreadthFirst(iterationFile, new String[] {"xlsx", "xls" }, 
					flagFileRecursion, new BasicFileHandler(), true);
			outputLine("Total number of OK checks: " + analysisStatTotalOKNum);
			outputLine("Total number of problems: " + analysisStatTotalProblemNum);
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
		class ExcelAddressHandler implements IHandleExcelAddress {
			@Override
			public void handle(CellAddress cellAddr, Sheet sheet, int cellRangeIndex) throws Exception {
				if (sheet == null || cellAddr == null)
					return;
				Row row = sheet.getRow(cellAddr.getRow());
				if (row  != null)
				{
					Cell cell = row.getCell(cellAddr.getColumn());
					boolean checkRes = ExcelUtils.checkConditionForCell(cell, comparison, flagIgnoreCase, params);					
					if (checkRes)
					{
						analysisStatTotalOKNum++;
						if (flagVerboseResult)						
							outputLine(cellAddr.formatAsString() + ": OK");
					}
					else 
					{	
						analysisStatTotalProblemNum++;
						outputLine("Problem: " + cellAddr.formatAsString() + ": " 
								+ ((cell!=null)?ExcelUtils.getStringFromCell(cell):"null") );
					}	
				}
				else {
					analysisStatTotalProblemNum++;
					outputLine("Problem: " + cellAddr.formatAsString() + ": row null");
				}
			}
			@Override
			public void handle(CellRangeAddress cellRangeAddr, Sheet sheet, int cellRangeIndex) throws Exception {
				//do nothing
			}
		}
		
		curExcelHandler = new ExcelAddressHandler();
		
		try {
			MiscUtils.iterateFiles_BreadthFirst(iterationFile, new String[] {"xlsx", "xls" }, 
					flagFileRecursion, new BasicFileHandler(), true);			
			outputLine("Total number of OK checks: " + analysisStatTotalOKNum);
			outputLine("Total number of problems: " + analysisStatTotalProblemNum);
		}
		catch (Exception x) {
			analysisErrors.add(x.getMessage());
		} 
		
		return 1;
	}
	
	int count() 
	{
		//TODO
		return 2;
	}
	
	int printValue() 
	{
		class ExcelAddressHandler implements IHandleExcelAddress {
			@Override
			public void handle(CellAddress cellAddr, Sheet sheet, int cellRangeIndex) throws Exception {
				if (sheet == null || cellAddr == null)
					return;
				Row row = sheet.getRow(cellAddr.getRow());
				if (row  != null)
				{
					Cell cell = row.getCell(cellAddr.getColumn());
					boolean checkRes = ExcelUtils.checkConditionForCell(cell, comparison, flagIgnoreCase, params);
					if (checkRes)
					{	
						analysisStatTotalOKNum++;
						outputLine(cellAddr.formatAsString() + ": " 
								+ ((cell!=null)?ExcelUtils.getStringFromCell(cell):"null") );
					}
					else {
						analysisStatTotalProblemNum++;
					}
				}
			}
			@Override
			public void handle(CellRangeAddress cellRangeAddr, Sheet sheet, int cellRangeIndex) throws Exception {
				//do nothing
			}
		}
		
		curExcelHandler = new ExcelAddressHandler(); 
		
		try {
			MiscUtils.iterateFiles_BreadthFirst(iterationFile, new String[] {"xlsx", "xls" }, 
					flagFileRecursion, new BasicFileHandler(), true);
			outputLine("Total number of OK checks: " + analysisStatTotalOKNum);
			outputLine("Total number of problems: " + analysisStatTotalProblemNum);
		}
		catch (Exception x) {
			analysisErrors.add(x.getMessage());
		} 
		return 3;
	}
	
	
	int generateJsonConfig() 
	{
		JSONGenerationInfo jsonInfo = getJSONGenerationInfoFromParams();
		if (!analysisErrors.isEmpty())
			return -1;
		
		class ExcelAddressHandler implements IHandleExcelAddress {
			@Override
			public void handle(CellAddress cellAddr, Sheet sheet, int cellRangeIndex) throws Exception {
				if (sheet == null || cellAddr == null)
					return;
				Row row = sheet.getRow(cellAddr.getRow());
				if (row  != null)
				{
					Cell cell = row.getCell(cellAddr.getColumn());
					if (cell != null) {						
						String jsonSection = generateJsonSection (cellAddr, sheet, cell, jsonInfo);
						outputLine(jsonSection + ",");
					}
				}
			}
			@Override
			public void handle(CellRangeAddress cellRangeAddr, Sheet sheet, int cellRangeIndex) throws Exception {
				//do nothing
			}
		}
		
		curExcelHandler = new ExcelAddressHandler(); 
		
		try {
			MiscUtils.iterateFiles_BreadthFirst(iterationFile, new String[] {"xlsx", "xls" }, 
					flagFileRecursion, new BasicFileHandler(), true);
			outputLine("Total number of OK checks: " + analysisStatTotalOKNum);
			outputLine("Total number of problems: " + analysisStatTotalProblemNum);
		}
		catch (Exception x) {
			analysisErrors.add(x.getMessage());
		} 
		return 4;
	}
	
	String generateJsonSection (CellAddress cellAddr, Sheet sheet, 
				Cell cell, JSONGenerationInfo jsonInfo)
	{
		StringBuffer sb = new StringBuffer();
		
		String endLine = "\n"; 
		if (jsonInfo.jsonElement == JSONGenerationElement.PARAMETER)
		{
			sb.append(jsonInfo.jsonOffset);
			sb.append("\"" + ExcelUtils.getStringFromCell(cell) + "\": {" + endLine);
			
			sb.append(jsonInfo.jsonOffset + "\t");
			sb.append("\"ITERATION\": \"ABSOLUTE_LOCATION\"," + endLine);
			
			sb.append(jsonInfo.jsonOffset + "\t");
			sb.append("\"SHEET_INDEX\": " + jsonInfo.sheetNumber + "," + endLine);
			
			int column = cellAddr.getColumn() + jsonInfo.columnShift;
			String columnStr = CellReference.convertNumToColString(column);
			sb.append(jsonInfo.jsonOffset + "\t");
			sb.append("\"COLUMN_INDEX\": \"" + columnStr + "\"," + endLine);
			
			int row = cellAddr.getRow() + jsonInfo.rowShift;
			sb.append(jsonInfo.jsonOffset + "\t");
			sb.append("\"ROW_INDEX\": " + row + endLine);
			
			sb.append(jsonInfo.jsonOffset);
			sb.append("}");
		}
		
		return sb.toString();
	}
	
	JSONGenerationInfo getJSONGenerationInfoFromParams()
	{
		JSONGenerationInfo jsonInfo = new JSONGenerationInfo();
		
		if (params == null)
		{
			//default task is generated
			return jsonInfo;
		}
		
		//Handle parameters
		for (int i = 0; i < params.length; i++)
		{
			String keyval[] = getKeyValuePair(params[i].toString());
			if (keyval == null)
			{	
				analysisErrors.add("Incorrect parameter for JSON Generation: " + params[i]);
				continue;
			}
			String key = keyval[0];
			String val = keyval[1];
			
			if (key.equalsIgnoreCase("offset"))
			{
				try {
					Integer nTabs = Integer.parseInt(val);
					if (nTabs >= 0)
					{
						String s = "";
						for (int k = 0; k < nTabs; k++)
							s += "\t";
						jsonInfo.jsonOffset = s;
					}
					else
						analysisErrors.add("Incorrect parameter for JSON Generation: " + params[i]);
				}
				catch (Exception e) {
					analysisErrors.add("Incorrect parameter for JSON Generation: " + params[i]);
				}
			}
		}
			
		return jsonInfo;
	}
	
	
	String[] getKeyValuePair(String token)
	{
		if (token.isEmpty())
			return null;
		
		int pos = token.indexOf("=");
		if (pos == -1)
		{   
			return null;
		}

		String key = token.substring(0, pos).trim();
		String value = token.substring(pos+1).trim();
		
		if (key.isEmpty() || value.isEmpty())
			return null;
		
		return new String[] {key, value};
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
		if (qualifier != null)
			sb.append("Qualifier: " + qualifier + "\n");
		
		sb.append("Verbose: " + flagVerboseResult + "\n");
		sb.append("File recursion: " + flagFileRecursion + "\n");
		if (flagConsoleOutOnly)
			sb.append("Console out only: " + flagConsoleOutOnly + "\n");
		else
			sb.append("Console out: " + flagConsoleOut + "\n");
		
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
