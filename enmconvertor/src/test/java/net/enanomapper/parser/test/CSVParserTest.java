package net.enanomapper.parser.test;

import java.io.FileReader;
import java.net.URL;

import junit.framework.Assert;
import net.idea.loom.nm.csv.CSV12Reader;
import net.idea.loom.nm.csv.CSV12SubstanceReader;

import org.junit.Test;

import ambit2.base.data.ILiteratureEntry._type;
import ambit2.base.data.LiteratureEntry;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.RawIteratingWrapper;

public class CSVParserTest  {

	
	@Test
	public void test_CSV() throws Exception {
		RawIteratingWrapper reader = null;
		try {
			LiteratureEntry entry = new LiteratureEntry("New test","http://example.com");
    		entry.setType(_type.Dataset);
    		
    		URL resource = getClass().getClassLoader().getResource("net/enanomapper/parser/csv/ProteinCoronaTest.csv");
    		Assert.assertNotNull(resource);
			CSV12Reader chemObjectReader = new CSV12Reader(new FileReader(resource.getFile()),entry,"TEST-");
			reader = new CSV12SubstanceReader(chemObjectReader);
			int r = 0;
			
			while (reader.hasNext()) {
				IStructureRecord mol = reader.nextRecord();
				Assert.assertTrue(mol instanceof SubstanceRecord);
				SubstanceRecord substance = (SubstanceRecord)mol;
				Assert.assertNotNull(substance.getPublicName());
				//System.out.println(substance.getPublicName());
				Assert.assertNotNull(substance.getSubstanceName());
				Assert.assertNotNull(substance.getMeasurements());
				//System.out.println(substance.getMeasurements());
				for (ProtocolApplication<Protocol, Params, String, Params, String> papp : substance.getMeasurements()) {
					Assert.assertNotNull(papp.getProtocol());
					Assert.assertNotNull(papp.getProtocol().getCategory());
					Assert.assertNotNull(papp.getProtocol().getTopCategory());
					Assert.assertNotNull(papp.getProtocol().getGuideline());
					Assert.assertNotNull(papp.getParameters());
					Assert.assertNotNull(papp.getEffects());
					for (EffectRecord<String,Params,String> measurement : papp.getEffects()) {
						Assert.assertNotNull(measurement.getEndpoint());
						Assert.assertNotNull(measurement.getConditions());
						//Assert.assertTrue(measurement.getConditions().size()>0);
						Assert.assertTrue( (measurement.getLoValue()!=null) || (measurement.getTextValue()!=null));
					}
				}
				
				r++;
			}
			Assert.assertEquals(2,r);
		} finally {
			reader.close();
		}
	}
	
}
