package net.idea.templates.annotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;

public class CuratedAnnotation implements IAnnotator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8329019073503279777L;

	protected JsonNode root = null;

	public CuratedAnnotation() throws Exception {
		this(getJSONConfig(new File("F:/Downloads/Chemical data/NANOREG1/TEST/terms_approved.json")));

	}

	public CuratedAnnotation(JsonNode node) {
		super();
		this.root = node;
	}

	protected String getQuery(TR record) {
		return TR.hix.cleanedvalue.get(record).toString();
	}

	@Override
	public void process(TR record) {
		process(record, getQueryField(), getQuery(record), 1, getLabel_tag());
	}

	static String[] ZnO = new String[] {"JRCNM01101A","JRCNM62101A"};
	

	static String[] TiO2 = new String[] {"JRCNM10200A","JRCNM10202A","JRCNM62001A","JRCNM62002A","JRCNM01005A","JRCNM01001A"}; 

	static String[] SiO2 = new String[] {"JRCNM10404A","JRCNM02001A", "JRCNM02002A","JRCNM02000A","JRCNM02004A"}; 

	static String[] MWCNT = new String[] {	"JRCNM04000A","JRCNM04001a","JRCNM04002A","JRCNM04003A","JRCNM40001A","JRCNM40002A","JRCNM40003A","JRCNM40004A","JRCNM40005A", 
			"JRCNM40006A","JRCNM40007A","JRCNM40008A","JRCNM40009A","JRCNM40010A","JRCNM46000A","JRCNM48001A"};
	
	static {
		Arrays.sort(ZnO);
		Arrays.sort(TiO2);
		Arrays.sort(SiO2);
		Arrays.sort(MWCNT);
	}
	
	public void process(TR record, String queryField, String query, int maxhits, String label) {
		query = AnnotationProcessor.hack_jrcnm(query.toUpperCase());

		JsonNode node = root.get(query.toLowerCase());
		try {
			ArrayNode enm = (ArrayNode) node.get("term").get("enm");
			for (int i = 0; i < enm.size(); i++) {
				JsonNode n = enm.get(i);
				
				if (1 == n.get("rank").asInt()) {
					label = n.get("label").asText();
					record.put("term_uri", n.get("uri").asText());
					record.put("term_label", label);
					record.put("term_score", n.get("score").asText());
					record.put(IAnnotator._termtag.rank.name(), n.get("rank").asInt());
					
					label = n.get("label").asText().toUpperCase();
					if (label.startsWith("JRC")) {
						
						if (Arrays.binarySearch(ZnO, label)>=0) 
							record.put("endpoint","ZnO");
						else if (Arrays.binarySearch(TiO2,label)>=0) 
							record.put("endpoint","TiO2");
						else if (Arrays.binarySearch(SiO2,label)>=0) 
							record.put("endpoint","SiO2");
						else if (Arrays.binarySearch(MWCNT,label)>=0) 
							record.put("endpoint","MWCNT");
						else record.put("endpoint","JRCNM");
						//System.out.println(String.format("%s\t%s",label,record.get("endpoint")));
					}
					
					break;
				}
			}
		} catch (Exception x) {
			//throw x;
		}
	}

	public static JsonNode getJSONConfig(File file) throws Exception {
		try (FileInputStream in = new FileInputStream(file)) {
			System.out.println(file.getAbsolutePath());
			return getJSONConfig(in);
		} catch (Exception x) {
			throw x;
		}
	}

	public static JsonNode getJSONConfig(InputStream in) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = null;
		try {
			root = mapper.readTree(in);

		} catch (Exception x) {
			throw x;
		} finally {
			try {
				in.close();
			} catch (Exception x) {
			}
		}
		return root;
	}
	/*
	 * public static void main(String[] args) { try { JsonNode node =
	 * CuratedAnnotation.getJSONConfig(new File(args[0])); CuratedAnnotation ca
	 * = new CuratedAnnotation(node); TR r = ca.find(args[1]);
	 * System.out.println(String.format("%s\t%s\n%s", args[0],args[1],r)); }
	 * catch (Exception x) { x.printStackTrace(); } }
	 */

	private String term_tag;
	private String queryField = "label";

	public String getTerm_tag() {
		return term_tag;
	}

	public void setTerm_tag(String term_tag) {
		this.term_tag = term_tag;
	}

	@Override
	public void setQueryField(String queryField) {
		this.queryField = queryField;
	}

	@Override
	public String getQueryField() {
		return queryField;
	}

	@Override
	public String getLabel_tag() {
		return "label";
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
