package net.enanomapper.parser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

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
	
	protected TreeMap<Integer, EffectRecord> mEffects = new TreeMap<Integer, EffectRecord>();
	
	protected TreeMap<Integer, CompositionRelation> mCompositions = new TreeMap<Integer, CompositionRelation>();
	
	
	
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
	
	/*
	public EffectRecord getEffect()
	{
		return getEffect(0);
	}
	*/
	
	public EffectRecord getEffect(int id)
	{
		EffectRecord ef = mEffects.get(id);
		if (ef != null)
			return ef;
		
		ef = new EffectRecord();
		mEffects.put(id, ef);
		return ef;
	}
	
	/*
	public CompositionRelation getComposition()
	{
		return getComposition(0);
	}
	*/
	
	public CompositionRelation getComposition(int id)
	{
		CompositionRelation comRel = mCompositions.get(id); 
		if (comRel !=null)
			return comRel;
		
		comRel = new CompositionRelation(null, null, null, null);
		mCompositions.put(id, comRel);
		
		return comRel;
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
		dispatchAllTo(target, 0);
	}
	
	
	public void dispatchAllTo(UniversalObject target, int targetId)
	{
		//All multiple objects are dispatched to the master objects with given id
		
		//System.out.println("dispatching : " + this.debugInfo(0) + " --> " + target.debugInfo(0));
		
		
		if (!mCompositions.isEmpty())
		{
			for(Entry<Integer, CompositionRelation> entry : mCompositions.entrySet())
				target.substanceRecord.addStructureRelation(entry.getValue());
		}
		
		/*
		if (effect != null)
		{
			if (target.protocolApplication != null)
				target.protocolApplication.addEffect(effect);
		}
		*/
		if (!mEffects.isEmpty())
		{
			if (target.protocolApplication != null)
			{
				for(Entry<Integer, EffectRecord> entry : mEffects.entrySet())
					target.protocolApplication.addEffect(entry.getValue());
			}
				
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
	
	public void dispatchTo(int sourceId, UniversalObject target, int targetId)
	{
		//TODO
	}
	
	
	/*
	 * TODO - more complex pattern
	 * 
	public void dispatchTo(UniversalObject target, DispatchShceme scheme)
	{
		
	}
	*/
	
	protected void dispatchTo(ElementSynchronization synchType, SynchronizationTarget synchTarget)
	{
		//TODO
	}
	
	public String debugInfo(int level)
	{
		StringBuffer sb = new StringBuffer();
		
		if (substanceRecord != null)
			sb.append("substanceRecord ");
		
		if (!mCompositions.isEmpty())
			sb.append("compositions-" + mCompositions.size() + " ");
		
		if (protocol != null)
			sb.append("protocol ");
		
		if (protocolApplication != null)
			sb.append("protocolApplication ");
		
		if (!mEffects.isEmpty())
			sb.append("effects-" + mEffects.size() + " ");
			
		return sb.toString();
	}
}
