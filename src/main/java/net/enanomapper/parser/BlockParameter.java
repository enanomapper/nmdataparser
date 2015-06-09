package net.enanomapper.parser;

import net.enanomapper.parser.ParserConstants.BlockParameterAssign;

public class BlockParameter 
{	
	public BlockParameterAssign assign = BlockParameterAssign.ASSIGN_TO_BLOCK;
	public boolean FlagAssign = false;
		
	public int columnPos = 0;
	public boolean FlagColumnPos = false;
	
	public int rowPos = 0;
	public boolean FlagRowPos = false;	
	
	public int valueRelativeColumnPos = -1;
	public boolean FlagValueRelativeColumnPos = false;
	
	public int valueRelativeRowPos = 0;
	public boolean FlagValueRelativeRowPos = false;
}
