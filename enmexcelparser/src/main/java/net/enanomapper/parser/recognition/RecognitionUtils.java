package net.enanomapper.parser.recognition;

public class RecognitionUtils {

	public static class QualifierValue {
		public Double value = null;
		public String qualifier = null;
		public String errorMsg = null;
	}
	//convert to lower case before comparison
	public static final String[] qualifiers = { " ", "=", "<=", ">=", "<", ">",
			 "ca.", "mean", "stddev", "sd","s.d.", "~", "±", "+-",
			"+/-", "geo mean", "average", "median","rsd %"};

	
	/**
	 * It is obligatory that "<=" is before "<" ... because of the order of  qualifier checking 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean matchTokens(String s1, String s2) {
		return matchTokens(s1, s2, true);
	}
	/**
	 * 
	 * @param s1
	 * @param s2
	 * @param caseSensitive
	 * @return
	 */
	public static boolean matchTokens(String s1, String s2,
			boolean caseSensitive) {
		String tok1[] = s1.split(" ");
		String tok2[] = s2.split(" ");

		if (tok1.length != tok2.length)
			return false;

		if (caseSensitive) {
			for (int i = 0; i < tok1.length; i++)
				if (!tok1[i].equals(tok2[i]))
					return false;
		} else {
			for (int i = 0; i < tok1.length; i++)
				if (!tok1[i].equalsIgnoreCase(tok2[i]))
					return false;
		}

		return true;
	}
	/**
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean inteligentTokenMatch(String s1, String s2) {
		// TODO
		return false;
	}
	/**
	 * 
	 * @param valueString
	 * @return
	 */
	public static QualifierValue extractQualifierValue(String valueString) {
		QualifierValue qvalue = new QualifierValue();
		String s = valueString.trim().toLowerCase();

		// Handle qualifier
		for (int i = 0; i < qualifiers.length; i++)
			if (s.startsWith(qualifiers[i])) {
				qvalue.qualifier = qualifiers[i];
				s = (s.substring(qualifiers[i].length())).trim();
				break;
			}

		// Handle value
		try {
			Double d = Double.parseDouble(s);
			qvalue.value = d;
		} catch (Exception e) {
			qvalue.errorMsg = e.getMessage();
		}

		return qvalue;
	}

}
