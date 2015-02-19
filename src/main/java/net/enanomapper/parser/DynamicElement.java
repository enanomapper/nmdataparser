package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.JsonNode;

import ambit2.base.data.Property;
import ambit2.base.data.StructureRecord;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.substance.ExternalIdentifier;
import ambit2.base.relation.STRUCTURE_RELATION;
import ambit2.base.relation.composition.CompositionRelation;
import ambit2.base.relation.composition.Proportion;
import net.enanomapper.parser.ParserConstants.ObjectType;
import net.enanomapper.parser.ParserConstants.ElementField;
import net.enanomapper.parser.ParserConstants.ElementPosition;
import net.enanomapper.parser.ParserConstants.ElementSynchronization;
import net.enanomapper.parser.json.JsonUtilities;
import net.enanomapper.parser.recognition.RecognitionUtils;
import net.enanomapper.parser.recognition.RichValue;
import net.enanomapper.parser.recognition.RichValueParser;


public class DynamicElement 
{
	//public ObjectType dataType =  null;   //it may be used if needed!
	//public boolean FlagDataType = false;
	
	public ElementField fieldType = ElementField.NONE;
	public boolean FlagFieldType = false;

	public int resultObjectId = 0;     //If id != 0 it can be used to put field in a newly created Object i
	public boolean FlagResultObjectId = false;
	
	public ElementSynchronization synchType = ElementSynchronization.NONE;
	public boolean FlagSynchType = false;
	
	public SynchronizationTarget synchTarget = null;

	public ElementPosition position = ElementPosition.ANY_ROW;
	public boolean FlagPosition = false;

	public int index = -1;
	public boolean FlagIndex = false;

	public String jsonInfo = null;

	public String parameterName = null;  //This field is used by fieldTypes: PARAMETER, CONDITION and PROPERTY
	
	public boolean infoFromHeader = true;
	public boolean FlagInfoFromHeader = false;

	public String variableKeys[] = null; //The information is taken (constructed) form the variables defined by their keys

	public int childElements[] = null; //The information is taken from another element
	
	//public String sourceDIS = null; //The information is taken from another DIS  

	public static DynamicElement  extractDynamicElement(JsonNode node, ExcelParserConfigurator conf, 
			String masterSection, int elNum)
	{
		DynamicElement element = new DynamicElement();
		JsonUtilities jsonUtils = new JsonUtilities();

		/*
		//DATA_TYPE
		if(node.path("DATA_TYPE").isMissingNode())
		{
			if (node.path("FIELD_TYPE").isMissingNode())
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword + \"DATA_TYPE\" and \"FIELD_TYPE\" are missing!"
						+ " At least one must be specified.");
		}
		else
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "DATA_TYPE", false);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"DATA_TYPE\": " + jsonUtils.getError());
			else
			{	
				element.dataType = ObjectType.fromString(keyword);
				if (element.dataType == ObjectType.UNDEFINED)
					conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
							+" subsection ELEMENT [" +(elNum +1) + "], keyword \"DATA_TYPE\" is incorrect or UNDEFINED!  -->"  + keyword);
				else
					element.FlagDataType = true;
			}	
		}
		*/

		//FIELD_TYPE
		if(!node.path("FIELD_TYPE").isMissingNode())
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "FIELD_TYPE", false);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"FIELD_TYPE\": " + jsonUtils.getError());
			else
			{	
				element.fieldType = ElementField.fromString(keyword);
				if (element.fieldType == ElementField.UNDEFINED)
					conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
							+" subsection ELEMENT [" +(elNum +1) + "], keyword \"FIELD_TYPE\" is incorrect or UNDEFINED!  -->"  + keyword);
				else
				{	
					element.FlagFieldType = true;
					
					if (( element.fieldType == ElementField.PARAMETER) || 
						 (element.fieldType == ElementField.CONDITION) ||
						 (element.fieldType == ElementField.PROPERTY)  ||
						 (element.fieldType == ElementField.EXTERNAL_IDENTIFIER)  )
					{	
						if(node.path("PARAMETER_NAME").isMissingNode())
						{
							conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
									+" subsection ELEMENT [" +(elNum +1) +
									"], keyword \"PARAMETER_NAME\" is missing for FIELD_TYPE = " + element.fieldType.toString());
						}
					}
					
					/*
					// Setting dataType / Checking field compatibility with dataType
					if (element.dataType == null)
						element.dataType = element.fieldType.getElement();
					else
					{
						if (element.dataType != element.fieldType.getElement())
							conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
									+" subsection ELEMENT [" +(elNum +1) + "], FIELD_TYPE \"" + element.fieldType.toString() + 
									"\" is incompatible with DATA_TYPE \"" + element.dataType.toString() + "\"");
					}
					*/
				}	
			}	
		}
		
		
		//RESULT_OBJECT_ID
		if(!node.path("RESULT_OBJECT_ID").isMissingNode())
		{
			Integer resId =  jsonUtils.extractIntKeyword(node, "RESULT_OBJECT_ID", false);
			if (resId == null)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"FIELD_IN_NEW_OBJECT\": " + jsonUtils.getError());
			else
			{	
				element.resultObjectId = resId - 1;  //1-based --> 0-based
				element.FlagResultObjectId = true;
			}	
		}
		
		//SYNCH_TYPE
		if(!node.path("SYNCH_TYPE").isMissingNode())
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "SYNCH_TYPE", false);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"SYNCH_TYPE\": " + jsonUtils.getError());
			else
			{	
				element.synchType = ElementSynchronization.fromString(keyword);
				if (element.synchType == ElementSynchronization.UNDEFINED)
					conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
							+" subsection ELEMENT [" +(elNum +1) + "], keyword \"SYNCH_TYPE\" is incorrect or UNDEFINED!  -->"  + keyword);
				else
				{	
					element.FlagSynchType = true;
				}	
			}	
		}


		//POSITION
		if(!node.path("POSITION").isMissingNode())
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "POSITION", false);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"POSITION\": " + jsonUtils.getError());
			else
			{	
				element.position = ElementPosition.fromString(keyword);
				if (element.position == ElementPosition.UNDEFINED)
					conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
							+" subsection ELEMENT [" +(elNum +1) + "], keyword \"POSITION\" is incorrect or UNDEFINED!  -->"  + keyword);
				else
					element.FlagPosition = true;
			}	
		}


		//INDEX
		if(node.path("INDEX").isMissingNode())
		{
			//It is not treated as an error
			//conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
			//		+" subsection ELEMENT [" +(elNum +1) + "], keyword \"INDEX\" is missing!");
		}
		else
		{
			//Index is extracted as column index (but it may be a row as well)
			int col_index = ExcelParserUtils.extractColumnIndex(node.path("INDEX"));
			if (col_index == -1)
			{
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword  \"INDEX\" is incorrect!");
			}
			else
			{	
				element.index = col_index;
				element.FlagIndex = true;
			}
		}


		//JSON_INFO
		if(!node.path("JSON_INFO").isMissingNode())
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "JSON_INFO", false);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"JSON_INFO\": " + jsonUtils.getError());
			else
			{	
				element.jsonInfo = keyword;
			}	
		}
		
		//PARAMETER_NAME
		if(!node.path("PARAMETER_NAME").isMissingNode())
		{
			String keyword =  jsonUtils.extractStringKeyword(node, "PARAMETER_NAME", false);
			if (keyword == null)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"PARAMETER_NAME\": " + jsonUtils.getError());
			else
			{	
				element.parameterName = keyword;
			}	
		}

		//INFO_FROM_HEADER
		if(!node.path("INFO_FROM_HEADER").isMissingNode())
		{
			Boolean b =  jsonUtils.extractBooleanKeyword(node, "INFO_FROM_HEADER", true);
			if (b == null)
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"INFO_FROM_HEADER\": " + jsonUtils.getError());
			else
			{	
				element.infoFromHeader = b;
				element.FlagInfoFromHeader = true;
			}	
		}

		//VARIABLE_KEYS
		JsonNode vkeys = node.path("VARIABLE_KEYS");
		if(!vkeys.isMissingNode())
		{
			if (vkeys.isArray())
			{	
				element.variableKeys = new String[vkeys.size()];
				for (int i = 0; i < vkeys.size(); i++)
				{	
					JsonNode keyNode = vkeys.get(i);
					if (keyNode.isTextual())
					{	
						String keyword =  keyNode.asText();
						if (keyword == null)
							conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
									+" subsection ELEMENT [" +(elNum +1) + "], keyword VARIABLE_KEYS [" + (i+1)+"]: is incorrect!");
						else
							element.variableKeys[i] = keyword;
					}
					else
					{	
						conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
								+" subsection ELEMENT [" +(elNum +1) + "], keyword VARIABLE_KEYS [" + (i+1)+"]: is not textual!");
					}
				}
			}
			else
			{
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"VARIABLE_KEYS\" is not an array!");
			}
		}

		//CHILD_ELEMENTS
		JsonNode children = node.path("CHILD_ELEMENTS");
		if(!children.isMissingNode())
		{
			if (children.isArray())
			{	
				element.childElements = new int [children.size()];
				for (int i = 0; i < children.size(); i++)
				{	
					JsonNode chNode = children.get(i);
					if (chNode.isInt())
					{	
						int intVal =  chNode.asInt();
						if (intVal <= 0)
							conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
									+" subsection ELEMENT [" +(elNum +1) + "], keyword CHILD_ELEMENTS [" + (i+1)+"]: is incorrect! --> " + intVal);
						else
							element.childElements[i] = intVal -1; //1-based --> 0-based
					}
					else
					{	
						conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
								+" subsection ELEMENT [" +(elNum +1) + "], keyword CHILD_ELEMENTS [" + (i+1)+"]: is not integer!");
					}
				}
			}
			else
			{
				conf.configErrors.add("In JSON Section \"" + masterSection + "\" subsection \"DYNAMIC_ITERATION_SPAN\", "
						+" subsection ELEMENT [" +(elNum +1) + "], keyword \"CHILD_ELEMENTS\" is not an array!");
			}
		}

		return element;
	}


	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");

		/*
		if (FlagDataType)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"DATA_TYPE\" : \"" + dataType.toString() + "\"");
			nFields++;
		}
		*/

		if (FlagFieldType)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"FIELD_TYPE\" : \"" + fieldType.toString() + "\"");
			nFields++;
		}
		
		
		if (FlagResultObjectId)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"RESULT_OBJECT_ID\" : " + (resultObjectId + 1));
			nFields++;
		}
		
		
		if (FlagSynchType)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"SYNCH_TYPE\" : \"" + synchType.toString() + "\"");
			nFields++;
		}

		if (FlagPosition)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"POSITION\" : \"" + position.toString() + "\"");
			nFields++;
		}

		if (FlagIndex)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"INDEX\" : " + (index + 1));
			nFields++;
		}

		if (jsonInfo != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"JSON_INFO\" : \"" + jsonInfo + "\"");
			nFields++;
		}
		
		if (parameterName != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"PARAMETER\" : \"" + parameterName + "\"");
			nFields++;
		}

		if (FlagInfoFromHeader)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"INFO_FROM_HEADER\" : " + infoFromHeader + "");
			nFields++;
		}

		if (variableKeys != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"VARIABLE_KEYS\" : [" );
			for (int i = 0; i < variableKeys.length; i++)
			{	
				sb.append("\"" + variableKeys[i] + "\"");
				if (i < (variableKeys.length -1))
					sb.append(", ");
			}	
			sb.append("]");
			nFields++;
		}

		if (childElements != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"CHILD_ELEMENTS\" : [" );
			for (int i = 0; i < childElements.length; i++)
			{	
				sb.append((childElements[i] + 1));    //0-based --> 1-based
				if (i < (childElements.length -1))
					sb.append(", ");
			}	
			sb.append("]");
			nFields++;
		}

		if (nFields > 0)
			sb.append("\n");

		sb.append(offset + "}");

		return sb.toString();
	}
	
	
	public void putElementInUniversalObject(Object elObj, UniversalObject uniObj)
	{
		ObjectType otype = fieldType.getObjectType();
		switch (otype)
		{
		case SUBSTANCE:
			SubstanceRecord sr = uniObj.getSubstanceRecord();
			putElementInSubstanceRecord(elObj, sr);
			break;
			
		case PROTOCOL_APPLICATION:
			ProtocolApplication pa = uniObj.getProtocolApplication();
			putElementInProtocolApplication(elObj, pa);
			break;
			
		case PROTOCOL:
			Protocol protocol = uniObj.getProtocol();
			putElementInProtocol(elObj, protocol);
			break;
			
		case EFFECT:
			EffectRecord er = uniObj.getEffect();
			putElementInEffectRecord(elObj, er);
			break;	
			
		case COMPOSITION:
			CompositionRelation comp = uniObj.getComposition(resultObjectId);
			putElementInComposition(elObj, comp);
			break;
		
		default:
			//does noting
		}
	}
	
	
	/*
	public void putElementInSubstanceRecordArray(Object elObj, ArrayList<SubstanceRecord> substanceRecords)
	{
		//TODO
	}
	*/
		
	public void putElementInSubstanceRecord(Object elObj, SubstanceRecord substanceRecord)
	{
		if (elObj == null)
			return;
		
		switch (fieldType)
		{
		case COMPANY_NAME:
			substanceRecord.setCompanyName(elObj.toString());
			break;
			
		case COMPANY_UUID:
			String s = elObj.toString();
			substanceRecord.setCompanyUUID("XLSX-"+UUID.nameUUIDFromBytes(s.getBytes()).toString());
			break;
			
		case OWNER_NAME:
			substanceRecord.setOwnerName(elObj.toString());
			break;
			
		case OWNER_UUID:
			substanceRecord.setCompanyUUID(elObj.toString()); 
			break;
			
		case SUBSTANCE_TYPE:
			substanceRecord.setSubstancetype(elObj.toString());
			break;
			
		case PUBLIC_NAME:
			substanceRecord.setPublicName(elObj.toString());
			break;	
			
		case ID_SUBSTANCE:
			if (elObj instanceof Double)
				substanceRecord.setIdsubstance(((Double)elObj).intValue());
			break;	
			
		case EXTERNAL_IDENTIFIER:
			List<ExternalIdentifier> ids = substanceRecord.getExternalids();
			if (ids == null)
			{
				ids = new ArrayList<ExternalIdentifier>();
				substanceRecord.setExternalids(ids);
			}
			ids.add(new ExternalIdentifier(parameterName, elObj.toString()));
			break;
			
		default:
			//The other element fields are not used by SubstanceRecord
		}
	}
	
	
	public void putElementInProtocolApplication(Object elObj, ProtocolApplication protocolApplication)
	{
		if (elObj == null)
			return;
		
		switch (fieldType)
		{
		case CITATION_TITLE:
			protocolApplication.setReference(elObj.toString());  //title is the reference 'itself'
			break;
		case CITATION_YEAR:
			protocolApplication.setReferenceYear(elObj.toString());
			break;
		case CITATION_OWNER:
			protocolApplication.setReferenceOwner(elObj.toString());
			break;	
		
		//TODO handle reliability	
			
		case PARAMETER:
			IParams params;
			Object p = protocolApplication.getParameters();
			if (p == null)
			{
				params = new Params();
				protocolApplication.setParameters(params);
			}
			else
				params = (IParams) p;
			params.put(parameterName, elObj);
			break;
		
		case INTERPRETATION_RESULT:
			protocolApplication.setInterpretationResult(elObj.toString());
			break;		
		
		case INTERPRETATION_CRITERIA:
			protocolApplication.setInterpretationCriteria(elObj.toString());
			break;		
		
		default:
			//The other element fields are not used by ProtocolApplication
		}
		
	}
	
	public void putElementInProtocol(Object elObj, Protocol protocol)
	{
		if (elObj == null)
			return;
		
		switch (fieldType)
		{
		case PROTOCOL_TOP_CATEGORY:
			protocol.setTopCategory(elObj.toString());
			break;
			
		case PROTOCOL_CATEGORY_CODE:
			protocol.setCategory(elObj.toString());
			break;	
			
		case PROTOCOL_CATEGORY_TITLE:
			//Currently it is not handled
			break;	
		
		case PROTOCOL_GUIDELINE:
			List<String> guideline = protocol.getGuideline();
			if (guideline == null)
			{	
				guideline = new ArrayList<String>();
				protocol.setGuideline(guideline);
			}	
			//Add info to guideline:
			guideline.add(elObj.toString());
			break;
			
		default:
			//The other element fields are not used by Protocol
		}
	}
	
	public void putElementInEffectRecord(Object elObj, EffectRecord effect)
	{
		if (elObj == null)
			return;
		
		switch (fieldType)
		{
		case ENDPOINT:
			effect.setEndpoint(elObj.toString());
			break;
			
		case SAMPLE_ID:
			effect.setSampleID(elObj.toString());
			break;
			
		case UNIT:
			effect.setUnit(elObj.toString());
			break;
			
		case LO_QUALIFIER:
			String lo_q = elObj.toString();	
			if (ExcelParserConfigurator.isValidQualifier(lo_q))
				effect.setLoQualifier(lo_q);
			else
				{  /* Handle error! */ }
			break;
			
		case LO_VALUE:
			if (elObj instanceof String)
			{
				RecognitionUtils.QualifierValue qv =  RecognitionUtils.extractQualifierValue(elObj.toString());
				if (qv.value != null)
				{
					effect.setLoValue(qv.value);
					if (qv.qualifier != null)
						effect.setLoQualifier(qv.qualifier);  //this qualifier takes precedence (if already set)
				}
				else
				{  /* Handle error! */ }
			}
			else
				if (elObj instanceof Double)
				{
					effect.setLoValue((Double) elObj);
				}
			break;
			
		case UP_QUALIFIER:
			String up_q = elObj.toString();	
			if (ExcelParserConfigurator.isValidQualifier(up_q))
				effect.setUpQualifier(up_q);
			else
				{  /* Handle error! */ }
			break;
			
		case UP_VALUE:
			if (elObj instanceof String)
			{
				RecognitionUtils.QualifierValue qv =  RecognitionUtils.extractQualifierValue(elObj.toString());
				if (qv.value != null)
				{
					effect.setUpValue(qv.value);
					if (qv.qualifier != null)
						effect.setUpQualifier(qv.qualifier);  //this qualifier takes precedence (if already set)
				}
				else
				{  /* Handle error! */ }
			}
			else
				if (elObj instanceof Double)
				{
					effect.setUpValue((Double) elObj);
				}
			break;	
			
		case TEXT_VALUE:
			effect.setTextValue(elObj.toString());
			break;	
			
		case ERR_QUALIFIER:
			String err_q = elObj.toString();	
			if (ExcelParserConfigurator.isValidQualifier(err_q))
				effect.setErrQualifier(err_q);
			else
				{  /* Handle error! */ }
			break;
			
		case ERR_VALUE:
			if (elObj instanceof String)
			{
				RecognitionUtils.QualifierValue qv =  RecognitionUtils.extractQualifierValue(elObj.toString());
				if (qv.value != null)
				{
					effect.setErrorValue(qv.value);
					if (qv.qualifier != null)
						effect.setErrQualifier(qv.qualifier);  //this qualifier takes precedence (if already set)
				}
				else
				{  /* Handle error! */ }
			}
			else
				if (elObj instanceof Double)
				{
					effect.setErrorValue((Double) elObj);
				}
			break;		
			
		case VALUE:
			if (elObj instanceof String)
			{
				RichValueParser rvParser = new RichValueParser ();
				RichValue rv = rvParser.parse(elObj.toString());
				String rv_error = rvParser.getAllErrorsAsString(); 
				
				if (rv_error == null)
				{
					if (rv.unit != null)
						effect.setUnit(rv.unit);
					if (rv.loValue != null)
						effect.setLoValue(rv.loValue);
					if (rv.loQualifier != null)
						effect.setLoQualifier(rv.loQualifier);
					if (rv.upValue != null)
						effect.setUpValue(rv.upValue);
					if (rv.upQualifier != null)
						effect.setUpQualifier(rv.upQualifier);
				}
				else
				{  /* Handle error! */ }
			}
			else
				if (elObj instanceof Double)
				{
					effect.setLoValue((Double) elObj);  //This is the default behavior if the cell is of type numeric
				}
			break;
		
		case CONDITION:
			IParams params;
			Object p = effect.getConditions();
			if (p == null)
			{
				params = new Params();
				effect.setConditions(params);
			}
			else
				params = (IParams) p;
			params.put(parameterName, elObj);
			break;
			
		default:
			//The other element fields are not used by EffectRecord
		}	
	}
	
	public void putElementInComposition(Object elObj, CompositionRelation composition)
	{
		if (elObj == null)
			return;
		
		//System.out.println("----> putElementInComposition: " + elObj.toString());
		
		switch (fieldType)
		{
		case STRUCTURE_RELATION:
			STRUCTURE_RELATION strRel = CompositionDataLocation.structureRelationFromString(elObj.toString());
			if (strRel == null)
			{
				//error!
			}
			else
				composition.setRelationType(strRel);
			break;
		
		case CONTENT:
			if (composition.getSecondStructure() == null)
				composition.setSecondStructure(new StructureRecord());
			composition.getSecondStructure().setContent(elObj.toString());
			break;
		
		case FORMAT:
			if (composition.getSecondStructure() == null)
				composition.setSecondStructure(new StructureRecord());
			composition.getSecondStructure().setFormat(elObj.toString());
			break;	
		
		case INCHI_KEY:
			if (composition.getSecondStructure() == null)
				composition.setSecondStructure(new StructureRecord());
			composition.getSecondStructure().setInchiKey(elObj.toString());
			break;	
			
		case INCHI:
			if (composition.getSecondStructure() == null)
				composition.setSecondStructure(new StructureRecord());
			composition.getSecondStructure().setInchi(elObj.toString());
			break;
			
		case FORMULA:
			if (composition.getSecondStructure() == null)
				composition.setSecondStructure(new StructureRecord());
			composition.getSecondStructure().setFormula(elObj.toString());
			break;	
			
		case SMILES:
			if (composition.getSecondStructure() == null)
				composition.setSecondStructure(new StructureRecord());
			composition.getSecondStructure().setSmiles(elObj.toString());
			break;	
			
		case PROPERTY:
			if (composition.getSecondStructure() == null)
				composition.setSecondStructure(new StructureRecord());
			
			String sameas = Property.guessLabel(parameterName);
			Property property = new Property(parameterName, "", "");
			property.setLabel(sameas);
			composition.getSecondStructure().setProperty(property, elObj);
			break;		
			
		case PROPORTION_FUNCTION:
			if (composition.getRelation() == null)
				composition.setRelation(new Proportion());
			composition.getRelation().setFunction(elObj.toString());
			break;
			
		case PROPORTION_TYPICAL_PRECISION:
			if (composition.getRelation() == null)
				composition.setRelation(new Proportion());
			composition.getRelation().setTypical(elObj.toString());
			break;	
			
		case PROPORTION_TYPICAL_VALUE:
			if (elObj instanceof Double)
			{	
				if (composition.getRelation() == null)
					composition.setRelation(new Proportion());
				composition.getRelation().setTypical_value((Double)elObj);
			}
			break;	
			
		case PROPORTION_TYPICAL_UNIT:
			if (composition.getRelation() == null)
				composition.setRelation(new Proportion());
			composition.getRelation().setTypical_unit(elObj.toString());
			break;	
			
		case PROPORTION_REAL_LOWER_PRECISION:
			if (composition.getRelation() == null)
				composition.setRelation(new Proportion());
			composition.getRelation().setReal_lower(elObj.toString());
			break;	
			
		case PROPORTION_REAL_LOWER_VALUE:
			if (elObj instanceof Double)
			{	
				if (composition.getRelation() == null)
					composition.setRelation(new Proportion());
				composition.getRelation().setReal_lowervalue((Double)elObj);
			}
			break;	
			
		case PROPORTION_REAL_UPPER_PRECISION:
			if (composition.getRelation() == null)
				composition.setRelation(new Proportion());
			composition.getRelation().setReal_upper(elObj.toString());
			break;	
			
		case PROPORTION_REAL_UPPER_VALUE:
			if (elObj instanceof Double)
			{	
				if (composition.getRelation() == null)
					composition.setRelation(new Proportion());
				composition.getRelation().setReal_uppervalue((Double)elObj);
			}
			break;		
		
		case PROPORTION_REAL_UNIT:
			if (composition.getRelation() == null)
				composition.setRelation(new Proportion());
			composition.getRelation().setReal_unit(elObj.toString());
			break;	
			
		default:
			//The other element fields are not used by CompositionRelation
		}
	}

}
