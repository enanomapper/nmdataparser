package net.enanomapper.parser.excel;

import java.util.List;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.IRawReader;

public class SubstanceDataAggregator 
{
	public enum AggrationMode {
		DATA_BLOCKS, ROWS, UNDEFINED;

		public static AggrationMode fromString(String s) {
			try {
				AggrationMode aggMod = AggrationMode.valueOf(s);
				return (aggMod);
			} catch (Exception e) {
				return AggrationMode.UNDEFINED;
			}
		}
	}

	public AggrationMode aggrationMode = AggrationMode.UNDEFINED;
	

	public void iterate(IRawReader<IStructureRecord> substanceIterator)
	{
		if (substanceIterator == null)
			return;

		while (substanceIterator.hasNext()) {
			SubstanceRecord r = (SubstanceRecord)substanceIterator.nextRecord();
			analyse(r);
		}
	}

	public void analyse(SubstanceRecord rec) 
	{
		List<ProtocolApplication> paList = rec.getMeasurements();
		for (ProtocolApplication pa : paList) 
		{
			analyse(pa);
		}	
	}


	public void analyse(ProtocolApplication pa)
	{
		List<EffectRecord> effects = pa.getEffects();
		for (EffectRecord eff : effects)
		{

		}
	}

	public void analyse(EffectRecord eff)
	{
		//TODO
	}
}
