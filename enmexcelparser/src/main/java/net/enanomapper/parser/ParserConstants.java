package net.enanomapper.parser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class ParserConstants {
	public enum IterationAccess {
		ROW_SINGLE {
			@Override
			public boolean isColumnInfoRequired() {
				return true;
			}
		},

		ROW_MULTI_FIXED {
			@Override
			public boolean isColumnInfoRequired() {
				return true;
			}
		},

		ROW_MULTI_DYNAMIC {
			@Override
			public boolean isColumnInfoRequired() {
				return true;
			}
		},

		COLUMN_SINGLE {
			@Override
			public boolean isRowInfoRequired() {
				return true;
			}
		},

		COLUMN_MULTI_FIXED {
			@Override
			public boolean isRowInfoRequired() {
				return true;
			}
		},

		COLUMN_MULTI_DYNAMIC {
			@Override
			public boolean isRowInfoRequired() {
				return true;
			}
		},

		ABSOLUTE_LOCATION {
			@Override
			public boolean isColumnInfoRequired() {
				return true;
			}

			@Override
			public boolean isRowInfoRequired() {
				return true;
			}
		},

		JSON_VALUE, JSON_REPOSITORY, VARIABLE, UNDEFINED;

		public static IterationAccess fromString(String s) {
			try {
				IterationAccess access = IterationAccess.valueOf(s);
				return (access);
			} catch (Exception e) {
				return IterationAccess.UNDEFINED;
			}
		}

		public boolean isColumnInfoRequired() {
			return false; // default value is overridden for some cases
		}

		public boolean isRowInfoRequired() {
			return false; // default value is overridden for some cases
		}
	}

	public enum DataType {
		CELL, ROW, COLUMN, BLOCK, UNDEFINED;

		public static DataType fromString(String s) {
			try {
				DataType type = DataType.valueOf(s);
				return (type);
			} catch (Exception e) {
				return DataType.UNDEFINED;
			}
		}
	}

	public enum DataInterpretation {
		DEFAULT, AS_TEXT, AS_DATE, AS_VALUE_OR_TEXT, UNDEFINED;

		public static DataInterpretation fromString(String s) {
			try {
				DataInterpretation di = DataInterpretation.valueOf(s);
				return (di);
			} catch (Exception e) {
				return DataInterpretation.UNDEFINED;
			}
		}
	}

	public enum CellType_ENM {
		BLANK, BOOLEAN, ERROR, FORMULA, NUMERIC, STRING, UNDEFINED;

		public static CellType_ENM fromString(String s) {
			try {
				CellType_ENM type = CellType_ENM.valueOf(s);
				return (type);
			} catch (Exception e) {
				return CellType_ENM.UNDEFINED;
			}
		}

		public static CellType_ENM fromPOICelType(CellType poiCellType) {
			switch (poiCellType) {
			case BLANK:
				return BLANK;
			case BOOLEAN:
				return BOOLEAN;
			case ERROR:
				return ERROR;
			case FORMULA:
				return FORMULA;
			case NUMERIC:
				return BLANK;
			case STRING:
				return BLANK;
			default:
				return UNDEFINED;	
			}
			
		}

		public static CellType toPOICellType(CellType_ENM type) {
			switch (type) {
			case BLANK:
				return CellType.BLANK;
			case BOOLEAN:
				return CellType.BOOLEAN;
			case ERROR:
				return CellType.ERROR;
			case FORMULA:
				return CellType.FORMULA;
			case NUMERIC:
				return CellType.NUMERIC;
			case STRING:
				return CellType.STRING;
			case UNDEFINED:
				return CellType.STRING;
			default:
				return CellType.BLANK;
			}
		}
	}

	public enum Recognition {
		BY_INDEX, BY_NAME, BY_INDEX_AND_NAME, UNDEFINED;

		public static Recognition fromString(String s) {
			try {
				Recognition rec = Recognition.valueOf(s);
				return (rec);
			} catch (Exception e) {
				return Recognition.UNDEFINED;
			}
		}
	}

	public enum DynamicIteration {
		NEXT_NOT_EMPTY, NEXT_DIFFERENT_VALUE, ROW_LIST, UNDEFINED;

		public static DynamicIteration fromString(String s) {
			try {
				DynamicIteration di = DynamicIteration.valueOf(s);
				return (di);
			} catch (Exception e) {
				return DynamicIteration.UNDEFINED;
			}
		}
	}

	public enum SheetSynchronization {
		NONE, MATCH_KEY, UNDEFINED;

		public static SheetSynchronization fromString(String s) {
			try {
				SheetSynchronization synch = SheetSynchronization.valueOf(s);
				return (synch);
			} catch (Exception e) {
				return SheetSynchronization.UNDEFINED;
			}
		}
	}

	/*
	 * Determines how the Element information is taken from the dynamic span
	 * rows/columns
	 */
	public enum ElementPosition {
		ANY_ROW, FIRST_ROW, NON_FIRST_ROW, EACH_FROM_FIRST_ROW, ANY_GROUP_ROW, FIRST_GROUP_ROW, NON_FIRST_GROUP_ROW, EACH_FROM_FIRST_GROUP_ROW, FIRST_GROUP_ROW_FROM_FIRST_ROW, UNDEFINED;

		public static ElementPosition fromString(String s) {
			try {
				ElementPosition pos = ElementPosition.valueOf(s);
				return (pos);
			} catch (Exception e) {
				return ElementPosition.UNDEFINED;
			}
		}
	}

	/*
	 * Determines how the current element/row/group/... is
	 * synchronized/assembled into larger elements (i.e. it is a sort of
	 * destination).
	 */
	public enum ElementSynchronization {
		NONE, PUT_IN_ELEMENT, PUT_IN_ROW, PUT_IN_GROUP, PUT_IN_CUMULATIVE_OBJECT, PUT_IN_EACH_ROW, PUT_IN_EACH_GROUP, PUT_IN_EACH_CUMULATIVE_OBJECT, PUT_IN_PRIMARY_CUMULATIVE_OBJECT, UNDEFINED;

		public static ElementSynchronization fromString(String s) {
			try {
				ElementSynchronization synch = ElementSynchronization.valueOf(s);
				return (synch);
			} catch (Exception e) {
				return ElementSynchronization.UNDEFINED;
			}
		}

	}

	public enum ObjectType {

		NONE, EFFECT, EFFECT_ARRAY, COMPOSITION, COMPOSITION_ARRAY, PROTOCOL, PROTOCOL_ARRAY, PROTOCOL_APPLICATION, PROTOCOL_APPLICATION_ARRAY, SUBSTANCE, SUBSTANCE_ARRAY, VARIABLE, VARIABLE_CONTAINER, UNDEFINED;

		public static ObjectType fromString(String s) {
			try {
				ObjectType type = ObjectType.valueOf(s);
				return (type);
			} catch (Exception e) {
				return ObjectType.UNDEFINED;
			}
		}

		public boolean isElementOf(ObjectType type) {
			// TODO improve it
			return (this.ordinal() < type.ordinal());
		}
	};

	public enum ElementField {
		NONE,

		// Substance record fields
		COMPANY_NAME {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.SUBSTANCE)
					return true;
				else
					return false;
			}
		},

		COMPANY_UUID {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.SUBSTANCE)
					return true;
				else
					return false;
			}
		},

		OWNER_NAME {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.SUBSTANCE)
					return true;
				else
					return false;
			}
		},

		OWNER_UUID {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.SUBSTANCE)
					return true;
				else
					return false;
			}
		},

		SUBSTANCE_TYPE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.SUBSTANCE)
					return true;
				else
					return false;
			}
		},

		PUBLIC_NAME {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.SUBSTANCE)
					return true;
				else
					return false;
			}
		},

		ID_SUBSTANCE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.SUBSTANCE)
					return true;
				else
					return false;
			}
		},

		EXTERNAL_IDENTIFIER {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.SUBSTANCE)
					return true;
				else
					return false;
			}
		},

		// Protocol fields
		PROTOCOL_TOP_CATEGORY {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL)
					return true;
				else
					return false;
			}
		},

		PROTOCOL_CATEGORY_CODE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL)
					return true;
				else
					return false;
			}
		},

		PROTOCOL_CATEGORY_TITLE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL)
					return true;
				else
					return false;
			}
		},

		PROTOCOL_GUIDELINE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL)
					return true;
				else
					return false;
			}
		},

		// Protocol application fields
		CITATION_TITLE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL_APPLICATION)
					return true;
				else
					return false;
			}
		},

		CITATION_YEAR {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL_APPLICATION)
					return true;
				else
					return false;
			}
		},

		CITATION_OWNER {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL_APPLICATION)
					return true;
				else
					return false;
			}
		},

		PARAMETER {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL_APPLICATION)
					return true;
				else
					return false;
			}
		},

		RELIABILITY_IS_ROBUST_STUDY {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL_APPLICATION)
					return true;
				else
					return false;
			}
		},

		RELIABILITY_IS_USED_FOR_CLASSIFICATION {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL_APPLICATION)
					return true;
				else
					return false;
			}
		},

		RELIABILITY_IS_USED_FOR_MSDS {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL_APPLICATION)
					return true;
				else
					return false;
			}
		},

		RELIABILITY_PURPOSE_FLAG {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL_APPLICATION)
					return true;
				else
					return false;
			}
		},

		RELIABILITY_STUDY_RESULT_TYPE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL_APPLICATION)
					return true;
				else
					return false;
			}
		},

		RELIABILITY_VALUE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL_APPLICATION)
					return true;
				else
					return false;
			}
		},

		INTERPRETATION_RESULT {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL_APPLICATION)
					return true;
				else
					return false;
			}
		},

		INTERPRETATION_CRITERIA {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.PROTOCOL_APPLICATION)
					return true;
				else
					return false;
			}
		},

		// Effect fields
		ENDPOINT {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},
		
		ENDPOINT_TYPE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},

		SAMPLE_ID {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},

		UNIT {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},

		LO_VALUE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},

		LO_QUALIFIER {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},

		UP_VALUE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},

		UP_QUALIFIER {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},

		TEXT_VALUE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},

		ERR_VALUE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},

		ERR_QUALIFIER {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},

		VALUE { // Used for rich value approach (setting lo/up/err + qualifiers
				// + unit at once)
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},

		CONDITION {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.EFFECT)
					return true;
				else
					return false;
			}
		},

		/*
		 * COMPOSITION_STR_TYPE {
		 * 
		 * @Override public boolean isFieldOf(ObjectType type) { if (type ==
		 * ObjectType.SUBSTANCE) return true; else return false; } },
		 * 
		 * COMPOSITION_STR_INFO {
		 * 
		 * @Override public boolean isFieldOf(ObjectType type) { if (type ==
		 * ObjectType.SUBSTANCE) return true; else return false; } },
		 * 
		 * COMPOSITION_RELATION {
		 * 
		 * @Override public boolean isFieldOf(ObjectType type) { if (type ==
		 * ObjectType.SUBSTANCE) return true; else return false; } },
		 */

		// Composition fields
		STRUCTURE_RELATION {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		CONTENT {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		FORMAT {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		INCHI_KEY {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		INCHI {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		FORMULA {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		SMILES {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		PROPERTY {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		PROPORTION_FUNCTION {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		PROPORTION_TYPICAL_PRECISION {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		PROPORTION_TYPICAL_VALUE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		PROPORTION_TYPICAL_UNIT {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		/*
		 * PROPORTION_REAL_VALUE {
		 * 
		 * @Override public boolean isFieldOf(ElementDataType type) { if (type
		 * == ElementDataType.COMPOSITION) return true; else return false; } },
		 */

		PROPORTION_REAL_LOWER_PRECISION {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		PROPORTION_REAL_LOWER_VALUE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		PROPORTION_REAL_UPPER_PRECISION {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		PROPORTION_REAL_UPPER_VALUE {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		PROPORTION_REAL_UNIT {
			@Override
			public boolean isFieldOf(ObjectType type) {
				if (type == ObjectType.COMPOSITION)
					return true;
				else
					return false;
			}
		},

		UNDEFINED;

		public boolean isFieldOf(ObjectType type) {
			return false;
		}

		public ObjectType getObjectType() {
			if (this.isFieldOf(ObjectType.SUBSTANCE))
				return ObjectType.SUBSTANCE;

			if (this.isFieldOf(ObjectType.PROTOCOL_APPLICATION))
				return ObjectType.PROTOCOL_APPLICATION;

			if (this.isFieldOf(ObjectType.PROTOCOL))
				return ObjectType.PROTOCOL;

			if (this.isFieldOf(ObjectType.EFFECT))
				return ObjectType.EFFECT;

			if (this.isFieldOf(ObjectType.COMPOSITION))
				return ObjectType.COMPOSITION;

			return ObjectType.NONE;
		}

		public static ElementField fromString(String s) {
			try {
				ElementField type = ElementField.valueOf(s);
				return (type);
			} catch (Exception e) {
				return ElementField.UNDEFINED;
			}
		}
	};

	public enum BlockParameterAssign {
		ASSIGN_TO_BLOCK, ASSIGN_TO_SUBBLOCK, ASSIGN_TO_VALUE, UNDEFINED;

		public static BlockParameterAssign fromString(String s) {
			try {
				BlockParameterAssign type = BlockParameterAssign.valueOf(s);
				return (type);
			} catch (Exception e) {
				return BlockParameterAssign.UNDEFINED;
			}
		}
	}

}
