package net.enanomapper.parser;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.enanomapper.parser.ParserConstants.DynamicIteration;
import net.enanomapper.parser.ParserConstants.IterationAccess;
import net.enanomapper.parser.ParserConstants.Recognition;
import net.enanomapper.parser.ParserConstants.SheetSynchronization;
import net.enanomapper.parser.json.JsonUtilities;
import net.enanomapper.parser.recognition.RecognitionUtils;

import org.apache.poi.hssf.util.CellReference;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import ambit2.base.data.substance.ExternalIdentifier;

/**
 * 
 * @author nick
 *	Internally all indices/numbers/ of rows, columns and sheets are represented as 0-based integers
 *  while in the JSON configuration and error messages they are 1-based represented (user-friendly style) 
 *  The conversion from 1-based to 0-based and vice versa is done on "parsing", error message generation and toJSON() procedures respectively.
 */
public class ExcelParserConfigurator 
{	
	private final static Logger logger = Logger.getLogger(ExcelParserConfigurator.class.getName());
	private static final int numGuideLinesToCheck = 5;
	private static final String guideLineJSONField = "guideline";
	
	public ArrayList<String> configErrors = new ArrayList<String>();
	public ArrayList<String> configWarnings = new ArrayList<String>();
	
	//Configuration flags
	public boolean Fl_FullCheckForEmptyColumnsAndRows = true;
	public boolean Fl_AllowQualifierInValueCell = true;  
	public boolean Fl_SkipEmptyRows = true;
	
	
	//Template info variables
	public String templateName = null;
	public String templateVersion = null;	
	public int templateType = 1;
	
	//Global configuration for the data access of the primary sheet
	public boolean basicIterationLoadSubstanceRecord = true;
	public boolean FlagBasicIterationLoadSubstanceRecord = false;
	
	public IterationAccess substanceIteration =  IterationAccess.ROW_SINGLE;
	public boolean FlagSubstanceIteration = false; 
	
	public int rowMultiFixedSize = 1;
	public boolean FlagRowMultiFixedSize = false;
	
	public int startRow = 2;
	public boolean FlagStartRow = false;
	
	public int endRow = -1;
	public boolean FlagEndRow = false;
	
	public int sheetIndex = 0;
	public boolean FlagSheetIndex = false;
	
	public String sheetName = null;
	public boolean FlagSheetName = false;
	
	public int startHeaderRow = 0;
	public boolean FlagStartHeaderRow = false;
	
	public int endHeaderRow = 0;
	public boolean FlagEndHeaderRow = false;
	
	public boolean allowEmpty = true;
	public boolean FlagAllowEmpty = false;
	
	public Recognition recognition = Recognition.BY_INDEX;
	public boolean FlagRecognition = false;
	
	public DynamicIteration dynamicIteration = DynamicIteration.NEXT_NOT_EMPTY;
	public boolean FlagDynamicIteration = false;
	
	public int dynamicIterationColumnIndex = 0;
	public boolean FlagDynamicIterationColumnIndex = false;
	
	public String dynamicIterationColumnName = null;
	public boolean FlagDynamicIterationColumnName = false;
	
	
	//Specific data locations
	public ArrayList<ExcelSheetConfiguration> parallelSheets = new ArrayList<ExcelSheetConfiguration>();
	public HashMap<String, ExcelDataLocation> substanceLocations = new HashMap<String, ExcelDataLocation>();
	public ArrayList<ProtocolApplicationDataLocation> protocolAppLocations = new ArrayList<ProtocolApplicationDataLocation>();
	public HashMap<String,Object> jsonRepository = new HashMap<String,Object>();
	public ArrayList<CompositionDataLocation> composition = new ArrayList<CompositionDataLocation>();
	public ArrayList<ExternalIdentifierDataLocation> externalIdentifiers = new ArrayList<ExternalIdentifierDataLocation>();
	
	//Read data as variables
	public HashMap<String, ExcelDataLocation> variableLocations = null;
	
	//Handling locations dynamically
	public boolean FlagDynamicSpan = false;
	public boolean FlagDynamicSpanOnSubtsanceLevel = false;
	public DynamicIterationSpan dynamicIterationSpan = null;
	public DynamicSpanInfo dynamicSpanInfo = null; //All dynamic span information is analyzed and stored here
	public ColumnSpan columnSpan = null;
	public RowSpan rowSpan = null;
	
	
	
	public static ExcelParserConfigurator loadFromJSON(String jsonConfig) throws Exception
	{
		FileInputStream fin = new FileInputStream(jsonConfig); 
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = null;
		
		try {
			root = mapper.readTree(fin);
		} catch (Exception x) {
			throw x;
		} finally {
			try {fin.close();} catch (Exception x) {}	
		}
		
		JsonUtilities jsonUtils = new JsonUtilities();
		ExcelParserConfigurator conf = new ExcelParserConfigurator(); 
		
		//Handle Json Repository
		JsonNode	curNode = root.path("REPOSITORY");
		if (!curNode.isMissingNode())
			extractJsonRepository(curNode, conf);

		
		//Handle template info 
		 curNode = root.path("TEMPLATE_INFO");
		if (curNode.isMissingNode())
			conf.configWarnings.add("JSON Section \"TEMPLATE_INFO\" is missing!");
		else
		{
			//NAME
			String keyword =  jsonUtils.extractStringKeyword(curNode, "NAME", false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.templateName = keyword;
			//VERSION
			keyword =  jsonUtils.extractStringKeyword(curNode, "VERSION", false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.templateVersion = keyword;
			//TYPE
			Integer intValue = jsonUtils.extractIntKeyword(curNode, "TYPE", true);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.templateType = intValue;
		}
		
		//Handle global data access
		curNode = root.path("DATA_ACCESS");
		if (curNode.isMissingNode())
			conf.configErrors.add("JSON Section \"DATA_ACCESS\" is missing!");
		else
		{
			//BASIC_ITERATION_LOAD_SUBSTANCE_RECORD
			if (!curNode.path("BASIC_ITERATION_LOAD_SUBSTANCE_RECORD").isMissingNode())
			{	
				Boolean b = jsonUtils.extractBooleanKeyword(curNode, "BASIC_ITERATION_LOAD_SUBSTANCE_RECORD", false);
				if (b == null)
					conf.configErrors.add(jsonUtils.getError());
				else
				{	
					conf.basicIterationLoadSubstanceRecord = b;
					conf.FlagBasicIterationLoadSubstanceRecord = true;
				}
			}
			
			//ITERATION
			if (!curNode.path("ITERATION").isMissingNode())
			{	
				String keyword =  jsonUtils.extractStringKeyword(curNode, "ITERATION", false);
				if (keyword == null)
					conf.configErrors.add(jsonUtils.getError());
				else
				{	
					conf.substanceIteration = IterationAccess.fromString(keyword);
					conf.FlagSubstanceIteration = true;
					if (conf.substanceIteration == IterationAccess.UNDEFINED)
						conf.configErrors.add("In JSON Section \"DATA_ACCESS\", keyword \"ITERATION\" is incorrect or UNDEFINED!");
				}
			}
			
			//SHEET_INDEX
			if (!curNode.path("SHEET_INDEX").isMissingNode())
			{
				Integer intValue = jsonUtils.extractIntKeyword(curNode, "SHEET_INDEX", false);
				if (intValue == null)
					conf.configErrors.add(jsonUtils.getError());
				else
				{	
					conf.sheetIndex = intValue - 1; //1-based --> 0-based
					conf.FlagSheetIndex = true;
				}
			}
			
			//SHEET_NAME
			if (!curNode.path("SHEET_NAME").isMissingNode())
			{	
				String keyword = jsonUtils.extractStringKeyword(curNode, "SHEET_NAME", false);
				if (keyword == null)
					conf.configErrors.add(jsonUtils.getError());
				else
				{	
					conf.sheetName = keyword; 
					conf.FlagSheetName = true;
				}
			}
			
			//START_ROW
			if (!curNode.path("START_ROW").isMissingNode())
			{
				Integer intValue = jsonUtils.extractIntKeyword(curNode, "START_ROW", false);
				if (intValue == null)
					conf.configErrors.add(jsonUtils.getError());
				else
				{	
					conf.startRow = intValue - 1; //1-based --> 0-based
					conf.FlagStartRow = true;
				}
			}
			
			//END_ROW
			if (!curNode.path("END_ROW").isMissingNode())
			{
				Integer intValue = jsonUtils.extractIntKeyword(curNode, "END_ROW", false);
				if (intValue == null)
					conf.configErrors.add(jsonUtils.getError());
				else
				{	
					conf.endRow = intValue - 1; //1-based --> 0-based
					conf.FlagEndRow = true;
				}
			}
			
			//START_HEADER_ROW
			if (!curNode.path("START_HEADER_ROW").isMissingNode())
			{
				Integer intValue = jsonUtils.extractIntKeyword(curNode, "START_HEADER_ROW", false);
				if (intValue == null)
					conf.configErrors.add(jsonUtils.getError());
				else
				{	
					conf.startHeaderRow = intValue - 1; //1-based --> 0-based
					conf.FlagStartHeaderRow = true;
				}	
			
			}
			
			//END_HEADER_ROW
			if (!curNode.path("END_HEADER_ROW").isMissingNode())
			{
				Integer intValue = jsonUtils.extractIntKeyword(curNode, "END_HEADER_ROW", false);
				if (intValue == null)
					conf.configErrors.add(jsonUtils.getError());
				else
				{	
					conf.endHeaderRow = intValue - 1 ; //1-based --> 0-based
					conf.FlagEndHeaderRow = true;
				}	
			}
			
			//ALLOW_EMPTY
			if (!curNode.path("ALLOW_EMPTY").isMissingNode())
			{
				Boolean boolValue = jsonUtils.extractBooleanKeyword(curNode, "ALLOW_EMPTY", false);
				if (boolValue == null)
					conf.configErrors.add(jsonUtils.getError());
				else
				{	
					conf.allowEmpty = boolValue;
					conf.FlagAllowEmpty = true;
				}	
			}
			
			//RECOGNITION
			if (!curNode.path("RECOGNITION").isMissingNode())
			{
				String keyword =  jsonUtils.extractStringKeyword(curNode, "RECOGNITION", true);
				if (keyword == null)
					conf.configErrors.add(jsonUtils.getError());
				else
				{	
					conf.recognition = Recognition.fromString(keyword);
					conf.FlagRecognition = true;
					if (conf.recognition == Recognition.UNDEFINED)
						conf.configErrors.add("In JSON Section \"DATA_ACCESS\", keyword \"RECOGNITION\" is incorrect or UNDEFINED!");
				}
			}
			
			//DYNAMIC_ITERATION
			if (!curNode.path("DYNAMIC_ITERATION").isMissingNode())
			{
				String keyword =  jsonUtils.extractStringKeyword(curNode, "DYNAMIC_ITERATION", true);
				if (keyword == null)
					conf.configErrors.add(jsonUtils.getError());
				else
				{	
					conf.dynamicIteration = DynamicIteration.fromString(keyword);
					conf.FlagDynamicIteration = true;
					if (conf.dynamicIteration == DynamicIteration.UNDEFINED)
						conf.configErrors.add("In JSON Section \"DATA_ACCESS\", keyword \"DYNAMIC_ITERATION\" is incorrect or UNDEFINED!");
				}
			}
			
			//DYNAMIC_ITERATION_COLUMN_INDEX
			if (!curNode.path("DYNAMIC_ITERATION_COLUMN_INDEX").isMissingNode())
			{
				int col_index = ExcelParserUtils.extractColumnIndex(curNode.path("DYNAMIC_ITERATION_COLUMN_INDEX"));
				if (col_index == -1)
				{
					conf.configErrors.add("In JSON section \"DATA_ACESS\", keyword \"DYNAMIC_ITERATION_COLUMN_INDEX\" is incorrect! " + jsonUtils.getError());
				}
				else
				{	
					conf.dynamicIterationColumnIndex = col_index; 
					conf.FlagDynamicIterationColumnIndex = true;
				}
			}
			
			//DYNAMIC_ITERATION_COLUMN_NAME
			if (!curNode.path("DYNAMIC_ITERATION_COLUMN_NAME").isMissingNode())
			{	
				String keyword = jsonUtils.extractStringKeyword(curNode, "DYNAMIC_ITERATION_COLUMN_NAME", false);
				if (keyword == null)
					conf.configErrors.add(jsonUtils.getError());
				else
				{	
					conf.dynamicIterationColumnName = keyword; 
					conf.FlagDynamicIterationColumnName = true;
				}
			}
			
			
			//VARIABLES
			JsonNode varNode = curNode.path("VARIABLES");
			if (!varNode.isMissingNode())
			{
				conf.variableLocations = extractDynamicSection(varNode, conf);
			}
			
			
			//DYNAMIC_ITERATION_SPAN
			if (!curNode.path("DYNAMIC_ITERATION_SPAN").isMissingNode())
			{
				DynamicIterationSpan span = 
						DynamicIterationSpan.extractDynamicIterationSpan(curNode.path("DYNAMIC_ITERATION_SPAN"), conf, "DATA_ACCESS");
				conf.dynamicIterationSpan = span;
				conf.dynamicIterationSpan.isPrimarySheet = true; 
			}
			
			//COLUMN_SPAN
			if (!curNode.path("COLUMN_SPAN").isMissingNode())
			{
				ColumnSpan span = extractColumnSpan(curNode.path("COLUMN_SPAN"), conf, "DATA_ACCESS");
				conf.columnSpan = span;
			}
			
			//ROW_SPAN
			if (!curNode.path("ROW_SPAN").isMissingNode())
			{
				RowSpan span = extractRowSpan(curNode.path("ROW_SPAN"), conf, "DATA_ACCESS");
				conf.rowSpan = span;
			}
			
		}
		
		//Handle SubstanceRecord data locations
		curNode = root.path("SUBSTANCE_RECORD");
		if (curNode.isMissingNode())
			conf.configErrors.add("JSON Section \"SUBSTANCE_RECORD\" is missing!");
		else
		{
			//COMPANY_NAME
			ExcelDataLocation loc = extractDataLocation(curNode,"COMPANY_NAME", conf);
			if (loc != null)
			{	
				if (loc.nErrors == 0)							
					conf.substanceLocations.put("SubstanceRecord.companyName", loc);
				//error messages are already added to conf (this is valid for all other location extractions)
			}
			else
			{	
				//Missing section is not counted as an error. Same treatment for the other sections
				//conf.configErrors.add("JSON Section \"SUBSTANCE_RECORD\", keyword  \"COMPANY_NAME\" is missing!");
			}
			
			//COMPANY_UUID
			loc = extractDataLocation(curNode,"COMPANY_UUID", conf);
			if (loc != null)
			{	
				if (loc.nErrors == 0)							
					conf.substanceLocations.put("SubstanceRecord.companyUUID", loc);
			}
			
			//OWNER_NAME
			loc = extractDataLocation(curNode,"OWNER_NAME", conf);
			if (loc != null)
			{	
				if (loc.nErrors == 0)							
					conf.substanceLocations.put("SubstanceRecord.ownerName", loc);
			}
			
			//OWNER_UUID
			loc = extractDataLocation(curNode,"OWNER_UUID", conf);
			if (loc != null)
			{	
				if (loc.nErrors == 0)							
					conf.substanceLocations.put("SubstanceRecord.ownerUUID", loc);
			}
			
			//SUBSTANCE_TYPE
			loc = extractDataLocation(curNode,"SUBSTANCE_TYPE", conf);
			if (loc != null)
			{	
				if (loc.nErrors == 0)							
					conf.substanceLocations.put("SubstanceRecord.substanceType", loc);
			}
			
			
			//PUBLIC_NAME
			loc = extractDataLocation(curNode,"PUBLIC_NAME", conf);
			if (loc != null)
			{	
				if (loc.nErrors == 0)							
					conf.substanceLocations.put("SubstanceRecord.publicName", loc);
			}
			
			//ID_SUBSTANCE
			loc = extractDataLocation(curNode,"ID_SUBSTANCE", conf);
			if (loc != null)
			{	
				if (loc.nErrors == 0)							
					conf.substanceLocations.put("SubstanceRecord.idSubstance", loc);
			}
			
			
			//COMPOSITION
			JsonNode composNode = curNode.path("COMPOSITION");
			if (!composNode.isMissingNode())
			{
				if (composNode.isArray())
				{
					for (int i = 0; i < composNode.size(); i++)
					{	
						CompositionDataLocation compDL = extractCompositionDataLocation(composNode.get(i), conf, i);
						conf.composition.add(compDL);
					}	
				}
				else
					conf.configErrors.add("Section \"COMPOSITION\" is not an array!");
			}
			
			//EXTERNAL_IDENTIFIERS
			JsonNode extIdNode = curNode.path("EXTERNAL_IDENTIFIERS");
			if (!extIdNode.isMissingNode())
			{
				if (extIdNode.isArray())
				{
					for (int i = 0; i < extIdNode.size(); i++)
					{	
						ExternalIdentifierDataLocation eidl = ExternalIdentifierDataLocation.extractExternalIdentifier(extIdNode.get(i), conf);
						conf.externalIdentifiers.add(eidl);
					}	
				}
				else
					conf.configErrors.add("Section \"EXTERNAL_IDENTIFIERS\" is not an array!");
			}
		}
		
		
		//Handle Parallel Sheets
		curNode = root.path("PARALLEL_SHEETS");
		if (curNode.isMissingNode())
		{	
			//Nothing is done. Missing PARALLEL_SHEETS is not an error.
		}	
		else
		{
			if (!curNode.isArray())
			{
				conf.configErrors.add("JSON Section \"PARALLEL_SHEETS\" is not of type array!");
				return conf;
			}
			
			for (int i = 0; i < curNode.size(); i++)
			{	
				ExcelSheetConfiguration eshc= extractParallelSheet(curNode.get(i), i, conf);
				if (eshc == null)
					return conf;
				else
					conf.parallelSheets.add(eshc);
			}	
		}
		
		
		//Handle Protocol Applications (Measurements)
		curNode = root.path("PROTOCOL_APPLICATIONS");
		if (curNode.isMissingNode())
			conf.configErrors.add("JSON Section \"PROTOCOL_APPLICATIONS\" is missing!");
		else
		{
			if (!curNode.isArray())
			{
				conf.configErrors.add("JSON Section \"PROTOCOL_APPLICATIONS\" is not of type array!");
				return conf;
			}
			
			for (int i = 0; i < curNode.size(); i++)
			{	
				ProtocolApplicationDataLocation padl = extractProtocolApplicationDataLocations(curNode.get(i), i, conf);
				if (padl == null)
					return conf;
				else
					conf.protocolAppLocations.add(padl);
			}	
		}
		
		conf.checkDynamicConfiguration();
		
		return conf;
	}
	
	
	
	public String toJSONString()
	{
		ExcelDataLocation loc;
		int n;
		StringBuffer sb = new StringBuffer();
		sb.append("{\n");
		
		sb.append("\t\"TEMPLATE_INFO\" : \n");
		sb.append("\t{\n");
		if (templateName != null)
			sb.append("\t\t\"NAME\" : \"" + templateName + "\",\n" );
		if (templateVersion != null)
			sb.append("\t\t\"VERSION\" : \"" + templateVersion + "\",\n" );		
		sb.append("\t\t\"TYPE\" : " + templateType + "\n" );
		sb.append("\t},\n\n");
		
		sb.append("\t\"DATA_ACCESS\" : \n");
		sb.append("\t{\n");	
		int nDAFields = 0;
		
		if (FlagBasicIterationLoadSubstanceRecord)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"BASIC_ITERATION_LOAD_SUBSTANCE_RECORD\" : " + basicIterationLoadSubstanceRecord); 
			nDAFields++;
		}
		if (FlagSubstanceIteration)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"ITERATION\" : \"" + substanceIteration.toString() + "\"" );
			nDAFields++;
		}	
		if (FlagRowMultiFixedSize)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"ROW_MULTI_FIXED_SIZE\" : " + rowMultiFixedSize); 
			nDAFields++;
		}	
		if (FlagSheetIndex)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"SHEET_INDEX\" : " + (sheetIndex + 1)); //0-based --> 1-based
			nDAFields++;
		}	
		if (FlagSheetName)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"SHEET_NAME\" : \"" + sheetName + "\"" ); 
			nDAFields++;
		}	
		if (FlagStartRow)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"START_ROW\" : " + (startRow + 1)); //0-based --> 1-based
			nDAFields++;
		}
		if (FlagEndRow)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"END_ROW\" : " + (endRow + 1)); //0-based --> 1-based
			nDAFields++;
		}
		if (FlagStartHeaderRow)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"START_HEADER_ROW\" : " + (startHeaderRow + 1)); //0-based --> 1-based
			nDAFields++;
		}	
		if (FlagEndHeaderRow)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"END_HEADER_ROW\" : " + (endHeaderRow + 1)); //0-based --> 1-based
			nDAFields++;
		}	
		if (FlagAllowEmpty)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"ALLOW_EMPTY\" : \"" + allowEmpty + "\"" );	
			nDAFields++;
		}	
		if (FlagRecognition)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"RECOGNITION\" : \"" + recognition.toString() + "\"" );
			nDAFields++;
		}	
		if (FlagDynamicIteration)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"DYNAMIC_ITERATION\" : \"" + dynamicIteration.toString() + "\"" );	
			nDAFields++;
		}	
		if (FlagDynamicIterationColumnIndex)
		{	
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"DYNAMIC_ITERATION_COLUMN_INDEX\" : " + (dynamicIterationColumnIndex + 1) ); //0-based --> 1-based
			nDAFields++;
		}
		if (FlagDynamicIterationColumnName)
		{
			if (nDAFields > 0)
				sb.append(",\n");
			sb.append("\t\t\"DYNAMIC_ITERATION_COLUMN_NAME\" : \"" + dynamicIterationColumnName + "\"" );
			nDAFields++;
		}
		
		
		if (variableLocations != null)
		{	
			
			if (nDAFields > 0)
				sb.append(",\n\n");
			sb.append("\t\t\"VARIABLES\" : \n" );
			sb.append("\t\t{\n" );
			
			int nParams = 0;
			for (String var : variableLocations.keySet())
			{	
				loc = variableLocations.get(var);
				sb.append(loc.toJSONKeyWord("\t\t\t"));
				
				if (nParams < variableLocations.size())
					sb.append(",\n\n");
				else
					sb.append("\n");
				nParams++;
			}
			sb.append("\t\t}" );
			nDAFields++;
		}
		
		//Dynamic locations
		if (dynamicIterationSpan != null)
		{
			if (nDAFields > 0)
				sb.append(",\n\n");
			sb.append(dynamicIterationSpan.toJSONKeyWord("\t\t"));
			nDAFields++;
		}

		if (columnSpan != null)
		{
			if (nDAFields > 0)
				sb.append(",\n\n");
			sb.append(columnSpan.toJSONKeyWord("\t\t"));
			nDAFields++;
		}

		if (rowSpan != null)
		{
			if (nDAFields > 0)
				sb.append(",\n\n");
			sb.append(rowSpan.toJSONKeyWord("\t\t"));
			nDAFields++;
		}
		
		if (nDAFields > 0)
			sb.append("\n");
		
		sb.append("\t},\n\n");  //end of DATA_ACCESS section
		
		
		if (!parallelSheets.isEmpty())
		{	
			sb.append("\t\"PARALLEL_SHEETS\":\n");
			sb.append("\t[\n");
			for (int i = 0; i < parallelSheets.size(); i++)
			{	
				sb.append(parallelSheets.get(i).toJSONKeyWord("\t\t"));			
				if (i < parallelSheets.size()-1) 
					sb.append(",\n");
				sb.append("\n");
			}
			sb.append("\t],\n\n"); 
		}
		
		
		sb.append("\t\"SUBSTANCE_RECORD\" : \n");
		sb.append("\t{\n");
		n = 0;
		
		loc = substanceLocations.get("SubstanceRecord.companyName");
		if (loc != null)
		{
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}
		
		loc = substanceLocations.get("SubstanceRecord.companyUUID");
		if (loc != null)
		{
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}
		
		loc = substanceLocations.get("SubstanceRecord.ownerName");
		if (loc != null)
		{
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}
		
		loc = substanceLocations.get("SubstanceRecord.ownerUUID");
		if (loc != null)
		{
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}
		
		loc = substanceLocations.get("SubstanceRecord.substanceType");
		if (loc != null)
		{
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}
		
		loc = substanceLocations.get("SubstanceRecord.publicName");
		if (loc != null)
		{
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}
		
		loc = substanceLocations.get("SubstanceRecord.idSubstance");
		if (loc != null)
		{
			if (n > 0)
				sb.append(",\n\n");
			sb.append(loc.toJSONKeyWord("\t\t"));
			n++;
		}
		
		if (!externalIdentifiers.isEmpty())
		{	
			if (n > 0)
				sb.append(",\n\n");
			sb.append("\t\t\"EXTERNAL_IDENTIFIERS\":\n");
			sb.append("\t\t[\n");
			for (int i = 0; i < externalIdentifiers.size(); i++)
			{	
				sb.append(externalIdentifiers.get(i).toJSONKeyWord("\t\t\t"));			
				if (i < externalIdentifiers.size()-1) 
					sb.append(",\n");
				sb.append("\n");
			}
			sb.append("\t\t]"); 
		}
		
		if (!composition.isEmpty())
		{	
			if (n > 0)
				sb.append(",\n\n");
			sb.append("\t\t\"COMPOSITION\":\n");
			sb.append("\t\t[\n");
			for (int i = 0; i < composition.size(); i++)
			{	
				sb.append(composition.get(i).toJSONKeyWord("\t\t\t"));			
				if (i < composition.size()-1) 
					sb.append(",\n");
				sb.append("\n");
			}
			sb.append("\t\t]"); 
		}
		
		
		if (n > 0)
			sb.append("\n");
		
		sb.append("\t},\n\n"); //end of SUBSTANCE_RECORD
		
		
		sb.append("\t\"PROTOCOL_APPLICATIONS\":\n");
		sb.append("\t[\n");
		for (int i = 0; i < protocolAppLocations.size(); i++)
		{	
			sb.append(protocolAppLocations.get(i).toJSONKeyWord("\t\t"));			
			if (i < protocolAppLocations.size()-1) 
				sb.append(",\n");
			sb.append("\n");
		}
		sb.append("\t]"); //end of PROTOCOL_APPLICATIONS array
		
		
		if (!jsonRepository.isEmpty())
		{	
			sb.append(",\n\n");
			sb.append("\t\"REPOSITORY\":\n");
			sb.append("\t{\n");

			int nRepElements = 0;
			for (String key : jsonRepository.keySet())
			{	
				sb.append("\t\t\""+ key+"\" : ");
				Object o = jsonRepository.get(key);
				if (o instanceof Integer) 
					sb.append(o.toString());
				else
					if (o instanceof Double) 
						sb.append(o.toString());
					else
						if (o instanceof String) 
							sb.append("\""+o.toString() + "\"");
						else
							sb.append("\"***NOT_SUPPORTED_OBJECT***\""); //This line should not be reached
				
				if (nRepElements < jsonRepository.size()-1)
					sb.append(",\n");
				else
					sb.append("\n");
				nRepElements++;
			}

			sb.append("\t}"); 
		}
		
		sb.append("\n\n");
		
		sb.append("}\n"); //end of JSON
		return sb.toString();
	}
	
	public String getAllErrorsAsString()
	{
		if (configErrors.isEmpty())
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < configErrors.size(); i++)
			sb.append(configErrors.get(i) + "\n");
		return sb.toString();
	}
	
	public static ExcelDataLocation extractDataLocation(JsonNode node, ExcelParserConfigurator conf)
	{
		return extractDataLocation(node, null, conf);
	}
	
	
	public static ExcelDataLocation extractDataLocation(JsonNode node, String jsonSection, ExcelParserConfigurator conf)
	{
		//Error messages are stored globally in 'conf' variable and are
		//counted locally in return variable 'loc'
		
		JsonNode sectionNode;
		
		if (jsonSection == null)
			sectionNode = node; //The node itself is used
		else
		{	
			sectionNode = node.path(jsonSection);
			if (sectionNode.isMissingNode())
				return null;
		}
		JsonUtilities jsonUtils = new JsonUtilities();
		
		ExcelDataLocation loc = new ExcelDataLocation();
		loc.sectionName = jsonSection;
		
		//ITERATION
		if (sectionNode.path("ITERATION").isMissingNode())
		{
			loc.iteration = conf.substanceIteration; //default value is taken form global config
		}
		else
		{
			String keyword =  jsonUtils.extractStringKeyword(sectionNode, "ITERATION", false);
			if (keyword == null)
			{	
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"ITERATION\" : " + jsonUtils.getError());
				loc.nErrors++;
			}	
			else
			{	
				loc.FlagIteration = true;
				loc.iteration = IterationAccess.fromString(keyword);
				if (loc.iteration == IterationAccess.UNDEFINED)
				{	
					conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"ITERATION\" is incorrect or UNDEFINED!");
					loc.nErrors++;
				}	
			}
		}
		
		
		//RECOGNITION
		if (sectionNode.path("RECOGNITION").isMissingNode())
		{
			loc.recognition = conf.recognition; //default value is taken form global config
		}
		else
		{
			String keyword =  jsonUtils.extractStringKeyword(sectionNode, "RECOGNITION", false);
			if (keyword == null)
			{	
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"RECOGNITION\" : " + jsonUtils.getError());
				loc.nErrors++;
			}	
			else
			{	
				loc.FlagRecognition = true;
				loc.recognition = Recognition.fromString(keyword);
				if (loc.recognition == Recognition.UNDEFINED)
				{	
					conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"RECOGNITION\" is incorrect or UNDEFINED!");
					loc.nErrors++;
				}	
			}
		}
		
		
		//COLUMN_INDEX
		if (sectionNode.path("COLUMN_INDEX").isMissingNode())
		{	
			if (loc.iteration.isColumnInfoRequired())
			{	
				if (loc.recognition == Recognition.BY_INDEX) 
				{	
					conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"COLUMN_INDEX\" is missing!");
					loc.nErrors++;
				}
				
				if (loc.recognition == Recognition.BY_INDEX_AND_NAME && (sectionNode.path("COLUMN_NAME").isMissingNode()) )
				{	
					conf.configErrors.add("In JSON section \"" + jsonSection + 
								"\", both keywords \"COLUMN_INDEX\" and \"COLUMN_NAME\" are missing. "
								+ "At least one is required for RECOGNITION mode BY_INDEX_AND_NAME!");
					loc.nErrors++;
				}
			}	
		}
		else
		{
			int col_index = ExcelParserUtils.extractColumnIndex(sectionNode.path("COLUMN_INDEX"));
			if (col_index == -1)
			{
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"COLUMN_INDEX\" : " + jsonUtils.getError());
				loc.nErrors++;
			}
			else
			{	
				loc.FlagColumnIndex = true;
				loc.columnIndex = col_index;
			}	
		}
		
		
		//COLUMN_NAME
		if (sectionNode.path("COLUMN_NAME").isMissingNode())
		{
			if (loc.iteration.isColumnInfoRequired())
				if (loc.recognition == Recognition.BY_NAME)
				{	
					conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"COLUMN_NAME\" is missing!");
					loc.nErrors++;
				}	
			//Case loc.recognition == Recognition.BY_INDEX_AND_NAME is treated in COLUMN_INDEX
		}
		else
		{
			String stringValue = jsonUtils.extractStringKeyword(sectionNode, "COLUMN_NAME", false);
			if (stringValue == null)
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"COLUMN_NAME\" : " + jsonUtils.getError());
			else
			{	
				loc.FlagColumnName = true;
				loc.columnName = stringValue;
			}
		}
		
		//ROW_INDEX
		if (sectionNode.path("ROW_INDEX").isMissingNode())
		{
			if (loc.iteration.isRowInfoRequired())
			{	
				if (loc.recognition == Recognition.BY_INDEX)
				{	
					conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"ROW_INDEX\" is missing!");
					loc.nErrors++;
				}
				
				if (loc.recognition == Recognition.BY_INDEX_AND_NAME && (sectionNode.path("ROW_NAME").isMissingNode()) )
				{	
					conf.configErrors.add("In JSON section \"" + jsonSection + 
								"\", both keywords \"ROW_INDEX\" and \"ROW_NAME\" are missing. "
								+ "At least one is required for RECOGNITION mode BY_INDEX_AND_NAME!");
					loc.nErrors++;
				}
			}	
		}
		else
		{
			Integer intValue = jsonUtils.extractIntKeyword(sectionNode, "ROW_INDEX", true);
			if (intValue == null)
			{	
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"ROW_INDEX\" : " + jsonUtils.getError());
				loc.nErrors++;
			}	
			else
			{	
				loc.FlagRowIndex = true;
				loc.rowIndex = intValue - 1; //1-based --> 0-based
			}
		}
		
		//ROW_NAME
		if (sectionNode.path("ROW_NAME").isMissingNode())
		{
			if (loc.iteration.isRowInfoRequired())
				if (loc.recognition == Recognition.BY_NAME)
				{	
					conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"ROW_NAME\" is missing!");
					loc.nErrors++;
				}
			//Case loc.recognition == Recognition.BY_INDEX_AND_NAME is treated in ROW_INDEX
		}
		else
		{
			String stringValue = jsonUtils.extractStringKeyword(sectionNode, "ROW_NAME", false);
			if (stringValue == null)
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"ROW_NAME\" : " + jsonUtils.getError());
			else
			{	
				loc.FlagRowName = true;
				loc.rowName = stringValue;
			}
		}
		
		
		//SHEET_INDEX
		if (!sectionNode.path("SHEET_INDEX").isMissingNode())
		{	
			Integer intValue = jsonUtils.extractIntKeyword(sectionNode, "SHEET_INDEX", false);
			if (intValue == null)
			{	
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"SHEET_INDEX\" : " + jsonUtils.getError());
				loc.nErrors++;
			}	
			else
			{	
				loc.FlagSheetIndex = true;
				loc.sheetIndex = intValue - 1; //1-based --> 0-based
			}
		}
		
		
		//SHEET_NAME
		if (!sectionNode.path("SHEET_NAME").isMissingNode())
		{
			String stringValue = jsonUtils.extractStringKeyword(sectionNode, "SHEET_NAME", false);
			if (stringValue == null)
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"SHEET_NAME\" : " + jsonUtils.getError());
			else
			{	
				loc.FlagSheetName = true;
				loc.sheetName = stringValue;
			}
		}
		
		
		//JSON_VALUE
		if (sectionNode.path("JSON_VALUE").isMissingNode())
		{	
			if (loc.iteration == IterationAccess.JSON_VALUE)
			{	
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"JSON_VALUE\" is missing!");
				loc.nErrors++;
			}
		}
		else
		{
			Object jsonValue = extractObject (sectionNode.path("JSON_VALUE"));
			loc.setJsonValue(jsonValue); 
		}
		
		//JSON_REPOSITORY_KEY
		if (sectionNode.path("JSON_REPOSITORY_KEY").isMissingNode())
		{	
			if (loc.iteration == IterationAccess.JSON_REPOSITORY)
			{	
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"JSON_REPOSITORY_KEY\" is missing!");
				loc.nErrors++;
			}
		}
		else
		{
			String stringValue = jsonUtils.extractStringKeyword(sectionNode, "JSON_REPOSITORY_KEY", true);
			if (stringValue == null)
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"JSON_REPOSITORY_KEY\" : " + jsonUtils.getError());
			else
			{	
				loc.setJsonRepositoryKey(stringValue);
			}
		}
		
		
		//VARIABLE_KEY
		if (sectionNode.path("VARIABLE_KEY").isMissingNode())
		{	
			if (loc.iteration == IterationAccess.VARIABLE)
			{	
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"VARIABLE_KEY\" is missing!");
				loc.nErrors++;
			}
		}
		else
		{
			String stringValue = jsonUtils.extractStringKeyword(sectionNode, "VARIABLE_KEY", false);
			if (stringValue == null)
				conf.configErrors.add("In JSON section \"" + jsonSection + "\", keyword \"VARIABLE_KEY\" : " + jsonUtils.getError());
			else
			{	
				loc.setVariableKey(stringValue);
			}
		}
		
		return loc;
	}
	
	
	public static ProtocolApplicationDataLocation extractProtocolApplicationDataLocations(JsonNode node, int protocolNum, ExcelParserConfigurator conf)
	{
		ProtocolApplicationDataLocation padl = new ProtocolApplicationDataLocation();
		
		//CITATION_TITLE
		ExcelDataLocation loc = extractDataLocation(node,"CITATION_TITLE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.citationTitle = loc;
		}	
		
		//CITATION_YEAR
		loc = extractDataLocation(node,"CITATION_YEAR", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.citationYear = loc;
		}
		
		//CITATION_OWNER
		loc = extractDataLocation(node,"CITATION_OWNER", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.citationOwner = loc;
		}
		
		//PROTOCOL_TOP_CATEGORY
		loc = extractDataLocation(node,"PROTOCOL_TOP_CATEGORY", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.protocolTopCategory = loc;
		}
		
		//PROTOCOL_CATEGORY_CODE
		loc = extractDataLocation(node,"PROTOCOL_CATEGORY_CODE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.protocolCategoryCode = loc;
		}

		//PROTOCOL_CATEGORY_TITLE
		loc = extractDataLocation(node,"PROTOCOL_CATEGORY_TITLE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.protocolCategoryTitle = loc;
		}

		//PROTOCOL_ENDPOINT
		loc = extractDataLocation(node,"PROTOCOL_ENDPOINT", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.protocolEndpoint = loc;
		}
		
		//PROTOCOL_GUIDELINE
		JsonNode pglNode = node.path("PROTOCOL_GUIDELINE");
		if (!pglNode.isMissingNode())
		{
			ArrayList<ExcelDataLocation> protGuidline = new ArrayList<ExcelDataLocation>();
			HashMap<String, ExcelDataLocation> pglLocs = extractDynamicSection(pglNode, conf);
			for (int i = 1; i < numGuideLinesToCheck; i++)
			{
				ExcelDataLocation pglLoc = pglLocs.get(guideLineJSONField + i);
				if (pglLoc != null)
					protGuidline.add(pglLoc);
			}
			
			padl.protocolGuideline = protGuidline;
		}
		
		
		//PARAMETERS
		JsonNode parNode = node.path("PARAMETERS");
		if (!parNode.isMissingNode())
		{
			padl.parameters = extractDynamicSection(parNode, conf);
		}
		
		
		//RELIABILITY_IS_ROBUST_STUDY
		loc = extractDataLocation(node,"RELIABILITY_IS_ROBUST_STUDY", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.reliability_isRobustStudy = loc;
		}
		
		
		//RELIABILITY_IS_USED_FOR_CLASSIFICATION
		loc = extractDataLocation(node,"RELIABILITY_IS_USED_FOR_CLASSIFICATION", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.reliability_isUsedforClassification = loc;
		}
		
		//RELIABILITY_IS_USED_FOR_MSDS
		loc = extractDataLocation(node,"RELIABILITY_IS_USED_FOR_MSDS", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.reliability_isUsedforMSDS = loc;
		}

		//RELIABILITY_PURPOSE_FLAG
		loc = extractDataLocation(node,"RELIABILITY_PURPOSE_FLAG", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.reliability_purposeFlag = loc;
		}
		
		//RELIABILITY_STUDY_RESULT_TYPE
		loc = extractDataLocation(node,"RELIABILITY_STUDY_RESULT_TYPE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.reliability_studyResultType = loc;
		}

		//RELIABILITY_VALUE
		loc = extractDataLocation(node,"RELIABILITY_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.reliability_value = loc;
		}
		
		//INTERPRETATION_RESULT
		loc = extractDataLocation(node,"INTERPRETATION_RESULT", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.interpretationResult = loc;
		}
		
		//INTERPRETATION_CRITERIA
		loc = extractDataLocation(node,"INTERPRETATION_CRITERIA", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				padl.interpretationCriteria = loc;
		}
		
		
		
		//EFFECTS
		JsonNode effectsNode = node.path("EFFECTS");
		if (!effectsNode.isMissingNode())
		{
			if (!effectsNode.isArray())
			{	
				conf.configErrors.add("EFFECTS section is not of type array!");
				return padl;
			}
			
			padl.effects = new ArrayList<EffectRecordDataLocation>();
			
			for (int i = 0; i < effectsNode.size(); i++)
			{	
				EffectRecordDataLocation efrdl = extractEffectDataLocation(effectsNode.get(i) ,conf);
				padl.effects.add(efrdl);
			}	
		}
		
		return padl;
	}
	
	public static EffectRecordDataLocation extractEffectDataLocation(JsonNode node, ExcelParserConfigurator conf)
	{
		EffectRecordDataLocation efrdl = new EffectRecordDataLocation();
		
		//ENDPOINT
		ExcelDataLocation loc = extractDataLocation(node,"ENDPOINT", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				efrdl.endpoint = loc;
		}
		
		//SAMPLE_ID
		loc = extractDataLocation(node,"SAMPLE_ID", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				efrdl.sampleID = loc;
		}
		
		//UNIT
		loc = extractDataLocation(node,"UNIT", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				efrdl.unit = loc;
		}

		//LO_VALUE
		loc = extractDataLocation(node,"LO_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)
			{	
				efrdl.loValue = loc;
				if (conf.Fl_AllowQualifierInValueCell)
					efrdl.loValue.setFlagExtractValueQualifier(true);
			}		
		}
		
		//LO_QUALIFIER
		loc = extractDataLocation(node,"LO_QUALIFIER", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				efrdl.loQualifier = loc;
		}
		
		//UP_VALUE
		loc = extractDataLocation(node,"UP_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)
			{	
				efrdl.upValue = loc;
				if (conf.Fl_AllowQualifierInValueCell)
					efrdl.upValue.setFlagExtractValueQualifier(true);
			}	
		}
		
		//UP_QUALIFIER
		loc = extractDataLocation(node,"UP_QUALIFIER", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				efrdl.upQualifier = loc;
		}

		//TEXT_VALUE
		loc = extractDataLocation(node,"TEXT_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				efrdl.textValue = loc;
		}

		//ERR_VALUE
		loc = extractDataLocation(node,"ERR_VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)
			{	
				efrdl.errValue = loc;
				if (conf.Fl_AllowQualifierInValueCell)
					efrdl.errValue.setFlagExtractValueQualifier(true);
			}	
		}
		
		//ERR_QUALIFIER
		loc = extractDataLocation(node,"ERR_QUALIFIER", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				efrdl.errQualifier = loc;
		}
		
		//VALUE
		loc = extractDataLocation(node,"VALUE", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				efrdl.value = loc;
		}

		//CONDITIONS
		JsonNode effCondNode = node.path("CONDITIONS");
		if (!effCondNode.isMissingNode())
		{
			efrdl.conditions = extractDynamicSection(effCondNode, conf);
		}
		
		return efrdl;
	}
	
	public static ExcelSheetConfiguration extractParallelSheet(JsonNode node, int jsonArrayIndex,  ExcelParserConfigurator conf)
	{
		ExcelSheetConfiguration eshc = new ExcelSheetConfiguration();
		JsonUtilities jsonUtils = new JsonUtilities();
		
		//ITERATION
		if(!node.path("ITERATION").isMissingNode())
		{	
			String keyword =  jsonUtils.extractStringKeyword(node, "ITERATION", false);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"ITERATION\": " + jsonUtils.getError());
			else
			{	
				eshc.iteration = IterationAccess.fromString(keyword);
				if (eshc.iteration == IterationAccess.UNDEFINED)
					conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
							+ (jsonArrayIndex+1) + " keyword \"ITERATION\" is incorrect or UNDEFINED!");
				else
					eshc.FlagIteration = true;
			}
		}
		
		//SHEET_INDEX
		if(!node.path("SHEET_INDEX").isMissingNode())
		{
			Integer intValue = jsonUtils.extractIntKeyword(node, "SHEET_INDEX", false);
			if (intValue == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"SHEET_INDEX\": " + jsonUtils.getError());
			else
			{	
				eshc.sheetIndex = intValue - 1; //1-based --> 0-based
				eshc.FlagSheetIndex = true;
			}
		}
		
		//SHEET_NAME
		if(!node.path("SHEET_NAME").isMissingNode())
		{
			String keyword = jsonUtils.extractStringKeyword(node, "SHEET_NAME", false);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"SHEET_NAME\": " + jsonUtils.getError());
			else
			{	
				eshc.sheetName = keyword; 
				eshc.FlagSheetName = true;
			}
		}
		
		//ROW_MULTI_FIXED_SIZE
		if(!node.path("ROW_MULTI_FIXED_SIZE").isMissingNode())
		{
			Integer intValue = jsonUtils.extractIntKeyword(node, "ROW_MULTI_FIXED_SIZE", false);
			if (intValue == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"ROW_MULTI_FIXED_SIZE\": " + jsonUtils.getError());
			else
			{	
				eshc.rowMultiFixedSize = intValue; 		
				eshc.FlagRowMultiFixedSize = true;
			}
		}
		
		//START_ROW
		if(!node.path("START_ROW").isMissingNode())
		{
			Integer intValue = jsonUtils.extractIntKeyword(node, "START_ROW", false);
			if (intValue == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"START_ROW\": " + jsonUtils.getError());
			else
			{	
				eshc.startRow = intValue - 1; //1-based --> 0-based		
				eshc.FlagStartRow = true;
			}
		}
		
		//END_ROW
		if(!node.path("END_ROW").isMissingNode())
		{
			Integer intValue = jsonUtils.extractIntKeyword(node, "END_ROW", false);
			if (intValue == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"END_ROW\": " + jsonUtils.getError());
			else
			{	
				eshc.endRow = intValue - 1; //1-based --> 0-based		
				eshc.FlagEndRow = true;
			}
		}
		
		//START_HEADER_ROW
		if(!node.path("START_HEADER_ROW").isMissingNode())
		{
			Integer intValue = jsonUtils.extractIntKeyword(node, "START_HEADER_ROW", false);
			if (intValue == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"START_HEADER_ROW\": " + jsonUtils.getError());
			else
			{	
				eshc.startHeaderRow = intValue - 1; //1-based --> 0-based
				eshc.FlagStartHeaderRow = true;
			}
		}
		
		//END_HEADER_ROW
		if(!node.path("END_HEADER_ROW").isMissingNode())
		{
			Integer intValue = jsonUtils.extractIntKeyword(node, "END_HEADER_ROW", false);
			if (intValue == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"END_HEADER_ROW\": " + jsonUtils.getError());
			else
			{	
				eshc.endHeaderRow = intValue - 1 ; //1-based --> 0-based
				eshc.FlagEndHeaderRow = true;
			}
		}
		
		//ALLOW_EMPTY
		if(!node.path("ALLOW_EMPTY").isMissingNode())
		{
			Boolean boolValue = jsonUtils.extractBooleanKeyword(node, "ALLOW_EMPTY", false);
			if (boolValue == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"ALLOW_EMPTY\": " + jsonUtils.getError());
			else
			{	
				eshc.allowEmpty = boolValue;
				eshc.FlagAllowEmpty = true;
			}	
		}
		
		//RECOGNITION
		if(!node.path("RECOGNITION").isMissingNode())
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "RECOGNITION", true);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"RECOGNITION\": " + jsonUtils.getError());
			else
			{	
				eshc.recognition = Recognition.fromString(keyword);
				if (conf.recognition == Recognition.UNDEFINED)
					conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
							+ (jsonArrayIndex+1) + " keyword \"RECOGNITION\" is incorrect or UNDEFINED!");
			}
		}
		
		//DYNAMIC_ITERATION
		if(!node.path("DYNAMIC_ITERATION").isMissingNode())
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "DYNAMIC_ITERATION", true);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"DYNAMIC_ITERATION\": " + jsonUtils.getError());
			else
			{	
				eshc.dynamicIteration = DynamicIteration.fromString(keyword);
				if (eshc.dynamicIteration == DynamicIteration.UNDEFINED)
					conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
							+ (jsonArrayIndex+1) + " keyword \"DYNAMIC_ITERATION\" is incorrect or UNDEFINED!");
			}	
		}
		
		//DYNAMIC_ITERATION_COLUMN_INDEX
		if (!node.path("DYNAMIC_ITERATION_COLUMN_INDEX").isMissingNode())
		{
			int col_index = ExcelParserUtils.extractColumnIndex(node.path("DYNAMIC_ITERATION_COLUMN_INDEX"));
			if (col_index == -1)
			{
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"DYNAMIC_ITERATION_COLUMN_INDEX\": " + jsonUtils.getError());
			}
			else
			{	
				eshc.dynamicIterationColumnIndex = col_index; 
				eshc.FlagDynamicIterationColumnIndex = true;
			}
		}
		
		//DYNAMIC_ITERATION_COLUMN_NAME
		if (!node.path("DYNAMIC_ITERATION_COLUMN_NAME").isMissingNode())
		{	
			String keyword = jsonUtils.extractStringKeyword(node, "DYNAMIC_ITERATION_COLUMN_NAME", false);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
			{	
				eshc.dynamicIterationColumnName = keyword; 
				eshc.FlagDynamicIterationColumnName = true;
			}
		}
		
		//SYNCHRONIZATION
		if(!node.path("SYNCHRONIZATION").isMissingNode())
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "SYNCHRONIZATION", true);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"SYNCHRONIZATION\": " + jsonUtils.getError());
			else
			{	
				eshc.synchronization = SheetSynchronization.fromString(keyword);
				if (eshc.synchronization == SheetSynchronization.UNDEFINED)
					conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
							+ (jsonArrayIndex+1) + " keyword \"SYNCHRONIZATION\" is incorrect or UNDEFINED!");
			}	
		}
		
		//VARIABLES
		JsonNode varNode = node.path("VARIABLES");
		if (!varNode.isMissingNode())
		{
			eshc.variableLocations = extractDynamicSection(varNode, conf);
		}
		
		//DYNAMIC_ITERATION_SPAN
		if (!node.path("DYNAMIC_ITERATION_SPAN").isMissingNode())
		{
			DynamicIterationSpan span = 
					DynamicIterationSpan.extractDynamicIterationSpan(node.path("DYNAMIC_ITERATION_SPAN"), conf, "PARALLEL_SHEET[" + (jsonArrayIndex+1) + "]");
			eshc.dynamicIterationSpan = span;
		}
		
		//COLUMN_SPAN
		if (!node.path("COLUMN_SPAN").isMissingNode())
		{
			ColumnSpan span = extractColumnSpan(node.path("COLUMN_SPAN"), conf, "PARALLEL_SHEET[" + (jsonArrayIndex+1) + "]");
			eshc.columnSpan = span;
		}
		
		//ROW_SPAN
		if (!node.path("ROW_SPAN").isMissingNode())
		{
			RowSpan span = extractRowSpan(node.path("ROW_SPAN"), conf, "PARALLEL_SHEET[" + (jsonArrayIndex+1) + "]");
			eshc.rowSpan = span;
		}
		
		
		return eshc;
	}
	
	public static HashMap<String, ExcelDataLocation> extractDynamicSection(JsonNode node, ExcelParserConfigurator conf)
	{
		HashMap<String, ExcelDataLocation> hmap = new HashMap<String, ExcelDataLocation>();
		
		Iterator<Entry<String,JsonNode>> it = node.getFields();
		while (it.hasNext())
		{
			Entry<String,JsonNode> entry = it.next();
			ExcelDataLocation loc = extractDataLocation(entry.getValue(), conf);
			loc.sectionName = entry.getKey();
			hmap.put(entry.getKey(), loc);
		}
		
		return hmap;
	}
	
	public static CompositionDataLocation extractCompositionDataLocation(JsonNode node, ExcelParserConfigurator conf, int jsonArrayIndex)
	{
		CompositionDataLocation cdl = new CompositionDataLocation();
		JsonUtilities jsonUtils = new JsonUtilities();
	

		//STRUCTURE_RELATION
		if(!node.path("STRUCTURE_RELATION").isMissingNode())
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "STRUCTURE_RELATION", true);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"PARALLEL_SHEETS\", array element " 
						+ (jsonArrayIndex+1) + " keyword \"STRUCTURE_RELATION\": " + jsonUtils.getError());
			else
			{	
				cdl.structureRelation = CompositionDataLocation.structureRelationFromString(keyword);
				if (cdl.structureRelation == null)
					conf.configErrors.add("In JSON Section \"SUBSTANCE_RECORD\", subesction \"COMPOSITION\", array element " 
							+ (jsonArrayIndex+1) + ", keyword \"STRUCTURE_RELATION\" is incorrect! --> " + keyword);
				else
					cdl.FlagStructureRelation = true;
			}	
		}
		
		//CONTENT
		ExcelDataLocation loc = extractDataLocation(node,"CONTENT", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				cdl.content = loc;
		}
		
		if (!node.path("CONTENT").isMissingNode())
		{	
			if (node.path("FORMAT").isMissingNode())
				conf.configErrors.add("In JSON Section \"SUBSTANCE_RECORD\", subesction \"COMPOSITION\", array element " 
						+ (jsonArrayIndex+1) + ", keyword \"FORMAT\" is missing!");
		}		
		
		//FORMAT
		loc = extractDataLocation(node,"FORMAT", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				cdl.format = loc;
		}
		
		//INCHI_KEY
		loc = extractDataLocation(node,"INCHI_KEY", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				cdl.inchiKey = loc;
		}
		
		//INCHI
		loc = extractDataLocation(node,"INCHI", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				cdl.inchi = loc;
		}
		
		//FORMULA
		loc = extractDataLocation(node,"FORMULA", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				cdl.formula = loc;
		}
		
		//SMILES
		loc = extractDataLocation(node,"SMILES", conf);
		if (loc != null)
		{	
			if (loc.nErrors == 0)							
				cdl.smiles = loc;
		}
		
		//PROPERTIES
		JsonNode propNode = node.path("PROPERTIES");
		if (!propNode.isMissingNode())
		{
			cdl.properties = extractDynamicSection(propNode, conf);
		}
		
		
		//PROPORTION
		JsonNode proportionNode = node.path("PROPORTION");
		if (!proportionNode.isMissingNode())
		{
			cdl.proportion = ProportionDataLocation.extractProportion(proportionNode, conf);
		}
		
		return cdl;
	}
	
	
	
	
	public static ColumnSpan extractColumnSpan(JsonNode node, ExcelParserConfigurator conf, String masterSection)
	{
		//TODO
		return null;
	}
	
	public static RowSpan extractRowSpan(JsonNode node, ExcelParserConfigurator conf, String masterSection)
	{
		//TODO
		return null;
	}
	
	public static void extractJsonRepository(JsonNode node, ExcelParserConfigurator conf)
	{
		Iterator<Entry<String,JsonNode>> it = node.getFields();
		while (it.hasNext())
		{
			Entry<String,JsonNode> entry = it.next();
			JsonNode nd = entry.getValue();
			Object o = extractObject (nd);
			if (o != null)
				conf.jsonRepository.put(entry.getKey(), o);
		}
	}
	
	public static Object extractObject (JsonNode node)
	{
		if (node.isTextual())
		{
			String s = node.asText();
			if (s != null)
				return s;
		}
		
		if (node.isInt())
		{
			int i = node.asInt();
			return  new Integer(i);
		}
		
		if (node.isDouble())
		{
			double d  = node.asDouble();
			return new Double(d);
		}
		
		//TODO - eventually add array object extraction
		
		return null;
	}
	
	
	
	public static boolean isValidQualifier(String qualifier)
	{
		for (String q : RecognitionUtils.qualifiers)
			if (q.equals(qualifier))
				return true;
		return false;
	}
	
	public void checkDynamicConfiguration()
	{
		FlagDynamicSpan = haveDynamicSpan();
		if (!FlagDynamicSpan)
		{	
			if (!basicIterationLoadSubstanceRecord)
				configErrors.add("\"BASIC_ITERATION_LOAD_SUBSTANCE_RECORD\" is set to FALSE "
						+ "and no DYNAMIC_ITERATION_SPAN is present on SUBSTANCE level!");
			return; //No other checks are needed
		}
		
		analyzeDynamicSpanInfo();
		
		FlagDynamicSpanOnSubtsanceLevel = haveDynamicSpanOnSubstanceLevel();
		
		if (!basicIterationLoadSubstanceRecord)
		{	
			if (!FlagDynamicSpanOnSubtsanceLevel)
				configErrors.add("\"BASIC_ITERATION_LOAD_SUBSTANCE_RECORD\" is set to FALSE "
						+ "and no DYNAMIC_ITERATION_SPAN is present on SUBSTANCE level!");
			
		}
		else
		{
			if (FlagDynamicSpanOnSubtsanceLevel)
				configErrors.add("\"BASIC_ITERATION_LOAD_SUBSTANCE_RECORD\" is set to TRUE "
						+ "and DYNAMIC_ITERATION_SPAN is present on SUBSTANCE level!");
		}
		
		
		//Checking the consistency of each dynamic section 
		if (dynamicIterationSpan != null)
		{	
			dynamicIterationSpan.checkConsistency();
			
			if (!dynamicIterationSpan.errors.isEmpty())
				for (int i = 0; i < dynamicIterationSpan.errors.size(); i++)
					configErrors.add("Section DATA_ACCESS, subsection DYNAMIC_ITERATION_SPAN "
							+ "incosistency error: " + dynamicIterationSpan.errors.get(i));
		}
		
		for (int k = 0; k < parallelSheets.size(); k++)
		{	
			DynamicIterationSpan dis = parallelSheets.get(k).dynamicIterationSpan;
			if (dis != null)
			{
				dis.checkConsistency();
				
				if (!dis.errors.isEmpty())
					for (int i = 0; i < dis.errors.size(); i++)
						configErrors.add("Section PARALLEL_SHEETS[" + (k+1) + "], subsection DYNAMIC_ITERATION_SPAN "
								+ "incosistency error: " + dis.errors.get(i));
			}
		}
		
		
		//Set default DIS-ids and check for duplicating or empty ids
		ArrayList<String> ids = new ArrayList<String>();
		if (dynamicIterationSpan != null)
		{
			if (dynamicIterationSpan.FlagId)
			{
				if (dynamicIterationSpan.id.isEmpty())
					configErrors.add("Section DATA_ACCESS, subsection DYNAMIC_ITERATION_SPAN "
							+ "ID is empty!");
				else
					ids.add(dynamicIterationSpan.id);
			}
			else
			{	
				dynamicIterationSpan.id = "DIS0";
				ids.add(dynamicIterationSpan.id);
			}	
		}
		
		for (int k = 0; k < parallelSheets.size(); k++)
		{	
			DynamicIterationSpan dis = parallelSheets.get(k).dynamicIterationSpan;
			if (dis != null)
			{
				if (dis.FlagId)
				{
					if (dis.id.isEmpty())
					{	
						configErrors.add("Section PARALLEL_SHEETS[" + (k+1) + "], subsection DYNAMIC_ITERATION_SPAN "
								+ "ID is empty!");
						continue;
					}	
				}
				else
					dis.id = "DIS" + (k+1); //default id
								
				//Check for id duplication
				for (String id : ids)
				{
					if (id.equals(dis.id))
					{
						configErrors.add("Section PARALLEL_SHEETS[" + (k+1) + "], subsection DYNAMIC_ITERATION_SPAN "
								+ "ID " + dis.id + " is duplicated!");
						break;
					}
				}
				
				//Register new id
				ids.add(dis.id);
			}
		}
		
		
		
		//Checking the interrelated consistency of all dynamic sections together
		//TODO
	}
	
	public boolean haveDynamicSpanOnSubstanceLevel()
	{	
		
		logger.info("dynamicSpanInfo:\n" + dynamicSpanInfo.toString());
		
		if ((dynamicSpanInfo.substanceArray_Index != DynamicSpanInfo.INDEX_NONE) || 
				(dynamicSpanInfo.substance_Indices != null)  )
			return true;
		
		return false;
	}
	
	public boolean haveDynamicSpan()
	{	
		if (dynamicIterationSpan != null)
			return true;
		
		
		for (int i = 0; i < parallelSheets.size(); i++)
			if (parallelSheets.get(i).dynamicIterationSpan != null)
				return true;
		
		return false;
	}
	
	
	public void analyzeDynamicSpanInfo()
	{
		dynamicSpanInfo = new DynamicSpanInfo();
		
		ArrayList<Integer> substanceArray_Indices = new ArrayList<Integer>(); 
		ArrayList<DynamicIterationSpan> substanceArray_DS = new ArrayList<DynamicIterationSpan>();
		
		ArrayList<Integer> substance_Indices = new ArrayList<Integer>();
		ArrayList<DynamicIterationSpan> substance_DS = new ArrayList<DynamicIterationSpan>();
		
		if (dynamicIterationSpan != null)
		{	
			switch(dynamicIterationSpan.cumulativeObjectType)
			{
			case SUBSTANCE_ARRAY:
				substanceArray_Indices.add(DynamicSpanInfo.INDEX_PRIMARY_SHEET);
				substanceArray_DS.add(dynamicIterationSpan);
				break;
			case SUBSTANCE:
				substance_Indices.add(DynamicSpanInfo.INDEX_PRIMARY_SHEET);
				substance_DS.add(dynamicIterationSpan);
				break;
				
			default:
				break;
			}
			
		}		
		
		
		for (int i = 0; i < parallelSheets.size(); i++)
			if (parallelSheets.get(i).dynamicIterationSpan != null)
			{	
				switch(parallelSheets.get(i).dynamicIterationSpan.cumulativeObjectType)
				{
				case SUBSTANCE_ARRAY:
					substanceArray_Indices.add(i);
					substanceArray_DS.add(parallelSheets.get(i).dynamicIterationSpan);
					break;
				case SUBSTANCE:
					substance_Indices.add(i);
					substance_DS.add(parallelSheets.get(i).dynamicIterationSpan);
					break;
					
				default:
					break;
				}
			}
		
		
		if (!substanceArray_Indices.isEmpty())
		{	
			if (substanceArray_Indices.size() == 1)
			{	
				dynamicSpanInfo.substanceArray_Index = substanceArray_Indices.get(0);
				dynamicSpanInfo.substanceArray_DS = substanceArray_DS.get(0);
			}	
			else
			{	
				configErrors.add("Dynamic span on SUBSTANCE_ARRAY level is duplicated in following sheets: " 
						+ DynamicSpanInfo.indicesToSheetMessageString(substanceArray_Indices));
			}	
		}
		
		
		if (!substance_Indices.isEmpty())
		{
			dynamicSpanInfo.substance_Indices = new int[substance_Indices.size()];
			dynamicSpanInfo.substance_DS = new DynamicIterationSpan[substance_Indices.size()];
			for (int i = 0; i < substance_Indices.size(); i++)
			{	
				dynamicSpanInfo.substance_Indices[i] = substance_Indices.get(i);
				dynamicSpanInfo.substance_DS[i] = substance_DS.get(i);
			}	
		}
		
	}
	
	
}
