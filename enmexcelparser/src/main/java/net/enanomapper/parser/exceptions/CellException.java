package net.enanomapper.parser.exceptions;

public class CellException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2309827228891628788L;
	public final static String msg_format = "JSON Section %s , sheet %d , row %d ,  cell  %d %s";
	public CellException(String sectionName,int sheet, int row, int column, String msg) {
		super(String.format(msg_format,sectionName,sheet,row,column,msg));
	}
}
