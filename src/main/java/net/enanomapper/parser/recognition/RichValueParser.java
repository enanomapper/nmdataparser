package net.enanomapper.parser.recognition;

import java.util.ArrayList;
import java.util.List;



public class RichValueParser 
{
	//Setup variables and flags
	private String tokenSplitter = ";";
	private char intervalMiddleSplitter = '-';
	private String defaultIntervalLoQualifier = ">=";
	private String defaultIntervalUpQualifier = "<=";
	
	public boolean FlagAllowIntervalBrackets = false;
	public boolean FlagAllowUnit = true;
	
	
	//Work variables
	protected String rvString = null;
	protected RichValue rvalue = null;
	protected ArrayList<String> errors = new ArrayList<String>();
	
	protected int curChar;
	protected int nChars;
	protected String curToken = null;
	protected int curTokenNum = 0;
	
	
	public String getTokenSplitter() {
		return tokenSplitter;
	}

	public void setTokenSplitter(String tokenSplitter) {
		this.tokenSplitter = tokenSplitter;
	}
	
	public char getIntervalMiddleSplitter() {
		return intervalMiddleSplitter;
	}

	public void setIntervalMiddleSplitter(char intervalMiddleSplitter) {
		this.intervalMiddleSplitter = intervalMiddleSplitter;
	}
	
	public String getDefaultIntervalLoQualifier() {
		return defaultIntervalLoQualifier;
	}

	public void setDefaultIntervalLoQualifier(String defaultIntervalLoQualifier) {
		this.defaultIntervalLoQualifier = defaultIntervalLoQualifier;
	}

	public String getDefaultIntervalUpQualifier() {
		return defaultIntervalUpQualifier;
	}

	public void setDefaultIntervalUpQualifier(String defaultIntervalUpQualifier) {
		this.defaultIntervalUpQualifier = defaultIntervalUpQualifier;
	}
	
	public RichValue parse(String rvStr)
	{
		if (rvStr == null)
		{
			errors.add("Empty string!");
			return null;
		}
		
		rvString = rvStr;
		String tokens[] = rvString.split(tokenSplitter);
		
		curTokenNum = 0;
		rvalue = parseToken(tokens[0]);
		if (!errors.isEmpty())
			return null;
		
		for (int i = 1; i < tokens.length; i++)
		{
			curTokenNum = i;
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
			errors.add("Empty string!");
			return null;
		}
		
		rvString = rvStr;
		String tokens[] = rvString.split(tokenSplitter);
		List<RichValue> list = new ArrayList<RichValue>();
		
		for (int i = 0; i < tokens.length; i++)
		{
			curTokenNum = i;
			RichValue rv = parseToken(tokens[i]);
			if (errors.isEmpty())
				list.add(rv);
			else
			{	
				return null;
			}	
		}
		
		return list;
	}
	
	
	RichValue parseToken(String token)
	{
		//TODO - check for special token values like N/A ...
		
		curToken = token;
		curChar = 0;
		RichValue rv = new RichValue();
		
		int intervalPhase = 0; //0 nothing is read, 1 loValue is read, 2 upValue is read
		boolean FlagMidSplit = false;
		
		
		while (curChar < token.length())
		{	
			if (token.charAt(curChar) == ' ')
				curChar++; //Omitting spaces
			
			if (Character.isDigit(token.charAt(curChar)))
			{	
				Double d = extractNumber();
				if (d == null)
					return null; //error!
				
				switch (intervalPhase)
				{
				case 0:	{
					rv.loValue = d;
					intervalPhase = 1;
				}break;

				case 1:	{
					if (!FlagMidSplit)
					{
						errors.add("In Token #" + (curTokenNum + 1) + " " + token + 
								" Missing middle splitter!");
						return null;
					}
					rv.upValue = d;
					intervalPhase = 2;
				}break;

				default: //intervalPhase = 2
					errors.add("In Token #" + (curTokenNum + 1) + " " + token + 
							" Incorrect digit symbol after the interval definition: '" +  token.charAt(curChar) +"'" );
					return null;
				}
				continue;
				
			}//end of digit handling
			
			
			if (token.charAt(curChar) == '-')
			{	
				if (intervalPhase == 0)
				{	
					//'-' is treated as a beginning of a number
					Double d = extractNumber();
					if (d == null)
						return null; //error!
					rv.loValue = d;
					intervalPhase = 1;
					continue;
				}
				
				if (intervalPhase == 1)
				{	
					if (intervalMiddleSplitter == '-')
					{	
						if (!FlagMidSplit)
						{
							//'-' is treated as a middle splitter
							FlagMidSplit = true;
							curChar++;
							continue;
						}	
					}
					
					//'-' is treated as a beginning of a number
					Double d = extractNumber();
					rv.upValue = d;
					intervalPhase = 2;
					continue;
				}
				
				//For the case (intervalPhase == 2) '-' is treated as part of the unit
			}
			
			if (token.charAt(curChar) == '+')
			{	
				if (intervalPhase == 0)
				{	
					//'+' is treated as a beginning of a number
					Double d = extractNumber();
					if (d == null)
						return null; //error!
					rv.loValue = d;
					intervalPhase = 1;
					continue;
				}
				
				if (intervalPhase == 1)
				{	
					if (intervalMiddleSplitter == '+')  //although this is strange it is possible 
					{	
						if (!FlagMidSplit)
						{
							//'+' is treated as a middle splitter
							FlagMidSplit = true;
							curChar++;
							continue;
						}	
					}
					
					//'+' is treated as a beginning of a number
					Double d = extractNumber();
					rv.upValue = d;
					intervalPhase = 2;
					
					continue;
				}
				
				//For the case (intervalPhase == 2) '+' is treated as part of the unit
			}
			
			if ((token.charAt(curChar) == intervalMiddleSplitter) && 
					(intervalMiddleSplitter != '-') && (intervalMiddleSplitter != '+') ) 
			{
				if (intervalPhase == 1)
				{
					FlagMidSplit = true;
					curChar++;
					continue;
				}	
			}
			
			
			if (FlagAllowIntervalBrackets)
			{
				//TODO - handle (, [, ), ]
			}
			
			//default: handle unit
			//TODO
			
			
		} //end of while
		
		
		//Finalize the RichValue object
		switch (intervalPhase)
		{
		
		}
		
		return rv;
	}
	
	Double extractNumber()
	{
		//TODO
		return null;
	}

	

	

	
}
