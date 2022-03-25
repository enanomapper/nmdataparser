package net.enanomapper.parser.excel;

public class AggregationBlock 
{
	public static enum BlockType {
		IOM_TEMPLATE, JRC_LINES
	}
	
	public BlockType type = BlockType.IOM_TEMPLATE;
	public int sheetIndex = 0;
	
}
