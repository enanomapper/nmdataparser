package net.enanomapper.parser;

public class RecognitionUtils 
{
	public static boolean matchTokens(String s1, String s2)
	{	
		return matchTokens(s1, s2, true);
	}
	
	public static boolean matchTokens(String s1, String s2, boolean caseSensitive)
	{
		String tok1[] = s1.split(" ");
		String tok2[] = s2.split(" ");
		
		if (tok1.length != tok2.length)
			return false;
		
		if (caseSensitive)
		{	
			for (int i = 0; i <  tok1.length; i++)
				if (!tok1[i].equals(tok2[i]))
					return false;
		}
		else
		{	
			for (int i = 0; i <  tok1.length; i++)
				if (!tok1[i].equalsIgnoreCase(tok2[i]))
					return false;
		}	
		
		return true;
	}
	
	public static boolean inteligentTokenMatch(String s1, String s2)
	{	
		//TODO
		return false;
	}
	
	
}
