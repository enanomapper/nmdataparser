package net.idea.templates.annotation;

import java.util.ArrayList;

import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;

public class AnnotatorChain extends ArrayList<IAnnotator> implements IAnnotator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2142988743943447096L;

	@Override
	public void process(TR record) {
		for (IAnnotator a : this) 
			a.process(record);
	}

	private String term_tag;

	public String getTerm_tag() {
		return term_tag;
	}

	public void setTerm_tag(String term_tag) {
		this.term_tag = term_tag;
	}

	String queryField;

	public String getQueryField() {
		return queryField;
	}

	public void setQueryField(String queryField) {
		this.queryField = queryField;
	}
	@Override
	public void process(TR record, String queryField, String query, int maxhits, String label) {
		for (IAnnotator a : this) 
			a.process(record,queryField,query,maxhits,a.getLabel_tag());
	}
	@Override
	public String getLabel_tag() {
		return null;
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
