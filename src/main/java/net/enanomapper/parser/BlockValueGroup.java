package net.enanomapper.parser;

import java.util.List;

import org.codehaus.jackson.JsonNode;


/**
 * This class defines how a group of values (with associated parameters for each value)
 * is extracted from particular sub-block
 * @author nick
 *
 */
public class BlockValueGroup 
{	
	public String name = null;
	public boolean FlagName = false;
	
	//Values definitions are in the context of sub-block
	public Object valuesStartColumn = new Integer(1);  
	public boolean FlagValuesStartColumn = false;

	public Object valuesEndColumn = new Integer(1);
	public boolean FlagValuesEndColumn = false;

	public Object valuesStartRow = new Integer(1);  
	public boolean FlagValuesStartRow = false;

	public Object valuesEndRow = new Integer(1);
	public boolean FlagValuesEndRow = false;

	//The shifts are relative to the corresponding value position (value by default is treated as lo-value)
	public Object qualifierColumnPosShift = new Integer(0); 
	public boolean FlagQualifierColumnPosShift = false;

	public Object qualifierRowPosShift = new Integer(0); 
	public boolean FlagQualifierRowPosShift = false;

	public Object upValueColumnPosShift = new Integer(0); 
	public boolean FlagUpValueColumnPosShift = false;

	public Object upValueRowPosShift = new Integer(0); 
	public boolean FlagUpValueRowPosShift = false;

	public Object upQualifierColumnPosShift = new Integer(0); 
	public boolean FlagUpQualifierColumnPosShift = false;

	public Object upQualifierRowPosShift = new Integer(0); 
	public boolean FlagUpQualifierRowPosShift = false;

	public Object errorColumnPosShift = new Integer(0); 
	public boolean FlagErrorColumnPosShift = false;

	public Object errorRowPosShift = new Integer(0); 
	public boolean FlagErrorRowPosShift = false;
	
	public List<BlockParameter> parameters = null;
	
	
	public static BlockValueGroup extractValueGroup(JsonNode node, ExcelParserConfigurator conf)
	{
		BlockValueGroup bvg = new BlockValueGroup();
		//TODO
		
		
		return bvg;
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
