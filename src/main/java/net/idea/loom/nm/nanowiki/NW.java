package net.idea.loom.nm.nanowiki;

import java.io.IOException;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public enum NW {
	m_material, m_coating, m_condition, m_iep, m_size, m_zetapotential,m_sparql;
	public String SPARQL() throws IOException {
		URL url = Resources.getResource("net/idea/loom/nm/nanowiki/" + name()
				+ ".sparql");
		return Resources.toString(url, Charsets.UTF_8);

	}
}
