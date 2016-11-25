package net.enanomapper.templates;

public interface IAnnotator {
	void process(TR record,String queryField, String query, int maxhits, String label);
	void process(TR record);
	public String getLabel_tag();
	public String getTerm_tag();
	public void setTerm_tag(String term_tag);
	public String getQueryField();
	public void setQueryField(String queryField);
	enum _termtag {
		uri,label,score,rank
	}
}
