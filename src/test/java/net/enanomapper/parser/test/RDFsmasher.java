package net.enanomapper.parser.test;

import java.io.IOException;
import java.io.Writer;

import net.idea.modbcum.i.json.JSONUtils;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class RDFsmasher extends TestWithExternalFiles  {
	int maxlevel = Integer.MAX_VALUE;

	public int getMaxlevel() {
		return maxlevel;
	}

	public void setMaxlevel(int maxlevel) {
		this.maxlevel = maxlevel;
	}

	@Test
	public void testGO() throws Exception {
		smash("http://data.bioontology.org/ontologies/NCIT/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"GO",false,this);
	}
	

	@Test
	public void testGO_Gene() throws Exception {
		smash("http://data.bioontology.org/ontologies/NCIT/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"GO",false,this,"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C16612");
	}

	@Test
	public void testGO_GeneProduct() throws Exception {
		smash("http://data.bioontology.org/ontologies/NCIT/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"GO",false,this,"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C26548");
	}
	@Test
	public void testGO_ProteinFamily() throws Exception {
		smash("http://data.bioontology.org/ontologies/NCIT/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"GO",false,this,"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C20130");
	}
	@Test
	public void testBAO() throws Exception {
		smash("http://data.bioontology.org/ontologies/BAO/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"BAO",false,this,"http://www.w3.org/2002/07/owl#Thing");
	}

	@Test
	public void testENM() throws Exception {
		smash("http://data.bioontology.org/ontologies/ENM/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"ENM",true,this,"http://www.w3.org/2002/07/owl#Thing");

	}
	@Test
	public void testENM_substance() throws Exception {
		smash("http://data.bioontology.org/ontologies/ENM/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"ENM",false,this,"http://purl.obolibrary.org/obo/CHEBI_59999");
		
	}
	@Test
	public void testCHEBI() throws Exception {
		smash("http://data.bioontology.org/ontologies/CHEBI/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"ENM",false,this);
		
	}
	
	@Test
	public void testCLO() throws Exception {
		smash("http://data.bioontology.org/ontologies/CLO/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"CLO");
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
			if (label==null) label = new StringBuilder();
			label.append(node.asLiteral().getString());
		}
		ResIterator i = jmodel.listSubjectsWithProperty(RDFS.subClassOf, root);
		out.write("{");
		out.write("\n\"name\":");
		out.write(JSONUtils.jsonQuote(JSONUtils.jsonEscape(label==null?root.getLocalName():label.toString())));
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
		out.write(String.format("\n\"size\":%d",size+1));		
		out.write("\n}");
		out.flush();
		return size+1;
	}
	

	@Test
	public void extractSynonymsENM() throws Exception {
		smash("http://data.bioontology.org/ontologies/ENM/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf",
				"ENM",true,new ExtractSynonymsList(),"http://www.w3.org/2002/07/owl#Thing");
	}
}
