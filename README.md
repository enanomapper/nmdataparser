nmdataparser
============

The **nmdataparser** Java library is a configurable parser allowing to importing spreadsheet substance composition, characterisation and assay data into the [eNanoMapper database](https://apps.ideaconsult.net/enanomapper), via   [[API]](http://enanomapper.github.io/API/#!/substance_1/uploadSubstance). The parser converts the spreadsheet into the internal [AMBIT](http://ambit.sf.net) data model, using a JSON file for mapping the objects.  The main class `GenericExcelParser`  iterates over entries of `*.xls` and `*.xlsx` files returning a set of `SubstanceRecords` objects. 

#####N. Jeliazkova, C. Chomenidis, P. Doganis, B. Fadeel, R. Grafström, B. Hardy, J. Hastings, M. Hegi, V. Jeliazkov, N. Kochev, P. Kohonen, C. R. Munteanu, H. Sarimveis, B. Smeets, P. Sopasakis, G. Tsiliki, D. Vorgrimmler, and E. Willighagen, The eNanoMapper database for nanomaterial safety information,Beilstein J. Nanotechnol., vol. 6, pp. 1609-1634, Jul. 2015. [doi:10.3762/bjnano.6.165](http://dx.doi.org/10.3762/bjnano.6.165)

--

````
<dependency>
  <groupId>net.enanomapper</groupId>
  <artifactId>nmparser</artifactId>
  <version>1.0.2</version>
</dependency>
````
Stable release [![DOI](https://zenodo.org/badge/2503/enanomapper/nmdataparser.svg)](https://zenodo.org/badge/latestdoi/2503/enanomapper/nmdataparser)
````
<dependency>
  <groupId>net.enanomapper</groupId>
  <artifactId>nmparser</artifactId>
  <version>1.0.0</version>
</dependency>
````
Development snapshot at Maven repository http://nexus.ideaconsult.net/nexus/index.html#nexus-search;quick~enmexcelparser
````
<dependency>
  <groupId>net.enanomapper</groupId>
  <artifactId>enmexcelparser</artifactId>
  <version>1.0.3-SNAPSHOT</version>
</dependency>
````
##### Examples:
- [JSON configuration](https://github.com/enanomapper/nmdataparser/blob/master/src/test/resources/net/enanomapper/parser/csv/ProteinCoronaTest1.json)
- [Excel file](https://github.com/enanomapper/nmdataparser/blob/master/src/test/resources/net/enanomapper/parser/csv/ProteinCoronaTest1.xlsx)
- [More examples](https://github.com/enanomapper/nmdataparser/tree/master/src/test/resources/net/enanomapper/parser/csv)

#### Command line

Download https://nexus.ideaconsult.net/#nexus-search;gav~~enmconvertor~~jar~
Reads Excel file and JSON configuration and writes eNanoMapper RDF or ISA-JSON or JSON format.

````
java -jar enmconvertor.jar -i spreadsheet.xlsx -j spreadsheet.json -o test.rdf -f rdf
usage: enmconvertor-{version}
 -f,--outputformat <format>   json|isa|rdf
 -h,--help                    This help
 -i,--input <file>            Input file or folder
 -j,--json <file>             JSON config file
 -o,--output <file>           Output file
````

#### JSON Configuration

[JSON syntax documentation](doc/README.md)

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

