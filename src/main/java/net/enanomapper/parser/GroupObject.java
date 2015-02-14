package net.enanomapper.parser;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.relation.composition.CompositionRelation;

public class GroupObject 
{
	public RowObject rowObjects[] = null;
	//public Object groupObject = null;
	public SubstanceRecord substanceRecord = null;
	public ProtocolApplication protocolApplication = null;
	public EffectRecord effect = null;
	public CompositionRelation composition = null;
	public Protocol protocol = null;
}
