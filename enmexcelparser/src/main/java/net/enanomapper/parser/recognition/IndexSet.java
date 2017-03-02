package net.enanomapper.parser.recognition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * @author nick
 * This class is used to handle sets of row/column indices
 */


public class IndexSet 
{	
	//Two approaches for dealing the index set
	Set<Integer> indexSet = new HashSet<Integer>();
	List <int[]> indexRegions = new ArrayList<int[]>();
	
	public boolean contains(int index)
	{
		if (indexSet.isEmpty())
			return regionsContain(index);
		
		return indexSet.contains(index);
	}
	
	public void addIndex(int index)
	{
		indexSet.add(index);
	}
	
	public void addIndices(List<Integer> indices)
	{
		for (Integer ii : indices)
			indexSet.add(ii);
	}
	
	boolean regionsContain(int index)
	{
		//TODO
		return false;
	}
	
	public void addRegion(int region[])
	{
		//TODO
	}
	
	public static IndexSet getFromJsonNode(JsonNode node) throws Exception
	{
		if (node.isArray())
		{
			//TODO
		}
		 
		if (node.isTextual())
		{
			//TODO
		}
			
		if (node.isInt())
		{
			//TODO
		}
		
		return null;
	}
}
