package net.enanomapper.parser;
import java.util.ArrayList;
import java.util.List;

import net.enanomapper.parser.ParserConstants.ElementSynchronization;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.relation.composition.CompositionRelation;


public class UniversalObject 
{
	protected ArrayList<SubstanceRecord> substanceRecords = null;
	protected SubstanceRecord substanceRecord = null;
	protected ProtocolApplication protocolApplication = null;
	protected Protocol protocol = null;
	protected EffectRecord effect = null;
	protected CompositionRelation composition = null;
	
	protected ArrayList<CompositionRelation> compositionArray = new ArrayList<CompositionRelation>();
	
	
	public ArrayList<SubstanceRecord> getSubstanceRecords()
	{
		if (substanceRecords != null)
			return substanceRecords;
		
		substanceRecords = new ArrayList<SubstanceRecord>();
		return substanceRecords;
	}
	
	public void setSubstanceRecords(ArrayList<SubstanceRecord> substanceRecords)
	{
		this.substanceRecords = substanceRecords;
	}
	
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
	
	
	public void selfDispatch()
	{
		dispatchTo(this);
	}
	
	public void dispatchTo(SubstanceRecord record)
	{
		//TODO
	}
	
	public void dispatchTo(UniversalObject target)
	{
		//System.out.println("dispatching : " + this.debugInfo(0) + " --> " + target.debugInfo(0));
		
		if (composition != null)
		{
			if (target.substanceRecord != null)
				target.substanceRecord.addStructureRelation(composition);
		}
		
		if (effect != null)
		{
			if (target.protocolApplication != null)
				target.protocolApplication.addEffect(effect);
		}
		
		if (protocol != null)
		{
			if (target.protocolApplication != null)
				target.protocolApplication.setProtocol(protocol);
		}
		
		if (protocolApplication != null)
		{
			if (target.substanceRecord != null)
			{	
				List<ProtocolApplication> listPA = target.substanceRecord.getMeasurements();
				if (listPA == null)
				{	
					listPA = new ArrayList<ProtocolApplication>();
					target.substanceRecord.setMeasurements(listPA);
				}
				listPA.add(protocolApplication);
			}	
		}
		
	}
	
	protected void dispatchTo(ElementSynchronization synchType, SynchronizationTarget synchTarget)
	{
		//TODO
	}
	
	public String debugInfo(int level)
	{
		StringBuffer sb = new StringBuffer();
		
		if (substanceRecord != null)
			sb.append("substanceRecord ");
		if (composition != null)
			sb.append("composition ");
		if (protocol != null)
			sb.append("protocol ");
		if (protocolApplication != null)
			sb.append("protocolApplication ");
		if (effect != null)
			sb.append("effect ");
		
		return sb.toString();
	}
}
