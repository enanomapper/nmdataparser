package net.enanomapper.templates.test;

import java.io.IOException;
import java.io.Writer;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import net.enanomapper.templates.ExtractSynonymsList;
import net.enanomapper.templates.ToolsRDF;
import net.idea.modbcum.i.json.JSONUtils;
import net.idea.templates.generation.Tools;

public class RDFsmasher extends TestWithExternalFiles {



	int maxlevel = Integer.MAX_VALUE;

	public int getMaxlevel() {
		return maxlevel;
	}

	public void setMaxlevel(int maxlevel) {
		this.maxlevel = maxlevel;
	}

	
	public void testGO() throws Exception {
		ToolsRDF.smash("http://data.bioontology.org/ontologies/NCIT/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"GO", false, this);
	}

	
	public void testGO_Gene() throws Exception {
		ToolsRDF.smash("http://data.bioontology.org/ontologies/NCIT/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"GO", false, this,
				"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C16612",
				"RDF/XML");
	}

	
	public void testGO_GeneProduct() throws Exception {
		ToolsRDF.smash("http://data.bioontology.org/ontologies/NCIT/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"GO", false, this,
				"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C26548",
				"RDF/XML");
	}

	
	public void testGO_ProteinFamily() throws Exception {
		ToolsRDF.smash("http://data.bioontology.org/ontologies/NCIT/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"GO", false, this,
				"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C20130",
				"RDF/XML");
	}

	
	public void testBAO() throws Exception {
		ToolsRDF.smash("http://data.bioontology.org/ontologies/BAO/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"BAO", false, this, "http://www.w3.org/2002/07/owl#Thing",
				"RDF/XML");
	}

	
	public void testENM() throws Exception {
		ToolsRDF.smash("http://data.bioontology.org/ontologies/ENM/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"ENM", true, this, "http://www.w3.org/2002/07/owl#Thing",
				"RDF/XML");

	}

	
	public void testENM_substance() throws Exception {
		ToolsRDF.smash("http://data.bioontology.org/ontologies/ENM/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"ENM", false, this,
				"http://purl.obolibrary.org/obo/CHEBI_59999", "RDF/XML");

	}

	
	public void testCHEBI() throws Exception {
		ToolsRDF.smash("http://data.bioontology.org/ontologies/CHEBI/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"CHEBI", false, this);

	}

	
	public void testCLO() throws Exception {
		ToolsRDF.smash("http://data.bioontology.org/ontologies/CLO/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"CLO", false, this);
	}

	
	public void test_protein() throws Exception {
		ToolsRDF.smash("http://rdf.disgenet.org/download/v4.0.0/protein.ttl.gz",
				"Protein", false, this,
				"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C17021",
				"TURTLE",RDF.type.getURI());
	}
	
	public void test_gene() throws Exception {
		ToolsRDF.smash("http://rdf.disgenet.org/download/v4.0.0/gene.ttl.gz",
				"gene", false, this,
				"http://www.w3.org/2002/07/owl#Thing",
				"TURTLE",RDF.type.getURI());
	}
	
	@Override
	public String getFileExtension() {
		return "json";
	}

	/**
	 * generate tree json
	 * https://github.com/ideaconsult/Toxtree.js/blob/facet_kit/flare.json
	 * 
	 * @param root
	 * @param jmodel
	 * @param level
	 */
	@Override
	public int traverse(Resource root, Model jmodel, int level, Writer out)
			throws IOException {
		if (level > maxlevel)
			return 0;
		NodeIterator n = jmodel.listObjectsOfProperty(root, RDFS.label);
		StringBuilder label = null;
		while (n.hasNext()) {
			RDFNode node = n.next();
			if (label == null)
				label = new StringBuilder();
			label.append(node.asLiteral().getString());
		}
		String name = label == null ? root.getLocalName() : label.toString();
		if (name.indexOf("Retired") >= 0)
			return 0;
		ResIterator i = jmodel.listSubjectsWithProperty(getHproperty(), root);
		out.write("{");
		out.write("\n\"name\":");
		out.write(JSONUtils.jsonQuote(JSONUtils.jsonEscape(name)));
		out.write(",\n\"id\":");
		out.write(JSONUtils.jsonQuote(JSONUtils.jsonEscape(root.getLocalName())));

		int count = 0;
		int size = 0;
		while (i.hasNext()) {
			if (count == 0)
				out.write(",\n\"children\": [\n");
			else
				out.write(",");
			Resource res = i.next();
			size += traverse(res, jmodel, (level + 1), out);
			count++;
		}
		if (count > 0)
			out.write("\n]");
		out.write(",");
		out.write(String.format("\n\"size\":%d", size + 1));
		out.write("\n}");
		out.flush();
		return size + 1;
	}

	
	public void extractSynonymsENM() throws Exception {
		ToolsRDF.smash("http://data.bioontology.org/ontologies/ENM/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"ENM", true, new ExtractSynonymsList(),
				"http://www.w3.org/2002/07/owl#Thing", "RDF/XML");
	}
}
