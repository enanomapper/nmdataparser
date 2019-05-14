package net.idea.templates.annotation;

import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.TreeMap;

import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;
import net.enanomapper.maker.TemplateMaker._header;
import net.enanomapper.parser.KEYWORD;

/**
 * Relies on JRC/NANOREG templates structure, where the first row is a header
 * with merged columns
 * 
 * @author nina
 *
 */
public class SimpleAnnotator implements IAnnotator {
	Map<Integer, String> header = new TreeMap<Integer, String>();

	@Override
	public void process(TR record) {
		int row = Integer.parseInt(record.get(TR.hix.Row.name()).toString());
		int col = Integer.parseInt(record.get(TR.hix.Column.name()).toString());
		Object value = TR.hix.cleanedvalue.get(record);
		if (row == 0) {
			header.put(col, value.toString());
		} else {
			if (row > 4)
				TR.hix.Annotation.set(record, "data");
			else {
				for (Entry<Integer, String> entry : header.entrySet()) {
					if (col >= entry.getKey())
						TR.hix.Annotation.set(record, entry.getValue());
				}
				Object annotation = TR.hix.Annotation.get(record);
				if (annotation!=null)
				for (_header _type : _header.values()) 
					if (_type.toString().equals(annotation.toString())) {
					switch (_type) {
					case sample: {
						TR.hix.JSON_LEVEL1.set(record, KEYWORD.SUBSTANCE_RECORD.name());
						break;
					}
					case parameters:
					case method: {
						TR.hix.JSON_LEVEL1.set(record, KEYWORD.PARAMETERS.name());
						break;
						
					}
					case results: {
						TR.hix.JSON_LEVEL1.set(record, KEYWORD.EFFECTS.name());
						break;
					}
					case sop: {
						TR.hix.JSON_LEVEL1.set(record, KEYWORD.PROTOCOL_APPLICATIONS.name());
						TR.hix.JSON_LEVEL2.set(record, KEYWORD.PROTOCOL_GUIDELINE.name());
						break;
					}
					case size: {

					}

					}
				}				
			}	
		}

	}

	@Override
	public void process(TR record, String queryField, String query, int maxhits, String label) {

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

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void done() {
		// TODO Auto-generated method stub
		
	}
}