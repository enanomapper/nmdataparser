# NMDataParser JSON configuration — specification for config authoring

This is a distilled, self-contained reference for the JSON configuration files that drive
[NMDataParser](../README.md). It is intended as the **ground truth an LLM (or a human curator)
loads before checking an existing config or generating a new one**.

The parser reads an Excel spreadsheet (`.xls`/`.xlsx`) plus one JSON config and maps the
spreadsheet layout onto the eNanoMapper/AMBIT substance data model, emitting `SubstanceRecord`
objects. The config *is* the map.

Authoritative sources this document is derived from (consult them when in doubt):

- `enmexcelparser/src/main/java/net/enanomapper/parser/KEYWORD.java` — the exhaustive list of
  valid JSON keys (and which are arrays / integers / booleans).
- `enmexcelparser/src/main/java/net/enanomapper/parser/ParserConstants.java` — the valid values
  for the enumerated keys (`ITERATION`, `RECOGNITION`, `DYNAMIC_ITERATION`, …) and the
  field→object ownership (`ElementField`).
- `enmconvertor/src/site/markdown/jsonconfig.md` — the prose reference.
- Real configs in the sibling `nanodata-<project>` data repos, e.g.
  `nanodata-gracious/casestudies/pigments/pchem.json` (used as the golden example below).

The config's **target** — the object shape the parser must produce — is the AMBIT/eNanoMapper
substance model. To reason about or validate that target you can also consult two other
implementations of the same model: the canonical Java (`ambit-git`, incl. `SubstanceRecord` and the
REST/DB), and the Python Pydantic **pyambit** (`charisma/pyambit-main/src/pyambit/datamodel.py` —
`SubstanceRecord`, `ProtocolApplication`, `EffectRecord`, `Composition`, …, all JSON-serializable via
`model_dump_json`). pyambit is convenient for programmatically validating a generated config's
output.

---

## 1. The two golden rules

1. **Indices are 1-based in the JSON.** Sheet, row, and column indices you write in the config
   are 1-based (user-friendly, matching what you see in Excel). The parser converts them to
   0-based internally (`ExcelParserConfigurator`). Do not write 0-based indices.
2. **Columns may be integers or Excel letters.** `"COLUMN_INDEX": "AN"` and `"COLUMN_INDEX": 40`
   are equivalent. Letters are usually clearer and are what most real configs use.
   `COLUMN_INDICES`/`ROW_INDICES` accept an array (`["B","C","D"]` or `[2,3,4]`) or a range
   string (`"2-4"`).

Two more principles that shape *how* you should write configs:

- **Defaulting from `DATA_ACCESS`.** Any `ITERATION` / `SHEET_INDEX` omitted inside an Excel Data
  Location is inherited from the `DATA_ACCESS` section (or the relevant `PARALLEL_SHEET`). In
  `ROW_SINGLE` mode this is why most locations are just `{ "COLUMN_INDEX": "X" }`.
- **Minimal harmonization.** Store values as close to the original spreadsheet as possible.
  Terminology alignment and ontology annotation happen later in the pipeline, not in the config.

---

## 2. Excel Data Location (EDL) — the core leaf object

Almost every mapped field is expressed as an **Excel Data Location**: a small object saying
*where* and *how* to read a value. The same shape is reused everywhere (substance fields,
protocol fields, effect values, conditions).

```jsonc
{
  "ITERATION": "ROW_SINGLE",     // optional; defaults from DATA_ACCESS
  "SHEET_INDEX": 7,               // optional; defaults from DATA_ACCESS
  "COLUMN_INDEX": "AB",           // column as letter or 1-based integer
  "ROW_INDEX": 3,                 // 1-based; used by ABSOLUTE_LOCATION / COLUMN_* modes
  "COLUMN_NAME": "...",           // used when RECOGNITION is BY_NAME
  "ROW_NAME": "...",
  "IS_ARRAY": true,               // default false; read many cells
  "COLUMN_INDICES": ["B","C","D"],// or [2,3,4] or "2-4"
  "ROW_INDICES": "2-4",
  "JSON_VALUE": "literal value",  // with ITERATION JSON_VALUE, value comes from here
  "DATA_INTERPRETATION": "AS_TEXT",// DEFAULT | AS_TEXT | AS_DATE | AS_VALUE_OR_TEXT
  "DATE_FORMAT": "yyyy-MM-dd",
  "SOURCE_COMBINATION": "...",     // combine several cells
  "COMBINATION_SEPARATOR": " "
}
```

**Short forms** (encouraged for readability):

- A bare `{ "COLUMN_INDEX": "X" }` in `ROW_SINGLE` mode (sheet + iteration inherited).
- A bare string / number **is** a `JSON_VALUE`: `"OWNER_NAME": "GRACIOUS"` means the literal
  value `GRACIOUS` for every record. `"SUBSTANCE_TYPE": "CHEBI_59999"` likewise.

### Iteration modes (`ITERATION`)

From `ParserConstants.IterationAccess`:

| Mode | Needs | Use when |
|------|-------|----------|
| `ROW_SINGLE` | column | Each spreadsheet **row** is one substance record (the common NANoREG/JRC layout). Only a column index is needed per field. |
| `ROW_MULTI_FIXED` | column | A **fixed** number of rows make one record. |
| `ROW_MULTI_DYNAMIC` | column | A **variable** number of rows make one record; group boundary set by `DYNAMIC_ITERATION`. |
| `COLUMN_SINGLE` | row | Each **column** is one record (transposed layout). |
| `COLUMN_MULTI_FIXED` | row | Fixed number of columns per record. |
| `COLUMN_MULTI_DYNAMIC` | row | Variable number of columns per record. |
| `ABSOLUTE_LOCATION` | column + row | A single fixed cell (e.g. a header/metadata value); all of sheet, row, column are used. |
| `JSON_VALUE` | — | The value is written directly in the config (a constant for every record). |
| `JSON_REPOSITORY` | — | The value comes from the `REPOSITORY` section. |
| `VARIABLE` | — | The value comes from a variable defined in `DATA_ACCESS.VARIABLES`. |
| `SUBSTANCE_RECORD_MAP` | — | Map-based substance-record assembly. |

---

## 3. Top-level sections

The config is a single JSON object with these first-level sections:

| Section | Type | Purpose |
|---------|------|---------|
| `TEMPLATE_INFO` | object | Metadata about the template. Keys: `NAME`, `VERSION`, `TYPE` (internal). |
| `DATA_ACCESS` | object | Default access + iteration for the **primary** sheet; supplies defaults to every EDL. |
| `PARALLEL_SHEETS` | array | Extra sheets read simultaneously with the primary one (same shape as `DATA_ACCESS`). |
| `SUBSTANCE_RECORD` | object | EDLs for the basic substance fields + `COMPOSITION`. |
| `PROTOCOL_APPLICATIONS` | array | One entry per protocol application (measurement group). |
| `REPOSITORY` | object | Preconfigured data (protocols, parameters) read via `JSON_REPOSITORY`. |

### 3.1 `DATA_ACCESS`

Keys (all optional except `ITERATION`):

- `ITERATION` — the default iteration mode (see table above).
- `SHEET_INDEX` (1-based int) / `SHEET_NAME` — the primary sheet (name used when `RECOGNITION` is `BY_NAME`).
- `START_ROW`, `END_ROW` — 1-based data-row bounds for iteration.
- `START_HEADER_ROW`, `END_HEADER_ROW` — header rows (not iterated; can be read via `ABSOLUTE_LOCATION`).
- `ALLOW_EMPTY` (bool, default `true`).
- `RECOGNITION` — `BY_INDEX` | `BY_NAME` | `BY_INDEX_AND_NAME`.
- `DYNAMIC_ITERATION` — for `ROW_MULTI_DYNAMIC`: `NEXT_NOT_EMPTY` | `NEXT_DIFFERENT_VALUE` | `ROW_LIST`.
- `DYNAMIC_ITERATION_COLUMN_INDEX` / `DYNAMIC_ITERATION_COLUMN_NAME` — the column whose value defines group boundaries.
- `VARIABLES` — object mapping a variable name to a literal or an EDL; reusable via `ITERATION: VARIABLE` and `MAPPING`.
- `VARIABLE_MAPPINGS` — array of `{ NAME, KEYS_VARIABLE, VALUES_VARIABLE }` lookup tables.

### 3.2 `SUBSTANCE_RECORD`

Each value is an EDL (or a literal short form). Fields (from `KEYWORD` / `ElementField`,
`ObjectType.SUBSTANCE`):

`PUBLIC_NAME`, `SUBSTANCE_NAME`, `OWNER_NAME`, `OWNER_UUID`, `SUBSTANCE_TYPE`,
`SUBSTANCE_UUID`, `REFERENCE_SUBSTANCE_UUID`, `EXTERNAL_IDENTIFIERS` (array of
`{ "TYPE": "...", "ID": <EDL> }`), `COMPOSITION` (array).

**`COMPOSITION[]`** — each entry describes one component:

- `STRUCTURE_RELATION` (EDL/literal) — role, e.g. `HAS_CORE`, `HAS_COATING`.
- `CONTENT` (EDL) and `FORMAT` (e.g. `"INC"`, `"SMILES"`, `"INCHI"`).
- `PROPERTIES` — object of component identifiers: `NAME`, `CASRN`, `EINECS`, `SMILES`,
  `INCHI`, `INCHI_KEY`, `FORMULA`, `TRADENAME`, …
- `PROPORTION` — object with `FUNCTION`, `REAL_LOWER_VALUE`, `REAL_UPPER_VALUE`,
  `REAL_LOWER_PRECISION`, `REAL_UPPER_PRECISION`, `REAL_UNIT`, `TYPICAL_VALUE`,
  `TYPICAL_PRECISION`, `TYPICAL_UNIT` (each an EDL or literal).

### 3.3 `PROTOCOL_APPLICATIONS[]`

Each element is one protocol application. Fields (mix of EDLs and literals):

- Identity / citation: `PROTOCOL_APPLICATION_UUID`, `INVESTIGATION_UUID`, `ASSAY_UUID`,
  `CITATION_TITLE`, `CITATION_YEAR`, `CITATION_OWNER`.
- Protocol classification: `PROTOCOL_TOP_CATEGORY` (e.g. `P-CHEM`, `TOX`, `ECOTOX`),
  `PROTOCOL_CATEGORY_CODE` (e.g. `PC_WATER_SOL_SECTION`), `PROTOCOL_CATEGORY_TITLE`,
  `PROTOCOL_ENDPOINT`, `PROTOCOL_GUIDELINE` (literal or `{ "guideline1": "..." }`).
- Interpretation: `INTERPRETATION_RESULT`, `INTERPRETATION_CRITERIA`.
- Reliability: `RELIABILITY_VALUE`, `RELIABILITY_IS_ROBUST_STUDY`,
  `RELIABILITY_IS_USED_FOR_CLASSIFICATION`, `RELIABILITY_IS_USED_FOR_MSDS`,
  `RELIABILITY_PURPOSE_FLAG`, `RELIABILITY_STUDY_RESULT_TYPE`.
- `PARAMETERS` — object mapping a parameter name (an experimental **condition / factor** such as
  `E.method`, `MEDIUM`, `pH`, `E.exposure_time`, `E.sop_reference`) to a literal or EDL.
- `EFFECTS` — array of effect records (below).
- `EFFECTS_BLOCK` — array of effect blocks (§4), for dense dose-response / HTS layouts.

**`EFFECTS[]`** — each is one measurement (from `ElementField`, `ObjectType.EFFECT`):

- `ENDPOINT`, `ENDPOINT_TYPE`, `SAMPLE_ID`, `UNIT`.
- Values: `VALUE` (rich value — sets lo/up/err + qualifiers + unit at once), or the granular
  `LO_VALUE`, `UP_VALUE`, `ERR_VALUE`, `TEXT_VALUE`, with `LO_QUALIFIER`, `UP_QUALIFIER`,
  `ERR_QUALIFIER` (e.g. `"SD"`).
- `CONDITIONS` — array of EDLs for per-measurement experimental conditions.

---

## 4. `EFFECTS_BLOCK` — dense measurement grids

For layouts where one sheet holds many measurements varying across several experimental factors
(the classic dose-response / HTS plate), enumerating each `EFFECTS` entry by hand is impractical.
`EFFECTS_BLOCK` reads them in bulk. Structure (keys from `KEYWORD.java`, consumed in
`ExcelParserConfigurator.java`):

```jsonc
"EFFECTS_BLOCK": [
  {
    "LOCATION": { "SHEET_INDEX": 2, "COLUMN_INDEX": "A" },   // block anchor
    "ROW_SUBBLOCKS": 1,
    "COLUMN_SUBBLOCKS": 1,
    "SUBBLOCK_SIZE_ROWS": "=ITERATION_CUR_ROW_LIST_SIZE",    // formulas allowed
    "SUBBLOCK_SIZE_COLUMNS": 10,
    "VALUE_GROUPS": [
      {
        "NAME": "=Assay_endpoint",       // may reference a DATA_ACCESS variable
        "START_COLUMN": 6, "END_COLUMN": 9,
        "START_ROW": 1, "END_ROW": "=ITERATION_CUR_ROW_LIST_SIZE",
        "ENDPOINT_TYPE": "RAW DATA",
        "PARAMETERS": [                   // conditions attached to each value in the group
          { "NAME": "Concentration", "ASSIGN": "ASSIGN_TO_VALUE",
            "FIX_COLUMN_POS_TO_START_VALUE": true, "COLUMN_POS": -1, "ROW_POS": 0,
            "UNIT": "ug/ml" }
        ]
      }
    ]
  }
]
```

A block is a grid of **sub-blocks**; each sub-block holds **value groups**; each value group is a
set of measurements, and its `PARAMETERS` (distinct from protocol-application `PARAMETERS`) are the
per-measurement conditions. `ASSIGN` values come from `BlockParameterAssign`:
`ASSIGN_TO_EXCEL_SHEET`, `ASSIGN_TO_BLOCK`, `ASSIGN_TO_SUBBLOCK`, `ASSIGN_TO_VALUE`.
Full worked block configs live in `enmexcelparser/src/test/resources/.../testExcelParser/testfile3-config.json` and `testfile4-config.json`.

---

## 5. Golden example (real config)

`nanodata-gracious/casestudies/pigments/pchem.json`, paired in
`projectdata-gracious/.../case_study/pigments.properties` by the line
`/nanodata-gracious/casestudies/pigments/Pigments4import.xlsx#1=pchem.json` (the `#1` selects
worksheet index 1).

The `pchem` sheet is iterated `ROW_SINGLE` — one substance per row (rows 5–24) — and the same row
feeds several protocol applications, each reading a different result column:

```jsonc
{
  "TEMPLATE_INFO": { "NAME": "pigments", "VERSION": "01", "TYPE": 1 },
  "DATA_ACCESS": {
    "ITERATION": "ROW_SINGLE", "SHEET_NAME": "pchem",
    "START_ROW": 5, "END_ROW": 24,
    "START_HEADER_ROW": 1, "END_HEADER_ROW": 2,
    "ALLOW_EMPTY": true, "RECOGNITION": "BY_INDEX"
  },
  "SUBSTANCE_RECORD": {
    "PUBLIC_NAME":   { "COLUMN_INDEX": "AN" },
    "OWNER_NAME":    "GRACIOUS",              // literal short form
    "SUBSTANCE_NAME":{ "COLUMN_INDEX": "AN" },
    "SUBSTANCE_TYPE":"CHEBI_59999"
  },
  "PROTOCOL_APPLICATIONS": [
    {
      "CITATION_TITLE": "CASE STUDY PIGMENTS", "CITATION_OWNER": "GRACIOUS", "CITATION_YEAR": "2020",
      "INVESTIGATION_UUID": "Pigments case study pchem",
      "ASSAY_UUID": "Pigments case study pchem - dissolution",
      "PROTOCOL_TOP_CATEGORY": "P-CHEM",
      "PROTOCOL_CATEGORY_CODE": "PC_WATER_SOL_SECTION",
      "PROTOCOL_ENDPOINT": "dynamic dissolution",
      "PROTOCOL_GUIDELINE": { "guideline1": "dynamic dissolution" },
      "PARAMETERS": { "E.method": "Dynamic dissolution" },
      "EFFECTS": [
        { "ENDPOINT": "HALF TIME",       "UNIT": "d",         "VALUE": { "COLUMN_INDEX": "AB" } },
        { "ENDPOINT": "DISSOLUTION RATE","UNIT": "ng/cm²/h","VALUE": { "COLUMN_INDEX": "AA" } }
      ]
    },
    {
      "PROTOCOL_TOP_CATEGORY": "P-CHEM",
      "PROTOCOL_CATEGORY_CODE": "RADICAL_FORMATION_POTENTIAL_SECTION",
      "PROTOCOL_ENDPOINT": "FRAS",
      "PARAMETERS": { "E.method": "FRAS" },
      "EFFECTS": [
        { "ENDPOINT": "REACTIVITY_SURFACE", "UNIT": "nmol TEU/m2",
          "VALUE": { "COLUMN_INDEX": "AD" },
          "ERR_VALUE": { "COLUMN_INDEX": "AM" }, "ERR_QUALIFIER": "SD" }
      ]
    }
    // … further protocol applications for EPR, BET, TEM, and dispersion-stability
    // (the last vary MEDIUM / pH / E.exposure_time in PARAMETERS and read columns AF–AI)
  ]
}
```

Things to copy from this pattern when authoring: literal short forms for constant fields
(`OWNER_NAME`, `SUBSTANCE_TYPE`), one `PROTOCOL_APPLICATIONS` entry per distinct method/endpoint,
`PARAMETERS` for the conditions (`MEDIUM`, `pH`, …), and `VALUE`/`ERR_VALUE`/`ERR_QUALIFIER`/`UNIT`
in each `EFFECTS` record.

A second, structurally different exemplar (physicochemical size/DLS with header-row
`ABSOLUTE_LOCATION` reads) is
`enmconvertor/src/site/resources/templates/PCHEM/SIZE/json/size_sheet_size_DLS.json`.

---

## 6. Validating parser output with pyambit (Python)

A generated config is only correct if the records it produces are valid AMBIT substances. The
Python **pyambit** model (`charisma/pyambit-main/src/pyambit/datamodel.py`) is a convenient,
machine-checkable way to assert that.

First convert a spreadsheet with your config to AMBIT JSON using the CLI:

```bash
java -cp enmconvertor.jar net.enanomapper.parser.app.DataConvertor \
  -c data -i data.xlsx -x config.json -o out.json -O json
```

Then load and validate it (Pydantic raises on any shape/type violation), and traverse it to check
the endpoints/values your config was meant to produce:

```python
import json
from pyambit.datamodel import Substances   # pip install pyambit  (or use charisma/pyambit-main)

with open("out.json", encoding="utf-8") as f:
    substances = Substances(**json.load(f))   # validates the whole document

for record in substances.substance:           # each SubstanceRecord
    for papp in record.study:                  # each ProtocolApplication
        print(papp.protocol, papp.parameters)  # endpoint / conditions
        for effect in papp.effects:            # each EffectRecord
            print(effect.endpoint, effect.conditions, effect.result)  # result holds value + unit

# round-trip check
data = json.loads(substances.model_dump_json())
```

If `Substances(**...)` raises, the config produced something the data model rejects — inspect the
Pydantic error, fix the config (or the config→model mapping), and re-run. This is a good acceptance
gate for an LLM-generated config: convert → load in pyambit → assert the expected substances,
protocol applications, endpoints, and units are present.

## 7. Checklist for validating / generating a config

- [ ] Every key used appears in `KEYWORD.java`; every enumerated value appears in
      `ParserConstants.java` (e.g. `ITERATION` value, `RECOGNITION`, `DYNAMIC_ITERATION`, `ASSIGN`).
- [ ] Each field sits under the right owner (substance field under `SUBSTANCE_RECORD`, effect field
      under `EFFECTS`, etc. — see `ElementField.isFieldOf`).
- [ ] Indices are **1-based**; columns are letters or 1-based ints; `*_INDICES` are arrays or ranges.
- [ ] `DATA_ACCESS.ITERATION`/`SHEET_INDEX`/`SHEET_NAME` are set once; per-field EDLs omit what they
      can inherit.
- [ ] `START_ROW`/`END_ROW` cover the real data rows; header rows are excluded from iteration.
- [ ] Values that are constant for every record use the literal short form, not an EDL.
- [ ] Dense measurement grids use `EFFECTS_BLOCK`, not hundreds of hand-written `EFFECTS`.
- [ ] The config is valid JSON (the parser also emits precise error messages naming the offending
      key — read them; they are the fastest way to converge).
