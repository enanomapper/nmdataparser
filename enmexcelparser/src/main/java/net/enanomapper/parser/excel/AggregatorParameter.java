package net.enanomapper.parser.excel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AggregatorParameter 
{
	public enum SubstanceElement {
		CONDITION, PROTOCOL_PARAMETER, PROTOCOL, ENDPOINT, SUBSTANCE_NAME;
	}
	
	public enum AggregationTarget {
		BLOCK_AGGREGATION, BLOCK, SUBBLOCK, VALUE_GROUP;
	}
	
	public AggregatorParameter(String name) {
		this.name = name;
	}
	
	public AggregatorParameter(String name, SubstanceElement substanceElement) {
		this.name = name;
		this.substanceElement = substanceElement;
	}
	
	public String name = null;
	public SubstanceElement substanceElement = SubstanceElement.CONDITION;
	public AggregationTarget aggregationTarget = AggregationTarget.VALUE_GROUP; 
	public boolean isHorizontalOrientation = false;
	public int blockAggregationLevel = 1;
	public Set<Object> values = new HashSet<Object>();
	public List<Object> predefinedValues = null; 
	
	public Object[] getValuesAsArray() {
		if (predefinedValues != null)
			return predefinedValues.toArray();
		else
			return values.toArray();
	}
	
}
