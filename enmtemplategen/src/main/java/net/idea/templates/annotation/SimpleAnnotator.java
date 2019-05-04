package net.idea.templates.annotation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;


/**
 * Relies on JRC/NANOREG templates structure, where the first row is a header with merged columns
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
			for (Entry<Integer, String> entry : header.entrySet()) {
				if (col >= entry.getKey())
					TR.hix.Annotation.set(record, entry.getValue());
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
}