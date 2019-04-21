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
			processDataLocation(config.substanceLocations.get(key), KEYWORD.SUBSTANCE_RECORD.name(), key);
		}

		for (ProtocolApplicationDataLocation papp : config.protocolAppLocations) {
			processDataLocation(papp.assayUUID, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.ASSAY_UUID);
			processDataLocation(papp.citationOwner, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.CITATION_OWNER);
			processDataLocation(papp.citationTitle, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.CITATION_TITLE);
			processDataLocation(papp.citationYear, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.CITATION_YEAR);
			processDataLocation(papp.interpretationCriteria, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.INTERPRETATION_CRITERIA);
			processDataLocation(papp.interpretationResult, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.INTERPRETATION_RESULT);
			processDataLocation(papp.investigationUUID, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.INVESTIGATION_UUID);
			processDataLocation(papp.protocolApplicationUUID, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.PROTOCOL_APPLICATION_UUID);
			processDataLocation(papp.protocolCategoryCode, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.PROTOCOL_CATEGORY_CODE);
			processDataLocation(papp.protocolCategoryTitle, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.PROTOCOL_CATEGORY_TITLE);
			processDataLocation(papp.protocolEndpoint, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.PROTOCOL_ENDPOINT);
			processDataLocation(papp.protocolTopCategory, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.PROTOCOL_TOP_CATEGORY);
			for (ExcelDataLocation loc : papp.protocolGuideline)
				processDataLocation(loc, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.PROTOCOL_GUIDELINE);
			processDataLocation(papp.reliability_isRobustStudy, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.RELIABILITY_IS_ROBUST_STUDY);
			processDataLocation(papp.reliability_isUsedforClassification, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.RELIABILITY_IS_USED_FOR_CLASSIFICATION);
			processDataLocation(papp.reliability_isUsedforMSDS, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.RELIABILITY_IS_USED_FOR_MSDS);
			processDataLocation(papp.reliability_purposeFlag, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.RELIABILITY_PURPOSE_FLAG);
			processDataLocation(papp.reliability_studyResultType, KEYWORD.PROTOCOL_APPLICATIONS,
					KEYWORD.RELIABILITY_STUDY_RESULT_TYPE);
			processDataLocation(papp.reliability_value, KEYWORD.PROTOCOL_APPLICATIONS, KEYWORD.RELIABILITY_VALUE);

			keys = papp.parameters.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				processDataLocation(papp.parameters.get(key), KEYWORD.PARAMETERS, key);

			}
			for (EffectRecordDataLocation loc : papp.effects) {
				processDataLocation(loc.endpoint, KEYWORD.EFFECTS, ElementField.ENDPOINT);
				processDataLocation(loc.endpointType, KEYWORD.EFFECTS, ElementField.ENDPOINT_TYPE);
				processDataLocation(loc.errQualifier, KEYWORD.EFFECTS, ElementField.ERR_QUALIFIER);
				processDataLocation(loc.errValue, KEYWORD.EFFECTS, ElementField.ERR_VALUE);
				processDataLocation(loc.loQualifier, KEYWORD.EFFECTS, ElementField.LO_QUALIFIER);
				processDataLocation(loc.loValue, KEYWORD.EFFECTS, ElementField.LO_VALUE);
				processDataLocation(loc.sampleID, KEYWORD.EFFECTS, ElementField.SAMPLE_ID);
				processDataLocation(loc.textValue, KEYWORD.EFFECTS, ElementField.TEXT_VALUE);
				processDataLocation(loc.unit, KEYWORD.EFFECTS, ElementField.UNIT);
				processDataLocation(loc.upQualifier, KEYWORD.EFFECTS, ElementField.UP_QUALIFIER);
				processDataLocation(loc.value, KEYWORD.EFFECTS, ElementField.VALUE);
				processDataLocation(loc.upValue, KEYWORD.EFFECTS, ElementField.UP_VALUE);
				if (loc.conditions != null) {
					keys = loc.conditions.keySet().iterator();
					while (keys.hasNext()) {
						String key = keys.next();
						processDataLocation(loc.conditions.get(key), KEYWORD.CONDITIONS, key);
					}
				}

			}

		}

	}

	protected void processDataLocation(ExcelDataLocation loc, KEYWORD type_parent, String type) {
		processDataLocation(loc, type_parent == null ? null : type_parent.name(), type);
	}

	protected void processDataLocation(ExcelDataLocation loc, KEYWORD type_parent, ElementField type) {
		processDataLocation(loc, type_parent == null ? null : type_parent.name(), type == null ? null : type.name());
	}

	protected void processDataLocation(ExcelDataLocation loc, KEYWORD type_parent, KEYWORD type) {
		processDataLocation(loc, type_parent == null ? null : type_parent.name(), type == null ? null : type.name());
	}

	protected void processDataLocation(ExcelDataLocation loc, String type_parent, String type) {
		if (loc == null)
			return;
		if (loc.getAbsoluteLocationValue() == null)
			lookup.put(loc.columnIndex, new String[] { type_parent, type });

		else
			processDataLocation((ExcelDataLocation) loc.getAbsoluteLocationValue(), type_parent, type);
	}
}
