nmdataparser
============

**nmdataparser** library implements a configurable parser for importing spreadsheet data  into the eNanoMapper internal data base representation (Ambit database). The main class GenericExcelParser  iterates *.xls and *.xlsx file returning a set of SubstanceRecords objects. The class is configured by means of JSON file. 

##### Examples:
- [JSON configuration](https://github.com/enanomapper/nmdataparser/blob/master/src/test/resources/net/enanomapper/parser/csv/ProteinCoronaTest1.json)
- [Excel file](https://github.com/enanomapper/nmdataparser/blob/master/src/test/resources/net/enanomapper/parser/csv/ProteinCoronaTest1.xlsx)
- [More examples](https://github.com/enanomapper/nmdataparser/tree/master/src/test/resources/net/enanomapper/parser/csv)

#### JSON Configuration - Main sections
The JSON config file consists of several major sections (objects on the first level of the JSON schema):

**TEMPLATE_INFO** section defines basic info for the data format being imported. Intended mainly for internal usage. This section contains the options: **NAME**, **VERSION** and **TYPE**.

**DATA_ACCESS** section defines the basic access to the excel file

**PARALLEL_SHEETS** section is used to set up simultaneous access to several sheets of a given excel file.
 
**SUBSTANCE_RECORD** section defines the reading (data locations) of the basic fields of a substance record. 

**PROTOCOL_APPLICATIONS** section defines how to read an array of ProtocolApplication objects which are included in the SubtsanceRecord object defined in the previous section.

#### Excel Data Location
**Excel Data Location** is a key concept for the JSON configuration of excel parser reader. Excel data location is required for each data component of the Substance representation e.g. PUBLIC_NAME, CITATION_TITLE, ENDPOINT etc. must be defined by an excel data location.

Particular excel data location is defined by means of several options:
"ITERATION" field defines the iteration mode - how the data from this location is accessed. 
Following iteration modes are supported: 
**ROW_SINGLE** - data is accessed treating each excel table row as a separate data unit (e.g. Substance record),
**ROW_MULTI_FIXED** - a fixed number of rows are treated as a separate Substance record,
**ROW_MULTI_DYNAMIC** - a dynamic number of rows are used to load a Substance record (the number of rows may vary for each record),
**ABSOLUTE_LOCATION** - the data component is read from absolute location fro the excel file e.g. sheet, row and column must be defined
**JSON_VALUE** - the data component is taken directly from the JSON config file
**JSON_REPOSITORY** - the data component is taken directly from the JSON config file but special section REPOSOTORY is used.

Fields: "COLUMN_INDEX" "COLUMN_INDEX" and "COLUMN_INDEX" define the index of the column 

#### DATA_ACCESS JSON options

Section **DATA_ACCESS**	defines the basic parameters for data access and iteration of the primary sheet.

**ITERATION**	Defines the iteration mode. Possible iteration modes are:
ROW_SINGLE, ROW_MULTI_FIXED, ROW_MULTI_DYNAMIC, ABSOLUTE_LOCATION, JSON_VALUE, JSON_REPOSITORY, VARIABLE
**SHEET_INDEX**	The primary sheet for iteration
**SHEET_NAME**	The primary sheet name
**START_ROW**	The starting row for iteration
**START_HEADER_ROW**	The first (starting) header row
**END_HEADER_ROW**	The last (ending) header row
**ALLOW_EMPTY**	Flag that defines whether empty cells are allowed. Default value is true 
**RECOGNITION**	The mode for sheet/column/row recognition. These elements can be recognized by index or by name.
**DYNAMIC_ITERATION**	Defines how dynamic iteration is performed in mode ROW_MULTI_DYNAMIC. Several rows are read at once where the criterion for row group recognition is: NEXT_NOT_EMPTY or NEXT_DIFFERENT_VALUE.
**DYNAMIC_ITERATION_COLUMN_INDEX**	The column used for the dynamic iteration.
VARIABLES	Defines an array of excel locations that are read into work variables stored for later used if the reading process

#### PARALLEL_SHEETS JSON options

**PARALLEL_SHEETS [ ]**	This is an array of sections similar to section DATA_ACCESS that define the simultaneous reading of several sheets together with the primary sheet. 


#### SUBSTANCE_RECORD JSON options

**SUBSTANCE_RECORD**	Section that defines the excel locations for reading of the basic fields of a Substance Record: COMPANY_NAME, OWNER_NAME, SUBSTANCE_TYPE, OWNER_UUID, COMPANY_UUID, PUBLIC_NAME, ID_SUBSTANCE, COMPOSITION
 
#### PROTOCOL_APPLICATIONS JSON options

**PROTOCOL_APPLICATIONS [ ]**	This is an array of sections , defining the excel data locations for reading of Protocol Application data. Each section includes following fields:  CITATION_TITLE, CITATION_YEAR, CITATION_OWNER,  INTERPRETATION_RESULT, INTERPRETATION_CRITERIA, PROTOCOL_GUIDELINE, PARAMETERS (an array of data locations), EFFECTS (an array of sections)
**EFFECTS [ ]**	This is an array of sections. Each section defines data structures (effect record) for particular measurements and includes following excel data locations: SAMPLE_ID, ENDPOINT, LO_VALUE, UP_VALUE, ERR_VALUE, TEXT_VALUE, VALUE, LO_QUALIFIER, UP_QUALIFIER, ERR_QUALIFIER, UNIT, CONDITIONS (an array of data locations)
REPOSITORY	A JSON structure for defining preconfigured data (e.g. protocol, parameters) to be read directly from the JSON file into the data classes. 

--


####Data model JAVADOC 

#####Java classes for describing Substances

* [Substance](http://ambit.uni-plovdiv.bg/downloads/ambit2/2.7.0-SNAPSHOT/apidocs/ambit2/base/data/SubstanceRecord.html)

* [Substance composition](http://ambit.uni-plovdiv.bg/downloads/ambit2/2.7.0-SNAPSHOT/apidocs/ambit2/base/relation/composition/CompositionRelation.html)

#####[Java classes](http://ambit.uni-plovdiv.bg/downloads/ambit2/2.7.0-SNAPSHOT/apidocs/ambit2/base/data/study/package-summary.html) for describing measurements

* [Protocol](http://ambit.uni-plovdiv.bg/downloads/ambit2/2.7.0-SNAPSHOT/apidocs/ambit2/base/data/study/Protocol.html)

* [Protocol parameters or conditions](http://ambit.uni-plovdiv.bg/downloads/ambit2/2.7.0-SNAPSHOT/apidocs/index.html?ambit2/base/data/study/Params.html) 

* [Protocol application](http://ambit.uni-plovdiv.bg/downloads/ambit2/2.7.0-SNAPSHOT/apidocs/ambit2/base/data/study/ProtocolApplication.html)

* [EffectRecord](http://ambit.uni-plovdiv.bg/downloads/ambit2/2.7.0-SNAPSHOT/apidocs/ambit2/base/data/study/EffectRecord.html)

* [Value](http://ambit.uni-plovdiv.bg/downloads/ambit2/2.7.0-SNAPSHOT/apidocs/ambit2/base/data/study/Value.html)

* ![Java class diagram](ambit2.base.data.study.gif "Java class diagram")


--

 The parser enables import into http://data.enanomapper.net   [[API]](http://enanomapper.github.io/API/#!/substance_1/uploadSubstance)
 
--
Available at Maven repository http://ambit.uni-plovdiv.bg:8083/nexus/index.html#nexus-search;quick~i5z

Stable release
````
<dependency>
  <groupId>net.enanomapper</groupId>
  <artifactId>nmparser</artifactId>
  <version>0.0.1</version>
</dependency>
````

Development snapshot
````
<dependency>
  <groupId>net.enanomapper</groupId>
  <artifactId>nmparser</artifactId>
  <version>0.0.2-SNAPSHOT</version>
</dependency>
````
