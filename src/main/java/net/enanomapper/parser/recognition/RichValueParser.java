package net.enanomapper.parser.recognition;

import java.util.ArrayList;
import java.util.List;



public class RichValueParser 
{
	//Setup variables and flags
	private String tokenSplitter = ";";
	
	//Work variables
	protected String rvString = null;
	protected RichValue rvalue = null;
	protected ArrayList<String> errors = new ArrayList<String>();
	
	protected int curTokCh;
	protected int nTokChars;
	protected String curToken = null;
	protected int curTokenNum = 0;
	
	
	public String getTokenSplitter() {
		return tokenSplitter;
	}

	public void setTokenSplitter(String tokenSplitter) {
		this.tokenSplitter = tokenSplitter;
	}
	
	public RichValue parse(String rvStr)
	{
		if (rvStr == null)
		{
			errors.add("Empty input string!");
			return null;
		}
		
		rvString = rvStr;
		
		String tokens[] = rvString.split(tokenSplitter);
		
		curTokenNum = 1;
		rvalue = parseToken(tokens[0]);
		if (!errors.isEmpty())
			return null;
		
		for (int i = 1; i < tokens.length; i++)
		{
			curTokenNum = i+1;
			RichValue rv = parseToken(tokens[i]);
			if (errors.isEmpty())
				rvalue.additionalValues.add(rv);
			else
			{	
				return null;
			}	
		}
		
		return rvalue;
	}
	
	public List<RichValue> parseAsList(String rvStr)
	{
		if (rvStr == null)
		{
			errors.add("Empty input string!");
			return null;
		}
		
		//TODO
		
		return null;
	}
	
	
	RichValue parseToken(String token)
	{
		//TODO
		return null;
	}

	
}
