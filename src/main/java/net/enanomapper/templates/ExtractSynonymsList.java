package net.enanomapper.templates;

import java.io.IOException;
import java.io.Writer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;


public class ExtractSynonymsList implements IProcessRDF {
	int maxlevel = Integer.MAX_VALUE;
	protected Property hproperty = RDFS.subClassOf;

	public Property getHproperty() {
		return hproperty;
	}

	public void setHproperty(Property hproperty) {
		this.hproperty = hproperty;
	}

	
	public ExtractSynonymsList() {
	}
	public ExtractSynonymsList(int maxlevel) {
		setMaxlevel(maxlevel);
	}

	public int getMaxlevel() {
		return maxlevel;
	}

	public void setMaxlevel(int maxlevel) {
		this.maxlevel = maxlevel;
	}

	@Override
	public int traverse(Resource root, Model jmodel, int level, Writer out)
			throws IOException {

		if (level > maxlevel)
			return 0;
		NodeIterator n = jmodel.listObjectsOfProperty(root, RDFS.label);
		StringBuilder label = new StringBuilder();
		while (n.hasNext()) {
			RDFNode node = n.next();
			label.append(node.asLiteral().getString());
		}
		ResIterator i = jmodel.listSubjectsWithProperty(RDFS.subClassOf, root);
		out.write(String.format("\"%s\",\"%s\"\n", root.getLocalName(),label.toString()));
		processEntry(root.getLocalName(),label.toString());
		int size = 0;
		while (i.hasNext()) {
			Resource res = i.next();
			size += traverse(res, jmodel, (level + 1), out);
		}

		out.flush();
		return size;
	}

	@Override
	public String getFileExtension() {
		return "csv";
	}
	@Override
	public void processEntry(String entity, String label) {
		
	}
}
