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
	
	public ArrayList<String> getErrors() {
		return errors;
	}
	
	public String getAllErrorsAsString() {
		if (errors.isEmpty())
			return null;
		
		StringBuffer sb = new StringBuffer();
		for (String err : errors)
			sb.append(err + "\n");
		return sb.toString();
	}
	
	
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
		errors.clear();
		
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
		errors.clear();
		
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
		nChars = token.length();
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
			
			
			//Handle interval midle splitter that is not '-' or '+'
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
			
			
			
			//Default: any other char - typically letter and special symbols
			switch (intervalPhase)
			{
			case 0:	{
				String qualifier = getQualifierAtCurPos();
				if (qualifier == null)
				{
					errors.add("In Token #" + (curTokenNum + 1) + " " + token + 
							" Incorrect token!" );
					return null;
				}
				else
					rv.loQualifier = qualifier;
			}break;
			
			case 1:	{
				if (FlagMidSplit)
				{
					
					errors.add("In Token #" + (curTokenNum + 1) + " " + token + 
							" Incorrect symbol for token begining: '" +  token.charAt(curChar) +"'" );
					return null;
				}
				else
				{
					//unit is handled: the rest of the string interpreted as a unit
					String u = token.substring(curChar).trim(); 
					curChar = token.length(); //end of the token analysis
					if (checkUnit(u))
						rv.unit = u;
					else
					{
						errors.add("In Token #" + (curTokenNum + 1) + " " + token + 
								" Incorrect unit: " + u);
						return null;
					}	
				}
			}break;
			
			default: //intervalPhase = 2
				//unit is handled: the rest of the string interpreted as a unit
				String u = token.substring(curChar).trim(); 
				curChar = token.length(); //end of the token analysis
				if (checkUnit(u))
					rv.unit = u;
				else
				{
					errors.add("In Token #" + (curTokenNum + 1) + " " + token + 
							" Incorrect unit: " + u);
					return null;
				}	
			}
			
		} //end of while
		
		
		//Finalize the RichValue object
		switch (intervalPhase)
		{
			//TODO
		}
		
		return rv;
	}
	
	Double extractNumber()
	{
		//System.out.println("extractNumber: " + curToken.substring(curChar));
		
		int startPos = curChar;
		int dotPos = -1;
		int ePos = -1;
		boolean FlagContinue = true;
		
		while (curChar < nChars)
		{
			char ch = curToken.charAt(curChar);
			//System.out.println(" " + curChar + "  " + ch);
			
			if (Character.isDigit(ch))
			{
				curChar++;
				continue;
			}
			
			switch (ch)
			{
			case '-':
			case '+':
			{
				if (curChar == startPos) //The opening minus sign 
				{
					curChar++;
					continue;
				}
				
				if (ePos != -1)
					if (curChar == (ePos + 1)) //Minuse sign after 'E'
					{
						curChar++;
						continue;
					}
				
				//Stop of the number extraction
				FlagContinue = false;
			}break;		
			
			
			case 'e':
			case 'E':	
			{
				if (curChar == startPos)  
				{
					curChar++;
					errors.add("In Token #" + (curTokenNum + 1) + " " + curToken + 
							" Incorrect double number starting with E");
					FlagContinue = false;
				}
				
				if (ePos == -1) 
				{
					ePos = curChar;
					curChar++;
					continue;
				}
				
				//Stop of the number extraction since 'E' is found for a second time
				FlagContinue = false;
			}break;
			
			case '.':	
			{	
				if (dotPos == -1) 
				{
					dotPos = curChar;
					curChar++;
					continue;
				}
				
				//Stop of the number extraction since '.' is found for a second time
				FlagContinue = false;
			}break;
			
			
			default:
				FlagContinue = false;
				break;
			}
			
			if (!FlagContinue)
				break;
		}
		
		String dString = curToken.substring(startPos, curChar);
		
		try{
			Double d = Double.parseDouble(dString);
			return d;
		}
		catch(Exception e)
		{
			errors.add("In Token #" + (curTokenNum + 1) + " " + curToken + 
					" Incorrect double number: " + "\"" + dString + "\"");
			return null;
		}
	}

	String getQualifierAtCurPos()
	{
		for (int i = 0; i < RecognitionUtils.qualifiers.length; i++)
		if (curToken.indexOf(RecognitionUtils.qualifiers[i], curChar) == curChar)
			return RecognitionUtils.qualifiers[i];
		return null;
	}
	
	boolean checkUnit(String unit)
	{
		//TODO - currently does nothing. Everything is allowed for a unit
		return true;
	}
	

	

	
}
