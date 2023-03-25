package net.enanomapper.parser.recognition;

import java.util.ArrayList;
import java.util.List;

public class RichValueParser {
	// Setup variables and flags
	private String tokenSplitter = ";";
	private char intervalMiddleSplitter = '-';
	private String defaultIntervalLoQualifier = ">=";
	private String defaultIntervalUpQualifier = "<=";
	private char plusMinus = '±';

	public boolean FlagAllowIntervalBrackets = false;
	public boolean FlagAllowUnit = true;
	public boolean FlagSetDefaultQualifiersForInterval = true;

	// Work variables
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
	/**
	 * 
	 * @return
	 */
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
	
	public RichValue parse(String rvStr) {
		//By default representPlusMinusAsInterval = false
		//i.e. a ± b is not represented as an interval [a-b, a+b]
		//but as a value and an error
		return parse(rvStr, false);
	}	
	
	/**
	 * 
	 * @param rvStr
	 * @return
	 */
	public RichValue parse(String rvStr, boolean representPlusMinusAsInterval) {
		errors.clear();

		if (rvStr == null) {
			errors.add("Empty string!");
			return null;
		}

		rvString = rvStr;
		String tokens[] = rvString.split(tokenSplitter);

		curTokenNum = 0;
		rvalue = parseToken(tokens[0], representPlusMinusAsInterval);
		if (!errors.isEmpty())
			return null;

		for (int i = 1; i < tokens.length; i++) {
			curTokenNum = i;
			RichValue rv = parseToken(tokens[i], representPlusMinusAsInterval);
			if (errors.isEmpty()) {
				if (rvalue.additionalValues == null)
					rvalue.additionalValues = new ArrayList<RichValue>();

				rvalue.additionalValues.add(rv);
			} else {
				return null;
			}
		}

		// Checking the unit definition in all tokens:
		String u = rvalue.unit;
		if (rvalue.additionalValues != null)
			for (RichValue rv : rvalue.additionalValues) {
				if (rv.unit != null) {
					if (u == null)
						u = rv.unit;
					else {
						// unit u is already set. Checking consistency with
						// rv.unit
						if (!u.equalsIgnoreCase(rv.unit))
							errors.add("Inconsistent unit definitions in different tokens: "
									+ u + "  " + rv.unit);
					}
				}
			}

		// Typically unit definition would be given in the last token.
		// Therefore it is transfered to the first token and all other tokens
		if (rvalue.unit == null)
			rvalue.unit = u;

		if (rvalue.additionalValues != null)
			for (RichValue rv : rvalue.additionalValues)
				if (rv.unit == null)
					rv.unit = u;

		return rvalue;
	}
	/**
	 * 
	 * @param rvStr
	 * @return
	 */
	public List<RichValue> parseAsList(String rvStr, boolean representPlusMinusAsInterval) {
		errors.clear();

		if (rvStr == null) {
			errors.add("Empty string!");
			return null;
		}

		rvString = rvStr;
		String tokens[] = rvString.split(tokenSplitter);
		List<RichValue> list = new ArrayList<RichValue>();

		for (int i = 0; i < tokens.length; i++) {
			curTokenNum = i;
			RichValue rv = parseToken(tokens[i], representPlusMinusAsInterval);
			if (errors.isEmpty())
				list.add(rv);
			else {
				return null;
			}
		}

		if (list.isEmpty())
			return list;

		// Checking the unit definition in all tokens:
		String u = list.get(0).unit;
		for (int i = 1; i < list.size(); i++) {
			RichValue rv = list.get(i);
			if (rv.unit != null) {
				if (u == null)
					u = rv.unit;
				else {
					// unit u is already set. Checking consistency with rv.unit
					if (!u.equalsIgnoreCase(rv.unit))
						errors.add("Inconsistent unit definitions in different tokens: "
								+ u + "  " + rv.unit);
				}
			}
		}

		// Typically unit definition would be given in the last token.
		// Therefore it is transfered to the first token and all other tokens
		for (RichValue rv : list)
			if (rv.unit == null)
				rv.unit = u;

		return list;
	}
	/**
	 * 
	 * @param token
	 * @return
	 */
	RichValue parseToken(String token, boolean representPlusMinusAsInterval) {
		// TODO - check for special token values like N/A ...

		curToken = token;
		nChars = token.length();
		curChar = 0;
		RichValue rv = new RichValue();

		int intervalPhase = 0; // 0 nothing is read, 1 loValue is read, 2
								// upValue is read
		boolean FlagMidSplit = false;
		boolean FlagPlusMinus = false;

		while (curChar < token.length()) {
			char ch = token.charAt(curChar);

			if (ch == ' ') {
				curChar++; // Omitting spaces
				continue;
			}

			if (Character.isDigit(ch) || ch == '.') {
				Double d = extractNumber();
				if (d == null)
					return null; // error!

				switch (intervalPhase) {
				case 0: {
					rv.loValue = d;
					intervalPhase = 1;
				}
					break;

				case 1: {
					if (!FlagMidSplit && !FlagPlusMinus) {
						errors.add("In Token #" + (curTokenNum + 1) + " "
								+ token + " Missing middle splitter!");
						return null;
					}
					rv.upValue = d;
					intervalPhase = 2;
				}
					break;

				default: // intervalPhase = 2
					errors.add("In Token #"
							+ (curTokenNum + 1)
							+ " "
							+ token
							+ " Incorrect digit symbol after the interval definition: char #'"
							+ curChar + " in " + token + "'");
					return null;
				}
				continue;

			}// end of digit handling

			if (ch == '-') {
				if (intervalPhase == 0) {
					// '-' is treated as a beginning of a number
					Double d = extractNumber();
					if (d == null)
						return null; // error!
					rv.loValue = d;
					intervalPhase = 1;
					continue;
				}

				if (intervalPhase == 1) {
					if (intervalMiddleSplitter == '-') {
						if (!FlagMidSplit) {
							// '-' is treated as a middle splitter
							FlagMidSplit = true;
							curChar++;
							continue;
						}
					}

					// '-' is treated as a beginning of a number
					Double d = extractNumber();
					rv.upValue = d;
					intervalPhase = 2;
					continue;
				}

				// For the case (intervalPhase == 2) '-' is treated as part of
				// the unit
			}

			if (ch == '+') {
				if (intervalPhase == 0) {
					// '+' is treated as a beginning of a number
					Double d = extractNumber();
					if (d == null)
						return null; // error!
					rv.loValue = d;
					intervalPhase = 1;
					continue;
				}

				if (intervalPhase == 1) {
					if (intervalMiddleSplitter == '+') // although this is
														// strange it is
														// possible
					{
						if (!FlagMidSplit) {
							// '+' is treated as a middle splitter
							FlagMidSplit = true;
							curChar++;
							continue;
						}
					}

					// '+' is treated as a beginning of a number
					Double d = extractNumber();
					rv.upValue = d;
					intervalPhase = 2;

					continue;
				}

				// For the case (intervalPhase == 2) '+' is treated as part of
				// the unit
			}

			// Handle interval midle splitter that is not '-' or '+'
			if ((ch == intervalMiddleSplitter)
					&& (intervalMiddleSplitter != '-')
					&& (intervalMiddleSplitter != '+')) {
				if (intervalPhase == 1) {
					FlagMidSplit = true;
					curChar++;
					continue;
				}
			}
			
			//Handle plus minus
			if (ch == plusMinus) {
				
				switch (intervalPhase)
				{
				case 0:{
					//It is interpreted as loQualifier
					rv.loQualifier = ""+ plusMinus;
					curChar++;
					continue;
				}
				
				case 1:{
					FlagPlusMinus = true;
					curChar++;
					continue;
				}
				
				}
			}

			if (FlagAllowIntervalBrackets) {
				// TODO - handle (, [, ), ]
			}

			// Default: any other char - typically letter and special symbols
			switch (intervalPhase) {
			case 0: {
				String qualifier = getQualifierAtCurPos();
				if (qualifier == null) {
					errors.add("In Token #" + (curTokenNum + 1) + " " + token
							+ " Incorrect token!");
					return null;
				}

				rv.loQualifier = qualifier;
				curChar += qualifier.length();
			}
				break;

			case 1: {
				if (FlagMidSplit) {
					errors.add("In Token #" + (curTokenNum + 1) + " " + token
							+ " Incorrect symbol after middle splitter: '"
							+ token.charAt(curChar) + "'");
					return null;
				} else {
					// unit is handled: the rest of the string interpreted as a
					// unit
					String u = token.substring(curChar).trim();
					curChar = token.length(); // end of the token analysis
					if (checkUnit(u))
						rv.unit = u;
					else {
						errors.add("In Token #" + (curTokenNum + 1) + " "
								+ token + " Incorrect unit: " + u);
						return null;
					}
				}
			}
				break;

			default: // intervalPhase = 2
				// unit is handled: the rest of the string interpreted as a unit
				String u = token.substring(curChar).trim();
				curChar = token.length(); // end of the token analysis
				if (checkUnit(u))
					rv.unit = u;
				else {
					errors.add("In Token #" + (curTokenNum + 1) + " " + token
							+ " Incorrect unit: " + u);
					return null;
				}
			}

		} // end of while

		// Finalize the RichValue object
		switch (intervalPhase) {
		case 0: {
			if (rv.unit == null)
				errors.add("In Token #" + (curTokenNum + 1) + " " + token
						+ " Empty token!");
			else
				errors.add("In Token #" + (curTokenNum + 1) + " " + token
						+ " Only unit is specified!");
			return null;
		}

		case 1: {
			if (rv.loQualifier != null)
				if (rv.loQualifier.equals("<") || rv.loQualifier.equals("<=")) {
					// The qualifier corresponds to upValue therefore loValue is
					// made to upValue
					rv.upQualifier = rv.loQualifier;
					rv.upValue = rv.loValue;
					rv.loQualifier = null;
					rv.loValue = null;
				}
		}
			break;

		default: // case 2
			
			if (FlagPlusMinus)
			{		
				//In this case upValue is used for +- definition
				double sd = rv.upValue;
				if (representPlusMinusAsInterval)
				{	
					rv.loValue = rv.loValue - sd;
					rv.upValue = rv.loValue + sd;
				}
				else
				{
					rv.errorValue = rv.upValue;
					rv.errorValueQualifier = "±";
					rv.upValue = null;
				}
			}
			
			if (rv.loQualifier != null) {
				errors.add("In Token #" + (curTokenNum + 1) + " " + token
						+ " Defined interval and qualifier together!");
				return null;
			}
			
			if (rv.upValue != null)
				if (rv.loValue > rv.upValue) {
					errors.add("In Token #" + (curTokenNum + 1) + " " + token
							+ " Incorrect interval: loValue > upValue!");
					return null;
				}

			if (rv.upValue != null)
				if (FlagSetDefaultQualifiersForInterval) {
					rv.loQualifier = defaultIntervalLoQualifier;
					rv.upQualifier = defaultIntervalUpQualifier;
				}
			break;
		}

		return rv;
	}
	/**
	 * 
	 * @return
	 */
	Double extractNumber() {
		// System.out.println("extractNumber: " + curToken.substring(curChar));

		int startPos = curChar;
		int dotPos = -1;
		int ePos = -1;
		boolean FlagContinue = true;

		while (curChar < nChars) {
			char ch = curToken.charAt(curChar);
			// System.out.println(" " + curChar + "  " + ch);

			if (Character.isDigit(ch)) {
				curChar++;
				continue;
			}

			switch (ch) {
			case '-':
			case '+': {
				if (curChar == startPos) // The opening minus sign
				{
					curChar++;
					continue;
				}

				if (ePos != -1)
					if (curChar == (ePos + 1)) // Minuse sign after 'E'
					{
						curChar++;
						continue;
					}

				// Stop of the number extraction
				FlagContinue = false;
			}
				break;

			case 'e':
			case 'E': {
				if (curChar == startPos) {
					curChar++;
					errors.add("In Token #" + (curTokenNum + 1) + " "
							+ curToken
							+ " Incorrect double number starting with E");
					FlagContinue = false;
				}

				if (ePos == -1) {
					ePos = curChar;
					curChar++;
					continue;
				}

				// Stop of the number extraction since 'E' is found for a second
				// time
				FlagContinue = false;
			}
				break;

			case '.': {
				if (dotPos == -1) {
					dotPos = curChar;
					curChar++;
					continue;
				}

				// Stop of the number extraction since '.' is found for a second
				// time
				FlagContinue = false;
			}
				break;

			default:
				FlagContinue = false;
				break;
			}

			if (!FlagContinue)
				break;
		}

		String dString = curToken.substring(startPos, curChar);

		try {
			Double d = Double.parseDouble(dString);
			return d;
		} catch (Exception e) {
			errors.add("In Token #" + (curTokenNum + 1) + " " + curToken
					+ " Incorrect double number: " + "\"" + dString + "\"");
			return null;
		}
	}
	/**
	 * 
	 * @return
	 */
	String getQualifierAtCurPos() {
		for (int i = 0; i < RecognitionUtils.qualifiers.length; i++)
			if (curToken.indexOf(RecognitionUtils.qualifiers[i], curChar) == curChar)
				return RecognitionUtils.qualifiers[i];
		return null;
	}
	/**
	 * 
	 * @param unit
	 * @return
	 */
	boolean checkUnit(String unit) {
		// TODO - currently does nothing. Everything is allowed for a unit
		return true;
	}

}
