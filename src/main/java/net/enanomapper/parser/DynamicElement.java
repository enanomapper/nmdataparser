package net.enanomapper.parser;

import net.enanomapper.parser.ParserConstants.ElementDataType;

public class DynamicElement 
{
	public ElementDataType dataType= null;
	
	public int index = -1;
	public boolean FlagIndex = false;
	
	public String jsonInfo = null;
	
	public boolean infoFromHeader = true;
	public boolean FlagInfoFromHeader = false;
	
	public int subElementIndices[] = null;
	
}
