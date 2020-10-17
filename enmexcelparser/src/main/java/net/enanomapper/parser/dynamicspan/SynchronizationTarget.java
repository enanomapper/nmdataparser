package net.enanomapper.parser.dynamicspan;

public class SynchronizationTarget 
{
	public String originalString = null;
	
	//public boolean FlagPrimaryDIO = false;
	//public boolean FlagParallelGroups = false;
	//public boolean FlagParallelRows = false;
	
	public DynamicIterationSpan targetDIS = null;
	public String disID = null;
	public String groupID = null;
	public String rowID = null;
	public String elementID = null;
	
	public String error = null; 
	
	public static SynchronizationTarget parse(String stStr)
	{
		//Parse target in the following syntax   DIS_ID:GROUP:ROW:ELEMENT
		
		SynchronizationTarget st = new SynchronizationTarget();
		st.originalString = stStr;
		String tokens[] = stStr.split(":");
		
		if (tokens.length >= 1)
		{
			/*
			if (tokens[0].equals("PRIMARY"))
				st.FlagPrimaryDIO = true;
			*/	
		}
		
		if (tokens.length >= 2)
		{
			/*
			if (tokens[0].equals("PARALLEL"))
				st.FlagParallelGroups = true;
			*/	
		}
				
		//TODO
		return st;
	}
	
	public String toString()
	{
		return "";
	}
}
