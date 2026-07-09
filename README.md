nmdataparser
============

The **nmdataparser** Java library is a configurable parser allowing to importing spreadsheet substance composition, characterisation and assay data into the [eNanoMapper database](https://apps.ideaconsult.net/enanomapper), via   [[API]](http://enanomapper.github.io/API/#!/substance_1/uploadSubstance). The parser converts the spreadsheet into the internal [AMBIT](http://ambit.sf.net) data model, using a JSON file for mapping the objects.  The main class `GenericExcelParser`  iterates over entries of `*.xls` and `*.xlsx` files returning a set of `SubstanceRecords` objects. 

### What it does

Nanosafety data is predominantly recorded in Excel spreadsheets whose layouts vary widely between
labs and projects. nmdataparser is the core of the eNanoMapper FAIRification workflow: it takes
**one spreadsheet plus one JSON configuration file** (the map that describes where each value lives
in the sheet) and produces `SubstanceRecord` objects in the eNanoMapper/AMBIT data model. Those
records can be imported into an eNanoMapper database or serialized to **AMBIT JSON, eNanoMapper RDF,
or ISA-JSON**. The JSON config decouples the messy, per-lab spreadsheet layout from the common
semantic model, so the same tool handles physicochemical, in-vitro, in-vivo, and eco-toxicology
data without code changes.

##### Kochev, N.; Jeliazkova, N.; Paskaleva, V.; Tancheva, G.; Iliev, L.; Ritchie, P.; Jeliazkov, V. Your Spreadsheets Can Be FAIR: A Tool and FAIRification Workflow for the ENanoMapper Database. Nanomaterials 2020, 10 (10), 1908. [10.3390/nano10101908](https://www.mdpi.com/2079-4991/10/10/1908)

##### N. Jeliazkova, C. Chomenidis, P. Doganis, B. Fadeel, R. Grafström, B. Hardy, J. Hastings, M. Hegi, V. Jeliazkov, N. Kochev, P. Kohonen, C. R. Munteanu, H. Sarimveis, B. Smeets, P. Sopasakis, G. Tsiliki, D. Vorgrimmler, and E. Willighagen, The eNanoMapper database for nanomaterial safety information,Beilstein J. Nanotechnol., vol. 6, pp. 1609-1634, Jul. 2015. [doi:10.3762/bjnano.6.165](http://dx.doi.org/10.3762/bjnano.6.165)

### Modules

This is an Apache Maven multi-module project (Java 8).

| Module | Purpose |
|--------|---------|
| `enmexcelparser` | Core parser library (`net.enanomapper.parser`, incl. `GenericExcelParser`) and the canonical config keyword/enum definitions. |
| `enmconvertor` | Command-line application (`net.enanomapper.parser.app.DataConvertor`), the documentation site, and example config templates. |
| `enmtemplategen` | Template generation and annotation tooling. |
| `enmtemplates` | Template resources and annotation apps. |
| `enmnanowiki` | NanoWiki / eNanoMapper RDF reader. |
| `enm2h5` | HDF5 export. |

### Build

```
mvn install
```

Requires Java 8; dependencies (including AMBIT `-SNAPSHOT` artifacts) resolve from
`nexus.ideaconsult.net`.

### Command line application

Reads supported data formats (e.g. Excel + JSON configuration) and writes eNanoMapper RDF or ISA-JSON or JSON format. The entry point is `net.enanomapper.parser.app.DataConvertor`.

```
java -cp enmconvertor.jar net.enanomapper.parser.app.DataConvertor \
  -c data -i data.xlsx -x config.json -o out.json -O json
```

Key options: `-i` input, `-I` input format, `-o` output, `-O` output format, `-x` **JSON config
file** (required for `xls`/`xlsx` input), `-c` command, `-s` sheet, `-L` list formats.
Commands (`-c`): `data`, `extracttemplatefields`, `generatejsonconfig` (skeleton config from a
spreadsheet), `generatetemplate`.

* Download [enmconvertor-version.jar](https://nexus.ideaconsult.net/#nexus-search;gav~~enmconvertor~~jar~)

* [More details](http://ambit.sourceforge.net/enanomapper/templates/convertor.html) 

* JSON configuration documentation: **[docs/json-config-spec.md](docs/json-config-spec.md)** (config-authoring reference), the in-repo [jsonconfig.md](enmconvertor/src/site/markdown/jsonconfig.md), and the [GitHub wiki](https://github.com/enanomapper/nmdataparser/wiki).

* Working with this repo as an agent or new contributor: see **[AGENTS.md](AGENTS.md)**.

### Data entry templates

[Template Wizard)](https://enanomapper.adma.ai/help/#templatewizard) 

### For developers

````
<dependency>
  <groupId>net.enanomapper</groupId>
  <artifactId>nmparser</artifactId>
  <version>1.4.1-SNAPSHOT</version>
</dependency>
````
Stable release [![DOI](https://zenodo.org/badge/2503/enanomapper/nmdataparser.svg)](https://zenodo.org/badge/latestdoi/2503/enanomapper/nmdataparser)
````
<dependency>
  <groupId>net.enanomapper</groupId>
  <artifactId>nmparser</artifactId>
  <version>1.3.1</version>
</dependency>
````

Development [snapshot at Maven repository](https://nexus.ideaconsult.net/#nexus-search;gav~~enmexcelparser~~jar~)
````
<dependency>
  <groupId>net.enanomapper</groupId>
  <artifactId>enmexcelparser</artifactId>
  <version>1.4.1-SNAPSHOT</version>
</dependency>
````
##### Documentation and examples:

- https://github.com/enanomapper/nmdataparser/wiki/Quick-start

### NanoWiki RDF parser

The NanoWiki RDF parser code was moved from https://github.com/vedina/loom  - please update your dependencies !

````
<dependency>
  <groupId>net.enanomapper</groupId>
  <artifactId>enmnanowiki</artifactId>
  <version>1.4.1-SNAPSHOT</version>
</dependency>
````

### Related projects

The JSON config maps a spreadsheet onto the AMBIT/eNanoMapper substance data model. Other
implementations of that same model:

- **[AMBIT](http://ambit.sf.net)** — the canonical Java server, REST API, and database that consumes
  the parser output (the `SubstanceImport` and `admin/export/experiment` Solr export used in the
  import pipeline).
- **[pyambit](https://github.com/ideaconsult/pyambit)** — a Python (Pydantic) implementation of the
  AMBIT data model (`SubstanceRecord`, `ProtocolApplication`, `EffectRecord`, `Composition`, …). Handy
  for programmatically validating parser output; see the worked example in
  [docs/json-config-spec.md](docs/json-config-spec.md).


