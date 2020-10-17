package net.enanomapper.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import ambit2.base.data.study.Params;
import net.enanomapper.parser.BlockValueGroupExtractedInfo.ParamInfo;
import net.enanomapper.parser.ParserConstants.BlockParameterAssign;
import net.enanomapper.parser.ParserConstants.DataInterpretation;
import net.enanomapper.parser.ParserConstants.IterationAccess;
import net.enanomapper.parser.excel.ExcelUtils;
import net.enanomapper.parser.recognition.RichValue;
import net.enanomapper.parser.recognition.RichValueParser;

public class ExcelDataBlockUtils 
{
	protected RichValueParser rvParser = new RichValueParser();
	protected JexlEngine jexlEngine = null;
	
	protected Logger logger = null;
	protected SubstanceRecordMap substRecordMap = null; 
	protected ExcelParserConfigurator config = null;
	protected Workbook workbook = null;
	protected ArrayList<Row> curRows = null;
	protected int curRowNum = 1;
	protected HashMap<String, Object> curVariables = null;
	protected HashMap<String, HashMap<Object, Object>> curVariableMappings = null;
	protected int curRowSubblock = 1;
	protected int curColumnSubblock = 1;
	
	

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public SubstanceRecordMap getSubstRecordMap() {
		return substRecordMap;
	}

	public void setSubstRecordMap(SubstanceRecordMap substRecordMap) {
		this.substRecordMap = substRecordMap;
	}
	

	public ExcelParserConfigurator getConfig() {
		return config;
	}

	public void setConfig(ExcelParserConfigurator config) {
		this.config = config;
	}

	public Workbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(Workbook workbook) {
		this.workbook = workbook;
	}


	public ArrayList<Row> getCurRows() {
		return curRows;
	}

	public void setCurRows(ArrayList<Row> curRows) {
		this.curRows = curRows;
	}

	public int getCurRowNum() {
		return curRowNum;
	}


	public void setCurRowNum(int curRowNum) {
		this.curRowNum = curRowNum;
	}


	public HashMap<String, Object> getCurVariables() {
		return curVariables;
	}

	public void setCurVariables(HashMap<String, Object> curVariables) {
		this.curVariables = curVariables;
	}

	public HashMap<String, HashMap<Object, Object>> getCurVariableMappings() {
		return curVariableMappings;
	}


	public void setCurVariableMappings(HashMap<String, HashMap<Object, Object>> curVariableMappings) {
		this.curVariableMappings = curVariableMappings;
	}
		
	
	protected List<DataBlockElement> getDataBlockFromCellMatrix(Cell cells[][], int rowSubblocks, int columnSubblocks,
			int subblockSizeRows, int subblockSizeColumns, ExcelDataBlockLocation exdb_loc) {
		List<DataBlockElement> dbeList = new ArrayList<DataBlockElement>();

		if (exdb_loc.valueGroups == null)
			return dbeList;
		
		/*
		// Analyze value groups: positions info is extracted from the used
		// expressions
		List<BlockValueGroupExtractedInfo> bvgExtrInfo = extractAllBlockValueGroups(exdb_loc);
		*/

		// Iterating all sub-blocks
		for (int sbRow = 0; sbRow < rowSubblocks; sbRow++)
			for (int sbColumn = 0; sbColumn < columnSubblocks; sbColumn++) {
				// Upper left corner of the current sub-block
				int row0 = sbRow * subblockSizeRows;
				int column0 = sbColumn * subblockSizeColumns;
				curRowSubblock = sbRow + 1; //0-based --> 1-based
				curColumnSubblock = sbColumn + 1; //0-based --> 1-based
				
				// Analyze value groups: positions info is extracted from the used
				// expressions
				List<BlockValueGroupExtractedInfo> bvgExtrInfo = extractAllBlockValueGroups(exdb_loc);
								
				for (BlockValueGroupExtractedInfo bvgei : bvgExtrInfo) {
					if (bvgei.FlagValues) {
						// Shifted by -1 to make it 0-based indexing
						for (int i = bvgei.startRow - 1; i <= bvgei.endRow - 1; i++)
							for (int k = bvgei.startColumn - 1; k <= bvgei.endColumn - 1; k++) {
								Object o = ExcelUtils.getObjectFromCell(cells[row0 + i][column0 + k]);
								// Handle empty cell or incorrect values
								if (o == null)
									continue;

								DataBlockElement dbEl = new DataBlockElement();

								// Setting the endpoint name stored in the field
								// dbEl.blockValueGroup
								if (bvgei.endpointAssign == BlockParameterAssign.UNDEFINED)
									dbEl.blockValueGroup = bvgei.name;
								else {
									Cell c = null;
									switch (bvgei.endpointAssign) {
									case ASSIGN_TO_EXCEL_SHEET:
										// -1 for 0-based
										c = getCellFromSheet(exdb_loc.location.sheetIndex, bvgei.endpointRowPos - 1,
												bvgei.endpointColumnPos - 1);
										break;
									case ASSIGN_TO_BLOCK:
										// -1 for 0-based
										c = cells[bvgei.endpointRowPos - 1][bvgei.endpointColumnPos - 1];
										break;
									case ASSIGN_TO_SUBBLOCK:
										// (endpointRowPos,endpointColumnPos)
										// are the sub-block position
										// -1 for 0-based indexing
										c = cells[row0 + bvgei.endpointRowPos - 1][column0 + bvgei.endpointColumnPos
												- 1];
										break;
									case ASSIGN_TO_VALUE:
										// (endpointRowPos,endpointColumnPos)
										// are used as shifts
										int ep_row;
										if (bvgei.fixEndpointRowPosToStartValue)
											ep_row = row0 + (bvgei.startRow - 1) + bvgei.endpointRowPos;
										else
											ep_row = row0 + i + bvgei.endpointRowPos;

										int ep_col;
										if (bvgei.fixEndpointColumnPosToStartValue)
											ep_col = column0 + (bvgei.startColumn - 1) + bvgei.endpointColumnPos;
										else
											ep_col = column0 + k + bvgei.endpointColumnPos;

										c = cells[ep_row][ep_col];
										break;
									}

									if (c != null) {
										Object value = ExcelUtils.getObjectFromCell(c);
										if (value != null) {
											if (bvgei.endpointMapping != null)
												value = getMappingValue(value, bvgei.endpointMapping);
											if (value != null) {
												if ((bvgei.name != null) && bvgei.addValueGroupToEndpointName) {
													if (bvgei.addValueGroupAsPrefix)
														dbEl.blockValueGroup = bvgei.name + bvgei.separator
																+ value.toString();
													else
														dbEl.blockValueGroup = value.toString() + bvgei.separator
																+ bvgei.name;
												} else
													dbEl.blockValueGroup = value.toString();
											}
										}
									}

								}

								dbEl.unit = bvgei.unit; // The unit may be
														// overridden by the
														// setValue() function

								// By default if object o is a pure number
								// is stored as loValue
								dbEl.setValue(o, rvParser);

								if (bvgei.errorColumnShift != 0 || bvgei.errorRowShift != 0) {
									Number d = (Double) ExcelUtils
											.getNumericValue(cells[row0 + i + bvgei.errorRowShift][column0 + k
													+ bvgei.errorColumnShift]);
									if (d != null)
										dbEl.error = d.doubleValue();
								}

								// Handle ENDPOINT_TYPE
								if (bvgei.endpointType != null) {
									BlockValueGroupExtractedInfo.ParamInfo pi = bvgei.endpointType;
									Cell c = getCell(pi, cells, row0, column0, i, k, bvgei, exdb_loc);

									/*
									 * if (pi.jsonValue != null) { // json value
									 * takes precedence // over ASSIGN // This
									 * case is technically possible but instead
									 * should be // used short syntax:
									 * "ENDPOINT_TYPE" : "value" Object value =
									 * pi.jsonValue;
									 * 
									 * if (pi.mapping != null) value =
									 * getMappingValue(pi.jsonValue,
									 * pi.mapping);
									 * 
									 * value =
									 * RichValue.recognizeRichValueFromObject(
									 * value, pi.unit, rvParser);
									 * dbEl.endpointType = value.toString();
									 * //continue; }
									 */

									if (c != null) {
										Object value = ExcelUtils.getObjectFromCell(c);
										if (pi.mapping != null)
											value = getMappingValue(value, pi.mapping);
										if (value != null)
											dbEl.endpointType = value.toString();
									}
								} else {
									if (bvgei.endpointTypeString != null)
										dbEl.endpointType = bvgei.endpointTypeString;
								}

								// Handle VALUE_QUALIFIER
								if (bvgei.valueQualifier != null) {
									ParamInfo pi = bvgei.valueQualifier;
									Cell c = getCell(pi, cells, row0, column0, i, k, bvgei, exdb_loc);

									if (c != null) {
										Object value = ExcelUtils.getObjectFromCell(c);
										if (pi.mapping != null)
											value = getMappingValue(value, pi.mapping);
										// by default it is stored as
										// loQualifier
										if (value != null)
											dbEl.loQualifier = value.toString();
									}
								} else {
									if (bvgei.valueQualifierString != null)
										dbEl.loQualifier = bvgei.valueQualifierString;
								}

								// Handle ERROR_QUALIFIER
								if (bvgei.errorQualifier != null) {
									ParamInfo pi = bvgei.errorQualifier;
									Cell c = getCell(pi, cells, row0, column0, i, k, bvgei, exdb_loc);

									if (c != null) {
										Object value = ExcelUtils.getObjectFromCell(c);
										if (pi.mapping != null)
											value = getMappingValue(value, pi.mapping);
										if (value != null)
											dbEl.errQualifier = value.toString();
									}
								} else {
									if (bvgei.errorQualifierString != null)
										dbEl.errQualifier = bvgei.errorQualifierString;
								}
								
								// Handle SUBSTANCE_RECORD_MAP
								if (bvgei.substanceRecordMap != null) {
									ParamInfo pi = bvgei.substanceRecordMap;
									Cell c = getCell(pi, cells, row0, column0, i, k, bvgei, exdb_loc);

									if (c != null) {
										Object value = ExcelUtils.getObjectFromCell(c);
										if (pi.mapping != null)
											value = getMappingValue(value, pi.mapping);
										if (value != null)
											dbEl.substanceRecordMap = value.toString();
									}
								} else {
									if (bvgei.substanceRecordMapString != null)
										dbEl.substanceRecordMap = bvgei.substanceRecordMapString;
								}
								

								// Handle value group parameters (which are
								// effect conditions)
								if (bvgei.paramInfo != null)
									if (!bvgei.paramInfo.isEmpty()) {
										dbEl.params = new Params();
										for (BlockValueGroupExtractedInfo.ParamInfo pi : bvgei.paramInfo) {
											if (pi.jsonValue != null) {
												// json value takes precedence
												// over ASSIGN
												Object value = pi.jsonValue;

												if (pi.mapping != null)
													value = getMappingValue(pi.jsonValue, pi.mapping);

												if (pi.dataInterpretation == DataInterpretation.AS_TEXT)
												{
													//Take value as a string
													value = value.toString();
												}
												else
													value = RichValue.recognizeRichValueFromObject(value, pi.unit,
														rvParser);
												
												dbEl.params.put(pi.name, value);
												continue;
											}

											Cell c = null;
											switch (pi.assign) {
											case ASSIGN_TO_EXCEL_SHEET:
												int rowShift = 0;
												int colShift = 0;
												if (pi.syncExcelSheetRowWithValuePos)
													rowShift = i - (bvgei.startRow - 1);
												if (pi.syncExcelSheetColumnWithValuePos)
													colShift = k - (bvgei.startColumn - 1);
												// -1 for 0-based												
												c = getCellFromSheet(exdb_loc.location.sheetIndex, pi.rowPos - 1 + rowShift,
														pi.columnPos - 1 + colShift);
												break;
											case ASSIGN_TO_BLOCK:
												// -1 for 0-based
												c = cells[pi.rowPos - 1][pi.columnPos - 1];
												break;
											case ASSIGN_TO_SUBBLOCK:
												// (rowPos,columnPos) are the
												// sub-block position
												// -1 for 0-based indexing
												c = cells[row0 + pi.rowPos - 1][column0 + pi.columnPos - 1];
												break;
											case ASSIGN_TO_VALUE:
												// (pi.rowPos,pi.columnPos) are
												// used as shifts
												int par_row;
												if (pi.fixRowPosToStartValue)
													par_row = row0 + (bvgei.startRow - 1) + pi.rowPos;
												else
													par_row = row0 + i + pi.rowPos;

												int par_col;
												if (pi.fixColumnPosToStartValue)
													par_col = column0 + (bvgei.startColumn - 1) + pi.columnPos;
												else
													par_col = column0 + k + pi.columnPos;

												c = cells[par_row][par_col];
												break;
											case UNDEFINED:
												// nothing is done
												break;
											}

											if (c != null) {
												Object value = ExcelUtils.getObjectFromCell(c);

												if (value != null) {
													if (pi.mapping != null)
														value = getMappingValue(value, pi.mapping);
													
													if (pi.dataInterpretation == DataInterpretation.AS_TEXT)
													{
														//Take value as it is
														value = value.toString();
													}
													else
														value = RichValue.recognizeRichValueFromObject(value, pi.unit,
															rvParser);
													
													dbEl.params.put(pi.name, value);
												}
											}
										}
									}

								dbeList.add(dbEl);
							}
					}

				}

			} // iterating all sub-blocks

		return dbeList;
	}
	
	/*
	 * This function is used for extracting cell information for DataBlock elements 
	 * associated with ParamInfo  
	 */
	Cell getCell(ParamInfo pi, Cell cells[][], int row0, int column0, int i, int k, BlockValueGroupExtractedInfo bvgei,
			ExcelDataBlockLocation exdb_loc) {
		// Upper left corner of the current sub-block (row0, column0)
		// Current value position in the sub-block (i,k)

		Cell c = null;
		switch (pi.assign) {
		case ASSIGN_TO_EXCEL_SHEET:
			int rowShift = 0;
			int colShift = 0;			
			if (pi.syncExcelSheetRowWithValuePos)
				rowShift = i - (bvgei.startRow - 1);
			if (pi.syncExcelSheetColumnWithValuePos)
				colShift = k - (bvgei.startColumn - 1);
			// -1 for 0-based
			c = getCellFromSheet(exdb_loc.location.sheetIndex, 
					pi.rowPos - 1 + rowShift, pi.columnPos - 1 + colShift);
			break;
		case ASSIGN_TO_BLOCK:
			// (pi.rowPos,pi.columnPos) define
			// the block position
			// -1 for 0-based
			c = cells[pi.rowPos - 1][pi.columnPos - 1];
			break;
		case ASSIGN_TO_SUBBLOCK:
			// (pi.rowPos,pi.columnPos) define
			// the sub-block position
			// -1 for 0-based indexing
			c = cells[row0 + pi.rowPos - 1][column0 + pi.columnPos - 1];
			break;
		case ASSIGN_TO_VALUE:
			// (pi.rowPos,pi.columnPos) are
			// used as shifts from the value position (i,k)
			int par_row;
			if (pi.fixRowPosToStartValue)
				par_row = row0 + (bvgei.startRow - 1) + pi.rowPos;
			else
				par_row = row0 + i + pi.rowPos;

			int par_col;
			if (pi.fixColumnPosToStartValue)
				par_col = column0 + (bvgei.startColumn - 1) + pi.columnPos;
			else
				par_col = column0 + k + pi.columnPos;

			c = cells[par_row][par_col];
			break;
		case UNDEFINED:
			// nothing is done
			break;
		}

		return c;
	}

	protected Cell getCellFromSheet(int sheetNum, int rowNum, int columnNum) {
		Sheet sheet = workbook.getSheetAt(sheetNum);
		Row row = sheet.getRow(rowNum);
		if (row != null)
			return row.getCell(columnNum);
		return null;
	}
	
	
	protected List<BlockValueGroupExtractedInfo> extractAllBlockValueGroups(ExcelDataBlockLocation exdb_loc)
	{
		// Analyze value groups: positions info is extracted from the used expressions
		List<BlockValueGroupExtractedInfo> bvgExtrInfo = new ArrayList<BlockValueGroupExtractedInfo>();
		for (BlockValueGroup bvg : exdb_loc.valueGroups) {
			BlockValueGroupExtractedInfo bvgei = extractBlockValueGroup(bvg);
			if (bvgei.getErrors().isEmpty())
				bvgExtrInfo.add(bvgei);
			else {
				logger.warning("------- Value Group " + bvg.name + "errors:");
				for (String err : bvgei.getErrors())
					logger.warning("   --- " + err);
			}
		}
		
		return bvgExtrInfo;
	}
	
	
	protected BlockValueGroupExtractedInfo extractBlockValueGroup(BlockValueGroup bvg) {
		BlockValueGroupExtractedInfo bvgei = new BlockValueGroupExtractedInfo();

		if (bvg.name != null) {
			bvgei.name = getStringFromExpression(bvg.name);
			if (bvgei.name == null)
				bvgei.errors.add("VALUE_GROUPS: \"NAME\" is an incorrect expression: " + bvg.name);
		}

		// Setting of the endpoint by assigning it to block/sub-block/value
		if (bvg.endpointAssign != BlockParameterAssign.UNDEFINED) {
			bvgei.endpointAssign = bvg.endpointAssign;
			bvgei.endpointColumnPos = getIntegerFromExpression(bvg.endpointColumnPos);
			if (bvgei.endpointColumnPos == null) {
				bvgei.errors.add("ENDPOINT_COLUMN_POS:  incorrect result for expression: " + bvg.endpointColumnPos);
			}

			bvgei.endpointRowPos = getIntegerFromExpression(bvg.endpointRowPos);
			if (bvgei.endpointRowPos == null) {
				bvgei.errors.add("ENDPOINT_ROW_POS:  incorrect result for expression: " + bvg.endpointRowPos);
			}

			bvgei.fixEndpointColumnPosToStartValue = bvg.fixEndpointColumnPosToStartValue;
			bvgei.fixEndpointRowPosToStartValue = bvg.fixEndpointRowPosToStartValue;

			if (bvg.endpointMapping != null)
				bvgei.endpointMapping = bvg.endpointMapping;

			bvgei.addValueGroupToEndpointName = bvg.addValueGroupToEndpointName;
			bvgei.addValueGroupAsPrefix = bvg.addValueGroupAsPrefix;
			bvgei.separator = bvg.separator;
		}

		if (bvg.unit != null) {
			bvgei.unit = getStringFromExpression(bvg.unit);
			if (bvgei.name == null)
				bvgei.errors.add("VALUE_GROUPS: \"UNIT\" is an incorrect expression: " + bvg.unit);
		}

		// Handle values
		bvgei.startColumn = getIntegerFromExpression(bvg.startColumn);
		bvgei.endColumn = getIntegerFromExpression(bvg.endColumn);
		bvgei.startRow = getIntegerFromExpression(bvg.startRow);
		bvgei.endRow = getIntegerFromExpression(bvg.endRow);

		bvgei.errorColumnShift = getIntegerFromExpression(bvg.errorColumnShift);
		bvgei.errorRowShift = getIntegerFromExpression(bvg.errorRowShift);

		logger.info("--- Extracting inffo for value group: " + bvg.name);
		logger.info("--- startColumn " + bvgei.startColumn);
		logger.info("--- endColumn " + bvgei.endColumn);
		logger.info("--- startRow " + bvgei.startRow);
		logger.info("--- endRow " + bvgei.endRow);

		bvgei.FlagValues = true;

		if (bvgei.startColumn == null) {
			bvgei.errors.add("START_COLUMN:  incorrect result for expression: " + bvg.startColumn);
			bvgei.FlagValues = false;
		}

		if (bvgei.endColumn == null) {
			bvgei.errors.add("END_COLUMN:  incorrect result for expression: " + bvg.endColumn);
			bvgei.FlagValues = false;
		}

		if (bvgei.startRow == null) {
			bvgei.errors.add("START_ROW:  incorrect result for expression: " + bvg.startRow);
			bvgei.FlagValues = false;
		}

		if (bvgei.endRow == null) {
			bvgei.errors.add("END_ROW:  incorrect result for expression: " + bvg.endRow);
			bvgei.FlagValues = false;
		}

		if (bvgei.FlagValues) {
			if (bvgei.startColumn > bvgei.endColumn) {
				bvgei.errors.add("START_COLUMN > END_COLUMN");
				bvgei.FlagValues = false;
			}

			if (bvgei.startRow > bvgei.endRow) {
				bvgei.errors.add("START_ROW > END_ROW");
				bvgei.FlagValues = false;
			}

			if (bvgei.errorColumnShift == null) {
				bvgei.errors.add("ERROR_COLUMN_SHIFT:  incorrect result for expression: " + bvg.errorColumnShift);
				bvgei.FlagValues = false;
			}

			if (bvgei.errorRowShift == null) {
				bvgei.errors.add("ERROR_ROW_SHIFT:  incorrect result for expression: " + bvg.errorRowShift);
				bvgei.FlagValues = false;
			}
		}

		if (bvg.parameters != null)
			if (!bvg.parameters.isEmpty()) {
				bvgei.paramInfo = new ArrayList<BlockValueGroupExtractedInfo.ParamInfo>();

				for (int i = 0; i < bvg.parameters.size(); i++) {
					boolean FlagParamOK = true;
					BlockParameter bp = bvg.parameters.get(i);
					BlockValueGroupExtractedInfo.ParamInfo pi = new BlockValueGroupExtractedInfo.ParamInfo();
					if (bp.name == null) {
						bvgei.errors.add("Parameter " + (i + 1) + ": NAME is missing!");
						FlagParamOK = false;
					} else
						pi.name = bp.name;

					if (bp.jsonValue != null)
						pi.jsonValue = bp.jsonValue;

					if (bp.assign == BlockParameterAssign.UNDEFINED) {
						bvgei.errors.add("Parameter " + (i + 1) + ": ASSIGN is UNDEFINED!");
						FlagParamOK = false;
					} else
						pi.assign = bp.assign;

					Integer intVal = getIntegerFromExpression(bp.columnPos);
					if (intVal == null) {
						bvgei.errors.add("Parameter " + (i + 1) + ": COLUMN_POS is incorrect!");
						FlagParamOK = false;
					} else
						pi.columnPos = intVal;

					intVal = getIntegerFromExpression(bp.rowPos);
					if (intVal == null) {
						bvgei.errors.add("Parameter " + (i + 1) + ": ROW_POS is incorrect!");
						FlagParamOK = false;
					} else
						pi.rowPos = intVal;

					pi.fixColumnPosToStartValue = bp.fixColumnPosToStartValue;
					pi.fixRowPosToStartValue = bp.fixRowPosToStartValue;					
					
					pi.syncExcelSheetColumnWithValuePos = bp.syncExcelSheetColumnWithValuePos;
					pi.syncExcelSheetRowWithValuePos = bp.syncExcelSheetRowWithValuePos;
					
					pi.dataInterpretation = bp.dataInterpretation;
					
					if (bp.mapping != null)
						pi.mapping = bp.mapping;

					String strUnit = getStringFromExpression(bp.unit);
					if (strUnit != null)
						pi.unit = strUnit;

					if (FlagParamOK) {
						// TODO some additional checks for the positions if
						// needed
					}

					if (FlagParamOK)
						bvgei.paramInfo.add(pi);
				}
			}

		if (bvg.endpointTypeString != null)
			bvgei.endpointTypeString = bvg.endpointTypeString;
		else {
			if (bvg.endpointType != null) {
				// TODO Following code could be replaced with function
				// extractParamInfo()
				boolean FlagParamOK = true;
				BlockParameter bp = bvg.endpointType;
				BlockValueGroupExtractedInfo.ParamInfo pi = new BlockValueGroupExtractedInfo.ParamInfo();

				if (bp.jsonValue != null)
					pi.jsonValue = bp.jsonValue;

				if (bp.assign == BlockParameterAssign.UNDEFINED) {
					bvgei.errors.add("Value group, ENDPOINT_TYPE section, ASSIGN is UNDEFINED!");
					FlagParamOK = false;
				} else
					pi.assign = bp.assign;

				Integer intVal = getIntegerFromExpression(bp.columnPos);
				if (intVal == null) {
					bvgei.errors.add("Value group, ENDPOINT_TYPE section, COLUMN_POS is incorrect!");
					FlagParamOK = false;
				} else
					pi.columnPos = intVal;

				intVal = getIntegerFromExpression(bp.rowPos);
				if (intVal == null) {
					bvgei.errors.add("Value group, ENDPOINT_TYPE section, ROW_POS is incorrect!");
					FlagParamOK = false;
				} else
					pi.rowPos = intVal;

				pi.fixColumnPosToStartValue = bp.fixColumnPosToStartValue;
				pi.fixRowPosToStartValue = bp.fixRowPosToStartValue;

				if (bp.mapping != null)
					pi.mapping = bp.mapping;

				String strUnit = getStringFromExpression(bp.unit);
				if (strUnit != null)
					pi.unit = strUnit;

				if (FlagParamOK) {
					// TODO some additional checks for the positions if
					// needed
				}

				if (FlagParamOK)
					bvgei.endpointType = pi;
			}
		}

		if (bvg.valueQualifierString != null)
			bvgei.valueQualifierString = bvg.valueQualifierString;
		else {
			if (bvg.valueQualifier != null) {
				ParamInfo pi = extractParamInfo(bvg.valueQualifier, bvgei.errors, "ENDPOINT_QUALIFIER");
				if (pi != null)
					bvgei.valueQualifier = pi;
			}
		}

		if (bvg.errorQualifierString != null)
			bvgei.errorQualifierString = bvg.errorQualifierString;
		else {
			if (bvg.errorQualifier != null) {
				ParamInfo pi = extractParamInfo(bvg.errorQualifier, bvgei.errors, "ERROR_QUALIFIER");
				if (pi != null)
					bvgei.errorQualifier = pi;
			}
		}
		
		if (bvg.substanceRecordMapString != null)
			bvgei.substanceRecordMapString = bvg.substanceRecordMapString;
		else {
			if (bvg.substanceRecordMap != null) {
				ParamInfo pi = extractParamInfo(bvg.substanceRecordMap, bvgei.errors, "SUBSTANCE_RECORD_MAP");
				if (pi != null)
					bvgei.substanceRecordMap = pi;
			}
		}

		return bvgei;
	}

	ParamInfo extractParamInfo(BlockParameter bp, List<String> errorOutput, String section) {
		ParamInfo pi = new ParamInfo();
		boolean FlagParamOK = true;

		if (bp.jsonValue != null)
			pi.jsonValue = bp.jsonValue;

		if (bp.assign == BlockParameterAssign.UNDEFINED) {
			errorOutput.add("Value group, " + section + " section, ASSIGN is UNDEFINED!");
			FlagParamOK = false;
		} else
			pi.assign = bp.assign;

		Integer intVal = getIntegerFromExpression(bp.columnPos);
		if (intVal == null) {
			errorOutput.add("Value group, " + section + " section, COLUMN_POS is incorrect!");
			FlagParamOK = false;
		} else
			pi.columnPos = intVal;

		intVal = getIntegerFromExpression(bp.rowPos);
		if (intVal == null) {
			errorOutput.add("Value group, " + section + " section, ROW_POS is incorrect!");
			FlagParamOK = false;
		} else
			pi.rowPos = intVal;

		pi.fixColumnPosToStartValue = bp.fixColumnPosToStartValue;
		pi.fixRowPosToStartValue = bp.fixRowPosToStartValue;
		
		pi.syncExcelSheetColumnWithValuePos = bp.syncExcelSheetColumnWithValuePos;
		pi.syncExcelSheetRowWithValuePos = bp.syncExcelSheetRowWithValuePos;
		
		if (bp.mapping != null)
			pi.mapping = bp.mapping;

		String strUnit = getStringFromExpression(bp.unit);
		if (strUnit != null)
			pi.unit = strUnit;

		if (FlagParamOK) {
			// TODO some additional checks for the positions if
			// needed

			return pi;
		}

		return null;
	}

	protected Integer getIntegerFromExpression(Object obj) {
		if (obj == null)
			return null;

		if (obj instanceof Integer)
			return (Integer) obj;

		if (obj instanceof String) {
			String s = (String) obj;
			if (s.startsWith("=")) {
				s = s.substring(1);
				try {
					Object res = evaluateExpression(s);
					if (res != null) {
						// logger.info("Expression result: " + res +
						// " class name " + res.getClass().getName());

						if (res instanceof Integer)
							return (Integer) res;

						if (res instanceof Double)
							return ((Double) res).intValue();

						if (res instanceof Long)
							return ((Long) res).intValue();

					}

				} catch (Exception e) {
					logger.warning("Expression error: " + e.getMessage());
				}
			} else {
				try {
					Integer res = Integer.parseInt(s);
					return res;
				} catch (Exception e) {
					logger.warning("Expression error: " + e.getMessage());
				}
			}
		}

		return null;
	}

	protected String getStringFromExpression(Object obj) {
		if (obj == null)
			return null;

		if (obj instanceof Number)
			return obj.toString();

		if (obj instanceof String) {
			String s = (String) obj;
			if (s.startsWith("=")) {
				s = s.substring(1);
				try {
					Object res = evaluateExpression(s);
					if (res != null) {
						if (res instanceof Number)
							return res.toString();

						if (res instanceof String)
							return res.toString();
					}

				} catch (Exception e) {
					logger.info("Expression error: " + e.getMessage());
				}
			} else
				return s;
		}

		return null;
	}

	protected Object evaluateExpression(String expression) throws Exception {
		JexlEngine jexl = getJexlEngine();
		Expression e = jexl.createExpression(expression);

		// Create context from the variables
		JexlContext variableContext = getContextFromVariables();

		Object result = e.evaluate(variableContext);
		return result;
	}

	protected JexlContext getContextFromVariables() {
		JexlContext context = new MapContext();
		Set<String> keys = curVariables.keySet();

		// logger.info("variables:");

		for (String key : keys) {
			context.set(key, curVariables.get(key));

			// Setting variables for the current iteration state
			context.set("ITERATION_CUR_ROW_NUM", new Integer(curRowNum));

			if ((config.substanceIteration == IterationAccess.ROW_MULTI_DYNAMIC)
					|| (config.substanceIteration == IterationAccess.ROW_MULTI_FIXED)) {
				context.set("ITERATION_CUR_ROW_LIST_SIZE", new Integer(curRows.size()));
			}
			
			context.set("CUR_ROW_SUBBLOCK", new Integer(curRowSubblock));
			context.set("CUR_COLUMN_SUBBLOCK", new Integer(curColumnSubblock));
			

			// System.out.println(" ***** ITERATION_CUR_ROW_NUM = " +
			// context.get("ITERATION_CUR_ROW_NUM"));

			/*
			 * //Logging the variables values Object v = curVariables.get(key);
			 * String s = ""; if (v instanceof Object[]) { Object v1[] =
			 * (Object[]) v; for (int i = 0; i < v1.length; i++) s += (" " +
			 * v1[i]); } else s = v.toString(); logger.info(key + " : " + s);
			 */
		}

		return context;
	}

	protected JexlEngine getJexlEngine() {
		if (jexlEngine == null) {
			jexlEngine = new JexlEngine();
			jexlEngine.setCache(512);
			jexlEngine.setLenient(false);
			jexlEngine.setSilent(false);
		}
		return jexlEngine;
	}


	
	protected Object getMappingValue(Object originalValue, String mapping) {
		HashMap<Object, Object> map = curVariableMappings.get(mapping);
		if (map == null)
			return null;
		// Original read value is used as a key to obtain the result value;		
		return map.get(originalValue);
	}
		
	
}
