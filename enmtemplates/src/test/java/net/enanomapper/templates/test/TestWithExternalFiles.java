package net.enanomapper.templates.test;

import java.io.IOException;
import java.io.Writer;

import net.enanomapper.templates.ExtractSynonymsList;
import net.enanomapper.templates.IProcessRDF;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestWithExternalFiles implements IProcessRDF {
	protected ExtractSynonymsList syn;

	@Override
	public String getFileExtension() {
		if (syn == null)
			syn = new ExtractSynonymsList();
		return syn.getFileExtension();
	}

	@Override
	public int traverse(Resource root, Model jmodel, int level, Writer out)
			throws IOException {
		if (syn == null)
			syn = new ExtractSynonymsList();
		return syn.traverse(root, jmodel, level, out);

	}

	@Override
	public void processEntry(String entity, String label) {

	}

	@Override
	public Property getHproperty() {
		if (syn == null)
			syn = new ExtractSynonymsList();
		return syn.getHproperty();
	}

	@Override
	public void setHproperty(Property hproperty) {
		if (syn == null)
			syn = new ExtractSynonymsList();
		syn.setHproperty(hproperty);
	}
}
