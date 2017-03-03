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
	
	public String toJSONKeyWord()
	{	
		StringBuffer sb = new StringBuffer();
		if (!indexSet.isEmpty())
		{	
			sb.append("[");
			int n = 0;
			for (Integer iObj : indexSet)
			{
				if (n>0)
					sb.append(",");
				sb.append((iObj+1)); //0-base to 1-base indexing
				n++;
			}
			sb.append("]");
				
		}
		else
		{
			sb.append("\"");
			//TODO
			sb.append("\"");
		}
		return sb.toString();
	}	
	
	public static IndexSet getFromJsonNode(JsonNode node) throws Exception
	{
		if (node.isArray())
		{
			IndexSet iSet = new IndexSet(); 
			for (int i = 0; i < node.size(); i++)
			{
				if (node.get(i).isInt())
					iSet.addIndex(node.get(i).intValue() - 1);	//1-base to 0-base indexing			
			}
			return iSet;
		}
		 
		if (node.isTextual())
		{
			//TODO
			return null;
		}
			
		if (node.isInt())
		{
			//TODO
			return null;
		}
		
		throw new Exception ("incorrect index set");
		//return null;
	}
}
