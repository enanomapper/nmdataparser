#Data convertor

A command line application for converting between supported data formats with nanomaterial safety data.


* Download [https://github.com/enanomapper/nmdataparser](https://github.com/enanomapper/nmdataparser)

* Usage

````
java -jar enmconvertor.jar -h

usage: enmconvertor
 -h,--help                    This help
 -i,--input <file>            Input file or folder
 -I,--inputformat <format>    xls|xlsx|json|rdf|NWrdf
                              If not specified, recognised from the input file extension
 -L,--listformats             List supported formats
 -o,--output <file>           Output file or folder
 -O,--outputformat <format>   xls|xlsx|json|rdf|isa
                              If not specified, recognised from the output file extension
 -x,--xconfig <file>          JSON config file for input formats xls,xlsx

````

Excel spreadsheets import requires a separate JSON configuration file. More details about the [Excel parser](parser.html). 
Predefined JSON configuration files for the [NANoREG templates](./index.html) are provided next to each Excel file, e.g. [INVITRO/GENOTOXICITY/COMET](https://github.com/enanomapper/nmdataparser/tree/master/enmconvertor/src/site/resources/templates/INVITRO/GENOTOXICITY/COMET).   

* Supported formats

````
java -jar enmconvertor.jar -L

	(RW)	xls		Excel (.xls) spreadsheet, requires JSON configuration file (option -x)
	(RW)	xlsx	Excel (.xlsx) spreadsheet, requires JSON configuration file (option -x)
	(RW)	json	AMBIT JSON
	(RW)	rdf		eNanoMapper RDF (based on BioAssayOntology RDF)
	(W)		isa		ISA-JSON v1 (see https://github.com/ISA-tools/isa-api)
	(R)		NWrdf	NanoWiki RDF (Semantic Media Wiki RDF export)

````

## Examples

### Converting NanoSafety Cluster Excel spreadsheets

* Example `xlsx` file [INVITRO_VIABILITY_Trypanblue_TEST.xlsx](./examples/INVITRO_VIABILITY_Trypanblue_TEST.xlsx)

* Example `json configuration` file [INVITRO_VIABILITY_Trypanblue.json](./templates/INVITRO/VIABILITY/TrypanBlue/INVITRO_VIABILITY_TrypanBlue.json)  


#### to ISA-JSONv1
 
````
java -jar enmconvertor.jar -i "INVITRO_VIABILITY_Trypanblue_TEST.xlsx" -j "INVITRO_VIABILITY_Trypanblue.json" -I xlsx -O isa -o "INVITRO_VIABILITY_Trypan blue_TEST.isa.json"
````

Result file (zipped) [INVITRO_VIABILITY_Trypanblue_TEST.isa.json.zip](./examples/INVITRO_VIABILITY_Trypanblue_TEST.isa.json.zip)

#### to eNanoMapper [RDF](https://www.w3.org/RDF/) (N3 syntax)

````
java -jar enmconvertor.jar -i "INVITRO_VIABILITY_Trypanblue_TEST.xlsx" -j "INVITRO_VIABILITY_Trypanblue.json" -I xlsx -O isa -o "INVITRO_VIABILITY_Trypanblue_TEST.enm.n3"
````

Result file (zipped) [INVITRO_VIABILITY_Trypanblue_TEST.enm.n3.zip](./examples/INVITRO_VIABILITY_Trypanblue_TEST.enm.n3.zip)

### Converting [NanoWiki release4](https://figshare.com/articles/NanoWiki_4/4141593)

#### to ISA-JSONv1
 
````
java -jar enmconvertor.jar -i nanowiki.cczero.4.rdf.gz -I NWrdf -O isa -o nanowiki.cczero.4.isa.json
````

Result file (zipped) [nanowiki.cczero.4.isa.json.zip](./examples/nanowiki.cczero.4.isa.json.zip)

#### to enanomapper [RDF](https://www.w3.org/RDF/) (N3 syntax)

````
java -jar enmconvertor.jar -i nanowiki.cczero.4.rdf.gz -I NWrdf -O rdf -o nanowiki.cczero.4.enm.n3
````

Result file (zipped) [nanowiki.cczero.4.enm.n3.zip](./examples/nanowiki.cczero.4.enm.n3.zip)


## For developers

* ISA-JSON export software tools are available at: [ambit2-export](https://svn.code.sf.net/p/ambit/code/trunk/ambit2-all/ambit2-apps/ambit2-export/) package.
 
````
<dependency>
  <groupId>ambit</groupId>
  <artifactId>ambit2-export</artifactId>
  <version>3.0.3-SNAPSHOT</version>
</dependency>
````

* [Excel parser](parser.html)

````
<dependency>
  <groupId>net.enanomapper</groupId>
  <artifactId>nmparser</artifactId>
  <version>1.0.3-SNAPSHOT</version>
</dependency>
````

Maven repositories

````
<repository>
	<snapshots>
		<enabled>false</enabled>
	</snapshots>
		<id>nexus-idea-releases</id>
		<name>nexus-idea-releases</name>
		<url>https://nexus.ideaconsult.net/content/repositories/releases</url>
</repository>
<repository>
	<releases>
		<enabled>false</enabled>
	</releases>
	<id>nexus-idea-snapshots</id>
	<name>nexus-idea-snapshots</name>
	<url>https://nexus.ideaconsult.net/content/repositories/snapshots</url>
</repository>
````