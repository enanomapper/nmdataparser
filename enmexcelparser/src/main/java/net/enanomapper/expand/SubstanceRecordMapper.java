package net.enanomapper.expand;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.net.UrlEscapers;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.study.StructureRecordValidator;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.json.SubstanceStudyParser;
import net.idea.modbcum.p.DefaultAmbitProcessor;

/**
 * Processes expandmap (loaded with option -n expandconfig)
 * 
 * @author nina
 *
 */
public class SubstanceRecordMapper extends DefaultAmbitProcessor<IStructureRecord, IStructureRecord> {
	protected JsonNode conditionsMap = null;
	protected JsonNode paramsMap = null;
	protected JsonNode substanceOwnersMap = null;
	protected String prefix;

	public SubstanceRecordMapper(String prefix, File expandMap) throws IOException {
		super();
		this.prefix = prefix;
		ObjectMapper dx = new ObjectMapper();
		JsonNode rootNode = null;
		try (InputStream in = new FileInputStream(expandMap)) {
			rootNode = dx.readTree(in);
		} catch (IOException x) {
			throw x;
		}

		if (rootNode == null)
			throw new IOException("Invalid mapping file");
		try {
			conditionsMap = rootNode.get("EXPAND_MAP").get("CONDITIONS_MAP");
		} catch (Exception x) {
			conditionsMap = null;
		}
		try {
			paramsMap = rootNode.get("EXPAND_MAP").get("PARAMETERS_MAP");
		} catch (Exception x) {
			paramsMap = null;
		}
		try {
			substanceOwnersMap = rootNode.get("EXPAND_MAP").get("SUBSTANCEOWNER_MAP");
		} catch (Exception x) {
			substanceOwnersMap = null;
		}		
		if ((conditionsMap == null) && (paramsMap == null) && (substanceOwnersMap==null) )
			throw new IOException("Mappings not found!");
		// SubstanceStudyParser.parseParams(ObjectNode node)
	}

	@Override
	public IStructureRecord process(IStructureRecord record) throws Exception {
		if (record instanceof SubstanceRecord)
			return process((SubstanceRecord) record);
		else
			return record;
	}

	public IStructureRecord process(SubstanceRecord record) throws Exception {
		if (substanceOwnersMap!=null) {
			if (record.getOwnerName()==null) try {
				record.setOwnerName(substanceOwnersMap.get("DEFAULT").asText());
			} catch (Exception x) {
				
			}
		}
		if ((conditionsMap != null) || (paramsMap != null)  ) {
			Map<String, ProtocolApplication> papps_new = process(record, record.getMeasurements());
			record.getMeasurements().clear();
			record.getMeasurements().addAll(papps_new.values());
		}
		return record;
	}

	public Map<String, ProtocolApplication> process(SubstanceRecord record, List<ProtocolApplication> papps)
			throws Exception {

		if (papps == null)
			return null;
		Map<String, ProtocolApplication> papps_new = new HashMap<String, ProtocolApplication>();
		for (ProtocolApplication papp : papps) {
			process(record, papp, papps_new);
		}
		return papps_new;
	}

	protected Params expand(String where, Params params, JsonNode map, boolean copy) {
		
		Params params_new = null;
		for (Object key : params.keySet()) {
			JsonNode node = map==null?null:map.get(key.toString());
			if (node != null) {
				Object value = params.get(key);
				try {
					JsonNode submap = node.get(value.toString());
					if (submap != null) {
						JsonNode mappedNode = submap.get(where);
						Params mappedParams = (Params) SubstanceStudyParser.parseParams((ObjectNode) mappedNode);
						if (params_new == null)
							params_new = mappedParams;
						else
							if (mappedParams !=null)
								params_new.putAll(mappedParams);
					} else if (node instanceof TextNode) {
						if (params_new == null)
							params_new = new Params();
						boolean skip_map = false;
						String template = ((TextNode) node).asText();
						if (template.startsWith("http")) {
							if (value.toString().startsWith("http")) {
								skip_map = true;
							} else {
								value = UrlEscapers.urlPathSegmentEscaper().escape(value.toString());
							}
						}
						if (skip_map)
							params_new.put(key.toString(), value);
						else
							params_new.put(key.toString(), String.format(template, value.toString()));
					}
				} catch (Exception x) {
					x.printStackTrace();
				}
			} else if (copy) {
				if (params_new == null)
					params_new = new Params();
				params_new.put(key.toString(), params.get(key));
			}
		}
		return params_new;
	}

	protected ProtocolApplication smartClone(ProtocolApplication papp) {
		ProtocolApplication aclone = new ProtocolApplication(papp.getProtocol());
		aclone.setInterpretationCriteria(papp.getInterpretationCriteria());
		aclone.setInterpretationResult(papp.getInterpretationResult());
		aclone.setCompanyName(papp.getCompanyName());
		aclone.setCompanyUUID(papp.getCompanyUUID());
		aclone.setSubstanceUUID(papp.getSubstanceUUID());
		aclone.setInvestigationUUID(papp.getInvestigationUUID());
		aclone.setAssayUUID(papp.getAssayUUID());
		aclone.setUpdated(papp.getUpdated());
		aclone.setReference(papp.getReference());
		aclone.setReferenceOwner(papp.getReferenceOwner());
		aclone.setReferenceYear(papp.getReferenceYear());
		aclone.setReferenceSubstanceUUID(papp.getReferenceSubstanceUUID());
		aclone.setReliability(papp.getReliability());
		aclone.setEffects(null);
		aclone.setParameters(null);
		return aclone;
	}

	public void process(SubstanceRecord record, ProtocolApplication papp, Map<String, ProtocolApplication> papps_new)
			throws Exception {
		// map conditions
		Params params_new = (Params) papp.getParameters();
		if (papp.getParameters() != null) {
			params_new = expand("PARAMETERS", ((Params) papp.getParameters()), paramsMap, true);
		}
		if (papp.getEffects() != null)
			for (EffectRecord effect : (List<EffectRecord>) papp.getEffects()) {
				try {
					if (effect.getConditions() == null) effect.setConditions(new Params());
					Params conditions = (Params) effect.getConditions();
					if (conditionsMap != null) {
						Params conditions_new = expand("CONDITIONS", conditions, conditionsMap, true);
						effect.setConditions(conditions_new);
					}
					// System.out.println(String.format("%s\t%s", conditions.toString(),
					// conditions_new.toString()));
					// need to split into different protocolapplications if parameters differ ...

					Params cond2param = new Params();
					if (params_new != null)
						cond2param.putAll(params_new);

					if (conditionsMap != null) {
						Params tmp = expand("PARAMETERS", conditions, conditionsMap, false);
						if (tmp != null)
							cond2param.putAll(tmp);
					}

					List<String> guideline = ((Protocol) papp.getProtocol()).getGuideline();
					String u = String.format("%s-%s-%s-%s-%s-%s", record.getSubstanceName(),
							((Protocol) papp.getProtocol()).getCategory(), guideline == null ? "" : guideline.get(0),
							papp.getReference(), papp.getReferenceOwner(), cond2param.toString());
					String docUUID = StructureRecordValidator.generateUUIDfromString(prefix, u);
					// System.out.println(String.format("%s\t%s", docUUID, u));
					ProtocolApplication p = papps_new.get(docUUID);
					if (p == null) {
						p = smartClone(papp);
						p.setParameters(cond2param);
						p.setDocumentUUID(docUUID);
						papps_new.put(docUUID, p);
					}
					p.addEffect(effect);
				} catch (Exception x) {
					throw x;
				}
			}

	}
}
