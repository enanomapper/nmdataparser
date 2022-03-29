package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.List;

public class AggregationValueGroup 
{
	public int blockIndex = 0;
	public String endpointName = null;
	public String endpointType = null;
	public List<AggregatorParameter> parameters = new ArrayList<AggregatorParameter>();
	
	//Position relative to other value group
	public int refAggValueGroupIndex = -1;
	public int horizontalShift = 0;
	public int verticalShift = 0;
	
	//Position within a sub-block
	public int startRow = 0;
	public int endRow = 0;
	public int rowSize = 0;
	public int columnSize = 0;
	
	public String toString(String offset)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "endpointName = " + endpointName  + "\n");
		if (endpointType != null)
			sb.append(offset + "endpointType = " + endpointType + "\n");
		
		sb.append(offset + "refAggValueGroupIndex = " + refAggValueGroupIndex + "\n");
		sb.append(offset + "horizontalShift = " + horizontalShift + "\n");
		sb.append(offset + "verticalShift = " + verticalShift + "\n");
		
		if (!parameters.isEmpty()) {
			sb.append(offset);
			for (AggregatorParameter aggPar : parameters) {
				sb.append(" ");
				sb.append(aggPar.name);
				sb.append("\n");
			}
		}
		
		
		return sb.toString();
	}
	
}
