package net.enanomapper.parser;

import ambit2.base.relation.STRUCTURE_RELATION;
import net.enanomapper.parser.ParserConstants.StructureInfoType;

public class CompositionDataLocation 
{
	public ExcelDataLocation location = null;
	public StructureInfoType strInfoType = StructureInfoType.FORMULA;
	public STRUCTURE_RELATION strRelation = STRUCTURE_RELATION.CORE_OF;
}
