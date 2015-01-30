package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import ambit2.base.data.SubstanceRecord;
import net.enanomapper.parser.ParserConstants.ElementDataType;



/**
 * 
 * @author nick
 * This class defines information for dynamic extraction of information during iteration
 * This info is typically used by the iteration modes: ROW_MULTI_*, COLUMN_MULTI_*
 * 
 * 
 */
public class DynamicIterationSpan 
{	
	public static class Element
	{
		public ElementDataType dataType= null;
		
		public int index = -1;
		public boolean FlagIndex = false;
		
		public String jsonInfo = null;
		
		public boolean infoFromHeader = true;
		public boolean FlagInfoFromHeader = false;
		
	}
	
	public String masterErrorString = ""; //This is used for error messaging 
	public ArrayList<String> errors = new ArrayList<String>(); 
	
	public boolean handleByRows = true;    //The flag is related to the iteration mode and it determines whether basic data elements are rows or columns
	public boolean FlagHandleByRows = false;
	
	public ElementDataType cumulativeObjectType = null; //This is what type of object is formed by the cumulative effect of all of rows/columns
	public ElementDataType rowType = null;  //This is the default row level grouping 
	//public DataElementType columnType = null;  //This is the default column level grouping
	public ArrayList<Element> elements = null;  
	public ArrayList<DynamicGrouping> groupLevels = null;
		
	//element/data synchronization --> TODO
	
	
	public String toJSONKeyWord(String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "\"DYNAMIC_ITERATION_SPAN\":\n");
		sb.append(offset + "{\n");
		
		
		if (FlagHandleByRows)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"HANDLE_BY_ROWS\" : " + handleByRows + "");
			nFields++;
		}
		
		if (cumulativeObjectType != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"CUMULATIVE_OBJECT_TYPE\" : \"" + cumulativeObjectType.toString() + "\"");
			nFields++;
		}
		
		if (elements != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			
			sb.append(offset + "\t\"ELEMENTS\":\n");
			sb.append(offset + "\t[\n");
			for (int i = 0; i < elements.size(); i++)
			{	
				sb.append(toJSONKeyWord(elements.get(i), offset + "\t\t"));			
				if (i < elements.size()-1) 
					sb.append(",\n");
				sb.append("\n");
			}
			sb.append(offset+"\t]"); 
		}
		
		if (groupLevels != null)
		{
			if (nFields > 0)
				sb.append(",\n\n");
			
			sb.append(offset + "\t\"GROUP_LEVELS\":\n");
			sb.append(offset + "\t[\n");
			for (int i = 0; i < groupLevels.size(); i++)
			{	
				sb.append(groupLevels.get(i).toJSONKeyWord(offset + "\t\t"));			
				if (i < groupLevels.size()-1) 
					sb.append(",\n");
				sb.append("\n");
			}
			sb.append(offset+"\t],\n\n"); 
		}
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");
		
		return sb.toString();
	}
	
	
	public String toJSONKeyWord(Element element, String offset)
	{
		int nFields = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(offset + "{\n");
		
		if (element.dataType != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"DATA_TYPE\" : \"" + element.dataType.toString() + "\"");
			nFields++;
		}
		
		if (element.FlagIndex)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"INDEX\" : " + (element.index + 1));
			nFields++;
		}
		
		if (element.jsonInfo != null)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"JSON_INFO\" : \"" + element.jsonInfo + "\"");
			nFields++;
		}
		
		if (element.FlagInfoFromHeader)
		{
			if (nFields > 0)
				sb.append(",\n");
			sb.append(offset + "\t\"INFO_FROM_HEADER\" : " + element.infoFromHeader + "");
			nFields++;
		}
		
		
		if (nFields > 0)
			sb.append("\n");
		
		sb.append(offset + "}");

		return sb.toString();
	}
	
	
	public boolean checkConsistency()
	{	
		if (rowType != null)
			if (!rowType.isElementOf(cumulativeObjectType))
				errors.add(masterErrorString + " ROW_TYPE "  + rowType.toString() + 
						" is inconsistent with CULULATIVE_OBJECT_TYPE " + cumulativeObjectType.toString());
		
		checkElementConsistency();
		checkGroupLevelConsistency();
		
		return true;
	}
	
	protected boolean checkElementConsistency()
	{
		if (elements == null)
			return true;
		
		if (elements.isEmpty())
			return true;
		
		if (rowType != null)
			for (int i = 0; i < elements.size(); i++)
			{
				if (!elements.get(i).dataType.isElementOf(rowType))
					errors.add(masterErrorString + " ELEMENTS[" + (i+1) + "] type " + elements.get(i).dataType.toString() + 
							" is inconsistent with ROW_TYPE " + rowType.toString());
			}
		
		
		return true;
	}
		
	protected boolean checkGroupLevelConsistency()
	{	
		if (groupLevels == null)
			return true;
		
		if (groupLevels.isEmpty())
			return true;
		
		
		if (!groupLevels.get(0).groupCumulativeType.isElementOf(cumulativeObjectType))
			errors.add(masterErrorString + " GROUP_LEVELS[1].groupCumulativeType " +  groupLevels.get(0).groupCumulativeType.toString() + 
					" is not an element of cumulativeObjectType " + cumulativeObjectType.toString());

		for (int i = 0; i < groupLevels.size(); i++)
		{
			if (!groupLevels.get(i).checkConsistency())
				errors.add(masterErrorString + " GROUP_LEVELS[" + (i+1) + "] inconsistency error!");
			
			if (i > 0)
				if (!groupLevels.get(i).groupCumulativeType.isElementOf(groupLevels.get(i-1).groupCumulativeType))
					errors.add(masterErrorString + " GROUP_LEVELS[" + (i+1) + "].groupCumulativeType " + groupLevels.get(i).groupCumulativeType.toString() 
							+ " is not an element of GROUP_LEVELS[" + i + "].groupCumulativeType " + groupLevels.get(i-1).groupCumulativeType.toString());
		}
				
		return true;
	}
	
	
	public DynamicIterationObject getDynamicIterationObjectFromRows(ArrayList<Row> rows)
	{
		DynamicIterationObject dio = new DynamicIterationObject ();
		
		if (groupLevels == null)
			return handleRows(rows);
		else
			return handleGroupsLavels(rows);
	}
	
	
	protected DynamicIterationObject handleRows(ArrayList<Row> rows)
	{
		DynamicIterationObject dio = new DynamicIterationObject ();
		//TODO
		
		return dio;
	}
	
	protected DynamicIterationObject handleGroupsLavels(ArrayList<Row> rows)
	{
		DynamicIterationObject dio = new DynamicIterationObject ();
		//Currently only one level is handled
		
		
		//TODO
		
		return dio;
	}
	
	
	
	
	/*
	
	public DynamicIterationObject getDynamicIterationObjectFromRows(Sheet sheet, int startRowIndex, int endRowIndex)
	{
		DynamicIterationObject dio = new DynamicIterationObject ();
		//TODO
		return dio;
	}
	
	
	public Object[] createDataObjectsFromRows(Sheet sheet, int startRowIndex, int endRowIndex)
	{
		if (!handleByRows)
			return null;  //This cannot be done if the basic data element is not a row
		
		switch (cumulativeObjectType)
		{
		case SUBSTANCE_ARRAY:
		{	
			return getSubstanceRecordArray(sheet, startRowIndex, endRowIndex).toArray();
		}	
		case SUBSTANCE:
		{	
			SubstanceRecord r = getSubstanceRecord(sheet, startRowIndex, endRowIndex);
			SubstanceRecord array[] = new SubstanceRecord[] {r};
			return array;
		}
		
		default:
			break;
		}
		
		//TODO
		return null;
	}
	
	
	public ArrayList<SubstanceRecord> getSubstanceRecordArray (Sheet sheet, int startRowIndex, int endRowIndex)
	{
		//TODO
		return null;
	}
	
	public SubstanceRecord getSubstanceRecord (Sheet sheet, int startRowIndex, int endRowIndex)
	{
		//TODO
		return null;
	}
	
	*/
	
}
