package net.enanomapper.parser;

import org.codehaus.jackson.JsonNode;

import net.enanomapper.parser.ParserConstants.BlockParameterAssign;

public class BlockParameter 
{	
	public BlockParameterAssign assign = BlockParameterAssign.ASSIGN_TO_BLOCK;
	public boolean FlagAssign = false;
		
	public int columnPos = 0;
	public boolean FlagColumnPos = false;
	
	public int rowPos = 0;
	public boolean FlagRowPos = false;	
	
	public int relativeToValueColumnPos = -1;
	public boolean FlagRelativeToValueColumnPos = false;
	
	public int relativeToValueRowPos = 0;
	public boolean FlagRelativeToValueRowPos = false;
	
	public static BlockParameter extractBlockParameter(JsonNode node, ExcelParserConfigurator conf)
	{
		BlockParameter bp = new BlockParameter();
		//TODO
		
		return bp;
	}
	
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		
		sb.append(offset + "{\n");
		
		//TODO
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
	}	
}
