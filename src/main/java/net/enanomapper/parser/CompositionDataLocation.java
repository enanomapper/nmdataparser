package net.enanomapper.parser;

import ambit2.base.relation.STRUCTURE_RELATION;


public class CompositionDataLocation 
{	
	public STRUCTURE_RELATION structureRelation = STRUCTURE_RELATION.HAS_CORE;
	public boolean FlagStructureRelation = false;
	
	//Locations for the Structure Record
	public ExcelDataLocation inchiKey = null;
	
	
	
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		if (FlagStructureRelation)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"STRUCTURE_RELATION\" : \"" + structureRelation.toString() + "\"");
			nFields++;
		}
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");

		return sb.toString();
	}
	
	
	public static  STRUCTURE_RELATION structureRelationFromString(String s)
	{
		try{
			STRUCTURE_RELATION res = STRUCTURE_RELATION.valueOf(s);
			return res;
		}
		catch (Exception e){
			return null;
		}
	}
	
}
