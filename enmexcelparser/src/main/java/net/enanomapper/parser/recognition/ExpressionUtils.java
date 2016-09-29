package net.enanomapper.parser.recognition;

public class ExpressionUtils {
	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static String checkExpressionAsInteger(Object obj) {
		if (obj == null)
			return "null object expression!";

		if (obj instanceof String) {
			String s = (String) obj;
			if (!s.startsWith("="))
				return "Expression string does not start with \"=\"";

			// TODO some other checks

			return null; // No errors found
		}

		if (obj instanceof Integer)
			return null; // No errors found

		return "Expression is not String or Integers object!";
	}
}
