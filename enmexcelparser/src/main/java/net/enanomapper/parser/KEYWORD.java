package net.enanomapper.parser;

public enum KEYWORD {
	REPOSITORY,

	TEMPLATE_INFO, ITERATION, VERSION, TYPE, DATA_ACCESS, SKIP_ROWS, BASIC_ITERATION_LOAD_SUBSTANCE_RECORD, PARALLEL_SHEETS {
		@Override
		public boolean isArray() {
			return true;
		}
	},
	PARALLEL_SHEET, SHEET_INDEX {
		@Override
		public Boolean isInteger() {
			return true;
		}
	},
	SHEET_NAME, ROW_MULTI_FIXED_SIZE, START_ROW {
		public Boolean isInteger() {
			return true;
		}
	},
	END_ROW {
		public Boolean isInteger() {
			return true;
		}
	},
	START_HEADER_ROW {
		public Boolean isInteger() {
			return true;
		}
	},
	END_HEADER_ROW {
		public Boolean isInteger() {
			return true;
		}
	},
	ALLOW_EMPTY {
		@Override
		public Boolean isBoolean() {
			return true;
		}
	},
	RECOGNITION, DYNAMIC_ITERATION, DYNAMIC_ITERATION_COLUMN_INDEX, DYNAMIC_ITERATION_COLUMN_NAME, SYNCHRONIZATION, VARIABLES, VARIABLE_KEY, VARIABLE_KEYS {
		@Override
		public boolean isArray() {
			return true;
		}
	},
	KEYS_VARIABLE, VALUES_VARIABLE, VARIABLE_MAPPINGS {
		@Override
		public boolean isArray() {
			return true;
		}
	},
	DYNAMIC_ITERATION_SPAN, COLUMN_SPAN, ROW_SPAN, CLEAR_EMPTY_EFFECT_RECORDS,
	//
	SUBSTANCE_RECORD, CONTENT, FORMAT, COMPOSITION {
		@Override
		public boolean isArray() {
			return true;
		}
	},
	INCHI_KEY, INCHI, FORMULA, SMILES, PROPERTIES, PROPORTION, STRUCTURE_RELATION, SUBSTANCE_NAME, PUBLIC_NAME, EXTERNAL_IDENTIFIERS {
		@Override
		public boolean isArray() {
			return true;
		}
	},
	REFERENCE_SUBSTANCE_UUID, SUBSTANCE_UUID, OWNER_NAME, OWNER_UUID, SUBSTANCE_TYPE, FUNCTION {
		@Override
		public KEYWORD getParentSection() {
			return KEYWORD.PROPORTION;
		}
	},
	TYPICAL_PRECISION {
		@Override
		public KEYWORD getParentSection() {
			return KEYWORD.PROPORTION;

		}
	},
	TYPICAL_VALUE {

		@Override
		public KEYWORD getParentSection() {
			return KEYWORD.PROPORTION;
		}
	},
	TYPICAL_UNIT {
		@Override
		public KEYWORD getParentSection() {
			return KEYWORD.PROPORTION;
		}

	},
	REAL_VALUE {

		@Override
		public KEYWORD getParentSection() {
			return KEYWORD.PROPORTION;
		}

	},
	REAL_LOWER_PRECISION {

		@Override
		public KEYWORD getParentSection() {
			return KEYWORD.PROPORTION;
		}
	},
	REAL_LOWER_VALUE {

		@Override
		public KEYWORD getParentSection() {
			return KEYWORD.PROPORTION;
		}

	},
	REAL_UPPER_PRECISION {

		@Override
		public KEYWORD getParentSection() {
			return KEYWORD.PROPORTION;
		}
	},
	REAL_UPPER_VALUE {

		@Override
		public KEYWORD getParentSection() {
			return KEYWORD.PROPORTION;
		}

	},
	REAL_UNIT {

		@Override
		public KEYWORD getParentSection() {
			return KEYWORD.PROPORTION;
		}

	},
	//
	PROTOCOL_APPLICATIONS {

		@Override
		public boolean isArray() {
			return true;
		}

	},
	PROTOCOL_APPLICATION_UUID, INVESTIGATION_UUID, CITATION_TITLE, CITATION_YEAR, CITATION_OWNER, PROTOCOL_TOP_CATEGORY, PROTOCOL_CATEGORY_CODE, PROTOCOL_CATEGORY_TITLE, PROTOCOL_ENDPOINT, PROTOCOL_GUIDELINE, PARAMETERS, UNIT, NAME, JSON_VALUE, ASSIGN, COLUMN_POS, ROW_POS, MAPPING, FIX_COLUMN_POS_TO_START_VALUE, FIX_ROW_POS_TO_START_VALUE, LOCATION, ROW_SUBBLOCKS, COLUMN_SUBBLOCKS, SUBBLOCK_SIZE_ROWS, SUBBLOCK_SIZE_COLUMNS, VALUE_GROUPS {

		@Override
		public boolean isArray() {
			return true;
		}

	},
	SOURCE_COMBINATION, IS_ARRAY, TRIM_ARRAY, DATA_INTERPRETATION, DATE_FORMAT, COLUMN_INDEX, COLUMN_INDICES, COLUMN_NAME, ROW_INDEX, ROW_INDICES, ROW_NAME, JSON_REPOSITORY_KEY, RELIABILITY_IS_ROBUST_STUDY, RELIABILITY_IS_USED_FOR_CLASSIFICATION, RELIABILITY_IS_USED_FOR_MSDS, RELIABILITY_PURPOSE_FLAG, RELIABILITY_STUDY_RESULT_TYPE, RELIABILITY_VALUE, INTERPRETATION_RESULT, INTERPRETATION_CRITERIA, EFFECTS {

		@Override
		public boolean isArray() {
			return true;
		}

	},
	EFFECTS_BLOCK, CONDITIONS, REFERENCE, ADD_CONDITIONS_BY_REF {

		@Override
		public boolean isArray() {
			return true;
		}

	};

	public boolean isArray() {
		return false;
	};

	public Boolean isInteger() {
		return null;
	};

	public Boolean isBoolean() {
		return null;
	};

	public KEYWORD getParentSection() {
		return null;
	}

}
