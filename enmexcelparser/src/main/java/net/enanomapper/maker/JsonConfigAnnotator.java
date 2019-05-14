package net.enanomapper.maker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.enanomapper.parser.EffectRecordDataLocation;
import net.enanomapper.parser.ExcelDataLocation;
import net.enanomapper.parser.ExcelParserConfigurator;
import net.enanomapper.parser.KEYWORD;
import net.enanomapper.parser.ParserConstants.ElementField;
import net.enanomapper.parser.ProtocolApplicationDataLocation;

/**
 * JRC/NANoREG templates annotator
 * 
 * @author nina
 *
 */
public class JsonConfigAnnotator implements IAnnotator {
	protected ExcelParserConfigurator config;
	protected Map<Integer, String> header_lookup = new TreeMap<Integer, String>();
	protected Map<Integer, String[]> lookup = new HashMap<Integer, String[]>();

	public JsonConfigAnnotator(ExcelParserConfigurator config) {
		setConfig(config);
	}

	public JsonConfigAnnotator(File jsonConfig) throws FileNotFoundException, IOException, JsonProcessingException {
		setConfig(ExcelParserConfigurator.loadFromJSON(jsonConfig));
	}

	public ExcelParserConfigurator getConfig() {
		return config;
	}

	public void setConfig(ExcelParserConfigurator config) {
		this.config = config;
		processDataLocation(config);
	}

	@Override
	public void process(TR record) {
		process(record, getQueryField(), null, 1, null);
	}

	@Override
	public void process(TR record, String queryField, String query, int maxhits, String label) {
		try {
			int row = record.getRow();

			if (row == config.startHeaderRow) {
				record.setAnnotation("header0");
				header_lookup.put(record.getColumn(), record.getValue());
			} else if (row > config.startHeaderRow && row <= config.endHeaderRow) {
				setAnnotation(record);
				int col = record.getColumn();
				String[] levels = lookup.get(col);
				if (levels != null) {
					record.setJsonLevel1(levels[0]);
					record.setJsonLevel2(levels[1]);
					if (KEYWORD.PROTOCOL_APPLICATIONS.name().equals(levels[0])) {
						record.setJsonLevel3(levels[3]);
					} else {
						if (levels[2] != null)
							record.setValueClean(levels[2]);
						record.setJsonLevel3(levels[3]);
						record.setUnit(levels[4]);
					}
					record.setEndpoint(levels[5]);
				}
			} else {
				record.setAnnotation("data");
			}

		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	protected void setAnnotation(TR record) {
		record.setAnnotation("header1");
		Iterator<Integer> icol = header_lookup.keySet().iterator();
		Integer left = null;
		while (icol.hasNext()) {
			Integer right = icol.next();
			if (left != null)
				try {
					if (record.getColumn() >= left && record.getColumn() < right) {
						record.setAnnotation(header_lookup.get(left));
						break;
					}

				} catch (Exception e) {
				}
			left = right;
		}

	}

	@Override
	public String getLabel_tag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTerm_tag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTerm_tag(String term_tag) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getQueryField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setQueryField(String queryField) {
		// TODO Auto-generated method stub

	}

	public void processDataLocation(ExcelParserConfigurator config) {

		Iterator<String> keys = config.substanceLocations.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			processDataLocation(config.substanceLocations.get(key), KEYWORD.SUBSTANCE_RECORD.name(), key, null, null,
					null,null);
		}

		for (ProtocolApplicationDataLocation papp : config.protocolAppLocations) {
			Object section = papp.protocolCategoryCode.getJsonValue();

			processDataLocation(papp.protocolTopCategory, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.PROTOCOL_TOP_CATEGORY,
					section);
			processDataLocation(papp.protocolCategoryCode, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.PROTOCOL_CATEGORY_CODE, section);
			processDataLocation(papp.protocolCategoryTitle, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.PROTOCOL_CATEGORY_TITLE, section);
			processDataLocation(papp.protocolEndpoint, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.PROTOCOL_ENDPOINT, 
					section);

			processDataLocation(papp.assayUUID, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.ASSAY_UUID, section);
			processDataLocation(papp.citationOwner, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.CITATION_OWNER,section);
			processDataLocation(papp.citationTitle, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.CITATION_TITLE, section);
			processDataLocation(papp.citationYear, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.CITATION_YEAR, section);
			processDataLocation(papp.interpretationCriteria, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.INTERPRETATION_CRITERIA, section);
			processDataLocation(papp.interpretationResult, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.INTERPRETATION_RESULT,
					section);
			processDataLocation(papp.investigationUUID, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.INVESTIGATION_UUID, section);
			processDataLocation(papp.protocolApplicationUUID, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.PROTOCOL_APPLICATION_UUID, section);

			for (ExcelDataLocation loc : papp.protocolGuideline)
				processDataLocation(loc, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.PROTOCOL_GUIDELINE,section);
			processDataLocation(papp.reliability_isRobustStudy, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.RELIABILITY_IS_ROBUST_STUDY, section);
			processDataLocation(papp.reliability_isUsedforClassification, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.RELIABILITY_IS_USED_FOR_CLASSIFICATION, section);
			processDataLocation(papp.reliability_isUsedforMSDS, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.RELIABILITY_IS_USED_FOR_MSDS, section);
			processDataLocation(papp.reliability_purposeFlag, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.RELIABILITY_PURPOSE_FLAG, section);
			processDataLocation(papp.reliability_studyResultType, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.RELIABILITY_STUDY_RESULT_TYPE, section);
			processDataLocation(papp.reliability_value, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.RELIABILITY_VALUE, 
					section);

			keys = papp.parameters.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				ExcelDataLocation prm = papp.parameters.get(key);
				String unit = null;
				if (prm.otherLocationFields != null)
					try {
						unit = prm.otherLocationFields.get("UNIT").getJsonValue().toString();
					} catch (Exception x) {

					}
				processDataLocation(prm, KEYWORD.PARAMETERS, (ElementField) null, key, null, unit,section);

			}
			for (EffectRecordDataLocation loc : papp.effects) {
				Object endpoint = null;
				Object endpointType = null;
				Object unit = null;
				if (loc.endpoint != null && loc.endpoint.getJsonValue() != null)
					endpoint = loc.endpoint.getJsonValue();
				if (loc.endpointType != null && loc.endpointType.getJsonValue() != null)
					endpointType = loc.endpointType.getJsonValue();
				if (loc.unit != null && loc.unit.getJsonValue() != null)
					unit = loc.unit.getJsonValue();
				processDataLocation(loc.endpointType, KEYWORD.EFFECTS, ElementField.ENDPOINT_TYPE, endpoint,
						endpointType, unit,section);
				processDataLocation(loc.errQualifier, KEYWORD.EFFECTS, ElementField.ERR_QUALIFIER, endpoint,
						endpointType, unit,section);
				processDataLocation(loc.errValue, KEYWORD.EFFECTS, ElementField.ERR_VALUE, endpoint, endpointType,
						unit,section);
				processDataLocation(loc.loQualifier, KEYWORD.EFFECTS, ElementField.LO_QUALIFIER, endpoint, endpointType,
						unit,section);
				processDataLocation(loc.loValue, KEYWORD.EFFECTS, ElementField.LO_VALUE, endpoint, endpointType, unit,section);
				processDataLocation(loc.sampleID, KEYWORD.EFFECTS, ElementField.SAMPLE_ID, endpoint, endpointType,
						unit,section);
				processDataLocation(loc.textValue, KEYWORD.EFFECTS, ElementField.TEXT_VALUE, endpoint, endpointType,
						unit,section);
				processDataLocation(loc.unit, KEYWORD.EFFECTS, ElementField.UNIT, endpoint, endpointType, unit,section);
				processDataLocation(loc.upQualifier, KEYWORD.EFFECTS, ElementField.UP_QUALIFIER, endpoint, endpointType,
						unit,section);
				processDataLocation(loc.value, KEYWORD.EFFECTS, ElementField.VALUE, endpoint, endpointType, unit,section);
				processDataLocation(loc.upValue, KEYWORD.EFFECTS, ElementField.UP_VALUE, endpoint, endpointType, unit,section);
				if (loc.conditions != null) {
					keys = loc.conditions.keySet().iterator();
					while (keys.hasNext()) {
						String key = keys.next();
						ExcelDataLocation condition = loc.conditions.get(key);
						unit = null;
						if (condition.otherLocationFields != null)
							try {
								unit = condition.otherLocationFields.get("UNIT").getJsonValue().toString();
							} catch (Exception x) {

							}
						processDataLocation(condition, KEYWORD.EFFECTS, ElementField.CONDITION, key, null, unit, section);
					}
				}

			}

		}

	}

	protected void processDataLocation(ExcelDataLocation loc, KEYWORD type_parent, String type, Object section
			) {
		processDataLocation(loc, type_parent == null ? null : type_parent.name(), type, null,null,null,section);
	}

	protected void processDataLocation(ExcelDataLocation loc, KEYWORD type_parent, ElementField type, Object endpoint,
			Object endpointType, Object unit,Object section) {
		processDataLocation(loc, type_parent == null ? null : type_parent.name(), type == null ? null : type.name(),
				endpoint,endpointType,unit,section);
	}

	protected void processDataLocation(ExcelDataLocation loc, KEYWORD type_parent, KEYWORD type, Object section) {
		// Object value = loc==null?null:loc.getJsonValue();
		processDataLocation(loc, type_parent == null ? null : type_parent.name(), type == null ? null : type.name(),
				null,null,null,section);
	}

	protected void processDataLocation(ExcelDataLocation loc, String type_parent, String type, Object endpoint,
			Object endpointType, Object unit, Object section) {
		if (loc == null)
			return;
		if (loc.getAbsoluteLocationValue() == null) {

			lookup.put(loc.columnIndex, new String[] { type_parent, type, endpoint == null ? null : endpoint.toString(),
					endpointType == null ? null : endpointType.toString(), unit == null ? null : unit.toString(), section==null?null:section.toString() });

		} else {

			processDataLocation((ExcelDataLocation) loc.getAbsoluteLocationValue(), type_parent, type, endpoint,
					endpointType, unit, section);
		}
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void done() {
		// TODO Auto-generated method stub
		
	}
}
