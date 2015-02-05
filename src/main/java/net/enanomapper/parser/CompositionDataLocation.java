package net.enanomapper.parser;

import java.util.HashMap;

import ambit2.base.relation.STRUCTURE_RELATION;


public class CompositionDataLocation 
{	
	public STRUCTURE_RELATION structureRelation = STRUCTURE_RELATION.HAS_CORE;
	public boolean FlagStructureRelation = false;
	
	//Locations for the Structure Record
	public ExcelDataLocation content = null;
	public ExcelDataLocation format = null;
	public ExcelDataLocation inchiKey = null;
	public ExcelDataLocation inchi = null;
	public ExcelDataLocation formula = null;
	public ExcelDataLocation smiles = null;
	public HashMap<String, ExcelDataLocation> properties = null;
	
	//Proportion data locations
	
	
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		if (FlagStructureRelation)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(offset + "\t\"STRUCTURE_RELATION\" : \"" + structureRelation.toString() + "\"");
			nFields++;
		}
		
		if (content != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(content.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (format != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(format.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (inchiKey != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(inchiKey.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (inchi != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(inchi.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (formula != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(formula.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		if (smiles != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			sb.append(smiles.toJSONKeyWord(offset + "\t"));
			nFields++;
		}
		
		
		if (properties != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");

			sb.append(offset + "\t\"PROPERTIES\" : \n" );
			sb.append(offset + "\t{\n" );
			
			int nProps = 0;
			for (String prop : properties.keySet())
			{	
				ExcelDataLocation loc = properties.get(prop);
				sb.append(loc.toJSONKeyWord(offset+"\t\t"));
				
				if (nProps < properties.size())
					sb.append(",\n\n");
				else
					sb.append("\n");
				nProps++;
			}
			sb.append(offset + "\t}" );
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
