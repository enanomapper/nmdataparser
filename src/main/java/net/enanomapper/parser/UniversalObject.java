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
	protected Protocol protocol = null;
	protected EffectRecord effect = null;
	protected CompositionRelation composition = null;
	
	
	public SubstanceRecord getSubstanceRecord()
	{
		if (substanceRecord != null)
			return substanceRecord;
		
		substanceRecord = new SubstanceRecord();
		return substanceRecord;
	}
	
	public ProtocolApplication getProtocolApplication()
	{
		if (protocolApplication != null)
			return protocolApplication;
		
		protocolApplication = new ProtocolApplication(null);
		return protocolApplication;
	}
	
	public Protocol getProtocol()
	{
		if (protocol != null)
			return protocol;
		
		protocol = new Protocol(null);
		return protocol;
	}
	
	public EffectRecord getEffect()
	{
		if (effect != null)
			return effect;
		
		effect = new EffectRecord();
		return effect;
	}
	
	public CompositionRelation getComposition()
	{
		if (composition != null)
			return composition;
		
		composition = new CompositionRelation(null, null, null, null);
		return composition;
	}
}
