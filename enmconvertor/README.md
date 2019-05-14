#  Data convertor


A command line application for processing Excel based substance safety data. 

- Usage 

```
INFO   enmparser build:${buildNumber} ${timestamp}
${url}
usage: enmconvertor-{version}
 -c,--command
 <data|extracttemplatefields|generatejsonconfig|generatetemplate|>   The  type of converted content
 -h,--help  This help
 -i,--input <file>
 [data]: Input file or folder
 [extracttemplatefields]: Root spreadsheet folder as used in .properties
 [generatejsonconfig]: Input spreadsheet
 [generatetemplate]: Input file or folder 
 -I,--inputformat <format>  xls|xlsx|json|NWrdf|rdf  If not specified, recognised from input file extension
 -L,--listformats  List supported formats
 -o,--output <file>  Output file or folder
 -O,--outputformat <format>  |json|rdf|isa|report  If not specified, recognised from output file extension
 -s,--sheet <sheet>  Sheet number to be processed. All sheets if missing
 -x,--xconfig <file>  [data]: JSON config file for input formats xls,xlsx
 [extracttemplatefields]: .properties file assigning JSON to worksheets
```