package net.enanomapper.parser.exceptions;

import ambit2.base.data.study.Protocol;
import net.enanomapper.parser.ExcelDataBlockLocation;

public class ExceptionEffectBlock extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3048843313611743105L;

	public ExceptionEffectBlock(Exception ex,ExcelDataBlockLocation loc, int index, Protocol protocol) {
		super(String.format("Exception %s on getting Effect Block [%s] #%d, in protocol %s. nCheck EFFECT_BLOCK expressions for COLUMN_SUBBLOCKS, ROW_SUBBLOCKS, SUBBLOCK_SIZE_COLUMNS and SUBBLOCK_SIZE_ROWS",
					ex.getMessage(),loc.toString(),index,protocol.toString()));
	
	}
}
