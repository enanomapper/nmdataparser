package net.idea.templates.annotation;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import ambit2.base.json.JSONUtils;
import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;
import net.idea.modbcum.p.DefaultAmbitProcessor;

abstract public class AbstractAnnotationProcessor<IN, OUT> extends DefaultAmbitProcessor<IN, OUT> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 801308251637717327L;
	protected IAnnotator annotator;
	
	
	public IAnnotator getAnnotator() {
		return annotator;
	}

	public void setAnnotator(IAnnotator annotator) {
		this.annotator = annotator;
	}

	public AbstractAnnotationProcessor() {
		super();
	}

	public AbstractAnnotationProcessor(IAnnotator annotator) {
		super();
		this.annotator = annotator;
	}


	public static void writeTerms(HashMap<String, TR> terms, Writer writer) throws Exception {
		writer.write("{\n");
		String comma = "";
		for (String key : terms.keySet()) {
			writer.write(comma);
			writer.write(JSONUtils.jsonQuote(JSONUtils.jsonEscape(key)));
			writer.write(":");
			writer.write(terms.get(key).toJSON());
			comma = ",";
		}
		writer.write("\n}");
	}

	public static void writeTermsTSV(HashMap<String, TR> terms, Writer writer) throws Exception {

		for (String key : terms.keySet()) {
			TR term = terms.get(key);
			Object abbr = term.get("abbr");
			String h = String.format("%s\t%s", term.get("file"), term.get("value"));

			TR tags = (TR) term.get("term");
			if (tags != null)
				for (String tag : tags.keySet()) {
					List<TR> hits = (List<TR>) tags.get(tag);
					for (TR hit : hits) {
						Object label = hit.get("label");
						label = label == null ? "" : label.toString().replaceAll("\n", " ").replaceAll("\r", " ");
						writer.write(String.format("lucene\t%s\t%s\t%s\t%s\t%s\t%s\t\t%s\t\t%s\n",
								abbr == null ? key.toUpperCase() : abbr, hit.get("rank"), hit.get("score"), tag, tag,
								hit.get("uri"), label, h));
					}
				}
			else
				writer.write(String.format("lucene\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", key, "", "", "", "", "", "", h));

		}
	}

}
