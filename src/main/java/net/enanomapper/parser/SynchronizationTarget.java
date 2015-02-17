package net.enanomapper.parser;

public class SynchronizationTarget 
{
	public DynamicIterationSpan targetDIS = null;
	public String disID = null;
	public String groupID = null;
	public String rowID = null;
	public String elementID = null;
	
	public String error = null; 
	
	public static SynchronizationTarget parse(String stStr)
	{
		SynchronizationTarget st = new SynchronizationTarget();
		//Parse target in the following syntax   DIS_ID:GROUP:ROW:ELEMENT
		
		
		//TODO
		return st;
	}
	
	public String toString()
	{
		return "";
	}
}
