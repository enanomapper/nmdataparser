package net.idea.loom.nm.nanowiki;

import java.io.IOException;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public enum NW {
	m_allmaterials, m_materialprops, m_coating, m_condition,  m_sparql;
	public String SPARQL() throws IOException {
		return SPARQL(name());

	}

	public static String SPARQL(String resource) throws IOException {
		URL url = Resources.getResource("net/idea/loom/nm/nanowiki/" + resource
				+ ".sparql");
		return Resources.toString(url, Charsets.UTF_8);

	}
}
