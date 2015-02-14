package net.enanomapper.parser;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.relation.composition.CompositionRelation;


public class UniversalObject 
{
	protected SubstanceRecord substanceRecord = null;
	protected ProtocolApplication protocolApplication = null;
	protected EffectRecord effect = null;
	protected CompositionRelation composition = null;
	protected Protocol protocol = null;
	
	public SubstanceRecord getSubstanceRecord()
	{
		if (substanceRecord != null)
			return substanceRecord;
		
		substanceRecord = new SubstanceRecord();
		return substanceRecord;
	}
}
