package net.enanomapper.parser.exceptions;

import net.enanomapper.parser.ExcelDataLocation;


public class ExceptionAtLocation extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4029533223007396485L;

	public ExceptionAtLocation(ExcelDataLocation loc, int sheet, String typeExpected, String msg) {
		super(String.format("[%s ] Sheet %d %s '%s'",loc.sectionName,sheet,typeExpected,msg));
	}
}
