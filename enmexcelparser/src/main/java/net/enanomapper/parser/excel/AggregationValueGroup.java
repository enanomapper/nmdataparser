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
	
}
