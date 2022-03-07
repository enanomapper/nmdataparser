package net.enanomapper.parser.excel;

import java.util.ArrayList;
import java.util.List;

public class AggregationValueGroup 
{
	public String endpointName = null;
	public String endpointType = null;
	public List<AggregatorParameter> parameters = new ArrayList<AggregatorParameter>();
	
	//Position within a sub-block
	public int startRow = 0;
	public int endRow = 0;
	public int rowSize = 0;
	public int columnSize = 0;
	
}
