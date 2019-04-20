package net.enanomapper.parser.recognition;

import java.util.List;


public class Tokenize 
{
	public static enum Mode {
		REGIONS, SPLIT, UNDEFINED  
	}
	
	public Mode mode = Mode.REGIONS;	
	public String splitter = null;
	public List<TokenRegion> regions = null;
	
}
