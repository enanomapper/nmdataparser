nmdataparser
============

The **nmdataparser** Java library is a configurable parser allowing to importing spreadsheet substance composition, characterisation and assay data into the [eNanoMapper database](https://apps.ideaconsult.net/enanomapper), via   [[API]](http://enanomapper.github.io/API/#!/substance_1/uploadSubstance). The parser converts the spreadsheet into the internal [AMBIT](http://ambit.sf.net) data model, using a JSON file for mapping the objects.  The main class `GenericExcelParser`  iterates over entries of `*.xls` and `*.xlsx` files returning a set of `SubstanceRecords` objects. 

##### N. Jeliazkova, C. Chomenidis, P. Doganis, B. Fadeel, R. Grafström, B. Hardy, J. Hastings, M. Hegi, V. Jeliazkov, N. Kochev, P. Kohonen, C. R. Munteanu, H. Sarimveis, B. Smeets, P. Sopasakis, G. Tsiliki, D. Vorgrimmler, and E. Willighagen, The eNanoMapper database for nanomaterial safety information,Beilstein J. Nanotechnol., vol. 6, pp. 1609-1634, Jul. 2015. [doi:10.3762/bjnano.6.165](http://dx.doi.org/10.3762/bjnano.6.165)

### Command line application

Reads supported data formats (e.g. Excel + JSON configuration) and writes eNanoMapper RDF or ISA-JSON or JSON format.

* Download [enmconvertor-version.jar](https://nexus.ideaconsult.net/#nexus-search;gav~~enmconvertor~~jar~)

* [More details](http://ambit.sourceforge.net/enanomapper/templates/convertor.html) 

* [JSON configuration keywords](http://ambit.sourceforge.net/enanomapper/templates/jsonconfig.html)

### Data entry templates (NEW!)

[Nanomaterial characterisation and bioassay templates (based on NANoREG/JRC templates)](http://ambit.sourceforge.net/enanomapper/templates) 

### For developers

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

Development [snapshot at Maven repository](https://nexus.ideaconsult.net/#nexus-search;gav~~enmexcelparser~~jar~)
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



