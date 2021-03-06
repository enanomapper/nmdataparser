package net.enanomapper.templates;

import java.io.IOException;
import java.io.Writer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public interface IProcessRDF {
	public int traverse(Resource root, Model jmodel, int level, Writer out)
			throws IOException;

	public String getFileExtension();

	public void processEntry(String entity, String label);

	public Property getHproperty();

	public void setHproperty(Property hproperty);
}
