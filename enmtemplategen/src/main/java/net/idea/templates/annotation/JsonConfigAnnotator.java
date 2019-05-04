package net.idea.templates.annotation;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.util.CellReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;
import net.enanomapper.parser.KEYWORD;

public class JsonConfigAnnotator implements IAnnotator {
	protected ObjectMapper mapper = new ObjectMapper();

	protected ObjectNode config = mapper.createObjectNode();

	public JsonConfigAnnotator() throws IOException {
		super();
	}

	protected void setColumnIndex(ObjectNode node, int col) {
		node.put(KEYWORD.COLUMN_INDEX.name(), CellReference.convertNumToColString(col));
	}

	protected ObjectNode getJsonConfig(String key) {
		JsonNode _root = config.get(key);
		if (_root == null) {
			try (InputStream in = getClass().getClassLoader()
					.getResourceAsStream("net/idea/templates/config/config.json")) {
				JsonNode node = mapper.readTree(in);
				config.set(key, (ObjectNode) node);
				return (ObjectNode) node;
			} catch (IOException x) {
				x.printStackTrace();
				ObjectNode root = mapper.createObjectNode();
				ObjectNode node_template = mapper.createObjectNode();
				ObjectNode node_dataaccess = mapper.createObjectNode();
				ObjectNode node_substancerecord = mapper.createObjectNode();
				ArrayNode node_protocolapplications = mapper.createArrayNode();
				ObjectNode node_protocolapplication = mapper.createObjectNode();
				node_protocolapplication.set(KEYWORD.PARAMETERS.name(), mapper.createObjectNode());
				ArrayNode node_effects = mapper.createArrayNode();
				node_protocolapplication.set(KEYWORD.EFFECTS.name(), node_effects);
				node_protocolapplications.add(node_protocolapplication);

				root.set(KEYWORD.TEMPLATE_INFO.name(), node_template);
				root.set(KEYWORD.DATA_ACCESS.name(), node_dataaccess);
				root.set(KEYWORD.SUBSTANCE_RECORD.name(), node_substancerecord);
				root.set(KEYWORD.PROTOCOL_APPLICATIONS.name(), node_protocolapplications);

				config.set(key, root);
				return root;
			}

		} else {
			return (ObjectNode) _root;
		}

	}

	@Override
	public void process(TR record, String queryField, String query, int maxhits, String label) {
	}

	protected ObjectNode getProtocolApplication(String id, int index) {
		ObjectNode root = getJsonConfig(id);
		ArrayNode papp = (ArrayNode) (root.get(KEYWORD.PROTOCOL_APPLICATIONS.name()));
		return (ObjectNode) papp.get(0);
	}

	@Override
	public void process(TR record) {
		int row = Integer.parseInt(record.get(TR.hix.Row.name()).toString());
		int col = Integer.parseInt(record.get(TR.hix.Column.name()).toString());
		String id = record.get(TR.hix.id.name()).toString();
		String value = record.get(TR.hix.cleanedvalue.name()).toString();
		ObjectNode root = getJsonConfig(id);
		if (row == 0)
			return;
		if (row > 1)
			return;
		Object annotation = TR.hix.Annotation.get(record);
		if ("data".equals(annotation.toString()))
			return;
		Object level1 = TR.hix.JSON_LEVEL1.get(record);

		try {
			KEYWORD key = KEYWORD.valueOf(level1.toString());
			switch (key) {
			case PARAMETERS: {
				ObjectNode params = (ObjectNode) getProtocolApplication(id, 0).get(KEYWORD.PARAMETERS.name());
				ObjectNode param = mapper.createObjectNode();
				setColumnIndex(param, col);
				params.set(value, param);
				break;

			}
			case EFFECTS: {
				/*
				 * ArrayNode effects = (ArrayNode)
				 * getProtocolApplication(id,0).get(KEYWORD.EFFECTS.name());
				 * ObjectNode effect = mapper.createObjectNode();
				 * 
				 * ObjectNode effect_endpoint = mapper.createObjectNode();
				 * //setColumnIndex(param,col); //params.set(value, param);
				 * effects.add(effect);
				 */
				break;
			}
			case SUBSTANCE_RECORD: {
				/*
				 * ObjectNode params = (ObjectNode) getProtocolApplication(id,
				 * 0).get(KEYWORD.PARAMETERS.name()); ObjectNode param =
				 * mapper.createObjectNode(); setColumnIndex(param, col);
				 * params.set("E.sop_reference", param);
				 */
				break;
			}

			}

		} catch (Exception x) {
			// skip
		}

	}

	@Override
	public String getLabel_tag() {
		return null;
	}

	@Override
	public String getTerm_tag() {
		return null;
	}

	@Override
	public void setTerm_tag(String term_tag) {
	}

	@Override
	public String getQueryField() {
		return null;
	}

	@Override
	public void setQueryField(String queryField) {
	}

	@Override
	public String toString() {
		try {
			return mapper.writeValueAsString(config);
		} catch (Exception x) {
			return x.getMessage();
		}

	}

}
