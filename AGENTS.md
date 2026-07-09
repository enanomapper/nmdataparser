# AGENTS.md — nmdataparser

Guidance for coding agents (and new contributors) working in this repository.

## What this project is

**nmdataparser** is a configurable Java library + CLI that imports nanomaterial composition,
characterisation, and assay data from Excel spreadsheets into the eNanoMapper/AMBIT substance data
model. Given a spreadsheet (`.xls`/`.xlsx`) and a **JSON configuration file** (the map),
`GenericExcelParser` iterates the spreadsheet and returns `SubstanceRecord` objects, which the
`enmconvertor` CLI can serialize to AMBIT JSON, eNanoMapper RDF, or ISA-JSON.

Background paper: Kochev et al., *"Your Spreadsheets Can Be FAIR"*, Nanomaterials 2020, 10, 1908
(<https://doi.org/10.3390/nano10101908>).

## Repository map (Maven multi-module)

Parent POM: `net.enanomapper:enmdatamapper` (currently `1.4.1-SNAPSHOT`), `packaging=pom`, Java 8.

| Module | Purpose |
|--------|---------|
| **enmexcelparser** | Core parser library. Package `net.enanomapper.parser`. Contains `GenericExcelParser`, the config model, and the canonical keyword/enum definitions. This is where config semantics live. |
| **enmconvertor** | Command-line application (`net.enanomapper.parser.app`), the docs site (`src/site/markdown`), and ~126 example config templates (`src/site/resources/templates`). |
| **enmtemplategen** | Template generation (`net.idea.templates`, e.g. annotation tooling). |
| **enmtemplates** | Template resources + annotation apps (`net.enanomapper.templates.app`). |
| **enmnanowiki** | NanoWiki / eNanoMapper RDF reader (`net.idea.loom.nm.nanowiki`). Moved here from `vedina/loom`. |
| **enm2h5** | HDF5 export. |

## Build & test

Java 8 and Maven. Artifacts resolve from `nexus.ideaconsult.net` (see `pom.xml` repositories);
`ambit.version` is a `-SNAPSHOT`, so you need access to that Nexus or a local install of the AMBIT
artifacts.

```bash
mvn install                     # whole reactor
mvn -pl enmexcelparser install  # just the core parser
mvn -pl enmexcelparser test     # run the parser unit tests
```

Parser unit tests and their fixture configs live under
`enmexcelparser/src/test/resources/net/enanomapper/parser/testExcelParser/` — good, self-contained
examples (including `EFFECTS_BLOCK` configs in `testfile3-config.json` / `testfile4-config.json`).

## Entry points

- **Library:** `net.enanomapper.parser.GenericExcelParser` — construct with an Excel file + a JSON
  config, iterate `SubstanceRecord`s.
- **CLI:** `net.enanomapper.parser.app.DataConvertor` (the only production `main`). Build the
  `enmconvertor` jar or run the class directly. Options (`_OPTIONS.java`):

  | Option | Long | Meaning |
  |--------|------|---------|
  | `-i` | `--input` | Input file or folder |
  | `-I` | `--inputformat` | Input format (else inferred from extension) |
  | `-o` | `--output` | Output file or folder |
  | `-O` | `--outputformat` | Output format (else inferred from extension) |
  | `-x` | `--xconfig` | **JSON config file** (required for `xls`/`xlsx` input) |
  | `-c` | `--command` | Command (see below) |
  | `-s` | `--sheet` | Sheet number to process (all if omitted) |
  | `-t` | `--templateid` | Template identifier |
  | `-a` | `--annotation` | Path to annotation folder |
  | `-L` | `--listformats` | List supported read/write formats |
  | `-h` | `--help` | Help |

  Commands (`ConvertorCommand.java`): `data` (convert), `extracttemplatefields`,
  `generatejsonconfig` (skeleton config from a spreadsheet), `generatetemplate`.
  Formats (`IO_FORMAT.java`): read `xls`/`xlsx` (need `-x`), read/write `json` (AMBIT JSON), read
  `NWrdf`/`rdf`, write `isa`, `h5rest`, `report`, `xlsx_jrc`, `xlsx_multisheet`.

Example:

```bash
java -cp enmconvertor.jar net.enanomapper.parser.app.DataConvertor \
  -c data -i data.xlsx -x config.json -o out.json -O json
```

## Working on JSON configurations

If your task is to check or author a config, the schema authority is
**[docs/json-config-spec.md](docs/json-config-spec.md)**. It is derived from and must stay
consistent with:

- `enmexcelparser/src/main/java/net/enanomapper/parser/KEYWORD.java` — every valid JSON key.
- `enmexcelparser/src/main/java/net/enanomapper/parser/ParserConstants.java` — valid enum values
  (`IterationAccess`, `Recognition`, `DynamicIteration`, `BlockParameterAssign`) and the
  `ElementField` → `ObjectType` ownership (which field belongs to substance / composition /
  protocol / protocol-application / effect).
- `enmconvertor/src/site/markdown/jsonconfig.md` — prose reference.

Load-bearing rules that are easy to get wrong:

- **Indices in the JSON are 1-based**; the parser converts to 0-based internally
  (`ExcelParserConfigurator`). Columns may be Excel letters (`"AB"`) or 1-based integers.
- Omitted `ITERATION` / `SHEET_INDEX` inside an Excel Data Location **default from `DATA_ACCESS`**.
- A bare literal value is a `JSON_VALUE` (constant for every record).

If you change config keys or enum values in the Java source, update `docs/json-config-spec.md` and
`enmconvertor/src/site/markdown/jsonconfig.md` in the same change.

## Related implementations of the AMBIT data model

The config's *output* is the AMBIT/eNanoMapper substance model. Two other implementations of that
same model are useful cross-references when reasoning about what a config must produce:

- **AMBIT (Java, canonical)** — `../ambit-git` (Maven reactor `ambit2-all`, `ambit2-apps`). The
  server side: the `SubstanceRecord` classes this parser fills, the `SubstanceImport` used by the
  nanodata import tests, the MySQL schema, and the REST API (incl. the `admin/export/experiment`
  Solr export endpoint).
- **pyambit (Python, Pydantic)** — `../charisma/pyambit-main` (`src/pyambit/datamodel.py`). A typed,
  machine-checkable implementation of the same model: `SubstanceRecord`, `ProtocolApplication`,
  `EffectRecord`, `Composition`, `Citation`, etc., each with `model_dump_json`. `solr_writer.py`
  (`Ambit2Solr`) mirrors the Solr export. Handy for validating parser output or a generated config's
  target shape from Python.

## Conventions

- Keep Java 8 compatibility.
- Licenses: LGPL for code, CC-BY-SA 4.0 for the data templates under `enmconvertor` — keep new
  template files under the same terms.
- When adding example configs, prefer a real, parseable spreadsheet + config pair over a synthetic
  fragment, and place finalized templates in a named `json/` subfolder (drafts go under
  `jsondrafts/`).
