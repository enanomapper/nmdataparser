# Parser

eNanoMapper has developed [doi:10.3762/bjnano.6.165](http://www.beilstein-journals.org/bjnano/single/articleFullText.htm?publicId=2190-4286-6-165) a configurable Excel parser (https://github.com/enanomapper/nmdataparser), enabling import of NanoSafety Cluster Excel templates. The configuration metadata for the parser is defined in a separate file, mapping the custom spreadsheet structure into the internal eNanoMapper storage components.
eNanoMapper has developed a working prototype of a utility to export assay data from the eNanoMapper database into ISA-JSON format version 1. Where export to the older ISA-TAB format is needed, ISAtools can be used to convert both to and from the ISA-JSON to ISA-TAB formats.

Being able to parse diverse spreadsheets, as well as other input formats (such as OHT) into the same internal data model and export the data from this data model into different formats provides format converters, in the same fashion as OpenBabel (http://openbabel.org/) interconverts between chemical formats
