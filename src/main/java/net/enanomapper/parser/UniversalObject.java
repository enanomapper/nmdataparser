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
	
	protected TreeMap<Integer, ProtocolApplication> mProtocolApplications = new TreeMap<Integer, ProtocolApplication>();
	protected TreeMap<Integer, Protocol> mProtocols = new TreeMap<Integer, Protocol>();
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
	
	public ProtocolApplication getProtocolApplication(int index)
	{
		ProtocolApplication pa = mProtocolApplications.get(index);
		if (pa != null)
			return pa;
		
		pa = new ProtocolApplication(null);
		mProtocolApplications.put(index, pa);
		return pa;
	}
	
	public Protocol getProtocol(int index)
	{
		Protocol p = mProtocols.get(index);
		if (p!=null)
			return p;
		
		p = new Protocol(null);
		mProtocols.put(index, p);
		return p;
	}
	
	
	public EffectRecord getEffect(int index)
	{
		EffectRecord ef = mEffects.get(index);
		if (ef != null)
			return ef;
		
		ef = new EffectRecord();
		mEffects.put(index, ef);
		return ef;
	}
	
	
	public CompositionRelation getComposition(int index)
	{
		CompositionRelation comRel = mCompositions.get(index); 
		if (comRel !=null)
			return comRel;
		
		comRel = new CompositionRelation(null, null, null, null);
		mCompositions.put(index, comRel);
		
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
	
	
	public void dispatchAllTo(UniversalObject target, int targetIndex)
	{
		//All multiple objects are dispatched to the master objects with given id
		
		//System.out.println("dispatching : " + this.debugInfo(0) + " --> " + target.debugInfo(0));
		
		
		if (!mCompositions.isEmpty())
		{
			for(Entry<Integer, CompositionRelation> entry : mCompositions.entrySet())
				target.substanceRecord.addStructureRelation(entry.getValue());
		}
		
		
		if (!mEffects.isEmpty())
		{
			ProtocolApplication pa = target.mProtocolApplications.get(targetIndex);
			if (pa != null)
			{
				for(Entry<Integer, EffectRecord> entry : mEffects.entrySet())
					pa.addEffect(entry.getValue());
			}	
		}
		
		
		if (!mProtocols.isEmpty())
		{
			ProtocolApplication pa = target.mProtocolApplications.get(targetIndex);
			if (pa != null)
				for(Entry<Integer, Protocol> entry : mProtocols.entrySet())
					pa.setProtocol(entry.getValue());
		}
		
		if (!mProtocolApplications.isEmpty())
		{
			if (target.substanceRecord != null)
			{	
				List<ProtocolApplication> listPA = target.substanceRecord.getMeasurements();
				if (listPA == null)
				{	
					listPA = new ArrayList<ProtocolApplication>();
					target.substanceRecord.setMeasurements(listPA);
				}
				
				for(Entry<Integer, ProtocolApplication> entry : mProtocolApplications.entrySet())
					listPA.add(entry.getValue());
			}	
		}
		
	}
	
	public void dispatchTo(int sourceId, UniversalObject target, int targetIndex)
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
		
		if (mProtocols.isEmpty())
			sb.append("protocols- " + mProtocols.size() + " ");
		
		if (mProtocolApplications.isEmpty())
			sb.append("protocolApplications- " + mProtocolApplications.size() + " ");
		
		if (!mEffects.isEmpty())
			sb.append("effects-" + mEffects.size() + " ");
			
		return sb.toString();
	}
}
