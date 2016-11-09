#Data convertor


````
usage: enmconvertor-{version}
 -h,--help                    This help
 -i,--input <file>            Input file or folder
 -I,--inputformat <format>    xls|xlsx|json|rdf|NWrdf
                              If not specified, recognised from input file
                              extension
 -L,--listformats             List supported formats
 -o,--output <file>           Output file or folder
 -O,--outputformat <format>   xls|xlsx|json|rdf|isa
                              If not specified, recognised from output
                              file extension
 -x,--xconfig <file>          JSON config file for input formats xls,xlsx

````

## Supported formats

````
java -Jar enmconvertor.jar -L

	(RW)	xls		Excel (.xls) spreadsheet, requires JSON configuration file (option -x)
	(RW)	xlsx	Excel (.xlsx) spreadsheet, requires JSON configuration file (option -x)
	(RW)	json	AMBIT JSON
	(RW)	rdf		eNanoMapper RDF (based on BioAssayOntology RDF)
	(W)		isa		ISA-JSON v1 (see https://github.com/ISA-tools/isa-api)
	(R)		NWrdf	NanoWiki RDF (Semantic Media Wiki RDF export)

````

### Nanosafety cluster Excel templates


### ISA-JSON export

ISA-JSON export software tools are available at: [ambit2-export](https://svn.code.sf.net/p/ambit/code/trunk/ambit2-all/ambit2-apps/ambit2-export/) package.

````
TODO maven dep 
````


### RDF export

