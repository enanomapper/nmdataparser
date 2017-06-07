package net.enanomapper.parser.recognition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EffectSet 
{
	public Map<String, Object> effects = new HashMap<String, Object>();
	
	public String toString(EffectSetPattern pattern)
	{
		StringBuffer sb = new StringBuffer();
		Set<String> endpoints = effects.keySet();
		int n = 0;
		for (String ep : endpoints)
		{
			if (n > 0)
				sb.append(" " + pattern.effectSeparator);
			Object value = effects.get(ep);
			sb.append(effectToString(ep, value, pattern));
		}
		return sb.toString();
	}
	
	public String effectToString(String endpoint, Object value, EffectSetPattern pattern)
	{
		StringBuffer sb = new StringBuffer();
		//TODO
		return sb.toString();
	}
}
