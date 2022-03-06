package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.List;

public class AggregatorParameter 
{
	public enum SubstanceElement {
		CONDITION, PROTOCOL_PARAMETER, SUBSTANCE_NAME;
	}
	
	public enum AggregationTarget {
		BLOCK_AGGREGATION, BLOCK, SUBBLOCK, VALUE_GROUP;
	}
	
	public AggregatorParameter(String name) {
		this.name = name;
	}
	
	public String name = null;
	public SubstanceElement substanceElement = SubstanceElement.CONDITION;
	public AggregationTarget aggregationTarget = AggregationTarget.VALUE_GROUP; 
	public boolean isHorizontalOrientation = false;
	public int blockAggregationLevel = 1;
	public List<Object> values = new ArrayList<Object>();
}
