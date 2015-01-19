package net.enanomapper.parser;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.enanomapper.parser.ParserConstants.IterationAccess;
import net.enanomapper.parser.ParserConstants.Recognition;
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
	private static final int numGuideLinesToCheck = 5;
	private static final String guideLineJSONField = "guideline";
	
	public ArrayList<String> configErrors = new ArrayList<String> ();
	public ArrayList<String> configWarning = new ArrayList<String> ();
	
	//Configuration flags
	public boolean FlagAllowQualifierInValueCell = true;  //default
	
	//Configuration variables
	public String templateName = null;
	public String templateVersion = null;	
	public int templateType = 1;
	
	//Global configuration for the data access
	public boolean FlagSkipEmptyRows = true;
	public IterationAccess substanceIteration =  IterationAccess.ROW_SINGLE;
	public int rowMultiFixedSize = 1;
	public int startRow = 2;
	public int sheetIndex = 0;
	public int startHeaderRow = 0;
	public int endHeaderRow = 0;
	public boolean allowEmpty = true;
	public Recognition recognition = Recognition.BY_INDEX;
	
	//Specific data locations
	public HashMap<String, ExcelDataLocation> substanceLocations = new HashMap<String, ExcelDataLocation>();
	public ArrayList<ProtocolApplicationDataLocation> protocolAppLocations = new ArrayList<ProtocolApplicationDataLocation>();
	public HashMap<String,Object> jsonRepository = new HashMap<String,Object>();
	
	
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
			conf.configWarning.add("JSON Section \"TEMPLATE_INFO\" is missing!");
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
			//ITERATION
			String keyword =  jsonUtils.extractStringKeyword(curNode, "ITERATION", true);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
			{	
				conf.substanceIteration = IterationAccess.fromString(keyword);
				if (conf.substanceIteration == IterationAccess.UNDEFINED)
					conf.configErrors.add("In JSON Section \"DATA_ACCESS\", keyword \"ITERATION\" is incorrect or UNDEFINED!");
			}			
			//SHEET_INDEX
			Integer intValue = jsonUtils.extractIntKeyword(curNode, "SHEET_INDEX", false);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.sheetIndex = intValue - 1; //1-based --> 0-based
			//START_ROW
			intValue = jsonUtils.extractIntKeyword(curNode, "START_ROW", false);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.startRow = intValue - 1; //1-based --> 0-based			
			//START_HEADER_ROW
			intValue = jsonUtils.extractIntKeyword(curNode, "START_HEADER_ROW", false);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.startHeaderRow = intValue - 1; //1-based --> 0-based
			//END_HEADER_ROW
			intValue = jsonUtils.extractIntKeyword(curNode, "END_HEADER_ROW", false);
			if (intValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.endHeaderRow = intValue - 1 ; //1-based --> 0-based
			//ALLOW_EMPTY
			Boolean boolValue = jsonUtils.extractBooleanKeyword(curNode, "ALLOW_EMPTY", false);
			if (boolValue == null)
				conf.configErrors.add(jsonUtils.getError());
			else
				conf.allowEmpty = boolValue;
			//RECOGNITION
			keyword =  jsonUtils.extractStringKeyword(curNode, "RECOGNITION", true);
			if (keyword == null)
				conf.configErrors.add(jsonUtils.getError());
			else
			{	
				conf.recognition = Recognition.fromString(keyword);
				if (conf.recognition == Recognition.UNDEFINED)
					conf.configErrors.add("In JSON Section \"DATA_ACCESS\", keyword \"RECOGNITION\" is incorrect or UNDEFINED!");
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
		}
		
		
		//Handle Protocol Applications (Measurements)
		curNode = root.path("PROTOCOL_APPLICATIONS");
		if (curNode.isMissingNode())
			conf.configErrors.add("JSON Section \"PROTOCOL_APPLICATIONS\" is missing!");
		else
		{
			if (!curNode.isArray())
			{
				conf.configErrors.add("JSON Section \"PROTOCOL_APPLICATIONS\" is not array!");
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
		
		
		//Handle (1) external identifies and (2) composition
		//TODO
		
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
		sb.append("\t\t\"ITERATION\" : \"" + substanceIteration.toString() + "\",\n" );	
		sb.append("\t\t\"SHEET_INDEX\" : " + (sheetIndex + 1) + ",\n" ); //0-based --> 1-based
		sb.append("\t\t\"START_ROW\" : " + (startRow + 1) + ",\n" ); //0-based --> 1-based
		sb.append("\t\t\"START_HEADER_ROW\" : " + (startHeaderRow + 1) + ",\n" ); //0-based --> 1-based
		sb.append("\t\t\"END_HEADER_ROW\" : " + (endHeaderRow + 1) + ",\n" ); //0-based --> 1-based
		sb.append("\t\t\"ALLOW_EMPTY\" : \"" + allowEmpty + "\",\n" );	
		sb.append("\t\t\"RECOGNITION\" : \"" + recognition.toString() + "\",\n" );	
		sb.append("\t},\n\n");
		
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
			int col_index = extractColumnIndex(sectionNode.path("COLUMN_INDEX"));
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
		
		//TODO SHEET_INDEX + SHEET_NAME
		
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
				if (conf.FlagAllowQualifierInValueCell)
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
				if (conf.FlagAllowQualifierInValueCell)
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
				if (conf.FlagAllowQualifierInValueCell)
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
	
	public static int extractColumnIndex(JsonNode node)
	{
		if (node.isInt())
		{	
			Integer intValue = node.asInt();
			if (intValue == null)
				return -1;
			else
				return intValue - 1;  //1 --> 0
		}
		
		if (node.isTextual())
		{
			String s = node.asText();
			if (s == null)
				return -1;
			
			//TODO better check for the string
			int col = CellReference.convertColStringToIndex(s);
			if (col >= 0)			
				return col;
		}
		
		return -1;
	}
	
	public static boolean isValidQualifier(String qualifier)
	{
		for (String q : RecognitionUtils.qualifiers)
			if (q.equals(qualifier))
				return true;
		return false;
	}
}
